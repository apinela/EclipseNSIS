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

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;

import org.w3c.dom.Node;

public class HKEYParam extends ComboParam
{
    public HKEYParam(Node node)
    {
        super(node);
    }

    protected Map getComboValues()
    {
        Map map = new LinkedHashMap();
        String[] hkeys = NSISWizardDisplayValues.getHKEYNames();
        if(!Common.isEmptyArray(hkeys)) {
            for (int i = 0; i < hkeys.length; i++) {
                map.put(hkeys[i], hkeys[i]);
            }
        }
        return map;
    }
}
