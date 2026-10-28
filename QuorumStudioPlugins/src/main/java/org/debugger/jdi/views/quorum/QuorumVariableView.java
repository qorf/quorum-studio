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
package org.debugger.jdi.views.quorum;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.debugger.Variable;
import org.debugger.jdi.views.JDIVariableView;
import org.debugger.jdi.views.QuorumView;

/**
 *
 * @author Andreas Stefik
 */
public abstract class QuorumVariableView {
    private QuorumView quorumView;
    private ThreadReference thread;
    private VirtualMachine machine;

    /**
     * Methods looked up by type and name, so each is found only once. They
     * belong to one running program, so they're forgotten when it changes.
     */
    private final Map<ReferenceType, Map<String, Method>> methods = new HashMap<>();
    
    /**
     * Returns the full name of the class in question.
     * 
     * @return 
     */
    public abstract String getStaticKey();
    
    /**
     * This method returns variables for children.
     * 
     * @param reference
     * @return 
     */
    public abstract Variable[] getSpecialVariableChildren(ObjectReference reference);

    /**
     * This returns the children from item from up to, but not including,
     * item to. A range of (0, 0) means all of them. Views of large
     * collections read only the range they're asked for.
     *
     * @param reference
     * @param from
     * @param to
     * @return
     */
    public Variable[] getSpecialVariableChildren(ObjectReference reference, int from, int to) {
        return JDIVariableView.slice(getSpecialVariableChildren(reference), from, to);
    }

    /**
     * This returns how many children a value has, such as the items in a
     * collection.
     *
     * @param reference
     * @return
     */
    public int getElementCount(ObjectReference reference) {
        return getSpecialVariableChildren(reference).length;
    }

    /**
     * This calls a method in the running program and returns its result.
     * Only the stopped thread runs during the call, and the method is looked
     * up once per type.
     *
     * @param object
     * @param name
     * @param args
     * @return
     */
    protected Value invoke(ObjectReference object, String name, List<? extends Value> args)
            throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        ReferenceType type = object.referenceType();
        Map<String, Method> byName = methods.get(type);
        if (byName == null) {
            byName = new HashMap<>();
            methods.put(type, byName);
        }
        Method method = byName.get(name);
        if (method == null) {
            List<Method> found = type.methodsByName(name);
            if (found.isEmpty()) {
                return null;
            }
            method = found.get(0);
            byName.put(name, method);
        }
        return object.invokeMethod(getThread(), method, args, ObjectReference.INVOKE_SINGLE_THREADED);
    }

    /**
     * @return the quorumView
     */
    public QuorumView getQuorumView() {
        return quorumView;
    }

    /**
     * @param quorumView the quorumView to set
     */
    public void setQuorumView(QuorumView quorumView) {
        this.quorumView = quorumView;
    }
    
    /**
     * This method returns the name of the type that should be displayed to the
     * user in the debugger.
     * 
     * @return 
     */
    public abstract String getObjectName();
    
    /**
     * This method returns a 
     * @param value
     * @return 
     */
    public String getValue(Value value) {
        if(value instanceof ObjectReference) {
            ObjectReference ref = (ObjectReference) value;
            String name = ref.referenceType().name();
            if(name.compareTo("java.lang.String")==0) {
                return QuorumView.toDisplay(value);
            }
            return "#" + ref.hashCode();
        } else if(value == null) {
            return getQuorumView().getNull();
        } else {
            return value.toString();
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
        if (this.machine != machine) {
            methods.clear();
        }
        this.machine = machine;
    }
}
