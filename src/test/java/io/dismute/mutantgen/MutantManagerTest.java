package io.dismute.mutantgen;

import io.dismute.singleton.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;

import java.util.List;

public class MutantManagerTest {
    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();

    private static final Logger logger = LogManager.getLogger(MutantManagerTest.class);

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
    public void testZkGenerateMutantsWithSingleMutator() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
    }

    @Test
    public void testZkGenerateMutantsWithMultipleMutator() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT,RFE,UFE,BCS");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
    }

    @Test
    public void testCasGenerateMutantsWithSingleMutator() {
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("MNT");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
    }

    @Ignore
    @Test
    public void testCasGenerateMutantsWithAllMutator() { // 仅作尝试，目前变异体规模：过滤前311个
        testResourceManager.casSetUp();
        TestUtils.initializeCassandraProjectFromArgs("ALL");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
    }

    @Test
    public void testKafkaGenerateMutantsWithSingleMutator() {
        testResourceManager.kafkaSetUp();
        TestUtils.initializeKafkaProjectFromArgs("RCS");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
    }

    @Ignore
    @Test
    public void testKafkaGenerateMutantsWithAllMutator() { // 仅作尝试，目前变异体规模：355, 1min31sec
        testResourceManager.kafkaSetUp();
        TestUtils.initializeKafkaProjectFromArgs("ALL");
        mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants();
        assert !mutants.isEmpty();
    }


}
