/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

public class NSISWizardDisplayValues implements INSISWizardConstants
{
    public static final String[] INSTALLER_TYPE_NAMES = new String[INSTALLER_TYPE_MUI+1];
    public static final String[] LICENSE_BUTTON_NAMES = new String[LICENSE_BUTTON_RADIO+1];
    public static final String[] COMPRESSOR_TYPE_NAMES = new String[MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY.length-1];
    public static final String[] HKEY_NAMES = {"HKEY_CLASSES_ROOT","HKEY_LOCAL_MACHINE","HKEY_CURRENT_USER","HKEY_USERS",
                                               "HKEY_CURRENT_CONFIG","HKEY_DYN_DATA","HKEY_PERFORMANCE_DATA"};
    public static final String[] SHORTCUT_TYPE_NAMES = new String[SHORTCUT_INSTALLELEMENT+1];
    public static final String[] OVERWRITE_MODE_NAMES = new String[OVERWRITE_IFDIFF+1];
    public static final String[] REG_VALUE_TYPES = new String[REG_DWORD+1];
    
    static {
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_SILENT] = EclipseNSISPlugin.getResourceString("installer.type.silent"); //$NON-NLS-1$
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_CLASSIC] = EclipseNSISPlugin.getResourceString("installer.type.classic"); //$NON-NLS-1$
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_MUI] = EclipseNSISPlugin.getResourceString("installer.type.mui"); //$NON-NLS-1$
        
        LICENSE_BUTTON_NAMES[LICENSE_BUTTON_CLASSIC] = EclipseNSISPlugin.getResourceString("license.button.classic"); //$NON-NLS-1$
        LICENSE_BUTTON_NAMES[LICENSE_BUTTON_CHECKED] = EclipseNSISPlugin.getResourceString("license.button.checked"); //$NON-NLS-1$
        LICENSE_BUTTON_NAMES[LICENSE_BUTTON_RADIO] = EclipseNSISPlugin.getResourceString("license.button.radio"); //$NON-NLS-1$
        
        System.arraycopy(MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY,0,COMPRESSOR_TYPE_NAMES,0,COMPRESSOR_TYPE_NAMES.length);
        COMPRESSOR_TYPE_NAMES[MakeNSISRunner.COMPRESSOR_DEFAULT] = EclipseNSISPlugin.getResourceString("default.compressor.label"); //$NON-NLS-1$
        
        SHORTCUT_TYPE_NAMES[SHORTCUT_URL] = EclipseNSISPlugin.getResourceString("shortcut.type.url"); //$NON-NLS-1$
        SHORTCUT_TYPE_NAMES[SHORTCUT_INSTALLELEMENT] = EclipseNSISPlugin.getResourceString("shortcut.type.installelement"); //$NON-NLS-1$
        
        OVERWRITE_MODE_NAMES[OVERWRITE_ON] = EclipseNSISPlugin.getResourceString("overwrite.on"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_OFF] = EclipseNSISPlugin.getResourceString("overwrite.off"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_TRY] = EclipseNSISPlugin.getResourceString("overwrite.try"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_NEWER] = EclipseNSISPlugin.getResourceString("overwrite.newer"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_IFDIFF] = EclipseNSISPlugin.getResourceString("overwrite.ifdiff"); //$NON-NLS-1$;

        REG_VALUE_TYPES[REG_SZ] = EclipseNSISPlugin.getResourceString("reg.value.string"); //$NON-NLS-1$;
        REG_VALUE_TYPES[REG_DWORD] = EclipseNSISPlugin.getResourceString("reg.value.dword"); //$NON-NLS-1$;
    }
}
