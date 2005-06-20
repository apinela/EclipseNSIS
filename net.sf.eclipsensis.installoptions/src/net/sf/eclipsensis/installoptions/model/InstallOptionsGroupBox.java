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

public class InstallOptionsGroupBox extends InstallOptionsUneditableElement
{
    public static Image GROUPBOX_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("groupbox.type.small.icon")); //$NON-NLS-1$

    public InstallOptionsGroupBox()
    {
        this(InstallOptionsModel.TYPE_GROUPBOX);
    }

    protected InstallOptionsGroupBox(String type)
    {
        super(type);
    }

    /**
     * @return
     */
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("groupbox.text.default"); //$NON-NLS-1$
    }

    /**
     * @return
     */
    protected Position getDefaultPosition()
    {
        return new Position(0,0,124,64);
    }

    public Image getIconImage()
    {
        return GROUPBOX_ICON;
    }
}
