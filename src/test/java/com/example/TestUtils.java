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

    public static MutantManager generateZKMutantManager() {
        MutantManager mutantManager = MutantManager.builder()
                .setProject(generateZKProject())
                .setMutator(MutatorType.RFB)
                .setMutator(MutatorType.MWT)
                .build();
        return mutantManager;
    }

    public static MutantManager generateZKMutantManager(MutatorType mutatorType) {
        MutantManager mutantManager = MutantManager.builder()
                .setProject(generateZKProject())
                .setMutator(mutatorType)
                .build();
        return mutantManager;
    }


}
