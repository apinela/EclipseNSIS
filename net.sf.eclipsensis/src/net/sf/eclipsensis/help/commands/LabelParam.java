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

import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class LabelParam extends NSISParam
{
    public LabelParam(Node node)
    {
        super(node);
    }

    protected NSISParamEditor createParamEditor()
    {
        return new LabelParamEditor();
    }

    protected class LabelParamEditor extends NSISParamEditor
    {
        protected void appendParamText(StringBuffer buf)
        {
            if(!Common.isEmpty(getName())) {
                buf.append(" ").append(Common.maybeQuote(getName())); //$NON-NLS-1$
            }
        }

        protected Control createParamControl(Composite parent)
        {
            return new Label(parent,SWT.NONE);
        }

        public String validateParam()
        {
            return null;
        }
    }
}
