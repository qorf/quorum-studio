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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
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
public class FileView extends QuorumVariableView{
    public final String key = "quorum.Libraries.System.File";
    
    @Override
    public String getStaticKey() {
        return key;
    }
    
    @Override
    public String getObjectName() {
        return "Libraries.System.File";
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        List<Value> args = new LinkedList<>();
        List<Variable> variables = new LinkedList<Variable>();
        ThreadReference thread = this.getThread();
        try {
            Value value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetWorkingDirectory").get(0),args, 0);
            JDIVariable variable = getQuorumView().convert(value);
            variable.setName("GetWorkingDirectory()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetPath").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetPath()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetAbsolutePath").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetAbsolutePath()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("Exists").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("Exists()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("IsFile").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("IsFile()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("IsDirectory").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("IsDirectory()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("IsHidden").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("IsHidden()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetFileName").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetFileName()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetFileExtension").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetFileExtension()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetParentDirectory").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetParentDirectory()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetFileSize").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetFileSize()");
            variables.add(variable);
            
            value = reference.invokeMethod(thread,
                    reference.referenceType().methodsByName("GetLastModifiedDate").get(0),args, 0);
            variable = getQuorumView().convert(value);
            variable.setName("GetLastModifiedDate()");
            variables.add(variable);
            
            return (Variable[]) variables.toArray(new JDIVariable[0]);
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
            ObjectReference reference = (ObjectReference) value;
            List<Value> args = new LinkedList<>();
            Value result;
            try {
                result = reference.invokeMethod(this.getThread(),
                        reference.referenceType().methodsByName("GetFileName").get(0),args, 0);
                JDIVariable variable = getQuorumView().convert(result);
                variable.setName("GetFileName()");
                return variable.getValue();
            } catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException ex) {
                Logger.getLogger(FileView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "#" + reference.hashCode();
        } else {
            return value.toString();
        }
    }
}
