package com.example.mutator.network;

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

public class MNRTest {
    static Project zkProject;
    static MutantManager mutantManager;

    private static final Logger logger = LogManager.getLogger(MNRTest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        mutantManager = TestUtils.generateZKMutantManager(MutatorType.MNR);
        zkProject = mutantManager.getProject();
    }

    @Test
    public void testGenMNRMutant() {
        mutantManager.generateMutants();
        // 打印文件的diff
        logger.info("Print diff for mutants");
        for (Mutant mutant : mutantManager.getMutantLs()) {
            FileUtil.fileDiff( mutant.getMutatedPath(), mutant.getOriginalCopyPath());
        }
    }
}
