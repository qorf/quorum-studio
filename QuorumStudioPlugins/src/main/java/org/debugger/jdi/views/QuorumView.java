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
package org.debugger.jdi.views;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.debugger.Variable;
import org.debugger.jdi.JDIVariable;
import org.debugger.jdi.views.quorum.ArrayView;
import org.debugger.jdi.views.quorum.BooleanView;
import org.debugger.jdi.views.quorum.DateTimeView;
import org.debugger.jdi.views.quorum.FileView;
import org.debugger.jdi.views.quorum.HashNodeView;
import org.debugger.jdi.views.quorum.HashView;
import org.debugger.jdi.views.quorum.Integer32BitArrayView;
import org.debugger.jdi.views.quorum.Integer64BitArrayView;
import org.debugger.jdi.views.quorum.IntegerView;
import org.debugger.jdi.views.quorum.ListView;
import org.debugger.jdi.views.quorum.Number32BitArrayView;
import org.debugger.jdi.views.quorum.Number64BitArrayView;
import org.debugger.jdi.views.quorum.NumberView;
import org.debugger.jdi.views.quorum.PrimitiveView;
import org.debugger.jdi.views.quorum.QuorumVariableView;
import org.debugger.jdi.views.quorum.StackView;
import org.debugger.jdi.views.quorum.TextObjectView;
import org.debugger.jdi.views.quorum.TextView;

/**
 * This class represents naming conventions for the Quorum programming language
 * inside of a variable window.
 *
 * @author Andreas Stefik
 */
public class QuorumView extends JDIVariableView {

    private HashMap<String, QuorumVariableView> views = new HashMap<String, QuorumVariableView>();

    public QuorumView() {
        QuorumVariableView view = new ArrayView();
        setupView(view);
        
        view = new Integer32BitArrayView();
        setupView(view);
        
        view = new Integer64BitArrayView();
        setupView(view);
        
        view = new Number32BitArrayView();
        setupView(view);
        
        view = new Number64BitArrayView();
        setupView(view);
        
        view = new ListView();
        setupView(view);
        
        view = new StackView();
        setupView(view);
        
        view = new HashView();
        setupView(view);
        
        view = new HashNodeView();
        setupView(view);
        
        view = new FileView();
        setupView(view);
        
        view = new DateTimeView();
        setupView(view);
        
        view = new IntegerView();
        setupView(view);
        
        view = new TextView();
        setupView(view);
        
        view = new TextObjectView();
        setupView(view);
        
        view = new NumberView();
        setupView(view);
        
        view = new BooleanView();
        setupView(view);
    }

    private void setupView(QuorumVariableView view) {
        views.put(view.getStaticKey(), view);
        view.setQuorumView(this);
    } 
    
    @Override
    public String getThis() {
        return "me";
    }

    @Override
    public String getIntPrimitive() {
        return "integer";
    }

    @Override
    public String getLongPrimitive() {
        return "integer";
    }

    @Override
    public String getBooleanPrimitive() {
        return "boolean";
    }

    @Override
    public String getFloatPrimitive() {
        return "number";
    }

    @Override
    public String getDoublePrimitive() {
        return "number";
    }

    @Override
    public String getString() {
        return "text";
    }

    @Override
    public String getObjectName(String name) {
        if (name.compareTo("hidden_") == 0) {
            return "Hidden";
        }
        QuorumVariableView view = views.get(name);
        if(view != null) {
            return view.getObjectName();
        }

        //by default, chop off the first dot values.
        String[] split = name.split("\\.");
        String newName = "";
        for (int i = 1; i < split.length; i++) {
            newName += split[i] + ".";
        }
        newName = newName.substring(0, newName.length() - 1);

        final String inter = "_";
        if (newName.endsWith(inter)) {
            newName = newName.substring(0, newName.length() - inter.length());
        }
        return newName;
    }

    /**
     * This method translates a variable name using special symbols into a
     * reasonable name. For Quorum, the only change is for parent variables.
     *
     * @param name
     * @return
     */
    public String getVariableName(String name) {
        if (name.endsWith("__")) {
            name = name.substring(0, name.length() - 2);
            name = name.replace('_', '.');
            name = "parent:" + name;
        }
        return name;
    }

    @Override
    public String getNull() {
        return "undefined";
    }

    /**
     * Boxed values, such as a Quorum Integer or Text object, are shown like
     * the primitives they hold, so they have no children.
     *
     * @param variable
     */
    @Override
    public void convertTypeName(JDIVariable variable) {
        String original = variable.getTypeName();
        super.convertTypeName(variable);
        //a field's declared type may be the class's interface, such as Integer_
        if (original.endsWith("_")) {
            original = original.substring(0, original.length() - 1);
        }
        if (views.get(original) instanceof PrimitiveView) {
            variable.setPrimitive(true);
        }
    }

    /**
     * Text is shown in quotes, with line breaks and tabs written out, so it
     * fits on one line and can't be mistaken for undefined.
     *
     * @param value
     * @return
     */
    public static String quote(String value) {
        String result = value.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return "\"" + result + "\"";
    }

    /**
     * This returns how a value is shown when it has no view of its own:
     * text in quotes, and anything else as JDI writes it.
     *
     * @param value
     * @return
     */
    public static String toDisplay(Value value) {
        if (value instanceof StringReference) {
            return quote(((StringReference) value).value());
        }
        return value.toString();
    }

    @Override
    public String getValue(Value value) {
        if (value instanceof ObjectReference) {
            ObjectReference ref = (ObjectReference) value;
            String name = ref.referenceType().name();
            QuorumVariableView view = views.get(name);
            if (view != null) {
                view.setThread(this.getThread());
                view.setMachine(this.getMachine());
                return view.getValue(value);
            }
            if (name.compareTo("java.lang.String") == 0) {
                return toDisplay(value);
            }
            return "#" + ref.hashCode();
        } else if (value == null) {
            return getNull();
        } else {
            return value.toString();
        }
    }

    @Override
    public boolean isSpecialVariable(ObjectReference reference) {
        ReferenceType type = reference.referenceType();
        if (views.containsKey(type.name())) {
            return true;
        }
        return false;
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference) {
        return getSpecialVariableChildren(reference, 0, 0);
    }

    @Override
    public Variable[] getSpecialVariableChildren(ObjectReference reference, int from, int to) {
        QuorumVariableView view = getView(reference);
        if (view != null) {
            return view.getSpecialVariableChildren(reference, from, to);
        }
        return new Variable[0];
    }

    @Override
    public int getSpecialVariableCount(ObjectReference reference) {
        QuorumVariableView view = getView(reference);
        if (view != null) {
            return view.getElementCount(reference);
        }
        return 0;
    }

    /**
     * This returns the view for a value's type, ready to read it, or null if
     * it has none.
     *
     * @param reference
     * @return
     */
    private QuorumVariableView getView(ObjectReference reference) {
        ReferenceType type = reference.referenceType();
        QuorumVariableView view = views.get(type.name());
        if (view != null) {
            view.setThread(this.getThread());
            view.setMachine(this.getMachine());
        }
        return view;
    }

    public JDIVariable convert(Value value) {
        JDIVariable var = new JDIVariable();
        var.setName("");
        if (value != null) {
            var.setTypeName(value.type().name());
            convertTypeName(var);
            var.setValue(this.getValue(value));
        } else {
            var.setValue(this.getNull());
        }
        var.setObjectReference(value);
        return var;
    }
}
