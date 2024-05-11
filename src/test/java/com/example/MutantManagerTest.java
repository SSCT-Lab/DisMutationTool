package com.example;

import com.example.mutator.MutatorType;
import org.junit.Before;
import org.junit.Test;

public class MutantManagerTest {
    Project project;

    @Before
    public void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        project = TestUtils.generateZKProject();
    }

    @Test
    public void testGenerateZKMutant() {
        MutantManager mutantManager = MutantManager.builder()
                .setProject(project)
                .setMutator(MutatorType.RUL)
                .build();
        mutantManager.generateMutants();
        System.out.println("done");
    }
}
