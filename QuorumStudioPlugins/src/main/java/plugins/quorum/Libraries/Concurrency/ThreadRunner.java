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
    public void Add(quorum.Libraries.Interface.Behaviors.Behavior_ action) {
        RunnableBehavior act = new RunnableBehavior(action);
        Future<?> submit = executor.submit(act);
        futures.add(submit);
    }
    
    public void Empty() {
        try {
        Iterator<Future<?>> iterator = futures.iterator(); 
        while(iterator.hasNext()) {
            Future<?> future = iterator.next();
            boolean cancel = future.cancel(true);
        }}catch(Exception e) {}//ignore it, if you have to.
    }
    public void ShutDown() {
        executor.shutdown();
    }
}
