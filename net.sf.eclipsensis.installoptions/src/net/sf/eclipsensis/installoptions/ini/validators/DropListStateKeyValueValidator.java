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

import java.util.ArrayList;
import java.util.Arrays;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class DropListStateKeyValueValidator extends ComboboxStateKeyValueValidator
{
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    public boolean isValid(INIKeyValue keyValue)
    {
        return super.isValid(keyValue) &&
               validateSelection(keyValue, new String[]{keyValue.getValue()});
    }

    protected String getType()
    {
        return InstallOptionsModel.TYPE_DROPLIST;
    }
    
    protected boolean validateSelection(INIKeyValue keyValue, String[] values)
    {
        if(!Common.isEmptyArray(values)) {
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_LISTITEMS);
            if(!Common.isEmptyArray(keyValues)) {
                String[] array = Common.tokenize(keyValues[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR);
                if(!Common.isEmptyArray(array)) {
                    ArrayList valuesList = new ArrayList(Arrays.asList(values));
                    valuesList.removeAll(Arrays.asList(array));
                    if(valuesList.size() == 0) {
                        return true;
                    }
                }
            }
            keyValue.addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("valid.selection.error", //$NON-NLS-1$
                    new String[]{InstallOptionsModel.PROPERTY_STATE,InstallOptionsModel.PROPERTY_LISTITEMS}));
            return false;
        }
        return true;
    }
}
