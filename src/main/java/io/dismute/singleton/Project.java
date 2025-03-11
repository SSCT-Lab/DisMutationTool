package io.dismute.singleton;

import com.google.common.annotations.VisibleForTesting;
import io.dismute.adapter.AntAdapter;
import io.dismute.adapter.BuildToolAdapter;
import io.dismute.adapter.GradleAdapter;
import io.dismute.adapter.MavenAdapter;
import io.dismute.mutantgen.MutatorType;
import io.dismute.utils.Constants;
import io.dismute.utils.FileUtil;
import io.dismute.utils.LogUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Project {

    private static final Logger logger = LogManager.getLogger(Project.class);

    public enum ProjectType {
        MAVEN, ANT, GRADLE
    }

    private static final PropertiesFile propertiesFile = PropertiesFile.getInstance();


    // 静态常量成员（项目级别路径）
    public static String MVN_SCRIPT_PATH; // 弃用
    public static String ANT_SCRIPT_PATH; // 弃用
    public static String GRADLE_SCRIPT_PATH; // 弃用


    // 单例实例（volatile 保证多线程可见性）
    private static volatile Project instance;

    // 非 static 实例成员（命令行参数）
    private final String basePath;
    private final List<String> allFileLs;
    private final List<String> srcFileLs;
    private final List<String> testFileLs;
    private final List<String> excludedTests;
    private final Set<MutatorType> mutatorTypes;
    private final ProjectType projectType;
    private final String buildOutputPath;
    private final String buildOutputPattern; // build输出目录的正则
    private final boolean isDocker;
    private final boolean isCoverage;

    // dismute的输出相关
    private final String resultOutputPath; // 结果输出根目录
    private final String mutantsPath; // 变异体存放路径
    private final String originalPath; // 原始文件存放路径(变异体对应原始文件)
    private final String originalBytecodePath; // 原始字节码存放路径
    private final String mutantBytecodePath; // 变异体字节码存放路径
    private final String mutantFilteredPath; // 变异体存放路径（过滤掉等价变异体之后）
    private final String testOutputsPath; // 脚本运行输出

    // Project运行过程中被设置的参数（coverage、docker、增量编译等）
    private final String classpathTxtPath; // classpath.txt的存储位置，用于增量编译


    // 私有构造器（只能通过 Builder 创建实例）
    private Project(ProjectBuilder builder) {
        this.basePath = builder.basePath;
        this.allFileLs = builder.allFileLs;
        this.srcFileLs = builder.srcFileLs;
        this.testFileLs = builder.testFileLs;
        this.excludedTests = builder.excludedTests;
        this.mutatorTypes = builder.mutators;
        this.projectType = builder.projectType;
        this.buildOutputPath = builder.buildOutputPath;
        this.buildOutputPattern = builder.buildOutputPattern;
        this.isDocker = builder.isDocker;
        this.isCoverage = builder.isCoverage;
        // 构造前检查
        if (this.mutatorTypes.isEmpty()) {
            throw new RuntimeException("Mutators cannot be empty");
        }
        // bash脚本路径相关
        // MVN_SCRIPT_PATH = Paths.get(System.getProperty("user.dir"), "bin", "mvn-runner-no-breaking.sh").toFile().getAbsolutePath();
        // ANT_SCRIPT_PATH = Paths.get(System.getProperty("user.dir"), "bin", "ant-runner-no-breaking.sh").toFile().getAbsolutePath();
        // GRADLE_SCRIPT_PATH = Paths.get(System.getProperty("user.dir"), "bin", "gradle-runner.sh").toFile().getAbsolutePath();
        // 通过classloader获取资源文件
        MVN_SCRIPT_PATH = Objects.requireNonNull(Project.class.getClassLoader().getResource("bin/mvn-runner-no-breaking.sh")).getFile();
        ANT_SCRIPT_PATH = Objects.requireNonNull(Project.class.getClassLoader().getResource("bin/ant-runner-no-breaking.sh")).getFile();
        GRADLE_SCRIPT_PATH = Objects.requireNonNull(Project.class.getClassLoader().getResource("bin/gradle-runner.sh")).getFile();
        logger.info("MVN_SCRIPT_PATH: {}", MVN_SCRIPT_PATH);
        logger.info("ANT_SCRIPT_PATH: {}", ANT_SCRIPT_PATH);
        logger.info("GRADLE_SCRIPT_PATH: {}", GRADLE_SCRIPT_PATH);

        // dismute的输出相关
        this.resultOutputPath = new File(builder.resultOutputPath).getAbsolutePath();
        this.mutantsPath = Paths.get(this.resultOutputPath, "mutants").toFile().getAbsolutePath();
        this.originalPath = Paths.get(this.resultOutputPath, "original").toFile().getAbsolutePath();
        this.originalBytecodePath = Paths.get(this.resultOutputPath, "originalBytecode").toFile().getAbsolutePath();
        this.mutantBytecodePath = Paths.get(this.resultOutputPath, "mutantBytecode").toFile().getAbsolutePath();
        this.mutantFilteredPath = Paths.get(this.resultOutputPath, "mutantsFiltered").toFile().getAbsolutePath();
        this.testOutputsPath = Paths.get(this.resultOutputPath, "testOutputs").toFile().getAbsolutePath();

        // dismute运行过程中存储结果相关
        this.classpathTxtPath = Paths.get(this.resultOutputPath, "classpath.txt").toFile().getAbsolutePath(); // classpath.txt的存储位置，仍存储在输出文件夹中

        //TODO docker相关
        logger.info(LogUtil.centerWithSeparator("Project Information"));
        logger.info("basePath: {}", this.basePath);
        logger.info("mutators: ");
        for (MutatorType mutator : this.mutatorTypes) {
            logger.info("    {}", mutator);
        }
        logger.info("projectType: {}", this.projectType);
        logger.info("srcFileSize: {}", this.srcFileLs.size());
        logger.info("buildOutputPath: {}", this.buildOutputPath);
        logger.info("buildOutputPattern: {}", this.buildOutputPattern);
        logger.info("MVN_SCRIPT_PATH: {}", MVN_SCRIPT_PATH);
        logger.info("ANT_SCRIPT_PATH: {}", ANT_SCRIPT_PATH);
        logger.info("GRADLE_SCRIPT_PATH: {}", GRADLE_SCRIPT_PATH);
        logger.info("resultOutputPath: {}", this.resultOutputPath);
        logger.info("mutantsPath: {}", this.mutantsPath);
        logger.info("originalPath: {}", this.originalPath);
        logger.info("originalBytecodePath: {}", this.originalBytecodePath);
        logger.info("mutantBytecodePath: {}", this.mutantBytecodePath);
        logger.info("mutantFilteredPath: {}", this.mutantFilteredPath);
        logger.info("testOutputsPath: {}", this.testOutputsPath);
        logger.info(LogUtil.centerWithSeparator(""));


        logger.info("Creating output directories...");
        // 创建输出目录
        try {
            FileUtils.deleteDirectory(new File(this.resultOutputPath));
        } catch (IOException e) {
            logger.error("Failed to delete directory: {}", this.resultOutputPath);
            throw new RuntimeException("Failed to delete directory: " + this.resultOutputPath);
        }
        FileUtil.createDirIfNotExist(this.mutantsPath);
        FileUtil.createDirIfNotExist(this.originalPath);
        FileUtil.createDirIfNotExist(this.originalBytecodePath);
        FileUtil.createDirIfNotExist(this.mutantBytecodePath);
        FileUtil.createDirIfNotExist(this.mutantFilteredPath);
        FileUtil.createDirIfNotExist(this.testOutputsPath);

    }

    // 获取单例实例的方法（双重检查锁实现）
    public static Project getInstance() {
        if (instance == null) {
            synchronized (Project.class) {
                if (instance == null) {
                    throw new IllegalStateException("Project has not been initialized!");
                }
            }
        }
        return instance;
    }


    // 初始化单例的方法（通过命令行参数构建)
    public static void initialize(String[] args) {
        if(instance != null) {
            throw new IllegalStateException("Project has already been initialized!");
        }
        synchronized (Project.class) {
            if (instance == null) {
                Project.ProjectBuilder builder = new Project.ProjectBuilder();
                Map<String, String> argMap = new HashMap<>();
                for (String arg : args) {
                    String[] split = arg.split("=");
                    argMap.put(split[0], split[1]);
                    logger.info("arg: {} = {}", split[0], split[1]);
                }

                // TODO docker模式, 覆盖率模式
                boolean isDocker = argMap.containsKey("--dockerfile") || propertiesFile.getProperty("app.docker.enable").equals("true");
                boolean isCoverage = argMap.containsKey("--coveragePath") || propertiesFile.getProperty("app.coverage.enable").equals("true");

                String basePath = getConfig("--basePath", "project.base.path", argMap);
                String mutatorListStr = getConfig("--mutators", "project.mutators", argMap);
                String projectType = getConfig("--projectType", "project.type", argMap);
                String srcPattern = getConfig("--srcPattern", "project.src.pattern", argMap);
                String excludedSrcFile = getConfig("--srcExcluded", "project.src.excluded", argMap);
                String buildOutputDirName = getConfig("--buildOutputDir", "project.build.output.path", argMap); // update：废弃这个参数
                String buildOutputDirPattern = getConfig("--buildOutputDirPattern", "project.build.output.pattern", argMap);
                String outputDirName = getConfig("--outputDir", "app.output.path", argMap);

                if (StringUtils.isEmpty(basePath)
                        || StringUtils.isEmpty(mutatorListStr)
                        || StringUtils.isEmpty(projectType)
                        || StringUtils.isEmpty(srcPattern)
                        || StringUtils.isEmpty(buildOutputDirPattern)
                        || StringUtils.isEmpty(outputDirName)) {
                    logger.error("Missing required arguments, please check the command line arguments or the configuration file.");
                    throw new RuntimeException("Missing required arguments");
                }

                builder.setBasePath(basePath)
                        .setMutators(mutatorListStr)
                        .setProjectType(projectType)
                        .setSrcPattern(srcPattern)
                        .setExcludedSrcFile(excludedSrcFile)
                        .setBuildOutputDirName(buildOutputDirName)
                        .setBuildOutputDirPattern(buildOutputDirPattern)
                        .setResultOutputPath(outputDirName)
                        .setDocker(isDocker)
                        .setCoverage(isCoverage);

                instance = new Project(builder);
                // TODO docker模式，从序列化文件中读取Project对象，设置fromSerialized为true
            } else {
                throw new IllegalStateException("Project has already been initialized!");
            }
        }
    }

    @VisibleForTesting
    public static void reset() {
        synchronized (Project.class) {
            instance = null;
        }
    }

    // 这个方法需要在clean build之后调用才能确保不返回空集合
    public List<String> getBuildOutputDirs() {
        return FileUtil.findDirBasedOnPattern(this.basePath, this.buildOutputPattern);
    }

    // 优先命令行参数，其次配置文件
    private static String getConfig(String nameAsArg, String nameAsConfigProp, Map<String, String> argMap) {
        if (argMap.containsKey(nameAsArg)) {
            return argMap.get(nameAsArg);
        } else {
            return propertiesFile.getProperty(nameAsConfigProp);
        }
    }

    // 根据项目类型，获取构建工具适配器
    public BuildToolAdapter getBuildToolAdapter() {
        switch (instance.getProjectType()) {
            case MAVEN:
                return new MavenAdapter();
            case ANT:
                return new AntAdapter();
            case GRADLE:
                return new GradleAdapter();
            default:
                throw new RuntimeException("Unsupported project type");
        }
    }

    @Getter
    @Setter
    // Builder 模式
    public static class ProjectBuilder {
        private String basePath;
        private List<String> allFileLs;
        private List<String> srcFileLs;
        private List<String> testFileLs;
        private List<String> excludedTests;
        private Set<MutatorType> mutators;
        private ProjectType projectType;
        private String buildOutputPath;
        private String buildOutputPattern;
        private boolean isDocker;
        private boolean isCoverage;
        private String resultOutputPath;

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

        public ProjectBuilder setSrcPattern(String pattern) {
            this.srcFileLs = this.srcFileLs.stream().filter(file -> file.matches(pattern)).collect(Collectors.toList());
            return this;
        }

        public ProjectBuilder setExcludedSrcFile(String excludedSrcFile) {
            if (excludedSrcFile != null) {
                String[] excludedSrcFiles = excludedSrcFile.split(",");
                for (String excludedSrc : excludedSrcFiles) {
                    this.srcFileLs = this.srcFileLs.stream().filter(file -> !file.endsWith(excludedSrc)).collect(Collectors.toList());
                }
            }
            return this;
        }

        public ProjectBuilder setTestPattern(String pattern) {
            this.testFileLs = this.testFileLs.stream().filter(file -> file.matches(pattern)).collect(Collectors.toList());
            return this;
        }

        public ProjectBuilder setMutators(String mutatorListStr) {
            if(mutatorListStr.equalsIgnoreCase("ALL")) {
                mutatorListStr = Constants.ALL_MUTATORS;
            }
            String[] mutatorList = mutatorListStr.split(",");
            this.mutators = new HashSet<>();
            for (String mutator : mutatorList) {
                this.mutators.add(MutatorType.valueOf(mutator.trim()));
            }
            return this;
        }

        public ProjectBuilder setProjectType(String projectType) {
            this.projectType = projectType.equals("mvn") ? ProjectType.MAVEN : projectType.equals("ant") ? ProjectType.ANT : ProjectType.GRADLE;
            return this;
        }

        // build输出相对路径转绝对路径
        public ProjectBuilder setBuildOutputDirName(String targetName) {
            this.buildOutputPath = this.basePath + FileSystems.getDefault().getSeparator() + targetName;
            return this;
        }

        // build输出路径，正则，用于字节码查找
        public ProjectBuilder setBuildOutputDirPattern(String buildOutputDirPattern) {
            this.buildOutputPattern = this.basePath + FileSystems.getDefault().getSeparator() + buildOutputDirPattern;
            return this;
        }

        // 结果输出路径，附加可读时间
        public ProjectBuilder setResultOutputPath(String resultOutputPath) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS");
            String formattedTime = LocalDateTime.now().format(formatter);
            this.resultOutputPath = resultOutputPath + FileSystems.getDefault().getSeparator() + formattedTime;
            return this;
        }

        public ProjectBuilder setDocker(boolean isDocker) {
            this.isDocker = isDocker;
            return this;
        }

        public ProjectBuilder setCoverage(boolean isCoverage) {
            this.isCoverage = isCoverage;
            return this;
        }

    }

}
