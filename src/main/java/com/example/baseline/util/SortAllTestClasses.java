package com.example.baseline.util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SortAllTestClasses {
    public static void sort(String filePath){
        Set<String> uniqueWords = new HashSet<>(); // 使用HashSet来存储去重后的单词
        Set<String> allWords = new HashSet<>(); // 使用HashSet来存储所有读取的单词

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 分割字符串并去除空格
                String[] words = line.split(",");
                for (String word : words) {
                    allWords.add(word.trim());
                    if (uniqueWords.add(word.trim())) {
                        // 如果uniqueWords中没有这个单词，add方法返回true
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 计算重复的单词数量
        int duplicatesCount = allWords.size() - uniqueWords.size();

        // 将排序后的集合转换为逗号分隔的字符串
        List<String> sortedUniqueWords = new ArrayList<>(uniqueWords);
        Collections.sort(sortedUniqueWords);
        String sortedUniqueWordsString = String.join(",", sortedUniqueWords);

        if (duplicatesCount > 0) {
            System.out.println("Removed " + duplicatesCount + " duplicates.");
        } else {
            System.out.println("NO Duplicate");
        }
        System.out.println(sortedUniqueWordsString);
    }
    public static void main(String[] args) {
        // 假设文件路径已经正确设置
        String filePath = "src/main/resources/ExcludedClasses"; // 这里需要替换成实际的文件路径
        SortAllTestClasses.sort(filePath);


    }
}

