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

import com.sun.jdi.Value;

/**
 * This class provides a template for handling naming conventions inside of
 * a variable window. For example, some languages may call a "double" value, to
 * use java terminology, a number, or the "this" pointer, "me." This class
 * allows the watch window to substitute in these conventions on a per 
 * language basis, without altering the implementation.
 * 
 * @author Andreas Stefik
 */
public interface VariableView {
    /**
     * This method returns the name of the "this" pointer.
     * 
     * @return 
     */
    public String getThis();
    
    /**
     * This method returns the name of the integer primitive.
     * 
     * @return 
     */
    public String getIntPrimitive();
    
    /**
     * This method returns the name of long integers.
     * 
     * @return 
     */
    public String getLongPrimitive();
    
    /**
     * This method returns the name for boolean primitive values.
     * 
     * @return 
     */
    public String getBooleanPrimitive();
    
    /**
     * This method returns the name for floating point values.
     * 
     * @return 
     */
    public String getFloatPrimitive();
    
    /**
     * This method returns the name for double values.
     * 
     * @return 
     */
    public String getDoublePrimitive();
    
    /**
     * This method returns the name for String values.
     * 
     * @return 
     */
    public String getString();
    
    /**
     * This method returns the name of the value "null" for the programming
     * language.
     * 
     * @return 
     */
    public String getNull();
    
    /**
     * This method takes in a raw type name and converts it to a name
     * appropriate for users of a particular programming language.
     * 
     * @param name
     * @return 
     */
    public String getObjectName(String name);
    
    /**
     * This converts a JDI value object into a Language specific name.
     * 
     * @param value
     * @return 
     */
    public String getValue(Value value);
    
    /**
     * This method translates a variable name using special symbols into
     * a reasonable name.
     * 
     * @param name
     * @return 
     */
    public String getVariableName(String name);
}
