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

import org.eclipse.gef.GraphicalEditPart;

public abstract class EditableElementFigure extends SWTControlFigure implements IEditableElementFigure
{
    protected String mState = ""; //$NON-NLS-1$

    /**
     * @param editPart
     */
    public EditableElementFigure(GraphicalEditPart editPart)
    {
        super(editPart);
    }
    
    public String getState()
    {
        return mState;
    }
    
    public void setState(String state) 
    {
        mState = state;
    }
}
