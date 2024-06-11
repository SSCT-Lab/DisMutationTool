package com.example.testRunner;

import java.io.IOException;

public abstract class TestSuiteRunner {
    public abstract int runTestSuite(String outputFilePath, String projectPath, String args) throws Exception;

    protected static long getProcessId(Process process) {
        try {
            // For UNIX-based systems
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                String pidField = process.getClass().getDeclaredField("pid").getName();
                if (pidField != null) {
                    process.getClass().getDeclaredField("pid").setAccessible(true);
                    return (long) process.getClass().getDeclaredField("pid").get(process);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    protected static boolean isProcessAlive(long pid) throws IOException {
        Process process = new ProcessBuilder("bash", "-c", "kill -0 " + pid).start();
        try {
            return process.waitFor() == 0;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
