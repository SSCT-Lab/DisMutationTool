package io.dismute.engine;

import io.dismute.singleton.Project;
import io.dismute.singleton.PropertiesFile;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class RunningEngineTest {
    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();
    private static final PropertiesFile propertiesFile = PropertiesFile.getInstance();
    private static final Logger logger = LogManager.getLogger(RunningEngineTest.class);


    @AfterClass
    public static void cleanup() {
        testResourceManager.tearDown();
    }

    @After
    public void tearDown() { Project.reset(); }

    // 测试初次编译项目并拷贝字节码文件
    @Test
    public void testCompileAndCopyOriginalBytecode() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        RunningEngine engine = RunningEngine.getInstance();
        engine.compileAndCopyOriginalBytecode();
        List<String> classFilesCopied = FileUtil.getFilesBasedOnPattern(Project.getInstance().getOriginalBytecodePath(), ".*\\.class");
        assert !classFilesCopied.isEmpty();
    }

    // 测试zookeeper，15个变异体，仅执行1个用例，1min19sec
    @Test
    public void testZkWithMNT() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        RunningEngine engine = RunningEngine.getInstance();
        engine.run();
    }


    @Test
    public void testZkCoverageWithMNT() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectWithCoverage("MNT");
        RunningEngine engine = RunningEngine.getInstance();
        assert Project.getInstance().isCoverage();
        engine.run();
    }

    @Test
    public void testCasWithMNT() {
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        RunningEngine engine = RunningEngine.getInstance();
        engine.run();
    }

    @Ignore
    @Test
    public void testKafkaWithMNT() { // 未能稳定运行，有待调试
        testResourceManager.kafkaSetUp();
        TestUtils.initializeKafkaProjectFromArgs("MNT");
        RunningEngine engine = RunningEngine.getInstance();
        engine.run();
    }

}
