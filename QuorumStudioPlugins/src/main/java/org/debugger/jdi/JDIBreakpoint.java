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

import org.debugger.Breakpoint;
import org.debugger.ClassInformation;

/**
 * This is an implementation of the breakpoint interface, tied to the Java
 * Debugging Interface (JDI).
 * 
 * @author Andreas Stefik
 */
public class JDIBreakpoint implements Breakpoint {
    private int line;
    private ClassInformation classInformation;
    private int countFilter = -1;
    private boolean hasCountFilter = false;
    
    @Override
    public int getLine() {
        return line;
    }

    @Override
    public ClassInformation getClassInformation() {
        return classInformation;
    }

    /**
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * @param classInformation the className to set
     */
    public void setClassInformation(ClassInformation classInformation) {
        this.classInformation = classInformation;
    }

    @Override
    public String getStaticKey() {
        return getLine() + ":" + getClassInformation().getFullyQualifiedName();
    }
    
    @Override
    public int hashCode() {
        return line;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JDIBreakpoint) {
            JDIBreakpoint bp = (JDIBreakpoint) obj;
            if(this.getClassInformation().getFullyQualifiedName().equals(bp.getClassInformation().getFullyQualifiedName())
               && this.line == bp.line) {
                return true;
            }
        }
        return false;
        
    }

    @Override
    public int getCountFilter() {
        return countFilter;
    }

    @Override
    public boolean hasCountFilter() {
        return hasCountFilter;
    }

    /**
     * @param countFilter the countFilter to set
     */
    public void setCountFilter(int countFilter) {
        this.countFilter = countFilter;
    }

    /**
     * @param hasCountFilter the hasCountFilter to set
     */
    public void setHasCountFilter(boolean hasCountFilter) {
        this.hasCountFilter = hasCountFilter;
    }
}
