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

import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.List;
import org.debugger.Variable;
import org.debugger.jdi.JDIVariable;
import org.debugger.jdi.views.JDIVariableView;
import org.debugger.jdi.views.QuorumView;

/**
 * This is a view for the hashtable class. Its items are its entries, each
 * a HashNode with a key and a value, in the order they sit in the table's
 * buckets. They're read from the table's fields, without calling into the
 * running program.
 * 
 * @author Andreas Stefik
 */
public class HashView extends QuorumVariableView {
    public final String key = "quorum.Libraries.Containers.HashTable";
    private static final String ARRAY = "array";
    private static final String SIZE = "size";
    private static final String NEXT = "next";
    private final ArrayView arrays = new ArrayView();
    
    @Override
    public String getStaticKey() {
        return key;
    }
    
    @Override
    public String getObjectName() {
        return "Libraries.Containers.HashTable";
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
        Field fieldArray = reference.referenceType().fieldByName(ARRAY);
        if(fieldArray == null || end <= start) {
            return new Variable[0];
        }
        Value value = reference.getValue(fieldArray);
        if(!(value instanceof ObjectReference)) {
            return new Variable[0];
        }

        ObjectReference array = (ObjectReference) value;
        List<Value> buckets = arrays.getElementValues(array, 0, arrays.getElementCount(array));
        Field next = null;
        int i = 0;
        for(Value bucket : buckets) {
            Value node = bucket;
            while(node instanceof ObjectReference && i < end) {
                ObjectReference current = (ObjectReference) node;
                if(i >= start) {
                    JDIVariable variable = getQuorumView().convert(current);
                    variable.setName("" + i);
                    variables.add(variable);
                }
                if(next == null) {
                    next = current.referenceType().fieldByName(NEXT);
                    if(next == null) {
                        break;
                    }
                }
                node = current.getValue(next);
                i++;
            }
            if(i >= end) {
                break;
            }
        }
        return variables.toArray(new Variable[0]);
    }

    @Override
    public int getElementCount(ObjectReference reference) {
        Field size = reference.referenceType().fieldByName(SIZE);
        if(size != null) {
            Value value = reference.getValue(size);
            if(value instanceof IntegerValue) {
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

    @Override
    public void setThread(ThreadReference thread) {
        super.setThread(thread);
        arrays.setThread(thread);
    }

    @Override
    public void setMachine(VirtualMachine machine) {
        super.setMachine(machine);
        arrays.setMachine(machine);
    }

    @Override
    public void setQuorumView(QuorumView quorumView) {
        super.setQuorumView(quorumView);
        arrays.setQuorumView(quorumView);
    }
}
