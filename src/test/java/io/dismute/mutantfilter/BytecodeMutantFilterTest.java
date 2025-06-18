package io.dismute.mutantfilter;

import io.dismute.engine.RunningEngine;
import io.dismute.mutantgen.Mutant;
import io.dismute.mutantgen.MutantManager;
import io.dismute.singleton.Project;
import io.dismute.singleton.PropertiesFile;
import io.dismute.testutils.TestResourceManager;
import io.dismute.testutils.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class BytecodeMutantFilterTest {
    private static final TestResourceManager testResourceManager = TestResourceManager.getInstance();
    private static final PropertiesFile propertiesFile = PropertiesFile.getInstance();
    private static final Logger logger = LogManager.getLogger(BytecodeMutantFilterTest.class);


    @AfterClass
    public static void cleanup() {
        testResourceManager.tearDown();
    }

    @After
    public void tearDown() {
        Project.reset();
    }

    // 如果不修改MNT定义的话，对于zookeeper-server-3.5.8来说，MNT_3是一个等效变异体
    // 在1188行 setSoTimeout(0) -> setSoTimeout((0) / 10)
    // 这个变异体能被EquivalentMutantFilter过滤掉
    @Test
    public void testBytecodeMutantFilter() {
        testResourceManager.zkSetUp();
        TestUtils.initializeZookeeperProjectFromArgs("MNT");
        RunningEngine engine = RunningEngine.getInstance();
        engine.generateClasspath();
        engine.compileAndCopyOriginalBytecode();
        MutantManager mutantManager = MutantManager.getInstance();
        List<Mutant> mutants = mutantManager.generateMutants()
                .stream()
                .filter(mutant -> mutant.getMutatedNameWithoutExtension().equals("QuorumCnxManager_MNT_3"))
                .collect(Collectors.toList());
        int originalSize = mutants.size();
        mutantManager.setMutants(mutants);
        MutantFilter semanticMutantFilter = new SemanticMutantFilter();
        MutantFilter bytecodeMutantFilter = new BytecodeMutantFilter();
        // 按照以上顺序依次过滤
        mutants = semanticMutantFilter.filter(mutants);
        mutants = bytecodeMutantFilter.filter(mutants);
        assert mutants.size() < originalSize;
    }
}
