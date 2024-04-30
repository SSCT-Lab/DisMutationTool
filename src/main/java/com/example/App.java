package com.example;

import com.example.mutantGen.MutatorType;
import com.example.testRunner.RandomRunner;
import com.example.utils.Config;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        // clean up
        try {
            FileUtils.cleanDirectory(new File(Config.MUTANT_PATH));
            FileUtils.cleanDirectory(new File(Config.ORIGINAL_PATH));
            FileUtils.cleanDirectory(new File(Config.OUTPUTS_PATH));
        } catch (Exception e) {
            throw new RuntimeException();
        }

        mutateZK();

//        Collection<File> txtFiles = FileUtils.listFiles(new File(Config.TMP_PATH), new String[]{"txt"}, true);
//        List<String> tmpFiles = txtFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
//        Set<String> originalFileNameSet = new HashSet<>();
//        for (String filePath : tmpFiles) {
//            // /home/zdc/code/ideaRemote/distributed-mutation-tool/tmp/CommitProcessor_RFB_1.txt -> CommitProcessor.java
//            String fileName = FileUtil.getFileName(filePath).split("_")[0] + ".java";
//            originalFileNameSet.add(fileName);
//        }
//        for (String fileName: originalFileNameSet){
//            logger.info(fileName);
//        }
//
//        Project zkProject = Project.builder().setBasePath(Config.ZK_PROJECT_PATH)
//                .excludeDir("build")
//                .withSrcPattern(".*/src/main/.*\\.java")
//                .withTestPattern(".*/src/test/.*Test\\.java")
//                .build();
//
//
//        MutantManager mutantManager = MutantManager.builder()
//                .setProject(zkProject)
//                .setMutator(MutatorType.RFB)
//                .build();
//        mutantManager.generateMutants(originalFileNameSet);
//
//        RandomRunner randomRunner = new RandomRunner(mutantManager);
//        randomRunner.run();

    }

    private static void mutateZK() {
        Project zkProject = Project.builder().setBasePath(Config.ZK_PROJECT_PATH)
                .excludeDir("build")
                .withSrcPattern(".*/src/main/.*\\.java")
                .withTestPattern(".*/src/test/.*Test\\.java")
                .build();


        MutantManager mutantManager = MutantManager.builder()
                .setProject(zkProject)
                .setMutator(MutatorType.UNE)
                .build();
        mutantManager.generateMutants();

        RandomRunner randomRunner = new RandomRunner(mutantManager, 1);
        randomRunner.run();
    }
}
