package com.example.testRunner;

import com.example.Project;
import com.example.mutantgen.MutantGenerator;
import com.example.mutator.Mutant;
import com.example.utils.Constants;
import com.example.utils.MutantUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class DockerRunner {
    private static final Logger logger = LogManager.getLogger(AllRunner.class);

    private int containerCnt;
    private String dockerfilePath;
    private String[] containerIds;
    private DockerClient dockerClient;
    private String imageId;
    private String projectPathInDocker;
    private Project project;
    private Map<String, String> originalArgMap;
    private String jarFileName;


    public DockerRunner(int containerCnt, String dockerfilePath, String projectPathInDocker, Project project, Map<String, String> originalArgMap) {
        this.containerCnt = containerCnt;
        this.dockerfilePath = dockerfilePath;
        this.containerIds = new String[containerCnt];
        this.projectPathInDocker = projectPathInDocker;
        this.project = project;
        this.originalArgMap = originalArgMap;
    }

    // 构建docker容器，分发变异体，容器内使用all runner运行测试

    public void run() {
        // 生成变异体，并序列化
        MutantGenerator mutantGenerator = new MutantGenerator(project);
        List<Mutant> mutants = mutantGenerator.generateMutants();
        MutantUtil.serializeMutantLs(mutants);

        // 构建docker容器
        dockerClient = DockerClientBuilder.getInstance().build();
        File dockerfile = new File(dockerfilePath);
        String imageId = dockerClient.buildImageCmd(dockerfile)
                .exec(new BuildImageResultCallback())
                .awaitImageId();
        this.imageId = imageId;

        // 设置主机和容器中的目录路径
        String hostDir = Project.MUTANT_OUTPUT_PATH;
        String containerDir = Constants.dockerOutputsBaseDir;

        // 创建容器，并绑定目录
        for (int i = 0; i < containerCnt; i++) {
            CreateContainerResponse container = dockerClient.createContainerCmd(imageId)
                    .withHostConfig(new HostConfig().withBinds(new Bind(hostDir, new Volume(containerDir))))
                    .exec();
            containerIds[i] = container.getId();
        }


        // 复制jar文件到容器中,启动容器
        for (int i = 0; i < containerCnt; i++) {
            dockerClient.startContainerCmd(containerIds[i]).exec();
            copyJarToContainer(containerIds[i]);
            // 容器中执行命令
            String[] args = new String[]{
                    "--partition=" + i + "-" + containerCnt,
                    "--projectPath=" + projectPathInDocker,
                    "--outputPath=" + containerDir,
                    "--projectType=" + originalArgMap.get("projectType"),
                    "--srcPattern=" + originalArgMap.get("srcPattern"),
                    "--buildOutputDir=" + originalArgMap.get("buildOutputDir"),
                    "--outputDir=" + originalArgMap.get("outputDir"),
            };
            String arg = String.join(" ", args);
            int finalI = i;
            new Thread(() -> {
                dockerClient.execCreateCmd(containerIds[finalI])
                        .withAttachStdout(true)
                        .withCmd("sh", "-c", "cd / && java -jar " + jarFileName + " " + arg)
                        .exec();
            }).start();
        }

    }

    private void copyJarToContainer(String containerId) {
        File jarFile;
        try {
            // 获取自身的jar文件路径
            jarFile = new File(DockerRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String jarFilePath = jarFile.getAbsolutePath();
            String jarFileName = jarFile.getName();
            this.jarFileName = jarFileName;
            logger.info("JAR file path: " + jarFilePath);
            logger.info("JAR file name: " + jarFileName);
            // 复制jar文件到容器中
            File fileToCopy = new File(jarFilePath);
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withHostResource(fileToCopy.getAbsolutePath())
                    .withRemotePath("/")
                    .exec();
        } catch (URISyntaxException e) {
            logger.error("Failed to get JAR file path", e);
            throw new RuntimeException(e);
        }


//        // 复制 JAR 文件到容器的根目录
//        try (InputStream uploadStream = Files.newInputStream(jarFile.toPath())) {
//            dockerClient.copyArchiveToContainerCmd(containerId)
//                    .withTarInputStream(uploadStream)
//                    .withRemotePath("/") // 复制到容器根目录
//                    .exec();
//        } catch (IOException e) {
//            logger.error("Failed to copy JAR file to container", e);
//            throw new RuntimeException(e);
//        }
    }


}
