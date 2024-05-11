package com.example.mutator.deprecated;

import com.example.MutantManager;
import com.example.Project;
import com.example.TestUtils;
import com.example.mutator.Mutant;
import com.example.mutator.MutatorType;
import com.example.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class RFETTest {
    static Project zkProject;
    static MutantManager mutantManager;

    private static final Logger logger = LogManager.getLogger(RFETTest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        mutantManager = TestUtils.generateZKMutantManager(MutatorType.RFET);
        zkProject = mutantManager.getProject();
    }

    @Test
    public void testGenRFETMutant() throws IOException {
        mutantManager.generateMutants();
        // 打印文件的diff
        logger.info("Print diff for mutants");
        logger.info("Mutant size: " + mutantManager.getMutantLs().size());
        for (Mutant mutant : mutantManager.getMutantLs()) {
            FileUtil.fileDiff( mutant.getMutatedPath(), mutant.getOriginalCopyPath());
        }
    }
}
