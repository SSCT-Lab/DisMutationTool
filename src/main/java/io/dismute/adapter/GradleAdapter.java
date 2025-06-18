package io.dismute.adapter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.utils.CommandLineUtil;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class GradleAdapter extends BuildToolAdapter {
    private static final Logger logger = LogManager.getLogger(GradleAdapter.class);
    @Override
    public void cleanAndGenerateClassPath() {
        logger.info("GradleAdapter cleanAndGenerateClassPath");
        Project project = Project.getInstance();
        int exitCode1 = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "./gradlew", "clean", "build", "-x", "test");
        if (exitCode1 != 0) {
            logger.error("Error while cleaning and generating classpath");
            throw new RuntimeException("Error while cleaning and generating classpath");
        }
        logger.info("GradleAdapter cleanAndGenerateClassPath finished successfully");
        logger.info("Generating classpath.txt for Gradle project");
        String BASE_PATH = project.getBasePath() + File.separator;
        List<String> classDirs = FileUtil.getFilesBasedOnPattern(BASE_PATH, ".*/build/classes/java/main");
        List<String> jarFiles = FileUtil.getFilesBasedOnPattern(BASE_PATH, ".*/build/libs/.*\\.jar");
        jarFiles.addAll(FileUtil.getFilesBasedOnPattern(BASE_PATH,  ".*/libs/.*\\.jar"));
        jarFiles.addAll(FileUtil.getFilesBasedOnPattern(System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1", ".*\\.jar"));
        String classpath = String.join(":", classDirs) + ":" + String.join(":", jarFiles);
        FileUtil.writeToFile(classpath, project.getClasspathTxtPath());
    }

    @Override
    public int clean() {
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "./gradlew", "clean");
        if (exitCode != 0) {
            logger.error("Error while run gradle clean!");
            throw new RuntimeException("Error while run gradle clean!");
        }
        return exitCode;
    }

    @Override
    public int compilation() {
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(Project.getInstance().getBasePath(), "./gradlew",  "build", "-x", "test");
        if (exitCode != 0) {
            logger.error("Error while run gradle compileJava!");
        }
        return exitCode;
    }

    @Override
    public int cleanAndCompilation() {
        logger.info("GradleAdapter cleanAndCompilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "./gradlew", "clean", "build", "-x", "test");
        if (exitCode != 0) {
            logger.error("Error while run gradle clean build!");
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
                coveredTestCases ="-x";
            } else {
                coveredTestCases =  String.join(",", "--tests " + mutant.getCoveredTestCases());
            }
        }
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputsToFileWithTimeout
                (project.getBasePath(), logFile.getAbsolutePath(), EXECUTION_TIMEOUT, "./gradlew", "clean", "test", coveredTestCases, String.join(" ", args));
        logger.info("Test process for mutant {} exited with code {}", mutant.getMutatedName(), exitCode);
        return exitCode;
    }
}
