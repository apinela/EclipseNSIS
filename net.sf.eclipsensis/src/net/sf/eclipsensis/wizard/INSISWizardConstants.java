/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

public interface INSISWizardConstants
{
    public static final int INSTALLER_TYPE_SILENT = 0;
    public static final int INSTALLER_TYPE_CLASSIC = 1;
    public static final int INSTALLER_TYPE_MUI = 2;
    
    public static final int LICENSE_BUTTON_CLASSIC = 0;
    public static final int LICENSE_BUTTON_CHECKED = 1;
    public static final int LICENSE_BUTTON_RADIO = 2;

    public static final int OVERWRITE_ON = 0;
    public static final int OVERWRITE_OFF = 1;
    public static final int OVERWRITE_TRY = 2;
    public static final int OVERWRITE_NEWER = 3;
    public static final int OVERWRITE_IFDIFF = 4;
    
    public static final int HKCR = 0;
    public static final int HKLM = 1;
    public static final int HKCU = 2;
    public static final int HKU = 3;
    public static final int HKCC = 4;
    public static final int HKDD = 5;
    public static final int HKPD = 6;
    
    public static final int REG_SZ = 0;
    public static final int REG_DWORD = 1;
    
    public static final int SHORTCUT_URL = 0;
    public static final int SHORTCUT_INSTALLELEMENT = 1;
    
    public static final String WIZARD_TEMPLATE_EXTENSION=".nst"; //$NON-NLS-1$
}
