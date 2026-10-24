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
package org.debugger.jdi.views;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import org.debugger.Variable;
import org.debugger.VariableView;

/**
 * This class provides a view for the ProcessJ programming language.
 * 
 * @author Andreas Stefik
 */
public class ProcessJView extends JDIVariableView {
    
    @Override
    public String getThis() {
        return "this";
    }

    @Override
    public String getIntPrimitive() {
        return "int";
    }

    @Override
    public String getLongPrimitive() {
        return "long";
    }

    @Override
    public String getBooleanPrimitive() {
        return "boolean";
    }

    @Override
    public String getFloatPrimitive() {
        return "float";
    }

    @Override
    public String getDoublePrimitive() {
        return "double";
    }
    
    @Override
    public String getString() {
        return "java.lang.String";
    }

    @Override
    public String getObjectName(String name) {
        return name;
    }
    
    @Override
    public String getNull() {
        return "null";
    }

    @Override
    public String getValue(Value value) {
        return value.toString();
    }
    
    /**
     * This method translates a variable name using special symbols into
     * a reasonable name. For Quorum, the only change is for parent variables.
     * 
     * @param name
     * @return 
     */
    @Override
    public String getVariableName(String name) {
        return name;
    }
    
    @Override
    public boolean isSpecialVariable(ObjectReference reference) {
        return false;
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        return new Variable[0];
    }
}
