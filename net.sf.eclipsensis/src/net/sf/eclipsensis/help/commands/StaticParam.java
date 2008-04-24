/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.util.*;

import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class StaticParam extends NSISParam
{
    private String mValue;

    public StaticParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        super.init(node);
        setValue(XMLUtil.getStringValue(node.getAttributes(), ATTR_VALUE,getName()));
    }

    public String getValue()
    {
        return mValue;
    }

    public void setValue(String value)
    {
        mValue = value;
    }

    protected String getDefaultValue()
    {
        return getValue();
    }

    protected NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return new StaticParamEditor(command, parentEditor);
    }

    protected class StaticParamEditor extends NSISParamEditor
    {
        public StaticParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        protected void appendParamText(StringBuffer buf)
        {
            if(!Common.isEmpty(getValue())) {
                buf.append(" ").append(maybeQuote(getValue())); //$NON-NLS-1$
            }
        }

        protected Control createParamControl(Composite parent)
        {
            return null;
        }

        public String validateParam()
        {
            return null;
        }
    }
}
