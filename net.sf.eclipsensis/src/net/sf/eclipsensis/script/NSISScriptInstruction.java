/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

public class NSISScriptInstruction extends AbstractNSISScriptElement
{
    private String mName = null;
    private Object mArg = null;

    /**
     * @param name
     */
    public NSISScriptInstruction(String name)
    {
        super(name);
        mName = name;
    }

    /**
     * @param name
     * @param arg
     */
    public NSISScriptInstruction(String name, Object arg)
    {
        super(name,arg);
        mName = name;
        mArg = arg;
    }

    /**
     * @return Returns the args.
     */
    public Object getArg()
    {
        return mArg;
    }

    /**
     * @param args The args to set.
     */
    public void setArg(Object args)
    {
        mArg = args;
        updateArgs();
    }

    /**
     *
     */
    private void updateArgs()
    {
        updateArgs(makeArray(mName,mArg));
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
