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
package org.debugger.jdi.views;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import org.debugger.Variable;
import org.debugger.VariableView;
import org.debugger.jdi.JDIVariable;

/**
 *
 * @author Andreas Stefik
 */
public abstract class JDIVariableView implements VariableView {
    private ThreadReference thread;
    private VirtualMachine machine;
    
    /**
     * This method examines the type of object on the system and returns 
     * whether or not it requires a special view in the Java Debugging Interface.
     * An example of an object that requires a special view would be arrays,
     * which are typically displayed with just its values, details of its
     * implementation aside.
     * 
     * @param reference
     * @return 
     */
    public abstract boolean isSpecialVariable(ObjectReference reference);
    
    /**
     * If this value is a special variable, return its special children. If not, 
     * return its fields.
     * 
     * @param reference
     * @param thread
     * @param machine
     * @return 
     */
    public abstract Variable[] getSpecialVariableChildren(ObjectReference reference);

    /**
     * This returns the special children from item from up to, but not
     * including, item to. A range of (0, 0) means all of them.
     *
     * @param reference
     * @param from
     * @param to
     * @return
     */
    public Variable[] getSpecialVariableChildren(ObjectReference reference, int from, int to) {
        return slice(getSpecialVariableChildren(reference), from, to);
    }

    /**
     * This returns how many special children a value has, such as the
     * number of items in an array.
     *
     * @param reference
     * @return
     */
    public int getSpecialVariableCount(ObjectReference reference) {
        return getSpecialVariableChildren(reference).length;
    }

    /**
     * This clamps a range to a count of children. A range of (0, 0) means
     * all of them. The result is the first child and the child after the
     * last.
     *
     * @param from
     * @param to
     * @param count
     * @return
     */
    public static int[] clampRange(int from, int to, int count) {
        if (from == 0 && to == 0) {
            return new int[] {0, count};
        }
        int start = Math.max(0, Math.min(from, count));
        int end = Math.max(start, Math.min(to, count));
        return new int[] {start, end};
    }

    /**
     * This returns part of an array of children. A range of (0, 0) means
     * all of them.
     *
     * @param all
     * @param from
     * @param to
     * @return
     */
    public static Variable[] slice(Variable[] all, int from, int to) {
        if (all == null || (from == 0 && to == 0)) {
            return all;
        }
        int[] range = clampRange(from, to, all.length);
        Variable[] part = new Variable[range[1] - range[0]];
        System.arraycopy(all, range[0], part, 0, part.length);
        return part;
    }
    
    /**
     * This method converts the type name to a view appropriate name.
     * 
     * @param variable 
     */
    public void convertTypeName(JDIVariable variable) {
        final String INT = "int";
        final String LONG = "long";
        final String FLOAT = "float";
        final String DOUBLE = "double";
        final String BOOLEAN = "boolean";
        final String TEXT = "java.lang.String";

        if (variable.getTypeName().compareTo(INT) == 0) {
            variable.setTypeName(this.getIntPrimitive());
            variable.setPrimitive(true);
        } else if (variable.getTypeName().compareTo(LONG) == 0) {
            variable.setTypeName(this.getLongPrimitive());
            variable.setPrimitive(true);
        } else if (variable.getTypeName().compareTo(FLOAT) == 0) {
            variable.setTypeName(this.getFloatPrimitive());
            variable.setPrimitive(true);
        } else if (variable.getTypeName().compareTo(DOUBLE) == 0) {
            variable.setTypeName(this.getDoublePrimitive());
            variable.setPrimitive(true);
        } else if (variable.getTypeName().compareTo(BOOLEAN) == 0) {
            variable.setTypeName(this.getBooleanPrimitive());
            variable.setPrimitive(true);
        } else if (variable.getTypeName().compareTo(TEXT) == 0) {
            variable.setTypeName(this.getString());
            variable.setPrimitive(true);
        } else {
            variable.setTypeName(this.getObjectName(variable.getTypeName()));
            variable.setName(this.getVariableName(variable.getName()));
            variable.setPrimitive(false);
        }
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
     * @return the machine
     */
    public VirtualMachine getMachine() {
        return machine;
    }

    /**
     * @param machine the machine to set
     */
    public void setMachine(VirtualMachine machine) {
        this.machine = machine;
    }
}
