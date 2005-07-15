/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini.validators;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INIKeyValue;
import net.sf.eclipsensis.installoptions.ini.INIProblem;
import net.sf.eclipsensis.util.Common;

public class RequiredNumberKeyValueValidator implements IINIKeyValueValidator
{
    protected boolean isNegativeAllowed()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    public boolean isValid(INIKeyValue keyValue)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            try {
                int i = Integer.parseInt(value);
                if(i < 0 && !isNegativeAllowed()) {
                    addError(keyValue, "positive.numeric.value.error"); //$NON-NLS-1$
                    return false;
                }
            }
            catch(Exception e) {
                addError(keyValue, "numeric.value.error"); //$NON-NLS-1$
                return false;
            }
        }
        else {
            addError(keyValue, "numeric.value.error"); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    protected void addError(INIKeyValue keyValue, String resourceString)
    {
        keyValue.addProblem(INIProblem.TYPE_ERROR,
                            InstallOptionsPlugin.getFormattedString(resourceString,
                                    new String[]{keyValue.getKey()}));
    }

}