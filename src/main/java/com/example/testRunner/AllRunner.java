package com.example.testRunner;

import com.example.Project;
import com.example.mutantgen.MutantGenerator;
import com.example.mutantrun.MutantRunnerScript;
import com.example.mutator.Mutant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AllRunner {
    private static final Logger logger = LogManager.getLogger(AllRunner.class);
    private List<Mutant> mutantLs;
    private final Project project;

    public AllRunner(Project project) {
        this.project = project;
    }

    public void run() {
        // 定义资源路径
        String resourcePath = project.getProjectType() == Project.ProjectType.MAVEN ? "bin/mvn-runner-no-breaking.sh" : "bin/ant-runner-no-breaking.sh";

        try {
            // 从资源中读取脚本文件
            InputStream resourceStream = AllRunner.class.getClassLoader().getResourceAsStream(resourcePath);
            if (resourceStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            // 创建临时目录和文件
            Path tempDir = Files.createTempDirectory("resources");
            Path tempFile = Paths.get(tempDir.toString(), "mvn.sh");

            // 将脚本文件复制到临时文件
            Files.copy(resourceStream, tempFile);

            // 获取绝对路径
            String absolutePath = tempFile.toAbsolutePath().toString();
            logger.info("Absolute path of mvn.sh: " + absolutePath);

            // 确保临时文件具有执行权限
            tempFile.toFile().setExecutable(true);

            MutantGenerator mutantGenerator = new MutantGenerator(project);
            mutantLs = mutantGenerator.generateMutants();

            for (Mutant mutant : mutantLs) {
                MutantRunnerScript mutantRunner = new MutantRunnerScript(mutant, project);
                mutantRunner.run(absolutePath, "");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }


//        TestSuiteRunner runner = project.getProjectType() == Project.ProjectType.MAVEN ? new MvnRunner() : new AntRunner();
//        for (Mutant mutant: mutantLs){
//            MutantRunner mutantRunner = new MutantRunner(mutant, project, runner);
//            mutantRunner.run();
//        }


    }
}
