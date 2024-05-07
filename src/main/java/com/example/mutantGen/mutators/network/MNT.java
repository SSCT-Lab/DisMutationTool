package com.example.mutantGen.mutators.network;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
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


/*
 * Modify Socket Timeout
 *
 * */
public class MNT extends MutantGen {
    public static final MutatorType mutator = MutatorType.MST;
    private static final Logger logger = LogManager.getLogger(MNT.class);
    private static final int FACTOR = 10;

    public List<Mutant> execute(String originalFilePath) {

        try {
            List<Mutant> res = new ArrayList<>();
            // Set up a minimal type solver that only looks at the classes used to run this sample.
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(new ReflectionTypeSolver());

            // Configure JavaParser to use type resolution
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
            StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

            // 解析现有的Java文件
            FileInputStream in = new FileInputStream(originalFilePath);
            CompilationUnit cu = StaticJavaParser.parse(in);
            LexicalPreservingPrinter.setup(cu); // 尽可能保留原始格式
            List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);

            int mutantNo = 0;
            for (int i = 0; i < methodCallExprs.size(); i++) {
                MethodCallExpr methodCallExpr = methodCallExprs.get(i);
                if (methodCallExpr.getName().asString().equals("connect")) {
                    // 检查方法的调用者是否是Socket
                    try {
                        ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                        String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                        if (packageAndClassName.equals("java.net.Socket")
                                && methodCallExpr.getArguments().size() == 2
                                && methodCallExpr.getArguments().get(1).calculateResolvedType().isNumericType()) {
                            mutantNo += 1;
                            Mutant mutant = genMutant(originalFilePath, methodCallExpr, mutantNo, cu, 1);
                            res.add(mutant);
                        }
                    } catch (UnsolvedSymbolException e) { // 防止引用到项目的其他文件导致解析失败
                        logger.info("UnsolvedSymbolException in methodCallExpr - " + methodCallExpr);
                    }
                } else if (methodCallExpr.getName().asString().equals("setSoTimeout")) {
                    try {
                        ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                        String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                        if (packageAndClassName.equals("java.net.Socket") || packageAndClassName.equals("java.net.ServerSocket")
                                && methodCallExpr.getArguments().size() == 1
                                && methodCallExpr.getArguments().get(1).calculateResolvedType().isNumericType()) {
                            mutantNo += 1;
                            Mutant mutant = genMutant(originalFilePath, methodCallExpr, mutantNo, cu, 0);
                            res.add(mutant);
                        }
                    } catch (UnsolvedSymbolException e) { // 防止引用到项目的其他文件导致解析失败
                        logger.info("UnsolvedSymbolException in methodCallExpr - " + methodCallExpr);
                    }
                }
            }

            return res;

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error generating MWT mutant for file" + originalFilePath);
            return null;
        }
    }


    private Mutant genMutant(String originalFilePath, MethodCallExpr methodCallExpr, int mutantNo, CompilationUnit
            cu, int argPos) {
        // 暂存methodCallExpr的第一个参数
        Expression originalArg = methodCallExpr.getArguments().get(argPos);
        // 暂存原始参数
        Expression originalArgClone = originalArg.clone();
        // 修改方法调用
        methodCallExpr.setArgument(argPos, new BinaryExpr(
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
        methodCallExpr.setArgument(argPos, originalArgClone);
        return new Mutant(lineNo, mutator, originalFilePath, mutantPath);
    }


}
