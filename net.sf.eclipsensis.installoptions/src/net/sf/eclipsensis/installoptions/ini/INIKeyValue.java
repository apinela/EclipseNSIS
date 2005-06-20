/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator;
import net.sf.eclipsensis.installoptions.ini.validators.INIKeyValueValidatorRegistry;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class INIKeyValue extends INILine
{
    private String mKey = ""; //$NON-NLS-1$
    private String mValue = ""; //$NON-NLS-1$
    private String mOriginalValue = ""; //$NON-NLS-1$
    
    public INIKeyValue(String key)
    {
        super();
        mKey = key;
    }
    
    INIKeyValue(String key, String value)
    {
        this(key);
        mValue = value;
        mOriginalValue = value;
    }
    
    public String getKey()
    {
        return mKey;
    }
    
    public String getValue()
    {
        return mValue;
    }
    
    public void setValue(String value)
    {
        mValue = value;
    }
    
    protected void checkProblems()
    {
        if(getParent() instanceof INISection) {
            INIKeyValue[] keyValues = ((INISection)getParent()).findKeyValues(getKey());
            if(keyValues.length > 1) {
                addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("duplicate.key.name.error",new String[]{getKey()})); //$NON-NLS-1$
            }
            INIKeyValue[] types = ((INISection)getParent()).findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
            IINIKeyValueValidator validator = null;
            if(!Common.isEmptyArray(types)) {
                String name = new StringBuffer(types[0].getValue()).append(".").append(getKey()).toString(); //$NON-NLS-1$
                validator = INIKeyValueValidatorRegistry.getKeyValueValidator(name);
            }
            if(validator == null) {
                validator = INIKeyValueValidatorRegistry.getKeyValueValidator(getKey());
            }
            if(validator != null) {
                validator.isValid(this);
            }
            if(getValue().length() > InstallOptionsModel.MAX_LENGTH.intValue()) {
                addProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getFormattedString("value.length.warning", //$NON-NLS-1$
                        new Object[]{getKey(),InstallOptionsModel.MAX_LENGTH}));
            }
        }
        else {
            addProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getResourceString("line.ignored.warning")); //$NON-NLS-1$
        }
    }

    public void update()
    {
        if(!Common.stringsAreEqual(mValue,mOriginalValue))
        {
            String text = getText();
            StringBuffer buf = new StringBuffer();
            if(Common.isEmpty(text)) {
                text = buf.append(mKey).append("=").append(mValue).toString(); //$NON-NLS-1$
            }
            else {
                int n = text.indexOf('=');
                if(Common.isEmpty(mOriginalValue)) {
                    buf.append(text.substring(0,n+1));
                    buf.append(mValue);
                    if(text.length() >= n+1) {
                        buf.append(text.substring(n+1));
                    }
                }
                else {
                    n = text.indexOf(mOriginalValue,n);
                    buf.append(text.substring(0,n));
                    buf.append(mValue);
                    buf.append(text.substring(n+mOriginalValue.length()));
                }
            }
            setText(buf.toString());
            mOriginalValue = mValue;
        }
    }
}
