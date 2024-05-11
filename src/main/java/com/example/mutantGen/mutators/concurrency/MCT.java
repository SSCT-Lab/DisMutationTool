package com.example.mutantGen.mutators.concurrency;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MCT extends MutantGen {

    public static MutatorType mutator = MutatorType.MCT;

    private static final int FACTOR = 10;

    private static final Logger logger = LogManager.getLogger(MCT.class);


    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new LinkedList<>();
        parse(originalFilePath);
        List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);
        int mutantNo = 0;
        for (int i = 0; i < methodCallExprs.size(); i++) {
            MethodCallExpr methodCallExpr = methodCallExprs.get(i);
            // 匹配wait(long timeout)或者 wait(long timeout, int nanos) 这种类型的方法
            if (methodCallExpr.getNameAsString().equals("wait")) {
                if (methodCallExpr.getArguments().size() == 1) {
                    try {
                        if (methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) {
                            res.add(generateMutant(i, 0, ++mutantNo, originalFilePath));
                        }
                    } catch (UnsolvedSymbolException e) {
                    }
                } else if (methodCallExpr.getArguments().size() == 2) {
                    try {
                        if (methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()
                                && methodCallExpr.getArguments().get(1).calculateResolvedType().isNumericType()) {
                            res.add(generateMutant(i, 0, ++mutantNo, originalFilePath));
                        }
                    } catch (UnsolvedSymbolException e) {
                    }
                }
                // 匹配Thread.sleep(long millis)
            } else if (methodCallExpr.getNameAsString().equals("sleep")
                    && methodCallExpr.getArguments().size() == 1
                    && methodCallExpr.getScope().isPresent()
                    && methodCallExpr.getScope().toString().equals("Thread")
                    && methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) {
                res.add(generateMutant(i, 0, ++mutantNo, originalFilePath));
                // 匹配ExecutorService.awaitTermination(long timeout, TimeUnit unit)
            } else if (methodCallExpr.getNameAsString().equals("awaitTermination")
                    && methodCallExpr.getArguments().size() == 2) {
                try { // 尝试resolve ExecutorService
                    ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                    String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                    if (packageAndClassName.equals("java.util.concurrent.ExecutorService")
                            && methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) {
                        res.add(generateMutant(i, 0, ++mutantNo, originalFilePath));
                    }
                } catch (UnsolvedSymbolException e) {
                }
                // 匹配CountDownLatch.await(long timeout, TimeUnit unit)
            } else if (methodCallExpr.getNameAsString().equals("await")
                    && methodCallExpr.getArguments().size() == 2) {
                try {
                    ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                    String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                    if (packageAndClassName.equals("java.util.concurrent.CountDownLatch")
                            && methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) {
                        res.add(generateMutant(i, 0, ++mutantNo, originalFilePath));
                    }
                } catch (UnsolvedSymbolException e) {
                }
                // 匹配CyclicBarrier.await(long timeout, TimeUnit unit)
            } else if (methodCallExpr.getNameAsString().equals("await")
                    && methodCallExpr.getArguments().size() == 2) {
                try {
                    ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                    String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                    if (packageAndClassName.equals("java.util.concurrent.CyclicBarrier")
                            && methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) {
                        res.add(generateMutant(i, 0, ++mutantNo, originalFilePath));
                    }
                } catch (UnsolvedSymbolException e) {
                }
            }
        }
        return res;
    }


    private Mutant generateMutant(int exprNo, int argIndex, int mutantNo, String originalFilePath) {
        // 在cuCopy上进行修改
        CompilationUnit cuCopy = generateCuCopy(originalFilePath);
        MethodCallExpr methodCallExprCopy = cuCopy.findAll(MethodCallExpr.class).get(exprNo);

        // 暂存methodCallExpr的第argIndex个参数
        Expression originalArg = methodCallExprCopy.getArgument(argIndex);
        // 将methodCallExpr的第argIndex个参数替换为原参数除以FACTOR
        methodCallExprCopy.setArgument(0, new BinaryExpr(
                new EnclosedExpr(originalArg),
                new LongLiteralExpr(String.valueOf(FACTOR)),
                BinaryExpr.Operator.DIVIDE));
        // 写入变异体
        return generateMutantAndSaveToFile(mutantNo, methodCallExprCopy.getRange().get().begin.line, mutator, originalFilePath, cuCopy);
    }
}
