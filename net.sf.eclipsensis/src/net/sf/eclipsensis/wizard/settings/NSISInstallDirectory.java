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
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallDirectoryDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISInstallDirectory extends AbstractNSISInstallItem
{
    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.directory.type"); //$NON-NLS-1$
    private static final Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.directory.icon")); //$NON-NLS-1$
    
    private String mName = null;
    private boolean mIsRecursive = true;
    private String mDestination = "$INSTDIR"; //$NON-NLS-1$
    private int mOverwriteMode = OVERWRITE_ON;
    private boolean mCopyFolderContents = false;
    
    static {
        NSISInstallElementFactory.register(TYPE, NSISInstallDirectory.class);
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
        return mName+(mCopyFolderContents?"\\*.*":""); //$NON-NLS-1$ //$NON-NLS-2$
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
        return new NSISInstallDirectoryDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
    }

    /**
     * @return Returns the destination.
     */
    public String getDestination()
    {
        return mDestination;
    }

    /**
     * @param destination The destination to set.
     */
    public void setDestination(String destination)
    {
        mDestination = destination;
    }
    
    /**
     * @return Returns the isRecursive.
     */
    public boolean isRecursive()
    {
        return mIsRecursive;
    }

    /**
     * @param isRecursive The isRecursive to set.
     */
    public void setRecursive(boolean isRecursive)
    {
        mIsRecursive = isRecursive;
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
     * @return Returns the overwriteMode.
     */
    public int getOverwriteMode()
    {
        return mOverwriteMode;
    }

    /**
     * @param overwriteMode The overwriteMode to set.
     */
    public void setOverwriteMode(int overwriteMode)
    {
        mOverwriteMode = overwriteMode;
    }
    
    /**
     * @return Returns the copyFolderContents.
     */
    public boolean isCopyFolderContents()
    {
        return mCopyFolderContents;
    }
    
    /**
     * @param copyFolderContents The copyWithFolderName to set.
     */
    public void setCopyFolderContents(boolean copyFolderContents)
    {
        mCopyFolderContents = copyFolderContents;
    }
}
