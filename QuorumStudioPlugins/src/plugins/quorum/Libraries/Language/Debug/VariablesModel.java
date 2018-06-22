/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import org.debugger.jdi.JDIDebugger;
import quorum.Libraries.Containers.Array_;
import quorum.Libraries.Language.Debug.Variable_;
import quorum.Libraries.Language.Debug.VariablesColumn_;
import quorum.Libraries.Language.Debug.Watch_;
import quorum.Libraries.Language.Object_;

/**
 *
 * @author stefika
 */
public class VariablesModel {
    public java.lang.Object me_ = null;
    private JDIDebugger debugger = null;
    org.debugger.VariablesModel variablesModel = null;
    
    public Array_ GetChildren(Variable_ vrbl_, int to, int from) {
        return null;
    }

    public void IsLeaf(Variable_ vrbl_) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void GetChildrenCount(Variable_ vrbl_) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String GetDisplayName(Variable_ vrbl_) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void GetValueAt(Variable_ vrbl_, VariablesColumn_ vc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void IsReadOnly(Variable_ vrbl_, VariablesColumn_ vc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void SetValueAt(Variable_ vrbl_, VariablesColumn_ vc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object_ GetWatchResult(Watch_ watch_) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        variablesModel = debugger.getVariablesModel();
    }
}
