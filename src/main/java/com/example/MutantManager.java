package com.example;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutatorType;
import com.example.mutantGen.MutatorFactory;
import com.example.utils.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

public class MutantManager {

    private static final Logger logger = LogManager.getLogger(MutantManager.class);

    private final Project project;
    private final Set<MutatorType> mutatorSet;
    private List<Mutant> mutantLs = new ArrayList<>();
    private Map<String, Map<MutatorType, List<Mutant>>> mutantMap = new HashMap<>(); // key: originalPath, value: [key: mutatorType, value: mutants]
    public static Builder builder() {
        return new Builder();
    }

    private MutantManager(Project project, Set<MutatorType> mutatorSet) {
        this.project = project;
        this.mutatorSet = mutatorSet;
    }

    /**
     * 为项目生成所有变异体
     */
    public void generateMutants() {
        logger.info("Generate mutants for project: " + project.getBasePath() + " ...");
        List<String> srcFileLs = project.getSrcFileLs();
        for (String srcFile : srcFileLs) {
            for (MutatorType mutator : mutatorSet) {
                List<Mutant> mutants = MutatorFactory.getMutator(mutator).execute(srcFile);

                // 处理内容完全相同的变异体
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

                // 生成Map，统计变异体数量
                mutantLs.addAll(mutants);
                for (Mutant mutant : mutants) {
                    if (!mutantMap.containsKey(mutant.getOriginalPath())) {
                        mutantMap.put(mutant.getOriginalPath(), new HashMap<>());
                    }
                    if (!mutantMap.get(mutant.getOriginalPath()).containsKey(mutant.getMutatorType())) {
                        mutantMap.get(mutant.getOriginalPath()).put(mutant.getMutatorType(), new ArrayList<>());
                    }
                    mutantMap.get(mutant.getOriginalPath()).get(mutant.getMutatorType()).add(mutant);
                }
            }
        }
        // 打印mutant统计信息
        logger.info("Generate completed");
        logger.info("Total mutants: " + mutantLs.size());
        for (String srcFile : mutantMap.keySet()) {
            logger.info("File: " + srcFile);
            for (MutatorType mutator : mutantMap.get(srcFile).keySet()) {
                logger.info("\tMutantGen: " + mutator + " count: " + mutantMap.get(srcFile).get(mutator).size());
            }
        }
    }

    /**
     * 注意，平时不用此方法
     * 补做实验，传入上一轮意外失败的突变体文件名称列表
     * 仅重新生成这些突变体，然后运行test suite
     *
     * @param fileNameSet 要重新突变的文件名称列表
     */
    public void generateMutants(Set<String> fileNameSet) {
        logger.info("Generate mutants for project: " + project.getBasePath() + " ...");
        List<String> srcFileLs = project.getSrcFileLs();
        for (String srcFile : srcFileLs) {
            if (!fileNameSet.contains(FileUtil.getFileName(srcFile) + ".java")) {
                continue;
            }
            for (MutatorType mutator : mutatorSet) {
                logger.info("Generate mutants for file: " + srcFile + " with mutator: " + mutator);
                List<Mutant> mutants = MutatorFactory.getMutator(mutator).execute(srcFile);
                mutantLs.addAll(mutants);
                for (Mutant mutant : mutants) {
                    if (!mutantMap.containsKey(mutant.getOriginalPath())) {
                        mutantMap.put(mutant.getOriginalPath(), new HashMap<>());
                    }
                    if (!mutantMap.get(mutant.getOriginalPath()).containsKey(mutant.getMutatorType())) {
                        mutantMap.get(mutant.getOriginalPath()).put(mutant.getMutatorType(), new ArrayList<>());
                    }
                    mutantMap.get(mutant.getOriginalPath()).get(mutant.getMutatorType()).add(mutant);
                }
            }
        }
        // 打印mutant统计信息
        logger.info("Generate completed");
        logger.info("Total mutants: " + mutantLs.size());
        for (String srcFile : mutantMap.keySet()) {
            logger.info("File: " + srcFile);
            for (MutatorType mutator : mutantMap.get(srcFile).keySet()) {
                logger.info("\tMutantGen: " + mutator + " count: " + mutantMap.get(srcFile).get(mutator).size());
            }
        }
    }

    public List<Mutant> getMutantLs() {
        return mutantLs;
    }

    public Map<String, Map<MutatorType, List<Mutant>>> getMutantMap() {
        return mutantMap;
    }

    public Project getProject() {
        return project;
    }

    public Set<MutatorType> getMutatorSet() {
        return mutatorSet;
    }

    static class Builder {
        private Project project;
        private Set<MutatorType> mutatorSet = new HashSet<>();

        public Builder setProject(Project project) {
            this.project = project;
            return this;
        }

        public Builder setMutator(MutatorType mutator) {
            this.mutatorSet.add(mutator);
            return this;
        }

        public MutantManager build() {
            return new MutantManager(project, mutatorSet);
        }


    }
}
