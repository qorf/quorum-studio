/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

import org.eclipse.jgit.lib.TextProgressMonitor;

/**
 *
 * @author stefika
 */
public class QuorumProgressMonitor extends TextProgressMonitor{
     @Override
     protected  void 	onEndTask(String taskName, int workCurr) {
         
     }
     @Override
    protected  void 	onEndTask(String taskName, int cmp, int totalWork, int pcnt) {
        
    }
     @Override
    protected  void 	onUpdate(String taskName, int workCurr) {
        
    }
     @Override
    protected  void 	onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
        
    }
}
