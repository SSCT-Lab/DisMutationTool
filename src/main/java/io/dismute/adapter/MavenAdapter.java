package io.dismute.adapter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.singleton.PropertiesFile;
import io.dismute.utils.CommandLineUtil;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class MavenAdapter extends BuildToolAdapter {

    private static final Logger logger = LogManager.getLogger(MavenAdapter.class);
    private static final int EXECUTION_TIMEOUT = Integer.parseInt(PropertiesFile.getInstance().getProperty("execution.timeout.seconds"));

    @Override
    public void cleanAndGenerateClassPath() {
        logger.info("MavenAdapter cleanAndGenerateClassPath");
        Project project = Project.getInstance();
        int retCode1 = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "clean", "install", "-DskipTests");
        if (retCode1 != 0) {
            logger.error("Error while cleaning and generating classpath");
            throw new RuntimeException("Error while cleaning and generating classpath");
        }
        int retCode2 = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "dependency:build-classpath", "-Dmdep.outputFile=" + project.getClasspathTxtPath());
        if (retCode2 != 0) {
            logger.error("Error while generating classpath file");
            throw new RuntimeException("Error while generating classpath file");
        }
        // 保险起见，将.class文件的父目录加入classpath（追加写入）
        String commonPathForClassFiles = project.getBasePath() + File.separator + "target" + File.separator + "classes" + File.separator; // 多个module的情况 ？ 已在BuildToolAdapter中将项目根目录和classpath.txt路径拼接
        FileUtil.appendStringToFile(project.getClasspathTxtPath(), ":" + commonPathForClassFiles);
        logger.info("Classpath.txt generated successfully. Path: {}", project.getClasspathTxtPath());
    }


    @Override
    public int clean() {
        logger.info("MavenAdapter clean");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "clean");
        if (exitCode != 0) {
            logger.error("Error while run mvn clean!");
            throw new RuntimeException("Error while run mvn clean!");
        }
        return exitCode;
    }

    @Override
    public int compilation() {
        logger.info("MavenAdapter compilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "compile");
        if (exitCode != 0) {
            logger.error("Error while run mvn compile!");
        }
        return exitCode;
    }

    @Override
    public int cleanAndCompilation() {
        logger.info("MavenAdapter cleanAndCompilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "clean", "compile");
        if (exitCode != 0) {
            logger.error("Error while run mvn clean compile!");
        }
        return exitCode;
    }


    // 注：该方法不负责装载变异体
    @Override
    public int testExecution(Mutant mutant, String... args) {
        // 构造ProcessBuilder执行命令
        Project project = Project.getInstance();
        File mvnLogFile = new File(project.getTestOutputsPath() + File.separator + FileUtil.getNameWithoutExtension(mutant.getMutatedPath()) + ".log");  // mvn 日志存放在输出文件夹的testOutputs子文件夹中
        String coveredTestCases = "";
        if(Project.getInstance().isCoverage()) { // 如果开启了覆盖率测试，如果当前变异体没有覆盖的测试用例，则跳过测试
            coveredTestCases = mutant.getCoveredTestCases().isEmpty() ? "-DskipTests" : "-Dtest=" + String.join(",", mutant.getCoveredTestCases()); // 如果开启了覆盖率测试， -Dtest=TestClass1,TestClass2
        }
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputsToFileWithTimeout
                (project.getBasePath(), mvnLogFile.getAbsolutePath(), EXECUTION_TIMEOUT, "mvn", "clean", "test", coveredTestCases, "-DfailIfNoTests=false", String.join(" ", args));
        logger.info("Test process for mutant {} exited with code {}", mutant.getMutatedName(), exitCode);
        return exitCode;
    }




//    @Override
//    public int testExecution(Mutant mutant, String... args) {
//        // 生成mutant对应的mvn log file，构造ProcessBuilder执行命令
//        logger.info(LogUtil.centerWithSeparator("TEST STARTED for mvn mutant {}" + mutant.getMutatedName()));
//        logger.info("MavenAdapter testExecution for mutant: {}", mutant);
//        Project project = Project.getInstance();
//        File mvnLogFile = new File(project.getTestOutputsPath() + File.separator + FileUtil.getNameWithoutExtension(mutant.getMutatedPath()) + ".log");  // mvn 日志存放在输出文件夹的testOutputs子文件夹中
//        String coveredTestCases = mutant.getCoveredTestCases().isEmpty() ? "" : "-DTest" + String.join(",", mutant.getCoveredTestCases()); // 如果开启了覆盖率测试， -Dtest=TestClass1,TestClass2
//        logger.info("Mutant name {}. Output file path: {}", mutant.getMutatedName(), mvnLogFile.getAbsolutePath());
//        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputsToFileWithTimeout(project.getBasePath(), mvnLogFile.getAbsolutePath(), EXECUTION_TIMEOUT, "mvn", "clean", "test", coveredTestCases, String.join(" ", args));
//        logger.info("Test process for mutant {} exited with code {}", mutant.getMutatedName(), exitCode);
//        return exitCode;
//    }
}
