package com.example.mutator.concurrency;

import com.example.Project;
import com.example.TestUtils;
import com.example.mutantgen.MutantGenerator;
import com.example.mutator.MutatorType;
import com.example.mutator.network.RNETest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class RCETest {
    static Project zkProject;
    private static final Logger logger = LogManager.getLogger(RCETest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        zkProject = TestUtils.generateZKProject(MutatorType.RCE);
    }

    @Test
    public void testGenRNEMutant() throws IOException {
        MutantGenerator mutantGenerator = new MutantGenerator(zkProject);
        mutantGenerator.generateMutantsWithoutFilterEq();

    }
}
