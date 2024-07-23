package com.example.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {

    private static final Logger logger = LogManager.getLogger(FileUtil.class);


    /**
     * This method is used to get a list of file paths from a given root directory based on a provided pattern.
     * It recursively traverses the directory structure, matching file paths against the pattern.
     *
     * @param rootDir The root directory from where the search begins.
     * @param pattern The pattern to match the file paths against. This is a regular expression pattern.
     * @return A list of file paths that match the provided pattern.
     */
    public static List<String> getFilesBasedOnPattern(String rootDir, String pattern) {
        List<String> files = new ArrayList<>();
        File file = new File(rootDir);
        if (file.isDirectory()) {
            File[] fileLs = file.listFiles();
            for (File f : fileLs) {
                if (f.isDirectory()) {
                    files.addAll(FileUtil.getFilesBasedOnPattern(f.getAbsolutePath(), pattern));
                } else {
                    // 如果文件名称（包含路径）符合正则表达式，则加入到列表中
                    if (f.getName().matches(pattern)) {
                        files.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return files;
    }

    /**
     * 在控制台上打印一个文件的内容
     * This method is used to show the content of a file.
     * @param path file path
     */
    public static void showFileContent(String path) {
        File file = new File(path);
        if (file.exists()) {
            logger.info("File path: " + path);
            logger.info("File content:");
            try {
                java.nio.file.Files.lines(file.toPath()).forEach(System.out::println);
            } catch (Exception e) {
                logger.error("Error reading file: " + path);
            }
        } else {
            logger.error("File not exist: " + path);
        }
    }

    /**
     * 获取文件的父目录
     * @param path
     * @return
     */
    public static String getFileDir(String path){
        File file = new File(path);
        if (file.exists()) {
            return file.getParent();
        } else {
            logger.error("File not exist: " + path);
            return null;
        }
    }

    /***
     * 获取文件名，并且去掉拓展名
     * This method is used to get the file name from a file path.
     * @param path file path
     * @return file name
     */
    public static String getFileName(String path) {
        File file = new File(path);
        if (file.exists()) {
            String fileName = file.getName();
            int index = fileName.lastIndexOf(".");
            if (index != -1) {
                return fileName.substring(0, index);
            } else {
                return fileName;
            }
        } else {
            logger.error("File not exist: " + path);
            throw new RuntimeException("File not exist: " + path);
        }
    }

    /**
     * 复制文件到目标文件夹并指定复制后的名称，若目标文件夹不存在则创建
     * @param sourcePath 源文件路径（包含文件名）
     * @param targetDir 目标文件夹路径
     * @param targetName 目标文件名
     * @throws FileNotFoundException 源文件不存在时抛出异常
     */
    public static void copyFileToTargetDir(String sourcePath, String targetDir, String targetName) {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            logger.error("Source file does not exist: " + sourcePath);
        }

        File targetDirFile = new File(targetDir);
        if (!targetDirFile.exists()) {
            targetDirFile.mkdirs(); // Create target directory if it doesn't exist
        }

        File targetFile = new File(targetDirFile, targetName);

        // 判断是否存在同名文件
        if (targetFile.exists()) {
            logger.info("File already exists, replacing it: " + targetFile.getAbsolutePath());
            targetFile.delete();
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            logger.error("Error occurred while copying file: " + e.getMessage());
        }
    }


    /**
     * 将字符串写入文件
     * @param content 内容
     * @param path  文件路径
     */
    public static void writeToFile(String content, String path){
        // 判断文件是否存在，存在则提示
        File file = new File(path);
        if (file.exists()) {
            logger.info("File already exists, replace it: " + file.getAbsolutePath());
            file.delete();
        }

        try {
            FileWriter fw = new FileWriter(path);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fileDiff(String file1Path, String file2Path) {
        try {
            List<String> file1Lines = Files.readAllLines(new File(file1Path).toPath(), StandardCharsets.UTF_8);
            List<String> file2Lines = Files.readAllLines(new File(file2Path).toPath(), StandardCharsets.UTF_8);
            Patch<String> patch = DiffUtils.diff(file1Lines, file2Lines);

            logger.info("diff " + file1Path + " " + file2Path);
            logger.info("***************");

            for (Delta<String> delta : patch.getDeltas()) {
                int origStart = delta.getOriginal().getPosition() + 1;
                int origEnd = origStart + delta.getOriginal().size() - 1;
                int revisedStart = delta.getRevised().getPosition() + 1;
                int revisedEnd = revisedStart + delta.getRevised().size() - 1;

                logger.info("*** " + origStart + "," + origEnd + " ****");
                for (Object line : delta.getOriginal().getLines()) {
                    logger.info("- " + line);
                }
                logger.info("--- " + revisedStart + "," + revisedEnd + " ----");
                for (Object line : delta.getRevised().getLines()) {
                    logger.info("+ " + line);
                }
                logger.info("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isFileIdentical(String path1, String path2) {
        try {
            byte[] hash1 = Files.readAllBytes(new File(path1).toPath());
            byte[] hash2 = Files.readAllBytes(new File(path2).toPath());

            return MessageDigest.isEqual(hash1, hash2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDirIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}
