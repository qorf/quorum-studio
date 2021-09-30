/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import org.debugger.DebuggerListener;
import org.debugger.events.DebuggerBreakpointEvent;
import org.debugger.events.DebuggerExceptionEvent;
import org.debugger.events.DebuggerStartEvent;
import org.debugger.events.DebuggerStepEvent;
import org.debugger.events.DebuggerStopEvent;
import quorum.Libraries.Language.Debug.Breakpoint;
import quorum.Libraries.Language.Debug.BreakpointEvent_;
import quorum.Libraries.Language.Debug.Breakpoint_;
import quorum.Libraries.Language.Debug.DebuggerErrorEvent_;
import quorum.Libraries.Language.Debug.DebuggerListener_;
import quorum.Libraries.Language.Debug.DebuggerStartEvent_;
import quorum.Libraries.Language.Debug.DebuggerStepEvent_;
import quorum.Libraries.Language.Debug.DebuggerStopEvent_;
import quorum.Libraries.System.File;
import quorum.Libraries.System.File_;

/**
 *
 * @author stefika
 */
public class DebuggerListenerWrapper implements DebuggerListener{
    private DebuggerListener_ listener;
    private DebuggerStartEvent_ start = new quorum.Libraries.Language.Debug.DebuggerStartEvent();
    private DebuggerStopEvent_ stop = new quorum.Libraries.Language.Debug.DebuggerStopEvent();
    private DebuggerErrorEvent_ error = new quorum.Libraries.Language.Debug.DebuggerErrorEvent();
    
    private static final String QUORUM = "quorum.";
    @Override
    public void accept(DebuggerStartEvent event) {
        listener.Run(start);
    }

    @Override
    public void accept(DebuggerStopEvent event) {
        listener.Run(stop);
    }

    @Override
    public void accept(DebuggerStepEvent event) {
        DebuggerStepEvent_ step = new quorum.Libraries.Language.Debug.DebuggerStepEvent();
        String source = event.getSource();
        int line = event.getLine();
        step.SetClassName(source.substring(QUORUM.length()));
        step.SetLine(line);
        listener.Run(step);
    }

    @Override
    public void accept(DebuggerExceptionEvent event) {
        listener.Run(error);
    }

    @Override
    public void accept(DebuggerBreakpointEvent event) {
        BreakpointEvent_ be = new quorum.Libraries.Language.Debug.BreakpointEvent();
        String path = event.getSource();
        int line = event.getLine();
        Breakpoint_ breakpoint = new Breakpoint();
        breakpoint.SetLine(line);
        if(path != null && path.length() > 7) { //remove the quorum. at the beginning
            breakpoint.SetDotName(path.substring(7));
        }
        be.SetBreakpoint(breakpoint);
        listener.Run(be);
    }

    @Override
    public String getName() {
        return listener.GetName();
    }

    /**
     * @return the listener
     */
    public DebuggerListener_ getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(DebuggerListener_ listener) {
        this.listener = listener;
    }
}
