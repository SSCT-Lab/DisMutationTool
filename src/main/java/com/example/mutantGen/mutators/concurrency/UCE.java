package com.example.mutantGen.mutators.concurrency;

import com.example.mutantGen.MutatorType;
import com.example.mutantGen.mutators.UpcastExceptionOperator;

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
