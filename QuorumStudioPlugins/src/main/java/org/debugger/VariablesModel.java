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
 * This interface provides a method for obtaining information about variables
 * from the system.
 * @author Andreas Stefik
 */
public interface VariablesModel {

    /**
     * For a particular object, obtain any children the object has. Debugger
     * implementations may limit the number of children returned.
     * 
     * @param parent
     * @param from
     * @param to
     * @return 
     */
    public Variable[] getChildren(Variable parent, int from, int to);

    /**
     * Returns whether the current value is a leaf node or not.
     * 
     * @param node
     * @return 
     */
    public boolean isLeaf(Variable node);

    /**
     * Returns the number of children a particular node has.
     * 
     * @param node
     * @return 
     */
    public int getChildrenCount(Variable node);

    /**
     * Returns the display name of the object.
     * 
     * @param node
     * @return 
     */
    public String getDisplayName(Variable node);

    /**
     * Returns a short text description for the node.
     * 
     * @param node
     * @return 
     */
    public String getShortDescription(Variable node);

    /**
     * If the debugger implementation has a multi-column interface, 
     * this method returns what information should be at each column for the
     * node.
     * 
     * @param node
     * @param column
     * @return 
     */
    public Object getValueAt(Variable node, VariableColumns column);

    /**
     * Determines whether the node is read only or not.
     * 
     * @param node
     * @param column
     * @return 
     */
    public boolean isReadOnly(Variable node, VariableColumns column);

    /**
     * This method attempts to set the value of the particular node. This may
     * or may not be allowed by the debugger implementation.
     * 
     * @param node
     * @param column
     * @param value 
     */
    public void setValueAt(Variable node, VariableColumns column, Object value);
    
    /**
     * This method returns a WatchResult object is the expression passed
     * in the Watch object was valid for this particular debugger
     * implementation.
     * 
     * @param watch
     * @return 
     */
    public Variable getWatchResult(Watch watch);
}
