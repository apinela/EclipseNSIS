/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallShortcutDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISInstallShortcut extends AbstractNSISInstallItem
{
    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.shortcut.type"); //$NON-NLS-1$
    private static final Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.shortcut.icon")); //$NON-NLS-1$
    
    private String mName = null;
    private String mLocation = null;
    private String mUrl = null;
    private String mPath = null;
    private int mShortcutType = OVERWRITE_ON; 

    static {
        NSISInstallElementFactory.register(TYPE, NSISInstallShortcut.class);
    }

    /**
     * @return Returns the path.
     */
    public String getPath()
    {
        return mPath;
    }
    /**
     * @param path The path to set.
     */
    public void setPath(String path)
    {
        mPath = path;
    }
    /**
     * @return Returns the location.
     */
    public String getLocation()
    {
        return mLocation;
    }
    /**
     * @param location The location to set.
     */
    public void setLocation(String location)
    {
        mLocation = location;
    }
    /**
     * @return Returns the uRL.
     */
    public String getUrl()
    {
        return mUrl;
    }
    /**
     * @param url The uRL to set.
     */
    public void setUrl(String url)
    {
        mUrl = url;
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return mName;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(Composite composite)
    {
        return new NSISInstallShortcutDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mName = name;
    }

    /**
     * @return Returns the shortcutType.
     */
    public int getShortcutType()
    {
        return mShortcutType;
    }

    /**
     * @param shortcutType The shortcutType to set.
     */
    public void setShortcutType(int shortcutType)
    {
        mShortcutType = shortcutType;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#setSettings(net.sf.eclipsensis.wizard.settings.NSISWizardSettings)
     */
    public void setSettings(NSISWizardSettings settings)
    {
        super.setSettings(settings);
        if(!Common.isEmpty(settings.getStartMenuGroup()) && Common.isEmpty(mLocation)) {
            mLocation = "$SMPROGRAMS\\"+settings.getStartMenuGroup(); //$NON-NLS-1$
        }
    }
}
