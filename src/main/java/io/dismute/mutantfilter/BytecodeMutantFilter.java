package io.dismute.mutantfilter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前置条件1：项目已经被编译过，所有符合Project.buildOutputPattern的字节码都已经生成，并且存储在Project.originalBytecodePath目录中 【engine负责的任务】
 * 前置条件2：SemanticMutantFilter已经将所有变异体增量编译，如果增量编译成功，将字节码存储在Project.mutantBytecodePath/<变异体名称>目录中
 * 通过字节码比较，过滤掉语义等价的变异体
 * 既要两两比较，又要和未变异的源码的编译结果进行比较
 * */
public class BytecodeMutantFilter implements MutantFilter {

    private static final Logger logger = LogManager.getLogger(BytecodeMutantFilter.class);

    @Override
    public List<Mutant> filter(List<Mutant> mutants) {
        // 1. 读取Project.originalBytecodePath目录下的所有字节码文件
        // 2. 读取Project.mutantBytecodePath目录下的所有字节码文件
        // 3. 两两比较，过滤掉语义等价的变异体
        // 4. 将过滤后的变异体存储在Project.filteredMutantBytecodePath目录中
        return Collections.emptyList();
    }

    // 比较两个Mutant的字节码是否等价
    private static boolean isEquivalentMutant(Mutant mutant1, Mutant mutant2) {
        Project project = Project.getInstance();
        String mutant1ClassFileBasePath = project.getMutantBytecodePath() + File.separator + mutant1.getMutatedName();
        List<String> mutant1ClassFiles = FileUtil.getFilesBasedOnPattern(mutant1ClassFileBasePath, ".*\\.class");
        String mutant2ClassFileBasePath = project.getMutantBytecodePath() + File.separator + mutant2.getMutatedName();
        List<String> mutant2ClassFiles = FileUtil.getFilesBasedOnPattern(mutant2ClassFileBasePath, ".*\\.class");
        if(mutant1ClassFiles.size() != mutant2ClassFiles.size()) {
            return false;
        }
        for(int i = 0; i < mutant1ClassFiles.size(); i++) {
            String mutant1ClassFile = mutant1ClassFiles.get(i);
            String mutant2ClassFile = mutant2ClassFiles.get(i);
            String fileName1 = FileUtil.getNameWithoutExtension(mutant1ClassFile);
            String fileName2 = FileUtil.getNameWithoutExtension(mutant2ClassFile);
            if(!fileName1.equals(fileName2)) { // 不同名的class文件，不可能是等价的
                continue;
            }
            boolean isContentIdentical = FileUtil.isFileIdentical(mutant1ClassFile, mutant2ClassFile);
            if(!isContentIdentical) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEquivalentOriginalBytecode(Mutant mutant, List<String> allOriginalBytecodeFiles) {
        List<String> mutantClassFiles = FileUtil.getFilesBasedOnPattern(mutant.getMutatedName(), ".*\\.class");
        List<String> mutantClassFileNames = mutantClassFiles.stream().map(FileUtil::getNameWithoutExtension).collect(Collectors.toList());
        List<String> originalClassFileMatches = allOriginalBytecodeFiles.
                stream().
                filter(file -> mutantClassFileNames.contains(FileUtil.getNameWithoutExtension(file)))
                .collect(Collectors.toList());

        for(String mutantClassFileName : mutantClassFileNames) {
            logger.info("Mutant class file: {}", mutantClassFileName);
        }
        logger.info("---------------------------------------------");
        for(String originalClassFile : originalClassFileMatches) {
            logger.info("Original class file: {}", originalClassFile);
        }
        if (mutantClassFiles.size() > originalClassFileMatches.size()) {
            logger.error("Mutant {} has more class files than original bytecode", mutant.getMutatedName());
            return false;
        }
        // 两两比较
        boolean res = true;
        for(String mutantClassFile : mutantClassFiles) {
            boolean curClassFileIdentical = false;
            String mutantClassFileName = FileUtil.getNameWithoutExtension(mutantClassFile);
            // 找出mutantClassFileName对应的originalClassFile（可能有多个）
            List<String> originalClassFileMatchesWithOneMutant = originalClassFileMatches.stream().filter(file -> file.contains(mutantClassFileName)).collect(Collectors.toList());
            if(originalClassFileMatchesWithOneMutant.isEmpty()) {
                logger.error("Mutant class file {} does not have a corresponding original class file", mutantClassFileName);
            } else {
                for (String originalClassFile : originalClassFileMatchesWithOneMutant) {
                    boolean isContentIdentical = FileUtil.isFileIdentical(mutantClassFile, originalClassFile);
                    if (isContentIdentical) {
                        curClassFileIdentical = true;
                    }
                }
            }
            res = res && curClassFileIdentical;
        }

        return res;
    }
}
