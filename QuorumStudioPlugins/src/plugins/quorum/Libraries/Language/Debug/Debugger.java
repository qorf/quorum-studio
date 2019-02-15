/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.debugger.jdi.JDIDebugger;
import quorum.Libraries.Containers.Array;
import quorum.Libraries.Containers.Array_;
import quorum.Libraries.Containers.Iterator_;
import quorum.Libraries.Language.Debug.Breakpoint_;
import quorum.Libraries.Language.Debug.DebuggerListener_;
import quorum.Libraries.Language.Debug.VariablesModel_;

/**
 *
 * @author stefika
 */
public class Debugger {
    public java.lang.Object me_ = null;
    private JDIDebugger debugger;
    String location = "";
    String workingDirectory = "";
    ArrayList<DebuggerListenerWrapper> listeners = new ArrayList<>();
    HashMap<String, BreakpointWrapper> breakpoints = new HashMap<>();
    quorum.Libraries.Language.Debug.VariablesModel model = new quorum.Libraries.Language.Debug.VariablesModel();
    quorum.Libraries.Language.Debug.CallStackModel callStackModel = new quorum.Libraries.Language.Debug.CallStackModel();
    
    public void RunToCursor(String className, int line) {
        if(debugger != null) {
            debugger.runForwardToLine("quorum." + className, line);
        }
    }
    
    public void Toggle(Breakpoint_ breakpoint) {
        if(breakpoints.containsKey(breakpoint.GetStaticKey())) {
            Remove(breakpoint);
        } else {
            Add(breakpoint);
        }
    }
    
    public void Add(Breakpoint_ breakpoint) {
        BreakpointWrapper wrapper = new BreakpointWrapper();
        wrapper.setBreakpoint(breakpoint);
        breakpoints.put(breakpoint.GetStaticKey(), wrapper);
        if(debugger != null) {
            debugger.add(wrapper);
        }
    }
    
    public void Remove(Breakpoint_ breakpoint) {
        if(breakpoints.containsKey(breakpoint.GetStaticKey())) {
            BreakpointWrapper remove = breakpoints.remove(breakpoint.GetStaticKey());
            if(debugger != null) {
                debugger.remove(remove);
            }
        }
    }
    
    public Iterator_ GetBreakpoints() {
        Array_ array = new Array();
        Iterator<BreakpointWrapper> iterator = breakpoints.values().iterator();
        while(iterator.hasNext()) {
            BreakpointWrapper next = iterator.next();
            Breakpoint_ nonwrapped = next.getBreakpoint();
            array.Add(nonwrapped);
        }
        return array.GetIterator();
    }
    
    public int GetBreakpointsSize() {
        return listeners.size();
    }
    
    public void EmptyBreakpoints() {
        breakpoints.clear();
        if(debugger != null) {
            debugger.clearBreakpoints();
        }
    }
    
    public Breakpoint_ GetBreakpoint(String key) {
        if(breakpoints.containsKey(key)) {
            return breakpoints.get(key).getBreakpoint();
        }
        return null;
    }
    
    public void Add(DebuggerListener_ listener) {
        DebuggerListenerWrapper wrapper = new DebuggerListenerWrapper();
        wrapper.setListener(listener);
        listeners.add(wrapper);
        if(debugger != null) {
            debugger.add(wrapper);
        }
    }
    
    public void Remove(DebuggerListener_ listener) {
        for(int i = 0; i < listeners.size(); i++) {
            DebuggerListenerWrapper item = listeners.get(i);
            if(item.getName().compareTo(listener.GetName()) == 0) {
                listeners.remove(i);
                debugger.remove(item);
                i--;
            }
        }
    }
    
    public DebuggerListener_ GetListener(int index) {
        return listeners.get(index).getListener();
    }
    
    public int GetListenersSize() {
        return listeners.size();
    }
    
    public void EmptyListeners() {
        listeners.clear();
        if(debugger != null) {
            debugger.clearListeners();
        }
    }
    
    public Iterator_ GetListeners() {
        Array_ array = new Array();
        for(int i = 0; i < listeners.size(); i++) {
            DebuggerListenerWrapper item = listeners.get(i);
            DebuggerListener_ nonwrapped = item.getListener();
            array.Add(nonwrapped);
        }
        return array.GetIterator();
    }
    
    public void SetExecutable(String file) {
        location = file;
    }
    
    public void SetWorkingDirectory(String wd) {
        this.workingDirectory = wd;
    }
    
    public void Start() {
        if(debugger != null) {
            debugger.stop();
            debugger = null;
        }
        
        debugger = new JDIDebugger();
        for(int i = 0; i < listeners.size(); i++) {
            DebuggerListenerWrapper item = listeners.get(i);
            debugger.add(item);
        }
        Iterator<BreakpointWrapper> iterator = breakpoints.values().iterator();
        while(iterator.hasNext()) {
            BreakpointWrapper next = iterator.next();
            debugger.add(next);
        }
        
        debugger.setExecutable(location);
        debugger.setWorkingDirectory(workingDirectory);
        debugger.launch();
    }
    
    public void Stop() {
        if(debugger != null) {
            debugger.stop();
            debugger = null;
        }
    }
    
    public void Pause() {
        if(debugger != null) {
            debugger.pause();
        }
    }
    
    public void Continue() {
        if(debugger != null) {
            debugger.forward();
        }
    }
    
    public void StepOver() {
        if(debugger != null) {
            debugger.stepOver();
        }
    }
    
    public void StepInto() {
        if(debugger != null) {
            debugger.stepInto();
        }
    }
    
    public void StepOut() {
        if(debugger != null) {
            debugger.stepOut();
        }
    }
    
    public VariablesModel_ GetVariablesModel() {
        model.plugin_.setDebugger(debugger);
        return model;
    }
//    
//    public CallStackModel_ GetCallStackModel() {
//        
//    }
}
