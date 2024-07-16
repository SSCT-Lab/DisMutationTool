package com.example.baseline.util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class TestClassExtract {

    public static void processFolder(List<String[]> fileDetails) {
        for (String[] detail : fileDetails) {
            if (detail.length != 2) {
                System.out.println("Each array must contain exactly two elements: output filename and HTML folder path.");
                continue;
            }
            String outputFileName = detail[0];
            String htmlFolderPath = detail[1];

            File folder = new File(htmlFolderPath);
            File[] listOfFiles = folder.listFiles();

            try {

                List<File> htmlFiles = Files.walk(Paths.get(htmlFolderPath))
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".html"))
                        .filter(path -> !path.getFileName().toString().contains("Test"))
                        .map(path -> path.toFile())
                        .collect(Collectors.toList());


                StringBuilder resultBuilder = new StringBuilder();
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".html") && !file.getName().contains("Test")) {
                        Document doc = Jsoup.parse(file, "UTF-8");
                        Elements elements = doc.select("html > body > section > div > table > tbody > tr > td:nth-child(3) > span");
                        List<String> elementTexts = elements.eachText();
                        resultBuilder.append(file.getName().split("\\.")[0])
                                .append(":\nElement count: ")
                                .append(elementTexts.size())
                                .append("\n")
                                .append(String.join("\n", elementTexts))
                                .append("\n\n");
                    }
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
                writer.write(resultBuilder.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Example usage:
        List<String[]> foldersToProcess = new ArrayList<>();
        foldersToProcess.add(new String[]{"zk-testlist.txt", "/Users/linzheyuan/zk-clover/clover/org/apache/zookeeper"});
//        model
//    foldersToProcess.add(new String[]{"output2.txt", "/path/to/html/folder2"});
        processFolder(foldersToProcess);
    }
}
