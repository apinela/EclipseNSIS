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

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.DimensionPropertySource;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsDialog extends InstallOptionsElement implements IInstallOptionsConstants
{
    public static final String PROPERTY_SIZE = "net.sf.eclipsensis.installoptions.size"; //$NON-NLS-1$
    public static final String PROPERTY_CHILDREN = "net.sf.eclipsensis.installoptions.children"; //$NON-NLS-1$
    public static final String PROPERTY_DIALOG_SIZE_VISIBLE = "net.sf.eclipsensis.installoptions.dialog_size_visible"; //$NON-NLS-1$

    protected static IPropertyDescriptor[] cDescriptors;

    protected Point mLocation = new Point(0, 0);

    protected Dimension mSize = new Dimension(-1, -1);

    protected List mChildren = new ArrayList();

    private static Image INSTALLOPTIONS_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.dialog.icon")); //$NON-NLS-1$

    protected InstallOptionsRuler leftRuler, mTopRuler;
    
    private boolean mDialogSizeVisible;

    static {
        PropertyDescriptor dimensionPropertyDescriptor = new PropertyDescriptor(PROPERTY_SIZE, InstallOptionsPlugin.getResourceString("size.property.name")); //$NON-NLS-1$
        dimensionPropertyDescriptor.setLabelProvider(new LabelProvider(){
            public String getText(Object element)
            {
                if(element instanceof Dimension) {
                    Dimension dim = (Dimension)element;
                    return new StringBuffer("(").append(dim.width).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                            dim.height).append(")").toString(); //$NON-NLS-1$
                }
                return super.getText(element);
            }
        });
        cDescriptors = new IPropertyDescriptor[]{dimensionPropertyDescriptor};
    }

    public InstallOptionsDialog()
    {
        mSize.width = 100;
        mSize.height = 100;
        mLocation.x = 0;
        mLocation.y = 0;
        createRulers();
    }

    public void setPropertyValue(Object id, Object value)
    {
        if (PROPERTY_SIZE.equals(id)) {
            setSize((Dimension)value);
        }
    }

    public void setSize(Dimension d)
    {
        if (mSize.equals(d)) {
            return;
        }
        Dimension oldSize = mSize;
        mSize = d.getCopy();
        firePropertyChange(PROPERTY_SIZE, oldSize, mSize); //$NON-NLS-1$
    }

    public Point getLocation()
    {
        return mLocation;
    }

    public Dimension getSize()
    {
        return mSize;
    }

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return cDescriptors;
    }

    public Object getPropertyValue(Object propName)
    {
        if (PROPERTY_SIZE.equals(propName)) {
            return new DimensionPropertySource(getSize());
        }
        return super.getPropertyValue(propName);
    }

    protected void fireChildAdded(String prop, Object child, Object index) {
        mListeners.firePropertyChange(prop, index, child);
    }

    protected void fireChildRemoved(String prop, Object child) {
        mListeners.firePropertyChange(prop, child, null);
    }

    public void addChild(InstallOptionsWidget child){
        addChild(child, -1);
    }

    public void addChild(InstallOptionsWidget child, int index){
        synchronized(this) {
            if (index >= 0) {
                mChildren.add(index,child);
            }
            else {
                mChildren.add(child);
                index = mChildren.indexOf(child);
            }
            updateChildIndices(index);
            child.setParent(this);
            fireChildAdded(PROPERTY_CHILDREN, child, new Integer(index));
        }
    }
    
    private void updateChildIndices(int index)
    {
        int size = mChildren.size();
        if(index < size) {
            for(int i=index; i<size; i++) {
                ((InstallOptionsWidget)mChildren.get(i)).setIndex(i);
            }
        }
    }

    public List getChildren(){
        return Collections.unmodifiableList(mChildren);
    }

    public void removeChild(InstallOptionsWidget child){
        synchronized(this) {
            int index = mChildren.indexOf(child);
            if(index >= 0) {
                mChildren.remove(child);
                child.setParent(null);
                updateChildIndices(index);
                fireChildRemoved(PROPERTY_CHILDREN, child);
            }
        }
    }

    public void removeChild(int index){
        removeChild((InstallOptionsWidget)mChildren.get(index));
    }

    protected void createRulers()
    {
        leftRuler = new InstallOptionsRuler(false);
        mTopRuler = new InstallOptionsRuler(true);
    }

    public Image getIconImage()
    {
        return INSTALLOPTIONS_ICON;
    }

    public InstallOptionsRuler getRuler(int orientation)
    {
        InstallOptionsRuler result = null;
        switch (orientation)
        {
            case PositionConstants.NORTH:
                result = mTopRuler;
                break;
            case PositionConstants.WEST:
                result = leftRuler;
                break;
        }
        return result;
    }

    public boolean isDialogSizeVisible()
    {
        return mDialogSizeVisible;
    }
    
    public void setDialogSizeVisible(boolean dialogSizeVisible)
    {
        if(mDialogSizeVisible != dialogSizeVisible) {
            boolean oldDialogSizeVisible = mDialogSizeVisible;
            mDialogSizeVisible = dialogSizeVisible;
            firePropertyChange(PROPERTY_DIALOG_SIZE_VISIBLE,Boolean.valueOf(oldDialogSizeVisible),Boolean.valueOf(mDialogSizeVisible));
        }
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsDialog dialog = (InstallOptionsDialog)super.clone();
        dialog.mLocation = new Point(mLocation);
        dialog.mSize = new Dimension(mSize);
        return dialog;
    }

    public String toString()
    {
        return InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
    }
}