package io.dismute.mutantfilter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.utils.FileUtil;
import io.dismute.utils.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
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
        logger.info(LogUtil.centerWithSeparator("Filtering Equivalent Mutants"));
        // 1. 读取Project.originalBytecodePath目录下的所有字节码文件
        // 2. 读取Project.mutantBytecodePath目录下的所有字节码文件
        // 3. 两两比较，过滤掉语义等价的变异体
        // 4. 将过滤后的变异体存储在Project.filteredMutantBytecodePath目录中
        Project project = Project.getInstance();
        String originalBytecodePath = project.getOriginalBytecodePath();
        List<String> allOriginalBytecodeFiles = FileUtil.getFilesBasedOnPattern(originalBytecodePath, ".*\\.class"); // engine拷贝过来的原始项目字节码
        List<Mutant> filtered = new ArrayList<>();
        for (int i = 0; i < mutants.size(); i++) {
            Mutant mutant1 = mutants.get(i);
            boolean isEquivalent = false;
            // 1. 和其他变异体比较
            for (int j = i + 1; j < mutants.size(); j++) {
                Mutant mutant2 = mutants.get(j);
                if (isEquivalentMutant(mutant1, mutant2)) {
                    isEquivalent = true;
                    logger.info("Mutant {} is equivalent to mutant {}", mutant1.getMutatedName(), mutant2.getMutatedName());
                    break;
                }
            }
            // 2. 和原始字节码比较
            if (!isEquivalent && isEquivalentOriginalBytecode(mutant1, allOriginalBytecodeFiles)) {
                isEquivalent = true;
                logger.info("Mutant {} is equivalent to original bytecode", mutant1.getMutatedName());
            }
            if (!isEquivalent) {
                filtered.add(mutant1);
            }
        }
        logger.info("Bytecode filter completed. Mutant size BEFORE: {}, AFTER: {}", mutants.size(), filtered.size());
        return filtered;
    }

    // 比较两个Mutant的字节码是否等价
    private static boolean isEquivalentMutant(Mutant mutant1, Mutant mutant2) {
        Project project = Project.getInstance();
        String mutant1ClassFileBasePath = project.getMutantBytecodePath() + File.separator + FileUtil.getNameWithoutExtension(mutant1.getMutatedPath());
        List<String> mutant1ClassFiles = FileUtil.getFilesBasedOnPattern(mutant1ClassFileBasePath, ".*\\.class");
        String mutant2ClassFileBasePath = project.getMutantBytecodePath() + File.separator + FileUtil.getNameWithoutExtension(mutant2.getMutatedPath());
        List<String> mutant2ClassFiles = FileUtil.getFilesBasedOnPattern(mutant2ClassFileBasePath, ".*\\.class");
        if(mutant1ClassFiles.size() != mutant2ClassFiles.size()) {
            return false;
        }
        // 排序两个列表，保证顺序一致，以便比较
        Collections.sort(mutant1ClassFiles);
        Collections.sort(mutant2ClassFiles);
        // 打印出两个列表的内容
//        logger.info("m1---------------------------------------------");
//        for(String mutant1ClassFile : mutant1ClassFiles) {
//            logger.info("Mutant class file: {}", mutant1ClassFile);
//        }
//        logger.info("m2---------------------------------------------");
//        for(String mutant2ClassFile : mutant2ClassFiles) {
//            logger.info("Mutant class file: {}", mutant2ClassFile);
//        }
        for(int i = 0; i < mutant1ClassFiles.size(); i++) {
            String mutant1ClassFile = mutant1ClassFiles.get(i);
            String mutant2ClassFile = mutant2ClassFiles.get(i);
            String fileName1 = FileUtil.getNameWithoutExtension(mutant1ClassFile);
            String fileName2 = FileUtil.getNameWithoutExtension(mutant2ClassFile);
            if(!fileName1.equals(fileName2)) { // 不同名的class文件，不可能是等价的
                return false;
            }
            boolean isContentIdentical = FileUtil.isFileIdentical(mutant1ClassFile, mutant2ClassFile);
            if(!isContentIdentical) {
                return false;
            }
        }
        return true;
    }

    // 比较变异体的字节码和原始字节码是否等价，原始字节码作为参数传进来防止每次都要读取
    private static boolean isEquivalentOriginalBytecode(Mutant mutant, List<String> allOriginalBytecodeFiles) {
        String mutantClassFileDir = Project.getInstance().getMutantBytecodePath() + File.separator + mutant.getMutatedNameWithoutExtension();
        List<String> mutantClassFiles = FileUtil.getFilesBasedOnPattern(mutantClassFileDir, ".*\\.class");
        List<String> mutantClassFileNames = mutantClassFiles.stream().map(FileUtil::getNameWithoutExtension).collect(Collectors.toList()); // 变异体的字节码文件名，不带路径，不带.class，用于和源码匹配
        List<String> originalClassFiles = allOriginalBytecodeFiles.
                stream().
                filter(file -> mutantClassFileNames.contains(FileUtil.getNameWithoutExtension(file)))
                .collect(Collectors.toList());
        logger.info("---------------------------------------------");
        for(String mutantClassFileName : mutantClassFileNames) {
            logger.info("Mutant class file: {}", mutantClassFileName);
        }
        logger.info("---------------------------------------------");
        for(String originalClassFile : originalClassFiles) {
            logger.info("Original class file: {}", originalClassFile);
        }
        if (mutantClassFiles.size() > originalClassFiles.size()) { // 变异体的字节码文件比原始字节码文件多，异常情况
            logger.error("Mutant {} has more class files than original bytecode", mutant.getMutatedName());
            return false;
        }
        // 两两比较
        for(String mutantClassFile : mutantClassFiles) {
            String mutantClassFileName = FileUtil.getNameWithoutExtension(mutantClassFile);
            // 找出mutantClassFileName对应的originalClassFile（可能有多个）
            List<String> originalClassFileMatchesWithOneMutant = originalClassFiles.stream().filter(file -> file.contains(mutantClassFileName)).collect(Collectors.toList());
            boolean curMutantClassFileIdentical = false; // 在originalClassFileMatchesWithOneMutant中找到了和mutantClassFile相同的文件
            if(originalClassFileMatchesWithOneMutant.isEmpty()) {
                logger.error("Mutant class file {} does not have a corresponding original class file", mutantClassFileName);
                return false;
            } else {
                for (String originalClassFile : originalClassFileMatchesWithOneMutant) {
                    logger.info("Comparing mutant class file {} with original class file {}", mutantClassFile, originalClassFile);
                    boolean isContentIdentical = FileUtil.isFileIdentical(mutantClassFile, originalClassFile);
                    if (isContentIdentical) {
                        logger.info("Mutant class file {} is equivalent to original class file {}", mutantClassFileName, originalClassFile);
                        curMutantClassFileIdentical = true;
                        break;
                    }
                }
            }
            if(!curMutantClassFileIdentical) { // 如果有一个mutantClassFile没有对应的originalClassFile，说明不是等价的
                logger.info("Mutant class file {} is not equivalent to any original class file", mutantClassFileName);
                return false;
            }
        }
        return true;
    }
}
