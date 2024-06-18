package com.example.mutantrun;

import com.example.Project;
import com.example.mutator.Mutant;
import com.example.utils.FileUtil;
import com.example.utils.MutantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class MutantRunner {
    private static final Logger logger = LogManager.getLogger(MutantRunner.class);

    private final Mutant mutant;
    private final Project project;
    private TestSuiteRunner runner;

    public MutantRunner(Mutant mutant, Project project, TestSuiteRunner runner) {
        this.mutant = mutant;
        this.project = project;
        this.runner = runner;
    }

    public void run() {
        String mutatedFilePath = mutant.getMutatedPath();
        String mutatedFileName = FileUtil.getFileName(mutatedFilePath) + ".java";

        String originalFilePath = mutant.getOriginalPath();
        String originalFileName = FileUtil.getFileName(originalFilePath) + ".java";

        // 将变异体代码写入项目
        MutantUtil.loadMutant(mutant);

        // 运行测试脚本
        try {
            long startTime = System.currentTimeMillis();
            String scriptPath = project.getProjectType() == Project.ProjectType.MAVEN ? Project.MVN_SCRIPT_PATH : Project.ANT_SCRIPT_PATH;
            String outputDir = Project.OUTPUTS_PATH + "/" + mutant.getMutatorType();
            // 如果outputDir不存在，则创建
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }
            String outputFilePath = outputDir + "/" + FileUtil.getFileName(mutatedFilePath) + ".txt";
            logger.info("运行测试脚本: " + scriptPath + " " + outputFilePath + " " + project.getBasePath() + " ");

            // 运行测试脚本
            int exitCode = runner.runTestSuite(outputFilePath, project.getBasePath(), "-Dtest.runners=7");


            // 打印脚本执行结果
            if (exitCode == 0) {
                logger.info("脚本执行成功！");
            } else {
                logger.error("脚本执行失败！");
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            logger.info("脚本执行耗时: " + executionTime + " 毫秒");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error during mutating: " + mutatedFileName + " " + e.getMessage(), e);
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
