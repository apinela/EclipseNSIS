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
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;


public class MinLenKeyValueValidator extends PositiveNumberKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        boolean b = super.validate(keyValue, fixFlag);
        if(b) {
            int minValue = 0;
            String value = keyValue.getValue();
            if(!Common.isEmpty(value)) {
                minValue = Integer.parseInt(value);
            }

            int maxValue = InstallOptionsModel.INSTANCE.getMaxLength();
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_MAXLEN);
            if(!Common.isEmptyArray(keyValues)) {
                value = keyValues[0].getValue();
                if(!Common.isEmpty(value)) {
                    try {
                        maxValue = Integer.parseInt(value);
                    }
                    catch(Exception e) {
                        maxValue = InstallOptionsModel.INSTANCE.getMaxLength();
                    }
                }
            }
            if(minValue > maxValue) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    keyValue.setValue(Integer.toString(maxValue));
                }
                else {
                    keyValue.addProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("minmax.value.error",new Object[]{ //$NON-NLS-1$
                            keyValue.getKey(),new Integer(0),new Integer(maxValue)}));
                    b = false;
                }
            }
        }
        return b;
    }
}
