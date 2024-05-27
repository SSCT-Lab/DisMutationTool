package com.example;

import com.example.mutantgen.MutantGenerator;
import com.example.mutator.MutatorType;
import com.example.testRunner.AllRunner;
import com.example.utils.Config;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        // clean up
        setUp();

        mutantCas();
    }


    protected static void mutantCas(){
        Project casProject = Project.builder()
                .setBasePath(Config.CAS_PROJECT_PATH)
                .setProjectType(Project.ProjectType.ANT)
                .setMutator(MutatorType.MNR)
                .setMutator(MutatorType.MNT)
                .setMutator(MutatorType.RRC)
                .setMutator(MutatorType.UNE)
                .setMutator(MutatorType.BCS)
                .setMutator(MutatorType.RCS)
                .setMutator(MutatorType.NCS)
                .setMutator(MutatorType.SCS)
                .setMutator(MutatorType.RTS)
                .setMutator(MutatorType.UCE)
                .setMutator(MutatorType.MCT)
                .setMutator(MutatorType.RCF)
                .setMutator(MutatorType.UFE)
                .excludeDir("build")
                .withSrcPattern(".*/src/.*\\.java")
                .withTestPattern(".*/test/.*Test\\.java")
                .buildOutputDirName("build/classes")
                .build();

//        MutantGenerator mutantGenerator = new MutantGenerator(casProject);
//        mutantGenerator.generateMutantsWithoutFilterEq();
        AllRunner allRunner = new AllRunner(casProject);
        allRunner.run();

    }

    private static void mutateZK() {

        Project zkProject1 = Project.builder()
                .setBasePath(Config.ZK_PROJECT_PATH)
                .setProjectType(Project.ProjectType.MAVEN)
                .setMutator(MutatorType.MNR)
                .setMutator(MutatorType.MNT)
                .setMutator(MutatorType.RRC)
                .setMutator(MutatorType.UNE)
                .setMutator(MutatorType.BCS)
                .setMutator(MutatorType.RCS)
                .setMutator(MutatorType.NCS)
                .setMutator(MutatorType.SCS)
                .setMutator(MutatorType.RTS)
                .setMutator(MutatorType.UCE)
                .setMutator(MutatorType.MCT)
                .setMutator(MutatorType.RCF)
                .setMutator(MutatorType.UFE)
                .excludeDir("build")
                .withSrcPattern(".*/src/main/.*\\.java")
                .withTestPattern(".*/src/test/.*Test\\.java")
                .buildOutputDirName("target/classes")
                .build();

        Project zkProject2 = Project.builder()
                .setBasePath(Config.ZK_PROJECT_PATH)
                .setProjectType(Project.ProjectType.MAVEN)
                .setMutator(MutatorType.UNE)
                .setMutator(MutatorType.UCE)
                .setMutator(MutatorType.UFE)
                .excludeDir("build")
                .withSrcPattern(".*/src/main/.*\\.java")
                .withTestPattern(".*/src/test/.*Test\\.java")
                .buildOutputDirName("target/classes")
                .build();


        Project zkProject3 = Project.builder()
                .setBasePath(Config.ZK_PROJECT_PATH)
                .setProjectType(Project.ProjectType.MAVEN)
                .setMutator(MutatorType.RTS)
                .excludeDir("build")
                .withSrcPattern(".*/src/main/.*\\.java")
                .withTestPattern(".*/src/test/.*Test\\.java")
                .buildOutputDirName("target/classes")
                .build();



//        MutantGenerator mutantGenerator = new MutantGenerator(zkProject1);
//        mutantGenerator.generateMutantsWithoutFilterEq();

        AllRunner allRunner = new AllRunner(zkProject1);
        allRunner.run();
    }

    private static void setUp(){
        try {
            if(!new File(Config.MUTANT_PATH).exists())
                new File(Config.MUTANT_PATH).mkdirs();
            if(!new File(Config.ORIGINAL_PATH).exists())
                new File(Config.ORIGINAL_PATH).mkdirs();
            if(!new File(Config.OUTPUTS_PATH).exists())
                new File(Config.OUTPUTS_PATH).mkdirs();
            if(!new File(Config.ORIGINAL_BYTECODE_PATH).exists())
                new File(Config.ORIGINAL_BYTECODE_PATH).mkdirs();
            FileUtils.cleanDirectory(new File(Config.MUTANT_PATH));
            FileUtils.cleanDirectory(new File(Config.ORIGINAL_PATH));
            FileUtils.cleanDirectory(new File(Config.OUTPUTS_PATH));
            FileUtils.cleanDirectory(new File(Config.ORIGINAL_BYTECODE_PATH));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
