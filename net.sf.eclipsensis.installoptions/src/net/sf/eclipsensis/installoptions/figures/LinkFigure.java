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

import net.sf.eclipsensis.installoptions.model.InstallOptionsLink;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LinkFigure extends LabelFigure
{
    private RGB mTxtColor = null;
    /**
     * @param editPart
     */
    public LinkFigure(GraphicalEditPart editPart)
    {
        super(editPart);
    }
    
    public RGB getTxtColor()
    {
        return mTxtColor;
    }

    public void setTxtColor(RGB txtColor)
    {
        mTxtColor = txtColor;
    }

    /**
     * @return
     */
    protected Control createSWTControl(Composite parent)
    {
        Control label = super.createSWTControl(parent);
        final Color color = new Color(label.getDisplay(),(mTxtColor==null?InstallOptionsLink.DEFAULT_TXTCOLOR:mTxtColor));
        label.setForeground(color);
        label.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                color.dispose();
            }
        });
        return label;
    }

    /**
     * @return
     */
    public int getDefaultStyle()
    {
        return SWT.LEFT;
    }
}
