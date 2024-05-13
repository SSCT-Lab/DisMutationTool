package com.example;

import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private Project(ProjectBuilder builder) {
        this.basePath = builder.basePath;
        this.allFileLs = builder.allFileLs;
        this.srcFileLs = builder.srcFileLs;
        this.testFileLs = builder.testFileLs;
        this.excludedTests = builder.excludedTests;
        this.projectType = builder.projectType;
        this.mutators = builder.mutators;
        this.buildOutputPath = builder.buildOutputPath;
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
        private final Set<MutatorType> mutators = new HashSet<>();
        ProjectType projectType = ProjectType.MAVEN;
        private String buildOutputPath;

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



        public Project build() {
            return new Project(this);
        }
    }


}
