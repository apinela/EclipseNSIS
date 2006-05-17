/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.properties.tabbed.section.ButtonPropertySectionCreator;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.IPropertySectionCreator;

public class InstallOptionsButton extends InstallOptionsGenericButton
{
    protected InstallOptionsButton(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_BUTTON;
    }

    /**
     * @return
     */
    protected Position getDefaultPosition()
    {
        return new Position(0,0,50,15);
    }

    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("button.text.default"); //$NON-NLS-1$
    }

    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new ButtonPropertySectionCreator(this);
    }

    public boolean usesOtherTab()
    {
        return false;
    }
}
