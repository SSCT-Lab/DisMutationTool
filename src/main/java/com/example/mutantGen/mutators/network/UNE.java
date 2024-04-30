package com.example.mutantGen.mutators.network;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ThrowStmt;
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

/*
 * Upcast network exception
 * 将UnknownHostException, UnknownServiceException
 * ProtocolException, RemoteException, SaslException, SocketException, SSLException
 * SyncFailedException, JMXServerErrorException, JMXProviderException, HttpRetryException
 * 转为IOException
 */

public class UNE extends MutantGen {

    public static final MutatorType mutator = MutatorType.UNE;
    private static final Logger logger = LogManager.getLogger(UNE.class);


    private static final ArrayList<String> exceptions = new ArrayList<>();

    static {
        exceptions.add("java.net.UnknownHostException");
        exceptions.add("java.net.UnknownServiceException");
        exceptions.add("java.net.ProtocolException");
        exceptions.add("java.rmi.RemoteException");
        exceptions.add("javax.security.sasl.SaslException");
        exceptions.add("java.net.SocketException");
        exceptions.add("javax.net.ssl.SSLException");
        exceptions.add("java.io.SyncFailedException");
        exceptions.add("javax.management.remote.JMXServerErrorException");
        exceptions.add("javax.management.JMXProviderException");
        exceptions.add("java.net.HttpRetryException");
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
                            logger.info("Found network exception: " + qualifiedName);
                            // 在拷贝cu上修改异常类型
                            CompilationUnit cuCopy = StaticJavaParser.parse(new File(originalFilePath));
                            LexicalPreservingPrinter.setup(cuCopy); // 尽可能保留原始格式
                            cuCopy.findAll(ThrowStmt.class).get(i).getExpression().asObjectCreationExpr().setType("IOException");
//                            Optional<ObjectCreationExpr> oceOptional = throwStmt.getExpression().toObjectCreationExpr();
//                            if (oceOptional.isPresent()) {
//                                ObjectCreationExpr oce = oceOptional.get();
//                                oce.setType("IOException");
//                            }
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
