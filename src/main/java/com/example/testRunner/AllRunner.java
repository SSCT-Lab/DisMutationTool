package com.example.testRunner;

import com.example.Project;
import com.example.mutantgen.MutantGenerator;
import com.example.mutator.Mutant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AllRunner {
    private static final Logger logger = LogManager.getLogger(AllRunner.class);
    private List<Mutant> mutantLs;
    private final Project project;

    public AllRunner(Project project) {
        this.project = project;
    }

    public void run() {
        MutantGenerator mutantGenerator = new MutantGenerator(project);
        mutantLs = mutantGenerator.generateMutants();
        for (Mutant mutant: mutantLs){
            MutantRunnerScript mutantRunner = new MutantRunnerScript(mutant, project);
            mutantRunner.run();
        }
    }
}
