package com.example.utils;

public class Constants {
    public static final String persistMutantsName = "mutants.ser"; // 分发任务时，将变异体序列化后的文件名
    public static final String dockerOutputsBaseDir = "/mutantOutputs";  // docker容器中输出的根目录
    public static boolean isPartition = false;
    public static String[] excludeSrcFiles = new String[] {"InternalTopologyBuilder"};
    public static final String buildMvmCmd = "mvn clean compile";
    public static final String buildGradleCmd = "/gradlew clean compileJava";
}
