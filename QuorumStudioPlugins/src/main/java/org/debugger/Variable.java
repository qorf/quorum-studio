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
package org.debugger;

/**
 * This class represents a variable on the system.
 * 
 * @author Andreas Stefik
 */
public interface Variable {
    
    /**
     * Returns whether the given variable is a field variable or a local variable.
     * If this value is true, it is a field variable.
     * 
     * @return the isField
     */
    public boolean isField();

    /**
     * This value returns true if this variable is the result of a watch
     * expression being evaluated.
     * 
     * @return 
     */
    public boolean isWatchExpression();
    
    /**
     * This method returns true if the variable is publicly available to other
     * classes. 
     * 
     * @return the isPublic
     */
    public boolean isPublic();

    /**
     * This method returns true if the variable is private.
     * 
     * @return the isPrivate
     */
    public boolean isPrivate();

    /**
     * This method returns the name of the variable.
     * 
     * @return the name
     */
    public String getName();

    /**
     * This method returns the name of the type of variable.
     * 
     * @return the typeName
     */
    public String getTypeName();

    /**
     * This method returns the value of the variable.
     * 
     * @return the value
     */
    public String getValue();
    
    /**
     * This method returns whether or not the variable is a primitive value.
     * If it is, then by definition it has no children.
     * 
     * @return 
     */
    public boolean isPrimitive();
    
    /**
     * This method returns true if the variable is a "parent" variable, as in, 
     * it stores some kind of pertinent information about a superclass or
     * inherited set of values.
     * 
     * @return 
     */
    public boolean isParent();
    
    /**
     * This method returns a raw reference to the object being referenced.
     * Whether this value is null is used in computing values in the variable
     * window.
     * 
     * @return 
     */
    public Object getReference();
}
