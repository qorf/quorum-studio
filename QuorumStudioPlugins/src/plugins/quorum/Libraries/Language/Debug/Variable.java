/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import quorum.Libraries.Language.Object_;

/**
 *
 * @author stefika
 */
public class Variable {
    public java.lang.Object me_ = null;
    private org.debugger.Variable variable = null;
    
    public boolean IsField() {
        return variable.isField();
    }

    public boolean IsWatchExpression() {
        return variable.isWatchExpression();
    }

    public boolean IsPublic() {
        return variable.isPublic();
    }

    public boolean IsPrivate() {
        return variable.isPrivate();
    }

    public String GetName() {
        return variable.getName();
    }

    public String GetTypeName() {
        return variable.getTypeName();
    }

    public String GetValue() {
        return variable.getValue();
    }

    public boolean IsPrimitive() {
        return variable.isPrimitive();
    }

    public boolean IsParent() {
        return variable.isParent();
    }

    public Object_ GetReference() {
        Object reference = variable.getReference();
        if(reference instanceof Object_) {
            return (Object_) reference;
        }
        return null;
    }

    /**
     * @return the variable
     */
    public org.debugger.Variable getVariable() {
        return variable;
    }

    /**
     * @param variable the variable to set
     */
    public void setVariable(org.debugger.Variable variable) {
        this.variable = variable;
    }
}
