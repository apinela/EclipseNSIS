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

import java.util.*;

import net.sf.eclipsensis.help.NSISKeywords;
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

    public static String[] getPathConstantsAndVariables(int targetPlatform)
    {
        List list = new ArrayList();
        String[] array = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES);
        for (int i = 0; i < array.length; i++) {
            boolean exclude = false;
            switch(targetPlatform) {
                case INSISWizardConstants.TARGET_PLATFORM_X64:
                    exclude = IOUtility.is32BitPath(array[i]);
                    break;
                case INSISWizardConstants.TARGET_PLATFORM_X86:
                    exclude = IOUtility.is64BitPath(array[i]);
                    break;
            }
            if(!exclude) {
                list.add(array[i]);
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }
}
