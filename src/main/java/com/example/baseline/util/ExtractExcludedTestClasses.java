package com.example.baseline.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractExcludedTestClasses {
    public static void extract(String filePath){
        Pattern pattern = Pattern.compile("testClass=([^,]+)");
        Set<String> classNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    classNames.add(matcher.group(1));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from file: " + e.getMessage());
        }

        // 转换Set为List并排序
        List<String> sortedClassNames = new ArrayList<>(classNames);
        Collections.sort(sortedClassNames);

        // 转换List为字符串，使用逗号分隔
        String result = String.join("</param>\n<param>", sortedClassNames);
        System.out.println(result);
    }
    public static void main(String[] args) {
        String filePath = "src/main/resources/ExcludedClasses"; // 替换为实际文件路径
        ExtractExcludedTestClasses.extract(filePath);
    }
}

