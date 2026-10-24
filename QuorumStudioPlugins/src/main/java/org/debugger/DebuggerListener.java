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

import org.debugger.events.DebuggerBreakpointEvent;
import org.debugger.events.DebuggerExceptionEvent;
import org.debugger.events.DebuggerStartEvent;
import org.debugger.events.DebuggerStepEvent;
import org.debugger.events.DebuggerStopEvent;

/**
 * A debugger listener is an object that listens for events from the system.
 * Examples of events include the virtual machine starting up, shutting down,
 * stepping, or hitting a breakpoint.
 * 
 * @author Andreas Stefik
 */
public interface DebuggerListener {
    
    /**
     * This even signifies that the debugger has begun executing.
     * 
     * @param event 
     */
    public void accept(DebuggerStartEvent event);
    
    /**
     * This event signifies that the debugger has stopped executing.
     * 
     * @param event 
     */
    public void accept(DebuggerStopEvent event);
    
    /**
     * This event signifies that the debugger has made a step.
     * 
     * @param event 
     */
    public void accept(DebuggerStepEvent event);
    
    /**
     * This event signifies that an exception was thrown in the virtual machine.
     * 
     * @param event 
     */
    public void accept(DebuggerExceptionEvent event);
    
    /**
     * This event signifies that the debugger has hit a breakpoint.
     * 
     * @param event 
     */
    public void accept(DebuggerBreakpointEvent event);
    
    /**
     * Provides a unique name for the event listener on the system. Listeners 
     * that are submitted to the debugger may override others if their name
     * is not unique.
     * 
     * @return 
     */
    public String getName();
}
