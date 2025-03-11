package io.dismute.mutator.concurrency;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutator.MutatorBase;
import io.dismute.mutantgen.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BCS extends MutatorBase {
    private static final Logger logger = LogManager.getLogger(BCS.class);
    public static final MutatorType mutator = MutatorType.BCS;

    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<SynchronizedStmt> synchronizedStmts = cu.findAll(SynchronizedStmt.class);
        int mutantNo = 0;
        for (int i = 0; i < synchronizedStmts.size(); i++) {
            SynchronizedStmt synchronizedStmt = synchronizedStmts.get(i);
            // 获取synchronized代码块的父节点
            Optional<Node> parentNode = synchronizedStmt.getParentNode();
            if (parentNode.isPresent() && parentNode.get() instanceof BlockStmt) {
                BlockStmt parentBlock = (BlockStmt) parentNode.get();
                int index = parentBlock.getStatements().indexOf(synchronizedStmt);

                // 检查synchronized代码块之后是否有足够的语句
                if (index != -1 && parentBlock.getStatements().size() > index + 1) {
                    // 符合条件，在cuCopy上进行修改
                    CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                    SynchronizedStmt synchronizedStmtCopy = cuCopy.findAll(SynchronizedStmt.class).get(i);
                    Optional<Node> parentNodeCopy = synchronizedStmtCopy.getParentNode();
                    BlockStmt parentBlockCopy = (BlockStmt) parentNodeCopy.get();

                    int totalStatementsAfter = parentBlockCopy.getStatements().size() - (index + 1);
                    int thirdOfStatements = totalStatementsAfter / 3;
                    if(thirdOfStatements == 0) { // 如果statement是1或者2,取1个
                        thirdOfStatements = 1;
                    }

                    // 将前三分之一的语句移动到synchronized代码块中
                    for (int j = 0; j < thirdOfStatements; j++) {
                        synchronizedStmtCopy.getBody().addStatement(parentBlockCopy.getStatements().get(index + 1));
                        parentBlockCopy.remove(parentBlockCopy.getStatements().get(index + 1));
                    }
                    // 写入变异体文件
                    int lineNo = synchronizedStmtCopy.getRange().get().begin.line;
                    Mutant mutant = generateMutantAndSaveToFile(++mutantNo, lineNo , mutator, originalFilePath, cuCopy);
                    res.add(mutant);
                }
            }
        }

        return res;
    }
}
