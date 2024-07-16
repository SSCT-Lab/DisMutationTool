package com.example.testRunner;

import com.example.Project;
import com.example.mutantgen.MutantGenerator;
import com.example.mutantrun.MutantRunnerScript;
import com.example.mutator.Mutant;
import com.example.utils.FileUtil;
import com.example.utils.TestRunnerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CoverageBasedRunner {
    private static final Logger logger = LogManager.getLogger(AllRunner.class);
    private List<Mutant> mutantLs;
    private final Project project;
    private String coverageFilePath;

    public CoverageBasedRunner(Project project, String coverageFilePath) {
        this.project = project;
        this.coverageFilePath = coverageFilePath;
    }

    public void run() {
        // 处理coverageFilePath文件内容

        String absolutePath = TestRunnerUtil.getScriptPath(project);

        MutantGenerator mutantGenerator = new MutantGenerator(project);
        mutantLs = mutantGenerator.generateMutants();

        Map<String, Set<String>> coverageMap = parseTestClasses(coverageFilePath);

        for (Mutant mutant : mutantLs) {
            StringBuilder args = new StringBuilder();
            args.append("-Dtest=");
            String originalClassName = FileUtil.getFileName(mutant.getOriginalPath()); // 从path中去掉路径前缀和.java后缀，直接获取文件名
            Set<String> originalClassCoverageInfo = coverageMap.get(originalClassName);
            if (originalClassCoverageInfo == null) {
                args = new StringBuilder().append("-DskipTests");
            } else {
                for (String originalClassCoverage : originalClassCoverageInfo) {
                    args.append(originalClassCoverage);
                    args.append(",");
                }
            }
            logger.info(originalClassName + "\t" + args);
            MutantRunnerScript mutantRunner = new MutantRunnerScript(mutant, project);
            mutantRunner.run(absolutePath, args.toString());
        }

    }


    public static Map<String, Set<String>> parseTestClasses(String filePath) {
        Map<String, Set<String>> result = new HashMap<>();
        String currentSourceFile = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Check if the line ends with a colon, indicating a source file
                if (line.endsWith(":")) {
                    currentSourceFile = line.substring(0, line.length() - 1);
                } else if (line.contains(".")) { // Check if the line contains a dot to identify a test class
                    if (currentSourceFile != null) {
                        // Add the test class to the set for the current source file
//                        result.computeIfAbsent(currentSourceFile, k -> new HashSet<>()).add(line.substring(0, line.lastIndexOf(".")));
                        result.computeIfAbsent(currentSourceFile, k -> new HashSet<>()).add(line);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading coverage info from " + filePath, e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return result;
    }
}
