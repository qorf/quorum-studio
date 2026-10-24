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
 * This class is a general interface for representing breakpoints on the system.
 * 
 * @author Andreas Stefik
 */
public interface Breakpoint {
    
    /**
     * This method returns the line number the breakpoint is on.
     * 
     * @return 
     */
    public int getLine();
    
    /**
     * This method returns the fully qualified class name of the breakpoint.
     * 
     * @return 
     */
    public ClassInformation getClassInformation();
    
    /**
     * This method returns a key generated from information in the breakpoint
     * that can be used to identify unique breakpoints. By default, implementors
     * follow the format line:className.
     * @return 
     */
    public String getStaticKey();
    
    /**
     * This returns the number of times the breakpoint should be hit. This is
     * useful for creating one-off breakpoints, that fire only a single time, 
     * for example in a run forward to line call.
     * 
     * @return 
     */
    public int getCountFilter();
    
    /**
     * This method returns whether or not the breakpoint should be hit 
     * only a certain number of times.
     * 
     * @return 
     */
    public boolean hasCountFilter();
}
