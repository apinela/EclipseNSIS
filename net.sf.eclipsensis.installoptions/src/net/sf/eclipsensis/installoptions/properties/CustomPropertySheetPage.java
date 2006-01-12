/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;

public class CustomPropertySheetPage extends PropertySheetPage
{
    private static final PropertySheetSorter cNonSorter = new PropertySheetSorter() {
        public int compare(IPropertySheetEntry entryA, IPropertySheetEntry entryB)
        {
            return -1;
        }

        public int compareCategories(String categoryA, String categoryB)
        {
            return -1;
        }
    };

    private InstallOptionsEditDomain mEditDomain;

    public CustomPropertySheetPage(InstallOptionsEditDomain editDomain)
    {
        super();
        mEditDomain = editDomain;
        super.setSorter(cNonSorter);
    }

    public void createControl(Composite parent)
    {
        super.createControl(parent);
        super.setPropertySourceProvider(new CustomPropertySourceProvider());
    }

    protected void setSorter(PropertySheetSorter sorter)
    {
    }

    public void setPropertySourceProvider(IPropertySourceProvider newProvider)
    {
    }

    private class CustomPropertySourceProvider implements IPropertySourceProvider
    {
        public IPropertySource getPropertySource(Object object)
        {
            IPropertySource source = null;
            if(object instanceof IPropertySource) {
                source = (IPropertySource)object;
            }
            else if(object instanceof IAdaptable) {
                source = (IPropertySource)((IAdaptable)object).getAdapter(IPropertySource.class);
            }
            if(source != null && !(source instanceof CustomPropertySource)) {
                source = new CustomPropertySource(source);
            }
            return source;
        }
    }

    public class CustomPropertySource implements IPropertySource
    {
        private IPropertySource mDelegate;
        private Map mDescriptors = new HashMap();

        public CustomPropertySource(IPropertySource delegate)
        {
            super();
            mDelegate = delegate;
        }

        public Object getEditableValue()
        {
            Object value = mDelegate.getEditableValue();
            if(value instanceof IPropertySource && !(value instanceof CustomPropertySource)) {
                value = new CustomPropertySource((IPropertySource)value);
            }
            return value;
        }

        public IPropertyDescriptor[] getPropertyDescriptors()
        {
            List list = new ArrayList();
            IPropertyDescriptor[] descriptors = mDelegate.getPropertyDescriptors();
            if(!Common.isEmptyArray(descriptors)) {
                for (int i = 0; i < descriptors.length; i++) {
                    IPropertyDescriptor descriptor = (IPropertyDescriptor)mDescriptors.get(descriptors[i].getId());
                    if(descriptor == null) {
                        if(!(descriptors[i] instanceof ReadOnlyPropertyDescriptor)) {
                            descriptor = new ReadOnlyPropertyDescriptor(descriptors[i]);
                        }
                        else {
                            descriptor = descriptors[i];
                        }
                        mDescriptors.put(descriptors[i].getId(), descriptor);
                    }
                    list.add(descriptor);
                }
            }
            return (IPropertyDescriptor[])list.toArray(new IPropertyDescriptor[list.size()]);
        }

        public Object getPropertyValue(Object id)
        {
            return mDelegate.getPropertyValue(id);
        }

        public boolean isPropertySet(Object id)
        {
            return mDelegate.isPropertySet(id);
        }

        public void resetPropertyValue(Object id)
        {
            mDelegate.resetPropertyValue(id);
        }

        public void setPropertyValue(Object id, Object value)
        {
            mDelegate.setPropertyValue(id,value);
        }
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
