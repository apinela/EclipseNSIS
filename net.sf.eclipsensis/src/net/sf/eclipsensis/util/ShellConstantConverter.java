/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.*;

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.help.NSISKeywords.ShellConstant;

public class ShellConstantConverter
{
    private String mShellContext = ShellConstant.CONTEXT_GENERAL;
    private List mShellConstants = NSISKeywords.getInstance().getShellConstants();

    public String getShellContext()
    {
        return mShellContext;
    }

    public void setShellContext(String shellContext)
    {
        mShellContext = shellContext;
    }

    public String encodeConstants(String line)
    {
        if(!Common.isEmpty(line)) {
            for (Iterator iter = mShellConstants.iterator(); iter.hasNext();) {
                ShellConstant constant = (ShellConstant)iter.next();
                if(constant.value.length() <= line.length()) {
                    if(!ShellConstant.CONTEXT_GENERAL.equals(constant.context) &&
                       !mShellContext.equals(constant.context) &&
                       !ShellConstant.CONTEXT_GENERAL.equals(mShellContext)){
                        continue;
                    }
                    String newLine = Common.replaceAll(line, constant.value, constant.name, true);
                    if(!newLine.equals(line) && !ShellConstant.CONTEXT_GENERAL.equals(constant.context)) {
                        mShellContext = constant.context;
                        line = newLine;
                    }
                }
            }
        }
        return line;
    }

    public String decodeConstants(String line)
    {
        if(!Common.isEmpty(line)) {
            for (Iterator iter = mShellConstants.iterator(); iter.hasNext();) {
                ShellConstant constant = (ShellConstant)iter.next();
                if(constant.value.length() <= line.length()) {
                    if(!ShellConstant.CONTEXT_GENERAL.equals(constant.context) &&
                       !mShellContext.equals(constant.context) &&
                       !ShellConstant.CONTEXT_GENERAL.equals(mShellContext)){
                        continue;
                    }
                    String newLine = Common.replaceAll(line, constant.name, constant.value, true);
                    if(!newLine.equals(line) && !ShellConstant.CONTEXT_GENERAL.equals(constant.context)) {
                        mShellContext = constant.context;
                        line = newLine;
                    }
                }
            }
        }
        return line;
    }
}
