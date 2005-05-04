/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.PositionPropertySource;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public abstract class InstallOptionsWidget extends InstallOptionsElement
{
    public static final String PROPERTY_POSITION = "net.sf.eclipsensis.installoptions.position"; //$NON-NLS-1$
    
    protected static IPropertyDescriptor[] cDescriptors;
    static {
        PropertyDescriptor positionPropertyDescriptor = new PropertyDescriptor(PROPERTY_POSITION, InstallOptionsPlugin.getResourceString("position.property.name")); //$NON-NLS-1$
        positionPropertyDescriptor.setLabelProvider(new LabelProvider(){
            public String getText(Object element)
            {
                if(element instanceof Position) {
                    Position pos = (Position)element;
                    return new StringBuffer("(").append(pos.left).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                            pos.top).append(",").append(pos.right).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                            pos.bottom).append(")").toString(); //$NON-NLS-1$
                }
                return super.getText(element);
            }
        });
        cDescriptors = new IPropertyDescriptor[]{positionPropertyDescriptor};
    }
    protected InstallOptionsDialog mParent = null;
    protected int mIndex = -1;
    protected Position mPosition = new Position();
    private InstallOptionsGuide mVerticalGuide, mHorizontalGuide;

    public InstallOptionsGuide getHorizontalGuide()
    {
        return mHorizontalGuide;
    }

    public InstallOptionsGuide getVerticalGuide()
    {
        return mVerticalGuide;
    }

    public void setHorizontalGuide(InstallOptionsGuide hGuide)
    {
        mHorizontalGuide = hGuide;
    }

    public void setVerticalGuide(InstallOptionsGuide vGuide)
    {
        mVerticalGuide = vGuide;
    }

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return cDescriptors;
    }

    public Object getPropertyValue(Object id)
    {
        if (PROPERTY_POSITION.equals(id)) {
            return new PositionPropertySource(this);
        }
        return super.getPropertyValue(id);
    }
    
    public void setPropertyValue(Object id, Object value)
    {
        if(PROPERTY_POSITION.equals(id)) {
            setPosition((Position)value);
        }
    }

    public InstallOptionsDialog getParent()
    {
        return mParent;
    }

    void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
    }
    
    public int getIndex()
    {
        return mIndex;
    }
    
    void setIndex(int index)
    {
        mIndex = index;
    }

    public String toString()
    {
        return InstallOptionsPlugin.getFormattedString("widget.field.format",new Integer[]{new Integer(getIndex())}); //$NON-NLS-1$
    }
    
    public Image getIconImage()
    {
        return null;
    }

    private int toGraphical(int value, int refValue)
    {
        if(value < 0) {
            value = Math.max(value,refValue+value);
        }
        return value;
    }

    public Position toGraphical(Position p)
    {
        p = p.getCopy();
        InstallOptionsDialog dialog = getParent();
        if(dialog == null) {
            p.set(0,0,0,0);
        }
        else {
            Dimension size = dialog.getSize();
            p.left = toGraphical(p.left,size.width);
            p.top = toGraphical(p.top,size.height);
            p.right = toGraphical(p.right,size.width);
            p.bottom = toGraphical(p.bottom,size.height);
        }
        return p;
    }

    private int toModel(int value, int localValue, int refValue)
    {
        if(localValue < 0 && value < refValue) {
            value = Math.max(0,value) - refValue;
        }
        return value;
    }

    public Position toModel(Position p)
    {
        p = p.getCopy();
        InstallOptionsDialog dialog = getParent();
        if(dialog == null) {
            p.set(0,0,0,0);
        }
        else {
            Dimension size = dialog.getSize();
            p.left = toModel(p.left, mPosition.left, size.width);
            p.top = toModel(p.top, mPosition.top, size.height);
            p.right = toModel(p.right, mPosition.right, size.width);
            p.bottom = toModel(p.bottom, mPosition.bottom, size.height);
        }
        return p;
    }

    public Position getPosition()
    {
        return mPosition;
    }
    
    public void setPosition(Position position)
    {
        if(!mPosition.equals(position)) {
            Position mOldPosition = mPosition;
            mPosition = position;
            firePropertyChange(PROPERTY_POSITION,mOldPosition,mPosition);
        }
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsWidget element = (InstallOptionsWidget)super.clone();
        element.mParent = null;
        element.mHorizontalGuide = null;
        element.mVerticalGuide = null;
        element.mPosition = mPosition.getCopy();
        return element;
    }
    
    public abstract String getType();
}
