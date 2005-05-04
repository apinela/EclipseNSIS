/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import java.util.*;
import java.util.HashSet;
import java.util.Set;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySheetEntryListener;

public class CustomPropertySheetEntry implements IPropertySheetEntry, IPropertySheetEntryListener
{
    private IPropertySheetEntry mDelegate;
    private Set mPropertySheetEntryListeners = new HashSet();
    
    /**
     * 
     */
    public CustomPropertySheetEntry(IPropertySheetEntry delegate)
    {
        mDelegate = delegate;
        mDelegate.addPropertySheetEntryListener(this);
    }

    public void childEntriesChanged(IPropertySheetEntry node)
    {
        for(Iterator iter=mPropertySheetEntryListeners.iterator(); iter.hasNext(); ) {
            ((IPropertySheetEntryListener)iter.next()).childEntriesChanged(this);
        }
    }

    public void errorMessageChanged(IPropertySheetEntry entry)
    {
        for(Iterator iter=mPropertySheetEntryListeners.iterator(); iter.hasNext(); ) {
            ((IPropertySheetEntryListener)iter.next()).errorMessageChanged(this);
        }
    }

    public void valueChanged(IPropertySheetEntry entry)
    {
        for(Iterator iter=mPropertySheetEntryListeners.iterator(); iter.hasNext(); ) {
            ((IPropertySheetEntryListener)iter.next()).valueChanged(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#addPropertySheetEntryListener(org.eclipse.ui.views.properties.IPropertySheetEntryListener)
     */
    public void addPropertySheetEntryListener(
            IPropertySheetEntryListener listener)
    {
        mPropertySheetEntryListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#applyEditorValue()
     */
    public void applyEditorValue()
    {
        mDelegate.applyEditorValue();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#dispose()
     */
    public void dispose()
    {
        mDelegate.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getCategory()
     */
    public String getCategory()
    {
        return mDelegate.getCategory();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getChildEntries()
     */
    public IPropertySheetEntry[] getChildEntries()
    {
        IPropertySheetEntry[] childEntries = mDelegate.getChildEntries();
        if(!Common.isEmptyArray(childEntries)) {
            CustomPropertySheetEntry[] newChildEntries = new CustomPropertySheetEntry[childEntries.length];
            for (int i = 0; i < childEntries.length; i++) {
                if(!(childEntries[i] instanceof CustomPropertySheetEntry)) {
                    CustomPropertySheetEntry entry = new CustomPropertySheetEntry(childEntries[i]);
                    newChildEntries[i] = entry;
                }
                else {
                    newChildEntries[i] = (CustomPropertySheetEntry)childEntries[i];
                }
            }
            childEntries = newChildEntries;
        }
        return childEntries;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getDescription()
     */
    public String getDescription()
    {
        return mDelegate.getDescription();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getDisplayName()
     */
    public String getDisplayName()
    {
        String displayName = mDelegate.getDisplayName();
        int colon = displayName.lastIndexOf(':');
        if (colon != -1) {
            displayName = displayName.substring(colon + 1);
        }
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getEditor(org.eclipse.swt.widgets.Composite)
     */
    public CellEditor getEditor(Composite parent)
    {
        return mDelegate.getEditor(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getErrorText()
     */
    public String getErrorText()
    {
        return mDelegate.getErrorText();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getFilters()
     */
    public String[] getFilters()
    {
        return mDelegate.getFilters();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getHelpContextIds()
     */
    public Object getHelpContextIds()
    {
        return mDelegate.getHelpContextIds();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getImage()
     */
    public Image getImage()
    {
        return mDelegate.getImage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#getValueAsString()
     */
    public String getValueAsString()
    {
        return mDelegate.getValueAsString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#hasChildEntries()
     */
    public boolean hasChildEntries()
    {
        return mDelegate.hasChildEntries();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#removePropertySheetEntryListener(org.eclipse.ui.views.properties.IPropertySheetEntryListener)
     */
    public void removePropertySheetEntryListener(IPropertySheetEntryListener listener)
    {
        mPropertySheetEntryListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#resetPropertyValue()
     */
    public void resetPropertyValue()
    {
        mDelegate.resetPropertyValue();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#setValues(java.lang.Object[])
     */
    public void setValues(Object[] values)
    {
        mDelegate.setValues(values);
    }
}
