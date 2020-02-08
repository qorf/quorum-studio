/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import quorum.Libraries.Language.Errors.Error;

/**
 *
 * @author alleew
 */
public class ErrorReportingExecutor extends ScheduledThreadPoolExecutor {

    public ErrorReportingExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Object result = ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null) {
            StackTraceElement[] array = t.getStackTrace();
            
            if(t instanceof Error) {
                Error error = (Error) t;
                String message = error.GetErrorMessage();
                System.out.println(message);
            }
            else {
                System.out.println(t.getMessage());
            }
            
            for(int i = 0; i < array.length; i++) {
                System.out.println(array[i]);
            }
        }
    }
}
