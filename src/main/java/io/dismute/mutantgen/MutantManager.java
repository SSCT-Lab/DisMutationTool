package io.dismute.mutantgen;

import io.dismute.coverage.CoverageManager;
import io.dismute.mutator.MutatorBase;
import io.dismute.singleton.Project;
import io.dismute.singleton.PropertiesFile;
import io.dismute.utils.LogUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Getter
public class MutantManager {

    private static final Logger logger = LogManager.getLogger(MutantManager.class);

    private static volatile MutantManager instance;


    // update: 状态控制交给engine
//    private boolean isMutantGenerated = false;

    private List<Mutant> mutantLs;
    private Map<String, Map<MutatorType, List<Mutant>>> mutantMap;

    private MutantManager() {
        this.mutantLs = new ArrayList<>();
        this.mutantMap = new HashMap<>();
    }

    public static MutantManager getInstance() {
        if (instance == null) {
            synchronized (MutantManager.class) {
                if (instance == null) {
                    instance = new MutantManager();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        synchronized (MutantManager.class) {
            instance = null;
        }
    }


    // 生成变异体
    public List<Mutant> generateMutants() {
        logger.info(LogUtil.centerWithSeparator("Generating Mutants"));
        List<String> srcFileLs = Project.getInstance().getSrcFileLs();
        // update: 不去除同名文件
        for (String srcFile : srcFileLs) {
//            logger.info("Scanning {}" , srcFile);
//            if(srcFile.endsWith("InternalTopologyBuilder.java")){ //update: Project从配置文件读取
//                continue;
//            }
            for (MutatorType mutatorType : Project.getInstance().getMutatorTypes()) {
                MutatorBase mutator = MutatorFactory.getMutator(mutatorType);
                List<Mutant> mutants = mutator.execute(srcFile);
                if (mutants != null) {
                    mutantLs.addAll(mutants);
                    if (mutantMap.containsKey(srcFile)) {
                        mutantMap.get(srcFile).put(mutatorType, mutants);
                    } else {
                        Map<MutatorType, List<Mutant>> map = new HashMap<>();
                        map.put(mutatorType, mutants);
                        mutantMap.put(srcFile, map);
                    }
                }
            }
        }
        // 为所有的mutant添加coverage信息
        addCoverageInfoToMutants();

        // 打印mutant统计信息
        logger.info("Total mutants: {}", mutantLs.size());
//        for (String srcFile : mutantMap.keySet()) {
//            logger.info("File: {}", srcFile);
//            for (MutatorType mutator : mutantMap.get(srcFile).keySet()) {
//                logger.info("\t{} count: {}", mutator, mutantMap.get(srcFile).get(mutator).size());
//            }
//        }
        return mutantLs;
    }

    private void addCoverageInfoToMutants() {
        if(!Project.getInstance().isCoverage()){
            return;
        }
        logger.info(LogUtil.centerWithSeparator("Adding Coverage Info to Mutants"));
        CoverageManager coverageManager = CoverageManager.getInstance();
        for( Mutant mutant : mutantLs) {
            String srcFileName = mutant.getOriginalName().replace(".java", "");
            if (coverageManager.getCoverageInfo().containsKey(srcFileName)) {
                List<String> coveredTestCases = coverageManager.getCoverageInfo().get(srcFileName);
                mutant.setCoveredTestCases(coveredTestCases);
            } else {
                mutant.setCoveredTestCases(Collections.emptyList());
            }
        }
    }


    public void setMutants(List<Mutant> mutants) {
        this.mutantLs = mutants;
        // 暂时不更新map
    }
}
