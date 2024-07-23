package com.example.baseline.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MutationTestAnalysis {

    public static void main(String[] args) {
        String filePath = "src/main/resources/pit-skywalking-final"; // 替换为你的文件路径

        try {
            String input = readFile(filePath);
            analyzeMutationTest(input);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void analyzeMutationTest(String input) {
        Map<String, MutationStats> stats = new HashMap<>();
        List<String> originalSentences = new ArrayList<>();

        // 要匹配的特定算子
        String[] mutators = {
                "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
                "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator",
                "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
                "org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator",
                "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
                "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator",

        };

        // 初始化统计信息
        for (String mutator : mutators) {
            stats.put(mutator, new MutationStats());
        }

        // 正则表达式模式
        Pattern mutatorPattern = Pattern.compile("> (org\\.pitest\\.mutationtest\\.engine\\.gregor\\.mutators\\.[^\\s]+)");
        Pattern statsPattern = Pattern.compile(">> Generated (\\d+) Killed (\\d+)");

        Matcher mutatorMatcher = mutatorPattern.matcher(input);
        Matcher statsMatcher = statsPattern.matcher(input);

        String currentMutator = null;

        // 迭代输入以找到匹配项
        while (mutatorMatcher.find()) {
            currentMutator = mutatorMatcher.group(1);

            // 仅处理特定的算子
            if (stats.containsKey(currentMutator)) {
                // 检查算子行后是否有统计行
                if (statsMatcher.find(mutatorMatcher.end())) {
                    int generated = Integer.parseInt(statsMatcher.group(1));
                    int killed = Integer.parseInt(statsMatcher.group(2));

                    // 更新统计信息
                    MutationStats mutatorStats = stats.get(currentMutator);
                    mutatorStats.generated += generated;
                    mutatorStats.killed += killed;

                    // 保存原始语句
                    originalSentences.add(statsMatcher.group(0));
                }
            }
        }

        // 打印结果
        for (Map.Entry<String, MutationStats> entry : stats.entrySet()) {
            System.out.println("Mutator: " + entry.getKey());
            System.out.println("Generated: " + entry.getValue().generated);
            int pass=entry.getValue().generated-entry.getValue().killed;
            System.out.println("Passed: " + pass);
            System.out.println();
        }

        // 输出 generated 大于 500 的原始句子
        System.out.println("Original sentences with Generated > 500:");
        for (String sentence : originalSentences) {
            Matcher matcher = statsPattern.matcher(sentence);
            if (matcher.find()) {
                int generated = Integer.parseInt(matcher.group(1));
                if (generated > 500) {
                    System.out.println(sentence);
                }
            }
        }

        System.out.println("Original sentences with Killed = 0:");
        for (String sentence : originalSentences) {
            Matcher matcher = statsPattern.matcher(sentence);
            if (matcher.find()) {
                int killed = Integer.parseInt(matcher.group(2));
                int generated = Integer.parseInt(matcher.group(1));
                if (killed/generated<0.1) {
                    System.out.println(sentence);
                }
            }
        }

    }

    // 辅助类用于存储变异测试统计信息
    static class MutationStats {
        int generated = 0;
        int killed = 0;
    }
}