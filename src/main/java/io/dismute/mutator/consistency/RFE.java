package io.dismute.mutator.consistency;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import io.dismute.mutantgen.MutatorType;
import io.dismute.mutator.DiscardExceptionOperator;

public class RFE extends DiscardExceptionOperator {
    public RFE() {
        exceptions.add("java.io.FileNotFoundException");
        exceptions.add("java.io.IOException");
        exceptions.add("java.lang.SecurityException");
        exceptions.add("java.nio.file.AccessDeniedException");
        exceptions.add("java.nio.file.NoSuchFileException");
        exceptions.add("java.nio.file.FileAlreadyExistsException");
        exceptions.add("java.nio.file.InvalidPathException");
        exceptions.add("java.nio.file.FileSystemException");

        mutator = MutatorType.RFE;

        targetException = "java.lang.Exception";
    }

    @Override
    protected boolean continueProcess(CompilationUnit cu) {
//        ArrayList<String> importKeywords = new ArrayList<>();
//        importKeywords.add("org.apache.cassandra.config");
//        importKeywords.add("org.apache.hadoop.conf");
//        importKeywords.add("org.apache.kafka.common.config");
//        importKeywords.add("org.apache.rocketmq.proxy.config");
//        importKeywords.add("org.apache.rocketmq.store.config");
//        importKeywords.add("org.apache.zookeeper.server.quorum.QuorumPeerConfig");
        // (import|package).*conf.*

        // 验证package是否有conf关键字
        if (cu.getPackageDeclaration().isPresent()) {
            String packageName = cu.getPackageDeclaration().get().getNameAsString();
            if(!packageName.contains("test") && (packageName.contains(".conf") || packageName.contains(".config"))){
                return true;
            }
        }

        // 验证是否import了conf
        for (ImportDeclaration importDeclaration : cu.findAll(ImportDeclaration.class)) {
            String importName = importDeclaration.getNameAsString();
            if(!importName.contains("test") && (importName.contains(".conf") || importName.contains(".config"))){
                return true;
            }
        }

        return false;

//        // 验证当前.java文件的package是否包含importKeywords中的任何一个关键字
//        if (cu.getPackageDeclaration().isPresent()) {
//            for (String keyword : importKeywords) {
//                if(keyword.contains("test")) continue;
//                if (cu.getPackageDeclaration().get().getNameAsString().contains(keyword)) {
//                    res = true;
//                    break;
//                }
//            }
//        }
//        // 验证当前.java文件的任何一条import语句是否包含importKeywords中的任何一个关键字
//        for (ImportDeclaration importDeclaration : cu.findAll(ImportDeclaration.class)) {
//            if (res) {
//                break;
//            }
//            for (String keyword : importKeywords) {
//                if (importDeclaration.getNameAsString().contains(keyword)) {
//                    res = true;
//                    break;
//                }
//            }
//        }
//        return res;
    }
}
