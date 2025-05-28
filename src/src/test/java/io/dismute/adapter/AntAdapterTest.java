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

public class AntAdapterTest {
    private static final Logger logger = LogManager.getLogger(AntAdapterTest.class);

    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();

    private static MutantManager mutantManager;

    @BeforeClass
    public static void setup() {
        testResourceManager.casSetUp();
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
    public void testAntIncrementalCompilationWithSingleMutant() {
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        mutantManager.generateMutants();
        assert !mutantManager.getMutantLs().isEmpty();
        Mutant mutant = mutantManager.getMutantLs().get(0);
        BuildToolAdapter antAdapter = new AntAdapter();
        antAdapter.cleanAndGenerateClassPath();
        boolean isCompilationSuccess = antAdapter.incrementalCompilation(mutant);
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

    @Test
    public void testAntIncrementalCompilationWithMultiMutant() {
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        mutantManager.generateMutants();
        assert mutantManager.getMutantLs().size() >= 5;
        List<Mutant> mutantsToTest = mutantManager.getMutantLs().subList(0, 5);
        BuildToolAdapter antAdapter = new AntAdapter();
        antAdapter.cleanAndGenerateClassPath();
        for (Mutant mutant : mutantsToTest) {
            boolean isCompilationSuccess = antAdapter.incrementalCompilation(mutant);
            if(!isCompilationSuccess) {
                logger.error("Incremental compilation failed for mutant {}", mutant.getMutatedName());
            } else {
                String curMutantBytecodePath = Project.getInstance().getMutantBytecodePath() + File.separator + FileUtil.getNameWithoutExtension(mutant.getMutatedPath());
                List<String> classFiles = FileUtil.getFilesBasedOnPattern(curMutantBytecodePath, ".*\\.class");
                for(String classFile : classFiles) {
                    logger.info("Class file: {}", classFile);
                }
                assert !classFiles.isEmpty();
            }
        }
    }


}
