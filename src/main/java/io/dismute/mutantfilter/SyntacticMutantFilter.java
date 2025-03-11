package io.dismute.mutantfilter;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.dismute.mutantgen.Mutant;
import io.dismute.utils.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;


/**
 * 通过第三方类库进行语法静态检查，过滤掉不符合语法规范的变异体
 */
public class SyntacticMutantFilter implements MutantFilter {

    private static final Logger logger = LogManager.getLogger(SyntacticMutantFilter.class);

    @Override
    public List<Mutant> filter(List<Mutant> mutants) {
        logger.info(LogUtil.centerWithSeparator("Filtering Syntax Mutants"));
        List<Mutant> filteredMutants = new ArrayList<>();
        for (Mutant mutant : mutants) {
            File sourceFile = new File(mutant.getMutatedPath());
            try {
                // 使用 JavaParser 解析文件 如果解析成功，表示语法正确
                CompilationUnit cu = StaticJavaParser.parse(sourceFile);
            } catch (ParseProblemException e) {
                // 捕获并输出语法错误
                logger.error("Syntax error in file: {}", sourceFile.getName());
                e.getProblems().forEach(problem -> {
                    logger.error("Problem: {}", problem.getMessage());
                    logger.error("Location: {}", problem.getLocation().get().toString());
                });
                continue;
            } catch (Exception e) {
                logger.error("Error while parsing file: {} {}", sourceFile.getName(), e.getMessage());
            }
            // 语法正确的变异体
            filteredMutants.add(mutant);
        }

        return filteredMutants;  // 可进一步实现过滤逻辑
    }
}