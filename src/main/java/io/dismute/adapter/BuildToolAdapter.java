package io.dismute.adapter;

import io.dismute.mutantgen.Mutant;
import io.dismute.singleton.Project;
import io.dismute.utils.CommandLineUtil;
import io.dismute.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

public abstract class BuildToolAdapter {

    private static final Logger logger = LogManager.getLogger(BuildToolAdapter.class);

    // 生成classpath.txt文件，存储在输出目录下
    public abstract void cleanAndGenerateClassPath();

    //    void coverage();
    public abstract void clean();
    public abstract void compilation();
    public abstract void cleanAndCompilation();
    public abstract void testExecution(Mutant mutant);

    /**
     * 增量编译某个变异体对应的.java文件
     * 如果编译失败返回false
     * 如果编译成功，编译结果存放在Project.mutantBytecodePath目录中，文件夹形式，文件夹名为变异体名，返回true
     * @param mutant
     * @return
     */
    public boolean incrementalCompilation(Mutant mutant) {
        logger.info("IncrementalCompilation for mutant: {}", mutant.getMutatedName());
        Project project = Project.getInstance();
        String mutatedPath = mutant.getMutatedPath();
        String originalName = mutant.getOriginalName();

        // 在Project.mutantBytecodePath目录中创建以变异体名命名的文件夹
        String compilationBasePath = Paths.get(project.getMutantBytecodePath(), FileUtil.getNameWithoutExtension(mutant.getMutatedPath())).toString();
        FileUtil.createDirIfNotExist(compilationBasePath);

        // 将变异体的.java文件复制到该文件夹中，并改回originalName
        FileUtil.copyFileToTargetDir(mutatedPath, compilationBasePath, originalName);
        String targetJavaFilePath = Paths.get(compilationBasePath, originalName).toString(); // 接下来要编译的.java文件，内容为变异后的，文件名为原始文件名
        // 进入到在Project.mutantBytecodePath/<变异体名>目录下，对其中的变异体执行javac命令
        /*
        javac -d /Users/zhaodongchen/DismuteTestField/ \
        -cp "/Users/zhaodongchen/code/distributedSources/apache-zookeeper-3.5.8/zookeeper-server/target/classes:$(cat /Users/zhaodongchen/code/distributedSources/apache-zookeeper-3.5.8/zookeeper-server/classpath.txt)" \
        ServerCnxn.java
        */
        String classpath = FileUtil.readFileToString(project.getClasspathTxtPath());
        if(StringUtils.isEmpty(classpath)) {
            logger.error("Error while reading classpath.txt");
            throw new RuntimeException("Error while reading classpath.txt");
        }
        String fullClasspath = project.getBuildOutputPath() + ":" + classpath;

        int exitCode = CommandLineUtil.executeCommandAndRedirectOutputs(
                compilationBasePath,
                "javac",
                "-d", compilationBasePath,
                "-cp", fullClasspath,
                originalName
        );
        logger.info("Incremental compilation for mutant {} exited with code {}", mutant.getMutatedName(), exitCode);
        return exitCode == 0;
    }
}
