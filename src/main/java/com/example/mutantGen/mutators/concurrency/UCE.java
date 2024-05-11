package com.example.mutantGen.mutators.concurrency;

import com.example.mutantGen.Mutant;
import com.example.mutantGen.MutantGen;
import com.example.mutantGen.MutatorType;
import com.example.mutantGen.mutators.UpcastExceptionOperator;
import com.example.utils.FileUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UCE extends UpcastExceptionOperator {

    static {
        exceptions.add("java.lang.IllegalThreadStateException");
        exceptions.add("java.lang.IllegalMonitorStateException");
        exceptions.add("java.lang.InterruptedException");
        exceptions.add("java.util.concurrent.TimeoutException");
        exceptions.add("java.util.concurrent.BrokenBarrierException");
        exceptions.add("java.util.concurrent.CancellationException");
        exceptions.add("java.util.concurrent.ExecutionException");
        exceptions.add("java.util.concurrent.RejectedExecutionException");
        exceptions.add("java.util.concurrent.ConcurrentModificationException");

        mutator = MutatorType.UCE;
        targetException = "java.lang.Exception";
    }
}
