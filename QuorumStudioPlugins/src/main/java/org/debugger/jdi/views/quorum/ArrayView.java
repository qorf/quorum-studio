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
 *
 * @author Andreas Stefik
 */
public class ArrayView extends QuorumVariableView {
    public final String key = "quorum.Libraries.Containers.Array";
    
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
        List<Value> args = new LinkedList<>();
        ThreadReference thread = this.getThread();
        VirtualMachine machine = this.getMachine();
        try {
            Value sval = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetSize").get(0),args, 0);

            if (sval instanceof IntegerValue) {
                IntegerValue s = (IntegerValue) sval;
                int size = s.intValue();
                Variable[] variables = new Variable[size];
                //loop through all of the values and get their 
                //objects in the array
                for(int i = 0; i < size; i++) {
                    args = new LinkedList<>();
                    IntegerValue position = machine.mirrorOf(i);
                    args.add(position);

                    Value result = reference.invokeMethod(thread,
                        reference.referenceType().methodsByName("Get").get(0),args, 0);
                    JDIVariable variable = getQuorumView().convert(result);
                    variable.setName("" + i);
                    variables[i] = variable;
                }
                return variables;
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
            try {
                
                List<Value> args = new LinkedList<>();
                Value sval = ref.invokeMethod(getThread(),
                        ref.referenceType().methodsByName("GetSize").get(0),args, 0);
                if(sval != null && sval instanceof IntegerValue) {
                    return "size = " + sval.toString();
                }
            } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException ex) {
                Logger.getLogger(ArrayView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "#" + ref.hashCode();
        }  else {
            return value.toString();
        }
    }
    
}
