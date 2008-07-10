/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;


import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class AbstractNSISInformationControlCreator implements IInformationControlCreator,INSISConstants
{
    protected int mStyle = SWT.NONE;

    protected NSISInformationControl.IInformationPresenter mInformationPresenter = createInformationPresenter();

    public AbstractNSISInformationControlCreator(int style)
    {
        mStyle = style;
    }

    public IInformationControl createInformationControl(Shell parent)
    {
        return new NSISInformationControl(parent,mStyle,mInformationPresenter);
    }

    protected NSISInformationControl.IInformationPresenter createInformationPresenter()
    {
        return null;
    }
}