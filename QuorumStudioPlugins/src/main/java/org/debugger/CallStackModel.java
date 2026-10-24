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
 * The CallStackModel interface represents the call stack on the system. It
 * provides a basic ability to know where a particular method is in the context
 * of a running program.
 *
 * @author Andreas Stefik
 */
public interface CallStackModel {

    /**
     * Returns the number of children under this StackFrame object.
     * 
     * @param o
     * @param i
     * @param i1
     * @return 
     */
    public Object[] getChildren(StackFrame o, int i, int i1);

    /**
     * Returns true if this stack frame has no children.
     * 
     * @param o
     * @return 
     */
    public boolean isLeaf(StackFrame o);

    /**
     * Returns the number of children this stack frame has.
     * 
     * @param o
     * @return 
     */
    public int getChildrenCount(StackFrame o);

    /**
     * Returns the name of this stack frame in HTML.
     * 
     * @param o
     * @return 
     */
    public String getDisplayName(StackFrame o);

    /**
     * Returns a short description of the value of this stack frame.
     * 
     * @param o
     * @return 
     */
    public String getShortDescription(StackFrame o);

    /**
     * Returns whether this stack frame object can be modified.
     * 
     * @param node
     * @param column
     * @return 
     */
    public boolean isReadOnly(StackFrame node, VariableColumns column);

    /**
     * Returns a value at a particular column for this stack frame.
     * 
     * @param node
     * @param column
     * @return 
     */
    public Object getValueAt(StackFrame node, VariableColumns column);
}
