package com.example.utils;

import com.example.Project;
import com.example.mutator.Mutant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.List;

public class MutantUtil {
    private static final Logger logger = LogManager.getLogger(MutantUtil.class.getName());

    // 将变异体代码写入项目
    public static void loadMutant(Mutant mutant) {
        String mutatedFilePath = mutant.getMutatedPath();
        String mutatedFileName = FileUtil.getFileName(mutatedFilePath) + ".java";
        String originalFilePath = mutant.getOriginalPath();
        String originalFileName = FileUtil.getFileName(originalFilePath) + ".java";
        logger.info("======================Mutating " + originalFileName + " with " + mutatedFileName + "...======================");
        // 打印文件的diff
        logger.info("Diff between original file" + mutatedFileName + " and mutant file" + originalFileName + ":");
        FileUtil.fileDiff(mutant.getMutatedPath(), mutant.getOriginalPath());
        FileUtil.copyFileToTargetDir(mutatedFilePath, FileUtil.getFileDir(originalFilePath), originalFileName);
    }

    // 撤销变异
    public static void unloadMutant(Mutant mutant) {
        String originalFilePath = mutant.getOriginalPath();
        String originalFileName = FileUtil.getFileName(originalFilePath) + ".java";
        logger.info("Undo mutating " + originalFileName + "...");
        FileUtil.copyFileToTargetDir(mutant.getOriginalCopyPath(), FileUtil.getFileDir(originalFilePath), originalFileName);
    }

    // 将mutants列表序列化
    public static void serializeMutantLs(List<Mutant> mutants) {
        String path = Project.MUTANT_OUTPUT_PATH + "/" + Constants.persistMutantsName;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(mutants);
            logger.info("Mutants are persisted to " + path);
        } catch (IOException e) {
            logger.error("Error while persisting mutants");
            throw new RuntimeException("Error while persisting mutants");
        }
    }

    public static List<Mutant> deserializeMutantLs(String path) {
        List<Mutant> mutants = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            mutants = (List<Mutant>) ois.readObject();
            logger.info("Mutants are deserialized from " + path);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error while deserializing mutants");
            throw new RuntimeException("Error while deserializing mutants");
        }
        return mutants;
    }
}
