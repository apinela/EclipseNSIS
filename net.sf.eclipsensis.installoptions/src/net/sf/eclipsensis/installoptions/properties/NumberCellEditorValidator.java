/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NumberCellEditorValidator implements ICellEditorValidator
{
    private static NumberCellEditorValidator cInstance;

    public static NumberCellEditorValidator instance()
    {
        if (cInstance == null)
            cInstance = new NumberCellEditorValidator();
        return cInstance;
    }

    public String isValid(Object value)
    {
        try {
            new Integer((String)value);
            return null;
        }
        catch (NumberFormatException exc) {
            return InstallOptionsPlugin.getResourceString("number.error.message"); //$NON-NLS-1$
        }
    }
}