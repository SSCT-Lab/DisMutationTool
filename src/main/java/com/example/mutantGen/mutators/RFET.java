package com.example.mutantGen.mutators;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
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
 * Remove File Existance Throws
 * if(file.exists()) { throws xxx } <- delete throws
 * */
public class RFET extends MutantGen {

    public static final MutatorType mutator = MutatorType.RFET;

    private static final Logger logger = LogManager.getLogger(RFET.class);


    @Override
    public List<Mutant> execute(String originalFilePath) {
        try {
            List<Mutant> res = new ArrayList<>();

            parse(originalFilePath);

            List<IfStmt> ifStmts = cu.findAll(IfStmt.class);
            int mutantNo = 0;
            // 遍历所有的if语句
            for (int i = 0; i < ifStmts.size(); i++) {
                IfStmt ifStmt = ifStmts.get(i);
                List<MethodCallExpr> methodCallExprs = ifStmt.getCondition().findAll(MethodCallExpr.class);
                // 遍历if的condition中所有的方法调用
                for (int j = 0; j < methodCallExprs.size(); j++) {
                    MethodCallExpr methodCallExpr = methodCallExprs.get(j);
                    if (methodCallExpr.getName().asString().equals("exists")) {
                        try {
                            ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                            String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                            if (packageAndClassName.equals("java.io.File")) {
                                logger.info("Found exists expr in if statement: " + ifStmt.getCondition());
                                List<ThrowStmt> throwStmts = ifStmt.findAll(ThrowStmt.class);

                                for (int k = 0; k < throwStmts.size(); k++) {
                                    ThrowStmt throwStmt = throwStmts.get(k);

                                    // 在拷贝cu上删除throw
                                    CompilationUnit cuCopy = StaticJavaParser.parse(new File(originalFilePath));
                                    LexicalPreservingPrinter.setup(cuCopy); // 尽可能保留原始格
                                    cuCopy.findAll(IfStmt.class).get(i).findAll(ThrowStmt.class).get(k).remove();

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
                            logger.info("UnsolvedSymbolException in methodCallExpr - " + methodCallExpr);
                        }
                    }
                }
            }

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
