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
import java.util.regex.Matcher;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomComboBoxPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.properties.validators.NumberCellEditorValidator;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsRuler;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.UpDownMover;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
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
    public static Image INSTALLOPTIONS_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.dialog.icon")); //$NON-NLS-1$
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
    protected List mChildren = new ArrayList();
    protected InstallOptionsRuler mLeftRuler;
    protected InstallOptionsRuler mTopRuler;

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
    private INIFile mINIFile;
    private Map mINISectionMap = new HashMap();
    private Dimension mDialogSize = new Dimension(100,100);
    private boolean mShowDialogSize = true;
    
    public InstallOptionsDialog()
    {
        super(InstallOptionsModel.TYPE_DIALOG);
        init();
    }

    public Dimension getDialogSize()
    {
        return mDialogSize;
    }
    
    public void setDialogSize(Dimension dialogSize)
    {
        mDialogSize = dialogSize;
    }
    
    public boolean isShowDialogSize()
    {
        return mShowDialogSize;
    }
    
    public void setShowDialogSize(boolean showDialogSize)
    {
        mShowDialogSize = showDialogSize;
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
        setDirty(true);
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
        int n = 0;
        PropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_TITLE, InstallOptionsPlugin.getResourceString("title.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_TITLE));
        list.add(new CustomPropertyDescriptor(descriptor,n++));
        list.add(new CustomPropertyDescriptor(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_ENABLED, InstallOptionsPlugin.getResourceString("cancel.enabled.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION),n++)); //$NON-NLS-1$
        list.add(new CustomPropertyDescriptor(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_SHOW, InstallOptionsPlugin.getResourceString("cancel.show.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION),n++)); //$NON-NLS-1$
        list.add(new CustomPropertyDescriptor(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_BACK_ENABLED, InstallOptionsPlugin.getResourceString("back.enabled.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION),n++)); //$NON-NLS-1$
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT, InstallOptionsPlugin.getResourceString("cancel.button.text.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT));
        list.add(new CustomPropertyDescriptor(descriptor,n++));
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT, InstallOptionsPlugin.getResourceString("back.button.text.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT));
        list.add(new CustomPropertyDescriptor(descriptor,n++));
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT, InstallOptionsPlugin.getResourceString("next.button.text.property.name")); //$NON-NLS-1$
        descriptor.setLabelProvider(cDefaultLabelProvider);
        descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT));
        list.add(new CustomPropertyDescriptor(descriptor,n++));
        descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_RECT, InstallOptionsPlugin.getResourceString("rect.property.name")); //$NON-NLS-1$
        descriptor.setValidator(new NumberCellEditorValidator(1,Integer.MAX_VALUE,true));
        descriptor.setLabelProvider(cDefaultLabelProvider);
        list.add(new CustomPropertyDescriptor(descriptor,n++));
        list.add(new CustomPropertyDescriptor(new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_RTL, InstallOptionsPlugin.getResourceString("rtl.property.name"), OPTION_DATA, OPTION_DISPLAY, DEFAULT_OPTION),n++)); //$NON-NLS-1$
        mDescriptors = (IPropertyDescriptor[])list.toArray(new IPropertyDescriptor[list.size()]);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if(InstallOptionsModel.PROPERTY_TITLE.equals(id)) {
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
        if(!Common.stringsAreEqual(mBackButtonText,backButtonText)) {
            mBackButtonText = backButtonText;
            setDirty(true);
        }
    }
    
    public String getBackEnabled()
    {
        return mBackEnabled;
    }
    
    public void setBackEnabled(String backEnabled)
    {
        if(!Common.stringsAreEqual(mBackEnabled,backEnabled)) {
            mBackEnabled = backEnabled;
            setDirty(true);
        }
    }
    
    public String getCancelButtonText()
    {
        return mCancelButtonText;
    }
    
    public void setCancelButtonText(String cancelButtonText)
    {
        if(!Common.stringsAreEqual(mCancelButtonText,cancelButtonText)) {
            mCancelButtonText = cancelButtonText;
            setDirty(true);
        }
    }
    
    public String getCancelEnabled()
    {
        return mCancelEnabled;
    }
    
    public void setCancelEnabled(String cancelEnabled)
    {
        if(!Common.stringsAreEqual(mCancelEnabled,cancelEnabled)) {
            mCancelEnabled = cancelEnabled;
            setDirty(true);
        }
    }
    
    public String getCancelShow()
    {
        return mCancelShow;
    }
    
    public void setCancelShow(String cancelShow)
    {
        if(!Common.stringsAreEqual(mCancelShow,cancelShow)) {
            mCancelShow = cancelShow;
            setDirty(true);
        }
    }
    
    public String getNextButtonText()
    {
        return mNextButtonText;
    }
    
    public void setNextButtonText(String nextButtonText)
    {
        if(!Common.stringsAreEqual(mNextButtonText,nextButtonText)) {
            mNextButtonText = nextButtonText;
            setDirty(true);
        }
    }
    
    public String getRect()
    {
        return mRect;
    }
    
    public void setRect(String rect)
    {
        if(!Common.stringsAreEqual(mRect,rect)) {
            mRect = rect;
            setDirty(true);
        }
    }
    
    public String getRTL()
    {
        return mRTL;
    }
    
    public void setRTL(String rtl)
    {
        if(!Common.stringsAreEqual(mRTL,rtl)) {
            mRTL = rtl;
            setDirty(true);
        }
    }
    
    public String getTitle()
    {
        return mTitle;
    }
    
    public void setTitle(String title)
    {
        if(!Common.stringsAreEqual(mTitle,title)) {
            mTitle = title;
            setDirty(true);
        }
    }
    
    public Object getPropertyValue(Object id)
    {
        if (InstallOptionsModel.PROPERTY_NUMFIELDS.equals(id)) {
            return new Integer(Common.isEmptyCollection(mChildren)?0:mChildren.size());
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
        setDirty(true);
    }

    protected void fireChildMoved(String prop, Object child, Object index) 
    {
        mListeners.firePropertyChange(prop, child, index);
        setDirty(true);
    }

    protected void fireChildRemoved(String prop, Object child) 
    {
        mListeners.firePropertyChange(prop, child, null);
        setDirty(true);
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
        setDirty(true);
    }

    public void setChild(InstallOptionsWidget child, int index){
        if (index >= mChildren.size()) {
            addChild(child,index);
        }
        else {
            InstallOptionsWidget oldChild = (InstallOptionsWidget)mChildren.get(index);
            mChildren.set(index,child);
            child.setIndex(index);
            child.setParent(this);
            if(oldChild != null) {
                fireChildRemoved(InstallOptionsModel.PROPERTY_CHILDREN, child);
            }
            fireChildAdded(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(index));
            setDirty(true);
        }
    }
    
    private void updateChildIndices(int index)
    {
        updateChildIndices(index,mChildren.size()-1);
    }

    private void updateChildIndices(int startIndex, int endIndex)
    {
        if(startIndex <= endIndex) {
            for(int i=startIndex; i<=endIndex; i++) {
                InstallOptionsWidget widget = (InstallOptionsWidget)mChildren.get(i);
                if(widget != null) {
                    widget.setIndex(i);
                }
            }
            setDirty(true);
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
            setDirty(true);
        }
    }


    public void moveChild(InstallOptionsWidget child, int newIndex){
        int oldIndex = mChildren.indexOf(child);
        if(oldIndex >= 0 && newIndex >= 0 && oldIndex != newIndex) {
            mChildren.remove(child);
            mChildren.add(newIndex,child);
            updateChildIndices(Math.min(oldIndex,newIndex),Math.max(oldIndex,newIndex));
            fireChildMoved(InstallOptionsModel.PROPERTY_CHILDREN, child, new Integer(newIndex));
            setDirty(true);
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

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsDialog dialog = (InstallOptionsDialog)super.clone();
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
        dialog.setNextButtonText(getNextButtonText());
        dialog.setRect(getRect());
        dialog.setRTL(getRTL());
        dialog.setTitle(getTitle());
        return dialog;
    }

    protected List doGetPropertyNames()
    {
        List list = super.doGetPropertyNames();
        list.addAll(Arrays.asList(InstallOptionsModel.getInstance().getDialogSettings()));
        return list;
    }
    
    public String toString()
    {
        return InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
    }
    
    public void loadINIFile(INIFile file)
    {
        mINIFile = file;
        INISection[] sections = mINIFile.getSections();
        if(!Common.isEmptyArray(sections)) {
            for (int i = 0; i < sections.length; i++) {
                if(sections[i].getName().equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
                    loadSection(sections[i]);
                    mINISectionMap.put(sections[i],this);
                }
                else {
                    Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(sections[i].getName());
                    if(m.matches()){
                        int index = Integer.parseInt(m.group(1))-1;
                        INIKeyValue[] types = sections[i].findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                        if(!Common.isEmptyArray(types)) {
                            InstallOptionsElementFactory factory =  InstallOptionsElementFactory.getFactory(types[0].getValue());
                            if(factory != null) {
                                InstallOptionsWidget widget = (InstallOptionsWidget)factory.getNewObject();
                                widget.loadSection(sections[i]);
                                if(index >= mChildren.size()) {
                                    int diff = index-mChildren.size()+1;
                                    for(int j=0; j<diff; j++) {
                                        mChildren.add(null);
                                    }
                                }
                                setChild(widget, index);
                                widget.setDirty(false);
                                mINISectionMap.put(sections[i],widget);
                            }
                        }
                    }
                }
            }
            setDirty(false);
            //Remove null entries
            int oldSize = mChildren.size();
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                if(iter.next() == null) {
                    iter.remove();
                }
            }
            if(oldSize != mChildren.size()) {
                updateChildIndices(0);
            }
        }
    }
    
    public boolean canUpdateINIFile()
    {
        if(!isDirty()) {
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                InstallOptionsWidget child = (InstallOptionsWidget)iter.next();
                if(child.isDirty()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    public INIFile updateINIFile()
    {
        if(canUpdateINIFile()) {
            HashMap tempMap = new HashMap();
            INISection section;
            section = saveSection();
            if(!mINISectionMap.containsKey(section)) {
                mINIFile.addChild(section);
                mINISectionMap.put(section,this);
            }
            tempMap.put(section, this);
            INISection previousSection = null;
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                if(previousSection != null) {
                    int n = previousSection.getSize();
                    if(n > 0) {
                        INILine lastChild = previousSection.getChild(n-1);
                        if(lastChild.getDelimiter() == null) {
                            lastChild.setDelimiter(INSISConstants.LINE_SEPARATOR);
                        }
                    }
                }
                InstallOptionsWidget element = (InstallOptionsWidget)iter.next();
                section = element.saveSection();
                if(!mINISectionMap.containsKey(section)) {
                    mINIFile.addChild(section);
                    mINISectionMap.put(section,element);
                }
                tempMap.put(section, element);
                previousSection = section;
            }
            for (Iterator iter = mINISectionMap.keySet().iterator(); iter.hasNext();) {
                section = (INISection)iter.next();
                if(!tempMap.containsKey(section)) {
                    iter.remove();
                    mINIFile.removeChild(section);
                }
            }
            mINIFile.update();
            setDirty(false);
        }
        return mINIFile;
    }
    
    protected String getSectionName()
    {
        return InstallOptionsModel.SECTION_SETTINGS;
    }
}