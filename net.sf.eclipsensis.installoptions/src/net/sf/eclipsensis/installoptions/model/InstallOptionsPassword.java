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

public class InstallOptionsPassword extends InstallOptionsText
{
    private static Image PASSWORD_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("password.type.small.icon")); //$NON-NLS-1$

    /**
     * 
     */
    public InstallOptionsPassword()
    {
        this(InstallOptionsModel.TYPE_PASSWORD);
    }

    /**
     * @param type
     */
    public InstallOptionsPassword(String type)
    {
        super(type);
    }

    public Image getIconImage()
    {
        return PASSWORD_ICON;
    }
    
    protected String getDefaultState()
    {
        return "";
    }
}
