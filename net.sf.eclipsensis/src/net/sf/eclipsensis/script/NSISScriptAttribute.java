/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

public class NSISScriptAttribute extends AbstractNSISScriptElement
{
    private String mName = null;
    private Object mArgs = null;
    /**
     * @param name
     */
    public NSISScriptAttribute(String name)
    {
        super(name);
        mName = name;
    }

    /**
     * @param name
     * @param arg
     */
    public NSISScriptAttribute(String name, Object args)
    {
        super(name,args);
        mName = name;
        mArgs = args;
    }

    /**
     * @return Returns the args.
     */
    public Object getArgs()
    {
        return mArgs;
    }

    /**
     * @param args The args to set.
     */
    public void setArgs(Object args)
    {
        mArgs = args;
        updateArgs();
    }

    /**
     *
     */
    private void updateArgs()
    {
        updateArgs(makeArray(mName,mArgs));
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
}
