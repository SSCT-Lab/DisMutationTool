package com.example.mutator.deprecated;

import com.example.mutator.Mutant;
import com.example.mutator.MutantGen;
import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* modify wait timeout
 * 将wait(timeout)中的timeout修改为timeout/2
 * */

public class MWT extends MutantGen {
    public static MutatorType mutator = MutatorType.MWT;

    private static final int FACTOR = 10;

    private static final Logger logger = LogManager.getLogger(MWT.class);

    public List<Mutant> execute(String originalFilePath) {
        try {
            List<Mutant> res = new ArrayList<>();

            // 创建一个类型解析器
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(new ReflectionTypeSolver());
            // 如果您有源代码的路径，也可以添加JavaParserTypeSolver
            // combinedTypeSolver.add(new JavaParserTypeSolver(new File("src/main/java")));

            // 配置JavaParser使用符号解析器
            StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));


            // 解析现有的Java文件
            FileInputStream in = new FileInputStream(originalFilePath);
            CompilationUnit cu = StaticJavaParser.parse(in);
            LexicalPreservingPrinter.setup(cu); // 尽可能保留原始格式
            List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);


            int mutantNo = 0;
            for (int i = 0; i < methodCallExprs.size(); i++) {
                MethodCallExpr methodCallExpr = methodCallExprs.get(i);
                // 匹配wait(<Number>)这种类型的方法
                if (methodCallExpr.getNameAsString().equals("wait")
                        && methodCallExpr.getArguments().size() == 1
                        && methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) {
                    mutantNo += 1;
                    // 修改方法调用，将timeout修改为timeout/2，生成变异体，撤销修改
                    Mutant mutant = genMutant(originalFilePath, methodCallExpr, mutantNo, cu);
                    res.add(mutant);
                }
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error generating MWT mutant for file" + originalFilePath);
            return null;
        }
    }

    /**
     * 生成一个MWT变异体：修改方法调用，将timeout修改为timeout/2，生成变异体，撤销修改
     * @param originalFilePath 原文件路径
     * @param methodCallExpr 要变异的方法调用
     * @param mutantNo 变异体编号
     * @param cu CompilationUnit
     * @return
     */
    private Mutant genMutant(String originalFilePath, MethodCallExpr methodCallExpr, int mutantNo, CompilationUnit cu) {
        // 暂存methodCallExpr的第一个参数
        Expression originalArg = methodCallExpr.getArguments().get(0);
        // 暂存原始参数
        Expression originalArgClone = originalArg.clone();
        // 修改方法调用
        methodCallExpr.setArgument(0, new BinaryExpr(
                new EnclosedExpr(originalArg),
                new LongLiteralExpr(String.valueOf(FACTOR)),
                BinaryExpr.Operator.DIVIDE));
        // 写入变异体
        int lineNo = methodCallExpr.getRange().get().begin.line;
        // 回写到文件
        String mutantName = FileUtil.getFileName(originalFilePath) + "_" + mutator + "_" + mutantNo + ".java";
        String mutantPath = new File("./mutants/").getAbsolutePath() + "/" + mutantName;
        logger.info("Generating mutant: " + mutantName);
        FileUtil.writeToFile(LexicalPreservingPrinter.print(cu), mutantPath);
        // 撤销修改
        methodCallExpr.setArgument(0, originalArgClone);
        return new Mutant(lineNo, mutator, originalFilePath, mutantPath);
    }
}
