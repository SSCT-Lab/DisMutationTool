package com.example.mutantgen;

import com.example.Project;
import com.example.utils.Config;
import com.example.utils.FileUtil;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class EquivalentMutantFilter {

    private final Project project;

    public EquivalentMutantFilter(Project project) {
        this.project = project;
        buildOriginalProject();
    }

    private void buildOriginalProject() {
        InvocationRequest request = new DefaultInvocationRequest();
        if (project.getProjectType() == Project.ProjectType.MAVEN) {
            String pomPath = project.getBasePath() + "/pom.xml";
            request.setPomFile(new File(pomPath));
            request.setGoals(Collections.singletonList("clean compile"));
            // request.setBaseDirectory(new File(project.getBasePath()));
            Invoker invoker = new DefaultInvoker();
            try {
                InvocationResult result = invoker.execute(request);
                if (result.getExitCode() != 0) {
                    throw new RuntimeException("build failed");
                } else {
                    copyOriginalBytecode();
                }
            } catch (MavenInvocationException e) {
                e.printStackTrace();
                throw new RuntimeException("build failed");
            }
        } else {
            // TODO ant?
        }
    }

    // 将srcPath中.class文件复制到tarPath中
    private void copyOriginalBytecode(){
        String srcPath = project.getBuildOutputPath();
        String tarPath = Config.ORIGINAL_BYTECODE_PATH;
        List<String> classFiles = FileUtil.getFilesBasedOnPattern(srcPath, ".*\\.class");
        for (String classFile : classFiles) {
            String fileName = FileUtil.getFileName(classFile);
            FileUtil.copyFileToTargetDir(classFile, tarPath, fileName);
        }
    }


}
