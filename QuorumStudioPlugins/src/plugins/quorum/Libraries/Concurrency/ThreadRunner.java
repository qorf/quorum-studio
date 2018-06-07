/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author stefika
 */
public class ThreadRunner {
    public java.lang.Object me_ = null;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public void Add(quorum.Libraries.Interface.Behaviors.Behavior_ action) {
        RunnableBehavior act = new RunnableBehavior(action);
        Future<?> submit = executor.submit(act);
    }
    
    public void ShutDown() {
        executor.shutdown();
    }
}
