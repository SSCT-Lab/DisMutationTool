package com.example.mutator.network;

import com.example.MutantManager;
import com.example.Project;
import com.example.TestUtils;
import com.example.mutantgen.MutantGenerator;
import com.example.mutator.Mutant;
import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class UNETest {
    static Project zkProject;
    private static final Logger logger = LogManager.getLogger(UNETest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        zkProject = TestUtils.generateZKProject(MutatorType.UNE);
    }

    @Test
    public void testGenUNEMutant() throws IOException {
        MutantGenerator mutantGenerator = new MutantGenerator(zkProject);
        mutantGenerator.generateMutantsWithoutFilterEq();

    }
}
