package com.example;

import com.example.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;

public class ProjectTest {
    Project testProject;

    @Before
    public void setUp() {
        testProject = TestUtils.generateZKProject();
    }


    @Test
    public void testGetJavaFileLs() {
        for (String path : testProject.getSrcFileLs()) {
            System.out.println(path);
        }
        System.out.println("Total src files: " + testProject.getSrcFileLs().size());

        System.out.println("\n====================================\n");


        for (String path : testProject.getTestFileLs()) {
            System.out.println(path);
        }
        System.out.println("Total test files: " + testProject.getTestFileLs().size());
    }

    @Test
    public void testCatFile(){
        String selectedPath = testProject.getTestFileLs().get(0);
        FileUtil.showFileContent(selectedPath);
    }
}
