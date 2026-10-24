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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.util.LinkedList;
import java.util.List;
import org.debugger.Variable;
import org.debugger.jdi.JDIVariable;

/**
 *
 * @author Andreas Stefik
 */
public class HashNodeView extends QuorumVariableView{
    public final String key = "quorum.Libraries.Containers.Support.HashNode";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String NEXT = "next";
    
    @Override
    public String getStaticKey() {
        return key;
    }
    
    @Override
    public String getObjectName() {
        return "Libraries.Containers.Support.HashNode";
    }
    
    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        List<Variable> variables = new LinkedList<>();
        
        //get the fields
        Field keyField = reference.referenceType().fieldByName(KEY);
        Field valueField = reference.referenceType().fieldByName(VALUE);
        Field nextField = reference.referenceType().fieldByName(NEXT);
        
        //get their values
        Value keyValue = reference.getValue(keyField);
        Value valueValue = reference.getValue(valueField);
        Value nextValue = reference.getValue(nextField);
        
        //convert them to variables, key first
        JDIVariable keyVariable = getQuorumView().convert(keyValue);
        keyVariable.setName(KEY);
        variables.add(keyVariable);
        
        //now values
        JDIVariable valueVariable = getQuorumView().convert(valueValue);
        valueVariable.setName(VALUE);
        variables.add(valueVariable);
        
        //now the support hash node for separate chaining
        JDIVariable nextVariable = getQuorumView().convert(nextValue);
        nextVariable.setName(NEXT);
        variables.add(nextVariable);
        
        //now return the list of variables to the client.
        return (Variable[]) variables.toArray(new JDIVariable[0]);
    }
    
    @Override
    public String getValue(Value value) {
        if(value == null) {
            return getQuorumView().getNull();
        } else if(value instanceof ObjectReference) {
            ObjectReference ref = (ObjectReference) value;
            Field keyField = ref.referenceType().fieldByName(KEY);
            Field valueField = ref.referenceType().fieldByName(VALUE);
            
            Value keyValue = ref.getValue(keyField);
            Value valueValue = ref.getValue(valueField);
            JDIVariable keyVariable = getQuorumView().convert(keyValue);
            keyVariable.setName(KEY);

            //now values
            JDIVariable valueVariable = getQuorumView().convert(valueValue);
            valueVariable.setName(VALUE);
            
            String name = keyVariable.getValue() + " => " + valueVariable.getValue();
            return name;
        } else {
            return value.toString();
        }
    }
}
