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

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSectionDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISSection extends AbstractNSISInstallGroup
{
    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.section.type"); //$NON-NLS-1$

    private static Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.section.icon")); //$NON-NLS-1$
    private static String cFormat = EclipseNSISPlugin.getResourceString("wizard.section.format"); //$NON-NLS-1$
    
    private String mDescription = null;
    private String mName = null;
    private boolean mBold = false;
    private boolean mHidden = false;
    private boolean mDefaultUnselected = false;

    static {
        NSISInstallElementFactory.register(TYPE, NSISSection.class);
    }
    
    public NSISSection()
    {
        super();
        mChildTypes.add(NSISInstallFile.TYPE);
        mChildTypes.add(NSISInstallFiles.TYPE);
        mChildTypes.add(NSISInstallDirectory.TYPE);
        mChildTypes.add(NSISInstallShortcut.TYPE);
        mChildTypes.add(NSISInstallRegistryKey.TYPE);
        mChildTypes.add(NSISInstallRegistryValue.TYPE);
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
        return MessageFormat.format(cFormat,new Object[]{mName}).trim();
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
        return new NSISSectionDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
    }

    /**
     * @return Returns the bold.
     */
    public boolean isBold()
    {
        return mBold;
    }
    /**
     * @param bold The bold to set.
     */
    public void setBold(boolean bold)
    {
        mBold = bold;
    }
    /**
     * @return Returns the defaultUnselected.
     */
    public boolean isDefaultUnselected()
    {
        return mDefaultUnselected;
    }
    /**
     * @param defaultUnselected The defaultUnselected to set.
     */
    public void setDefaultUnselected(boolean defaultUnselected)
    {
        mDefaultUnselected = defaultUnselected;
    }
    /**
     * @return Returns the hidden.
     */
    public boolean isHidden()
    {
        return mHidden;
    }
    /**
     * @param hidden The hidden to set.
     */
    public void setHidden(boolean hidden)
    {
        mHidden = hidden;
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
     * @return Returns the description.
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        mDescription = description;
    }
}