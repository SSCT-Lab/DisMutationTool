package com.example.mutantgen;

import com.example.Project;
import com.example.mutantFilter.BytecodeFilter;
import com.example.mutator.Mutant;
import com.example.mutator.MutatorFactory;
import com.example.mutator.MutatorType;
import com.example.utils.Constants;
import com.example.utils.FileUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Getter
public class MutantGenerator {
    private static final Logger logger = LogManager.getLogger(MutantGenerator.class);

    private final Project project;
    private final Set<MutatorType> mutatorSet;
    private List<Mutant> mutants = new ArrayList<>();
    private final Map<String, Map<MutatorType, List<Mutant>>> mutantMap = new HashMap<>(); // key: originalPath, value: [key: mutatorType, value: mutants]


    public MutantGenerator(Project project) {
        this.project = project;
        this.mutatorSet = project.getMutators();
    }

    public List<Mutant> generateMutants(){
        return generateMutants(false);
    }

    public void generateMutantsWithoutFilterEq() {
        generateMutants(true);
    }


    private List<Mutant> generateMutants(boolean skipBytecode) {
        logger.info("\n\nPHASE: Generate initial mutants for project: " + project.getBasePath() + " ...\n\n");

        // 生成所有变异体
        List<String> srcFileLs = project.getSrcFileLs();
        // TODO 文件命名空间，暂时使文件名不重复
        // 如果两个srcFile文件名相同，去掉后者
        List<String> rmDupFileLs = new ArrayList<>();
        for (String srcFile : srcFileLs) {
            String javaFileName = FileUtil.getFileName(srcFile);
            boolean flag = true;
            for(String rmDupFile : rmDupFileLs){
                String rmDupJavaFileName = FileUtil.getFileName(rmDupFile);
                if(rmDupJavaFileName.equals(javaFileName)){
                    flag = false;
                }
            }
            if(flag){
                rmDupFileLs.add(srcFile);
            }
        }

        srcFileLs = rmDupFileLs;

        for (String srcFile : srcFileLs) {
            boolean skip = false;
            for(String excludeFile: Constants.excludeSrcFiles){
                if (srcFile.contains(excludeFile)) {
                    skip = true;
                    break;
                }
            }
            if(skip){
                logger.info("Skipping file: " + srcFile);
                continue;
            }
            for (MutatorType mutator : mutatorSet) {
                mutants.addAll(MutatorFactory.getMutator(mutator).execute(srcFile));
            }
        }


        // 删除内容相同的变异体
        logger.info("\n\nPHASE: Removing identical mutants...\n\n");
        IdenticalMutantFilter identicalMutantFilter = new IdenticalMutantFilter();
        mutants = identicalMutantFilter.filter(mutants);


        // 删除等价变异体
        if(!skipBytecode){
            logger.info("\n\nPHASE: Removing equivalent mutants...\n\n");
            BytecodeFilter bytecodeFilter = new BytecodeFilter(project, mutants);
            mutants = bytecodeFilter.filter();
        }

        // 统计变异体信息
        for (Mutant mutant : mutants) {
            if (!mutantMap.containsKey(mutant.getOriginalPath())) {
                mutantMap.put(mutant.getOriginalPath(), new HashMap<>());
            }
            if (!mutantMap.get(mutant.getOriginalPath()).containsKey(mutant.getMutatorType())) {
                mutantMap.get(mutant.getOriginalPath()).put(mutant.getMutatorType(), new ArrayList<>());
            }
            mutantMap.get(mutant.getOriginalPath()).get(mutant.getMutatorType()).add(mutant);
        }

        // 打印mutant统计信息
        logger.info("Generate completed");
        logger.info("Total mutants: " + mutants.size());
        for (String srcFile : mutantMap.keySet()) {
            logger.info("File: " + srcFile);
            for (MutatorType mutator : mutantMap.get(srcFile).keySet()) {
                logger.info("\t" + mutator + " count: " + mutantMap.get(srcFile).get(mutator).size());
            }
        }

        mutants.sort(Comparator.comparing(Mutant::getMutatedPath));

        return mutants;
    }


}
