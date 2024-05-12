package com.example.mutantgen;

import com.example.Project;
import com.example.mutator.Mutant;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import com.example.utils.MutantUtil;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EquivalentMutantFilter {

    Logger logger = LogManager.getLogger(EquivalentMutantFilter.class);

    private final Project project;

    public EquivalentMutantFilter(Project project) {
        this.project = project;
    }

    public List<Mutant> filterMutants(List<Mutant> mutants) {
        boolean originalCompileSuccess = compileSource();
        if (!originalCompileSuccess) {
            logger.error("original code compile failed");
            throw new RuntimeException("original code compile failed");
        }
        copyOriginalBytecode();
        return compileAndCompareEachMutant(mutants);
    }

    private boolean compileSource() {
        InvocationRequest request = new DefaultInvocationRequest();
        if (project.getProjectType() == Project.ProjectType.MAVEN) {
            String pomPath = project.getBasePath() + "/pom.xml";
            logger.info("pomPath: " + pomPath);
            request.setPomFile(new File(pomPath));
            request.setGoals(Collections.singletonList("clean compile"));
            Invoker invoker = new DefaultInvoker();
            try {
                InvocationResult result = invoker.execute(request);
                if (result.getExitCode() != 0) { // 编译失败
                    result.getExecutionException().printStackTrace();
                    return false;
                }
                return true;
            } catch (MavenInvocationException e) {
                e.printStackTrace();
                throw new RuntimeException("build failed");
            }
        } else {
            // TODO ant?
        }
        return true;
    }

    // 将srcPath中.class文件复制到tarPath中
    private void copyOriginalBytecode() {
        String srcPath = project.getBuildOutputPath();
        String tarPath = Config.ORIGINAL_BYTECODE_PATH;
        List<String> classFiles = FileUtil.getFilesBasedOnPattern(srcPath, ".*\\.class");
        for (String classFile : classFiles) {
            String fileName = FileUtil.getFileName(classFile) + ".class";
            FileUtil.copyFileToTargetDir(classFile, tarPath, fileName);
        }
    }

    // compile each mutant, extract bytecode with filename$ or filename.class
    // find them in originalBytecode, and compare those files

    private List<Mutant> compileAndCompareEachMutant(List<Mutant> mutants) {
        List<Integer> toDelete = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            Mutant mutant = mutants.get(i);
            // 装载变异体
            MutantUtil.loadMutant(mutant);
            // 编译装载后的代码
            boolean compileSuccess = compileSource();
            if (!compileSuccess) { // 编译失败，保留变异体，跳过后续步骤，之后运行会保存编译失败的文件
                logger.error("mutant compile failed: " + mutant.getMutatedPath());
                MutantUtil.unloadMutant(mutant);
                continue;
            }
            // 获取变异体的文件名前缀，匹配字节码文件
            String namePrefix = FileUtil.getFileName(mutant.getOriginalPath());
            logger.info("Searching bytecode files for" + FileUtil.getFileName(mutant.getOriginalPath()) + ".java");

            String pattern = ".*" + Pattern.quote(namePrefix) + "(?:\\$[^.]+)?\\.class$";

            List<String> originalBytecodeFiles = FileUtil.getFilesBasedOnPattern(Config.ORIGINAL_BYTECODE_PATH, pattern).stream().sorted().collect(Collectors.toList());
            List<String> mutatedBytecodeFiles = FileUtil.getFilesBasedOnPattern(project.getBuildOutputPath(), pattern).stream().sorted().collect(Collectors.toList());
            for (String filename : originalBytecodeFiles) {
                logger.info("\t" + FileUtil.getFileName(filename) + ".class");
            }
            // 比较两个list中，同名文件字节码是否相同
            boolean isEquivalent = true;
            for (int j = 0; j < originalBytecodeFiles.size(); j++) {
                String originalBytecodeFile = originalBytecodeFiles.get(j);
                String mutatedBytecodeFile = mutatedBytecodeFiles.get(j);
                if (!FileUtil.getFileName(originalBytecodeFile).equals(FileUtil.getFileName(mutatedBytecodeFile))) {
                    throw new RuntimeException("Bytecode files not corresponding: " + originalBytecodeFile + " " + mutatedBytecodeFile);
                }
                logger.info("Comparing " + (originalBytecodeFile) + " and " + (mutatedBytecodeFile));
                if (!FileUtil.isFileIdentical(originalBytecodeFile, mutatedBytecodeFile)) {
                    logger.info("Mutant " + FileUtil.getFileName(mutant.getMutatedPath()) + " is not equivalent to original code");
                    isEquivalent = false;
                    break;
                }
            }
            if (isEquivalent) {
                toDelete.add(i);
            }
            // 撤销装载
            MutantUtil.unloadMutant(mutant);
        }
        // 删除等价变异体
        List<Mutant> res = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            if (!toDelete.contains(i)) {
                res.add(mutants.get(i));
            }
        }
        return mutants;
    }


}
