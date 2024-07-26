package com.example.mutator.consistency;

import com.example.mutator.DiscardExceptionOperator;
import com.example.mutator.MutatorType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;

import java.util.ArrayList;

public class RFE extends DiscardExceptionOperator {
    public RFE() {
        exceptions.add("java.io.FileNotFoundException");
        exceptions.add("java.io.IOException");
        exceptions.add("java.lang.SecurityException");
        exceptions.add("java.nio.file.AccessDeniedException");
        exceptions.add("java.nio.file.NoSuchFileException");
        exceptions.add("java.nio.file.FileAlreadyExistsException");
        exceptions.add("java.nio.file.InvalidPathException");
        exceptions.add("java.nio.file.FileSystemException");

        mutator = MutatorType.RFE;

        targetException = "java.lang.Exception";
    }

    @Override
    protected boolean continueProcess(CompilationUnit cu) {
        boolean res = false;
        ArrayList<String> importKeywords = new ArrayList<>();
        importKeywords.add("org.apache.cassandra.config"); // TODO 写到config
        importKeywords.add("org.apache.hadoop.conf");
        importKeywords.add("org.apache.kafka.common.config");

        // 验证当前.java文件的package是否包含importKeywords中的任何一个关键字
        if (cu.getPackageDeclaration().isPresent()) {
            for (String keyword : importKeywords) {
                if (cu.getPackageDeclaration().get().getNameAsString().contains(keyword)) {
                    res = true;
                    break;
                }
            }
        }
        // 验证当前.java文件的任何一条import语句是否包含importKeywords中的任何一个关键字
        for (ImportDeclaration importDeclaration : cu.findAll(ImportDeclaration.class)) {
            if (res) {
                break;
            }
            for (String keyword : importKeywords) {
                if (importDeclaration.getNameAsString().contains(keyword)) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }
}
