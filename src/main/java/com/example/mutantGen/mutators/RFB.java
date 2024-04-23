package com.example.mutantGen.mutators;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RFB extends MutantGen {

    public static MutatorType mutator = MutatorType.RFB;

    private static final Logger logger = LogManager.getLogger(RFB.class);

    /**
     * 删除掉finally块中的内容，如果finally块为空，则不做任何操作
     * TODO：块中仅有logger或输出等
     * remove finally block's contents
     *
     * @param originalFilePath
     * @return
     */
    public List<Mutant> execute(String originalFilePath) {
        try {
            List<Mutant> res = new ArrayList<>();
            // 解析现有的Java文件
            FileInputStream in = new FileInputStream(originalFilePath);
            CompilationUnit cu = StaticJavaParser.parse(in);
            List<TryStmt> tryStmts = cu.findAll(TryStmt.class);

            int mutantNo = 0;
            for (int i = 0; i < tryStmts.size(); i++) {
                TryStmt tryStmt = tryStmts.get(i);
                if (tryStmt.getFinallyBlock().isPresent()) {
                    mutantNo += 1;
                    Mutant mutant = genMutant(originalFilePath, i, mutantNo);
                    res.add(mutant);
                }
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error generating RFB mutant for file" + originalFilePath);
            return null;
        }
    }

    /**
     * 生成一个RFB变异体
     *
     * @param originalFilePath 原文件路径
     * @param i                tryStmt的索引
     * @param mutantNo         变异体编号
     * @throws FileNotFoundException
     */
    private Mutant genMutant(String originalFilePath, int i, int mutantNo) throws FileNotFoundException {
        String mutantName = FileUtil.getFileName(originalFilePath) + "_" + mutator + "_" + mutantNo + ".java";
        String mutantPath = new File("./mutants/").getAbsolutePath() + "/" + mutantName;
        logger.info("Generating mutant: " + mutantName);

        FileInputStream in = new FileInputStream(originalFilePath);
        CompilationUnit cu = StaticJavaParser.parse(in);
        LexicalPreservingPrinter.setup(cu); // 尽可能保留原始格式
        TryStmt tryStmts = cu.findAll(TryStmt.class).get(i);
        tryStmts.getFinallyBlock().get().getStatements().clear();
        int lineNo = tryStmts.getFinallyBlock().get().getRange().get().begin.line;

        // 回写到文件
        FileUtil.writeToFile(LexicalPreservingPrinter.print(cu), mutantPath);
        return new Mutant(lineNo, mutator, originalFilePath, mutantPath);
    }
}
