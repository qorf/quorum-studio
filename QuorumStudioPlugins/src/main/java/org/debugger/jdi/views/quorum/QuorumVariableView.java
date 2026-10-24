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
public abstract class QuorumVariableView {
    private QuorumView quorumView;
    private ThreadReference thread;
    private VirtualMachine machine;
    
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
                return value.toString();
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
        this.machine = machine;
    }
}
