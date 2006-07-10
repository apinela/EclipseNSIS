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
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class TextStateKeyValueValidator extends MultiLineKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        boolean hasProblems = false;
        if(!Common.isEmpty(value)) {
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_FLAGS);
            if(!Common.isEmptyArray(keyValues)) {
                String[] flags = Common.tokenize(keyValues[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR,false);
                if(!Common.isEmptyArray(flags)) {
                    for (int i = 0; i < flags.length; i++) {
                        if(flags[i].equalsIgnoreCase(InstallOptionsModel.FLAGS_MULTILINE)) {
                            if(!super.validate(keyValue, fixFlag)) {
                                hasProblems = true;
                            }
                        }
                        else if(flags[i].equalsIgnoreCase(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                            try {
                                Integer.parseInt(value);
                            }
                            catch(Exception ex) {
                                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                                    char[] chars = value.toCharArray();
                                    for (int j = 0; j < chars.length; j++) {
                                        if(Character.isDigit(chars[i])) {
                                            buf.append(chars[i]);
                                        }
                                    }
                                    keyValue.setValue(buf.toString());
                                }
                                else {
                                    keyValue.addProblem(new INIProblem(INIProblem.TYPE_ERROR,
                                            InstallOptionsPlugin.getFormattedString("text.state.only.numbers.error", //$NON-NLS-1$
                                                    new String[]{InstallOptionsModel.PROPERTY_STATE,
                                                    InstallOptionsModel.FLAGS_ONLY_NUMBERS})));
                                    hasProblems = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return !hasProblems;
    }
}
