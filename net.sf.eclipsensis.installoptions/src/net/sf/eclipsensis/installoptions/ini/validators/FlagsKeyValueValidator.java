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

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModelTypeDef;
import net.sf.eclipsensis.util.Common;

public class FlagsKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        IINIContainer c = keyValue.getParent();
        if(c instanceof INISection) {
            String value = keyValue.getValue();
            if(!Common.isEmpty(value)) {
                INIKeyValue[] types = ((INISection)c).findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                if(!Common.isEmptyArray(types)) {
                    InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(types[0].getValue());
                    if(typeDef != null) {
                        Collection availableFlags;
                        availableFlags = typeDef.getFlags();
                        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                        List flags = Common.tokenizeToList(value,IInstallOptionsConstants.LIST_SEPARATOR,false);
                        int n = 0;
                        for (Iterator iter=flags.iterator(); iter.hasNext(); ) {
                            String flag = (String)iter.next();
                            if(!Common.isEmpty(flag) && !availableFlags.contains(flag)) {
                                if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                                    iter.remove();
                                }
                                else {
                                    if(n > 0) {
                                        buf.append(", "); //$NON-NLS-1$
                                    }
                                    buf.append("\"").append(flag).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                n++;
                            }
                        }
                        if(n > 0) {
                            if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                                keyValue.setValue(Common.flatten(flags, IInstallOptionsConstants.LIST_SEPARATOR));
                            }
                            else {
                                keyValue.addProblem(INIProblem.TYPE_WARNING,
                                                    InstallOptionsPlugin.getFormattedString("flags.value.warning", //$NON-NLS-1$
                                                            new Object[]{InstallOptionsModel.PROPERTY_TYPE,
                                                                         types[0].getValue(),new Integer(n),buf.toString()}));
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

}
