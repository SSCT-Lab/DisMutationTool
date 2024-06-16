package com.example.testRunner;

import com.example.Project;
import com.example.mutator.Mutant;

import java.util.List;

public class DockerRunner {
    private List<Mutant> mutantLs;
    private final Project project;
    private int containerCnt = 3;
    private String dockerfilePath;

    public DockerRunner(Project project) {
        this.project = project;
    }

    public DockerRunner(Project project, int containerCnt) {
        this.project = project;
        this.containerCnt = containerCnt;
    }

    // 构建docker容器，分发变异体，容器内使用all runner运行测试
    public void run(){

    }


}
