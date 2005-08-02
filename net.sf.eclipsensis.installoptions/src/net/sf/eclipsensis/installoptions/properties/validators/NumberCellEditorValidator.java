/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.validators;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NumberCellEditorValidator implements ICellEditorValidator
{
    public static NumberCellEditorValidator INSTANCE = new NumberCellEditorValidator(0,Integer.MAX_VALUE,false);
    private int mMinValue;
    private int mMaxValue;
    private boolean mBlankAllowed;
    
    public NumberCellEditorValidator(int minValue, int maxValue, boolean blankAllowed)
    {
        super();
        mMinValue = minValue;
        mMaxValue = maxValue;
        mBlankAllowed = blankAllowed;
    }

    public String isValid(Object value)
    {
        try {
            if(!isBlankAllowed() || !Common.isEmpty((String)value)) {
                int val = Integer.parseInt((String)value);
                if(val < getMinValue()) {
                    return InstallOptionsPlugin.getFormattedString("number.minvalue.error.message",new Object[]{new Integer(getMinValue())}); //$NON-NLS-1$
                }
                if(val > getMaxValue()) {
                    return InstallOptionsPlugin.getFormattedString("number.maxvalue.error.message",new Object[]{new Integer(getMinValue())}); //$NON-NLS-1$
                }
            }
            return null;
        }
        catch (NumberFormatException exc) {
            return InstallOptionsPlugin.getResourceString("number.error.message"); //$NON-NLS-1$
        }
    }
    
    public boolean isBlankAllowed()
    {
        return mBlankAllowed;
    }
    
    public int getMinValue()
    {
        return mMinValue;
    }
    
    public int getMaxValue()
    {
        return mMaxValue;
    }
}