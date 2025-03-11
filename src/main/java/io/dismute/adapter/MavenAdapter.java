package io.dismute.adapter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.singleton.PropertiesFile;
import io.dismute.utils.CommandLineUtil;
import io.dismute.utils.FileUtil;
import io.dismute.utils.LogUtil;
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
        String commonPathForClassFiles = project.getBasePath() + File.separator + "target" + File.separator + "classes" + File.separator; // TODO 多个module的情况
        FileUtil.appendStringToFile(project.getClasspathTxtPath(), ":" + commonPathForClassFiles);
        logger.info("Classpath.txt generated successfully. Path: {}", project.getClasspathTxtPath());
    }


    @Override
    public void clean() {
        logger.info("MavenAdapter clean");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "clean");
        if (exitCode != 0) {
            logger.error("Error while run mvn clean!");
            throw new RuntimeException("Error while run mvn clean!");
        }
    }

    @Override
    public void compilation() {
        logger.info("MavenAdapter compilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "compile");
        if (exitCode != 0) {
            logger.error("Error while run mvn compile!");
        }
    }

    @Override
    public void cleanAndCompilation() {
        logger.info("MavenAdapter cleanAndCompilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "mvn", "clean", "compile");
        if (exitCode != 0) {
            logger.error("Error while run mvn clean compile!");
        }
    }


    @Override
    public void testExecution(Mutant mutant) {
        // 生成mutant对应的mvn log file，构造ProcessBuilder执行命令
        logger.info(LogUtil.centerWithSeparator("TEST STARTED for mvn mutant {}" + mutant.getMutatedName()));
        logger.info("MavenAdapter testExecution for mutant: {}", mutant);
        Project project = Project.getInstance();
        File mvnLogFile = new File(project.getTestOutputsPath() + File.separator + FileUtil.getNameWithoutExtension(mutant.getMutatedPath()) + ".log");  // mvn 日志存放在输出文件夹的testOutputs子文件夹中
        logger.info("Mutant name {}. Output file path: {}", mutant.getMutatedName(), mvnLogFile.getAbsolutePath());
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputsToFileWithTimeout(project.getBasePath(), mvnLogFile.getAbsolutePath(), EXECUTION_TIMEOUT, "mvn", "clean", "test");
        logger.info("Test process for mutant {} exited with code {}", mutant.getMutatedName(), exitCode);
    }
}
