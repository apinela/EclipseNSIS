/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import org.eclipse.gef.RootEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class ButtonFigure extends SWTControlFigure
{
    private String mText;

    public ButtonFigure(RootEditPart editpart)
    {
        super(editpart);
    }

    public void setText(String text) 
    {
        mText = text;
        if(!isNeedsReScrape()) {
            setNeedsReScrape(true);
        }
        layout();
    }

    /**
     * @return
     */
    protected Control createSWTControl(Composite parent)
    {
        Button button = new Button(parent, SWT.CENTER | SWT.PUSH);
        button.setText(mText);
        return button;
    }
}
