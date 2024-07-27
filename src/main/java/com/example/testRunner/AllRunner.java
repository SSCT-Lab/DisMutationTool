package com.example.testRunner;

import com.example.Project;
import com.example.mutantgen.MutantGenerator;
import com.example.mutantrun.MutantRunnerScript;
import com.example.mutator.Mutant;
import com.example.utils.TestRunnerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AllRunner {
    private static final Logger logger = LogManager.getLogger(AllRunner.class);
    private List<Mutant> mutantLs;
    private final Project project;

    public AllRunner(Project project) {
        this.project = project;
    }

    public void run() {
        // 定义资源路径

        String absolutePath = TestRunnerUtil.getScriptPath(project);


        MutantGenerator mutantGenerator = new MutantGenerator(project);
        mutantLs = mutantGenerator.generateMutants();

        for (Mutant mutant : mutantLs) {
            MutantRunnerScript mutantRunner = new MutantRunnerScript(mutant, project);
            mutantRunner.run(absolutePath, "");
        }



//        TestSuiteRunner runner = project.getProjectType() == Project.ProjectType.MAVEN ? new MvnRunner() : new AntRunner();
//        for (Mutant mutant: mutantLs){
//            MutantRunner mutantRunner = new MutantRunner(mutant, project, runner);
//            mutantRunner.run();
//        }


    }
}
