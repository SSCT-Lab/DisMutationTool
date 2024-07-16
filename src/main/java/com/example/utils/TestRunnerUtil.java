package com.example.utils;

import com.example.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestRunnerUtil {

    private static final Logger logger = LogManager.getLogger(TestRunnerUtil.class);

    public static String getScriptPath(Project project){
        String resourcePath = project.getProjectType() == Project.ProjectType.MAVEN ? "bin/mvn-runner-no-breaking.sh" : "bin/ant-runner-no-breaking.sh";
        try {
            // 从资源中读取脚本文件
            InputStream resourceStream = TestRunnerUtil.class.getClassLoader().getResourceAsStream(resourcePath);
            if (resourceStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            // 创建临时目录和文件
            Path tempDir = Files.createTempDirectory("resources");
            Path tempFile = Paths.get(tempDir.toString(), "mvn.sh");

            // 将脚本文件复制到临时文件
            Files.copy(resourceStream, tempFile);

            // 获取绝对路径
            String absolutePath = tempFile.toAbsolutePath().toString();
            logger.info("Absolute path of mvn.sh: " + absolutePath);

            // 确保临时文件具有执行权限
            tempFile.toFile().setExecutable(true);

            return absolutePath;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
