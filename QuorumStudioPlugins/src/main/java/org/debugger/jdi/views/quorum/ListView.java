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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.Variable;
import org.debugger.jdi.JDIVariable;
import org.debugger.jdi.JDIVariablesModel;

/**
 * This is a view for list containers.
 * 
 * @author Andreas Stefik
 */
public class ListView extends QuorumVariableView {
    public final String key = "quorum.Libraries.Containers.List";
    
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
        List<Value> args = new LinkedList<>();
        List<Variable> variables = new LinkedList<Variable>();
        ThreadReference thread = this.getThread();
        VirtualMachine machine = this.getMachine();
        try {
            Value iteratorValue = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetIterator").get(0),args, 0);
            
            if(iteratorValue instanceof ObjectReference) {
                ObjectReference iterator = (ObjectReference) iteratorValue;
                
                Value boolValue = iterator.invokeMethod(thread,
                    iterator.referenceType().methodsByName("HasNext").get(0),args, 0);
                
                if (boolValue instanceof BooleanValue) {
                    BooleanValue bool = (BooleanValue) boolValue;
                    boolean hasNext = bool.booleanValue();
                    
                    int i = 0; //the position
                    while(hasNext) {
                        Value value = iterator.invokeMethod(thread,
                            iterator.referenceType().methodsByName("Next").get(0),args, 0);
                        JDIVariable variable = getQuorumView().convert(value);
                        variable.setName("" + i);
                        variables.add(variable);
                        
                        //check if there are more values
                        boolValue = iterator.invokeMethod(thread,
                            iterator.referenceType().methodsByName("HasNext").get(0),args, 0);
                        if (boolValue instanceof BooleanValue) {
                            bool = (BooleanValue) boolValue;
                            hasNext = bool.booleanValue();
                        } else {
                            hasNext = false;
                        }
                        
                        //update the position
                        i = i + 1;
                    }
                }
                return (Variable[]) variables.toArray(new JDIVariable[0]);
            }
        } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException ex) {
            Logger.getLogger(JDIVariablesModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new Variable[0];
    }

    @Override
    public String getValue(Value value) {
        if(value == null) {
            return getQuorumView().getNull();
        } else if(value instanceof ObjectReference) {
            ObjectReference ref = (ObjectReference) value;
            Field size = ref.referenceType().fieldByName("size");
            Value v = ref.getValue(size);
            JDIVariable variable = getQuorumView().convert(v);
            return "size = " + variable.getValue();
        }  else {
            return value.toString();
        }
    }
}
