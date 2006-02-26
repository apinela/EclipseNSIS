/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

import org.w3c.dom.Node;

public class VarParam extends ComboParam
{
    private Pattern mVarPattern;

    public VarParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        mVarPattern = Pattern.compile("\\$[0-9a-z_]+",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
        super.init(node);
    }

    protected Map getComboValues()
    {
        Map map = new LinkedHashMap();
        String[] vars = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.REGISTERS);
        if(!Common.isEmptyArray(vars)) {
            Arrays.sort(vars);
            for (int i = 0; i < vars.length; i++) {
                map.put(vars[i], vars[i]);
            }
        }
        
        return map;
    }

    protected boolean isUserEditable()
    {
        return true;
    }

    protected String validateUserValue(String value)
    {
        if(value != null) {
            Matcher m = mVarPattern.matcher(value);
            if(m.matches()) {
                return null;
            }
        }
        return EclipseNSISPlugin.getResourceString("var.param.error");  //$NON-NLS-1$
    }
}
