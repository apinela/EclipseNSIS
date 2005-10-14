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

import org.eclipse.draw2d.Figure;

public abstract class AbstractInstallOptionsFigure extends Figure implements IInstallOptionsFigure
{
    public boolean isTransparent()
    {
        return false;
    }

    public boolean hitTest(int x, int y)
    {
        boolean b = getBounds().contains(x,y);
        if(b && isTransparent()) {
            return !isTransparentAt(x,y);
        }
        return b;
    }
    
    protected boolean isTransparentAt(int x, int y)
    {
        return false;
    }
}
