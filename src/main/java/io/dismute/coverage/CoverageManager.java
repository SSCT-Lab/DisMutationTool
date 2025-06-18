package io.dismute.coverage;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


// 管理所有coverage信息，单例，受Engine管理，由Engine初始化
public class CoverageManager {
    private static final Logger logger = LogManager.getLogger(CoverageManager.class);

    @Getter
    // 所有coverage的map，key: src文件名; value<List>：cover该文件的测试类
    private Map<String, List<String>> coverageInfo;

    // 单例实例（volatile 保证多线程可见性）
    private static volatile CoverageManager instance;

    public static CoverageManager getInstance() {
        if (instance == null) {
            synchronized (CoverageManager.class) {
                if (instance == null) {
                    throw new IllegalStateException("CoverageManager has not been initialized yet.");
                }
            }
        }
        return instance;
    }

    public static void initialize(String coverageFilePath) {
        // 检查文件是否存在
        File coverageFile = new File(coverageFilePath);
        if (!coverageFile.exists()) {
            logger.error("Coverage file does not exist: {}", coverageFilePath);
            throw new RuntimeException("Coverage file does not exist: " + coverageFilePath);
        }
        instance = new CoverageManager();
        instance.coverageInfo = new java.util.HashMap<>();
        // 解析覆盖率文件
        instance.parseCoverageFile(coverageFile);
    }

    private void parseCoverageFile(File coverageFile) {
        try (Scanner scanner = new Scanner(coverageFile)) {
            String currentSrcFile = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("Element count:")) {
                    continue; // 忽略空行和计数行
                }
                if (!line.contains(".")) {
                    // 该行为源文件名（例如 TableNotEnabledException），去掉冒号，放入字典
                    currentSrcFile = line.trim().replace(":", "");
                    coverageInfo.putIfAbsent(currentSrcFile, new ArrayList<>());
                } else if (currentSrcFile != null) {
                    // 该行为测试类
                    coverageInfo.get(currentSrcFile).add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read coverage file: " + coverageFile.getAbsolutePath(), e);
        }
    }
}
