/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

public class NSISScriptDefine extends AbstractNSISScriptElement
{
    private String mName = null;
    private String mValue = null;

    /**
     * @param name
     * @param value
     */
    public NSISScriptDefine(String name, String value) {
        super("!define", makeArray(name, value)); //$NON-NLS-1$
        mName = name;
        mValue = value;
    }

    /**
     * @param name
     */
    public NSISScriptDefine(String name) {
        super("!define",name); //$NON-NLS-1$
        mName = name;
    }

    private void updateArgs()
    {
        updateArgs(makeArray(mName,mValue));
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mName = name;
        updateArgs();
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return mValue;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value)
    {
        mValue = value;
        updateArgs();
    }
}
