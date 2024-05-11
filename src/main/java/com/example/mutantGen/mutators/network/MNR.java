package com.example.mutantGen.mutators.network;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MNR extends MutantGen {

    public static MutatorType mutator = MutatorType.MNR;

    private static final Logger logger = LogManager.getLogger(MNR.class);

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<IfStmt> ifStmts = cu.findAll(IfStmt.class);
        int mutantNo = 0;
        // 遍历所有的if语句
        for (int i = 0; i < ifStmts.size(); i++) {
            IfStmt ifStmt = ifStmts.get(i);
            // 遍历if的condition中所有的方法调用
            List<MethodCallExpr> methodCallExprs = ifStmt.getCondition().findAll(MethodCallExpr.class);
            for (int j = 0; j < methodCallExprs.size(); j++) {
                MethodCallExpr methodCallExpr = methodCallExprs.get(j);
                boolean removeMethodCall = false;
                try {
                    ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                    String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                    // 判断是否是网络资源的if检查
                    if (packageAndClassName.equals("java.net.Socket") || packageAndClassName.equals("java.net.ServerSocket")) {
                        if (methodCallExpr.getName().asString().equals("isClosed") || methodCallExpr.getName().asString().equals("isBound")) {
                            removeMethodCall = true;
                        }
                    }
                    if (packageAndClassName.equals("ServerSocketChannel") || packageAndClassName.equals("NetworkChannel") || packageAndClassName.equals("SocketChannel")) {
                        if (methodCallExpr.getName().asString().equals("isOpen")) {
                            removeMethodCall = true;
                        }
                    }
                    if (packageAndClassName.equals("ServerSocketChannel")
                            && (methodCallExpr.getName().asString().equals("isConnected") || methodCallExpr.getName().asString().equals("isConnectionPending"))) {
                        removeMethodCall = true;
                    }
                } catch (UnsolvedSymbolException e) {
                    // logger.info("UnsolvedSymbolException in methodCallExpr - " + throwStmt.getExpression().asObjectCreationExpr().getType());
                }
                // 删除网络资源的if检查

                if (removeMethodCall) {
                    // 生成变异体
                    // 在拷贝cu上进行修改
                    CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                    IfStmt ifStmtCopy = cuCopy.findAll(IfStmt.class).get(i);
                    ifStmtCopy.setCondition(new BooleanLiteralExpr(true));

                    // TODO 判断删除之后，if的condition是否为空，如果为空则删除整个if
                    // 写入文件
                    int lineNo = ifStmt.getRange().get().begin.line;
                    res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
                }
            }
        }

        return res;
    }
}
