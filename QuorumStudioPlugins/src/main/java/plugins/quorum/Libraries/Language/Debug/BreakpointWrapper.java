/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Language.Debug;

import org.debugger.Breakpoint;
import org.debugger.ClassInformation;
import quorum.Libraries.Language.Debug.Breakpoint_;

/**
 *
 * @author stefika
 */
public class BreakpointWrapper implements Breakpoint, ClassInformation{
    private Breakpoint_ breakpoint = null;
    
    @Override
    public int getLine() {
        return breakpoint.GetLine() + 1;
    }

    @Override
    public ClassInformation getClassInformation() {
        return this;
    }

    @Override
    public String getStaticKey() {
        return breakpoint.GetStaticKey();
    }

    @Override
    public int getCountFilter() {
        return breakpoint.GetCountFilter();
    }

    @Override
    public boolean hasCountFilter() {
        return breakpoint.HasCountFilter();
    }

    @Override
    public String getFullyQualifiedName() {
        return breakpoint.GetFullyQualifiedName();
    }

    @Override
    public String getDotName() {
        return "quorum." + breakpoint.GetDotName();
    }

    /**
     * @return the breakpoint
     */
    public Breakpoint_ getBreakpoint() {
        return breakpoint;
    }

    /**
     * @param breakpoint the breakpoint to set
     */
    public void setBreakpoint(Breakpoint_ breakpoint) {
        this.breakpoint = breakpoint;
    }
    
}
