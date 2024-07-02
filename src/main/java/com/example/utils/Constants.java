package com.example.utils;

public class Constants {
    public static final String persistMutantsName = "mutants.ser"; // 分发任务时，将变异体序列化后的文件名
    public static final String dockerOutputsBaseDir = "/mutantOutputs";  // docker容器中输出的根目录
    public static final String jarPath = "/home/zdc/code/DisMutationTool/target/DisMutationTool-1.0-SNAPSHOT-jar-with-dependencies.jar"; // TODO 修改
    public static final String jarPathInDocker = "/DisMutationTool-1.0-SNAPSHOT-jar-with-dependencies.jar"; // dockerfile路径
}
