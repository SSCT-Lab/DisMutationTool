package com.example.mutantGen.mutators.concurrency;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UCE extends MutantGen {

    public static final MutatorType mutator = MutatorType.UCE;
    private static final Logger logger = LogManager.getLogger(UCE.class);

    private static final ArrayList<String> exceptions = new ArrayList<>();

    static {
        exceptions.add("java.lang.IllegalThreadStateException");
        exceptions.add("java.lang.IllegalMonitorStateException");
        exceptions.add("java.lang.InterruptedException");
        exceptions.add("java.util.concurrent.TimeoutException");
        exceptions.add("java.util.concurrent.BrokenBarrierException");
        exceptions.add("java.util.concurrent.CancellationException");
        exceptions.add("java.util.concurrent.ExecutionException");
        exceptions.add("java.util.concurrent.RejectedExecutionException");
        exceptions.add("java.util.concurrent.ConcurrentModificationException");
    }

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        // 遍历所有的throw
        List<ThrowStmt> throwStmts = cu.findAll(ThrowStmt.class);
        int mutantNo = 0;
        for (int i = 0; i < throwStmts.size(); i++) {
            ThrowStmt throwStmt = throwStmts.get(i);
            if (throwStmt.getExpression().isObjectCreationExpr()) {
                try {
                    ResolvedType resolvedType = throwStmt.getExpression().asObjectCreationExpr().getType().resolve();
                    if (resolvedType.isReferenceType()) {
                        Optional<ResolvedReferenceTypeDeclaration> resolvedReferenceTypeDeclaration = resolvedType.asReferenceType().getTypeDeclaration();
                        String packageName = resolvedReferenceTypeDeclaration.get().getPackageName();
                        String className = resolvedReferenceTypeDeclaration.get().getName();
                        String qualifiedName = packageName + "." + className;
                        // System.out.println(qualifiedName);
                        if (exceptions.contains(qualifiedName)) { // 确定是要upcast的异常
                            logger.info("Found concurrency exception: " + qualifiedName);
                            // 在拷贝cu上修改异常类型
                            CompilationUnit cuCopy = StaticJavaParser.parse(new File(originalFilePath));
                            LexicalPreservingPrinter.setup(cuCopy); // 尽可能保留原始格式
                            ThrowStmt throwStmtInCopy = cuCopy.findAll(ThrowStmt.class).get(i);

                            // 进行变异：
                            // 1. 修改import
                            // 检查是否已经导入了IOException
                            Optional<ImportDeclaration> ioExceptionImport = cuCopy.getImports().stream()
                                    .filter(importDeclaration -> importDeclaration.getName().asString().equals("java.lang.Exception"))
                                    .findFirst();

                            // 如果没有找到导入声明，则添加一个新的导入
                            if (!ioExceptionImport.isPresent()) {
                                ImportDeclaration importIOException = new ImportDeclaration("java.lang.Exception", false, false);
                                cuCopy.addImport(importIOException);
                            }

                            // 2.修改方法签名，确保方法能够throw IOException
                            Optional<MethodDeclaration> method = throwStmtInCopy.findAncestor(MethodDeclaration.class);
                            if (method.isPresent()) {
                                MethodDeclaration methodDecl = method.get();
                                // 检查方法签名是否已经声明抛出任何异常
                                if (methodDecl.getThrownExceptions().isEmpty()) {
                                    // 如果没有声明抛出任何异常，则添加throw IOException
                                    methodDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType("Exception"));
                                } else {
                                    // 如果已经声明抛出异常，则额外添加IOException
                                    boolean hasIOE = false;
                                    for (ReferenceType thrownException : methodDecl.getThrownExceptions()) {
                                        if (thrownException.toString().equals("Exception")) {
                                            // 如果已经声明抛出IOException，则不再添加
                                            // System.out.println("Already add IOE in method signature");
                                            hasIOE = true;
                                            break;
                                        }
                                    }
                                    if(!hasIOE){
                                        methodDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType("Exception"));
                                    }
//                                    // 无论是否已经声明抛出IOException，都添加IOException
//                                    methodDecl.getThrownExceptions()
//                                    methodDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType("IOException"));
                                }
                            } else { // 如果MethodDeclaration是空的，则说明throwStmt在构造函数中
                                Optional<ConstructorDeclaration> constructorDeclaration = throwStmtInCopy.findAncestor(ConstructorDeclaration.class);
                                if (constructorDeclaration.isPresent()) {
                                    ConstructorDeclaration constructorDecl = constructorDeclaration.get();
                                    // 检查构造函数签名是否已经声明抛出任何异常
                                    if (constructorDecl.getThrownExceptions().isEmpty()) {
                                        // 如果没有声明抛出任何异常，则添加throw IOException
                                        constructorDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType("Exception"));
                                    } else {
                                        // 如果已经声明抛出异常，则额外添加IOException
                                        boolean hasIOE = false;
                                        for (ReferenceType thrownException : constructorDecl.getThrownExceptions()) {
                                            if (thrownException.toString().equals("IOException")) {
                                                // 如果已经声明抛出IOException，则不再添加
                                                // System.out.println("Already add IOE in constructor signature");
                                                hasIOE = true;
                                                break;
                                            }
                                        }
                                        if(!hasIOE){
                                            constructorDecl.addThrownException(new com.github.javaparser.ast.type.ClassOrInterfaceType("Exception"));
                                        }
                                    }
                                } else {
                                    logger.error("failed to find methodDeclaration or constructorDeclaration in throwStmt!");
                                }
                            }


                            // 3. 修改异常类型
                            throwStmtInCopy.getExpression().asObjectCreationExpr().setType("Exception");

                            // 写入文件
                            mutantNo++;
                            String mutantName = FileUtil.getFileName(originalFilePath) + "_" + mutator + "_" + mutantNo + ".java";
                            String mutantPath = new File("./mutants/").getAbsolutePath() + "/" + mutantName;
                            logger.info("Generating mutant: " + mutantName);
                            FileUtil.writeToFile(LexicalPreservingPrinter.print(cuCopy), mutantPath);
                            // 生成变异体对象
                            Mutant mutant = new Mutant(throwStmt.getRange().get().begin.line, mutator, originalFilePath, mutantPath);
                            res.add(mutant);
                        }
                    }
                } catch (UnsolvedSymbolException e) { // 防止引用到项目的其他文件导致解析失败
                    // logger.info("UnsolvedSymbolException in methodCallExpr - " + throwStmt.getExpression().asObjectCreationExpr().getType());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        return res;
    }
}
