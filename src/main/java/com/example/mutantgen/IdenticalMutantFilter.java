package com.example.mutantgen;

import com.example.mutator.Mutant;
import com.example.utils.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdenticalMutantFilter {

    private final Logger logger = LogManager.getLogger(IdenticalMutantFilter.class);


    List<Mutant> filter(List<Mutant> mutants) {
        Set<String> fileToDelete = new HashSet<>();
        for (int i = 0; i < mutants.size(); i++) {
            String path1 = mutants.get(i).getMutatedPath();
            for (int j = i + 1; j < mutants.size(); j++) {
                String path2 = mutants.get(j).getMutatedPath();
                if(path1.equals(path2)) continue;
                if (FileUtil.isFileIdentical(path1, path2)) {
                    logger.info("Identical mutants FOUND: " + path1 + " and " + path2);
                    if (fileToDelete.contains(path1)) {
                        continue;
                    }
                    fileToDelete.add(path2);
                }
            }
        }
        // 删除对应的mutant和文件
        List<Mutant> newMutants = new ArrayList<>();
        for (Mutant mutant : mutants) {
            if (fileToDelete.contains(mutant.getMutatedPath())) {
                try {
                    logger.info("Deleting identical mutant: " + mutant.getMutatedPath());
                    FileUtils.delete(new File(mutant.getMutatedPath()));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file: " + mutant.getMutatedPath());
                }
            } else {
                newMutants.add(mutant);
            }
        }
        return newMutants;
    }
}
