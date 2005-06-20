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

public class InstallOptionsRadioButton extends InstallOptionsCheckBox
{
    public static Image RADIOBUTTON_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("radiobutton.type.small.icon")); //$NON-NLS-1$
    private static final String[] STATE_DISPLAY = {InstallOptionsPlugin.getResourceString("state.default"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.unselected"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.selected")}; //$NON-NLS-1$

    public InstallOptionsRadioButton()
    {
        this(InstallOptionsModel.TYPE_RADIOBUTTON);
    }

    /**
     * @param type
     */
    protected InstallOptionsRadioButton(String type)
    {
        super(type);
    }

    protected Position getDefaultPosition()
    {
        return new Position(0,0,76,11);
    }

    /**
     * @return
     */
    protected String[] getStateDisplay()
    {
        return STATE_DISPLAY;
    }

    public Image getIconImage()
    {
        return RADIOBUTTON_ICON;
    }

    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("radiobutton.text.default"); //$NON-NLS-1$
    }
}
