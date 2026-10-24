/*
 Copyright (c) 2013, Andreas Stefik and Matt Pedersen
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */
package org.debugger.jdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.Variable;
import org.debugger.VariableColumns;
import org.debugger.VariablesModel;
import org.debugger.Watch;
import org.debugger.filters.LanguageFilter;
import org.debugger.filters.QuorumFilter;
import org.debugger.jdi.views.JDIVariableView;

/**
 * This class gathers information from the system using JDI in regards to
 * variables.
 *
 * @author Andreas Stefik
 */
public class JDIVariablesModel implements VariablesModel {

    /**
     * This is the thread for which we are going to reference a stack frame.
     */
    private ThreadReference thread;

    /**
     * This is the debugger object which we are getting information from.
     */
    private JDIDebugger debugger;

    /**
     * Different programming languages use competing terminology. This class
     * translates variable information into language appropriate terms.
     */
    private JDIVariableView view;

    /**
     * A filter for finding out whether certain variables should be shown.
     */
    private LanguageFilter filter = new QuorumFilter();

    @Override
    public Variable[] getChildren(Variable node, int from, int to) {
        synchronized(debugger.getResumeMonitor()) {
            if (canReturnValues()) {
                if (node == null) { //the root note
                    try {
                        StackFrame frame = thread.frame(0);
                        List<LocalVariable> vars = frame.visibleVariables();
                        int size = vars.size();
                        Variable[] variables = new Variable[size + 1];

                        //get the "this" pointer
                        ObjectReference myThis = frame.thisObject();
                        if(myThis != null) {
                            JDIVariable me = new JDIVariable();
                            me.setField(true);
                            me.setName(view.getThis());
                            me.setTypeName(myThis.referenceType().name());
                            me.setValue(view.getValue(myThis));
                            view.convertTypeName(me);
                            variables[0] = me;

                            //get local variables that are both in scope in this frame
                            //and that exist at or before the current line number
                            //of execution.
                            Iterator<LocalVariable> iterator = vars.iterator();
                            int i = 1;
                            while (iterator.hasNext()) {
                                LocalVariable local = iterator.next();
                                JDIVariable var = convert(local, thread.frame(0));
                                variables[i] = var;
                                i++;
                            }
                            return variables;
                        } else {
                            return new Variable[0];
                        }

                    } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                        Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        //get the children of this variable
                        StackFrame frame = thread.frame(0);
                        //if the variable is the "this" pointer, do this.
                        if (node.getName().compareTo(view.getThis()) == 0) {
                            ObjectReference myThis = frame.thisObject();
                            return getFieldVariables(myThis);
                        } else if (node.getReference() != null) { //a child of a variable
                            if (node.getReference() instanceof ObjectReference) {
                                ObjectReference ref = (ObjectReference) node.getReference();
                                return getFieldVariables(ref);
                            }
                        } else if (node.getReference() == null) { //a local variable in scope
                            LocalVariable local = frame.visibleVariableByName(node.getName());
                            if (local != null) {
                                Value value = frame.getValue(local);
                                if (value instanceof ObjectReference) {
                                    return getFieldVariables((ObjectReference) value);
                                } else { //it isn't an object, so there are no children
                                    Variable[] vars = new Variable[0];
                                    return vars;
                                }
                            }
                        }
                    } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                        Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
        return null;
    }

    /**
     * This method returns a WatchResult object is the expression passed
     * in the Watch object was valid for this particular debugger
     * implementation.
     * 
     * @param watch
     * @return 
     */
    @Override
    public Variable getWatchResult(Watch watch) {
        synchronized(debugger.getResumeMonitor()) {
            try {
                String e = watch.getExpression();
                if(e.isEmpty()) {
                    return null;
                }
                StackFrame frame = thread.frame(0);
                LocalVariable local = frame.visibleVariableByName(e);
                if (local != null) {
                    JDIVariable var = convert(local, frame);
                    var.setWatchExpression(true);
                    return var;
                }
            } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    private boolean canReturnValues() {
        return debugger.isAlive() && thread != null && thread.isSuspended() && thread.status() != ThreadReference.THREAD_STATUS_ZOMBIE;
    }
    
    /**
     * This method converts a local variable to a JDIVariable implementation.
     *
     * @param local
     * @param frame
     * @return
     */
    private JDIVariable convert(LocalVariable local, StackFrame frame) {
        JDIVariable var = new JDIVariable();
        var.setName(local.name());
        var.setTypeName(local.typeName());
        Value value = null;
        try {
            value = frame.getValue(local);
            view.setThread(frame.thread());
            String viewed = view.getValue(value);
            var.setValue(viewed);
        } catch(Exception exception) {
            var.setValue(view.getNull()); //the variable may not be accessible
                                          //this seems to occur even if the
                                          //the stack frame says it is visible
            //Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, exception);
        }
        
        view.convertTypeName(var);
        return var;
    }
    
    private JDIVariable convert(Field field, Value reference) {
        JDIVariable var = new JDIVariable();
        var.setName(field.name());
        var.setTypeName(field.typeName());
        if(reference != null) {
            var.setValue(view.getValue(reference));
        } else {
            var.setValue(view.getNull());
        }
        var.setField(true);
        var.setObjectReference(reference);
        view.convertTypeName(var);
        return var;
    }
    
    

    /**
     * This method finds all children of a particular ObjectReference, for a
     * given StackFrame object.
     *
     * @param reference
     * @param frame
     * @return
     */
    private Variable[] getFieldVariables(ObjectReference reference) {
        ReferenceType type = reference.referenceType();
        if(view.isSpecialVariable(reference)) {
            view.setThread(thread);
            view.setMachine(getDebugger().getVirtualMachine());
            return view.getSpecialVariableChildren(reference);
        }
        List<Field> fields = type.allFields();
        int num = getNumberFields(reference);
        Variable[] variables = new Variable[num];

        Iterator<Field> it = fields.iterator();
        int i = 0;
        while (it.hasNext()) {
            Field field = it.next();
            if(filter.isVisibleField(field.name())) {
                boolean isParent = filter.isParentField(field.name());
                Value value = reference.getValue(field);
                JDIVariable var = convert(field, value);
                var.setParent(isParent);
                variables[i] = var;
                i++;
            }
        }
        return variables;
    }
    
    /**
     * This method returns the number of non-filtered fields that need to be
     * read.
     * 
     * @param reference
     * @return 
     */
    private int getNumberFields(ObjectReference reference) {
        ReferenceType type = reference.referenceType();
        List<Field> fields = type.allFields();
        Iterator<Field> it = fields.iterator();
        int i = 0;
        while (it.hasNext()) {
            Field field = it.next();
            if(filter.isVisibleField(field.name())) {
                i++;
            }
        }
        return i;
    }

    @Override
    public boolean isLeaf(Variable node) {
        synchronized(debugger.getResumeMonitor()) {
            if (node != null) {
                if (node.isPrimitive()) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int getChildrenCount(Variable node) {
        synchronized(debugger.getResumeMonitor()) {
            if (canReturnValues()) {
                if (node == null) { //the root note
                    try {
                        StackFrame frame = thread.frame(0);
                        List<LocalVariable> vars = frame.visibleVariables();
                        List<Value> arguments = frame.getArgumentValues();
                        return vars.size() + arguments.size() + 1; //don't forget "this"
                    } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                        Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    if (node.isPrimitive()) { //no kids if primitive
                        return 0;
                    } else {
                        try {
                            //otherwise return any of its values
                            StackFrame frame = thread.frame(0);
                            //in this case, we are referencing the "this" object.
                            if (node.getName().compareTo(view.getThis()) == 0) {
                                ObjectReference myThis = frame.thisObject();
                                return getNumberFields(myThis);
                            } else if (node.getReference() == null) { //we are referencing a local variable
                                LocalVariable local = frame.visibleVariableByName(node.getName());
                                if (local != null) {
                                    Value value = frame.getValue(local);
                                    if (value != null && value instanceof ObjectReference) {
                                        ObjectReference myThis = (ObjectReference) value;
                                        return getNumberFields(myThis);
                                    } else {
                                        return 0;
                                    }
                                }
                            } else if (node.getReference() != null) { //we are referencing a child variable not directly in scope.
                                Object value = node.getReference();
                                if (value != null && value instanceof ObjectReference) {
                                    ObjectReference myThis = (ObjectReference) value;
                                    return getNumberFields(myThis);
                                } else {
                                    return 0;
                                }
                            }
                        } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                            Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public String getDisplayName(Variable variable) {
        if (variable != null) {
            if (variable instanceof Variable) {
                Variable node = (Variable) variable;
                return node.getName();
            }
        }
        return "";
    }

    @Override
    public String getShortDescription(Variable node) {
        return "";
    }

    @Override
    public Object getValueAt(Variable node, VariableColumns column) {
        if (node == null) {
            return "";
        }
        switch (column) {
            case NAME:
                return node.getName();
            case TYPE:
                return node.getTypeName();
            case VALUE:
                return node.getValue();
        }
        return "";
    }

    @Override
    public boolean isReadOnly(Variable node, VariableColumns column) {
        return false;
    }

    @Override
    public void setValueAt(Variable node, VariableColumns column, Object value) {

    }

    /**
     * @return the thread
     */
    public ThreadReference getThread() {
        return thread;
    }

    /**
     * @param thread the thread to set
     */
    public void setThread(ThreadReference thread) {
        this.thread = thread;
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
        view = debugger.getView();
    }

}
