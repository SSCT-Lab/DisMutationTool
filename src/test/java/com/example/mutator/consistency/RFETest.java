package com.example.mutator.consistency;

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

public class RFETest {
    static Project zkProject;
    private static final Logger logger = LogManager.getLogger(RFETest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        zkProject = TestUtils.generateZKProject(MutatorType.RFE);
    }

    @Test
    public void testGenRNEMutant() throws IOException {
        MutantGenerator mutantGenerator = new MutantGenerator(zkProject);
        mutantGenerator.generateMutantsWithoutFilterEq();

    }
}
