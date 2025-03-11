package io.dismute.mutator.concurrency;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutator.MutatorBase;
import io.dismute.mutantgen.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SCS extends MutatorBase {
    private static final Logger logger = LogManager.getLogger(SCS.class);
    public static final MutatorType mutator = MutatorType.SCS;

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<SynchronizedStmt> synchronizedStmts = cu.findAll(SynchronizedStmt.class);

        int mutantNo = 0;
        for (int i = 0; i < synchronizedStmts.size(); i++) {
            // 在cuCopy上进行修改
            CompilationUnit cuCopy = generateCuCopy(originalFilePath);
            SynchronizedStmt synchronizedStmtCopy = cuCopy.findAll(SynchronizedStmt.class).get(i);
            BlockStmt block = synchronizedStmtCopy.getBody();
            int totalStatements = block.getStatements().size();
            if (totalStatements > 1) {
                int splitIndex = totalStatements / 2;
                BlockStmt firstBlock = new BlockStmt();
                BlockStmt secondBlock = new BlockStmt();
                for (int k = 0; k < splitIndex; k++) {
                    firstBlock.addStatement(block.getStatement(k));
                }
                for (int k = splitIndex; k < totalStatements; k++) {
                    secondBlock.addStatement(block.getStatement(k));
                }
                SynchronizedStmt syn1 = new SynchronizedStmt(); // 分裂后的第一个syn
                syn1.setExpression(synchronizedStmtCopy.getExpression());
                syn1.setBody(firstBlock);
                SynchronizedStmt syn2 = new SynchronizedStmt(); // 分裂后的第二个syn
                syn2.setExpression(synchronizedStmtCopy.getExpression());
                syn2.setBody(secondBlock);
                BlockStmt splitBlock = new BlockStmt().addStatement(syn1).addStatement(syn2);
                synchronizedStmtCopy.replace(splitBlock);

                //写入变异体文件
                int lineNo = synchronizedStmtCopy.getBegin().get().line;
                res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
            }
        }


        return res;
    }
}
