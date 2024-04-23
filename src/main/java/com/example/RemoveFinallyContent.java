package com.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.TryStmt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RemoveFinallyContent {

    public String FILE_PATH;

    public RemoveFinallyContent(String filePath) {
        this.FILE_PATH = filePath;
    }

    public void execute(){
        try {
            // 解析现有的Java文件
            FileInputStream in = new FileInputStream(FILE_PATH);
            CompilationUnit cu = StaticJavaParser.parse(in);

            // 遍历所有的try语句
            cu.findAll(TryStmt.class).forEach(tryStmt -> {
                // 检查是否存在finally块
                if (tryStmt.getFinallyBlock().isPresent()) {
                    // 删除finally块中的所有内容
                    tryStmt.getFinallyBlock().get().getStatements().clear();
                }
            });

            // 输出修改后的代码
            System.out.println(cu.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}