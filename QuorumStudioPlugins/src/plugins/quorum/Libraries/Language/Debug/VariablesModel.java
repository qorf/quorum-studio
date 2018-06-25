/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import org.debugger.Variable;
import org.debugger.jdi.JDIDebugger;
import quorum.Libraries.Containers.Array;
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
    
    public Array_ GetChildren(Variable_ variable, int to, int from) {
        if(variablesModel != null) {
            
            
            Variable[] children = null;
            if(variable == null) {
               children = variablesModel.getChildren(null, to, from);
            } else {
                Variable v = ((quorum.Libraries.Language.Debug.Variable) variable).plugin_.getVariable();
                children = variablesModel.getChildren(v, to, from);
            }
            
            Array array = new Array();
            if(children != null) {
                for(int i = 0; i < children.length; i++) {
                    quorum.Libraries.Language.Debug.Variable var = new quorum.Libraries.Language.Debug.Variable();
                    var.plugin_.setVariable(children[i]);
                    array.Add(var);
                }
                return array;
            }
        }
        return null;
    }

    public boolean IsLeaf(Variable_ variable) {
        if(variablesModel != null) {
            Variable v = ((quorum.Libraries.Language.Debug.Variable) variable).plugin_.getVariable();
            return variablesModel.isLeaf(v);
        }
        return true;
    }

    public int GetChildrenCount(Variable_ variable) {
        if(variablesModel != null) {
            Variable v = ((quorum.Libraries.Language.Debug.Variable) variable).plugin_.getVariable();
            return variablesModel.getChildrenCount(v);
        }
        return 0;
    }

    public String GetDisplayName(Variable_ variable) {
        if(variablesModel != null) {
            Variable v = ((quorum.Libraries.Language.Debug.Variable) variable).plugin_.getVariable();
            return variablesModel.getDisplayName(v);
        }
        return "";
    }

    public Object_ GetValueAt(Variable_ variable, VariablesColumn_ vc) {
        if(variablesModel != null) {
            Variable v = ((quorum.Libraries.Language.Debug.Variable) variable).plugin_.getVariable();
            
            Object valueAt = variablesModel.getValueAt(v, GetVariablesColumn(vc));
            
        }
        return null;
    }
    
    private static org.debugger.VariableColumns GetVariablesColumn(VariablesColumn_ vc) {
        org.debugger.VariableColumns columns;
        
        if(vc.GetCurrent().compareTo(vc.Get_Libraries_Language_Debug_VariablesColumn__NAME_()) == 0) {
            columns = org.debugger.VariableColumns.NAME;
        } else if(vc.GetCurrent().compareTo(vc.Get_Libraries_Language_Debug_VariablesColumn__TYPE_()) == 0) {
            columns = org.debugger.VariableColumns.TYPE;
        } else if(vc.GetCurrent().compareTo(vc.Get_Libraries_Language_Debug_VariablesColumn__VALUE_()) == 0) {
            columns = org.debugger.VariableColumns.VALUE;
        } else {
            columns = org.debugger.VariableColumns.NAME;
        }
        return columns;
    }

    public boolean IsReadOnly(Variable_ variable, VariablesColumn_ vc) {
        if(variablesModel != null) {
            Variable v = ((quorum.Libraries.Language.Debug.Variable) variable).plugin_.getVariable();
            return variablesModel.isReadOnly(v, GetVariablesColumn(vc));
        }
        return true;
    }

    public void SetValueAt(Variable_ variable, VariablesColumn_ vc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object_ GetWatchResult(Watch_ watch) {
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
