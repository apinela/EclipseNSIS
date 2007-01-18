/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;

import org.eclipse.swt.graphics.Image;

public class InstallOptionsIcon extends InstallOptionsPicture
{
    private static final Image ICON_IMAGE = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("icon.image")); //$NON-NLS-1$

    protected InstallOptionsIcon(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_ICON;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.model.InstallOptionsPicture#getImageName()
     */
    public Image getImage()
    {
        return ICON_IMAGE;
    }
}
