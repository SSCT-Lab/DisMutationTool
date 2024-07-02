package com.example.mutator.concurrency;

import com.example.mutator.DiscardExceptionOperator;
import com.example.mutator.MutatorType;

public class RCE extends DiscardExceptionOperator {
    public RCE() {

        exceptions.add("java.lang.IllegalThreadStateException");
        exceptions.add("java.lang.IllegalMonitorStateException");
        exceptions.add("java.lang.InterruptedException");
        exceptions.add("java.util.concurrent.TimeoutException");
        exceptions.add("java.util.concurrent.BrokenBarrierException");
        exceptions.add("java.util.concurrent.CancellationException");
        exceptions.add("java.util.concurrent.ExecutionException");
        exceptions.add("java.util.concurrent.RejectedExecutionException");
        exceptions.add("java.util.concurrent.ConcurrentModificationException");

        mutator = MutatorType.RCE;
        targetException = "java.lang.Exception";
    }
}
