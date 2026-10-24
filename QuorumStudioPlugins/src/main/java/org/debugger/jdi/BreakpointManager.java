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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.Breakpoint;

/**
 * This class manages breakpoints on the system. Doing so is slightly complex
 * as classes executed through the Java Debugging interface (JDI) cannot
 * cannot be loaded until a class prepare event is received from the system.
 * 
 * @author Andreas Stefik
 */
public class BreakpointManager {
    private HashMap<String, HashMap<Integer, Breakpoint>> unresolved = new HashMap<String, HashMap<Integer, Breakpoint>>();
    private HashMap<String, HashMap<Integer, Breakpoint>> resolved = new HashMap<String, HashMap<Integer, Breakpoint>>();
    private HashMap<String, ReferenceType> loadedTypes = new HashMap<String, ReferenceType>();
    private HashMap<String, BreakpointRequest> breakpointRequests = new HashMap<String, BreakpointRequest>();
    
    /**
     * This is the virtual machine object we are working with.
     */
    private VirtualMachine virtualMachine = null;
    
    /**
     * This method adds a breakpoint to the system. If a class is already loaded
     * then this method loads it into the virtual machine. If no class is loaded
     * then this method puts the breakpoint into an unresolved table.
     * 
     * @param breakpoint 
     */
    public void add(Breakpoint breakpoint) {
        String name = breakpoint.getClassInformation().getDotName();
        if(resolved.containsKey(name)) { //this class has been resolved already
            //therefore we add it to the resolved list and to the virtual machine
            HashMap<Integer, Breakpoint> lines = resolved.get(name);
            if(!lines.containsKey(breakpoint.getLine())) {
                lines.put(breakpoint.getLine(), breakpoint);
                //now add it to the virtual machine
                addToVirtualMachine(breakpoint);
            }
        } else {
            if(unresolved.containsKey(name)) {
                HashMap<Integer, Breakpoint> un = unresolved.get(name);
                un.put(breakpoint.getLine(), breakpoint);
            } else {
                HashMap<Integer, Breakpoint> un = new HashMap<>();
                un.put(breakpoint.getLine(), breakpoint);
                unresolved.put(name, un);
            }
        }
    }
    
    /**
     * This method removes an individual breakpoint from the virtual machine.
     * If the virtual machine has not yet loaded a breakpoint, it is removed
     * from the cache of waiting to be loaded breakpoints.
     * 
     * @param breakpoint
     * @return 
     */
    public Breakpoint remove(Breakpoint breakpoint) {
        String name = breakpoint.getClassInformation().getDotName();
        if(resolved.containsKey(name)) { //this class has been resolved already
            //therefore we add it to the resolved list and to the virtual machine
            HashMap<Integer, Breakpoint> lines = resolved.get(name);
            if(lines.containsKey(breakpoint.getLine())) {
                Breakpoint point = lines.remove(breakpoint.getLine());
                //now add it to the virtual machine
                removeFromVirtualmachine(breakpoint);
                return point;
            }
        } else {
            if(unresolved.containsKey(name)) {
                HashMap<Integer, Breakpoint> lines = unresolved.get(name);
                if(lines.containsKey(breakpoint.getLine())) {
                    Breakpoint point = lines.remove(breakpoint.getLine());
                    return point;
                }
            }
        }
        return null;
    }
    
    private void removeFromVirtualmachine(Breakpoint breakpoint) {
        EventRequestManager manager = virtualMachine.eventRequestManager();
        BreakpointRequest bp = breakpointRequests.remove(breakpoint.getStaticKey());
        if(bp != null) {
            bp.disable();
        }
    }
    
    /**
     * This method removes all breakpoints from the virtual machine.
     */
    public void clear() {
        unresolved.clear();
        resolved.clear();
        Iterator<BreakpointRequest> iterator = breakpointRequests.values().iterator();
        while(iterator.hasNext()) {
            BreakpointRequest bp = iterator.next();
            bp.disable();
        }
        breakpointRequests.clear();
    }
    
    /**
     * This method removes all breakpoints from the virtual machine. Once they
     * are removed, it also clears the types that have been loaded.
     */
    public void reset() {
        unresolved.clear();
        resolved.clear();
        breakpointRequests.clear();
        loadedTypes.clear();
        virtualMachine = null;
    }
    
    /**
     * This method returns a list of all breakpoints currently on the system.
     * Breakpoints are known, but may or may not be actually loaded onto the 
     * virtual machine, depending upon its current state.
     * 
     * @return 
     */
    public Iterator<Breakpoint> getBreakpoints() {
        ArrayList<Breakpoint> array = getBreakpointArray();
        return array.iterator();
    }
    
    /**
     * This method returns a list of all breakpoints currently on the system.
     * Breakpoints are known, but may or may not be actually loaded onto the 
     * virtual machine, depending upon its current state.
     * 
     * @return 
     */
    private ArrayList<Breakpoint> getBreakpointArray() {
        ArrayList<Breakpoint> points = new ArrayList<>();
        Iterator<HashMap<Integer, Breakpoint>> iterator = resolved.values().iterator();
        while(iterator.hasNext()) {
            HashMap<Integer, Breakpoint> next = iterator.next();
            Iterator<Breakpoint> it = next.values().iterator();
            while(it.hasNext()) {
                Breakpoint bp = it.next();
                points.add(bp);
            }
        }
        
        iterator = unresolved.values().iterator();
        while(iterator.hasNext()) {
            HashMap<Integer, Breakpoint> next = iterator.next();
            Iterator<Breakpoint> it = next.values().iterator();
            while(it.hasNext()) {
                Breakpoint bp = it.next();
                points.add(bp);
            }
        }
        
        return points;
    }
    
    /**
     * This method takes a breakpoint object and places it into the virtual
     * machine for processing.
     * 
     * @param breakpoint 
     */
    private void addToVirtualMachine(Breakpoint breakpoint) {
        EventRequestManager manager = virtualMachine.eventRequestManager();
        ReferenceType type = loadedTypes.get(breakpoint.getClassInformation().getDotName());
        if(type != null) {
            try {
                List<Location> locations = type.locationsOfLine(breakpoint.getLine());
                if(locations != null && !locations.isEmpty()) {
                    Location line = locations.get(0);
                    BreakpointRequest vmBreakpoint = manager.createBreakpointRequest(line);
                    if(breakpoint.hasCountFilter()) {
                        vmBreakpoint.addCountFilter(breakpoint.getCountFilter());
                    }
                    vmBreakpoint.enable();
                    breakpointRequests.put(breakpoint.getStaticKey(), vmBreakpoint);
                }
            } catch (AbsentInformationException ex) {
                Logger.getLogger(BreakpointManager.class.getName()).log(Level.INFO, null, ex);
            }
        }
    }
    
    public void action(ClassPrepareEvent event) {
        virtualMachine.suspend();
        ReferenceType type = event.referenceType();
        String name = type.name();
        
        //if this class has not yet been resolved, do so now by moving all
        //breakpoints to the resolved list and setting them up in the virtual machine
        if(!loadedTypes.containsKey(name)) {
            loadedTypes.put(name, type);
        }
        
        //if there are any unresolved breakpoints, handle that now
        if(unresolved.containsKey(name)) {
            HashMap<Integer, Breakpoint> un = unresolved.remove(name);
            resolved.put(name, un);
            
            //now throw them all in the virtual machine, if they are valid
            Iterator<Breakpoint> it = un.values().iterator();
            while(it.hasNext()) {
                Breakpoint next = it.next();
                addToVirtualMachine(next);
            }
        }
        virtualMachine.resume();
    }

    /**
     * @return the virtualMachine
     */
    public VirtualMachine getVirtualMachine() {
        return virtualMachine;
    }

    /**
     * @param virtualMachine the virtualMachine to set
     */
    public void setVirtualMachine(VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
    }
}
