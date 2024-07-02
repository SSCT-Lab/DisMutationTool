package com.example.testRunner;

import com.example.Project;
import com.example.mutantgen.MutantGenerator;
import com.example.mutantrun.MutantRunnerScript;
import com.example.mutator.Mutant;
import com.example.utils.Constants;
import com.example.utils.MutantUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Getter
public class PartitionRunner{
    private static final Logger logger = LogManager.getLogger(PartitionRunner.class);

    private int id; // 从0开始
    private int partitionCnt;
    private Project project;
    private List<Mutant> mutantLs;
    public PartitionRunner(int id, int partitionCnt, Project project, List<Mutant> mutantLs) {
        this.id = id;
        this.partitionCnt = partitionCnt;
        this.project = project;
        this.mutantLs = mutantLs.subList(id * mutantLs.size() / partitionCnt, (id + 1) * mutantLs.size() / partitionCnt);
    }

    public void run() {
        // 定义资源路径
        String resourcePath = project.getProjectType() == Project.ProjectType.MAVEN ? "bin/mvn-runner-no-breaking.sh" : "bin/ant-runner-no-breaking.sh";

        try {
            // 从资源中读取脚本文件
            InputStream resourceStream = PartitionRunner.class.getClassLoader().getResourceAsStream(resourcePath);
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

            // 反序列化，读取mutantLs
            mutantLs = MutantUtil.deserializeMutantLs();
            logger.info("Partition " + id + " has " + mutantLs.size() + " mutants");

            for (Mutant mutant : mutantLs) {
                MutantRunnerScript mutantRunner = new MutantRunnerScript(mutant, project);
                mutantRunner.run(absolutePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

}
