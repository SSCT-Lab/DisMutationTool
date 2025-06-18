package io.dismute.mutantfilter;

import io.dismute.engine.RunningEngine;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutantManager;
import io.dismute.singleton.Project;
import io.dismute.singleton.PropertiesFile;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class AllMutantFilterTest {

    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();
    private static final PropertiesFile propertiesFile = PropertiesFile.getInstance();
    private static final Logger logger = LogManager.getLogger(AllMutantFilterTest.class);

    @AfterClass
    public static void cleanup() {
        testResourceManager.tearDown();
    }

    @After
    public void tearDown() {
        Project.reset();
    }

    @Test
    public void testAllMutantFilterWithZk() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("ALL");
        RunningEngine engine = RunningEngine.getInstance();
        engine.generateClasspath();
        engine.compileAndCopyOriginalBytecode();
        engine.generateMutantsAndFilter();
    }
}
