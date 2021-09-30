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
public class RunnableBehavior implements Runnable{
    private quorum.Libraries.Interface.Behaviors.Behavior_  action;
    RunnableBehavior(quorum.Libraries.Interface.Behaviors.Behavior_  act) {
        action = act;
    }
    /**
     * @return the action
     */
    public quorum.Libraries.Interface.Behaviors.Behavior_  getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(quorum.Libraries.Interface.Behaviors.Behavior_  action) {
        this.action = action;
    }

    @Override
    public void run() {
        if(action != null) {
            action.Run(null);
        }
    }
}
