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

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.Variable;
import org.debugger.jdi.JDIVariable;
import org.debugger.jdi.views.JDIVariableView;

/**
 * This is a view for list containers. A list is a chain of nodes, so its
 * items are read by following the nodes' fields, without calling into the
 * running program. Calls are a fallback for a list without those fields.
 * 
 * @author Andreas Stefik
 */
public class ListView extends QuorumVariableView {
    public final String key = "quorum.Libraries.Containers.List";
    private static final String HEAD = "head";
    private static final String NEXT = "next";
    private static final String VALUE = "value";
    private static final String SIZE = "size";
    
    @Override
    public String getStaticKey() {
        return key;
    }
    
    @Override
    public String getObjectName() {
        return "Libraries.Containers.List";
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        return getSpecialVariableChildren(reference, 0, 0);
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference, int from, int to) {
        int[] range = JDIVariableView.clampRange(from, to, getElementCount(reference));
        int start = range[0];
        int end = range[1];
        List<Variable> variables = new ArrayList<>();
        Field head = reference.referenceType().fieldByName(HEAD);
        if (head == null) {
            return iterate(reference, start, end);
        }

        Value node = reference.getValue(head);
        Field next = null;
        Field value = null;
        int i = 0;
        while (node instanceof ObjectReference && i < end) {
            ObjectReference current = (ObjectReference) node;
            if (next == null) {
                next = current.referenceType().fieldByName(NEXT);
                value = current.referenceType().fieldByName(VALUE);
                if (next == null || value == null) {
                    return iterate(reference, start, end);
                }
            }
            if (i >= start) {
                JDIVariable variable = getQuorumView().convert(current.getValue(value));
                variable.setName("" + i);
                variables.add(variable);
            }
            node = current.getValue(next);
            i++;
        }
        return variables.toArray(new Variable[0]);
    }

    /**
     * This walks the list with its iterator, calling into the running
     * program, for a list whose nodes can't be read.
     *
     * @param reference
     * @param start
     * @param end
     * @return
     */
    private Variable[] iterate(ObjectReference reference, int start, int end) {
        List<Variable> variables = new ArrayList<>();
        List<Value> args = Collections.<Value>emptyList();
        try {
            Value iteratorValue = invoke(reference, "GetIterator", args);
            if (iteratorValue instanceof ObjectReference) {
                ObjectReference iterator = (ObjectReference) iteratorValue;
                int i = 0;
                while (i < end && hasNext(iterator)) {
                    Value value = invoke(iterator, "Next", args);
                    if (i >= start) {
                        JDIVariable variable = getQuorumView().convert(value);
                        variable.setName("" + i);
                        variables.add(variable);
                    }
                    i++;
                }
            }
        } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException ex) {
            Logger.getLogger(ListView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return variables.toArray(new Variable[0]);
    }

    private boolean hasNext(ObjectReference iterator)
            throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        Value value = invoke(iterator, "HasNext", Collections.<Value>emptyList());
        return value instanceof BooleanValue && ((BooleanValue) value).booleanValue();
    }

    @Override
    public int getElementCount(ObjectReference reference) {
        Field size = reference.referenceType().fieldByName(SIZE);
        if (size != null) {
            Value value = reference.getValue(size);
            if (value instanceof IntegerValue) {
                return ((IntegerValue) value).intValue();
            }
        }
        return 0;
    }

    @Override
    public String getValue(Value value) {
        if(value == null) {
            return getQuorumView().getNull();
        } else if(value instanceof ObjectReference) {
            return "size = " + getElementCount((ObjectReference) value);
        }  else {
            return value.toString();
        }
    }
}
