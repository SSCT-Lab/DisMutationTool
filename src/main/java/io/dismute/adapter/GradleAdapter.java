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
        int exitCode1 = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "gradlew", "clean", "build", "-x", "test");
        if (exitCode1 != 0) {
            logger.error("Error while cleaning and generating classpath");
            throw new RuntimeException("Error while cleaning and generating classpath");
        }
        String BASE_PATH = project.getBasePath() + File.separator;
        List<String> classDirs = FileUtil.getFilesBasedOnPattern(BASE_PATH, "*/build/classes/java/main");
        List<String> jarFiles = FileUtil.getFilesBasedOnPattern(BASE_PATH, "*/build/libs/*.jar");
        jarFiles.addAll(FileUtil.getFilesBasedOnPattern(BASE_PATH,  "*/libs/*.jar"));
        jarFiles.addAll(FileUtil.getFilesBasedOnPattern(System.getProperty("user.home"), "\\.gradle/caches/modules-2/files-2.1/*.jar"));
        String classpath = String.join(":", classDirs) + ":" + String.join(":", jarFiles);
        FileUtil.writeToFile(classpath, project.getClasspathTxtPath());
    }

    @Override
    public void clean() {
        System.out.println("GradleAdapter clean");
    }

    @Override
    public void compilation() {
        System.out.println("Gradle compilation");
    }

    @Override
    public void cleanAndCompilation() {
        logger.info("GradleAdapter cleanAndCompilation");
        Project project = Project.getInstance();
        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(project.getBasePath(), "./gradlew", "clean", "build", "-x", "test");
        if (exitCode != 0) {
            logger.error("Error while run gradle clean build!");
        }
    }

    @Override
    public void testExecution(Mutant mutant) {

    }
}
