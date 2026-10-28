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

import com.sun.jdi.ArrayReference;
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
 * A view of Quorum's arrays. An array keeps its items in its plugin, a Java
 * array, so the items are read straight from it, only the range that's
 * asked for, without calling into the running program. Calls are a fallback
 * for an array without a plugin to read.
 *
 * @author Andreas Stefik
 */
public class ArrayView extends QuorumVariableView {
    public final String key = "quorum.Libraries.Containers.Array";
    private static final String PLUGIN = "plugin_";
    private static final String SIZE = "size";
    
    @Override
    public String getStaticKey() {
        return key;
    }
    
    @Override
    public String getObjectName() {
        return "Libraries.Containers.Array";
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        return getSpecialVariableChildren(reference, 0, 0);
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference, int from, int to) {
        int[] range = JDIVariableView.clampRange(from, to, getElementCount(reference));
        List<Value> values = getElementValues(reference, range[0], range[1]);
        Variable[] variables = new Variable[values.size()];
        for (int i = 0; i < variables.length; i++) {
            JDIVariable variable = getQuorumView().convert(values.get(i));
            variable.setName("" + (range[0] + i));
            variables[i] = variable;
        }
        return variables;
    }

    @Override
    public int getElementCount(ObjectReference reference) {
        ObjectReference plugin = getPlugin(reference);
        if (plugin != null) {
            Field size = plugin.referenceType().fieldByName(SIZE);
            if (size != null && !size.isStatic()) {
                Value value = plugin.getValue(size);
                if (value instanceof IntegerValue) {
                    return ((IntegerValue) value).intValue();
                }
            }
            ArrayReference array = getBackingArray(plugin);
            return array == null ? 0 : array.length();
        }

        try {
            Value value = invoke(reference, "GetSize", Collections.<Value>emptyList());
            if (value instanceof IntegerValue) {
                return ((IntegerValue) value).intValue();
            }
        } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException ex) {
            Logger.getLogger(ArrayView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * This returns the array's items from start up to, but not including,
     * end. Hash tables use it to read their buckets.
     *
     * @param reference
     * @param start
     * @param end
     * @return
     */
    public List<Value> getElementValues(ObjectReference reference, int start, int end) {
        if (end <= start) {
            return Collections.emptyList();
        }
        ObjectReference plugin = getPlugin(reference);
        if (plugin != null) {
            ArrayReference array = getBackingArray(plugin);
            if (array == null) {
                return Collections.emptyList();
            }
            end = Math.min(end, array.length());
            if (end <= start) {
                return Collections.emptyList();
            }
            return array.getValues(start, end - start);
        }

        List<Value> values = new ArrayList<>();
        try {
            for (int i = start; i < end; i++) {
                List<Value> args = Collections.<Value>singletonList(reference.virtualMachine().mirrorOf(i));
                values.add(invoke(reference, "Get", args));
            }
        } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException ex) {
            Logger.getLogger(ArrayView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return values;
    }

    /**
     * This returns the array's plugin, the Java object that holds its items,
     * or null if it has none.
     *
     * @param reference
     * @return
     */
    private ObjectReference getPlugin(ObjectReference reference) {
        Field field = reference.referenceType().fieldByName(PLUGIN);
        if (field == null) {
            return null;
        }
        Value value = reference.getValue(field);
        if (value instanceof ObjectReference) {
            return (ObjectReference) value;
        }
        return null;
    }

    /**
     * This returns the Java array inside a plugin, such as Array's items or
     * Integer32BitArray's ints, or null if it hasn't made one yet.
     *
     * @param plugin
     * @return
     */
    private ArrayReference getBackingArray(ObjectReference plugin) {
        for (Field field : plugin.referenceType().allFields()) {
            if (field.isStatic()) {
                continue;
            }
            Value value = plugin.getValue(field);
            if (value instanceof ArrayReference) {
                return (ArrayReference) value;
            }
        }
        return null;
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
