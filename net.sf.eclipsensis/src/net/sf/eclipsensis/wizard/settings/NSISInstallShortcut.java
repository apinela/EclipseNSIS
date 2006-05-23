/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallShortcutDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallShortcut extends AbstractNSISInstallItem
{
	private static final long serialVersionUID = 7567273788917909918L;
    public static final String STARTMENUGROUP_SHORTCUT_LOCATION;

    public static final String TYPE = "Shortcut"; //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.shortcut.icon")); //$NON-NLS-1$

    private boolean mCreateInStartMenuGroup = false;
    private String mName = null;
    private String mLocation = null;
    private String mUrl = null;
    private String mPath = null;
    private int mShortcutType = SHORTCUT_URL;

    static {
        STARTMENUGROUP_SHORTCUT_LOCATION = EclipseNSISPlugin.getResourceString("wizard.smgroup.shortcut.location","$SMPROGRAMS\\$StartMenuGroup"); //$NON-NLS-1$ //$NON-NLS-2$
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.shortcut.type.name"), IMAGE, NSISInstallShortcut.class); //$NON-NLS-1$
    }

    public boolean isCreateInStartMenuGroup()
    {
        return mCreateInStartMenuGroup;
    }

    public void setCreateInStartMenuGroup(boolean createInStartMenuGroup)
    {
        mCreateInStartMenuGroup = createInStartMenuGroup;
        if(mCreateInStartMenuGroup) {
            mLocation = ""; //$NON-NLS-1$
        }
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
        if(mLocation != null && mLocation.startsWith(STARTMENUGROUP_SHORTCUT_LOCATION)) {
            mCreateInStartMenuGroup = true;
            mLocation = ""; //$NON-NLS-1$
        }
        return mLocation;
    }

    /**
     * @param location The location to set.
     */
    public void setLocation(String location)
    {
        if(location.startsWith(STARTMENUGROUP_SHORTCUT_LOCATION)) {
            mCreateInStartMenuGroup = true;
            mLocation = ""; //$NON-NLS-1$
        }
        else {
            mCreateInStartMenuGroup = false;
            mLocation = location;
        }
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

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallShortcutDialog(wizard,this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
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
        if(settings != null && !Common.isEmpty(getSettings().getStartMenuGroup()) && Common.isEmpty(mLocation)) {
            mCreateInStartMenuGroup = true;
        }
    }
    public String validate(boolean recursive)
    {
        if(isCreateInStartMenuGroup()) {
            if(!getSettings().isCreateStartMenuGroup()) {
                return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.no.smgroup.error"); //$NON-NLS-1$
            }
        }
        else if(!IOUtility.isValidNSISPathName(getLocation())) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.location.error"); //$NON-NLS-1$
        }
        if(!IOUtility.isValidFileName(getName())) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.name.error"); //$NON-NLS-1$
        }
        int n = getShortcutType();
        if((n == SHORTCUT_INSTALLELEMENT && !IOUtility.isValidNSISPathName(getPath()))) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.file.error"); //$NON-NLS-1$
        }
        else if((n == SHORTCUT_URL && !IOUtility.isValidURL(getUrl()))) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.url.error"); //$NON-NLS-1$
        }
        else {
            return super.validate(recursive);
        }
    }
}
