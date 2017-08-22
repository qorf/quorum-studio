/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

/**
 *
 * @author stefika
 */
public class RunnableAction implements Runnable{
    private quorum.Libraries.Interface.Action_ action;
    RunnableAction(quorum.Libraries.Interface.Action_ act) {
        action = act;
    }
    /**
     * @return the action
     */
    public quorum.Libraries.Interface.Action_ getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(quorum.Libraries.Interface.Action_ action) {
        this.action = action;
    }

    @Override
    public void run() {
        if(action != null) {
            action.Do();
        }
    }
}
