package com.example;

import com.example.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Project {

    private static final Logger logger = LogManager.getLogger(Project.class);

    private final String BASE_PATH;
    private final List<String> allFileLs; // 所有java文件列表
    private final List<String> srcFileLs; // src目录下的java文件列表
    private final List<String> testFileLs; // 要执行的junit测试，.java文件


    private Project(String basePath, List<String> allFileLs, List<String> srcFileLs, List<String> testFileLs) {
        this.BASE_PATH = basePath;
        this.allFileLs = allFileLs;
        this.srcFileLs = srcFileLs;
        this.testFileLs = testFileLs;
    }

    public String getBasePath() {
        return BASE_PATH;
    }

    public List<String> getAllFileLs() {
        return allFileLs;
    }

    public List<String> getSrcFileLs() {
        return srcFileLs;
    }

    public List<String> getTestFileLs() {
        return testFileLs;
    }

    public static ProjectBuilder builder() {
        return new ProjectBuilder();
    }

    static class ProjectBuilder {
        private String basePath;
        private List<String> allFileLs;
        private List<String> srcFileLs;
        private List<String> testFileLs;

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

        public ProjectBuilder excludeTest(String pattern) {
            this.testFileLs = this.testFileLs.stream().filter(file -> !file.matches(pattern)).collect(Collectors.toList());
            return this;
        }


        public Project build() {
            return new Project(basePath, allFileLs, srcFileLs, testFileLs);
        }
    }


}
