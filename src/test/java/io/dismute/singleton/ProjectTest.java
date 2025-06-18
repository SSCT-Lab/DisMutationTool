package io.dismute.singleton;

import io.dismute.adapter.AntAdapter;
import io.dismute.adapter.BuildToolAdapter;
import io.dismute.adapter.GradleAdapter;
import io.dismute.adapter.MavenAdapter;
import io.dismute.mutantgen.MutatorType;
import io.dismute.testutils.TestUtils;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import io.dismute.testutils.TestResourceManager;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;


public class ProjectTest {

    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();
    private static final PropertiesFile propertiesFile = PropertiesFile.getInstance();
    private static final Logger logger = LogManager.getLogger(ProjectTest.class);


    @AfterClass
    public static void cleanup() {
        testResourceManager.tearDown();
    }

    @After
    public void tearDown() { Project.reset(); }


    // 测试命令行参数构造Project
    @Test
    public void testInitZookeeperProjectWithArgs() {
        testResourceManager.zkSetUp();
        String basePath = Paths.get(testResourceManager.getExtractionPath().toString(), "apache-zookeeper-3.5.8", "zookeeper-server").toString();
        String srcPattern = ".*/src/main/.*\\.java";
        String buildOutputDirName = "target/classes";
        String outputDirName = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "outputForDisMuteTest";
        String projectType = "ant";
        String mutatorListStr = "MNT,RFE";


        String args = "--basePath=" + basePath + "\n" +
                "--mutators=" + mutatorListStr + "\n" +
                "--projectType=" + projectType + "\n" +
                "--srcPattern=" + srcPattern + "\n" +
                "--buildOutputDir=" + buildOutputDirName + "\n" +
                "--outputDir=" + outputDirName;

        String[] argsArray = args.split("\n");

        Project.initialize(argsArray);
        Project project = Project.getInstance();

        assert project.getBasePath().equals(basePath);
        assert project.getProjectType().equals(Project.ProjectType.ANT);
        assert !project.getSrcFileLs().isEmpty();
        assert !project.getTestFileLs().isEmpty();
        assert project.getMutatorTypes().size() == 2;
        assert project.getMutatorTypes().contains(MutatorType.MNT);
        assert project.getMutatorTypes().contains(MutatorType.RFE);
        // 打印src文件列表
        for (String srcFile : project.getSrcFileLs()) {
            logger.info("src file: {}", srcFile);
        }
    }

    // projectType和mutators参数为空，应从配置文件中读取
    @Test
    public void testInitZookeeperProjectWithArgsAndConfigFile() {
        testResourceManager.zkSetUp();
        String basePath = Paths.get(testResourceManager.getExtractionPath().toString(), "apache-zookeeper-3.5.8", "zookeeper-server").toString();
        String srcPattern = ".*/src/main/.*\\.java";
        String buildOutputDirName = "target/classes";
        String outputDirName = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "outputForDisMuteTest";


        String args = "--basePath=" + basePath + "\n" +
                "--srcPattern=" + srcPattern + "\n" +
                "--buildOutputDir=" + buildOutputDirName + "\n" +
                "--outputDir=" + outputDirName;

        String[] argsArray = args.split("\n");

        Project.initialize(argsArray);
        Project project = Project.getInstance();

        assert project.getBasePath().equals(basePath);
        assert !project.getSrcFileLs().isEmpty();
        assert !project.getTestFileLs().isEmpty();
        assert project.getMutatorTypes().size() == propertiesFile.getProperty("project.mutators").split(",").length;

    }

    @Test
    public void testInitCassandraProjectWithArgs(){
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        Project project = Project.getInstance();
        for(String srcFile: project.getSrcFileLs()){
            logger.info("src file: {}", srcFile);
        }

        assert project.getProjectType().equals(Project.ProjectType.ANT);
        assert project.getBasePath().endsWith("apache-cassandra-3.11.6-src");
    }

    @Test
    public void testInitKafkaProjectWithArgs(){
        testResourceManager.kafkaSetUp();
        TestUtils.initializeKafkaProjectFromArgs("MNT");
        Project project = Project.getInstance();
        for(String srcFile: project.getSrcFileLs()){
            logger.info("src file: {}", srcFile);
        }

        assert project.getProjectType().equals(Project.ProjectType.GRADLE);
        assert project.getBasePath().endsWith("kafka-2.4.0-src");
    }

    @Test
    public void testBuildOutputPatternForZkProject() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        Project project = Project.getInstance();
        BuildToolAdapter buildToolAdapter = new MavenAdapter();
        buildToolAdapter.cleanAndCompilation();
        logger.info("Project buildOutputPattern: {}", project.getBuildOutputPattern());
        List<String> buildOutputDirs = project.getBuildOutputDirs();
        for(String buildOutputDir: buildOutputDirs){
            logger.info("buildOutputDirs that match pattern: {}", buildOutputDir);
        }
        assert buildOutputDirs.size() == 1;
    }

    @Test
    public void testBuildOutputPatternForCasProject() {
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        Project project = Project.getInstance();
        BuildToolAdapter buildToolAdapter = new AntAdapter();
        buildToolAdapter.cleanAndCompilation();
        logger.info("Project buildOutputPattern: {}", project.getBuildOutputPattern());
        List<String> buildOutputDirs = project.getBuildOutputDirs();
        for(String buildOutputDir: buildOutputDirs){
            logger.info("buildOutputDirs that match pattern: {}", buildOutputDir);
        }
        assert buildOutputDirs.size() == 1;
    }

    @Ignore // 1min12sec on linux, may success on MacOS
    @Test
    public void testBuildOutputPatternForKafkaProject() {
        testResourceManager.kafkaSetUp();
        TestUtils.initializeKafkaProjectFromArgs("MNT");
        Project project = Project.getInstance();
        BuildToolAdapter buildToolAdapter = new GradleAdapter();
        buildToolAdapter.cleanAndCompilation();
        logger.info("Project buildOutputPattern: {}", project.getBuildOutputPattern());
        List<String> buildOutputDirs = project.getBuildOutputDirs();
        for(String buildOutputDir: buildOutputDirs){
            logger.info("buildOutputDirs that match pattern: {}", buildOutputDir);
        }
        assert !buildOutputDirs.isEmpty() && buildOutputDirs.size() > 1;
    }

    @Test
    public void testInitKafkaProjectExcludeSomeSrc() {
        testResourceManager.kafkaSetUp();
        TestUtils.initializeKafkaProjectFromArgs("MNT");
        Project project = Project.getInstance();
        String[] excludedSrcLs = propertiesFile.getProperty("project.src.excluded").split(",");
        for (String srcFile : project.getSrcFileLs()) {
            for(String excludedSrc: excludedSrcLs){
                if(srcFile.contains(excludedSrc)){
                    logger.error("Excluded src file: {} matches {}", srcFile, excludedSrc);
                    assert false;
                }
            }
        }
    }
}