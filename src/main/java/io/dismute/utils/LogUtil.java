package io.dismute.utils;

public class LogUtil {
    // 使用"="将内容包裹，内容居中，内容总长度为LOG_SEPARATOR_LENGTH
    public static String centerWithSeparator(String log) {
        StringBuilder sb = new StringBuilder();
        int left = (Constants.LOG_SEPARATOR_LENGTH - log.length()) / 2;
        int right = Constants.LOG_SEPARATOR_LENGTH - log.length() - left;
        for (int i = 0; i < left; i++) {
            sb.append("=");
        }
        sb.append(log);
        for (int i = 0; i < right; i++) {
            sb.append("=");
        }
        return sb.toString();
    }
}
