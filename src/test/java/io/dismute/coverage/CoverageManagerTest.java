package io.dismute.coverage;

import io.dismute.testutils.TestResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoverageManagerTest {
    private static final Logger logger = LogManager.getLogger(CoverageManagerTest.class);

    private static final String COVERAGE_FILE_NAME = "zk-testlist.txt";

    @Test
    public void testInitializeCoverageManager() {
        // 测试初始化CoverageManager


        // 使用 ClassLoader 获取资源路径
        URL resourceUrl = CoverageManagerTest.class.getClassLoader().getResource(COVERAGE_FILE_NAME);
        if (resourceUrl == null) {
            throw new RuntimeException("Resource not found: " + COVERAGE_FILE_NAME);
        }
        Path coverageFilePath = Paths.get(resourceUrl.getFile());
        logger.info("Coverage path: {}", coverageFilePath.toString());

        CoverageManager.initialize(coverageFilePath.toString());
        CoverageManager coverageManager = CoverageManager.getInstance();
        assert coverageManager != null;
        assert ! coverageManager.getCoverageInfo().isEmpty();
        logger.info("CoverageManager initialized successfully.");

        // 打印coverageInfo，先打印key，然后每行缩紧打印value
        logger.info("Coverage Info:");
        coverageManager.getCoverageInfo().forEach((key, value) -> {
            logger.info("Source File: {}", key);
            value.forEach(testClass -> logger.info("\tTest Class: {}", testClass));
        });
    }

}
