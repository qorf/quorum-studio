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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import org.debugger.Variable;
import org.debugger.jdi.views.QuorumView;

/**
 *
 * @author Andreas Stefik
 */
public class StackView extends QuorumVariableView {
    public final String key = "quorum.Libraries.Containers.Stack";
    private ListView view = new ListView();
    
    @Override
    public String getStaticKey() {
        return key;
    }
    
    @Override
    public String getObjectName() {
        return "Libraries.Containers.Stack";
    }
    
    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        Field list = reference.referenceType().fieldByName("list");
        Value value = reference.getValue(list);
        if(value instanceof ObjectReference) {
            return view.getSpecialVariableChildren((ObjectReference)value);
        }
        return new Variable[0];
    }

    @Override
    public String getValue(Value value) {
        if(value == null) {
            return getQuorumView().getNull();
        } else if(value instanceof ObjectReference) {
            ObjectReference reference = (ObjectReference) value;
            Field list = reference.referenceType().fieldByName("list");
            Value v = reference.getValue(list);
            if(v instanceof ObjectReference) {
                return view.getValue((ObjectReference)v);
            }
            return "#" + reference.hashCode();
        }  else {
            return value.toString();
        }
    }
    
    /**
     * @param machine the machine to set
     */
    @Override
    public void setMachine(VirtualMachine machine) {
        super.setMachine(machine);
        view.setMachine(machine);
    }
    
    /**
     * @param thread the thread to set
     */
    @Override
    public void setThread(ThreadReference thread) {
        super.setThread(thread);
        view.setThread(thread);
    }
    
    public void setQuorumView(QuorumView quorumView) {
        super.setQuorumView(quorumView);
        view.setQuorumView(quorumView);
    }
}
