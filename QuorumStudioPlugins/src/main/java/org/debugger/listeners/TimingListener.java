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
package org.debugger.listeners;

import org.debugger.DebuggerListener;
import org.debugger.events.DebuggerBreakpointEvent;
import org.debugger.events.DebuggerExceptionEvent;
import org.debugger.events.DebuggerStartEvent;
import org.debugger.events.DebuggerStepEvent;
import org.debugger.events.DebuggerStopEvent;

/**
 * This class is a listener that times the running of the program running in 
 * the debugger. It is used predominately for unit testing and debugging.
 * 
 * @author Andreas Stefik
 */
public class TimingListener implements DebuggerListener {

    long startTime = 0;
    long stopTime = 0;
    @Override
    public String getName() {
        return "Timing Listener";
    }

    @Override
    public void accept(DebuggerStartEvent event) {
        startTime = System.nanoTime();
    }

    @Override
    public void accept(DebuggerStopEvent event) {
        stopTime = System.nanoTime();
        double total = stopTime - startTime;
        total = total / 1000000000;
        System.out.println("Total Running Time: " + total + " seconds.");
    }

    @Override
    public void accept(DebuggerStepEvent event) {
    }

    @Override
    public void accept(DebuggerBreakpointEvent event) {
    }
    
    @Override
    public void accept(DebuggerExceptionEvent event) {
    }
}
