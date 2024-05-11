package com.example.mutantGen.mutators.concurrency;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RCS extends MutantGen {
    private static final Logger logger = LogManager.getLogger(RCS.class);
    public static final MutatorType mutator = MutatorType.RCS;

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<SynchronizedStmt> synchronizedStmts = cu.findAll(SynchronizedStmt.class);
        int mutantNo = 0;
        for (int i = 0; i < synchronizedStmts.size(); i++) {
            SynchronizedStmt synchronizedStmt = synchronizedStmts.get(i);
            // 在克隆cu对象上删除synchronized块
            CompilationUnit cuCopy = generateCuCopy(originalFilePath);
            cuCopy.findAll(SynchronizedStmt.class).get(i).replace(synchronizedStmt.getBody());
            // 写入变异体文件
            int lineNo = synchronizedStmt.getRange().get().begin.line;
            res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
        }
        return res;
    }
}
