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
import net.sf.eclipsensis.installoptions.properties.*;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomComboBoxPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.properties.validators.NumberCellEditorValidator;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsRuler;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.UpDownMover;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.*;

public class InstallOptionsDialog extends InstallOptionsElement implements IInstallOptionsConstants
{
    private static final int DEFAULT_OPTION = 0;
    private static final String[] OPTION_DATA = {"","0","1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String[] OPTION_DISPLAY = {InstallOptionsPlugin.getResourceString("option.default"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("option.no"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("option.yes")}; //$NON-NLS-1$
    private static Image INSTALLOPTIONS_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.dialog.icon")); //$NON-NLS-1$
    private static LabelProvider cDefaultLabelProvider = new LabelProvider(){
        public String getText(Object element)
        {
            if(element instanceof String) {
                if(Common.isEmpty((String)element)) {
                    return InstallOptionsPlugin.getResourceString("value.default"); //$NON-NLS-1$
                }
                else {
                    return (String)element;
                }
            }
            return super.getText(element);
        }
    };

    protected IPropertyDescriptor[] mDescriptors;
    protected Point mLocation = new Point(0, 0);
    protected Dimension mSize = new Dimension(-1, -1);
    protected List mChildren = new ArrayList();
    protected InstallOptionsRuler mLeftRuler;
    protected InstallOptionsRuler mTopRuler;
    private boolean mDialogSizeVisible;
    private String mTitle=""; //$NON-NLS-1$
    private String mCancelEnabled=""; //$NON-NLS-1$
    private String mCancelShow=""; //$NON-NLS-1$
    private String mBackEnabled=""; //$NON-NLS-1$
    private String mCancelButtonText=""; //$NON-NLS-1$
    private String mNextButtonText=""; //$NON-NLS-1$
    private String mBackButtonText=""; //$NON-NLS-1$
    private String mRect=""; //$NON-NLS-1$
    private String mRTL=""; //$NON-NLS-1$
    
    private int[] mSelectedIndices = null;
    private UpDownMover mUpDownMover = null;
    
    public InstallOptionsDialog()
    {
        super(InstallOptionsModel.TYPE_DIALOG);
        mSize.width = 100;
        mSize.height = 100;
        mLocation.x = 0;
        mLocation.y = 0;
        init();
    }

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return mDescriptors;
    }
    
    public void setChildren(List children)
    {
        mChildren.clear();
        mChildren.addAll(children);
        updateChildIndices(0);
        mListeners.firePropertyChange(InstallOptionsModel.PROPERTY_CHILDREN, null, null);
    }

    public boolean canSendBackward()
    {
        return mUpDownMover.canMoveUp();
    }

    public boolean canSendToBack()
    {
        return mUpDownMover.canMoveUp();
    }

    public boolean canBringForward()
    {
        return mUpDownMover.canMoveDown();
    }

    public boolean canBringToFront()
    {
        return mUpDownMover.canMoveDown();
    }
    
    public void sendBackward()
    {
        mUpDownMover.moveUp();
    }
    
    public void bringForward()
    {
        mUpDownMover.moveDown();
    }
    
    public void sendToBack()
    {
        mUpDownMover.moveToTop();
    }
    
    public void bringToFront()
    {
        mUpDownMover.moveToBottom();
    }
    
    public void setSelection(List selection)
    {
        if(Common.isEmptyCollection(selection)) {
            mSelectedIndices = new int[0];
        }
        mSelectedIndices = new int[selection.size()];
        for (int i = 0; i < mSelectedIndices.length; i++) {
            mSelectedIndices[i] = mChildren.indexOf(selection.get(i));
        }
        Arrays.sort(mSelectedIndices);
    }

    protected void createPropertyDescriptors()
    {
        ArrayList list = new ArrayList();
        PropertyDescriptor dimensionPropertyDescriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_SIZE, InstallOptionsPlugin.getResourceString("size.property.name")); //$NON-NLS-1$
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
        list.add(dimensionPropertyDescriptor);
        PropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_TITLE, InstallOptionsPlugin.getResourceString("title.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_TITLE));
        list.add(descriptor);
        list.add(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_ENABLED, InstallOptionsPlugin.getResourceString("cancel.enabled.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION)); //$NON-NLS-1$
        list.add(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_SHOW, InstallOptionsPlugin.getResourceString("cancel.show.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION)); //$NON-NLS-1$
        list.add(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_BACK_ENABLED, InstallOptionsPlugin.getResourceString("back.enabled.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION)); //$NON-NLS-1$
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT, InstallOptionsPlugin.getResourceString("cancel.button.text.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT));
        list.add(descriptor);
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT, InstallOptionsPlugin.getResourceString("back.button.text.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT));
        list.add(descriptor);
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT, InstallOptionsPlugin.getResourceString("next.button.text.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT));
        list.add(descriptor);
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_RECT, InstallOptionsPlugin.getResourceString("rect.property.name")); //$NON-NLS-1$
        descriptor.setValidator(new NumberCellEditorValidator(1,Integer.MAX_VALUE,true));
        descriptor.setLabelProvider(cDefaultLabelProvider);
        list.add(descriptor);
        list.add(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_RTL, InstallOptionsPlugin.getResourceString("rtl.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION)); //$NON-NLS-1$
        mDescriptors = (IPropertyDescriptor[])list.toArray(new IPropertyDescriptor[list.size()]);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if (InstallOptionsModel.PROPERTY_SIZE.equals(id)) {
            setSize((Dimension)value);
        }
        else if(InstallOptionsModel.PROPERTY_TITLE.equals(id)) {
            setTitle((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_ENABLED.equals(id)) {
            setCancelEnabled((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_SHOW.equals(id)) {
            setCancelShow((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT.equals(id)) {
            setCancelButtonText((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_BACK_ENABLED.equals(id)) {
            setBackEnabled((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT.equals(id)) {
            setBackButtonText((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT.equals(id)) {
            setNextButtonText((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_RECT.equals(id)) {
            setRect((String)value);
        }
        else if(InstallOptionsModel.PROPERTY_RTL.equals(id)) {
            setRTL((String)value);
        }
    }

    public String getBackButtonText()
    {
        return mBackButtonText;
    }
    
    public void setBackButtonText(String backButtonText)
    {
        mBackButtonText = backButtonText;
    }
    
    public String getBackEnabled()
    {
        return mBackEnabled;
    }
    
    public void setBackEnabled(String backEnabled)
    {
        mBackEnabled = backEnabled;
    }
    
    public String getCancelButtonText()
    {
        return mCancelButtonText;
    }
    
    public void setCancelButtonText(String cancelButtonText)
    {
        mCancelButtonText = cancelButtonText;
    }
    
    public String getCancelEnabled()
    {
        return mCancelEnabled;
    }
    
    public void setCancelEnabled(String cancelEnabled)
    {
        mCancelEnabled = cancelEnabled;
    }
    
    public String getCancelShow()
    {
        return mCancelShow;
    }
    
    public void setCancelShow(String cancelShow)
    {
        mCancelShow = cancelShow;
    }
    
    public String getNextButtonText()
    {
        return mNextButtonText;
    }
    
    public void setNextButtonText(String nextButtonText)
    {
        mNextButtonText = nextButtonText;
    }
    public String getRect()
    {
        return mRect;
    }
    
    public void setRect(String rect)
    {
        mRect = rect;
    }
    
    public String getRTL()
    {
        return mRTL;
    }
    
    public void setRTL(String rtl)
    {
        mRTL = rtl;
    }
    
    public String getTitle()
    {
        return mTitle;
    }
    
    public void setTitle(String title)
    {
        mTitle = title;
    }
    
    public void setSize(Dimension d)
    {
        if (mSize.equals(d)) {
            return;
        }
        Dimension oldSize = mSize;
        mSize = d.getCopy();
        firePropertyChange(InstallOptionsModel.PROPERTY_SIZE, oldSize, mSize); //$NON-NLS-1$
    }

    public Point getLocation()
    {
        return mLocation;
    }

    public Dimension getSize()
    {
        return mSize;
    }

    public Object getPropertyValue(Object id)
    {
        if (InstallOptionsModel.PROPERTY_SIZE.equals(id)) {
            return new DimensionPropertySource(getSize());
        }
        else if(InstallOptionsModel.PROPERTY_TITLE.equals(id)) {
            return getTitle();
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_ENABLED.equals(id)) {
            return getCancelEnabled();
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_SHOW.equals(id)) {
            return getCancelShow();
        }
        else if(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT.equals(id)) {
            return getCancelButtonText();
        }
        else if(InstallOptionsModel.PROPERTY_BACK_ENABLED.equals(id)) {
            return getBackEnabled();
        }
        else if(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT.equals(id)) {
            return getBackButtonText();
        }
        else if(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT.equals(id)) {
            return getNextButtonText();
        }
        else if(InstallOptionsModel.PROPERTY_RECT.equals(id)) {
            return getRect();
        }
        else if(InstallOptionsModel.PROPERTY_RTL.equals(id)) {
            return getRTL();
        }
        return super.getPropertyValue(id);
    }

    protected void fireChildAdded(String prop, Object child, Object index) 
    {
        mListeners.firePropertyChange(prop, index, child);
    }

    protected void fireChildMoved(String prop, Object child, Object index) 
    {
        mListeners.firePropertyChange(prop, child, index);
    }

    protected void fireChildRemoved(String prop, Object child) 
    {
        mListeners.firePropertyChange(prop, child, null);
    }

    public void addChild(InstallOptionsWidget child){
        addChild(child, -1);
    }

    public void addChild(InstallOptionsWidget child, int index){
        if (index >= 0) {
            mChildren.add(index,child);
        }
        else {
            mChildren.add(child);
            index = mChildren.indexOf(child);
        }
        updateChildIndices(index);
        child.setParent(this);
        fireChildAdded(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(index));
    }
    
    private void updateChildIndices(int index)
    {
        updateChildIndices(index,mChildren.size()-1);
    }

    private void updateChildIndices(int startIndex, int endIndex)
    {
        if(startIndex <= endIndex) {
            for(int i=startIndex; i<=endIndex; i++) {
                ((InstallOptionsWidget)mChildren.get(i)).setIndex(i);
            }
        }
    }

    public List getChildren(){
        return Collections.unmodifiableList(mChildren);
    }

    public void removeChild(InstallOptionsWidget child){
        int index = mChildren.indexOf(child);
        if(index >= 0) {
            mChildren.remove(child);
            child.setParent(null);
            updateChildIndices(index);
            fireChildRemoved(InstallOptionsModel.PROPERTY_CHILDREN, child);
        }
    }


    public void moveChild(InstallOptionsWidget child, int newIndex){
        int oldIndex = mChildren.indexOf(child);
        if(oldIndex >= 0 && newIndex >= 0 && oldIndex != newIndex) {
            mChildren.remove(child);
            mChildren.add(newIndex,child);
            updateChildIndices(Math.min(oldIndex,newIndex),Math.max(oldIndex,newIndex));
            fireChildMoved(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(newIndex));
        }
    }

    public void removeChild(int index){
        removeChild((InstallOptionsWidget)mChildren.get(index));
    }

    protected void init()
    {
        mLeftRuler = new InstallOptionsRuler(false);
        mTopRuler = new InstallOptionsRuler(true);
        mUpDownMover = new UpDownMover() {
            protected int[] getSelectedIndices()
            {
                return mSelectedIndices;
            }

            protected List getAllElements()
            {
                return mChildren;
            }

            protected void updateElements(List elements, List move, boolean isDown)
            {
                setChildren(elements);
            }
        };
        createPropertyDescriptors();
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
                result = mLeftRuler;
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
            firePropertyChange(InstallOptionsModel.PROPERTY_DIALOG_SIZE_VISIBLE,Boolean.valueOf(oldDialogSizeVisible),Boolean.valueOf(mDialogSizeVisible));
        }
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsDialog dialog = (InstallOptionsDialog)super.clone();
        dialog.mLocation = new Point(mLocation);
        dialog.setSize(new Dimension(mSize));
        if(dialog.mSelectedIndices != null) {
            dialog.mSelectedIndices = (int[])dialog.mSelectedIndices.clone();
        }
        dialog.init();
        ArrayList list = new ArrayList();;
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            InstallOptionsWidget child = (InstallOptionsWidget)((InstallOptionsWidget)iter.next()).clone();
            child.setParent(dialog);
            list.add(child);
        }
        dialog.setChildren(list);
        dialog.setBackButtonText(getBackButtonText());
        dialog.setBackEnabled(getBackEnabled());
        dialog.setCancelButtonText(getCancelButtonText());
        dialog.setCancelEnabled(getCancelEnabled());
        dialog.setCancelShow(getCancelShow());
        dialog.setDialogSizeVisible(mDialogSizeVisible);
        dialog.setNextButtonText(getNextButtonText());
        dialog.setRect(getRect());
        dialog.setRTL(getRTL());
        dialog.setTitle(getTitle());
        return dialog;
    }

    public String toString()
    {
        return InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
    }
}