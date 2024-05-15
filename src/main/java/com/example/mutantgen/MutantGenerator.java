package com.example.mutantgen;

import com.example.MutantManager;
import com.example.Project;
import com.example.mutator.Mutant;
import com.example.mutator.MutatorFactory;
import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class MutantGenerator {
    private static final Logger logger = LogManager.getLogger(MutantGenerator.class);

    private final Project project;
    private final Set<MutatorType> mutatorSet;
    private List<Mutant> mutants = new ArrayList<>();
    private final Map<String, Map<MutatorType, List<Mutant>>> mutantMap = new HashMap<>(); // key: originalPath, value: [key: mutatorType, value: mutants]


    public MutantGenerator(Project project) {
        this.project = project;
        this.mutatorSet = project.getMutators();
    }

    public List<Mutant> generateMutants(){
        return generateMutants(false);
    }

    public List<Mutant> generateMutantsWithoutFilterEq() {
        return generateMutants(true);
    }


    public List<Mutant> generateMutants(boolean skipBytecodeComp) {
        logger.info("\n\nPHASE: Generate initial mutants for project: " + project.getBasePath() + " ...\n\n");
        // 生成所有变异体
        List<String> srcFileLs = project.getSrcFileLs();
        for (String srcFile : srcFileLs) {
            for (MutatorType mutator : mutatorSet) {
                mutants.addAll(MutatorFactory.getMutator(mutator).execute(srcFile));
            }
        }

//        // 去重
//        mutants = new ArrayList<>(new HashSet<>(mutants));

        // 删除内容相同的变异体
        logger.info("\n\nPHASE: Removing identical mutants...\n\n");
        deleteIdenticalMutants();

        if(!skipBytecodeComp){
            // 删除等价变异体
            logger.info("\n\nPHASE: Removing equivalent mutants...\n\n");
            EquivalentMutantFilter equivalentMutantFilter = new EquivalentMutantFilter(project);
            mutants = equivalentMutantFilter.filterMutants(mutants);
        }

        // 统计变异体信息
        for (Mutant mutant : mutants) {
            if (!mutantMap.containsKey(mutant.getOriginalPath())) {
                mutantMap.put(mutant.getOriginalPath(), new HashMap<>());
            }
            if (!mutantMap.get(mutant.getOriginalPath()).containsKey(mutant.getMutatorType())) {
                mutantMap.get(mutant.getOriginalPath()).put(mutant.getMutatorType(), new ArrayList<>());
            }
            mutantMap.get(mutant.getOriginalPath()).get(mutant.getMutatorType()).add(mutant);
        }

        // 打印mutant统计信息
        logger.info("Generate completed");
        logger.info("Total mutants: " + mutants.size());
        for (String srcFile : mutantMap.keySet()) {
            logger.info("File: " + srcFile);
            for (MutatorType mutator : mutantMap.get(srcFile).keySet()) {
                logger.info("\t" + mutator + " count: " + mutantMap.get(srcFile).get(mutator).size());
            }
        }

        return mutants;
    }

    private void deleteIdenticalMutants() {
        Set<String> fileToDelete = new HashSet<>();
        for (int i = 0; i < mutants.size(); i++) {
            String path1 = mutants.get(i).getMutatedPath();
            for (int j = i + 1; j < mutants.size(); j++) {
                String path2 = mutants.get(j).getMutatedPath();
                if (FileUtil.isFileIdentical(path1, path2)) {
                    logger.info("Identical mutants FOUND: " + path1 + " and " + path2);
                    if (fileToDelete.contains(path1)) {
                        continue;
                    }
                    fileToDelete.add(path2);
                }
            }
        }
        // 删除对应的mutant和文件
        List<Mutant> newMutants = new ArrayList<>();
        for (Mutant mutant : mutants) {
            if (fileToDelete.contains(mutant.getMutatedPath())) {
                try {
                    logger.info("Deleting identical mutant: " + mutant.getMutatedPath());
                    FileUtils.delete(new File(mutant.getMutatedPath()));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file: " + mutant.getMutatedPath());
                }
            } else {
                newMutants.add(mutant);
            }
        }
        mutants = newMutants;
    }
}
