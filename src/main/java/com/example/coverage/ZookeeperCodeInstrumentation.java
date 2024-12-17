package com.example.coverage;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ZookeeperCodeInstrumentation {
    private static final String SOURCE_DIR = "/home/zdc/Desktop/apache-zookeeper-3.5.8/zookeeper-server";
//    private static final String SOURCE_DIR = "/home/zdc/Desktop/apache-cassandra-3.11.6-src/src/java";
    private static final String OUTPUT_FILE = "/home/zdc/Desktop/outputs.txt";

    public static void main(String[] args) throws IOException {
        // 清空输出文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            writer.write(""); // 写入空内容以清空文件
        }

        // 获取所有源码文件，排除测试代码
        List<Path> javaFiles = Files.walk(Paths.get(SOURCE_DIR))
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> !path.toString().toLowerCase().contains("test")) // 排除测试代码
                .collect(Collectors.toList());

        // 对每个 Java 文件进行处理
        for (Path javaFile : javaFiles) {
            processJavaFile(javaFile);
        }
    }

    private static void processJavaFile(Path javaFile) throws IOException {
        // 读取文件内容
        String sourceCode = readFile(javaFile);

        // 解析源码
        CompilationUnit compilationUnit = StaticJavaParser.parse(sourceCode);

        // 遍历类和方法，插入代码
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            String className = clazz.getFullyQualifiedName().orElse("UnknownClass");

            clazz.findAll(MethodDeclaration.class).forEach(method -> {
                // 获取方法名
                String methodName = method.getNameAsString();
                String fullMethodName = className + "." + methodName;

                System.out.println("Instrumenting: " + fullMethodName);

                // 插入日志代码
                method.getBody().ifPresent(body -> {
                    // 插桩代码
//                    String logCode = String.format(
//                            "try (java.io.BufferedWriter xxwriter = java.nio.file.Files.newBufferedWriter(java.nio.file.Paths.get(\"%s\"), java.nio.charset.StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) { " +
//                                    "xxwriter.write(\"%s\\n\"); } catch (Exception xxexp) { xxexp.printStackTrace(); }",
//                            OUTPUT_FILE.replace("\\", "\\\\"), fullMethodName);

                    String logCode = String.format("System.out.println(\"Executing: %s\");", fullMethodName);
//                    String logCode = "System.out.print(\"\");";


                    body.addStatement(0, StaticJavaParser.parseStatement(logCode));
                });
            });
        });

        // 保存修改后的源码
        writeFile(javaFile, compilationUnit.toString());
    }

    private static String readFile(Path filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    private static void writeFile(Path filePath, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
