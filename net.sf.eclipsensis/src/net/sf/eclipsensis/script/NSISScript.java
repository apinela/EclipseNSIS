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

import java.util.Iterator;

import net.sf.eclipsensis.INSISConstants;

public class NSISScript extends AbstractNSISScriptElementContainer
{
    private String mName;
    private boolean mHasUninstall = false;

    /**
     * @param name
     */
    public NSISScript()
    {
        this(""); //$NON-NLS-1$
    }

    /**
     * @param name
     */
    public NSISScript(String name)
    {
        super("Name",name); //$NON-NLS-1$
        mName = name;
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
        updateArgs(name);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    public void write(NSISScriptWriter writer)
    {
        super.write(writer);
        writer.println();
        writeElements(writer);
    }

    public void compact()
    {
        INSISScriptElement previous = null;
        for (Iterator iter = mElements.iterator(); iter.hasNext();) {
            INSISScriptElement element = (INSISScriptElement)iter.next();
            if(previous instanceof NSISScriptBlankLine && element instanceof NSISScriptBlankLine) {
                iter.remove();
            }
            else {
                previous = element;
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.AbstractNSISScriptElementContainer#validateElement(net.sf.eclipsensis.script.INSISScriptElement)
     */
    protected void validateElement(INSISScriptElement element) throws InvalidNSISScriptElementException
    {
        if(element != null) {
            if(element instanceof NSISScriptAttribute || element instanceof NSISScriptFunction ||
               element instanceof NSISScriptSectionGroup || element instanceof NSISScriptInclude ||
               element instanceof NSISScriptMacro) {
                return;
            }
            else if(element instanceof NSISScriptSection) {
                if(!((NSISScriptSection)element).getName().equalsIgnoreCase(INSISConstants.UNINSTALL_SECTION_NAME)) {
                    return;
                }
                else if(!mHasUninstall) {
                    mHasUninstall = true;
                    return;
                }
            }
        }
        super.validateElement(element);
    }

    public void append(NSISScriptlet scriptlet) throws InvalidNSISScriptElementException
    {
        for(Iterator iter = scriptlet.mElements.iterator(); iter.hasNext(); ) {
            addElement((INSISScriptElement)iter.next());
        }
    }
}
