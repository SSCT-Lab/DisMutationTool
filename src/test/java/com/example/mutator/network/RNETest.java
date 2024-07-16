package com.example.mutator.network;

import com.example.Project;
import com.example.TestUtils;
import com.example.mutantgen.MutantGenerator;
import com.example.mutator.MutatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class RNETest {
    static Project zkProject;
    private static final Logger logger = LogManager.getLogger(RNETest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        zkProject = TestUtils.generateZKProject(MutatorType.RNE);
    }

    @Test
    public void testGenRNEMutant() throws IOException {
        MutantGenerator mutantGenerator = new MutantGenerator(zkProject);
        mutantGenerator.generateMutantsWithoutFilterEq();

    }
}
