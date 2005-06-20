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

import java.util.Arrays;
import java.util.Set;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INIKeyValue;
import net.sf.eclipsensis.installoptions.ini.INIProblem;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.CaseInsensitiveSet;

public class TypeKeyValueValidator implements IINIKeyValueValidator
{
    private static final Set TYPES = new CaseInsensitiveSet(Arrays.asList(InstallOptionsModel.getInstance().getControlTypes()));

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    public boolean isValid(INIKeyValue keyValue)
    {
        String value = keyValue.getValue();
        if(value.length() > 0 &&  TYPES.contains(value)) {
            return true;
        }
        keyValue.addProblem(INIProblem.TYPE_ERROR,
                            InstallOptionsPlugin.getFormattedString("type.value.error", //$NON-NLS-1$
                                    new Object[]{new Integer(value.length()),
                                    InstallOptionsModel.PROPERTY_TYPE,value}));
        return false;
    }
}
