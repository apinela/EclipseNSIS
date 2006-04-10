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
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.swt.SWT;

public class MultiLineKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = TypeConverter.ESCAPED_STRING_CONVERTER.asString(keyValue.getValue());
        boolean hasProblems = false;
        boolean checkErrors = true;
        boolean checkWarnings = true;
        boolean fixWarnings = (fixFlag & INILine.VALIDATE_FIX_WARNINGS) > 0;
        boolean fixErrors = (fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0;

        char[] chars = value.toCharArray();
        StringBuffer buf = null;
        if(fixWarnings || fixErrors) {
            buf = new StringBuffer("");
        }
        for (int i = 0; i < chars.length; i++) {
            if(fixWarnings || fixErrors) {
                buf.append(chars[i]);
            }
            switch(chars[i]) {
                case SWT.CR:
                    if(checkWarnings) {
                        if(i < chars.length-1) {
                            if(chars[i+1] == SWT.LF) {
                                if(fixWarnings || fixErrors) {
                                    buf.append(SWT.LF);
                                }
                                i++;
                                continue;
                            }
                        }
                        if(fixWarnings) {
                            buf.append(SWT.LF);
                        }
                        else {
                            keyValue.addProblem(INIProblem.TYPE_WARNING,
                                    InstallOptionsPlugin.getFormattedString("missing.lf.warning", //$NON-NLS-1$
                                            new Object[]{keyValue.getKey()}));
                            hasProblems = true;
                            checkWarnings = false;
                        }
                    }
                    break;
                case SWT.LF:
                    if(checkErrors) {
                        if(i > 0) {
                            if(chars[i-1] == SWT.CR) {
                                continue;
                            }
                        }
                        if(fixErrors) {
                            buf.insert(buf.length()-1, SWT.CR);
                        }
                        else {
                            keyValue.addProblem(INIProblem.TYPE_ERROR,
                                    InstallOptionsPlugin.getFormattedString("missing.cr.error", //$NON-NLS-1$
                                            new Object[]{keyValue.getKey()}));
                            hasProblems = true;
                            checkErrors = false;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if(fixWarnings || fixErrors) {
            keyValue.setValue((String)TypeConverter.ESCAPED_STRING_CONVERTER.asType(buf.toString()));
        }
        return !hasProblems;
    }
}
