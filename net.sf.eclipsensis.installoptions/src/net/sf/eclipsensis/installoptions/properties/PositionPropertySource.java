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
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.Position;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomPropertyDescriptor;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.*;

public class PositionPropertySource implements IPropertySource
{
    public static String ID_LEFT = "left"; //$NON-NLS-1$
    public static String ID_TOP = "top";//$NON-NLS-1$
    public static String ID_RIGHT = "right"; //$NON-NLS-1$
    public static String ID_BOTTOM = "bottom";//$NON-NLS-1$

    private IPropertyDescriptor[] mDescriptors;

    private void createDescriptors()
    {
        PropertyDescriptor leftProp = new TextPropertyDescriptor(ID_LEFT,
                InstallOptionsPlugin.getResourceString("left.property.name")); //$NON-NLS-1$ //$NON-NLS-2$
        leftProp.setValidator(new PositionCellEditorValidator(ID_LEFT));
        PropertyDescriptor topProp = new TextPropertyDescriptor(ID_TOP,
                InstallOptionsPlugin.getResourceString("top.property.name")); //$NON-NLS-1$ //$NON-NLS-2$
        topProp.setValidator(new PositionCellEditorValidator(ID_TOP));
        PropertyDescriptor rightProp = new TextPropertyDescriptor(ID_RIGHT,
                InstallOptionsPlugin.getResourceString("right.property.name")); //$NON-NLS-1$ //$NON-NLS-2$
        rightProp.setValidator(new PositionCellEditorValidator(ID_RIGHT));
        PropertyDescriptor bottomProp = new TextPropertyDescriptor(ID_BOTTOM,
                InstallOptionsPlugin.getResourceString("bottom.property.name")); //$NON-NLS-1$ //$NON-NLS-2$
        bottomProp.setValidator(new PositionCellEditorValidator(ID_BOTTOM));
        mDescriptors = new IPropertyDescriptor[]{new CustomPropertyDescriptor(leftProp,0), 
                                                 new CustomPropertyDescriptor(topProp,1), 
                                                 new CustomPropertyDescriptor(rightProp,2),
                                                 new CustomPropertyDescriptor(bottomProp,3)};
    }

    protected InstallOptionsWidget mWidget = null;
    protected Position mPosition = null;

    public PositionPropertySource(InstallOptionsWidget widget)
    {
        mWidget = widget;
        mPosition = mWidget.getPosition().getCopy();
        createDescriptors();
    }

    public Object getEditableValue()
    {
        return mPosition.getCopy();
    }

    public Object getPropertyValue(Object propName)
    {
        return getPropertyValue((String)propName);
    }

    public Object getPropertyValue(String propName)
    {
        if (ID_TOP.equals(propName)) {
            return new String(new Integer(mPosition.top).toString());
        }
        if (ID_LEFT.equals(propName)) {
            return new String(new Integer(mPosition.left).toString());
        }
        if (ID_RIGHT.equals(propName)) {
            return new String(new Integer(mPosition.right).toString());
        }
        if (ID_BOTTOM.equals(propName)) {
            return new String(new Integer(mPosition.bottom).toString());
        }
        return null;
    }

    public void setPropertyValue(Object propName, Object value)
    {
        setPropertyValue((String)propName, value);
    }

    public void setPropertyValue(String propName, Object value)
    {
        if (ID_TOP.equals(propName)) {
            mPosition.top = Integer.parseInt((String)value);
        }
        if (ID_LEFT.equals(propName)) {
            mPosition.left = Integer.parseInt((String)value);
        }
        if (ID_RIGHT.equals(propName)) {
            mPosition.right = Integer.parseInt((String)value);
        }
        if (ID_BOTTOM.equals(propName)) {
            mPosition.bottom = Integer.parseInt((String)value);
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
        return isPropertySet((String)propName);
    }

    public boolean isPropertySet(String propName)
    {
        if (ID_TOP.equals(propName) || ID_LEFT.equals(propName) || 
            ID_RIGHT.equals(propName) || ID_BOTTOM.equals(propName))
            return true;
        return false;
    }

    public String toString()
    {
        return new StringBuffer("(").append(mPosition.left).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
            mPosition.top).append(",").append(mPosition.right).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
            mPosition.bottom).append(")").toString(); //$NON-NLS-1$
    }
    
    private class PositionCellEditorValidator implements ICellEditorValidator
    {
        private String mId;
        
        public PositionCellEditorValidator(String id)
        {
            mId = id;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
         */
        public String isValid(Object value)
        {
            try {
                int val = Integer.parseInt((String)value);
                Position pos = mPosition.getCopy();
                if(mId.equals(ID_LEFT)) {
                    pos.left = val;
                }
                else if(mId.equals(ID_TOP)) {
                    pos.top = val;
                }
                else if(mId.equals(ID_RIGHT)) {
                    pos.right = val;
                }
                else if(mId.equals(ID_BOTTOM)) {
                    pos.bottom = val;
                }
                pos = mWidget.toGraphical(pos);
                if(pos.left > pos.right) {
                    return InstallOptionsPlugin.getResourceString("position.horizontal.error.message"); //$NON-NLS-1$
                }
                else if(pos.top > pos.bottom) {
                    return InstallOptionsPlugin.getResourceString("position.vertical.error.message"); //$NON-NLS-1$
                }
                return null;
            }
            catch (NumberFormatException nfe){
                return InstallOptionsPlugin.getResourceString("number.error.message"); //$NON-NLS-1$
            }
        }
        
    }
}