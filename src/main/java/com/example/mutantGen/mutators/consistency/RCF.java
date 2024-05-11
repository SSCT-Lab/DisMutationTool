package com.example.mutantGen.mutators.consistency;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RCF extends MutantGen {

    private static final Logger logger = LogManager.getLogger(RCF.class);
    public static final MutatorType mutator = MutatorType.RCF;
    private static final List<String> importKeywords = new ArrayList<>();
    private static final List<String> fileObjKeywords = new ArrayList<>();

    static {
        importKeywords.add("org.apache.cassandra.config");
        importKeywords.add("org.apache.hadoop.conf");

        fileObjKeywords.add("conf");
        fileObjKeywords.add("data");
        fileObjKeywords.add("snap");
        fileObjKeywords.add("persist");
        fileObjKeywords.add("store");
    }

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);

        boolean matchAnyFileObj = false; // 是否匹配到任何名称的file对象
        // 验证当前.java文件的package是否包含importKeywords中的任何一个关键字
        if (cu.getPackageDeclaration().isPresent()) {
            for (String keyword : importKeywords) {
                if (cu.getPackageDeclaration().get().getNameAsString().contains(keyword)) {
                    matchAnyFileObj = true;
                    break;
                }
            }
        }

        // 验证当前.java文件的任何一条import语句是否包含importKeywords中的任何一个关键字
        for (ImportDeclaration importDeclaration : cu.findAll(ImportDeclaration.class)) {
            if (matchAnyFileObj) {
                break;
            }
            for (String keyword : importKeywords) {
                if (importDeclaration.getNameAsString().contains(keyword)) {
                    matchAnyFileObj = true;
                    break;
                }
            }
        }

        List<IfStmt> ifStmts = cu.findAll(IfStmt.class);
        int mutantNo = 0;
        // 遍历所有的if语句
        for (int i = 0; i < ifStmts.size(); i++) {
            IfStmt ifStmt = ifStmts.get(i);
            // 遍历if的condition中所有的方法调用
            for (int j = 0; j < ifStmt.getCondition().findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).size(); j++) {
                MethodCallExpr methodCallExpr = ifStmt.getCondition().findAll(MethodCallExpr.class).get(j);
                // 如果当前方法名称为exists
                if (methodCallExpr.getName().asString().equals("exists")) {
                    try {
                        // 解析方法调用方的类型
                        ResolvedMethodDeclaration resolvedMethodDeclaration = methodCallExpr.resolve();
                        String packageAndClassName = resolvedMethodDeclaration.getPackageName() + "." + resolvedMethodDeclaration.getClassName();
                        if (packageAndClassName.equals("java.io.File")) {
                            // 符合if(fileObj.exists())的条件
                            // 根据matchAnyFileObj, 判断是否需要将文件对象变量名fileObjKeywords进行匹配
                            String fileObjName = methodCallExpr.getScope().get().toString();
                            boolean genMutant = false;
                            if(!matchAnyFileObj){
                                genMutant = fileObjKeywords.stream().anyMatch(fileObjName::contains);
                            } else {
                                genMutant = true;
                            }
                            if(genMutant){
                                // 在cuCopy上生成变异体
                                CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                                IfStmt ifStmtCopy = cuCopy.findAll(IfStmt.class).get(i);
                                ifStmtCopy.replace(ifStmtCopy.getThenStmt());

                                mutantNo++;
                                String mutantName = FileUtil.getFileName(originalFilePath) + "_" + mutator + "_" + mutantNo + ".java";
                                String mutantPath = new File(Config.MUTANT_PATH).getAbsolutePath() + "/" + mutantName;
                                logger.info("Generating mutant: " + mutantName);
                                FileUtil.writeToFile(LexicalPreservingPrinter.print(cuCopy), mutantPath);
                                res.add(new Mutant(ifStmtCopy.getRange().get().begin.line, mutator, originalFilePath, mutantPath));
                            }
                        }
                    } catch (UnsolvedSymbolException e) {
                    }
                }
            }
        }


        return res;
    }
}
