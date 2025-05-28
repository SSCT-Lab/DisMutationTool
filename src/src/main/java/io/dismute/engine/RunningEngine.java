package io.dismute.engine;

import com.google.common.annotations.VisibleForTesting;
import io.dismute.adapter.BuildToolAdapter;
import io.dismute.coverage.CoverageManager;
import io.dismute.mutantfilter.*;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutantManager;
import io.dismute.singleton.Project;
import io.dismute.utils.FileUtil;
import io.dismute.utils.LogUtil;
import io.dismute.utils.MutantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 替代app的作用，Project, MutantManager，TestRunner管理
// docker、coverage模式各自运行逻辑
public class RunningEngine {

    private static final Logger logger = LogManager.getLogger(RunningEngine.class);

    private static volatile RunningEngine instance;

    private Project project;
    private MutantManager mutantManager;
    private BuildToolAdapter buildToolAdapter;
    private CoverageManager coverageManager;

    public static RunningEngine getInstance() {
        if (instance == null) {
            synchronized (RunningEngine.class) {
                if (instance == null) {
                    instance = new RunningEngine();
                }
            }
        }
        return instance;
    }

    /**
     * --获取资源--
     * 1. 获取Project实例
     * 2. 获取MutantManager实例
     * 3. 根据项目类型获取对应BuildAdapter实例
     */
    private RunningEngine() {
        project = Project.getInstance();
        if (project.isCoverage()) {
            CoverageManager.initialize(project.getCoveragePath());
            coverageManager = CoverageManager.getInstance();
        }
        mutantManager = MutantManager.getInstance();
        buildToolAdapter = project.getBuildToolAdapter();
    }


    /**
     * --执行操作
     * 1. 编译原始项目，生成classpath.txt用于增量编译
     * 2. 编译原始项目，拷贝字节码
     * 3. 运行原始项目测试用例，确保all green
     * 4. 生成最初的变异体列表
     * 5. 过滤变异体，原则上允许用户编排
     */
    public void run() {
        // 1. 编译原始项目，生成classpath.txt用于增量编译
        generateClasspath();
        // 2. 编译原始项目，拷贝字节码
        compileAndCopyOriginalBytecode();
        // 3. 运行原始项目测试用例，确保all green
        // 为了节省时间，暂时不需要
        // 4. 生成最初的变异体列表，并过滤
        generateMutantsAndFilter();
        // 5. 运行测试
        runTestSuites();
    }

    @VisibleForTesting
    public void generateClasspath() {
        buildToolAdapter.cleanAndGenerateClassPath();
    }

    @VisibleForTesting
    public void compileAndCopyOriginalBytecode() {
        logger.info(LogUtil.centerWithSeparator("Compiling and Copying Original Bytecode"));
        // 1. clean & compile原始项目，生成字节码
        buildToolAdapter.cleanAndCompilation();
        // 2. 根据project.getBuildOutputDirs的结果，扫描所有原始字节码的路径
        List<String> buildOutputDirs = project.getBuildOutputDirs(); // 这是根据预设/用户传入的编译产物正则找到的目录集合，作为字节码搜索起点
        List<String> originalBytecodeFiles = new ArrayList<>();
        for(String buildOutputDir: buildOutputDirs){
            List<String> classFiles = FileUtil.getFilesBasedOnPattern(buildOutputDir, ".*\\.class");
            originalBytecodeFiles.addAll(classFiles);
        }
        // 3. 拷贝原始字节码到指定目录
        for(String originalBytecodeFile: originalBytecodeFiles){
            // 获取字节码文件路径去掉Project.getBasePath()的部分
            String relativeDir = FileUtil.getFileDir(originalBytecodeFile).replace(project.getBasePath(), "");
            String targetDir = project.getOriginalBytecodePath() + File.separator + relativeDir;
            FileUtil.copyFileToTargetDir(originalBytecodeFile, targetDir, FileUtil.getNameWithoutExtension(originalBytecodeFile) + ".class");
        }
        logger.info("Total original bytecode files copied: {}", originalBytecodeFiles.size());
    }

    @VisibleForTesting
    public void generateMutantsAndFilter() {
        List<Mutant> mutants = mutantManager.generateMutants();
        MutantFilter syntacticMutantFilter = new SyntacticMutantFilter();
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        MutantFilter identicalMutantFilter = new IdenticalMutantFilter();
        MutantFilter bytecodeMutantFilter = new BytecodeMutantFilter();
        // 按照以上顺序依次过滤
        mutants = syntacticMutantFilter.filter(mutants);
        mutants = semanticMutantFilter.filter(mutants);
        mutants = identicalMutantFilter.filter(mutants);
        mutants = bytecodeMutantFilter.filter(mutants);
        // 将过滤后的变异体另存到 project.getMutantFilteredPath() 中，并更新mutantManager中的统计信息
        mutantManager.setMutants(mutants);
        for (Mutant mutant : mutants) {
            String targetDir = project.getMutantFilteredPath();
            String targetName = mutant.getMutatedName();
            FileUtil.copyFileToTargetDir(mutant.getMutatedPath(), targetDir, targetName);
        }
    }

    @VisibleForTesting
    public void runTestSuites() {
        logger.info(LogUtil.centerWithSeparator("Running Test Suites"));
        int curMutantNo = 0;
        int totalMutantNo = mutantManager.getMutantLs().size();
        for(Mutant mutant: mutantManager.getMutantLs()) {
            logger.info(LogUtil.centerWithSeparator("TEST STARTED for mvn mutant {}" + mutant.getMutatedName()));
            logger.info("No {} of {}", ++curMutantNo, totalMutantNo);
            MutantUtil.loadMutant(mutant); // 将变异体代码写入项目
            buildToolAdapter.testExecution(mutant);
            MutantUtil.unloadMutant(mutant); // 撤销变异
        }
        logger.info(LogUtil.centerWithSeparator("All Test Suites Finished"));
    }


    @VisibleForTesting
    public void resultAnalysis() {
        // TODO 结果分析
    }

    private boolean isCoverageEnabled() {
        return Project.getInstance().isCoverage();
    }
}
