package com.example.testRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AntRunner {

    private static final int TIMEOUT_MINUTES = 10;

    private static final Logger logger = LogManager.getLogger(AntRunner.class);

    private static int runAntProcess(String outputFilePath, String projectPath, String antArgs) throws IOException, InterruptedException {
        File outputFile = new File(outputFilePath);
        outputFile.createNewFile();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectPath));
        processBuilder.command("bash", "-c", "ant clean test " + antArgs + " >> " + outputFilePath + " 2>&1 &");

        Process antProcess = processBuilder.start();
        long antPid = getProcessId(antProcess);

        FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(outputFilePath));
        long maxInactiveTimeMillis = TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES);

        while (isProcessAlive(antPid)) {
            Thread.sleep(3000);

            FileTime currentModifiedTime = Files.getLastModifiedTime(Paths.get(outputFilePath));

            if (!lastModifiedTime.equals(currentModifiedTime)) {
                lastModifiedTime = currentModifiedTime;
            }

            long inactiveTime = System.currentTimeMillis() - lastModifiedTime.toMillis();

            if (inactiveTime >= maxInactiveTimeMillis) {
                logger.warn("Output file " + outputFilePath + " has not been updated in 10 minutes, terminating process " + antPid);
                antProcess.destroyForcibly();
                Thread.sleep(10000);
                killAntProcesses(projectPath);
                Thread.sleep(60000);
                return 2;
            }
        }

        return 0;
    }

    private static long getProcessId(Process process) {
        try {
            // For UNIX-based systems
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                String pidField = process.getClass().getDeclaredField("pid").getName();
                if (pidField != null) {
                    process.getClass().getDeclaredField("pid").setAccessible(true);
                    return (long) process.getClass().getDeclaredField("pid").get(process);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static boolean isProcessAlive(long pid) throws IOException {
        Process process = new ProcessBuilder("bash", "-c", "kill -0 " + pid).start();
        try {
            return process.waitFor() == 0;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private static void killAntProcesses(String targetDir) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "lsof +D " + targetDir + " | grep 'ant' | awk '{print $2}'");
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        List<String> pids = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            pids.add(line);
        }

        if (!pids.isEmpty()) {
            logger.warn("Terminating the following ant processes: " + String.join(" ", pids));
            for (String pid : pids) {
                new ProcessBuilder("bash", "-c", "kill -9 " + pid).start().waitFor();
            }
        } else {
            logger.warn("No ant processes found accessing " + targetDir);
        }
    }
}
