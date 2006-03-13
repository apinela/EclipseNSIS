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
import net.sf.eclipsensis.installoptions.model.InstallOptionsLink;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.graphics.RGB;

public class TxtColorKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            RGB rgb = (RGB)TypeConverter.RGB_CONVERTER.asType(value);
            if(rgb != null) {
                return true;
            }
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                keyValue.setValue(TypeConverter.RGB_CONVERTER.asString(InstallOptionsLink.DEFAULT_TXTCOLOR));
            }
            else {
                keyValue.addProblem(INIProblem.TYPE_ERROR,
                                    InstallOptionsPlugin.getFormattedString("txtcolor.value.error", //$NON-NLS-1$
                                            new String[]{keyValue.getKey()}));
                return false;
            }
        }
        return true;
    }

}
