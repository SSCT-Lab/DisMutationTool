package com.example.mutantgen;

import com.example.Project;
import com.example.mutator.Mutant;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import com.example.utils.MutantUtil;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EquivalentMutantFilter {

    Logger logger = LogManager.getLogger(EquivalentMutantFilter.class);

    private final Project project;

    public EquivalentMutantFilter(Project project) {
        this.project = project;
    }

    List<Mutant> filter(List<Mutant> mutants) {
        boolean originalCompileSuccess = compileSource();
        if (!originalCompileSuccess) {
            logger.error("original code compile failed");
            throw new RuntimeException("original code compile failed");
        }
        copyOriginalBytecode();
        return compileAndCompareEachMutant(mutants);
    }



    // 将srcPath中.class文件复制到tarPath中
    private void copyOriginalBytecode() {
        String srcPath = project.getBuildOutputPath();
        // String tarPath = Config.ORIGINAL_BYTECODE_PATH;
        String tarPath = project.ORIGINAL_BYTECODE_PATH;
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
            // 创建文件夹，以保存变异体编译后的字节码
            String mutantBytecodeDir = Project.MUTANT_BYTECODE_PATH + "/" +  FileUtil.getFileName(mutant.getOriginalPath());
            File mutantBytecodeDirFile = new File(mutantBytecodeDir);
            if (!mutantBytecodeDirFile.exists()) {
                mutantBytecodeDirFile.mkdirs();
            }
            
            // 编译装载后的代码
            boolean compileSuccess = compileSource();
            if (!compileSuccess) { // 编译失败，保留变异体，跳过后续步骤，之后运行会保存编译失败的文件
                logger.error("BUILD FAILED FOR MUTANT: " + mutant.getMutatedPath());
                MutantUtil.unloadMutant(mutant);
                continue;
            } else {
                logger.info("BUILD SUCCESS FOR MUTANT: " + mutant.getMutatedPath());
            }
            // 获取变异体的文件名前缀，匹配字节码文件
            String namePrefix = FileUtil.getFileName(mutant.getOriginalPath());
            logger.info("Searching bytecode files for" + FileUtil.getFileName(mutant.getOriginalPath()) + ".java");

            String pattern = "^" + Pattern.quote(namePrefix) + "(?:\\$[^.]+)?\\.class$";

            List<String> originalBytecodeFiles = FileUtil.getFilesBasedOnPattern(Project.ORIGINAL_BYTECODE_PATH, pattern);
            List<String> mutatedBytecodeFiles = FileUtil.getFilesBasedOnPattern(project.getBuildOutputPath(), pattern);
            Collections.sort(originalBytecodeFiles);
            Collections.sort(mutatedBytecodeFiles);
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
                logger.info("Equivalent mutant found: " + FileUtil.getFileName(mutant.getMutatedPath()) + " is equivalent to original code");
                toDelete.add(i);
            }
            // 将变异体的字节码文件复制到mutantBytecodeDir中
            for (String mutatedBytecodeFile : mutatedBytecodeFiles) {
                String fileName = FileUtil.getFileName(mutatedBytecodeFile) + ".class";
                FileUtil.copyFileToTargetDir(mutatedBytecodeFile, mutantBytecodeDir, fileName);
            }

            // 撤销装载
            MutantUtil.unloadMutant(mutant);
        }
        // 删除等价变异体
        for(int i: toDelete){
            String path = mutants.get(i).getMutatedPath();
            try {
                logger.info("Removing equivalent mutant: " + mutants.get(i).getMutatedPath());
                FileUtils.delete(new File(path));
            } catch (IOException e){
                throw new RuntimeException("Failed to delete file: " + path);
            }
        }

        // 更新结果
        List<Mutant> res = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            if (!toDelete.contains(i)) {
                res.add(mutants.get(i));
            }
        }
        return res;
    }




    private boolean compileSource() {
        if (project.getProjectType() == Project.ProjectType.MAVEN) {
            InvocationRequest request = new DefaultInvocationRequest();
            String pomPath = project.getBasePath() + "/pom.xml";
            request.setPomFile(new File(pomPath));
            request.setGoals(Collections.singletonList("clean compile"));
            Invoker invoker = new DefaultInvoker();
            // 设置自定义输出处理器
            invoker.setOutputHandler(new CustomOutputHandler());
            try {
                InvocationResult result = invoker.execute(request);
                if (result.getExitCode() != 0) { // 编译失败
                    // result.getExecutionException().printStackTrace();
                    return false;
                }
                return true;
            } catch (MavenInvocationException e) {
                e.printStackTrace();
                throw new RuntimeException("build failed without exit code");
            }
        } else {
            String buildFilePath = project.getBasePath() + "/build.xml";
            try {
                // 获取build.xml文件的目录
                File buildFile = new File(buildFilePath);
                File directory = buildFile.getParentFile();

                // 创建命令
                String[] command = {"ant", "clean", "build"};

                // 创建ProcessBuilder
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(directory);
                processBuilder.redirectErrorStream(true);

                // 启动进程
                Process process = processBuilder.start();

                // 读取输出
                Thread outputThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // 启动输出线程
                outputThread.start();

                // 等待进程结束
                int exitCode = process.waitFor();
                outputThread.join();  // 等待输出线程结束

                // 返回输出和状态
                return exitCode == 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }


    public class CustomOutputHandler implements InvocationOutputHandler {
        @Override
        public void consumeLine(String s) throws IOException {

        }
    }


}
