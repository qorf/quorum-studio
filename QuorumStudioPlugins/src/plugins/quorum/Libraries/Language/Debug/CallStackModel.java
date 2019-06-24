/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import org.debugger.StackFrame;
import org.debugger.jdi.JDIDebugger;
import quorum.Libraries.Language.Debug.StackFrame_;

/**
 *
 * @author stefika
 */
public class CallStackModel {
    public java.lang.Object me_ = null;
    private JDIDebugger debugger = null;
    org.debugger.CallStackModel callStackModel = null;
    StackFrame_[] currentFrames = null;
    
    public StackFrame_ Get(int i) {
        if(currentFrames == null) {
            return null;
        }
        return currentFrames[i];
    } 
    
    public int GetSize() {
        if(currentFrames == null) {
            return 0;
        }
        return currentFrames.length;
    }
    
    public void Update() {
        if(callStackModel == null) {
            return;
        }
        Object[] children = callStackModel.getChildren(null, 0, 0);
        if(children == null) {
            return;
        }
        currentFrames = new StackFrame_[children.length];
        for(int i = 0; i < children.length; i++) {
            StackFrame frame = (StackFrame) children[i];
            StackFrame_ quorumFrame = new quorum.Libraries.Language.Debug.StackFrame();
            quorumFrame.SetName(frame.getMethodName());
            quorumFrame.SetIsCurrent(frame.isCurrent());
            quorumFrame.SetLine(frame.getLine());
            quorumFrame.SetDotName(frame.getClassInformation().getDotName());
            currentFrames[i] = quorumFrame;
        }
    }
    
    /**
     * @return the debugger
     */
    public JDIDebugger getDebugger() {
        return debugger;
    }

    /**
     * @param debugger the debugger to set
     */
    public void setDebugger(JDIDebugger debugger) {
        this.debugger = debugger;
        callStackModel = debugger.getCallStackModel();
    }
}
