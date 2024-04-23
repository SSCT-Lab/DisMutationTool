package com.example;

import com.example.utils.Config;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Collections;

public class MavenInvokerExample {
    public static void main(String[] args) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(Config.ZK_PROJECT_PATH + "/pom.xml"));
        request.setGoals(Collections.singletonList("test"));
        request.setBaseDirectory(new File(Config.ZK_PROJECT_PATH));

        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                System.out.println("测试运行失败");
            } else {
                System.out.println("测试运行成功");
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
}
