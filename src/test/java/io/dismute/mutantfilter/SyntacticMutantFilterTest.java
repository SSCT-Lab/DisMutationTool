package io.dismute.mutantfilter;

import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutantManager;
import io.dismute.singleton.Project;
import io.dismute.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;

import java.util.List;

public class SyntacticMutantFilterTest {
    private static final Logger logger = LogManager.getLogger(SyntacticMutantFilterTest.class);

    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();

    private static MutantManager mutantManager ;

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
    public void testSyntacticMutantFilterWithSingleMutantType() {
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
        // 取第一个变异体
        mutants = mutants.subList(0, 1);
        logger.info("Mutant name: {} Mutant path: {}", mutants.get(0).getMutatedName(), mutants.get(0).getMutatedPath());
        MutantFilter syntaxMutantFilter = new SyntacticMutantFilter();
        syntaxMutantFilter.filter(mutants);
    }

    @Test
    public void testSyntacticMutantFilterWithAllMutantTypes() {
        TestUtils.initializeZookeeperProjectFromArgs(Constants.ALL_MUTATORS);
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
        MutantFilter syntaxMutantFilter = new SyntacticMutantFilter();
        syntaxMutantFilter.filter(mutants);
    }

}
