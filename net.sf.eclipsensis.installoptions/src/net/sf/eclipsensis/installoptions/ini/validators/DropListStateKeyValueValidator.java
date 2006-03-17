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

import java.util.ArrayList;
import java.util.List;

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
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        return super.validate(keyValue, fixFlag) &&
               validateSelection(keyValue, (value != null && value.length() > 0?new String[]{value}:Common.EMPTY_STRING_ARRAY), 
                                 fixFlag);
    }

    protected String getType()
    {
        return InstallOptionsModel.TYPE_DROPLIST;
    }

    protected boolean validateSelection(INIKeyValue keyValue, String[] values, int fixFlag)
    {
        if(!Common.isEmptyArray(values)) {
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_LISTITEMS);
            if(!Common.isEmptyArray(keyValues)) {
                List allValues = Common.tokenizeToList(keyValues[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR,false);
                if(!Common.isEmptyCollection(allValues)) {
                    ArrayList valuesList = Common.makeList(values);
                    valuesList.removeAll(allValues);
                    if(valuesList.size() == 0) {
                        return true;
                    }
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        valuesList = Common.makeList(values);
                        valuesList.retainAll(allValues);
                        keyValue.setValue(Common.flatten(valuesList, IInstallOptionsConstants.LIST_SEPARATOR));
                        return true;
                    }
                }
            }
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                keyValue.setValue(""); //$NON-NLS-1$
            }
            else {
                keyValue.addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("valid.selection.error", //$NON-NLS-1$
                        new String[]{InstallOptionsModel.PROPERTY_STATE,InstallOptionsModel.PROPERTY_LISTITEMS}));
                return false;
            }
        }
        return true;
    }
}
