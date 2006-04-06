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

import org.w3c.dom.Node;

public abstract class SimpleParam extends NSISParam
{
    public SimpleParam(Node node)
    {
        super(node);
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
            if(!isSelected()) {
                buf.append(" ").append(maybeQuote(getDefaultValue())); //$NON-NLS-1$
            }
            else {
                appendSimpleParamText(buf);
            }
        }

        protected abstract void appendSimpleParamText(StringBuffer buf);
    }
}

