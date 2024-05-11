package com.example.mutator.network;

import com.example.mutator.Mutant;
import com.example.mutator.MutantGen;
import com.example.mutator.MutatorType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RRC extends MutantGen {

    public static MutatorType mutator = MutatorType.RRC;

    private static final Logger logger = LogManager.getLogger(RRC.class);

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        // 遍历所有的finally块
        List<TryStmt> tryStmts = cu.findAll(TryStmt.class);
        int mutantNo = 0;
        for (int i = 0; i < tryStmts.size(); i++) {
            TryStmt tryStmt = tryStmts.get(i);
            if (tryStmt.getFinallyBlock().isPresent()) {
                List<MethodCallExpr> methodCallExprs = tryStmt.getFinallyBlock().get().findAll(MethodCallExpr.class);
                for (int j = 0; j < methodCallExprs.size(); j++) {
                    MethodCallExpr methodCallExpr = methodCallExprs.get(j);
                    if (methodCallExpr.getName().asString().equals("close")) { // 找到finally块中的所有close方法
                        try {
                            ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                            String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                            if (packageAndClassName.equals("java.net.Socket") || packageAndClassName.equals("java.net.ServerSocket")) { // 判断close方法的调用者是否是Socket或ServerSocket TODO 其他shutdown方法
                                // 生成变异体
                                // 在拷贝cu上删除close方法
                                CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                                MethodCallExpr methodCallExprInCopy = cuCopy.findAll(TryStmt.class).get(i).getFinallyBlock().get().findAll(MethodCallExpr.class).get(j);
                                methodCallExprInCopy.removeForced();
                                // 写入文件
                                int lineNo = methodCallExprInCopy.getRange().get().begin.line;
                                res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
                            }
                        } catch (UnsolvedSymbolException e) {
                            // logger.info("Unsolved 'close' SymbolException in methodCallExpr - " + throwStmt.getExpression().asObjectCreationExpr().getType());
                        }
                    }
                }
            }
        }
        return res;
    }
}