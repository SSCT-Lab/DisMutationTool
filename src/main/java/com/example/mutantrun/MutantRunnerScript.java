package com.example.mutantrun;

import com.example.Project;
import com.example.mutator.Mutant;
import com.example.utils.FileUtil;
import com.example.utils.MutantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class MutantRunnerScript {
    private static final Logger logger = LogManager.getLogger(MutantRunnerScript.class);
    private final Mutant mutant;
    private final Project project;

    public MutantRunnerScript(Mutant mutant, Project project) {
        this.mutant = mutant;
        this.project = project;
    }

    public void run(String scriptPath, String args) {
        String mutatedFilePath = mutant.getMutatedPath();
        String mutatedFileName = FileUtil.getFileName(mutatedFilePath) + ".java";

        String originalFilePath = mutant.getOriginalPath();
        String originalFileName = FileUtil.getFileName(originalFilePath) + ".java";

        // 将变异体代码写入项目
        MutantUtil.loadMutant(mutant);

        try {
            long startTime = System.currentTimeMillis();
            String outputDir = Project.OUTPUTS_PATH + "/" + mutant.getMutatorType();
            // 如果outputDir不存在，则创建
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }
            String outputFilePath = outputDir + "/" + FileUtil.getFileName(mutatedFilePath) + ".txt";
            logger.info("Running script: " + scriptPath + " " + outputFilePath + " " + project.getBasePath() + " " + args);
            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath, outputFilePath, project.getBasePath(), args);
            processBuilder.redirectErrorStream(true); // merge stdout and stderr
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            logger.info("Script execution result: " + exitCode);
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            logger.info("Running time: " + executionTime/1000 + " seconds");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.error("Error while running script " + e.getMessage(), e);
        } finally {
            // 撤销变异
            MutantUtil.unloadMutant(mutant);
            // 变异文件复制到输出目录的<mutator>子目录下
            FileUtil.copyFileToTargetDir(mutant.getMutatedPath(), Project.OUTPUTS_PATH + "/" + mutant.getMutatorType(), mutatedFileName);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
