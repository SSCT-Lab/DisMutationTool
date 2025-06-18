package io.dismute.mutator.concurrency;


import io.dismute.mutantgen.MutatorType;
import io.dismute.mutator.DiscardExceptionOperator;

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
