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

public class InstallOptionsIcon extends InstallOptionsPicture
{
    public static Image ICON_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("icon.type.small.icon")); //$NON-NLS-1$
    private static Image ICON_IMAGE = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("icon.image")); //$NON-NLS-1$

    /**
     * @param type
     */
    public InstallOptionsIcon()
    {
        super(InstallOptionsModel.TYPE_ICON);
    }

    public Image getIconImage()
    {
        return ICON_ICON;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.model.InstallOptionsPicture#getImageName()
     */
    public Image getImage()
    {
        return ICON_IMAGE;
    }
}
