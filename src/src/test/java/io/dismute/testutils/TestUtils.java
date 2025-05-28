package io.dismute.testutils;

import io.dismute.singleton.Project;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class TestUtils {
    private static final Logger logger = LogManager.getLogger(TestUtils.class);

    public static Path extractTarGz(String resourcePath, String outputDir) {
        Path outputPath = Paths.get(outputDir);

        try (InputStream fileInputStream = new FileInputStream(resourcePath);
             GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            TarArchiveEntry entry;

            // 遍历压缩包内所有文件和目录
            while ((entry = tarInputStream.getNextTarEntry()) != null) {
                Path entryPath = outputPath.resolve(entry.getName());

                if (entry.isDirectory()) {
                    // 如果是目录，创建目录
                    Files.createDirectories(entryPath);
                } else {
                    // 如果是文件，写入文件
                    Files.createDirectories(entryPath.getParent());  // 确保父目录存在
                    try (OutputStream outputStream = Files.newOutputStream(entryPath)) {
                        IOUtils.copy(tarInputStream, outputStream);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error extracting tar.gz file: " + resourcePath, e);
        }

        logger.info("Extracted tar.gz file to: {}", outputPath);

        return outputPath;
    }

    public static void deleteDirectory(Path path) {
        try {
            // 删除目录及其子目录和文件
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException e) {
            logger.error("Error deleting directory: {}", path);
            throw new RuntimeException("Error deleting directory: " + path, e);
        }
    }


    // 生成一个Zookeeper项目
    public static void initializeZookeeperProjectFromArgs(String mutatorTypeStr) {
        logger.info("Initializing Zookeeper Project for DisMute Test");
        Project.reset();

        String basePath = Paths.get(TestResourceManager.getInstance().getExtractionPath().toString(), "apache-zookeeper-3.5.8", "zookeeper-server").toString();
        String srcPattern = ".*/src/main/.*\\.java";
        String buildOutputDirName = "target/classes";
        String outputDirName = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "outputForDisMuteTest";
        String projectType = "mvn";


        String args = "--basePath=" + basePath + "\n" +
                "--mutators=" + mutatorTypeStr + "\n" +
                "--projectType=" + projectType + "\n" +
                "--srcPattern=" + srcPattern + "\n" +
                "--buildOutputDir=" + buildOutputDirName + "\n" +
                "--outputDir=" + outputDirName;

        String[] argsArray = args.split("\n");

        Project.initialize(argsArray);

        logger.info("Zookeeper Project Initialized");
    }


    // 生成一个Cassandra(ant)项目
    //    nohup java -jar ./DisMutationTool-1.0-SNAPSHOT-jar-with-dependencies.jar \
    //            --projectPath=/home/ubuntu/distributedSystems/cas/apache-cassandra-3.11.6-src \
    //            --mutators=RRC,MNT,MNR,RNE,BCS,RCS,NCS,SCS,RTS,RCE,MCT,RCF,RFE \
    //            --projectType=ant \
    //            --srcPattern='.*\/src\/java\/org\/apache\/cassandra\/.*\.java' \
    //            --buildOutputDir=build/classes \
    //            --outputDir=/home/ubuntu/outputs/casMutation \
    //            > /home/ubuntu/disMutTool/casMutation.log 2>&1 &
    public static void initializeCassandraProjectFromArgs(String mutatorTypeStr) {
        logger.info("Initializing Cassandra Project for DisMute Test");
        Project.reset();

        String basePath = Paths.get(TestResourceManager.getInstance().getExtractionPath().toString(), "apache-cassandra-3.11.6-src").toString();
        String srcPattern = ".*/src/java/org/apache/cassandra/.*\\.java";
        String buildOutputDirName = "build/classes";
        String outputDirName = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "outputForDisMuteTest";
        String projectType = "ant";

        String args = "--basePath=" + basePath + "\n" +
                "--mutators=" + mutatorTypeStr + "\n" +
                "--projectType=" + projectType + "\n" +
                "--srcPattern=" + srcPattern + "\n" +
                "--buildOutputDir=" + buildOutputDirName + "\n" +
                "--outputDir=" + outputDirName;

        String[] argsArray = args.split("\n");

        Project.initialize(argsArray);

        logger.info("Cassandra Project Initialized");
    }

    // 生成一个Kafka(Gradle)项目
    public static void initializeKafkaProjectFromArgs(String mutatorTypeStr) {
        logger.info("Initializing Kafka Project for DisMute Test");
        Project.reset();

        String basePath = Paths.get(TestResourceManager.getInstance().getExtractionPath().toString(), "kafka-2.4.0-src").toString();
        String srcPattern = ".*/(core|clients|connect|generator|streams|tools|metadata|raft|shell)/src/main/.*\\.java";
        String buildOutputDirName = ".*/build/classes";
        String outputDirName = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "outputForDisMuteTest";
        String projectType = "gradle";

        String args = "--basePath=" + basePath + "\n" +
                "--mutators=" + mutatorTypeStr + "\n" +
                "--projectType=" + projectType + "\n" +
                "--srcPattern=" + srcPattern + "\n" +
                "--buildOutputDir=" + buildOutputDirName + "\n" +
                "--outputDir=" + outputDirName;

        String[] argsArray = args.split("\n");

        Project.initialize(argsArray);

        logger.info("Kafka Project Initialized");
    }

    // 生成一个带coverageInfo的zookeeper项目
    public static void initializeZookeeperProjectWithCoverage(String mutatorTypeStr) {
        logger.info("Initializing Zookeeper Project with Coverage for DisMute Test");
        Project.reset();

        String basePath = Paths.get(TestResourceManager.getInstance().getExtractionPath().toString(), "apache-zookeeper-3.5.8", "zookeeper-server").toString();
        String srcPattern = ".*/src/main/.*\\.java";
        String buildOutputDirName = "target/classes";
        String outputDirName = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "outputForDisMuteTest";
        String projectType = "mvn";

        // 获取coverage资源
        // 使用 ClassLoader 获取资源路径
        URL resourceUrl = TestUtils.class.getClassLoader().getResource(TestResourceManager.COVERAGE_FILE_NAME);
        if (resourceUrl == null) {
            throw new RuntimeException("Resource not found: " + TestResourceManager.COVERAGE_FILE_NAME);
        }
        Path coverageFilePath = Paths.get(resourceUrl.getFile());
        logger.info("Coverage path: {}", coverageFilePath.toString());


        String args = "--basePath=" + basePath + "\n" +
                "--mutators=" + mutatorTypeStr + "\n" +
                "--projectType=" + projectType + "\n" +
                "--srcPattern=" + srcPattern + "\n" +
                "--buildOutputDir=" + buildOutputDirName + "\n" +
                "--outputDir=" + outputDirName + "\n" +
                "--coveragePath=" + coverageFilePath.toString();

        String[] argsArray = args.split("\n");

        Project.initialize(argsArray);

        logger.info("Zookeeper Project with Coverage Initialized");
    }

}