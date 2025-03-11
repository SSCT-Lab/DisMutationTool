package io.dismute.mutantfilter;

import io.dismute.adapter.BuildToolAdapter;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutantManager;
import io.dismute.singleton.Project;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;

import java.util.List;

public class SemanticMutantFilterTest {

    private static final Logger logger = LogManager.getLogger(SemanticMutantFilterTest.class);

    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();

    private static MutantManager mutantManager ;

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
    public void testSemanticMutantFilterUsingMNTWithZkProject() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        BuildToolAdapter buildToolAdapter = Project.getInstance().getBuildToolAdapter();
        buildToolAdapter.cleanAndGenerateClassPath();
        assert !mutants.isEmpty();
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        semanticMutantFilter.filter(mutants);
    }


    @Test
    public void testSemanticMutantFilterUsingSCSWithZkProject() { // 70 before, 54 after
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("SCS");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        BuildToolAdapter buildToolAdapter = Project.getInstance().getBuildToolAdapter();
        buildToolAdapter.cleanAndGenerateClassPath();
        assert !mutants.isEmpty();
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        semanticMutantFilter.filter(mutants);
    }

    //  Semantic filter completed. Mutant size BEFORE: 387, AFTER: 326
    @Ignore // 3min7sec
    @Test
    public void testSemanticMutantFilterWithAllMutatorWithZkProject() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("All");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        BuildToolAdapter buildToolAdapter = Project.getInstance().getBuildToolAdapter();
        buildToolAdapter.cleanAndGenerateClassPath();
        assert !mutants.isEmpty();
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        semanticMutantFilter.filter(mutants);
    }

    @Test
    public void testSemanticMutantFilterWithMNTMutatorWithCasProject() {
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        BuildToolAdapter buildToolAdapter = Project.getInstance().getBuildToolAdapter();
        buildToolAdapter.cleanAndGenerateClassPath();
        assert !mutants.isEmpty();
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        semanticMutantFilter.filter(mutants);
    }


    // 4min26sec
    @Ignore // Mutant size BEFORE: 311, AFTER: 268
    @Test
    public void testSemanticMutantFilterWithAllMutatorWithCasProject() {
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("All");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        BuildToolAdapter buildToolAdapter = Project.getInstance().getBuildToolAdapter();
        buildToolAdapter.cleanAndGenerateClassPath();
        assert !mutants.isEmpty();
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        semanticMutantFilter.filter(mutants);
    }
}
