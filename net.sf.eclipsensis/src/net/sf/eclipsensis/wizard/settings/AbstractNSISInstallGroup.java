/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.util.*;

import net.sf.eclipsensis.util.Common;

public abstract class AbstractNSISInstallGroup implements INSISInstallElement
{
	private static final long serialVersionUID = 6871218426689788748L;

    protected LinkedHashSet mChildTypes = new LinkedHashSet();
    protected ArrayList mChildren = new ArrayList();
    protected INSISInstallElement mParent = null;
    protected NSISWizardSettings mSettings = null;
    private transient boolean mExpanded = true;
    
    /**
     * 
     */
    public AbstractNSISInstallGroup()
    {
        setChildTypes();
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isRemovable()
     */
    public boolean isRemovable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#hasChildren()
     */
    public boolean hasChildren()
    {
        return mChildren.size() > 0;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildren()
     */
    public INSISInstallElement[] getChildren()
    {
        return (INSISInstallElement[])mChildren.toArray(new INSISInstallElement[0]);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildTypes()
     */
    public String[] getChildTypes()
    {
        return (String[])mChildTypes.toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#addChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public void addChild(INSISInstallElement child)
    {
        if(child != null && mChildTypes.contains(child.getType()) && !mChildren.contains(child)) {
            INSISInstallElement oldParent = child.getParent();
            if(oldParent != null) {
                oldParent.removeChild(child);
            }
            mChildren.add(child);
            child.setParent(this);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public void removeChild(INSISInstallElement child)
    {
        if(child != null && mChildTypes.contains(child.getType()) && mChildren.contains(child)) {
            mChildren.remove(child);
            child.setParent(null);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeAllChildren()
     */
    public void removeAllChildren()
    {
        for(Iterator iter=mChildren.iterator(); iter.hasNext(); ) {
            INSISInstallElement child = (INSISInstallElement)iter.next();
            iter.remove();
            child.setParent(null);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#setParent(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public void setParent(INSISInstallElement parent)
    {
        mParent = parent;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getParent()
     */
    public INSISInstallElement getParent()
    {
        return mParent;
    }

    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
    }
    
    public NSISWizardSettings getSettings()
    {
        return mSettings;
    }
    
    /**
     * @return Returns the expanded.
     */
    public boolean isExpanded()
    {
        return mExpanded;
    }
    
    /**
     * @param expanded The expanded to set.
     */
    public void setExpanded(boolean expanded)
    {
        setExpanded(expanded, false);
    }
    
    /**
     * @param expanded The expanded to set.
     * @param recursive Perform recursively
     */
    public void setExpanded(boolean expanded, boolean recursive)
    {
        mExpanded = expanded;
        if(recursive) {
            if(!Common.isEmptyCollection(mChildren)) {
                for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                    INSISInstallElement child = (INSISInstallElement)iter.next();
                    if(child instanceof AbstractNSISInstallGroup) {
                        ((AbstractNSISInstallGroup)child).setExpanded(expanded, recursive);
                    }
                }
            }
        }
    }
    
    public final void resetChildTypes(boolean recursive)
    {
        setChildTypes();
        if(recursive) {
            if(!Common.isEmptyCollection(mChildren)) {
                for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                    INSISInstallElement child = (INSISInstallElement)iter.next();
                    if(child instanceof AbstractNSISInstallGroup) {
                        ((AbstractNSISInstallGroup)child).resetChildTypes(recursive);
                    }
                }
            }
        }
    }
    
    public abstract void setChildTypes();
}
