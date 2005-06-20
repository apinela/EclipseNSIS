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

public class ToggleKeyValueValidator implements IINIKeyValueValidator
{

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    public boolean isValid(INIKeyValue keyValue)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            char[] chars = value.toCharArray();
            if(chars.length == 1 && chars[0] == '0' || chars[0] == '1') {
                return true;
            }
            keyValue.addProblem(INIProblem.TYPE_ERROR,
                                InstallOptionsPlugin.getFormattedString("toggle.value.error", //$NON-NLS-1$
                                        new String[]{keyValue.getKey()}));
            return false;
        }
        return true;
    }

}
