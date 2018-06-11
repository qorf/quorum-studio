/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import java.util.ArrayList;
import org.debugger.jdi.JDIDebugger;
import quorum.Libraries.Containers.Array;
import quorum.Libraries.Containers.Array_;
import quorum.Libraries.Containers.Iterator_;
import quorum.Libraries.Language.Debug.DebuggerListener_;

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
    
    public void RunToCursor(String className, int line) {
        //TODO
    }
    
//    public static String staticKeyToJVMName(String name) {
//        String newName = name.replace('.', '/');
//        if (newName.startsWith("/")) {
//            return "quorum" + newName;
//        }
//        return "quorum/" + newName;
//    }
    
    public void Add(DebuggerListener_ listener) {
        DebuggerListenerWrapper wrapper = new DebuggerListenerWrapper();
        wrapper.setListener(listener);
        listeners.add(wrapper);
    }
    
    public void Remove(DebuggerListener_ listener) {
        for(int i = 0; i < listeners.size(); i++) {
            DebuggerListenerWrapper item = listeners.get(i);
            if(item.getName().compareTo(listener.GetName()) == 0) {
                listeners.remove(i);
                i--;
            }
        }
    }
    
    public DebuggerListener_ GetListener(int index) {
        return listeners.get(index).getListener();
    }
    
    public int GetSize() {
        return listeners.size();
    }
    
    public void Empty() {
        listeners.clear();
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
}
