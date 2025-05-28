package io.dismute.singleton;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件工具类
 */
public class PropertiesFile {
    // 饿汉式单例，类加载时初始化
    @Getter
    private static final PropertiesFile instance = new PropertiesFile();
    private final Properties properties = new Properties();

    private PropertiesFile() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Could not find the configuration file!");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Configuration file load failed", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
