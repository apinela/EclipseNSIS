/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.List;

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.Node;

public abstract class SimpleParam extends NSISParam
{
    public static final String ATTR_INCLUDE_PREVIOUS = "includePrevious"; //$NON-NLS-1$

    private boolean mIncludePrevious;

    public SimpleParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        super.init(node);
        setIncludePrevious(XMLUtil.getBooleanValue(node.getAttributes(), ATTR_INCLUDE_PREVIOUS));
    }

    protected boolean isIncludePrevious()
    {
        return mIncludePrevious;
    }

    protected void setIncludePrevious(boolean includePrevious)
    {
        mIncludePrevious = includePrevious;
    }
    
    protected final NSISParamEditor createParamEditor(INSISParamEditor parentEditor)
    {
        return createSimpleParamEditor(parentEditor);
    }

    protected abstract String getDefaultValue();
    protected abstract SimpleParamEditor createSimpleParamEditor(INSISParamEditor parentEditor);

    protected abstract class SimpleParamEditor extends NSISParamEditor
    {
        public SimpleParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        protected final void appendParamText(StringBuffer buf)
        {
            if(isOptional() && isIncludePrevious()) {
                INSISParamEditor parentEditor = getParentEditor();
                if(parentEditor != null) {
                    List children = parentEditor.getChildEditors();
                    if(!Common.isEmptyCollection(children)) {
                        int n = children.indexOf(this);
                        if(n > 0) {
                            INSISParamEditor child = (INSISParamEditor)children.get(n-1);
                            if(child instanceof SimpleParamEditor && !child.isSelected()) {
                                ((SimpleParamEditor)child).appendParamText(buf);
                            }
                        }
                    }
                }
            }
            if(!isSelected()) {
                buf.append(" ").append(maybeQuote(getDefaultValue())); //$NON-NLS-1$
            }
            else {
                appendSimpleParamText(buf);
            }
        }

        protected String internalValidate()
        {
            String error = super.internalValidate();
            if(error == null) {
                if(isOptional() && isIncludePrevious()) {
                    INSISParamEditor parentEditor = getParentEditor();
                    if(parentEditor != null) {
                        List children = parentEditor.getChildEditors();
                        if(!Common.isEmptyCollection(children)) {
                            int n = children.indexOf(this);
                            if(n > 1) {
                                INSISParamEditor child = (INSISParamEditor)children.get(n-1);
                                if(child instanceof SimpleParamEditor && !child.isSelected()) {
                                    return ((SimpleParamEditor)child).internalValidate();
                                }
                            }
                        }
                    }
                }
            }
            return error;
        }

        protected abstract void appendSimpleParamText(StringBuffer buf);
    }
}

