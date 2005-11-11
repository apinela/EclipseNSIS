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
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;


public class MaxLenKeyValueValidator extends PositiveNumberKeyValueValidator
{
    public boolean isValid(INIKeyValue keyValue)
    {
        boolean b = super.isValid(keyValue);
        if(b) {
            int maxValue = InstallOptionsModel.INSTANCE.getMaxLength();
            String value = keyValue.getValue();
            if(!Common.isEmpty(value)) {
                maxValue = Integer.parseInt(value);
            }

            int minValue = 0;
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_MINLEN);
            if(!Common.isEmptyArray(keyValues)) {
                value = keyValues[0].getValue();
                if(!Common.isEmpty(value)) {
                    try {
                        minValue = Integer.parseInt(value);
                    }
                    catch(Exception e) {
                        minValue = 0;
                    }
                }
            }
            if(minValue > maxValue) {
                keyValue.addProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("minmax.value.error",new Object[]{ //$NON-NLS-1$
                        keyValue.getKey(),new Integer(minValue),new Integer(maxValue)}));
                b = false;
            }
        }
        return b;
    }
}
