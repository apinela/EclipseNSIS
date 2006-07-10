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
import net.sf.eclipsensis.installoptions.model.InstallOptionsModelTypeDef;

public class TypeKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        if(value.length() > 0 && value.indexOf(' ') < 0 && value.indexOf('\t') < 0) {
            InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(value);
            if(typeDef != null) {
                return true;
            }
        }
        if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
            keyValue.setValue(InstallOptionsModel.TYPE_UNKNOWN);
        }
        else {
            keyValue.addProblem(new INIProblem(INIProblem.TYPE_WARNING,
                                InstallOptionsPlugin.getFormattedString("type.value.warning", //$NON-NLS-1$
                                        new Object[]{InstallOptionsModel.PROPERTY_TYPE})));
        }
        return false;
    }
}
