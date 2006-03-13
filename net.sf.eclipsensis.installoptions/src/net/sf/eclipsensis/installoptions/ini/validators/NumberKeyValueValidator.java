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
        String error = null;
        if(!Common.isEmpty(value)) {
            try {
                int i = Integer.parseInt(value);
                if(i < 0 && !isNegativeAllowed()) {
                    error = "positive.numeric.value.error"; //$NON-NLS-1$
                }
            }
            catch(Exception e) {
                error = "numeric.value.error"; //$NON-NLS-1$
            }
        }
        else if(!isEmptyAllowed()){
            error = "numeric.value.error"; //$NON-NLS-1$
        }
        if(error != null) {
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                keyValue.setValue("0");
                return validate(keyValue, fixFlag);
            }
            else {
                keyValue.addProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString(error,new String[]{keyValue.getKey()}));
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
