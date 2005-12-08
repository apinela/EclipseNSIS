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
import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INIKeyValue;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class ListboxStateKeyValueValidator extends DropListStateKeyValueValidator
{

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    public boolean isValid(INIKeyValue keyValue)
    {
        boolean b = true;
        if(!isMultiSelect(keyValue)) {
            String arg = new StringBuffer("\"").append(InstallOptionsModel.FLAGS_MULTISELECT).append( //$NON-NLS-1$
                                          "\",\"").append(InstallOptionsModel.FLAGS_MULTISELECT).append( //$NON-NLS-1$
                                          "\"").toString(); //$NON-NLS-1$
            b = super.validateSingleSelection(keyValue,InstallOptionsPlugin.getFormattedString("multi.selection.error", //$NON-NLS-1$
                    new String[]{InstallOptionsModel.PROPERTY_STATE,arg}));
        }
        return b && super.validateSelection(keyValue,Common.tokenize(keyValue.getValue(),
                                            IInstallOptionsConstants.LIST_SEPARATOR,false));

    }

    private boolean isMultiSelect(INIKeyValue keyValue)
    {
        INIKeyValue[] values = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_FLAGS);
        if(!Common.isEmptyArray(values)) {
            List flags = Arrays.asList(Common.tokenize(values[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR));
            if(flags.contains(InstallOptionsModel.FLAGS_MULTISELECT) ||
               flags.contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT)) {
                return true;
            }
        }
        return false;
    }

    protected String getType()
    {
        return InstallOptionsModel.TYPE_LISTBOX;
    }
}
