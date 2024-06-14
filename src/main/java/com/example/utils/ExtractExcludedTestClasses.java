package com.example.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractExcludedTestClasses {
    public static void main(String[] args) {
        String filePath = "path/to/your/input.txt"; // 替换为实际文件路径
        Pattern pattern = Pattern.compile("testClass=([^,]+)");
        StringBuilder result = new StringBuilder();
        boolean first = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    if (!first) {
                        result.append(",");
                    } else {
                        first = false;
                    }
                    result.append(matcher.group(1));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from file: " + e.getMessage());
        }

        System.out.println(result.toString());
    }
}