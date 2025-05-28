package io.dismute.mutator.network;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutator.MutatorBase;
import io.dismute.mutantgen.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


/*
 * Modify Socket Timeout
 *
 * */
public class MNT extends MutatorBase {
    public static final MutatorType mutator = MutatorType.MNT;
    private static final Logger logger = LogManager.getLogger(MNT.class);
    private static final int FACTOR = 10;

    public List<Mutant> execute(String originalFilePath) {

        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);

        int mutantNo = 0;
        for (int i = 0; i < methodCallExprs.size(); i++) {
            MethodCallExpr methodCallExpr = methodCallExprs.get(i);
            // Socket.connect(peer, timeout)
            if (methodCallExpr.getName().asString().equals("connect")) {
                try {
                    ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                    String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                    if (packageAndClassName.equals("java.net.Socket")
                            && methodCallExpr.getArguments().size() == 2
                            && methodCallExpr.getArguments().get(1).calculateResolvedType().isNumericType()) { // UnsolvedSymbolException is thrown if the type is not numeric
                        Mutant mutant = genMutant(originalFilePath, i, ++mutantNo, 1);
                        res.add(mutant);
                    }
                } catch (UnsolvedSymbolException e) { // 防止引用到项目的其他文件导致解析失败
                    logger.info("UnsolvedSymbolException in methodCallExpr - " + methodCallExpr);
                }
            // Socket.setSoTimeout(timeout) OR ServerSocket.setSoTimeout(timeout)
            } else if (methodCallExpr.getName().asString().equals("setSoTimeout")) {
                try {
                    ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                    String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                    if (packageAndClassName.equals("java.net.Socket") || packageAndClassName.equals("java.net.ServerSocket")
                            && methodCallExpr.getArguments().size() == 1
                            && methodCallExpr.getArguments().get(0).calculateResolvedType().isNumericType()) { // UnsolvedSymbolException is thrown if the type is not numeric
                        Mutant mutant = genMutant(originalFilePath, i, ++mutantNo, 0);
                        res.add(mutant);
                    }
                } catch (UnsolvedSymbolException e) { // 防止引用到项目的其他文件导致解析失败
                    logger.info("UnsolvedSymbolException in methodCallExpr - " + methodCallExpr);
                }
            }
        }
        return res;
    }

    private Mutant genMutant(String originalFilePath, int methodCallExprNo, int mutantNo, int argPos) {
        // 在cuCopy上进行修改
        CompilationUnit cuCopy = generateCuCopy(originalFilePath);
        MethodCallExpr methodCallExprCopy = cuCopy.findAll(MethodCallExpr.class).get(methodCallExprNo);
        Expression originalArg = methodCallExprCopy.getArguments().get(argPos); // 原始timeout的表达式
        methodCallExprCopy.setArgument(argPos, new BinaryExpr(
                new EnclosedExpr(originalArg),
                new LongLiteralExpr(String.valueOf(FACTOR)),
                BinaryExpr.Operator.DIVIDE));
        // 写入变异体
        int lineNo = methodCallExprCopy.getRange().get().begin.line;
        return generateMutantAndSaveToFile(mutantNo, lineNo, mutator, originalFilePath, cuCopy);
    }


}
