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
package org.debugger.jdi;

import org.debugger.Variable;

/**
 * This class represents a variable, as stored through the Java Debugging
 * Interface (JDI). 
 * 
 * @author Andreas Stefik
 */
public class JDIVariable implements Variable {
    private boolean isField = false;
    private boolean isPublic = false;
    private boolean isPrivate = false;
    private String name = "";
    private String typeName = "";
    private String value = "";
    private boolean isPrimitive = true;
    private Object object;
    private boolean parent = false;
    private boolean watchExpression = false;
    
    /**
     * @return the isField
     */
    @Override
    public boolean isField() {
        return isField;
    }

    /**
     * @param isField the isField to set
     */
    public void setField(boolean isField) {
        this.isField = isField;
    }

    /**
     * @return the isPublic
     */
    @Override
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * @param isPublic the isPublic to set
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * @return the isPrivate
     */
    @Override
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * @param isPrivate the isPrivate to set
     */
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the typeName
     */
    @Override
    public String getTypeName() {
        return typeName;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return the value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isPrimitive() {
        return isPrimitive;
    }
    
    /**
     * This method sets whether this variable is a primitive value or not.
     * 
     * @param primitive 
     */
    public void setPrimitive(boolean primitive) {
        this.isPrimitive = primitive;
    }

    @Override
    public Object getReference() {
        return object;
    }
    
    /**
     * This sets the object in question.
     * 
     * @param object 
     */
    public void setObjectReference(Object object) {
        this.object = object;
    }

    @Override
    public boolean isParent() {
        return parent;
    }
    
    /**
     * Setting this to true indicates this is a parent variable.
     * 
     * @param parent 
     */
    public void setParent(boolean parent) {
        this.parent = parent;
    }

    /**
     * @return the watchExpression
     */
    public boolean isWatchExpression() {
        return watchExpression;
    }

    /**
     * @param watchExpression the watchExpression to set
     */
    public void setWatchExpression(boolean watchExpression) {
        this.watchExpression = watchExpression;
    }
}
