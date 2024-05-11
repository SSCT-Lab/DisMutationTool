package com.example;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutatorType;
import com.example.utils.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestUtils {

    public static void clearMutantAndOriginalDir() {
        try {
            // 如果没有这三个文件夹，就创建
            if(!new File(Config.MUTANT_PATH).exists())
                new File(Config.MUTANT_PATH).mkdirs();
            if(!new File(Config.ORIGINAL_PATH).exists())
                new File(Config.ORIGINAL_PATH).mkdirs();
            if(!new File(Config.OUTPUTS_PATH).exists())
                new File(Config.OUTPUTS_PATH).mkdirs();

            FileUtils.cleanDirectory(new File(Config.MUTANT_PATH));
            FileUtils.cleanDirectory(new File(Config.ORIGINAL_PATH));
            FileUtils.cleanDirectory(new File(Config.OUTPUTS_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Project generateZKProject() {
        Project zkProject = Project.builder().setBasePath(Config.ZK_PROJECT_PATH)
                .excludeDir("build")
                .withSrcPattern(".*/src/main/.*\\.java")
                .withTestPattern(".*/src/test/.*Test\\.java")
                .build();
        return zkProject;
    }

    public static MutantManager generateZKMutantManager(MutatorType mutatorType) {
        MutantManager mutantManager = MutantManager.builder()
                .setProject(generateZKProject())
                .setMutator(mutatorType)
                .build();
        return mutantManager;
    }


}
