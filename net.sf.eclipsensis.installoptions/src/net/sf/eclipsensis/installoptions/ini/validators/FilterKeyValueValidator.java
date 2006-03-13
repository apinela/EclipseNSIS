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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.util.Common;

public class FilterKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            String[] array = Common.tokenize(value,IInstallOptionsConstants.LIST_SEPARATOR,false);
            int n = array.length;
            if(n%2 != 0) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    keyValue.setValue(new StringBuffer(value).append(IInstallOptionsConstants.LIST_SEPARATOR).append(array[n-1]).toString());
                }
                else {
                    keyValue.addProblem(INIProblem.TYPE_ERROR,
                                        InstallOptionsPlugin.getFormattedString("filter.value.error", //$NON-NLS-1$
                                                new Object[]{keyValue.getKey()}));
                    return false;
                }
            }
        }
        return true;
    }
}
