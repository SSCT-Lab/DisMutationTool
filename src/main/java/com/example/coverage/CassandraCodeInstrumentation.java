package com.example.coverage;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class CassandraCodeInstrumentation {
    private static final String SOURCE_DIR = "/home/zdc/Desktop/apache-cassandra-3.11.6-src/src/java";
//    private static final String SOURCE_DIR = "/home/zdc/code/DisMu2/src/main/java/org/example/deprecated/Outer.java";

    public static void main(String[] args) throws IOException {
        // 获取所有源码文件
        List<Path> javaFiles = Files.walk(Paths.get(SOURCE_DIR))
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());

        // 对每个 Java 文件进行处理
        for (Path javaFile : javaFiles) {
            processJavaFile(javaFile);
        }
    }

    private static void processJavaFile(Path javaFile) throws IOException {
        // 读取文件内容
        String sourceCode = readFile(javaFile);

        // 获取文件名（不含扩展名）
        String fileNameWithoutExtension = javaFile.getFileName().toString().replace(".java", "");

        if(fileNameWithoutExtension.endsWith("LogbackLoggingSupport") || fileNameWithoutExtension.endsWith("DataType")){
            // /org/apache/cassandra/utils/logging/
            // /org/apache/cassandra/transport/
            System.out.println("Skipping file: " + javaFile);
            return;
        }

        // 解析源码
        CompilationUnit compilationUnit = StaticJavaParser.parse(sourceCode);

        // 检查是否为接口文件，若是则直接跳过
        boolean isInterfaceFile = compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .anyMatch(ClassOrInterfaceDeclaration::isInterface);
        if (isInterfaceFile) {
            System.out.println("Skipping interface file: " + javaFile);
            return;
        }

        // 强制添加 Logger 的 import 语句
        if (!compilationUnit.getImports().stream()
                .anyMatch(importStmt -> importStmt.getNameAsString().equals("org.slf4j.Logger"))) {
            compilationUnit.addImport("org.slf4j.Logger");
        }
        if (!compilationUnit.getImports().stream()
                .anyMatch(importStmt -> importStmt.getNameAsString().equals("org.slf4j.LoggerFactory"))) {
            compilationUnit.addImport("org.slf4j.LoggerFactory");
        }

        // 遍历所有类，插入日志逻辑
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            // 判断是否为外部类
            if (isOuterClass(clazz)) {
                // 定义 Logger
                String loggerDefinition = String.format("private static final Logger xxlogger = LoggerFactory.getLogger(%s.class);", clazz.getNameAsString());
                clazz.addMember(StaticJavaParser.parseBodyDeclaration(loggerDefinition));
            }

            // 插入日志逻辑（无论是否为外部类）
            clazz.findAll(MethodDeclaration.class).forEach(method -> {
                method.getBody().ifPresent(body -> {
                    String fullClassName = getFullClassName(clazz);
                    String logCode = String.format("xxlogger.info(\"Executing: %s.%s\");", fullClassName, method.getNameAsString());
                    body.addStatement(0, StaticJavaParser.parseStatement(logCode));
                });
            });
        });

        // 保存修改后的源码
        writeFile(javaFile, compilationUnit.toString());
        // System.out.println("Processed and inserted logging statements: " + javaFile);
    }

//    private static void processJavaFile(Path javaFile) throws IOException {
//        // 读取文件内容
//        String sourceCode = readFile(javaFile);
//
//        // 解析源码
//        CompilationUnit compilationUnit = StaticJavaParser.parse(sourceCode);
//
//        // 获取文件名（不含扩展名）
//        String fileNameWithoutExtension = javaFile.getFileName().toString().replace(".java", "");
//
//        // 强制添加 Logger 的 import 语句
//        compilationUnit.addImport("org.slf4j.Logger", true, false);
//        compilationUnit.addImport("org.slf4j.LoggerFactory", true, false);
//
//        // 遍历所有类和枚举，插入日志逻辑
//        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
//            if(clazz.isInterface()){ // 接口不处理
//                return;
//            }
//            if (isOuterClass(clazz)) {
//                // 为外部类定义 Logger
//                String loggerDefinition = String.format("private static final Logger xxlogger = LoggerFactory.getLogger(%s.class);", clazz.getNameAsString());
//                clazz.addMember(StaticJavaParser.parseBodyDeclaration(loggerDefinition));
//            }
//
//            // 为方法插入日志逻辑
//            clazz.findAll(MethodDeclaration.class).forEach(method -> {
//                method.getBody().ifPresent(body -> {
//                    String logCode = String.format("xxlogger.info(\"Executing: %s.%s\");", clazz.getNameAsString(), method.getNameAsString());
//                    body.addStatement(0, StaticJavaParser.parseStatement(logCode));
//                });
//            });
//        });
//
//        // 处理枚举类型
//        compilationUnit.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
//            // 为枚举定义 Logger
//            String loggerDefinition = String.format("private static final Logger xxlogger = LoggerFactory.getLogger(%s.class);", enumDeclaration.getNameAsString());
//            if(!enumDeclaration.isNestedType()) {
//                enumDeclaration.addMember(StaticJavaParser.parseBodyDeclaration(loggerDefinition));
//            }
//
//            // 为枚举方法插入日志
//            enumDeclaration.findAll(MethodDeclaration.class).forEach(method -> {
//                method.getBody().ifPresent(body -> {
//                    String logCode = String.format("xxlogger.info(\"Executing: %s.%s\");", enumDeclaration.getNameAsString(), method.getNameAsString());
//                    body.addStatement(0, StaticJavaParser.parseStatement(logCode));
//                });
//            });
//        });
//
//        // 保存修改后的源码
//        writeFile(javaFile, compilationUnit.toString());
//        System.out.println("Processed and inserted logging statements: " + javaFile);
//    }

    private static boolean isOuterClass(ClassOrInterfaceDeclaration clazz) {
        // 方法中定义的类不是外部类,匿名内部类
        try {
            clazz.getFullyQualifiedName().get();
        } catch (NoSuchElementException e) {
            return false;
        }
        return !( clazz.isInnerClass() || clazz.isNestedType());
    }

    private static String getFullClassName(ClassOrInterfaceDeclaration clazz) {
        StringBuilder fullClassName = new StringBuilder(clazz.getNameAsString());
        ClassOrInterfaceDeclaration parent = clazz;
        while (parent.getParentNode().isPresent() && parent.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
            parent = (ClassOrInterfaceDeclaration) parent.getParentNode().get();
            fullClassName.insert(0, parent.getNameAsString() + ".");
        }
        return fullClassName.toString();
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
