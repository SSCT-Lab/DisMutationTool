package com.example.mutantgen;

import com.example.MutantManager;
import com.example.Project;
import com.example.mutator.Mutant;
import com.example.mutator.MutatorFactory;
import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class MutantGenerator {
    private final Project project;
    private static final Logger logger = LogManager.getLogger(MutantManager.class);


    public MutantGenerator(Project project) {
        this.project = project;
    }

    public void generateMutants() {
        List<Mutant> mutants = new ArrayList<>();
        List<MutatorType> mutatorTypes = project.getMutators();
        if (mutatorTypes.isEmpty()) { // 说明全量运行，加入所有算子
            mutatorTypes.addAll(Arrays.asList(MutatorType.values()));
        }
        // 生成所有变异体
        List<String> srcFileLs = project.getSrcFileLs();
        for (String srcFile : srcFileLs) {
            for (MutatorType mutator : mutatorTypes) {
                mutants.addAll(MutatorFactory.getMutator(mutator).execute(srcFile));
            }
        }

        mutants = deleteIdenticalMutants(mutants);

        // 收集原始字节码
        EquivalentMutantFilter equivalentMutantFilter = new EquivalentMutantFilter(project);
        equivalentMutantFilter.filterMutants();
    }

    private List<Mutant> deleteIdenticalMutants(List<Mutant> mutants) {
        Set<String> fileToDelete = new HashSet<>();
        for (int i = 0; i < mutants.size(); i++) {
            for (int j = i + 1; j < mutants.size(); j++) {
                String path1 = mutants.get(i).getMutatedPath();
                String path2 = mutants.get(j).getMutatedPath();
                if (FileUtil.isFileIdentical(path1, path2)) {
                    fileToDelete.add(path2);
                }
            }
        }
        // 删除对应的mutant和文件
        for (String path : fileToDelete) {
            mutants.removeIf(mutant -> mutant.getMutatedPath().equals(path));
            try {
                logger.info("Removing identical mutant: " + path);
                FileUtils.delete(new File(path));
            } catch (IOException e){
                throw new RuntimeException("Failed to delete file: " + path);
            }
        }
        return mutants;
    }
}
