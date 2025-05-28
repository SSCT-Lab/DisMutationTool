package io.dismute.mutator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiscardExceptionOperator extends MutatorBase {
    private static final Logger logger = LogManager.getLogger(DiscardExceptionOperator.class);
    protected ArrayList<String> exceptions = new ArrayList<>();
    protected String targetException = "";
    protected String targetExceptionClassName = "";
    protected MutatorType mutator;

    protected boolean continueProcess(CompilationUnit cu) {
        return true;
    }

    @Override
    public List<Mutant> execute(String originalFilePath) {
        targetExceptionClassName = targetException.substring(targetException.lastIndexOf(".") + 1);
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        if (!continueProcess(cu)) { // 文件过滤器，RFE使用
            return res;
        }
        // 遍历所有throw
        List<ThrowStmt> throwStmts = cu.findAll(ThrowStmt.class);
        int mutantNo = 0;
        for (int i = 0; i < throwStmts.size(); i++) {
            ThrowStmt throwStmt = throwStmts.get(i);
            if (!(throwStmt.getExpression() instanceof ObjectCreationExpr))
                continue;
            try {
                ResolvedType resolvedType = throwStmt.getExpression().asObjectCreationExpr().getType().resolve();
                if (!resolvedType.isReferenceType())
                    continue;
                Optional<ResolvedReferenceTypeDeclaration> resolvedReferenceTypeDeclaration = resolvedType.asReferenceType().getTypeDeclaration();
                String packageName = resolvedReferenceTypeDeclaration.get().getPackageName();
                String className = resolvedReferenceTypeDeclaration.get().getName();
                String qualifiedName = packageName + "." + className;
                if (!exceptions.contains(qualifiedName))
                    continue;
                // 找到了需要upcast的异常，在cuCopy上进行变异
                CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                ThrowStmt throwStmtCopy = cuCopy.findAll(ThrowStmt.class).get(i);

                // 创建一个 if (false) 语句
                IfStmt ifStmt = new IfStmt();
                ifStmt.setCondition(new BooleanLiteralExpr(false));

                // 创建一个 BlockStmt 并将 throw 语句添加到其中
                BlockStmt blockStmt = new BlockStmt();
                blockStmt.addStatement(throwStmt);

                // 将 blockStmt 设置为 if 语句的 then 部分
                ifStmt.setThenStmt(blockStmt);
                throwStmtCopy.replace(ifStmt);

                // 写入文件
                int lineNo = throwStmtCopy.getRange().get().begin.line;
                res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
            } catch (UnsolvedSymbolException e) {

            }
        }
        return res;
    }
}
