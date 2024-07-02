package com.example;

import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Project {

    public enum ProjectType {
        MAVEN, ANT
    }


    private static final Logger logger = LogManager.getLogger(Project.class);

    private final String basePath;
    private final List<String> allFileLs; // 所有java文件列表
    private final List<String> srcFileLs; // src目录下的java文件列表
    private final List<String> testFileLs; // 要执行的junit测试，.java文件
    private final List<String> excludedTests; // 要排除的测试名称
    private final Set<MutatorType> mutators; // 变异算子列表
    private final ProjectType projectType;
    private final String buildOutputPath; // 编译输出路径，用于进行字节码比较

    // paths to save mutants, original files, bytecodes, and test outputs
    public static String MVN_SCRIPT_PATH;
    public static String ANT_SCRIPT_PATH;
    public static String MUTANT_OUTPUT_PATH; // 输出文件的根文件夹路径
    public static String MUTANTS_PATH;
    public static String ORIGINAL_PATH;
    public static String ORIGINAL_BYTECODE_PATH;
    public static String MUTANT_BYTECODE_PATH;
    public static String OUTPUTS_PATH;

    private Project(ProjectBuilder builder) {
        this.basePath = builder.basePath;
        this.allFileLs = builder.allFileLs;
        this.srcFileLs = builder.srcFileLs;
        this.testFileLs = builder.testFileLs;
        this.excludedTests = builder.excludedTests;
        this.projectType = builder.projectType;
        this.mutators = builder.mutators;
        this.buildOutputPath = builder.buildOutputPath;
        if (this.mutators.isEmpty()) {
            throw new RuntimeException("Mutators cannot be empty");
        }

        MVN_SCRIPT_PATH = Paths.get(System.getProperty("user.dir"), "bin", "mvn-runner-no-breaking.sh").toFile().getAbsolutePath();
        ANT_SCRIPT_PATH = Paths.get(System.getProperty("user.dir"), "bin", "ant-runner-no-breaking.sh").toFile().getAbsolutePath();
        logger.info("MVN_SCRIPT_PATH: " + MVN_SCRIPT_PATH);
        logger.info("ANT_SCRIPT_PATH: " + ANT_SCRIPT_PATH);

        MUTANT_OUTPUT_PATH = new File(builder.mutantRunnerOutputPath).getAbsolutePath();
        MUTANTS_PATH = Paths.get(MUTANT_OUTPUT_PATH, "mutants").toFile().getAbsolutePath();
        ORIGINAL_PATH = Paths.get(MUTANT_OUTPUT_PATH, "original").toFile().getAbsolutePath();
        ORIGINAL_BYTECODE_PATH = Paths.get(MUTANT_OUTPUT_PATH, "originalBytecode").toFile().getAbsolutePath();
        MUTANT_BYTECODE_PATH = Paths.get(MUTANT_OUTPUT_PATH, "mutantBytecode").toFile().getAbsolutePath();
        OUTPUTS_PATH = Paths.get(MUTANT_OUTPUT_PATH, "testOutputs").toFile().getAbsolutePath();

        // 如果存在，删除原有的MUTANT_OUTPUT_PATH
        try {
            FileUtils.deleteDirectory(new File(MUTANT_OUTPUT_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 判断以上5个路径是否存在，不存在则创建
        FileUtil.createDirIfNotExist(MUTANTS_PATH);
        FileUtil.createDirIfNotExist(ORIGINAL_PATH);
        FileUtil.createDirIfNotExist(ORIGINAL_BYTECODE_PATH);
        FileUtil.createDirIfNotExist(MUTANT_BYTECODE_PATH);
        FileUtil.createDirIfNotExist(OUTPUTS_PATH);

        // 打印项目信息，如果遇到Collection循环缩进打印
        logger.info("Project Information:");
        logger.info("basePath: " + this.basePath);
        logger.info("srcFileLs: ");
        for (String file : this.srcFileLs) {
            logger.info("    " + file);
        }
        logger.info("mutators: ");
        for (MutatorType mutator : this.mutators) {
            logger.info("    " + mutator);
        }
        logger.info("projectType: " + this.projectType);
        logger.info("buildOutputPath: " + this.buildOutputPath);
        logger.info("MVN_SCRIPT_PATH: " + MVN_SCRIPT_PATH);
        logger.info("ANT_SCRIPT_PATH: " + ANT_SCRIPT_PATH);
        logger.info("MUTANT_OUTPUT_PATH: " + MUTANT_OUTPUT_PATH);
        logger.info("MUTANTS_PATH: " + MUTANTS_PATH);
        logger.info("ORIGINAL_PATH: " + ORIGINAL_PATH);
        logger.info("ORIGINAL_BYTECODE_PATH: " + ORIGINAL_BYTECODE_PATH);
        logger.info("OUTPUTS_PATH: " + OUTPUTS_PATH);

    }

    public static ProjectBuilder builder() {
        return new ProjectBuilder();
    }

    public static class ProjectBuilder {
        private String basePath;
        private List<String> allFileLs;
        private List<String> srcFileLs;
        private List<String> testFileLs;
        private final List<String> excludedTests = new ArrayList<>();
        private final Set<MutatorType> mutators = new TreeSet<>();
        ProjectType projectType = ProjectType.MAVEN;
        private String buildOutputPath;
        private String mutantRunnerOutputPath;

        public ProjectBuilder setBasePath(String basePath) {
            this.basePath = basePath;
            this.allFileLs = FileUtil.getFilesBasedOnPattern(basePath, ".*\\.java");
            this.srcFileLs = new ArrayList<>(this.allFileLs);
            this.testFileLs = new ArrayList<>(this.allFileLs);
            // 按照默认情况，初始化src和test文件的默认值
            // this.srcFileLs = FileUtil.getFilesBasedOnPattern(basePath, ".*/src/.*\\.java$");
            // this.testFileLs = FileUtil.getFilesBasedOnPattern(basePath, ".*/test/.*\\.java$");
            return this;
        }

        public ProjectBuilder excludeDir(String dirName) {
            String pattern = "/" + dirName + "/";
            this.allFileLs = this.allFileLs.stream().filter(file -> !file.contains(pattern)).collect(Collectors.toList());
            this.srcFileLs = this.srcFileLs.stream().filter(file -> !file.contains(pattern)).collect(Collectors.toList());
            this.testFileLs = this.testFileLs.stream().filter(file -> !file.contains(pattern)).collect(Collectors.toList());
            return this;
        }

        public ProjectBuilder withSrcPattern(String pattern) {
            this.srcFileLs = this.srcFileLs.stream().filter(file -> file.matches(pattern)).collect(Collectors.toList());
            return this;
        }

        public ProjectBuilder withTestPattern(String pattern) {
            this.testFileLs = this.testFileLs.stream().filter(file -> file.matches(pattern)).collect(Collectors.toList());
            return this;
        }

        public ProjectBuilder excludeTest(String testName) {
            this.excludedTests.add(testName);
            return this;
        }

        public ProjectBuilder setProjectType(ProjectType projectType) {
            this.projectType = projectType;
            return this;
        }

        public ProjectBuilder setMutator(MutatorType mutator) {
            this.mutators.add(mutator);
            return this;
        }

        public ProjectBuilder buildOutputDirName(String targetName) {
            this.buildOutputPath = this.basePath + "/" + targetName;
            return this;
        }

        public ProjectBuilder setMutantRunnerOutputPath(String path) {
            this.mutantRunnerOutputPath = path;
            return this;
        }


        public Project build() {
            return new Project(this);
        }
    }


}
