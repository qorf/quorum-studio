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

import org.debugger.jdi.events.JDIDebuggerEvent;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMStartEvent;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.DebuggerListener;
import org.debugger.StackFrame;
import org.debugger.jdi.events.JDIDebuggerBreakpointEvent;
import org.debugger.jdi.events.JDIDebuggerExceptionEvent;
import org.debugger.jdi.events.JDIDebuggerLocationEvent;
import org.debugger.jdi.events.JDIDebuggerStartEvent;
import org.debugger.jdi.events.JDIDebuggerStepEvent;
import org.debugger.jdi.events.JDIDebuggerStopEvent;

/**
 * The VirtualMachineRequestManager is a helper class for receiving events from
 * a running virtual machine and dispatching them to any objects listening to
 * the results.
 *
 * @author Andreas Stefik
 */
public class VirtualMachineRequestManager extends Thread {

    /**
     * This is the virtual machine object we are working with.
     */
    private VirtualMachine virtualMachine = null;

    /**
     * This is the implementation of the debugger we are using for this
     * dispatcher.
     */
    private JDIDebugger debugger;

    @Override
    public void run() {
        boolean connected = true;
        while (connected) {
            try {
                EventSet set = virtualMachine.eventQueue().remove();
                EventIterator eventIterator = set.eventIterator();
                while (eventIterator.hasNext()) {
                    Event next = eventIterator.next();
                    JDIDebuggerEvent event = convert(next);
                    fireEvent(event);
                }
            } catch (InterruptedException ex) {
            } catch (VMDisconnectedException ex) {
                connected = false;
                JDIDebuggerEvent exit = new JDIDebuggerEvent();
                exit.setValue("VMDisconnectedException");
                fireEvent(exit);
            }
        }
    }
    
    /**
     * This method converts an event from the virtual machine to a structured
     * debugger event for processing by listeners.
     * 
     * @param event
     * @return 
     */
    public JDIDebuggerEvent convert(Event event) {
        JDIDebuggerEvent debug = null;
        String output = "";
        if(event instanceof ClassPrepareEvent) { 
            debug = new JDIDebuggerEvent();
            //send it back to the debugger so it can handle 
            //any details about rewriting or 
            ClassPrepareEvent prep = (ClassPrepareEvent) event;
            debugger.action(prep);
        } else if (event instanceof ModificationWatchpointEvent) {
            debug = new JDIDebuggerEvent();
            ModificationWatchpointEvent mod = (ModificationWatchpointEvent) event;
            Field field = mod.field();
            String typeName = field.typeName();
            String name = field.name();
            Value current = mod.valueCurrent();
            Value newValue = mod.valueToBe();
            output = "Field: " + typeName + " " + name + ", Current: " + current + ", new: " + newValue;
            virtualMachine.resume();
        } else if(event instanceof VMStartEvent) {
        } else if(event instanceof VMDeathEvent) {
            VMDeathEvent eve = (VMDeathEvent) event;
            debug = new JDIDebuggerStopEvent();
        } else if(event instanceof StepEvent) {
            StepEvent eve = (StepEvent) event;
            debug = new JDIDebuggerStepEvent();
            setLIneInformation(eve, (JDIDebuggerStepEvent) debug);
            ThreadReference thread = eve.thread();
            debugger.setThreadReference(thread);
        } else if(event instanceof BreakpointEvent) {
            BreakpointEvent eve = (BreakpointEvent) event;
            debug = new JDIDebuggerBreakpointEvent();
            setLIneInformation(eve, (JDIDebuggerBreakpointEvent) debug);   
            ThreadReference thread = eve.thread();
            debugger.setThreadReference(thread);
        } else if (event instanceof ExceptionEvent) {
            ExceptionEvent eve = (ExceptionEvent) event;
            JDIDebuggerExceptionEvent exceptionEvent = new JDIDebuggerExceptionEvent();
            
            ObjectReference exception = eve.exception();
            ReferenceType exceptionType = exception.referenceType();
            try {
                //this is not a great solution, but remote debugging with
                //glassfish appears to throw a series of exceptions, for reasons
                //I can't immediately determine. This probably slows down
                //remote debugging, and is a temporary fix, but should probably
                //not remain here permanently. In general, exceptions need 
                //to improve here.
                if(exception.owningThread() == null) {
                    debug = null;
                    exception.virtualMachine().resume(); //just ignore these
                } else {
                    List<Method> methods = exceptionType.methodsByName("getStackTrace");
                    Field messageField = exceptionType.fieldByName("errorMessage");
                    if(exception != null && messageField != null && exception.getValue(messageField) != null) {
                    String message = exception.getValue(messageField).toString();
                        Object[] sf = debugger.getCallStackModel().getChildren(null, 0, 0);
                        if( sf instanceof JDIStackFrame[]) {
                            StackFrame[] frame = (JDIStackFrame[]) sf;
                            exceptionEvent.setStackFrame(frame);
                            exceptionEvent.setMessage(message);
                        }
                    }
                }
            } catch (IncompatibleThreadStateException ex) {
                Logger.getLogger(VirtualMachineRequestManager.class.getName()).log(Level.INFO, null, ex);
            }
            
            debug = exceptionEvent;
        } else {
            debug = new JDIDebuggerEvent();
            output = event.toString();
        }
        
        if(debug != null) {
            debug.setValue(output);
        }
        return debug;
    }
    
    private void setLIneInformation(LocatableEvent event, JDIDebuggerLocationEvent jdi) {
        Location location = event.location();
        int line = location.lineNumber();
        String source = "";
        source = location.declaringType().name();
        jdi.setLine(line);
        jdi.setSource(source);
    }
    
    /**
     * This method fires events to all event listeners currently loaded.
     * 
     * @param event 
     */
    public void fireEvent(JDIDebuggerEvent event) {
        if(event == null) {
            return;
        }
        Iterator<DebuggerListener> listeners = debugger.getListeners();
        while(listeners.hasNext()) {
            DebuggerListener listener = listeners.next();
            dispatch(listener, event);
        }
    }
    
    private void dispatch(DebuggerListener listener, JDIDebuggerEvent event) {
        if(event instanceof JDIDebuggerStartEvent) {
            JDIDebuggerStartEvent eve = (JDIDebuggerStartEvent) event;
            listener.accept(eve);
        } else if(event instanceof JDIDebuggerStopEvent) {
            JDIDebuggerStopEvent eve = (JDIDebuggerStopEvent) event;
            listener.accept(eve);
        } else if(event instanceof JDIDebuggerStepEvent) {
            JDIDebuggerStepEvent eve = (JDIDebuggerStepEvent) event;
            listener.accept(eve);
        } else if(event instanceof JDIDebuggerBreakpointEvent) {
            JDIDebuggerBreakpointEvent eve = (JDIDebuggerBreakpointEvent) event;
            listener.accept(eve);
        } else if(event instanceof JDIDebuggerExceptionEvent) {
            JDIDebuggerExceptionEvent eve = (JDIDebuggerExceptionEvent) event;
            listener.accept(eve);
        }
    }
    /**
     * @return the debugger
     */
    public JDIDebugger getDebugger() {
        return debugger;
    }

    /**
     * @param debugger the debugger to set
     */
    public void setDebugger(JDIDebugger debugger) {
        this.debugger = debugger;
        virtualMachine = debugger.getVirtualMachine();
    }
}
