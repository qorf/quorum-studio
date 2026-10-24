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

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.CallStackModel;
import org.debugger.ClassInformation;
import org.debugger.StackFrame;
import org.debugger.VariableColumns;
import org.debugger.filters.LanguageFilter;

/**
 * This class provides information about a stack frame.
 * 
 * @author Andreas Stefik
 */
public class JDICallStackModel implements CallStackModel {
    private JDIDebugger debugger;
    private LanguageFilter filter;
    ThreadReference thread;
    
    @Override
    public Object[] getChildren(StackFrame node, int i, int i1) {
        JDIStackFrame[] result = new JDIStackFrame[0];
        if(node == null && thread != null && thread.status() != ThreadReference.THREAD_STATUS_ZOMBIE &&
            thread.isSuspended()) {
            try {
                List<com.sun.jdi.StackFrame> frames = thread.frames();
                List<JDIStackFrame> convert = convert(frames);
                return convert.toArray();
            } catch (IncompatibleThreadStateException ex) {
                Logger.getLogger(JDICallStackModel.class.getName()).log(Level.SEVERE, null, ex);
                return result;
            }
        } else {
            return result;
        }
    }

    /**
     * This method converts stack frames from the Java Debugging Interface (JDI)
     * to our own stack frame object for passing around.
     * 
     * @param frames
     * @return 
     */
    private List<JDIStackFrame> convert(List<com.sun.jdi.StackFrame> frames) {
        List<JDIStackFrame> list = new LinkedList<>();
        Iterator<com.sun.jdi.StackFrame> iterator = frames.iterator();
        while(iterator.hasNext()) {
            com.sun.jdi.StackFrame jdiFrame = iterator.next();
            JDIStackFrame frame = new JDIStackFrame();
            frame.setMethodName(jdiFrame.location().method().name());
            ClassInformation ci = getClassInformation(jdiFrame);
            frame.setClassInformation(ci);
            frame.setLine(jdiFrame.location().lineNumber());
            if(filter.isValidStackFrame(frame)) {
                list.add(frame);
            }
        }
        
        if(!list.isEmpty()) {
            list.get(0).setCurrent(true);
        }
        return list;
    }
    
    /**
     * This method converts a stack frame into a ClassInformation object, which
     * stores the name of the class the frame is contained in.
     * 
     * @param frame
     * @return 
     */
    private ClassInformation getClassInformation(com.sun.jdi.StackFrame frame) {
        ReferenceType type = frame.location().declaringType();
        JDIClassInformation info = new JDIClassInformation();
        info.setClassName(type.name().replace('.', '/'));
        return info;
    }
    
    @Override
    public boolean isLeaf(StackFrame node) {
        if(node == null && thread != null && thread.status() != ThreadReference.THREAD_STATUS_ZOMBIE) {
            try {
                List<com.sun.jdi.StackFrame> frames = thread.frames();
                List<JDIStackFrame> convert = convert(frames);
                return convert.isEmpty();
            } catch (IncompatibleThreadStateException ex) {
                Logger.getLogger(JDICallStackModel.class.getName()).log(Level.SEVERE, null, ex);
                return true;
            }
        } else {
            return true; //stack frames do not have any children currently.
        }
    }

    @Override
    public int getChildrenCount(StackFrame node) {
        if(node == null && thread != null && thread.status() != ThreadReference.THREAD_STATUS_ZOMBIE) {
            try {
                List<com.sun.jdi.StackFrame> frames = thread.frames();
                List<JDIStackFrame> convert = convert(frames);
                return convert.size();
            } catch (IncompatibleThreadStateException ex) {
                Logger.getLogger(JDICallStackModel.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        } else {
            return 0; //stack frames do not have any children currently.
        }
    }

    @Override
    public String getDisplayName(StackFrame node) {
        if(node == null) {
            return "";
        } else {
            return node.getMethodName() + ":" + node.getLine();
        }
    }

    @Override
    public String getShortDescription(StackFrame node) {
        return "";
    }

    @Override
    public boolean isReadOnly(StackFrame node, VariableColumns column) {
        return true;
    }

    @Override
    public Object getValueAt(StackFrame node, VariableColumns column) {
        if(node == null) {
            return "";
        } else {
            return node.getMethodName() + ":" + node.getLine();
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
    }

    /**
     * @return the thread
     */
    public ThreadReference getThread() {
        return thread;
    }

    /**
     * @param thread the thread to set
     */
    public void setThread(ThreadReference thread) {
        this.thread = thread;
    }
    
    /**
     * @return the filter
     */
    public LanguageFilter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(LanguageFilter filter) {
        this.filter = filter;
    }
}
