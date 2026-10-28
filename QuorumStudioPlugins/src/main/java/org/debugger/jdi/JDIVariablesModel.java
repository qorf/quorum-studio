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
import java.util.ArrayList;
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
                try {
                    if (node == null) { //the root node
                        return getRootVariables();
                    }
                    ObjectReference reference = resolve(node);
                    if (reference == null) { //it isn't an object, so there are no children
                        return new Variable[0];
                    }
                    return getFieldVariables(reference, from, to);
                } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                    Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RuntimeException ex) {
                    //the program moved on, or the object was collected
                    Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }
        return null;
    }

    /**
     * This returns how many children a variable has: the items of a
     * collection, such as an array's size, or the fields of any other
     * object. It's 0 for a primitive or undefined value, or if the program
     * isn't stopped.
     *
     * @param node
     * @return
     */
    public int getElementCount(Variable node) {
        synchronized(debugger.getResumeMonitor()) {
            if (canReturnValues() && node != null && !node.isPrimitive()) {
                try {
                    ObjectReference reference = resolve(node);
                    if (reference == null) {
                        return 0;
                    }
                    if (view.isSpecialVariable(reference)) {
                        view.setThread(thread);
                        view.setMachine(getDebugger().getVirtualMachine());
                        return view.getSpecialVariableCount(reference);
                    }
                    return getNumberFields(reference);
                } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                    Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RuntimeException ex) {
                    //the program moved on, or the object was collected
                    Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }
        return 0;
    }

    /**
     * This returns "me" and the local variables of the current frame.
     *
     * @return
     */
    private Variable[] getRootVariables() throws IncompatibleThreadStateException, AbsentInformationException {
        StackFrame frame = thread.frame(0);
        view.setThread(thread);
        view.setMachine(getDebugger().getVirtualMachine());
        List<Variable> variables = new ArrayList<>();

        //get the "this" pointer
        ObjectReference myThis = frame.thisObject();
        if(myThis != null) {
            JDIVariable me = new JDIVariable();
            me.setField(true);
            me.setName(view.getThis());
            me.setTypeName(myThis.referenceType().name());
            me.setValue(view.getValue(myThis));
            me.setObjectReference(myThis);
            view.convertTypeName(me);
            variables.add(me);
        }

        //get local variables that are both in scope in this frame
        //and that exist at or before the current line number
        //of execution.
        for (LocalVariable local : frame.visibleVariables()) {
            variables.add(convert(local, frame));
        }
        return variables.toArray(new Variable[0]);
    }

    /**
     * This returns the object a variable holds, or null if it holds a
     * primitive or undefined. Variables keep the value they were read with,
     * so their children come from the same object even after the program
     * has moved on to other frames. The name is only looked up for a
     * variable without one.
     *
     * @param node
     * @return
     */
    private ObjectReference resolve(Variable node) throws IncompatibleThreadStateException, AbsentInformationException {
        Object reference = node.getReference();
        if (reference instanceof ObjectReference) {
            return (ObjectReference) reference;
        } else if (reference != null) {
            return null;
        }

        StackFrame frame = thread.frame(0);
        if (node.getName().compareTo(view.getThis()) == 0) {
            return frame.thisObject();
        }
        LocalVariable local = frame.visibleVariableByName(node.getName());
        if (local != null) {
            Value value = frame.getValue(local);
            if (value instanceof ObjectReference) {
                return (ObjectReference) value;
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
            var.setObjectReference(value);
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
    private Variable[] getFieldVariables(ObjectReference reference, int from, int to) {
        ReferenceType type = reference.referenceType();
        view.setThread(thread);
        view.setMachine(getDebugger().getVirtualMachine());
        if(view.isSpecialVariable(reference)) {
            return view.getSpecialVariableChildren(reference, from, to);
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
        return JDIVariableView.slice(variables, from, to);
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
        if (node == null) { //the root node
            synchronized(debugger.getResumeMonitor()) {
                Variable[] root = getChildren(null, 0, 0);
                return root == null ? 0 : root.length;
            }
        }
        return getElementCount(node);
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
