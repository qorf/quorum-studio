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

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import org.debugger.Debugger;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.Breakpoint;
import org.debugger.CallStackModel;
import org.debugger.DebuggerListener;
import org.debugger.VariablesModel;
import org.debugger.filters.LanguageFilter;
import org.debugger.filters.QuorumFilter;
import org.debugger.jdi.events.JDIDebuggerStartEvent;
import org.debugger.jdi.views.JDIVariableView;
import org.debugger.jdi.views.QuorumView;

/**
 * This is an implementation of the debugger interface that uses JDI
 * to analyze events from a java virtual machine language.
 * 
 * @author Andreas Stefik
 */
public class JDIDebugger implements Debugger {

    /**
     * The names of JDI's standard connectors. Connectors are found by these
     * names rather than by their classes (com.sun.tools.jdi), which the JDK
     * doesn't export: JDK 17 and later block code from using them.
     */
    private static final String RAW_COMMAND_LINE_LAUNCH = "com.sun.jdi.RawCommandLineLaunch";
    private static final String COMMAND_LINE_LAUNCH = "com.sun.jdi.CommandLineLaunch";
    private static final String SOCKET_ATTACH = "com.sun.jdi.SocketAttach";

    /**
     * This is a virtual machine object used by JDI to begin a process.
     */
    private VirtualMachine virtualMachine = null;

    /**
     * This is a logger object that can put out information from the system.
     */
    private Logger logger = Logger.getLogger(JDIDebugger.class.getName());
    
    /**
     * This value represents the arguments for the program.
     */
    private String arguments = "";

    /**
     * This value represents the full path name to the executable to be launched.
     */
    private String executable = null;
    
    /**
     * This is the input stream object from the virtual machine.
     */
    private InputStream inputStream = null;
    
    /**
     * This is the output stream object from the virtual machine.
     */
    private OutputStream outputStream = null;
    
    /**
     * This is the error stream object from the virtual machine.
     */
    private InputStream errorStream = null;

    /**
     * This HashMap object stores all debugger listeners.
     */
    private final HashMap<String, DebuggerListener> listeners = new HashMap<>();
    
    /**
     * This is an object that gets events from the virtual machine in another
     * thread and dispatches them to listeners.
     */
    private VirtualMachineRequestManager requestManager;
    
    /**
     * This class manages all breakpoints on the system, including
     * resolving them appropriately when new classes are loaded by the
     * class loaders.
     * 
     */
    private BreakpointManager breakpoints = new BreakpointManager();
    
    /**
     * This is a reference to the currently executing thread in the virtual
     * machine.
     */
    private ThreadReference threadReference = null;
    
    /**
     * This value gathers information about the variables on the system.
     * 
     */
    private JDIVariablesModel variables;
    
    /**
     * This variable provides naming convention support for altering the 
     * variable window to use a particular programming language's style
     * for the variable window.
     * 
     */
    private JDIVariableView view;
    
    /**
     * This value allows us to filter out given fields or parts of a stack
     * trace depending upon what should be hidden to users by default for 
     * a particular programming language.
     */
    private LanguageFilter filter;
    
    /**
     * This variable represents the call stack on the system.
     */
    private JDICallStackModel callStack;
    
    /**
     * This object is the monitor for this debugger's step operations.
     * 
     */
    private Object monitor = new Object();
    
    /**
     * This string represents the current working directory.
     */
    private String workingDirectory = "";
    
    /**
     * Determines whether the debugger should try and make a remote connection.
     */
    private boolean isRemoteDebugging = false;
    
    /**
     * The port to connect to remotely.
     */
    private int remotePort = 8000;
    
    /**
     * The transport command.
     */
    private String remoteTransport = "dt_socket";
    
    /**
     * The remote host to connect to. By default this is set to localhost.
     */
    private String host = "127.0.0.1";
    
    @Override
    public void stepBackOver() {
    }

    /**
     * Clears out the buffer of step requests so new ones can proceed.
     * 
     * @param threadReference 
     */
    public void clearStepRequests(ThreadReference threadReference) {
        EventRequestManager evm = this.virtualMachine.eventRequestManager();
        Iterator<StepRequest> iterator = evm.stepRequests().iterator();
        while (iterator.hasNext()) {
            StepRequest next = iterator.next();
            if (next.thread().equals(threadReference)) {
                evm.deleteEventRequest(next);
                break;
            }
        }
    }
    
    @Override
    public void stepOut() {
        synchronized(monitor) {
            EventRequestManager manager = virtualMachine.eventRequestManager();
            if(threadReference != null) {
                clearStepRequests(threadReference);
                StepRequest request = manager.createStepRequest(threadReference, StepRequest.STEP_LINE, StepRequest.STEP_OUT);

                List<String> list = getExclusionList();
                Iterator<String> it = list.iterator();
                while(it.hasNext()) {
                    String next = it.next();
                    request.addClassExclusionFilter(next);
                }
                request.addCountFilter(1);
                request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                request.enable();
                virtualMachine.resume();
            }
        }
    }
    
    @Override
    public void stepOver() {
        synchronized(monitor) {
            EventRequestManager manager = virtualMachine.eventRequestManager();
            if(threadReference != null) {
                clearStepRequests(threadReference);
                StepRequest request = manager.createStepRequest(threadReference, StepRequest.STEP_LINE, StepRequest.STEP_OVER);

                List<String> list = getExclusionList();
                Iterator<String> it = list.iterator();
                while(it.hasNext()) {
                    String next = it.next();
                    request.addClassExclusionFilter(next);
                }
                request.addCountFilter(1);
                request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                request.enable();
                virtualMachine.resume();
            }
        }
    }

    @Override
    public void stepInto() {
        synchronized(monitor) {
            EventRequestManager manager = virtualMachine.eventRequestManager();
            if(threadReference != null) {
                clearStepRequests(threadReference);
                StepRequest request = manager.createStepRequest(threadReference, StepRequest.STEP_LINE, StepRequest.STEP_INTO);

                List<String> list = getExclusionList();
                Iterator<String> it = list.iterator();
                while(it.hasNext()) {
                    String next = it.next();
                    request.addClassExclusionFilter(next);
                }
                request.addCountFilter(1);
                request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                request.enable();
                virtualMachine.resume();
            }
        }
    }

    @Override
    public void stepBackInto() {
    }

    @Override
    public void runForwardToLine(String fullyQualifiedClassName, int line) {
        synchronized(monitor) {
            JDIBreakpoint point = new JDIBreakpoint();
            point.setLine(line);

            JDIClassInformation info = new JDIClassInformation();
            info.setClassName(fullyQualifiedClassName);
            point.setClassInformation(info);

            point.setHasCountFilter(true);
            point.setCountFilter(1);
            add(point);


            if(virtualMachine != null) {
                virtualMachine.resume();
            }
        }
    }

    @Override
    public void runBackToLine(String fullyQualifiedClassName, int line) {
    }

    @Override
    public void backward() {
    }

    @Override
    public void forward() {
        synchronized(monitor) {
            if(virtualMachine != null) {
                virtualMachine.resume();
            }
        }
    }

    /**
     * This method returns an object that is used as a monitor for
     * synchronization of the debugger.
     * 
     * @return 
     */
    @Override
    public Object getResumeMonitor() {
        return monitor;
    }
    
    @Override
    public void launch() {
        if (isRemoteDebugging || (getExecutable() != null && getArguments() != null)) {
            if(isRemoteDebugging) {
                connectToVirtualMachineRemotely();
            } else {
                boolean shouldComputeRaw = checkShouldDoRaw();
                if(!shouldComputeRaw) {
                    connectToVirtualMachineDefaultLaunchingConnector();
                } else {
                    //we have to compute the raw command manually
                    connectToVirtualMachineRaw();
                }
            }
            //if the virtual machine exists, setup the event system with it.
            //this is the same regardless of how we connected
            if(virtualMachine != null) {
                setupEventRequests();

                if(!isRemoteDebugging) { //so far as I can tell, we can't get these remotely
                    inputStream = this.virtualMachine.process().getInputStream();
                    errorStream = this.virtualMachine.process().getErrorStream();
                    outputStream = this.virtualMachine.process().getOutputStream();
                } else {
                    inputStream = null;
                    errorStream = null;
                    outputStream = null;
                }
                
                //this will need to change if other languages are supported
                view = new QuorumView();
                filter = new QuorumFilter();
    
                // Set up the event dispatcher.
                breakpoints.setVirtualMachine(virtualMachine);
                variables = new JDIVariablesModel();
                variables.setDebugger(this);

                //setup the call stack
                callStack = new JDICallStackModel();
                callStack.setDebugger(this);
                callStack.setFilter(filter);

                requestManager = new VirtualMachineRequestManager();
                requestManager.setDebugger(this);
                JDIDebuggerStartEvent start = new JDIDebuggerStartEvent();
                this.requestManager.fireEvent(start);
                requestManager.start();
            } else {
                if(this.isRemoteDebugging) {
                    Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, 
                    "The virtual machine could not be connected to at port: " +
                            this.remotePort + " using transport " + this.remoteTransport
                            + ".");
                } else {
                    Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, 
                    "Could not create virtual machine.");
                }
                
            }
        }
    }
    
    private boolean checkShouldDoRaw() {
        String os = System.getProperty("os.name");
        String version = System.getProperty("os.version");
        
        boolean doRaw = false;
        if(os.compareTo("Mac OS X") == 0) {
            //ok now split the string up
            String[] split = version.split("\\.");
            ArrayList<Integer> list = new ArrayList<Integer>();
            for(int i = 0; i < split.length; i++) {
                list.add(Integer.parseInt(split[i]));
            }
            //version 11 or higher
            if(list.size() >= 2 && list.get(0) == 10 && list.get(1) > 10) {
                doRaw = true;
            } else if(list.size() >= 2 && list.get(0) > 10) {
                doRaw = true;
            }
        }
        
        return doRaw;
    }
    
    private void connectToVirtualMachineDefaultLaunchingConnector() {
        LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();
        Map map = connector.defaultArguments();
        //Connector.Argument home = (Connector.Argument) map.get("home");
        //home.setValue("\"" + home.value() + "");
        //Connector.Argument java = (Connector.Argument) map.get("vmexec");
        //java.setValue(java.value() + "\"");
        Connector.Argument mainArg = (Connector.Argument) map.get("main");
        mainArg.setValue("-jar \"" + executable + "\"");
        Connector.Argument optionsArg = (Connector.Argument) map.get("options");
        String optionsString = "-Duser.dir=\"" + workingDirectory + "\"";

        String os = System.getProperty("os.name");
        if(os.equals("Mac OS X")) {
            optionsString += " -XstartOnFirstThread";
        }
        optionsArg.setValue(optionsString);
        try {
            virtualMachine = connector.launch(map);
        } catch (IOException | IllegalConnectorArgumentsException | VMStartException ex) {
            Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void connectToVirtualMachineRemotely() {
        List<AttachingConnector> attachingConnectors = Bootstrap.virtualMachineManager().attachingConnectors();
        Iterator<AttachingConnector> it = attachingConnectors.iterator();
        boolean notFinished = true;
        while(it.hasNext() && notFinished) {
            AttachingConnector next = it.next();
            if(SOCKET_ATTACH.equals(next.name())) {
                //this is the one we want.
                AttachingConnector socket = next;
                Map map = socket.defaultArguments();
                Connector.Argument port = (Connector.Argument) map.get("port");
                port.setValue("" + this.remotePort);

                Connector.Argument hostname = (Connector.Argument) map.get("hostname");
                hostname.setValue(host);
                try {
                    virtualMachine = socket.attach(map);
                } catch (IOException ex) {
                    Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalConnectorArgumentsException ex) {
                    Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, null, ex);
                }
                notFinished = false;
            }
        }
        if(notFinished) { //no socket connection was found, bail
            Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, 
                    "No SocketAttachingConnector could be found in this virtual machine.");
        }
    }
    
    private void connectToVirtualMachineRaw() {
        List<LaunchingConnector> launchingConnectors = Bootstrap.virtualMachineManager().launchingConnectors();
        Iterator<LaunchingConnector> it = launchingConnectors.iterator();
        LaunchingConnector raw = null;
        LaunchingConnector sun = null;

        while(it.hasNext()) {
            LaunchingConnector next = it.next();
            if(RAW_COMMAND_LINE_LAUNCH.equals(next.name())) {
                //this is the one we want.
                raw = next;

            } else if(COMMAND_LINE_LAUNCH.equals(next.name())) {
                sun = next;
            }
        }

        if(raw != null && sun != null) {
            Map sunMap = sun.defaultArguments();
            
            //hard code the raw host parameters to get around 
            //needing to modify the /etc/hosts file on recent versions of Mac.
            String connectionString = "";
            String host = "127.0.0.1:56070";
            Connector.Argument home = (Connector.Argument) sunMap.get("home");
            connectionString += home.value() + "/bin/java";
            
            String os = System.getProperty("os.name");
            String firstThreadArgument = "";
            if(os.equals("Mac OS X")) 
                firstThreadArgument = " -XstartOnFirstThread";
            
            //-agentlib:jdwp is the current form of -Xrunjdwp. -Xdebug did nothing
            //since Java 6 and is deprecated from JDK 22, so it's left out.
            String primaryCommand = "-Duser.dir=\"" + workingDirectory + "\"" + firstThreadArgument + " -agentlib:jdwp=transport=dt_socket,address=" + host + ",suspend=y";
            String jarCommand = "-jar \"" + executable + "\"";
            
            connectionString += " " + primaryCommand + " " + jarCommand;
            //the format of this string looks something like this now
            //"/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java -Duser.dir= -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:56070,suspend=y -jar /Users/stefika/Desktop/Quorum/Run/Default.jar"
            Map map = raw.defaultArguments();
            Connector.Argument command = (Connector.Argument) map.get("command");
            command.setValue(connectionString);
            Connector.Argument address = (Connector.Argument) map.get("address");
            address.setValue(host);
            try {
                virtualMachine = raw.launch(map);
            } catch (IOException | IllegalConnectorArgumentsException | VMStartException ex) {
                Logger.getLogger(JDIDebugger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * This method sets up default events in the debugger.
     */
    private void setupEventRequests() {
        EventRequestManager manager = virtualMachine.eventRequestManager();
        
        //these extra methods were left in as comments, in case
        //they end up being useful when we turn the debugger omniscient.
        //MethodEntryRequest entry = manager.createMethodEntryRequest();
        //MethodExitRequest exit = manager.createMethodExitRequest();
        ClassPrepareRequest classPrepare = manager.createClassPrepareRequest();
        List<ReferenceType> classes = virtualMachine.classesByName("java.lang.Throwable");
        ExceptionRequest except = null;
        if(!classes.isEmpty()) {
            except = manager.createExceptionRequest(classes.get(0), true, true);
        }
        
        List<String> list = getExclusionList();
        Iterator<String> it = list.iterator();
        while(it.hasNext()) {
            String next = it.next();
            //entry.addClassExclusionFilter(next);
            //exit.addClassExclusionFilter(next);
            classPrepare.addClassExclusionFilter(next);
            if(except != null) {
                except.addClassExclusionFilter(next);
            }
        }
        
        if(except != null) {
            except.enable();
        }
        
        //entry.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        //entry.enable();
        //exit.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        //exit.enable();
        classPrepare.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        classPrepare.enable();
    }
    
    private List<String> getExclusionList() {
        final String JAVA_EXCLUDE = "java.*";
        final String SUN_EXCLUDE = "sun.*";
        final String JAVA_X_EXCLUDE = "javax.*";
        final String COM_APPLE_EXCLUDE = "com.apple.*";
        final String APPLE_EXCLUDE = "apple.*";
        final String SODBEANS_EXCLUDE = "org.sodbeans.*";
        final String COM_SUN_EXCLUDE = "com.sun.*";
        final String QUORUM_PLUGINS_EXCLUDE = "plugins.quorum.*";
        final String GLASSFISH_EXCLUDE = "org.glassfish.*";
        final String APACHE_EXCLUDE = "org.apache.*";
        final String QUORUM_SERVLET = "web.servlet.*";
        final String ANTLR_SERVLET = "antlr.*";
        
        LinkedList<String> list = new LinkedList<>();
        list.add(JAVA_EXCLUDE);
        list.add(SUN_EXCLUDE);
        list.add(JAVA_X_EXCLUDE);
        list.add(COM_APPLE_EXCLUDE);
        list.add(APPLE_EXCLUDE);
        list.add(SODBEANS_EXCLUDE);
        list.add(COM_SUN_EXCLUDE);
        list.add(QUORUM_PLUGINS_EXCLUDE);
        list.add(GLASSFISH_EXCLUDE);
        list.add(APACHE_EXCLUDE);
        list.add(QUORUM_SERVLET);
        list.add(ANTLR_SERVLET);
        
        return list;
    }

    @Override
    public void stop() {
        if(virtualMachine != null) {
            breakpoints.reset();
            inputStream = null;
            outputStream = null;
            errorStream = null;
            variables = null;
            callStack = null;
            view = null;
            filter = null;
            threadReference = null;
            requestManager = null;
            VirtualMachine copy = virtualMachine;
            virtualMachine = null;
            try {
                copy.suspend();
                copy.exit(0);
                copy = null;
                System.gc();
            } catch(Exception exception) {
            }
        }
    }

    @Override
    public void resume() {
        synchronized(getResumeMonitor()) {
            if(virtualMachine != null) {
                virtualMachine.resume();
            }
        }
    }

    @Override
    public void pause() {
        synchronized(getResumeMonitor()) {
            if(virtualMachine != null) {
                virtualMachine.suspend();
            }
        }
    }

    /**
     * @return the arguments
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    /**
     * @return the virtualMachine
     */
    public VirtualMachine getVirtualMachine() {
        return virtualMachine;
    }

    @Override
    public void add(DebuggerListener listener) {
        listeners.put(listener.getName(), listener);
    }

    @Override
    public DebuggerListener remove(DebuggerListener listener) {
        return listeners.remove(listener.getName());
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public Iterator<DebuggerListener> getListeners() {
        return listeners.values().iterator();
    }

    @Override
    public void add(Breakpoint breakpoint) {
        this.breakpoints.add(breakpoint);
    }
    
    /**
     * When a ClassPrepareEvent is fired, this method is executed. Its purpose
     * is to set up watching of fields and breakpoints.
     * 
     * @param event 
     */
    public void action(ClassPrepareEvent event) {
        breakpoints.action(event);
    }

    private void addFieldWatches(ReferenceType type) {
        EventRequestManager manager = virtualMachine.eventRequestManager();
        List<Field> allFields = type.allFields();
        Iterator<Field> fields = allFields.iterator();
        while(fields.hasNext()) {
            Field field = fields.next();
            ModificationWatchpointRequest request = manager.createModificationWatchpointRequest(field);
            request.enable();
        }
    }
    
    @Override
    public Breakpoint remove(Breakpoint breakpoint) {
        Breakpoint bp = breakpoints.remove(breakpoint);
        return bp;
    }

    @Override
    public void clearBreakpoints() {
        breakpoints.clear();
    }

    @Override
    public Iterator<Breakpoint> getBreakpoints() {
        return breakpoints.getBreakpoints();
    }

    @Override
    public boolean isOmniscient() {
        return false;
    }

    @Override
    public void setExecutable(String jar) {
        this.executable = jar;
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    /**
     * @return the threadReference
     */
    public ThreadReference getThreadReference() {
        return threadReference;
    }

    /**
     * @param threadReference the threadReference to set
     */
    public void setThreadReference(ThreadReference threadReference) {
        this.threadReference = threadReference;
        if(variables != null) {
            variables.setThread(threadReference);
            callStack.setThread(threadReference);
        }
    }
    
    @Override
    public VariablesModel getVariablesModel() {
        return variables;
    }

    /**
     * @return the isAlive
     */
    public boolean isAlive() {
        return virtualMachine != null;
    }

    /**
     * @return the view
     */
    public JDIVariableView getView() {
        return view;
    }

    /**
     * @param view the view to set
     */
    public void setView(JDIVariableView view) {
        this.view = view;
    }

    @Override
    public CallStackModel getCallStackModel() {
        return callStack;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return errorStream;
    }

    @Override
    public void setWorkingDirectory(String directory) {
        workingDirectory = directory;
    }
    
    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @return the isRemoteDebugging
     */
    @Override
    public boolean isRemoteDebugging() {
        return isRemoteDebugging;
    }

    /**
     * @param isRemoteDebugging the isRemoteDebugging to set
     */
    @Override
    public void setRemoteDebugging(boolean isRemoteDebugging) {
        this.isRemoteDebugging = isRemoteDebugging;
    }

    /**
     * @return the remotePort
     */
    @Override
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * @param remotePort the remotePort to set
     */
    @Override
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * @return the host
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    @Override
    public void setHost(String host) {
        this.host = host;
    }
}
