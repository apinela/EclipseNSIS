/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.descriptors;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class CustomPropertyDescriptor implements IPropertyDescriptor
{
    private IPropertyDescriptor mDelegate;
    private String mPrefix;

    public CustomPropertyDescriptor(IPropertyDescriptor delegate, int sortOrder)
    {
        mDelegate = delegate;
        mPrefix = MessageFormat.format("a{0,number,000}:",new Object[]{new Integer(sortOrder)}); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
     */
    public CellEditor createPropertyEditor(Composite parent)
    {
        return mDelegate.createPropertyEditor(parent);
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
        return mPrefix+mDelegate.getDisplayName();
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

    public IPropertyDescriptor getDelegate()
    {
        return mDelegate;
    }
}
