/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.TextProgressMonitor;
import quorum.Libraries.Development.Versioning.VersionProgressMonitor_;

/**
 *
 * @author stefika
 */
public class QuorumProgressMonitor implements ProgressMonitor{
    private VersionProgressMonitor_ monitor = null;
    
    @Override
    public void start(int i) {
        monitor.Start(i);
    }

    @Override
    public void beginTask(String string, int i) {
        monitor.BeginTask(string, i);
    }

    @Override
    public void update(int i) {
        monitor.Update(i);
    }

    @Override
    public void endTask() {
        monitor.EndTask();
    }

    @Override
    public boolean isCancelled() {
        return monitor.IsCancelled();
    }

    /**
     * @return the monitor
     */
    public VersionProgressMonitor_ getMonitor() {
        return monitor;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(VersionProgressMonitor_ monitor) {
        this.monitor = monitor;
    }
}
