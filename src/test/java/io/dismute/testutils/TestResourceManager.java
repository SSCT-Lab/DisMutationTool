package io.dismute.testutils;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Getter
public class TestResourceManager {

    Logger logger = LogManager.getLogger(TestResourceManager.class);

    private static final String ZK_RESOURCE_NAME = "apache-zookeeper-3.5.8.tar.gz";
    private static final String ZK_EXTRACTED_DIR_NAME = "zookeeper-3.5.8";
    // /zookeeper-3.5.8/apache-zookeeper-3.5.8/zookeeper-server/

    private static final String CAS_RESOURCE_NAME = "apache-cassandra-3.11.6-src.tar.gz";
    private static final String CAS_EXTRACTED_DIR_NAME = "cassandra-3.11.6";
    // /cassandra-3.11.6/apache-cassandra-3.11.6-src/

    private static final String KAFKA_RESOURCE_NAME = "kafka-2.4.0-src.tar.gz";
    private static final String KAFKA_EXTRACTED_DIR_NAME = "kafka-2.4.0";
    private static final String KAFKA_DIR_NAME = "kafka-2.4.0-src";
    // /kafka-2.4.0/kafka-2.4.0-src/
    public static final String COVERAGE_FILE_NAME = "zk-testlist.txt";

    private static volatile TestResourceManager instance;

    private Path resourcePath;
    private Path extractionPath;

    public void zkSetUp() {
        setUp(ZK_RESOURCE_NAME, ZK_EXTRACTED_DIR_NAME);
    }

    public void casSetUp() {
        setUp(CAS_RESOURCE_NAME, CAS_EXTRACTED_DIR_NAME);
    }

    public void kafkaSetUp() {
        setUp(KAFKA_RESOURCE_NAME, KAFKA_EXTRACTED_DIR_NAME);
    }

    private void setUp(String resourceName, String extractedDirName) {
        // 1. 使用 ClassLoader 获取资源路径
        URL resourceUrl = TestResourceManager.class.getClassLoader().getResource(resourceName);
        if (resourceUrl == null) {
            throw new RuntimeException("Resource not found: " + resourceName);
        }
        resourcePath = Paths.get(resourceUrl.getFile());
        String resourcePathStr = resourcePath.toString();
        logger.info("Resource path: {}", resourcePathStr);

        // 2. 创建解压目录
        logger.info("Creating extraction directory");
        extractionPath = Paths.get(System.getProperty("user.home"), extractedDirName);
        // 2.1 如果目录已存在，删除目录
        if (Files.exists(extractionPath)) {
            try {
                FileUtils.deleteDirectory(extractionPath.toFile());
            } catch (IOException e) {
                logger.error("Error deleting existing extraction directory: {}", extractionPath);
                throw new RuntimeException("Error deleting existing extraction directory: " + extractionPath, e);
            }
        }
        try {
            Files.createDirectories(extractionPath);
        } catch (IOException e) {
            logger.error("Error creating extraction directory: {}", extractionPath);
            throw new RuntimeException("Error creating extraction directory: " + extractionPath, e);
        }

        // 3. 解压资源到解压目录
        logger.info("Extracting resource to extraction directory");
        logger.info("Extraction directory: {}", extractionPath.toAbsolutePath());
        TestUtils.extractTarGz(resourcePathStr, extractionPath.toString());

        // 如果是gradle项目，确保gradlew可执行
        if (resourceName.contains("kafka")) {
            logger.info("Setting gradlew executable");
            String gradlewPath = Paths.get(extractionPath.toString(), KAFKA_DIR_NAME, "gradlew").toString();
            File gradlewFile = new File(gradlewPath);
            if(!gradlewFile.exists()) {
                logger.error("gradlew file not found");
                throw new RuntimeException("gradlew file not found");
            }
            logger.info("gradlew file position: {}", gradlewFile);
            if(!gradlewFile.setExecutable(true)) {
                logger.error("Error setting gradlew executable");
                throw new RuntimeException("Error setting gradlew executable");
            }
        }

        logger.info("Initialization complete");
    }

    public void tearDown() {
        logger.info("Cleaning up extraction directory");
        TestUtils.deleteDirectory(extractionPath);
    }

    private TestResourceManager() {}

    public static TestResourceManager getInstance() {
        if (instance == null) {
            synchronized (TestResourceManager.class) {
                if (instance == null) {
                    instance = new TestResourceManager();
                }
            }
        }
        return instance;
    }

}
