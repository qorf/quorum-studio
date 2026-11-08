/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author stefika
 */
public class ThreadRunner {
    public java.lang.Object me_ = null;
    ExecutorService executor = new ErrorReportingExecutor(1); //Executors.newSingleThreadExecutor();
    LinkedList<Future<?>> futures = new LinkedList<>();
    public synchronized void Add(quorum.Libraries.Interface.Behaviors.Behavior_ action) {
        RunnableBehavior act = new RunnableBehavior(action);
        Future<?> submit = executor.submit(act);
        futures.add(submit);
    }

    public synchronized void Empty() {
        try {
        Iterator<Future<?>> iterator = futures.iterator();
        while(iterator.hasNext()) {
            Future<?> future = iterator.next();
            boolean cancel = future.cancel(true);
        }}catch(Exception e) {}//ignore it, if you have to.
    }

    // True while a job is waiting or running. A cancelled job, or one that
    // ended with an error, counts as done, so this can't stay true (11b).
    // Finished jobs are dropped here; before, the list only grew.
    public synchronized boolean IsBusy() {
        futures.removeIf(Future::isDone);
        return !futures.isEmpty();
    }

    public void ShutDown() {
        executor.shutdown();
    }
}
