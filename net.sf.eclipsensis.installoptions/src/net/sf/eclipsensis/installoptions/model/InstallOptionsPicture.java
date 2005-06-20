/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class InstallOptionsPicture extends InstallOptionsLabel
{
    public static final String PROPERTY_IMAGE = "Image"; //$NON-NLS-1$
    
    /**
     * @param type
     */
    public InstallOptionsPicture(String type)
    {
        super(type);
    }

    public boolean isMultiLine()
    {
        return false;
    }

    protected String getDefaultText()
    {
        return ""; //$NON-NLS-1$
    }

    protected Position getDefaultPosition()
    {
        Rectangle rect = getImage().getBounds();
        return new Position(0,0,rect.width-1,rect.height-1);
    }
    
    public Object getPropertyValue(Object propName)
    {
        if(PROPERTY_IMAGE.equals(propName)) {
            return getImage();
        }
        else {
            return super.getPropertyValue(propName);
        }
    }
    
    public abstract Image getImage();
}
