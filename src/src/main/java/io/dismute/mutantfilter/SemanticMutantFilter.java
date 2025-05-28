package io.dismute.mutantfilter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.utils.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 前置条件：项目已经编译通过，classpath.txt已生成，存放在Project.classpathTxtPath
 *
 */

public class SemanticMutantFilter implements MutantFilter{

    private static final Logger logger = LogManager.getLogger(SemanticMutantFilter.class);

    @Override
    public List<Mutant> filter(List<Mutant> mutants) {
        logger.info(LogUtil.centerWithSeparator("Filtering Semantic Mutants"));
        Project project = Project.getInstance();
        List<Mutant> filtered = new ArrayList<>();
        for(Mutant mutant : mutants) {
            boolean isCompilationSuccess = project.getBuildToolAdapter().incrementalCompilation(mutant);
            if(!isCompilationSuccess) {
                logger.info("Mutant {} failed to compile", mutant.getMutatedName());
            } else {
                filtered.add(mutant);
            }
        }
        logger.info("Semantic filter completed. Mutant size BEFORE: {}, AFTER: {}", mutants.size(), filtered.size());
        return filtered;
    }


}
