package io.dismute.mutator.deprecated;

import io.dismute.mutantgen.Mutant;
import io.dismute.mutator.MutatorBase;
import io.dismute.mutantgen.MutatorType;
import io.dismute.utils.FileUtil;
import io.dismute.singleton.Project;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Remove unlock
 */
public class RUL extends MutatorBase {

    public static final MutatorType mutator = MutatorType.RUL;
    private static final Logger logger = LogManager.getLogger(RUL.class);
    private static final Project project = Project.getInstance();


    @Override
    public List<Mutant> execute(String originalFilePath) {
        List<Mutant> res = new ArrayList<>();
        parse(originalFilePath);
        List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);
        int mutantNo = 0;
        for (int i = 0; i < methodCallExprs.size(); i++) {
            MethodCallExpr methodCallExpr = methodCallExprs.get(i);
            if (methodCallExpr.getName().asString().equals("unlock")) {
                // 在克隆cu对象上删除unlock方法调用
                CompilationUnit cuCopy = generateCuCopy(originalFilePath);
                cuCopy.findAll(MethodCallExpr.class).get(i).removeForced();
                // 写入变异体文件
                mutantNo++;
                String mutantName = FileUtil.getNameWithoutExtension(originalFilePath) + "_" + mutator + "_" + mutantNo + ".java";
                String mutantPath = new File(project.getMutantsPath()).getAbsolutePath()+ "/" + mutantName;
                logger.info("Generating mutant: " + mutantName);
                FileUtil.writeToFile(LexicalPreservingPrinter.print(cuCopy), mutantPath);
                res.add(new Mutant(methodCallExpr.getRange().get().begin.line, mutator, originalFilePath, mutantPath));
            }
        }
        return res;
    }
}
