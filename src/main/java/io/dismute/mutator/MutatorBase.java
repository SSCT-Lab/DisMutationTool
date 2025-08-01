package io.dismute.mutator;

import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutatorType;
import io.dismute.singleton.Project;
import io.dismute.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class MutatorBase {

    private static final Logger logger = LogManager.getLogger(MutatorBase.class);
    protected CompilationUnit cu;

    public MutatorBase() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
    }

    /**
     * 解析Java文件
     * @param originalFilePath
     * @return
     */
    protected CompilationUnit parse(String originalFilePath) {
        try {
            FileInputStream in = new FileInputStream(originalFilePath);
            // logger.info("Reading original file: " + originalFilePath);
            cu = StaticJavaParser.parse(in);
            LexicalPreservingPrinter.setup(cu); // 尽可能保留原始格式
            return this.cu;
        } catch (IOException e){
            e.printStackTrace();
            logger.error("Error parsing file " + originalFilePath);
            return this.cu;
        }
    }

    protected CompilationUnit generateCuCopy(String originalFilePath){
        try {
            FileInputStream in = new FileInputStream(originalFilePath);
            CompilationUnit cuCopy = StaticJavaParser.parse(in);
            LexicalPreservingPrinter.setup(cuCopy);
            return cuCopy;
        } catch (IOException e){
            e.printStackTrace();
            logger.error("Error parsing file " + originalFilePath);
            return null;
        }
    }

    // 根据传入信息，生成变异体并保存到文件
    protected Mutant generateMutantAndSaveToFile(int mutantNo, int lineNo, MutatorType mutatorType, String originalPath, CompilationUnit cu){
        String mutantName = FileUtil.getNameWithoutExtension(originalPath) + "_" + mutatorType + "_" + mutantNo + ".java";
        String mutantPath = new File(Project.getInstance().getMutantsPath()).getAbsolutePath() + File.separator + mutantName;
        logger.info("Generating {} mutant: {} path {}", mutatorType, mutantName, mutantPath);
        FileUtil.writeToFile(LexicalPreservingPrinter.print(cu), mutantPath);
        return new Mutant(lineNo, mutatorType, originalPath, mutantPath);
    }

    public abstract List<Mutant> execute(String originalFilePath);


}
