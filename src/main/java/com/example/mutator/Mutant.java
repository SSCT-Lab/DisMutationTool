package com.example.mutator;

import com.example.utils.Config;
import com.example.utils.FileUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class Mutant {
    private int lineNo;
    private MutatorType mutatorType;
    private String originalPath;
    private String originalCopyPath; // 复制到当前选股original文件夹下
    private String mutatedPath;
//    private String originalBytecodePath;
//    private String mutatedBytecodePath;

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
}
