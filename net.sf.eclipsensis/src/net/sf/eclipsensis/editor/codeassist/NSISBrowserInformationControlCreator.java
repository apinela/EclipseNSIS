/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class NSISBrowserInformationControlCreator extends AbstractNSISInformationControlCreator
{
    public NSISBrowserInformationControlCreator(int style)
    {
        super(style);
    }

    public IInformationControl createInformationControl(Shell parent)
    {
        if(!NSISBrowserInformationControl.isAvailable(parent)) {
            return super.createInformationControl(parent);
        }
        else {
            return new NSISBrowserInformationControl(parent, SWT.RESIZE,mStyle);
        }
    }
}