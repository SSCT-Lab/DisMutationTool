package io.dismute;

import io.dismute.engine.RunningEngine;
import io.dismute.singleton.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class App {

    private static final Logger logger = LogManager.getLogger(App.class);


    public static void main(String[] args) {
        // 1. 初始化Project对象
        Project.initialize(args);
        // 2. 生成变异体
        RunningEngine runningEngine = RunningEngine.getInstance();
        runningEngine.run();
    }

}