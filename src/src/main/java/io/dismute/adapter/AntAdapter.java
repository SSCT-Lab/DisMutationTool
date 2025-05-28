package io.dismute.adapter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.utils.CommandLineUtil;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class AntAdapter extends BuildToolAdapter {

    private static final Logger logger = LogManager.getLogger(AntAdapter.class);



    @Override
    public void cleanAndGenerateClassPath() {
        logger.info("AntAdapter cleanAndGenerateClassPath");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "ant", "clean", "jar");
        if (exitCode != 0) {
            logger.error("Error while cleaning and generating classpath");
            throw new RuntimeException("Error while cleaning and generating classpath");
        }
        String BASE_PATH = project.getBasePath() + File.separator;
//        List<String> classDirs = executeFind(BASE_PATH + "build", "*.class");
//        List<String> jarFiles = executeFind(BASE_PATH + "build", "*.jar");
//        jarFiles.addAll(executeFind(BASE_PATH + "lib", "*.jar"));
        List<String> classDirs = FileUtil.getFilesBasedOnPattern(BASE_PATH + "build", ".*\\.class");
        List<String> jarFiles = FileUtil.getFilesBasedOnPattern(BASE_PATH + "build", ".*\\.jar");
        jarFiles.addAll(FileUtil.getFilesBasedOnPattern(BASE_PATH + "lib", ".*\\.jar"));

        // String classpath = String.join(":", classDirs) + ":" + String.join(":", jarFiles);
        // update: classpath瘦身，classDirs提取公共目录的绝对路径
        String commonPathForClassFiles = BASE_PATH + "build" + File.separator;
        String classpath = String.join(":", jarFiles) + ":" + commonPathForClassFiles;
        FileUtil.writeToFile(classpath, project.getClasspathTxtPath());
    }

    @Override
    public int clean() {
        logger.info("AntAdapter clean");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "ant", "clean");
        if (exitCode != 0) {
            logger.error("Error while run ant clean!");
            throw new RuntimeException("Error while run ant clean!");
        }
        return exitCode;
    }

    @Override
    public int compilation() {
        logger.info("AntAdapter compilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "ant", "compile");
        if (exitCode != 0) {
            logger.error("Error while run ant compile!");
        }
        return exitCode;
    }

    @Override
    public int cleanAndCompilation() {
        logger.info("AntAdapter cleanAndCompilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "ant", "clean", "build");
        if (exitCode != 0) {
            logger.error("Error while run ant clean compile!");
            throw new RuntimeException("Error while run ant clean compile!");
        }
        return exitCode;
    }

    @Override
    public int testExecution(Mutant mutant, String... args) {
        Project project = Project.getInstance();
        File logFile = new File(project.getTestOutputsPath() + File.separator + FileUtil.getNameWithoutExtension(mutant.getMutatedPath()) + ".log");  // mvn 日志存放在输出文件夹的testOutputs子文件夹中
        String coveredTestCases = "";
        if(Project.getInstance().isCoverage()) { // 如果开启了覆盖率测试，如果当前变异体没有覆盖的测试用例，则跳过测试
            if(mutant.getCoveredTestCases().isEmpty()) {
                coveredTestCases ="-Dtest.class=none";
            } else {
                coveredTestCases = "testSome -Dtest.name=" + String.join(",", mutant.getCoveredTestCases());
            }
        }
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputsToFileWithTimeout
                (project.getBasePath(), logFile.getAbsolutePath(), EXECUTION_TIMEOUT, "ant", "clean", "test", coveredTestCases, String.join(" ", args));
        logger.info("Test process for mutant {} exited with code {}", mutant.getMutatedName(), exitCode);
        return exitCode;
    }


    /**
     * 执行 find 命令以获取文件列表
     */
    private static List<String> executeFind(String directory, String pattern)  {
        try {
            ProcessBuilder pb = new ProcessBuilder("find", directory, "-name", pattern);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            List<String> results = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    results.add(line.trim());
                }
            }
            process.waitFor();
            // 打印results
//            for (String result : results) {
//                logger.info(result);
//            }
            return results;
        } catch (InterruptedException e) {
            logger.error("Command interrupted", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("Failed to start process", e);
            throw new RuntimeException(e);
        }

    }

}
