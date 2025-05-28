package io.dismute.docker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

public class DockerApiClient {

    private static final Properties properties = new Properties();


    static {
        try (InputStream input = DockerApiClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("配置文件未找到！");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("加载配置文件失败", e);
        }
    }

    public static void main(String[] args) {

        String DOCKER_API_URL = properties.getProperty("docker.url")  +"/containers/json?all=true";

        System.out.println("Docker API URL: " + DOCKER_API_URL);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(DOCKER_API_URL)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Docker Response: " + response.body().string());
            } else {
                System.err.println("Request Failed: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
