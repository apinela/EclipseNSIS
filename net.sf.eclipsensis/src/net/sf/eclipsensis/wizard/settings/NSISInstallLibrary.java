/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallLibraryDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallLibrary extends AbstractNSISInstallItem
{
    private static final long serialVersionUID = -3834188758921066360L;

    public static final String TYPE = "Library"; //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.library.icon")); //$NON-NLS-1$

    private String mName;
    private boolean mShared = true;
    private String mDestination = NSISKeywords.getInstance().getKeyword("$INSTDIR"); //$NON-NLS-1$
    private int mLibType = LIBTYPE_DLL;
    private boolean mProtected = true;
    private boolean mReboot = true;
    private boolean mRemoveOnUninstall = true;
    private boolean mRefreshShell = false;
    private boolean mUnloadLibraries = false;

    static {
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.library.type.name"), IMAGE, NSISInstallLibrary.class); //$NON-NLS-1$
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallLibraryDialog(wizard,this).open() == Window.OK;
    }

    public String getDisplayName()
    {
        return mName;
    }

    public Image getImage()
    {
        return IMAGE;
    }

    public String getType()
    {
        return TYPE;
    }

    public boolean isEditable()
    {
        return true;
    }

    public boolean isRemoveOnUninstall()
    {
        return mRemoveOnUninstall;
    }

    public void setRemoveOnUninstall(boolean removeOnUninstall)
    {
        mRemoveOnUninstall = removeOnUninstall;
    }

    public boolean isRefreshShell()
    {
        return mRefreshShell;
    }

    public void setRefreshShell(boolean refreshShell)
    {
        mRefreshShell = refreshShell;
    }

    public boolean isUnloadLibraries()
    {
        return mUnloadLibraries;
    }

    public void setUnloadLibraries(boolean unloadLibraries)
    {
        mUnloadLibraries = unloadLibraries;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public String getDestination()
    {
        return mDestination;
    }

    public void setDestination(String destination)
    {
        mDestination = destination;
    }

    public boolean isProtected()
    {
        return mProtected;
    }

    public void setProtected(boolean protected1)
    {
        mProtected = protected1;
    }

    public boolean isReboot()
    {
        return mReboot;
    }

    public void setReboot(boolean reboot)
    {
        mReboot = reboot;
    }

    public int getLibType()
    {
        return mLibType;
    }

    public void setLibType(int libType)
    {
        switch(libType) {
            case LIBTYPE_DLL:
            case LIBTYPE_REGDLL:
            case LIBTYPE_TLB:
            case LIBTYPE_REGDLLTLB:
                mLibType = libType;
                break;
            default:
                mLibType = LIBTYPE_DLL;
        }
        mLibType = libType;
    }

    public boolean isShared()
    {
        return mShared;
    }

    public void setShared(boolean shared)
    {
        mShared = shared;
    }

    public String validate(boolean recursive)
    {
        if(!IOUtility.isValidFile(IOUtility.decodePath(getName()))) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.name.error"); //$NON-NLS-1$
        }
        else if(!IOUtility.isValidNSISPathName(getDestination())) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.destination.error"); //$NON-NLS-1$
        }
        else {
            return super.validate(recursive);
        }
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((mDestination == null)?0:mDestination.hashCode());
        result = PRIME * result + mLibType;
        result = PRIME * result + ((mName == null)?0:mName.hashCode());
        result = PRIME * result + (mProtected?1231:1237);
        result = PRIME * result + (mReboot?1231:1237);
        result = PRIME * result + (mRefreshShell?1231:1237);
        result = PRIME * result + (mRemoveOnUninstall?1231:1237);
        result = PRIME * result + (mShared?1231:1237);
        result = PRIME * result + (mUnloadLibraries?1231:1237);
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NSISInstallLibrary other = (NSISInstallLibrary)obj;
        if (mDestination == null) {
            if (other.mDestination != null)
                return false;
        }
        else if (!mDestination.equals(other.mDestination))
            return false;
        if (mLibType != other.mLibType)
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        }
        else if (!mName.equals(other.mName))
            return false;
        if (mProtected != other.mProtected)
            return false;
        if (mReboot != other.mReboot)
            return false;
        if (mRefreshShell != other.mRefreshShell)
            return false;
        if (mRemoveOnUninstall != other.mRemoveOnUninstall)
            return false;
        if (mShared != other.mShared)
            return false;
        if (mUnloadLibraries != other.mUnloadLibraries)
            return false;
        return true;
    }
}
