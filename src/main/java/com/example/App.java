package com.example;

import com.example.mutantgen.MutantGenerator;
import com.example.mutator.Mutant;
import com.example.mutator.MutatorType;
import com.example.testRunner.AllRunner;
import com.example.testRunner.CoverageBasedRunner;
import com.example.testRunner.DockerRunner;
import com.example.testRunner.PartitionRunner;
import com.example.utils.Config;
import com.example.utils.Constants;
import com.example.utils.MutantUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void oldMain(String[] args) {
        // clean up
//        setUp();
//        mutateHDFS();
    }

    // --projectPath=/home/zdc/code/distributedSystems/rmq/rocketmq-all-5.2.0-source-release --mutators=RRC,MNT,MNR,UNE,BCS,RCS,NCS,SCS,RTS,UCE,MCT,RCF,UFE --projectType=mvn --srcPattern=.*/src/main/.*\.java --buildOutputDir=target/classes --outputDir=/home/zdc/outputs/rmqMutant --dockerfile=/home/zdc/code/DisMutationTool/Dockerfile/rmq/Dockerfile --projectPathInDocker=/usr/local/src/rocketmq/rocketmq-all-5.2.0-source-release

    public static void main(String[] args) {


//        args = new String[]{
//                "--projectPath=/home/zdc/code/distributedSystems/rmq/rocketmq-all-5.2.0-source-release",
//                "--mutators=MNT,MNR",
//                "--projectType=mvn",
//                "--srcPattern=.*/src/main/.*\\.java",
//                "--buildOutputDir=target/classes",
//                "--outputDir=/home/zdc/outputs/rmqMutant",
//
//                "--dockerfile=/home/zdc/code/DisMutationTool/Dockerfile/rmq/Dockerfile",
//                "--projectPathInDocker=/usr/local/src/rocketmq/rocketmq-all-5.2.0-source-release"
//        };
        if (args.length == 0) {
            logger.error("No arguments provided");
            return;
        }

        Map<String, String> argMap = new HashMap<>();
        for (String arg : args) {
            String[] split = arg.split("=");
            argMap.put(split[0], split[1]);
        }

        boolean isDocker = argMap.containsKey("--dockerfile");
        boolean isPartition = argMap.containsKey("--partition");
        boolean isCoverage = argMap.containsKey("--coveragePath"); // 是否以coverage模式运行

        String basePath = argMap.get("--projectPath");
        String[] mutatorList = argMap.get("--mutators").split(",");
        String projectType = argMap.get("--projectType");
        String srcPattern = argMap.get("--srcPattern");
        String buildOutputDirName = argMap.get("--buildOutputDir");
        String outputDirName = argMap.get("--outputDir");
        String coveragePath = argMap.get("--coveragePath");
        String scriptArgs = argMap.get("--scriptArgs");  // 脚本额外参数，例如rmq的 -DfailIfNoTests=false

        Project.ProjectType type = projectType.equals("mvn") ? Project.ProjectType.MAVEN : Project.ProjectType.ANT;

        Project.ProjectBuilder builder = Project.builder()
                .setBasePath(basePath)
                .setProjectType(type)
                .withSrcPattern(srcPattern)
                .buildOutputDirName(buildOutputDirName)
                .setMutantRunnerOutputPath(outputDirName);

        for (String mutator : mutatorList) {
            builder.setMutator(MutatorType.valueOf(mutator));
        }

        Project project = builder.build();

        if (isDocker) {
            logger.info("Using docker runner");
            DockerRunner dockerRunner = new DockerRunner(3, argMap.get("--dockerfile"), argMap.get("--projectPathInDocker"), project, argMap);
            dockerRunner.run();
        } else if (isPartition) {
            logger.info("Using partition runner");
            Constants.isPartition = true; // 目前给Project看，不会删除已经映射到docker内的文件夹
            int id = Integer.parseInt(argMap.get("--partition").split("-")[0]);
            int partitionCnt = Integer.parseInt(argMap.get("--partition").split("-")[1]);
            PartitionRunner partitionRunner = new PartitionRunner(id, partitionCnt, project);
            partitionRunner.run();
        } else if (isCoverage) {
            logger.info("Using coverage runner");
            CoverageBasedRunner coverageBasedRunner = new CoverageBasedRunner(project, coveragePath);
            coverageBasedRunner.run();
        }
        else {
            logger.info("Using normal runner");
            AllRunner allRunner = new AllRunner(project);
            allRunner.run();
        }

    }

    public static void mutateHDFS() {
        Project project = Project.builder()
                .setBasePath(Config.HDFS_PROJECT_PATH)
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
                .setMutantRunnerOutputPath("/home/zdc/outputs/rmqMutant")
                .build();


        MutantGenerator mutantGenerator = new MutantGenerator(project);
        mutantGenerator.generateMutantsWithoutFilterEq();

    }

    protected static void mutantRmq() {
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


    protected static void mutantCas() {
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

    private static void setUp() {
        try {
            if (!new File(Config.MUTANT_PATH).exists())
                new File(Config.MUTANT_PATH).mkdirs();
            if (!new File(Config.ORIGINAL_PATH).exists())
                new File(Config.ORIGINAL_PATH).mkdirs();
            if (!new File(Config.OUTPUTS_PATH).exists())
                new File(Config.OUTPUTS_PATH).mkdirs();
            if (!new File(Config.ORIGINAL_BYTECODE_PATH).exists())
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
