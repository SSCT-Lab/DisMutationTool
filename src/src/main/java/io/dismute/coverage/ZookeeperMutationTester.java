package io.dismute.coverage;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperMutationTester {
    public static void main(String[] args) throws Exception {
        // 定义 Maven 编译输出的路径
//        String classesDir = "target/classes"; // 源码编译后的目录
//        String testClassesDir = "target/test-classes"; // 测试代码编译后的目录
//        String dependenciesDir = "target/dependency"; // 依赖的目录
        String classesDir = "/home/zdc/Desktop/apache-zookeeper-3.5.8/zookeeper-server/target/classes"; // 源码编译后的目录
        String testClassesDir = "/home/zdc/Desktop/apache-zookeeper-3.5.8/zookeeper-server/target/test-classes"; // 测试代码编译后的目录
        String dependenciesDir = "/home/zdc/Desktop/apache-zookeeper-3.5.8/zookeeper-server/target/dependency"; // 依赖的目录

        // 加载编译输出和依赖到 ClassLoader
        URLClassLoader classLoader = createClassLoader(classesDir, testClassesDir, dependenciesDir);
        Thread.currentThread().setContextClassLoader(classLoader);

        // 运行测试用例
        runTests(testClassesDir, classLoader);
    }

    private static URLClassLoader createClassLoader(String classesDir, String testClassesDir, String dependenciesDir) throws Exception {
        List<URL> urls = new ArrayList<>();
        urls.add(new File(classesDir).toURI().toURL());
        urls.add(new File(testClassesDir).toURI().toURL());

        // 添加所有依赖的 JAR 包
        File dependencyDir = new File(dependenciesDir);
        File[] files = dependencyDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                urls.add(file.toURI().toURL());
            }
        }

        return new URLClassLoader(urls.toArray(new URL[0]));
    }

    private static void runTests(String testClassesDir, ClassLoader classLoader) throws Exception {
        File testDir = new File(testClassesDir);
        List<Class<?>> testClasses = new ArrayList<>();

        // 遍历所有测试类
        findTestClasses(testDir, "", testClasses, classLoader);



        // 统计测试用例数量
        int totalTestCases = 0;
        for (Class<?> testClass : testClasses) {
            int testCases = countTestCases(testClass);
            System.out.println("Found " + testCases + " test cases in: " + testClass.getName());
            totalTestCases += testCases;
        }

        System.out.println("Found " + testClasses.size() + " test classes");


        System.out.println("Found " + totalTestCases + " total test cases");

//        testClasses = testClasses.subList(0, 20);


//        System.exit(0);
//
        // 使用 JUnitCore 运行测试用例
//        for (Class<?> testClass : testClasses) {
//            System.out.println("Running tests in: " + testClass.getName());
//            Result result = JUnitCore.runClasses(testClass);
//
//            // 输出测试结果
//            for (Failure failure : result.getFailures()) {
//                System.err.println("Test failed: " + failure.toString());
//            }
//            System.out.println("Test successful: " + result.wasSuccessful());
//        }


        File resultsDir = new File("results");
        if (!resultsDir.exists() && !resultsDir.mkdirs()) {
            System.err.println("Failed to create results directory.");
            return;
        }

        for (Class<?> testClass : testClasses) {
            // 获取当前测试类的全限定名
            String testClassName = testClass.getName();
            System.out.println("Running tests in: " + testClassName);


            // 创建结果文件
            File resultFile = new File(resultsDir, testClassName + ".txt");
            try (FileOutputStream fos = new FileOutputStream(resultFile);
                 PrintStream ps = new PrintStream(fos)) {

                // 重定向输出
                PrintStream originalOut = System.out;
                PrintStream originalErr = System.err;
                System.setOut(ps);
                System.setErr(ps);

                // 运行测试
                Result result = JUnitCore.runClasses(testClass);

                // 输出测试结果
                System.out.println("Running tests in: " + testClassName);
                for (Failure failure : result.getFailures()) {
                    System.err.println("Test failed: " + failure.toString());
                }
                System.out.println("Test successful: " + result.wasSuccessful());

                // 恢复标准输出
                System.setOut(originalOut);
                System.setErr(originalErr);

                System.out.println("Test results written to: " + resultFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error writing results for " + testClassName + ": " + e.getMessage());
            }
        }
    }

    private static void findTestClasses(File dir, String packageName, List<Class<?>> testClasses, ClassLoader classLoader) throws Exception {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                findTestClasses(file, packageName + file.getName() + ".", testClasses, classLoader);
            } else if (file.getName().endsWith(".class") && file.getName().contains("Test")) {
                String className = packageName + file.getName().replace(".class", "");
                if (!className.equals(className.split("\\$")[0])) continue; // Skip inner classes
                if(!className.endsWith("Test")) continue; // Skip classes that don't end with Test
                Class<?> clazz = Class.forName(className, true, classLoader);

                // 检查类是否为 public 且是有效的测试类
                if (isPublicTestClass(clazz)) {
                    testClasses.add(clazz);
                }
            }
        }
    }

    // 判断类是否是 public 的测试类
    private static boolean isPublicTestClass(Class<?> clazz) {
        return clazz.getModifiers() == java.lang.reflect.Modifier.PUBLIC &&
                clazz.getConstructors().length == 1 &&
                clazz.getConstructors()[0].getParameterCount() == 0;
    }


    // 统计一个测试类中的测试用例数量
    public static int countTestCases(Class<?> testClass) {
        int count = 0;

        // 遍历类中所有的方法
        for (Method method : testClass.getDeclaredMethods()) {
            // 检查是否带有 JUnit 4 的 @Test 注解
            if (method.isAnnotationPresent(org.junit.Test.class)) {
                count++;
            }

//            // 检查是否带有 JUnit 5 的 @Test 注解
//            if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
//                count++;
//            }
        }

        return count;
    }
}
