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

import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomPropertyDescriptor;

import org.eclipse.gef.internal.ui.properties.PropertySheetEntry;
import org.eclipse.gef.internal.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class CustomPropertySheetEntry extends UndoablePropertySheetEntry
{
    private InstallOptionsEditDomain mEditDomain;
    
    /**
     * @param stack
     */
    public CustomPropertySheetEntry(InstallOptionsEditDomain editDomain)
    {
        super(editDomain.getCommandStack());
        mEditDomain = editDomain;
    }

    protected PropertySheetEntry createChildEntry()
    {
        return new CustomPropertySheetEntry(mEditDomain);
    }

    protected List computeMergedPropertyDescriptors()
    {
        List list = super.computeMergedPropertyDescriptors();
        ArrayList list2 = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            IPropertyDescriptor element = (IPropertyDescriptor)iter.next();
            if(element instanceof CustomPropertyDescriptor) {
                list2.add(new ReadOnlyPropertyDescriptor(((CustomPropertyDescriptor)element).getDelegate()));
            }
            else {
                list2.add(new ReadOnlyPropertyDescriptor(element));
            }
        }
        return list2;
    }
    
    private class ReadOnlyPropertyDescriptor implements IPropertyDescriptor
    {
        private IPropertyDescriptor mDelegate;
        
        /**
         * 
         */
        public ReadOnlyPropertyDescriptor(IPropertyDescriptor delegate)
        {
            super();
            mDelegate = delegate;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
         */
        public CellEditor createPropertyEditor(Composite parent)
        {
            if(mEditDomain.isReadOnly()) {
                return null;
            }
            else {
                return mDelegate.createPropertyEditor(parent);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getCategory()
         */
        public String getCategory()
        {
            return mDelegate.getCategory();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getDescription()
         */
        public String getDescription()
        {
            return mDelegate.getDescription();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getDisplayName()
         */
        public String getDisplayName()
        {
            return mDelegate.getDisplayName();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getFilterFlags()
         */
        public String[] getFilterFlags()
        {
            return mDelegate.getFilterFlags();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getHelpContextIds()
         */
        public Object getHelpContextIds()
        {
            return mDelegate.getHelpContextIds();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getId()
         */
        public Object getId()
        {
            return mDelegate.getId();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getLabelProvider()
         */
        public ILabelProvider getLabelProvider()
        {
            return mDelegate.getLabelProvider();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.properties.IPropertyDescriptor#isCompatibleWith(org.eclipse.ui.views.properties.IPropertyDescriptor)
         */
        public boolean isCompatibleWith(IPropertyDescriptor anotherProperty)
        {
            return mDelegate.isCompatibleWith(anotherProperty);
        }
    }
}
