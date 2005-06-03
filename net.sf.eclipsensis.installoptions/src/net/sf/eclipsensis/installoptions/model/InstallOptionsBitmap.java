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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.swt.graphics.Image;

public class InstallOptionsBitmap extends InstallOptionsPicture
{
    private static Image BITMAP_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("bitmap.type.small.icon")); //$NON-NLS-1$
    private static Image BITMAP_IMAGE = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("bitmap.image")); //$NON-NLS-1$

    /**
     * @param type
     */
    public InstallOptionsBitmap()
    {
        super(InstallOptionsModel.TYPE_BITMAP);
    }

    public Image getIconImage()
    {
        return BITMAP_ICON;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.model.InstallOptionsPicture#getImageName()
     */
    public Image getImage()
    {
        return BITMAP_IMAGE;
    }
}
