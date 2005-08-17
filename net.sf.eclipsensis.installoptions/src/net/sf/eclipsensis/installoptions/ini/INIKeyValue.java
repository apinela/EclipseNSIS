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
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;

public class INIKeyValue extends INILine
{
    private static final long serialVersionUID = -1955779348753709847L;
    
    private String mKey = ""; //$NON-NLS-1$
    private String mValue = ""; //$NON-NLS-1$
    private String mOriginalValue = ""; //$NON-NLS-1$
    private boolean mQuoted = false;
    
    public INIKeyValue(String key)
    {
        super();
        mKey = key;
    }
    
    INIKeyValue(String key, String value)
    {
        this(key);
        if(value != null && value.length() > 2 && value.startsWith("\"") && value.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            value = value.substring(1,value.length()-1);
            mQuoted = true;
        }
        setValue(value);
        mOriginalValue = getValue();
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
        mValue = (value==null?"":TypeConverter.INI_STRING_CONVERTER.asString(value)); //$NON-NLS-1$
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
            if(!Common.isEmptyArray(types) && types[0] != this) {
                if(InstallOptionsModel.INSTANCE.getControlTypeDef(types[0].getValue()) != null) {
                    String name = new StringBuffer(types[0].getValue()).append(".").append(getKey()).toString(); //$NON-NLS-1$
                    validator = INIKeyValueValidatorRegistry.getKeyValueValidator(name);
                }
            }
            if(validator == null) {
                validator = INIKeyValueValidatorRegistry.getKeyValueValidator(getKey());
            }
            if(validator != null) {
                validator.isValid(this);
            }
            int maxLen = InstallOptionsModel.INSTANCE.getMaxLength();
            if(getValue().length() > maxLen) {
                addProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getFormattedString("value.length.warning", //$NON-NLS-1$
                        new Object[]{getKey(),new Integer(maxLen)}));
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
            boolean oldQuoted = mQuoted;
            String current = (String)TypeConverter.INI_STRING_CONVERTER.asType(maybeQuote(mValue));

            if(Common.isEmpty(text)) {
                text = buf.append(mKey).append("=").append(current).toString(); //$NON-NLS-1$
            }
            else {
                int n = text.indexOf('=');
                if(mOriginalValue.length() == 0 && !oldQuoted) {
                    buf.append(text.substring(0,n+1));
                    buf.append(current);
                    if(text.length() >= n+1) {
                        buf.append(text.substring(n+1));
                    }
                }
                else {
                    String original = (String)TypeConverter.INI_STRING_CONVERTER.asType(oldQuoted?quote(mOriginalValue):mOriginalValue);
                    n = text.indexOf(original,n);
                    buf.append(text.substring(0,n));
                    buf.append(current);
                    buf.append(text.substring(n+original.length()));
                }
            }
            setText(buf.toString());
            mOriginalValue = mValue;
        }
    }
    
    private String maybeQuote(String text)
    {
        mQuoted = false;
        if(text != null && text.length() > 0) {
            if(Character.isWhitespace(text.charAt(0)) || Character.isWhitespace(text.charAt(text.length()-1))) {
                text = quote(text);
                mQuoted = true;
            }
        }
        return text;
    }
    
    private String quote(String text)
    {
        return new StringBuffer("\"").append(text).append("\"").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
