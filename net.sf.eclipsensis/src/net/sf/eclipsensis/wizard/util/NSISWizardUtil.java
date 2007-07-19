/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.wizard.INSISWizardConstants;

public class NSISWizardUtil
{
    private NSISWizardUtil()
    {
    }

    public static String convertPath(int targetPlatform, String path)
    {
        switch(targetPlatform) {
            case INSISWizardConstants.TARGET_PLATFORM_X64:
                path = IOUtility.convertPathTo64bit(path);
                break;
            case INSISWizardConstants.TARGET_PLATFORM_X86:
                path = IOUtility.convertPathTo32bit(path);
                break;
        }
        return path;
    }
}
