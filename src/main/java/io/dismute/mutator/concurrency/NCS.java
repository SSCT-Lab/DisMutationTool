package io.dismute.mutator.concurrency;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutator.MutatorBase;
import io.dismute.mutantgen.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NCS extends MutatorBase {
    private static final Logger logger = LogManager.getLogger(NCS.class);
    public static final MutatorType mutator = MutatorType.NCS;

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
        int mutantNo = 0;
        for (int i = 0; i < methodDeclarations.size(); i++) {
            MethodDeclaration methodDeclaration = methodDeclarations.get(i);
            List<SynchronizedStmt> synchronizedStmts = methodDeclaration.findAll(SynchronizedStmt.class);
            for (int j = 0; j < synchronizedStmts.size(); j++) {
                // 在cuCopy上进行修改
                CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                MethodDeclaration methodDeclarationCopy = cuCopy.findAll(MethodDeclaration.class).get(i);
                SynchronizedStmt synchronizedStmtCopy = methodDeclarationCopy.findAll(SynchronizedStmt.class).get(j);
                BlockStmt block = synchronizedStmtCopy.getBody();

                NodeList<Statement> statements = block.getStatements();
                int totalStatements = block.getStatements().size();

                if (totalStatements > 2) {
                    int splitIndex = totalStatements / 3 * 2;
                    BlockStmt firstBlock = new BlockStmt();
                    BlockStmt secondBlock = new BlockStmt();
                    for (int k = 0; k < splitIndex; k++) {
                        firstBlock.addStatement(block.getStatement(k));
                    }
                    for (int k = splitIndex; k < totalStatements; k++) {
                        secondBlock.addStatement(block.getStatement(k));
                    }
                    BlockStmt newBlockStmt = new BlockStmt();
                    SynchronizedStmt shrunkSyn = new SynchronizedStmt(); // 收缩后的synchronized块
                    shrunkSyn.setExpression(synchronizedStmtCopy.getExpression());
                    shrunkSyn.setBody(firstBlock);
                    newBlockStmt.addStatement(shrunkSyn);
                    newBlockStmt.addStatement(secondBlock);
                    synchronizedStmtCopy.replace(newBlockStmt);

                    //写入变异体文件
                    int lineNo = synchronizedStmtCopy.getRange().get().begin.line;
                    res.add(generateMutantAndSaveToFile(++mutantNo, lineNo, mutator, originalFilePath, cuCopy));
                }

            }
        }


        return res;
    }
}
