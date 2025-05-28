package io.dismute.utils;

import io.dismute.singleton.PropertiesFile;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandLineUtil {

    private static final Logger logger = LogManager.getLogger(CommandLineUtil.class);

    private static PropertiesFile propertiesFile = PropertiesFile.getInstance();

    /**
     * 执行命令并返回退出状态，命令执行结果不输出到控制台
     */
    public static int executeCommandWithoutRedirectOutputs(String workingDir, String... command) {
        try {
            logger.info("Executing command without outputs redirected: {}. WorkingDir: {}", String.join(" ", command), workingDir);
            workingDirExists(workingDir); // 验证工作目录是否存在
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(workingDir));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return process.waitFor();

        } catch (IOException e) {
            logger.error("Failed to start process", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.info("Command interrupted", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 执行命令并返回退出状态，命令执行结果输出到控制台
     */
    public static int executeCommandAndRedirectOutputs(String workingDir, String... command) {
        try {
            String fullCommandStr = String.join(" ", command);
            if(fullCommandStr.length() > 5000) {
                logger.info("Executing LONG command with outputs redirected to CONSOLE: {}......... . WorkingDir: {}", fullCommandStr.substring(0,2000), workingDir);

            } else {
                logger.info("Executing command with outputs redirected to CONSOLE: {}. WorkingDir: {}", fullCommandStr, workingDir);
            }
            workingDirExists(workingDir); // 验证工作目录是否存在
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(workingDir));
            pb.redirectErrorStream(true);
            Map<String, String> env = pb.environment();
            env.put("http_proxy", propertiesFile.getProperty("http.proxy"));
            env.put("https_proxy", propertiesFile.getProperty("https.proxy"));
            env.put("ALL_PROXY", propertiesFile.getProperty("socks.proxy"));
            pb.inheritIO();
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
            return process.waitFor();
        } catch (IOException e) {
            logger.error("Failed to start process", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.info("Command interrupted", e);
            throw new RuntimeException(e);
        }
    }

    public static int executeCommandAndRedirectOutputsToFileWithTimeout(String workingDir, String outputFilePath, int timeoutSec, String... command) {
        List<String> commandFiltered = Arrays.stream(command)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());
        logger.info("Executing command with output redirected to FILE. Command: {}. WorkingDir: {}. OutputFile: {}. TimeoutSec: {}", String.join(" ", commandFiltered), workingDir, outputFilePath, timeoutSec);

        // 创建输出文件
        File outputFile = new File(outputFilePath);
        FileUtil.createFileIfNotExist(outputFile.getAbsolutePath());
        // 验证工作目录是否存在
        workingDirExists(workingDir);

        ProcessBuilder pb = new ProcessBuilder(commandFiltered)
                .directory(new File(workingDir))
                .redirectErrorStream(true)
                .redirectOutput(outputFile);

        Map<String, String> env = pb.environment();
        env.put("http_proxy", propertiesFile.getProperty("http.proxy"));
        env.put("https_proxy", propertiesFile.getProperty("https.proxy"));
        env.put("ALL_PROXY", propertiesFile.getProperty("socks.proxy"));

        try {
            Process process = pb.start();
            long processStartTime = System.currentTimeMillis();

            // 如果超时时间小于等于0，不设置守护线程
            if (timeoutSec <= 0) {
                int exitCode = process.waitFor();
                long processDurationSec = (System.currentTimeMillis() - processStartTime) / 1000;
                logger.info("Process started at {} exited with code {}. Duration {} seconds", processStartTime, exitCode, processDurationSec);
                return exitCode;
            }

            // 设置守护线程
            Thread monitorThread = new Thread(() -> {
                while (true) {
                    logger.info("Monitor process started at {} is alive", processStartTime);
                    try {
                        if (!process.isAlive()) {
                            logger.info("Monitor process started at {} exited normally", processStartTime);
                            break;
                        }
                        // 检查日志更新时间
                        if (outputFile.exists()) {
                            long lastModified = outputFile.lastModified();
                            long inactiveDuration = System.currentTimeMillis() - lastModified;
                            if (inactiveDuration > TimeUnit.SECONDS.toMillis(timeoutSec)) {
                                logger.info("Monitor process started at {} exited abnormally after {} seconds. Reason: output file not updated", processStartTime, timeoutSec);
                                process.destroyForcibly();
                                Thread.sleep(10000);
                                // 写入mvn日志文件
                                String content = "Monitor process started at " + processStartTime + " exited abnormally after " + timeoutSec + " seconds\n";
                                FileUtils.writeStringToFile(outputFile, content, StandardCharsets.UTF_8, true);
                                break;
                            }
                        } else {
                            // 处理日志文件尚未创建的情况
                            long processDuration = System.currentTimeMillis() - processStartTime;
                            if (processDuration > TimeUnit.SECONDS.toMillis(timeoutSec)) {
                                logger.info("Monitor process started at {} exited abnormally after {} seconds. Reason: output file not created", processStartTime, timeoutSec);
                                process.destroyForcibly();
                                Thread.sleep(10000);
                                break;
                            }
                        }
                        // 每10秒检查一次
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        logger.info("Monitor process started at {} interrupted", processStartTime);
                        break;
                    } catch (IOException e) {
                        logger.error("Error while writing to output file: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });

            monitorThread.start();

            // 等待进程结束
            int exitCode = process.waitFor();

            // 终止监控线程
            monitorThread.interrupt(); // TODO 优雅的终止线程
            monitorThread.join();

            long processDurationSec = (System.currentTimeMillis() - processStartTime) / 1000;

            logger.info("Process started at {} exited with code {}. Duration {} seconds", processStartTime, exitCode, processDurationSec);

            // 在输出文件的最后一行写入命令行
            String commandLine = "Command executed: " + String.join(" ", commandFiltered) + "\n";
            FileUtils.writeStringToFile(outputFile, commandLine, StandardCharsets.UTF_8, true);

            return exitCode;

        } catch (IOException e) {
            logger.error("Failed to start process", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("Error while waiting for process: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }



    private static void workingDirExists(String workingDir) {
        if (!Files.exists(new File(workingDir).toPath())) {  // 验证工作目录是否存在
            logger.error("Working directory does not exist: {}", workingDir);
            throw new RuntimeException("Working directory does not exist: " + workingDir);
        }
    }


}
