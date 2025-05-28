package io.dismute.mutantfilter;

import io.dismute.mutantgen.Mutant;
import io.dismute.utils.FileUtil;
import io.dismute.utils.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.tools.*;
import java.io.File;
import java.util.*;

/**
 * 两两比较变异体，如果两个变异体的文件内容完全相同，则认为是相同的变异体
 * */
public class IdenticalMutantFilter implements MutantFilter {

    private static final Logger logger = LogManager.getLogger(IdenticalMutantFilter.class);

    @Override
    public List<Mutant> filter(List<Mutant> mutants) {
        logger.info(LogUtil.centerWithSeparator("Filtering Identical Mutants"));

        List<Mutant> filteredMutants = new ArrayList<>();
        for(int i = 0; i<mutants.size();i++) {
            Mutant mutant1 = mutants.get(i);
            boolean isIdentical = false;
            for(int j = i+1; j<mutants.size();j++) {
                Mutant mutant2 = mutants.get(j);
                if(mutant1.getMutatedPath().equals(mutant2.getMutatedPath())) {
                    continue;
                }
                if(FileUtil.isFileIdentical(mutant1.getMutatedPath(), mutant2.getMutatedPath())) {
                    isIdentical = true;
                    logger.info("Identical mutants FOUND: {} and {}", mutant1.getMutatedName(), mutant2.getMutatedName());
                    logger.info("\t path1: {}", mutant1.getMutatedPath());
                    logger.info("\t path2: {}", mutant2.getMutatedPath());
                    break;
                }
            }
            if(!isIdentical) {
                filteredMutants.add(mutant1);
            }
        }
        logger.info("Identical filter completed. Mutant size BEFORE: {}, AFTER: {}", mutants.size(), filteredMutants.size());
        return filteredMutants;
    }
}
