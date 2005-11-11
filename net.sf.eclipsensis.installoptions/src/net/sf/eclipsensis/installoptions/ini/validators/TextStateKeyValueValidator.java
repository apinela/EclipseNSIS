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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class TextStateKeyValueValidator implements IINIKeyValueValidator
{
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    public boolean isValid(INIKeyValue keyValue)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_FLAGS);
            if(!Common.isEmptyArray(keyValues)) {
                String[] flags = Common.tokenize(keyValues[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR);
                if(!Common.isEmptyArray(flags)) {
                    for (int i = 0; i < flags.length; i++) {
                        if(flags[i].equalsIgnoreCase(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                            try {
                                Integer.parseInt(value);
                            }
                            catch(Exception ex) {
                                keyValue.addProblem(INIProblem.TYPE_ERROR,
                                        InstallOptionsPlugin.getFormattedString("text.state.only.numbers.error", //$NON-NLS-1$
                                                new String[]{InstallOptionsModel.PROPERTY_STATE,
                                                InstallOptionsModel.FLAGS_ONLY_NUMBERS}));
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
