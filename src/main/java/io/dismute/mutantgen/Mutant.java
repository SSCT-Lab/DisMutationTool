package io.dismute.mutantgen;


import io.dismute.singleton.Project;
import io.dismute.utils.FileUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;

@Getter
@Setter
public class Mutant implements Serializable {
    private String originalName;
    private String mutatedName; // 原始文件名_算子名_突变体编号.java，唯一标识一个突变体
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
        String fileName = FileUtil.getNameWithoutExtension(originalPath) + ".java";
        this.originalName = fileName;
        this.mutatedName = FileUtil.getNameWithoutExtension(mutatedPath) + ".java";
        // update: 修复文件名重复问题，存放在project.originalCopyPath的文件名不再是原始文件名，而是mutatedName
        this.originalCopyPath = Project.getInstance().getOriginalPath() + File.separator + this.mutatedName;
        FileUtil.copyFileToTargetDir(originalPath, Project.getInstance().getOriginalPath(), mutatedName);
        // 如果文件被其他（同一文件）的突变体复制过，不复制
//        if(!new File((this.originalCopyPath)).exists()){
//            FileUtil.copyFileToTargetDir(originalPath, project.getOriginalPath(), fileName);
//        }
    }
}
