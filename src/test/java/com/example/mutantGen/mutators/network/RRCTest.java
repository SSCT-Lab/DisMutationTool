package com.example.mutantGen.mutators.network;

import com.example.MutantManager;
import com.example.Project;
import com.example.TestUtils;
import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutatorType;
import com.example.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class RRCTest {
    static Project zkProject;
    static MutantManager mutantManager;

    private static final Logger logger = LogManager.getLogger(RRCTest.class);

    @BeforeClass
    public static void setUp() {
        TestUtils.clearMutantAndOriginalDir();
        mutantManager = TestUtils.generateZKMutantManager(MutatorType.RRC);
        zkProject = mutantManager.getProject();
    }

    @Test
    public void testGenRRCMutant() {
        mutantManager.generateMutants();
        // 打印文件的diff
        logger.info("Print diff for mutants");
        for (Mutant mutant : mutantManager.getMutantLs()) {
            FileUtil.fileDiff( mutant.getMutatedPath(), mutant.getOriginalCopyPath());
        }
    }
}
