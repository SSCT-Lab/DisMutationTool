package com.example.testRunner;

import com.example.mutantGen.Mutant;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MutantRunner {
    private static final Logger logger = LogManager.getLogger(MutantRunner.class);

    private Mutant mutant;

    public MutantRunner(Mutant mutant) {
        this.mutant = mutant;
    }

    public void run() {


        String mutatedFilePath = mutant.getMutatedPath();
        String mutatedFileName = FileUtil.getFileName(mutatedFilePath) + ".java";

        String originalFilePath = mutant.getOriginalPath();
        String originalFileName = FileUtil.getFileName(originalFilePath) + ".java";

        // 打印文件的diff
        logger.info("Diff between original file" + mutatedFileName + " and mutant file" + originalFileName + ":");
        FileUtil.fileDiff(mutant.getMutatedPath(), mutant.getOriginalPath());

        // 将变异体代码写入项目
        FileUtil.copyFileToTargetDir(mutatedFilePath, FileUtil.getFileDir(originalFilePath), originalFileName);

         logger.info("Starting mutator" + mutatedFileName + " on " + originalFileName);

         // 删除target目录
        if(new File(Config.ZK_PROJECT_PATH + "/target").exists()){
            try {
                logger.info("cleaning target dir");
                Thread.sleep(2000);
                FileUtils.cleanDirectory(new File(Config.ZK_PROJECT_PATH + "/target"));
                Thread.sleep(2000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }


         // 运行测试脚本
        try {
            long startTime = System.currentTimeMillis();

            ProcessBuilder processBuilder = new ProcessBuilder("bash", Config.SCRIPT_PATH, FileUtil.getFileName(mutatedFilePath) + ".txt", Config.ZK_PROJECT_PATH, "");

            processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出

            // 启动进程并执行脚本
            Process process = processBuilder.start();

            // 读取脚本输出并打印到控制台
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }


            int exitCode = process.waitFor();

            // 打印脚本执行结果
            // TODO 汇总统计
            if (exitCode == 0) {
                logger.info("脚本执行成功！");
            } else {
                logger.error("脚本执行失败！");
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            logger.info("脚本执行耗时: " + executionTime + " 毫秒");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.error("执行脚本时发生异常: " + e.getMessage(), e);
        } finally {
            // 撤销变异
            logger.info("撤销变异体代码...");
            FileUtil.copyFileToTargetDir(mutant.getOriginalCopyPath(), FileUtil.getFileDir(originalFilePath), originalFileName);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info("脚本执行完成.");
    }
}
