package com.example.mutantFilter;

import com.example.Project;
import com.example.mutator.Mutant;
import com.example.utils.FileUtil;
import com.example.utils.MutantUtil;
import lombok.extern.flogger.Flogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BytecodeFilter {
    private static Logger logger = LogManager.getLogger(BytecodeFilter.class);
    private Project project;
    private List<Mutant> mutants;

    public BytecodeFilter(Project project, List<Mutant> mutants) {
        this.project = project;
        this.mutants = mutants;
    }

    public List<Mutant> filter() {
        originalBuild();
        for (Mutant mutant : mutants) {
            compileMutant(mutant);
        }
        List<Mutant> filteredMutants = filterMutants();
        saveToMutantsFilteredDir(filteredMutants);
        return filteredMutants;
    }

    // 编译原始代码
    private void originalBuild() {
        logger.info("Build original bytecode");
        boolean originalCompileSuccess = compileSource();
        if (!originalCompileSuccess) {
            logger.error("original code compile failed");
            throw new RuntimeException("original code compile failed");
        }
        String srcPath = project.getBasePath();
        String tarPath = Project.ORIGINAL_BYTECODE_PATH;
        List<String> classFiles = FileUtil.getFilesBasedOnPattern(srcPath, ".*\\.class");
        logger.info("Copying classes from " + srcPath + " to " + tarPath);
        for (String classFile : classFiles) {
            String fileName = FileUtil.getFileName(classFile) + ".class";
            String filePath = FileUtil.getFileDir(classFile);
            if (filePath == null || !filePath.startsWith(project.getBasePath())) {
                throw new RuntimeException("Class file path does not start with " + project.getBasePath());
            }
            filePath = filePath.replace(project.getBasePath(), Project.ORIGINAL_BYTECODE_PATH);
            // logger.info("Copying " + classFile + " to " + filePath);
            FileUtil.copyFileToTargetDir(classFile, filePath, fileName);
        }
    }


    private void compileMutant(Mutant mutant) {
        // 装载变异体
        MutantUtil.loadMutant(mutant);
        // 创建文件夹，以保存变异体编译后的字节码
        String mutantBytecodeDir = getMutantBytecodeDir(mutant);
        FileUtil.createDirIfNotExist(mutantBytecodeDir);
        logger.info("Building mutant: " + mutant.getMutatedPath());
        boolean compileSuccess = compileSource();
        if (!compileSuccess) { // 编译失败，撤销装载
            logger.error("BUILD FAILED FOR MUTANT: " + mutant.getMutatedPath());
            MutantUtil.unloadMutant(mutant);
            return;
        }

        // 获取变异体的文件名前缀，匹配字节码文件
        logger.info("BUILD SUCCESS FOR MUTANT: {}", mutant.getMutatedPath());
        String namePrefix = FileUtil.getFileName(mutant.getOriginalPath());
        logger.info("Searching bytecode files for" + FileUtil.getFileName(mutant.getOriginalPath()) + ".java");
        String pattern = "^" + Pattern.quote(namePrefix) + "(?:\\$[^.]+)?\\.class$";
        List<String> mutatedBytecodeFiles = FileUtil.getFilesBasedOnPattern(project.getBasePath(), pattern);

        // 将这些文件拷贝到 mutantBytecodeDir
        for (String classFile : mutatedBytecodeFiles) {
            String fileName = FileUtil.getFileName(classFile) + ".class";
            String originalFilePath = FileUtil.getFileDir(classFile);
            if (classFile == null) {
                throw new RuntimeException("Class file path does not start with " + project.getBasePath());
            }
            String filePath = originalFilePath.replace(project.getBasePath(), mutantBytecodeDir); //这些文件应该有独立的命名空间，否则相同名称的.class文件会被覆盖
            FileUtil.copyFileToTargetDir(classFile, filePath, fileName);
        }

        // 撤销装载
        MutantUtil.unloadMutant(mutant);
    }


    private boolean compileSource() {
        try {
            File directory = new File(project.getBasePath());

            // 创建命令
            String[] command = getBuildCmd();

            // 创建ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(directory);
            processBuilder.redirectErrorStream(true);

            // 启动进程
            Process process = processBuilder.start();

            // 创建一个线程来读取输出流
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                         System.out.println(line);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待进程结束
            int exitCode = process.waitFor();

            // 返回输出和状态
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String[] getBuildCmd() {
        if (this.project.getProjectType() == Project.ProjectType.MAVEN) {
            return new String[]{"mvn", "compile"};
        } else if (this.project.getProjectType() == Project.ProjectType.GRADLE) {
            // /gradlew clean compileJava
            return new String[]{"./gradlew", "clean", "compileJava"};
        } else if (this.project.getProjectType() == Project.ProjectType.ANT) {
            return new String[]{"ant", "clean", "build"};
        }
        return new String[]{""};
    }

    private List<Mutant> filterMutants() {
        logger.info("Start filtering mutants");
        List<Mutant> res = new ArrayList<>();
        for (Mutant mutant : mutants) {
            logger.info("Filtering mutant for bytecode{}", mutant.getMutatedPath());
            if (isMutantCompileFailed(mutant)) { // 编译失败的变异体直接添加
                res.add(mutant);
                continue;
            }
            if (isBytecodeIdenticalWithOriginal(mutant)) continue;
            if (isBytecodeIdenticalWithOtherMutants(mutant, res)) continue;
            logger.info("Mutant {} is not equal to others", mutant.getMutatedPath());
            res.add(mutant);
        }
        return res;
    }

    private boolean isMutantCompileFailed(Mutant mutant) {
        List<String> mutatedBytecodeFiles = FileUtil.getFilesBasedOnPattern(getMutantBytecodeDir(mutant), ".*\\.class$");
        if (mutatedBytecodeFiles.isEmpty()) {
            logger.info("Skipping bytecode comparison for COMPILE FAILED mutant {}", FileUtil.getFileName(mutant.getOriginalPath()));
            return true;
        }
        return false;
    }

    private boolean isBytecodeIdenticalWithOriginal(Mutant mutant) {
        List<String> mutatedBytecodeFiles = FileUtil.getFilesBasedOnPattern(getMutantBytecodeDir(mutant), ".*\\.class$");
        String namePrefix = FileUtil.getFileName(mutant.getOriginalPath()); // 变异体文件名前缀，即.java文件名
        logger.info("Search in original bytecode files for" + namePrefix);
        String pattern = "^" + Pattern.quote(namePrefix) + "(?:\\$[^.]+)?\\.class$";
        List<String> originalBytecodeFiles = FileUtil.getFilesBasedOnPattern(Project.ORIGINAL_BYTECODE_PATH, pattern);
        Collections.sort(originalBytecodeFiles);
        Collections.sort(mutatedBytecodeFiles);
        for (String filename : originalBytecodeFiles) {
            logger.info("\t\t" + FileUtil.getFileName(filename) + ".class");
        }
        if (originalBytecodeFiles.size() != mutatedBytecodeFiles.size()) {
            throw new RuntimeException("Bytecode files's size not corresponding");
        }
        // 比较两个list中，同名文件字节码是否相同
        for (int j = 0; j < originalBytecodeFiles.size(); j++) {
            String originalBytecodeFile = originalBytecodeFiles.get(j);
            String mutatedBytecodeFile = mutatedBytecodeFiles.get(j);
            logger.info("Comparing {} to ORIGINAL {}", mutatedBytecodeFile, originalBytecodeFile);
            if (!FileUtil.getFileName(originalBytecodeFile).equals(FileUtil.getFileName(mutatedBytecodeFile))) {
                throw new RuntimeException("Bytecode files not corresponding: " + originalBytecodeFile + " " + mutatedBytecodeFile);
            }
            if (!FileUtil.isFileIdentical(originalBytecodeFile, mutatedBytecodeFile)) {
                return false;
            }
        }
        logger.info("Equivalent mutant found: " + FileUtil.getFileName(mutant.getMutatedPath()) + " is equivalent to original code");
        return true;
    }

    private boolean isBytecodeIdenticalWithOtherMutants(Mutant mutant, List<Mutant> others) {
        for (Mutant otherMutant : others) {
            if (isBytecodeIdenticalBetweenTwoMutants(mutant, otherMutant)) return true;
        }
        return false;
    }

    private boolean isBytecodeIdenticalBetweenTwoMutants(Mutant m1, Mutant m2) {
        logger.info("Comparing bytecode between {} and {}", FileUtil.getFileName(m1.getMutatedPath()), FileUtil.getFileName(m2.getMutatedPath()));
        List<String> m1Files = FileUtil.getFilesBasedOnPattern(getMutantBytecodeDir(m1), ".*\\.class$");
        List<String> m2Files = FileUtil.getFilesBasedOnPattern(getMutantBytecodeDir(m2), ".*\\.class$");
        Collections.sort(m1Files);
        Collections.sort(m2Files);
        if (m1Files.isEmpty() || m2Files.isEmpty()) return false;
        if (m1Files.size() != m2Files.size()) return false;
        for (int i = 0; i < m1Files.size(); i++) {
            String f1 = m1Files.get(i);
            String f2 = m2Files.get(i);
            if (!f1.equals(f2)) return false;
            logger.info("Comparing {} to {}", m1Files.get(i), m2Files.get(i));
            if (!FileUtil.isFileIdentical(f1, f2)) return false;
        }
        logger.info("Equivalent mutant found: {} is equivalent to {}", FileUtil.getFileName(m1.getMutatedPath()), FileUtil.getFileName(m2.getMutatedPath()));
        return true;
    }

    private static void saveToMutantsFilteredDir(List<Mutant> mutants) {
        for (Mutant mutant : mutants) {
            String mutantsFilteredDir = Project.MUTANT_FILTERED_PATH;
            String mutantFileName = FileUtil.getFileName(mutant.getOriginalPath()) + ".java";
            logger.info("saving mutant {} to {}", mutantFileName, mutantsFilteredDir);
            FileUtil.copyFileToTargetDir(mutant.getMutatedPath(), mutantsFilteredDir, mutantFileName);
        }
    }

    private static String getMutantBytecodeDir(Mutant mutant) {
        return Project.MUTANT_BYTECODE_PATH + "/" + FileUtil.getFileName(mutant.getMutatedPath());
    }
}
