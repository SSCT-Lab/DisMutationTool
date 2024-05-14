package com.example;

import com.example.utils.Config;
import com.example.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileUtilTest {
    private static final Logger logger = LogManager.getLogger(FileUtilTest.class);

    @Test
    public void testGetFileDir() {
        String absolutePath = Config.TEST_RESOURCES_PATH + "/AuthTest.java";
        System.out.println(FileUtil.getFileDir(absolutePath));
    }

    @Test
    public void testGetFileName() throws FileNotFoundException {
        String absolutePath = Config.TEST_RESOURCES_PATH + "/AuthTest.java";
        System.out.println(FileUtil.getFileName(absolutePath));
    }

    @Test
    public void testCopyFileToTargetDir() throws FileNotFoundException {
        String filePath = Config.TEST_RESOURCES_PATH + "/AuthTest.java";
        String targetDir = Config.TEST_RESOURCES_PATH;
        String targetName = FileUtil.getFileName(filePath) + "_copy.java";
        FileUtil.copyFileToTargetDir(filePath, targetDir, targetName);
        // delete AuthTest.java
        new File(targetDir + "/" + targetName).delete();
    }

    @Test
    public void testFileDiff() throws IOException {
        String file1 = Config.TEST_RESOURCES_PATH + "/mutants/RFB/ClientCnxnSocketNetty_RFB_1.java";
        String file2 = Config.TEST_RESOURCES_PATH + "/mutants/RFB/ClientCnxnSocketNetty_RFB_2.java";
        String file3 = Config.TEST_RESOURCES_PATH + "/mutants/RFB/ClientCnxnSocketNetty_RFB_3.java";
        String file4 = Config.TEST_RESOURCES_PATH + "/mutants/RFB/ClientCnxnSocketNetty_RFB_4.java";
        String file5 = Config.TEST_RESOURCES_PATH + "/mutants/RFB/ClientCnxnSocketNetty_RFB_5.java";
        String fileOriginal = Config.TEST_RESOURCES_PATH + "/original/RFB/ClientCnxnSocketNetty.java";
        FileUtil.fileDiff(file1, fileOriginal);
        FileUtil.fileDiff(file2, fileOriginal);
        FileUtil.fileDiff(file3, fileOriginal);
        FileUtil.fileDiff(file4, fileOriginal);
        FileUtil.fileDiff(file5, fileOriginal);
    }
}
