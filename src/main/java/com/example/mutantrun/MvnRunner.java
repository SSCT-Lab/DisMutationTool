package com.example.mutantrun;

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


public class MvnRunner extends TestSuiteRunner {

    private static final int TIMEOUT_MINUTES = 10;

    private static final Logger logger = LogManager.getLogger(MvnRunner.class);

    public int runTestSuite(String outputFilePath, String projectPath, String args) throws IOException, InterruptedException {

        File outputFile = new File(outputFilePath);
        outputFile.createNewFile();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(projectPath));

        processBuilder.command("bash", "-c", "mvn clean test " + args + " >> " + outputFilePath + " 2>&1 &");

        Process mvnProcess = processBuilder.start();
        long mvnPid = getProcessId(mvnProcess);

        FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(outputFilePath));
        long maxInactiveTimeMillis = TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES);

        while (isProcessAlive(mvnPid)) {
            Thread.sleep(3000);

            FileTime currentModifiedTime = Files.getLastModifiedTime(Paths.get(outputFilePath));

            if (!lastModifiedTime.equals(currentModifiedTime)) {
                lastModifiedTime = currentModifiedTime;
            }

            long inactiveTime = System.currentTimeMillis() - lastModifiedTime.toMillis();

            if (inactiveTime >= maxInactiveTimeMillis) {
                logger.warn("Output file " + outputFilePath + " has not been updated in 10 minutes, terminating process " + mvnPid);
                mvnProcess.destroyForcibly();
                Thread.sleep(10000);
                killMvnProcesses(projectPath);
                Thread.sleep(30000);
                return 2;
            }

            if (Files.readAllLines(Paths.get(outputFilePath)).contains("BUILD FAILURE")) {
                logger.warn("Build failed, check the log file: " + outputFilePath);
                Thread.sleep(10000);
                mvnProcess.destroyForcibly();
                return 1;
            }
        }

        logger.info("Build success, results have been redirected to " + outputFilePath);

        return 0;
    }




    private static void killMvnProcesses(String targetDir) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "lsof +D " + targetDir + " | grep 'mvn' | awk '{print $2}'");
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        List<String> pids = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            pids.add(line);
        }

        if (!pids.isEmpty()) {
            System.out.println("Terminating the following mvn processes: " + String.join(" ", pids));
            for (String pid : pids) {
                new ProcessBuilder("bash", "-c", "kill -9 " + pid).start().waitFor();
            }
        } else {
            System.out.println("No mvn processes found accessing " + targetDir);
        }
    }
}
