package com.example.testRunner;

import com.example.MutantManager;
import com.example.mutator.Mutant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

/*
    临时，用于随机选择当前的一定百分比的变异体进行测试
 */
public class RandomRunner {
    private static final Logger logger = LogManager.getLogger(RandomRunner.class);

    private MutantManager mutantManager;
    private double mutantPercentage = 0.1; // 选择变异体的百分比

    public RandomRunner(MutantManager mutantManager, double mutantPercentage) {
        this.mutantManager = mutantManager;
        this.mutantPercentage = mutantPercentage;
    }


    /**
     * 通过maven-invoker来运行测试，并且将控制台结果重定向到文件
     */
    public void run() {
        List<Mutant> mutantLs = mutantManager.getMutantLs();
//        // 删除mutantLs中，Mutant的originalPath重复的元素，只保留第一个
//        for (int i = 0; i < mutantLs.size(); i++) {
//            for (int j = i + 1; j < mutantLs.size(); j++) {
//                if (mutantLs.get(i).getOriginalPath().equals(mutantLs.get(j).getOriginalPath())) {
//                    mutantLs.remove(j);
//                    j--;
//                }
//            }
//        }

        // 选取mutantManager中的一定百分比的变异体进行测试
        int mutantNum = mutantManager.getMutantLs().size();
        int testNum = (int) (mutantNum * mutantPercentage);
        mutantLs.sort(Comparator.comparing(Mutant::getMutatedPath));
        List<Mutant> selectedMutants = mutantLs.subList(0, testNum);
        // 打印选择的变异体
        logger.info("Selected mutants: ");
        for (Mutant mutant : selectedMutants) {
            logger.info(mutant.getMutatedPath());
        }
        // 运行测试
        for (Mutant mutant: selectedMutants){
            MutantRunner mutantRunner = new MutantRunner(mutant);
            mutantRunner.run();
        }
    }
}
