package io.dismute.utils;

import org.junit.Test;

import java.util.List;

public class FileUtilTest {
    @Test
    public void testGetFilesBasedOnPattern() {
        List<String> jarFiles =  FileUtil.getFilesBasedOnPattern(System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1", ".*\\.jar");
        assert !jarFiles.isEmpty();
    }
}
