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

import java.util.Collection;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.labelproviders.MultiLineLabelProvider;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NSISStringLengthValidator implements ICellEditorValidator
{
    private static final Integer MAX_LENGTH;

    private String mPropertyName;

    static {
        int maxLen;
        try {
            maxLen = Integer.parseInt(NSISPreferences.getPreferences().getNSISOption("NSIS_MAX_STRLEN")); //$NON-NLS-1$
        }
        catch(Exception ex){
            maxLen = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
        }
        MAX_LENGTH = new Integer(maxLen);
    }
    
    /**
     * 
     */
    public NSISStringLengthValidator(String propertyName)
    {
        super();
        mPropertyName = propertyName;
    }
    
    public String isValid(String value)
    {
        value = MultiLineLabelProvider.INSTANCE.getText(value);
        if(value.length() > MAX_LENGTH.intValue())  {
            return InstallOptionsPlugin.getFormattedString("property.maxlength.error",new Object[]{mPropertyName,MAX_LENGTH});
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
     */
    public String isValid(Object value)
    {
        if(value instanceof String) {
            return isValid((String)value);
        }
        else if(value instanceof Collection) {
            return isValid(Common.flatten(((Collection)value).toArray(),IInstallOptionsConstants.LIST_SEPARATOR));
        }
        return null;
    }
}
