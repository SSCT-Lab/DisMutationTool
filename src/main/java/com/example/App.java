package com.example;

import com.example.mutantgen.MutantGenerator;
import com.example.mutator.MutatorType;
import com.example.testRunner.AllRunner;
import com.example.utils.Config;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main0(String[] args) {
        // clean up
        setUp();
        mutantRmq();
    }

    public static void main(String[] args) {


        args = new String[]{
          Config.RMQ_PROJECT_PATH,
          "RRC,MNT,MNR,UNE,BCS,RCS,NCS,SCS,RTS,UCE,MCT,RCF,UFE",
          "mvn",
          "build",
          ".*/src/main/.*\\.java",
          "target/classes",
          "/home/zdc/outputs/rmqMutant"
        };

        if (args.length < 7) {
            System.out.println("Please provide all required arguments: basePath, mutatorList, projectType, excludeDir, srcPattern, compileOutputDirName, outputDirName");
            return;
        }

        String basePath = args[0];
        String[] mutatorList = args[1].split(",");
        String projectType = args[2];
        String excludeDir = args[3];
        String srcPattern = args[4];
        String buildOutputDirName = args[5];
        String outputDirName = args[6];

        Project.ProjectType type = projectType.equals("mvn") ? Project.ProjectType.MAVEN : Project.ProjectType.ANT;

        Project.ProjectBuilder builder = Project.builder()
                .setBasePath(basePath)
                .setProjectType(type)
                .excludeDir(excludeDir)
                .withSrcPattern(srcPattern)
                .buildOutputDirName(buildOutputDirName)
                .setMutantRunnerOutputPath(outputDirName);

        for(String mutator : mutatorList){
            builder.setMutator(MutatorType.valueOf(mutator));
        }

        Project project = builder.build();

//        AllRunner allRunner = new AllRunner(project);
//        allRunner.run();
        MutantGenerator mutantGenerator = new MutantGenerator(project);
        mutantGenerator.generateMutantsWithoutFilterEq();
    }

    protected static void mutantRmq(){
        Project hbaseProject = Project.builder()
                .setBasePath(Config.RMQ_PROJECT_PATH)
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
                .withTestPattern(".*/test/.*\\.java")
                .buildOutputDirName("target/classes")
                .build();

//        MutantGenerator mutantGenerator = new MutantGenerator(hbaseProject);
//        mutantGenerator.generateMutantsWithoutFilterEq();
        AllRunner allRunner = new AllRunner(hbaseProject);
        allRunner.run();

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
                .setMutator(MutatorType.RRC)
                .excludeDir("build")
                .withSrcPattern(".*/src/main/.*\\.java")
                .withTestPattern(".*/src/test/.*Test\\.java")
                .buildOutputDirName("target/classes")
                .build();



//        MutantGenerator mutantGenerator = new MutantGenerator(zkProject1);
//        mutantGenerator.generateMutantsWithoutFilterEq();

        AllRunner allRunner = new AllRunner(zkProject3);
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
