/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini.validators;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.util.Common;

public class NumberKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            try {
                int i = Integer.parseInt(value);
                if(i < 0 && !isNegativeAllowed()) {
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        keyValue.setValue(Integer.toString(-i)); //$NON-NLS-1$
                        return validate(keyValue, fixFlag);
                    }
                    else {
                        keyValue.addProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("positive.numeric.value.error",new String[]{keyValue.getKey()})); //$NON-NLS-1$
                        return false;
                    }
                }
            }
            catch(Exception e) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    char[] chars = value.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if(Character.isDigit(chars[i])) {
                            buf.append(chars[i]);
                        }
                    }
                    keyValue.setValue(buf.toString());
                    return validate(keyValue, fixFlag);
                }
                else {
                    keyValue.addProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("numeric.value.error",new String[]{keyValue.getKey()})); //$NON-NLS-1$
                    return false;
                }
            }
        }
        else if(!isEmptyAllowed()){
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                keyValue.setValue("0"); //$NON-NLS-1$
                return validate(keyValue, fixFlag);
            }
            else {
                keyValue.addProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("numeric.value.error",new String[]{keyValue.getKey()})); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    protected boolean isEmptyAllowed()
    {
        return true;
    }

    protected boolean isNegativeAllowed()
    {
        return true;
    }
}
