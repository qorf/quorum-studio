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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * The Debugger interface represents a way to launch, stop, and control a debugger
 for a program.
 * 
 * @author Andreas Stefik
 */
public interface Debugger {
    
    /**
     * This steps a program backward in time by one line, the opposite of
     * step over.
     * 
     */
    public void stepBackOver();

    /**
     * This conducts a Step Out in the debugger. Typically, this runs a program
     * unto the end of its stack frame and then stops.
     */
    public void stepOut();
    
    /**
     * This conducts a typical step over in the environment.
     */
    public void stepOver();

    /**
     * This method does a step into.
     */
    public void stepInto();

    /**
     * This method does a step back into, which goes backward in time. It is
     * the opposite of step into.
     * 
     */
    public void stepBackInto();

    /**
     * This method runs the program forward in time to a particular line and
     * class name specified. Breakpoints may be hit along the way and the program
     * is not guaranteed to reach the line specified.
     * 
     * @param fullyQualifiedClassName This is the fully qualified class name where
     *  the program should stop. For example, java.lang.object.
     * 
     * @param line This is the line number where the program should stop.
     */
    public void runForwardToLine(String fullyQualifiedClassName, int line);

    /**
     * This method runs the program backward in time to a particular line and
     * class name specified. Breakpoints may be hit along the way and the program
     * is not guaranteed to reach the line specified.
     * 
     * @param fullyQualifiedClassName This is the fully qualified class name where
     *  the program should stop. For example, java.lang.object.
     * 
     * @param line This is the line number where the program should stop.
     */
    public void runBackToLine(String fullyQualifiedClassName, int line);

    /**
     * This method runs the debugger backward in time, stopping at any
     * points.
     */
    public void backward();
    
    /**
     * This method runs the debugger forward in time, stopping at any
     * points.
     */
    public void forward();
    
    /**
     * This command launches the process. Implementations may require setup
     * information in order to launch properly. This does not cause the debugger
     * to begin processing events. To move the process forward, you must first
     * call the forward method.
     * 
     */
    public void launch();
    
    /**
     * This method returns true if the implementation is an omniscient debugger;
     * 
     * @return 
     */
    public boolean isOmniscient();
    
    /**
     * This method stops the debugger and clears all values.
     */
    public void stop();

    /**
     * This method resumes the debugger after being paused. If the debugger
     * is already running, this command does nothing.
     */
    public void resume();

    /**
     * This command pauses a running program. If a program is not executing in 
     * the debugger, it does nothing.
     */
    public void pause();
    
    /**
     * Adds a new listener to any events that happen in the debugger.
     * 
     * @param listener 
     */
    public void add(DebuggerListener listener);
    
    /**
     * Removes a listener from the debugger.
     * 
     * @param listener 
     * @return Returns the removed DebuggerListener. If no listener existed, null
     * is returned.
     */
    public DebuggerListener remove(DebuggerListener listener);
    
    /**
     * Removes all listeners from the debugger.
     * 
     */
    public void clearListeners();
    
    /**
     * Obtains an iterator of all listeners currently held by the debugger.
     * 
     * @return 
     */
    public Iterator<DebuggerListener> getListeners();
    
    /**
     * This method adds a new breakpoint to the debugger.
     * 
     * @param breakpoint 
     */
    public void add(Breakpoint breakpoint);
    
    /**
     * This method removes a breakpoint from the debugger.
     * 
     * @param breakpoint 
     * @return  
     */
    public Breakpoint remove(Breakpoint breakpoint);
    
    /**
     * This method clears all breakpoints out of the debugger.
     * 
     */
    public void clearBreakpoints();
    
    /**
     * This method returns a copy of all breakpoints currently loaded in the 
     * debugger.
     * 
     * @return 
     */
    public Iterator<Breakpoint> getBreakpoints();
    
    /**
     * This method sets the name of the executable as a full path. For example.
     * for one debugger implementation, we might set the full path to a jar
     * file.
     * 
     * @param string 
     */
    public void setExecutable(String string);
    
    /**
     * This method returns the full path to the executable that is being
     * debugged.
     * 
     * @return 
     */
    public String getExecutable();
    
    /**
     * This method returns a variables model for the debugger. This model can
     * typically only be accessed if a debugger is suspended.
     * 
     * @return 
     */
    public VariablesModel getVariablesModel();
    
    /**
     * This method returns a representation of the call stack on the system.
     * 
     * @return 
     */
    public CallStackModel getCallStackModel();
    
    /**
     * This method returns the input stream for the process being debugged. If 
     * no process is being debugged, this returns null.
     * 
     * @return the inputStream
     */
    public InputStream getInputStream();

    /**
     * This method returns the outpu stream for the process being debugged. If 
     * no process is being debugged, this returns null.
     * 
     * @return the outputStream
     */
    public OutputStream getOutputStream();

    /**
     * This method returns the input error stream for the process being debugged. If 
     * no process is being debugged, this returns null.
     * 
     * @return the errorStream
     */
    public InputStream getErrorStream();
    
    /**
     * This method returns a monitor that can be used to synchronize on
     * debugger operations.
     * 
     * @return 
     */
    public Object getResumeMonitor();
    
    /**
     * This method sets the location that any files should be set relative to. As
     * the details for how files work are programming language and operating
     * system dependent, how this works varies by implementation. Not all
     * implementations are guaranteed to honor the meaning of this method.
     * 
     * @param directory 
     */
    public void setWorkingDirectory(String directory);
    
    /**
     * This method returns the location of the default working directory, if one
     * is known.
     * 
     * @return 
     */
    public String getWorkingDirectory();
    
    /**
     * This method returns whether or not the implementation is remotely 
     * debugging. By default, this value is false.
     * 
     * @return the isRemoteDebugging
     */
    public boolean isRemoteDebugging();

    /**
     * This method tells the debugger to remotely debug. By default, this value
     * is false in implements.
     * 
     * @param isRemoteDebugging the isRemoteDebugging to set
     */
    public void setRemoteDebugging(boolean isRemoteDebugging);

    /**
     * This method returns the port that is being remotely debugged. By default,
     * implementations return port 8000.
     * 
     * @return the remotePort
     */
    public int getRemotePort();

    /**
     * If an application is set to be remote debugged, this method sets the 
     * port that will be used. This value is only used if isRemoteDebugging()
     * returns true. 
     * 
     * @param remotePort the remotePort to set
     */
    public void setRemotePort(int remotePort);
    
    /**
     * This method returns the host string (e.g., 127.0.0.1).
     * @return the host
     */
    public String getHost();

    /**
     * This method sets the host string (e.g., 127.0.0.1).
     * @param host the host to set
     */
    public void setHost(String host);
}
