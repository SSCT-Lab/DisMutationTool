import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class DockerExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 创建Docker客户端
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        // 设置Dockerfile路径
        File dockerfile = new File("/home/zdc/code/DisMutationTool/Dockerfile/rmq/Dockerfile");

        // 构建镜像
        String imageId = dockerClient.buildImageCmd(dockerfile)
                .withTags(Collections.singleton("rmq1:latest"))
                .exec(new BuildImageResultCallback())
                .awaitImageId();

        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(imageId)
                .withCmd("tail", "-f", "/dev/null")
                .exec();

        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();

        System.out.println("Container ID: " + container.getId());

        // 将文件复制到容器的根目录
        File fileToCopy = new File("/home/zdc/code/DisMutationTool/target/DisMutationTool-1.0-SNAPSHOT-jar-with-dependencies.jar");
        dockerClient.copyArchiveToContainerCmd(container.getId())
                .withHostResource(fileToCopy.getAbsolutePath())
                .withRemotePath("/")
                .exec();

        System.out.println("File copied to container");



        // 清理和关闭Docker客户端
        dockerClient.close();
    }
}
