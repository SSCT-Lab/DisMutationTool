package com.example.mutator;

import com.example.utils.Config;
import com.example.utils.FileUtil;

import java.io.File;

public class Mutant {
    private int lineNo;
    private MutatorType mutatorType;
    private String originalPath;
    private String originalCopyPath; // 复制到当前选股original文件夹下
    private String mutatedPath;

    public Mutant(int lineNo, MutatorType mutatorType, String originalPath, String mutatedPath) {
        this.lineNo = lineNo;
        this.mutatorType = mutatorType;
        this.originalPath = originalPath;
        this.mutatedPath = mutatedPath;
        String fileName = FileUtil.getFileName(originalPath) + ".java";
        this.originalCopyPath = Config.ORIGINAL_PATH + File.separator + fileName;
        // 如果文件被其他（同一文件）的突变体复制过，不复制
        if(!new File((this.originalCopyPath)).exists()){
            FileUtil.copyFileToTargetDir(originalPath, Config.ORIGINAL_PATH, fileName);
        }
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public MutatorType getMutatorType() {
        return mutatorType;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getMutatedPath() {
        return mutatedPath;
    }

    public String getOriginalCopyPath() {
        return originalCopyPath;
    }
}
