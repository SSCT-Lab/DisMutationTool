package com.example.mutantGen.mutators;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class UpcastExceptionOperator extends MutantGen {

    private static final Logger logger = LogManager.getLogger(UpcastExceptionOperator.class);
    protected static ArrayList<String> exceptions = new ArrayList<>();
    protected static String targetException = "";
    protected static String targetExceptionClassName = "";
    protected static MutatorType mutator;

    @Override
    public List<Mutant> execute(String originalFilePath) {
        targetExceptionClassName = targetException.substring(targetException.lastIndexOf(".") + 1);
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
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
                // 1. 修改import，检测是否导入了targetException
                Optional<ImportDeclaration> targetExceptionImport = cuCopy.getImports().stream()
                        .filter(importDeclaration -> importDeclaration.getName().asString().equals(targetException))
                        .findFirst();
                if (!targetExceptionImport.isPresent()) { // 如果没有导入targetException，添加import
                    ImportDeclaration importDeclaration = new ImportDeclaration(targetException, false, false);
                    cuCopy.addImport(importDeclaration);
                }
                // 2. 修改方法签名，确保能够抛出targetException
                Optional<MethodDeclaration> method = throwStmtCopy.findAncestor(MethodDeclaration.class);
                if (method.isPresent()) {
                    MethodDeclaration methodDecl = method.get();
                    // 检查方法签名是否已经声明抛出任何异常
                    if (methodDecl.getThrownExceptions().isEmpty()) {
                        // 如果没有声明抛出任何异常，则添加throw targetException
                        methodDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType(targetExceptionClassName));
                    } else {
                        // 如果已经声明抛出异常，则额外添加targetException
                        boolean hasTargetException = false;
                        for (ReferenceType thrownException : methodDecl.getThrownExceptions()) {
                            if (thrownException.toString().equals(targetExceptionClassName)) { // 如果已经声明抛出targetException，则不再添加
                                hasTargetException = true;
                                break;
                            }
                        }
                        if (!hasTargetException) { // 方法签名中没有声明抛出targetException
                            methodDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType(targetExceptionClassName));
                        }
                    }
                } else {
                    Optional<ConstructorDeclaration> constructorDeclaration = throwStmtCopy.findAncestor(ConstructorDeclaration.class);
                    if(! constructorDeclaration.isPresent()){
                        logger.warn("failed to find methodDeclaration or constructorDeclaration in throwStmt!");
                        continue;
                    }
                    ConstructorDeclaration constructorDecl = constructorDeclaration.get();
                    if (constructorDecl.getThrownExceptions().isEmpty()) {
                        constructorDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType(targetExceptionClassName));
                    } else {
                        boolean hasTargetException = false;
                        for (ReferenceType thrownException : constructorDecl.getThrownExceptions()) {
                            if (thrownException.toString().equals(targetExceptionClassName)) {
                                hasTargetException = true;
                                break;
                            }
                        }
                        if (!hasTargetException) {
                            constructorDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType(targetExceptionClassName));
                        }
                    }
                }
                // 3. 修改异常类型
                throwStmtCopy.getExpression().asObjectCreationExpr().setType(targetExceptionClassName);
                // 写入文件
                int lineNo = throwStmtCopy.getRange().get().begin.line;
                res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
            } catch (UnsolvedSymbolException e) {

            }
        }
        return res;
    }
}
