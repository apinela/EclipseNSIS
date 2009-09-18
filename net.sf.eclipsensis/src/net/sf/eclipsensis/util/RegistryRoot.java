/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 ******************************************************************************/
package net.sf.eclipsensis.util;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.swt.graphics.Image;

public class RegistryRoot extends RegistryKey
{
    private static final Image REGROOT_IMAGE;
    static final int[] ROOT_KEYS = {WinAPI.HKEY_CLASSES_ROOT, WinAPI.HKEY_CURRENT_USER, WinAPI.HKEY_LOCAL_MACHINE, WinAPI.HKEY_USERS, WinAPI.HKEY_CURRENT_CONFIG};

    static {
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        REGROOT_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.root.image")); //$NON-NLS-1$
    }

    public static String getRootKeyName(int rootKey)
    {
        switch (rootKey)
        {
            case WinAPI.HKEY_CLASSES_ROOT:
                return "HKEY_CLASSES_ROOT"; //$NON-NLS-1$
            case WinAPI.HKEY_CURRENT_CONFIG:
                return "HKEY_CURRENT_CONFIG"; //$NON-NLS-1$
            case WinAPI.HKEY_CURRENT_USER:
                return "HKEY_CURRENT_USER"; //$NON-NLS-1$
            case WinAPI.HKEY_DYN_DATA:
                return "HKEY_DYN_DATA"; //$NON-NLS-1$
            case WinAPI.HKEY_LOCAL_MACHINE:
                return "HKEY_LOCAL_MACHINE"; //$NON-NLS-1$
            case WinAPI.HKEY_PERFORMANCE_DATA:
                return "HKEY_PERFORMANCE_DATA"; //$NON-NLS-1$
            case WinAPI.HKEY_USERS:
                return "HKEY_USERS"; //$NON-NLS-1$
            default:
                return ""; //$NON-NLS-1$
        }
    }

    public static int getRootKey(String rootKey)
    {
        if (rootKey.equalsIgnoreCase("HKEY_CLASSES_ROOT") || rootKey.equalsIgnoreCase("HKCR")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_CLASSES_ROOT;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_CURRENT_CONFIG") || rootKey.equalsIgnoreCase("HKCC")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_CURRENT_CONFIG;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_CURRENT_USER") || rootKey.equalsIgnoreCase("HKCU")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_CURRENT_USER;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_DYN_DATA") || rootKey.equalsIgnoreCase("HKDD")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_DYN_DATA;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_LOCAL_MACHINE") || rootKey.equalsIgnoreCase("HKLM")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_LOCAL_MACHINE;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_PERFORMANCE_DATA") || rootKey.equalsIgnoreCase("HKPD")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_PERFORMANCE_DATA;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_USERS") || rootKey.equalsIgnoreCase("HKU")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_USERS;
        }
        else {
            return 0;
        }
    }

    public RegistryRoot()
    {
        super(null, -1,null);
        setName(Common.getMyComputerLabel());
        mChildren = new RegistryKey[ROOT_KEYS.length];
        for (int i = 0; i < mChildren.length; i++) {
            mChildren[i] = new RegistryKey(this, ROOT_KEYS[i], getRootKeyName(ROOT_KEYS[i]));
        }
        mChildCount = mChildren.length;
    }

    @Override
    protected void expandName(StringBuffer buf)
    {
    }

    @Override
    public Image getImage()
    {
        return REGROOT_IMAGE;
    }

    @Override
    public Image getExpandedImage()
    {
        return REGROOT_IMAGE;
    }
}
