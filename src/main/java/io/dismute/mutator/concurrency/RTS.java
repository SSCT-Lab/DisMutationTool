package io.dismute.mutator.concurrency;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutator.MutatorBase;
import io.dismute.mutantgen.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RTS extends MutatorBase {

    private static final Logger logger = LogManager.getLogger(RTS.class);
    public static final MutatorType mutator = MutatorType.RTS;

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        int mutantNo = 0;
        for (int i = 0; i < methodDeclarations.size(); i++) {
            MethodDeclaration methodDeclaration = methodDeclarations.get(i);
            if (methodDeclaration.getBody().isPresent()) {
                List<MethodCallExpr> methodCallExprs = methodDeclaration.findAll(MethodCallExpr.class);
                Stack<Integer> lockStack = new Stack<>();  // 用于配对嵌套的lock-unlock

                for (int j = 0; j < methodCallExprs.size(); j++) {
                    MethodCallExpr methodCallExpr = methodCallExprs.get(j);
                    String methodName = methodCallExpr.getNameAsString();
                    String scope = methodCallExpr.getScope().map(Object::toString).orElse("");
                    // 只处理lock和unlock方法
                    if ((methodName.equals("lock") || methodName.equals("lockInterruptibly") || methodName.equals("tryLock")) && !scope.isEmpty()) {
                        lockStack.push(j);
                    } else if (methodName.equals("unlock") && !scope.isEmpty()) {
                        if(lockStack.isEmpty()) {
                            logger.warn("Unlock without lock at file" + originalFilePath + " " + methodCallExpr.getRange().get());
                            continue;
                        }
                        int lockMethodCallExprIndex = lockStack.pop();
                        mutantNo++;
                        Mutant mutant = generateMutant(originalFilePath, i, lockMethodCallExprIndex, j, mutantNo);
                        res.add(mutant);
                    }
                }
            }
        }
        return res;
    }

    private Mutant generateMutant(String originalFilePath, int methodDeclarationIndex, int lockMethodCallExprIndex, int unlockMethodCallExprIndex, int mutantNo) {
        CompilationUnit cuCopy = generateCuCopy(originalFilePath);
        MethodDeclaration methodDeclarationCopy = cuCopy.findAll(MethodDeclaration.class).get(methodDeclarationIndex);
        MethodCallExpr lockMethodCallExpr = methodDeclarationCopy.findAll(MethodCallExpr.class).get(lockMethodCallExprIndex);
        MethodCallExpr unlockMethodCallExpr = methodDeclarationCopy.findAll(MethodCallExpr.class).get(unlockMethodCallExprIndex);
        lockMethodCallExpr.removeForced();
        unlockMethodCallExpr.removeForced();
        //写入变异体文件
        int lineNo = lockMethodCallExpr.getRange().get().begin.line;
        return generateMutantAndSaveToFile(mutantNo, lineNo, mutator, originalFilePath, cuCopy);
    }
}
