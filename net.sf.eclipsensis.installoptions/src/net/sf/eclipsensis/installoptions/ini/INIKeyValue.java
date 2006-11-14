/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
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
    private static final long serialVersionUID = -1955779348753709847L;

    private String mKey = ""; //$NON-NLS-1$
    private String mValue = ""; //$NON-NLS-1$
    private String mOriginalValue = ""; //$NON-NLS-1$
    private boolean mQuoted = false;

    public INIKeyValue(String key)
    {
        super(""); //$NON-NLS-1$
        mKey = key;
    }

    INIKeyValue(String text, String delimiter, String key, String value)
    {
        super(text, delimiter);
        mKey = key;
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
        mValue = (value==null?"":value); //$NON-NLS-1$
    }

    protected void checkProblems(int fixFlag)
    {
        if(getParent() instanceof INISection) {
            final INIKeyValue[] keyValues = ((INISection)getParent()).findKeyValues(getKey());
            if(keyValues.length > 1) {
                if((fixFlag & VALIDATE_FIX_ERRORS) > 0) {
                    for (int i = 0; i < keyValues.length; i++) {
                        if(keyValues[i] != this) {
                            getParent().removeChild(keyValues[i]);
                        }
                    }
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("duplicate.key.name.error",new String[]{getKey()})); //$NON-NLS-1$
                    addProblem(problem);
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.dup.keys")) { //$NON-NLS-1$
                        protected INIProblemFix[] createFixes()
                        {
                            INIProblemFix[] fixes = new INIProblemFix[keyValues.length-1];
                            for (int i = 0, j = 0; i < keyValues.length; i++) {
                                if(keyValues[i] != INIKeyValue.this) {
                                    fixes[j++] = new INIProblemFix(keyValues[i]);
                                }
                            }
                            return fixes;
                        }
                    });
                }
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
                validator.validate(this,fixFlag);
            }
            final int maxLen = InstallOptionsModel.INSTANCE.getMaxLength();
            if(getValue().length() > maxLen) {
                if((fixFlag & VALIDATE_FIX_WARNINGS)> 0) {
                    setValue(getValue().substring(0,maxLen));
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getFormattedString("value.length.warning", //$NON-NLS-1$
                                                new Object[]{getKey(),new Integer(maxLen)}));
                    addProblem(problem);
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.value.length")) { //$NON-NLS-1$
                        protected INIProblemFix[] createFixes()
                        {
                            return new INIProblemFix[] {new INIProblemFix(INIKeyValue.this,buildText(getValue().substring(0,maxLen))+(getDelimiter()==null?"":getDelimiter()))}; //$NON-NLS-1$
                        }
                    });
                }
            }
        }
        else {
            if((fixFlag & VALIDATE_FIX_WARNINGS)> 0) {
                getParent().removeChild(this);
            }
            else {
                INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getResourceString("line.ignored.warning")); //$NON-NLS-1$
                addProblem(problem);
                problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.line")) { //$NON-NLS-1$
                    protected INIProblemFix[] createFixes()
                    {
                        return new INIProblemFix[] {new INIProblemFix(INIKeyValue.this)};
                    }
                });
            }
        }
    }

    public String buildText(String value)
    {
        String text = getText();
        StringBuffer buf = new StringBuffer();
        boolean oldQuoted = mQuoted;
        String current = maybeQuote(value);

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
                String original = (oldQuoted?quote(mOriginalValue):mOriginalValue);
                n = text.indexOf(original,n);
                buf.append(text.substring(0,n));
                buf.append(current);
                buf.append(text.substring(n+original.length()));
            }
        }
        return buf.toString();
    }

    public void update()
    {
        if(Common.isEmpty(getText())|| !Common.stringsAreEqual(mValue,mOriginalValue))
        {
            setText(buildText(mValue));
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

    public int hashCode()
    {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((mKey == null)?0:mKey.hashCode());
        result = PRIME * result + (mQuoted?1231:1237);
        result = PRIME * result + ((mValue == null)?0:mValue.hashCode());
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final INIKeyValue other = (INIKeyValue)obj;
        if (mKey == null) {
            if (other.mKey != null)
                return false;
        }
        else if (!mKey.equals(other.mKey))
            return false;
        if (mQuoted != other.mQuoted)
            return false;
        if (mValue == null) {
            if (other.mValue != null)
                return false;
        }
        else if (!mValue.equals(other.mValue))
            return false;
        return true;
    }
}
