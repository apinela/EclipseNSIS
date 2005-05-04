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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.ui.views.properties.*;

public class DimensionPropertySource implements IPropertySource
{
    public static String ID_WIDTH = "width"; //$NON-NLS-1$

    public static String ID_HEIGHT = "height";//$NON-NLS-1$

    protected static IPropertyDescriptor[] mDescriptors;

    static {
        PropertyDescriptor widthProp = new TextPropertyDescriptor(ID_WIDTH,
                "a1:"+InstallOptionsPlugin.getResourceString("width.property.name")); //$NON-NLS-1$ //$NON-NLS-2$
        widthProp.setValidator(NumberCellEditorValidator.instance());
        PropertyDescriptor heightProp = new TextPropertyDescriptor(ID_HEIGHT,
                "a2:"+InstallOptionsPlugin.getResourceString("height.property.name")); //$NON-NLS-1$ //$NON-NLS-2$
        heightProp.setValidator(NumberCellEditorValidator.instance());
        mDescriptors = new IPropertyDescriptor[]{widthProp, heightProp};
    }

    protected Dimension mDimension = null;

    public DimensionPropertySource(Dimension dimension)
    {
        this.mDimension = dimension.getCopy();
    }

    public Object getEditableValue()
    {
        return mDimension.getCopy();
    }

    public Object getPropertyValue(Object propName)
    {
        return getPropertyValue((String)propName);
    }

    public Object getPropertyValue(String propName)
    {
        if (ID_HEIGHT.equals(propName)) {
            return new String(new Integer(mDimension.height).toString());
        }
        if (ID_WIDTH.equals(propName)) {
            return new String(new Integer(mDimension.width).toString());
        }
        return null;
    }

    public void setPropertyValue(Object propName, Object value)
    {
        setPropertyValue((String)propName, value);
    }

    public void setPropertyValue(String propName, Object value)
    {
        if (ID_HEIGHT.equals(propName)) {
            Integer newInt = new Integer((String)value);
            mDimension.height = newInt.intValue();
        }
        if (ID_WIDTH.equals(propName)) {
            Integer newInt = new Integer((String)value);
            mDimension.width = newInt.intValue();
        }
    }

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return mDescriptors;
    }

    public void resetPropertyValue(String propName)
    {
    }

    public void resetPropertyValue(Object propName)
    {
    }

    public boolean isPropertySet(Object propName)
    {
        return true;
    }

    public boolean isPropertySet(String propName)
    {
        if (ID_HEIGHT.equals(propName) || ID_WIDTH.equals(propName))
            return true;
        return false;
    }

    public String toString()
    {
        return new String("(" + mDimension.width + "," + mDimension.height + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}