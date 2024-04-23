package com.example;

import org.apache.zookeeper.ZooKeeperTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;


import java.util.List;

/**
 * Hello world!
 */
public class ExternalTestRunner {

    @BeforeClass
    public static void setSystemProperties() {
        System.setProperty("build.test.dir", "/home/zdc/code/ideaRemote/distributed-mutation-tool/target/surefire");
    }

    @Test
    public void test1() throws ClassNotFoundException {
        JUnitCore core = new JUnitCore();
        // print all test outputs to console
        core.addListener(new DetailedRunListener());

        Project zkProject = Project.builder()
                .setBasePath("/home/zdc/code/distributedSystems/zk/apache-zookeeper-3.5.8/zookeeper-server")
                .withSrcPattern(".*/src/main.*java")
                .withTestPattern(".*/src/test/.*Test.java")
                .build();

        List<String> testClasses = zkProject.getTestFileLs();
        for (String testClass : testClasses) {
            System.out.println(testClass);
        }

        System.out.println("Test classes: " + testClasses.size());

//        for(String testClass : testClasses) {
//            // 获取文件名
//            String className = testClass.substring(testClass.lastIndexOf("/") + 1, testClass.lastIndexOf("."));
//            System.out.println("Test class: " + className);
//            Class<?> testClazz = Class.forName(className);
//            Result result = core.run(testClazz);
//            System.out.println("-----------------------------------");
//            System.out.println("Test class: " + className);
//            for (Failure failure : result.getFailures()) {
//                System.out.println(failure.toString());
//            }
//        }


        Result result = core.run(ZooKeeperTest.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("Tests successful: " + result.wasSuccessful());

    }



     class DetailedRunListener extends RunListener {
         // 在所有测试开始前调用
         public void testRunStarted(Description description) throws Exception {
             System.out.println("Number of tests to execute: " + description.testCount());
         }

         // 在所有测试结束后调用
         public void testRunFinished(Result result) throws Exception {
             System.out.println("Number of tests executed: " + result.getRunCount());
         }

         // 在每个测试开始前调用
         public void testStarted(Description description) throws Exception {
             System.out.println("Starting execution of test case: " + description.getMethodName());
         }

         // 在每个测试结束后调用
         public void testFinished(Description description) throws Exception {
             System.out.println("Finished execution of test case: " + description.getMethodName());
         }

         // 当测试失败时调用
         public void testFailure(Failure failure) throws Exception {
             System.out.println(failure);
             System.out.println("Execution of test case failed: " + failure.getMessage());
         }

         // 当测试被忽略时调用
         public void testIgnored(Description description) throws Exception {
             System.out.println("Execution of test case ignored: " + description.getMethodName());
         }

     }


}
