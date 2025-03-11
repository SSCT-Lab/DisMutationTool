package io.dismute.adapter;

import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutantManager;
import io.dismute.singleton.Project;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;
import io.dismute.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class MavenAdapterTest {
    private static final Logger logger = LogManager.getLogger(MavenAdapterTest.class);

    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();

    private static MutantManager mutantManager;

    @BeforeClass
    public static void setup() {
        testResourceManager.zkSetUp();
    }

    @AfterClass
    public static void cleanup() {
        testResourceManager.tearDown();
    }

    @After
    public void tearDown() {
        Project.reset();
        MutantManager.reset();
    }

    @Test
    public void testMavenTestExecutionWithSingleMutant() {
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        mutantManager.generateMutants();
        assert !mutantManager.getMutantLs().isEmpty();
        Mutant mutant = mutantManager.getMutantLs().get(0);
        BuildToolAdapter mavenAdapter = new MavenAdapter();
        mavenAdapter.testExecution(mutant);
        // assert project.getTestOutputsPath 并不为空(包含脚本输出), 并且均为.log文件
        Project zkProject = Project.getInstance();
        String logPath = zkProject.getTestOutputsPath();
        File[] logFiles = new File(logPath).listFiles();
        assert logFiles != null;
        for (File logFile : logFiles) {
            assert logFile.getName().endsWith(".log");
        }
    }

    @Test
    public void testMavenTestExecutionWithMultipleMutants() {
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        mutantManager.generateMutants();
        assert !mutantManager.getMutantLs().isEmpty();
        assert mutantManager.getMutantLs().size() > 5;
        BuildToolAdapter mavenAdapter = new MavenAdapter();
        for (Mutant mutant : mutantManager.getMutantLs().subList(0, 5)) {
            mavenAdapter.testExecution(mutant);
        }
        // assert project.getTestOutputsPath 并不为空(包含脚本输出), 并且均为.log文件
        Project zkProject = Project.getInstance();
        String logPath = zkProject.getTestOutputsPath();
        File[] logFiles = new File(logPath).listFiles();
        assert logFiles != null;
        for (File logFile : logFiles) {
            assert logFile.getName().endsWith(".log");
        }
    }

    @Test
    public void testMavenIncrementalCompilationWithSingleMutant() {
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        mutantManager.generateMutants();
        assert !mutantManager.getMutantLs().isEmpty();
        Mutant mutant = mutantManager.getMutantLs().get(0);
        BuildToolAdapter mavenAdapter = new MavenAdapter();
        mavenAdapter.cleanAndGenerateClassPath();
        boolean isCompilationSuccess = mavenAdapter.incrementalCompilation(mutant);
        // assert project.getMutantBytecodePath 并不为空(包含编译输出), 并且均为.class文件
        // 如果增量编译成功，列出编译后的.class文件
        if(!isCompilationSuccess) {
            logger.error("Incremental compilation failed for mutant {}", mutant.getMutatedName());
            return;
        }
        List<String> classFiles = FileUtil.getFilesBasedOnPattern(Project.getInstance().getMutantBytecodePath(), ".*\\.class");
        for(String classFile : classFiles) {
            logger.info("Class file: {}", classFile);
        }
        assert !classFiles.isEmpty();
    }
}
