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

import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.draw2d.geometry.Rectangle;

public abstract class ScrollBarsFigure extends AbstractInstallOptionsFigure
{
    public Rectangle getDirectEditArea()
    {
        Rectangle rect = getClientArea().getCopy();
        if(supportsScrollBars()) {
            rect.width -= (isVScroll()?WinAPI.GetSystemMetrics (WinAPI.SM_CXVSCROLL):0);
            rect.height -= (isHScroll()?WinAPI.GetSystemMetrics (WinAPI.SM_CYHSCROLL):0);
        }
        return rect;
    }
    
    protected abstract boolean supportsScrollBars();
}
