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

import java.beans.*;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.properties.editors.MultiLineTextCellEditor;

import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class MultiLineTextPropertyDescriptor extends TextPropertyDescriptor implements PropertyChangeListener
{
    private static final String PROPERTY_LABEL_PROVIDER="LabelProvider";

    private boolean mMultiLine = true;
    private boolean mOnlyNumbers = false;
    private PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * @param id
     * @param displayName
     */
    public MultiLineTextPropertyDescriptor(Object id, String displayName)
    {
        super(id, displayName);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
            List newFlags = (List)evt.getNewValue();
            setMultiLine(newFlags.contains(InstallOptionsModel.FLAGS_MULTILINE));
            setOnlyNumbers(newFlags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    public boolean isMultiLine()
    {
        return mMultiLine;
    }
    
    public void setMultiLine(boolean multiLine)
    {
        if(mMultiLine != multiLine) {
            boolean oldMultiLine = mMultiLine;
            mMultiLine = multiLine;
            mPropertyChangeSupport.firePropertyChange(InstallOptionsModel.FLAGS_MULTILINE,oldMultiLine,mMultiLine);
        }
    }
    
    public boolean isOnlyNumbers()
    {
        return mOnlyNumbers;
    }
    
    public void setOnlyNumbers(boolean onlyNumbers)
    {
        if(mOnlyNumbers != onlyNumbers) {
            boolean oldOnlyNumbers = mOnlyNumbers;
            mOnlyNumbers = onlyNumbers;
            mPropertyChangeSupport.firePropertyChange(InstallOptionsModel.FLAGS_ONLY_NUMBERS,oldOnlyNumbers,mOnlyNumbers);
        }
    }
    
    public void setLabelProvider(ILabelProvider provider)
    {
        ILabelProvider oldProvider = getLabelProvider();
        super.setLabelProvider(provider);
        mPropertyChangeSupport.firePropertyChange(PROPERTY_LABEL_PROVIDER,oldProvider,getLabelProvider());
    }
    
    public CellEditor createPropertyEditor(Composite parent) 
    {
        final ProxyTextCellEditor editor = new ProxyTextCellEditor(parent, isMultiLine(), getLabelProvider());
        final PropertyChangeListener listener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(InstallOptionsModel.FLAGS_MULTILINE)) {
                    editor.setStyle(((Boolean)evt.getNewValue()).booleanValue()?1:0);
                    editor.create();
                }
                else if(evt.getPropertyName().equals(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                    editor.setOnlyNumbers(((Boolean)evt.getNewValue()).booleanValue());
                }
                else if(evt.getPropertyName().equals(PROPERTY_LABEL_PROVIDER)) {
                    editor.setLabelProvider((ILabelProvider)evt.getNewValue());
                }
            }
        };
        addPropertyChangeListener(listener);
        editor.setOnlyNumbers(isOnlyNumbers());
        editor.getParent().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                removePropertyChangeListener(listener);
            }
        });
        editor.setValidator(getValidator());
        return editor;
    }

    private class ProxyTextCellEditor extends CellEditor
    {
        private Composite mParent;
        private CellEditor mDelegate;
        private Object mValue;
        private ListenerList mListeners = new ListenerList();
        private ListenerList mPropertyChangeListeners = new ListenerList();
        private ICellEditorValidator mValidator;
        private boolean mOnlyNumbers = false;
        private VerifyListener mNumberVerifyListener;
        private ILabelProvider mLabelProvider;
        
        public ProxyTextCellEditor(Composite parent, boolean multiLine, ILabelProvider labelProvider)
        {
            super(parent,(multiLine?1:0));
            mParent = parent;
            setLabelProvider(labelProvider);
            
        }

        public void setLabelProvider(ILabelProvider labelProvider)
        {
            mLabelProvider = labelProvider;
            if(mDelegate instanceof MultiLineTextCellEditor) {
                ((MultiLineTextCellEditor)mDelegate).setLabelProvider(labelProvider);
            }
        }
        
        private VerifyListener getNumberVerifyListener()
        {
            if(mNumberVerifyListener == null) {
                mNumberVerifyListener = new VerifyListener(){
                    public void verifyText(VerifyEvent e)
                    {
                        char[] chars = e.text.toCharArray();
                        for (int i = 0; i < chars.length; i++) {
                            if(!Character.isDigit(chars[i])) {
                                e.doit = false;
                                return;
                            }
                        }
                    }
                };
            }
            return mNumberVerifyListener;
        }

        public void create()
        {
            dispose();
            create(mParent);
        }
        
        public Composite getParent()
        {
            return mParent;
        }
        
        public void setOnlyNumbers(boolean onlyNumbers)
        {
            if(mOnlyNumbers != onlyNumbers) {
                mOnlyNumbers = onlyNumbers;
                updateNumberVerifier();
            }
        }
        
        private void updateNumberVerifier()
        {
            if(mDelegate != null) {
                if(mDelegate instanceof TextCellEditor) {
                    Text text = (Text)mDelegate.getControl();
                    if(text != null) {
                        if(mOnlyNumbers) {
                            text.addVerifyListener(getNumberVerifyListener());
                        }
                        else {
                            text.removeVerifyListener(getNumberVerifyListener());
                        }
                    }
                }
                else {
                    ((MultiLineTextCellEditor)mDelegate).setOnlyNumbers(mOnlyNumbers);
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets.Composite)
         */
        protected Control createControl(Composite parent)
        {
            createInnerEditor(parent,getStyle()==1);
            return mDelegate.getControl();
        }
        
        protected void createInnerEditor(Composite parent, boolean multiLine)
        {
            if(mDelegate != null) {
                mDelegate.dispose();
                mDelegate = null;
            }
            if(multiLine) {
                mDelegate = new MultiLineTextCellEditor(parent);
                ((MultiLineTextCellEditor)mDelegate).setLabelProvider(mLabelProvider);
            }
            else {
                mDelegate = new TextCellEditor(parent);
            }
            updateNumberVerifier();
            if (getValidator() != null) {
                mDelegate.setValidator(getValidator());
            }
            if(mListeners != null && mListeners.size() > 0) {
                Object[] listeners = mListeners.getListeners();
                for(int i=0; i<listeners.length; i++) {
                    mDelegate.addListener((ICellEditorListener)listeners[i]);
                }
            }
            if(mPropertyChangeListeners != null && mPropertyChangeListeners.size() > 0) {
                Object[] listeners = mPropertyChangeListeners.getListeners();
                for(int i=0; i<listeners.length; i++) {
                    mDelegate.addPropertyChangeListener((IPropertyChangeListener)listeners[i]);
                }
            }
            Object value = getValue();
            mDelegate.setValue(value==null?"":value);
        }

        public void activate()
        {
            if(mDelegate != null) {
                mDelegate.activate();
                mDelegate.getControl().setVisible(true);
            }
            super.activate();
        }

        public void deactivate() 
        {
            if(mDelegate != null)
                mDelegate.deactivate();
            super.deactivate();
        }

        public void addListener(ICellEditorListener listener)
        {
            mListeners.add(listener);
            if(mDelegate != null)
                mDelegate.addListener(listener);
        }
        
        public void addPropertyChangeListener(IPropertyChangeListener listener)
        {
            mPropertyChangeListeners.add(listener);
            if(mDelegate != null)
                mDelegate.addPropertyChangeListener(listener);
        }
        
        public void dispose()
        {
            if(mDelegate != null)
                mDelegate.dispose();
            super.dispose();
        }
        
        public String getErrorMessage()
        {
            if(mDelegate != null)
                return mDelegate.getErrorMessage();
            else
                return null;
        }

        public ICellEditorValidator getValidator()
        {
            return mValidator;
        }

        public boolean isCopyEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isCopyEnabled();
            else
                return super.isCopyEnabled();
        }
        
        public boolean isCutEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isCutEnabled();
            else
                return super.isCutEnabled();
        }

        public boolean isDeleteEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isDeleteEnabled();
            else
                return super.isDeleteEnabled();
        }

        public boolean isDirty()
        {
            if(mDelegate != null)
                return mDelegate.isDirty();
            else
                return super.isDirty();
        }

        public boolean isFindEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isFindEnabled();
            else
                return super.isFindEnabled();
        }
        
        public boolean isPasteEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isPasteEnabled();
            else
                return super.isPasteEnabled();
        }
        
        public boolean isRedoEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isRedoEnabled();
            else
                return super.isRedoEnabled();
        }
        
        public boolean isSelectAllEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isSelectAllEnabled();
            else
                return super.isSelectAllEnabled();
        }
        
        public boolean isUndoEnabled()
        {
            if(mDelegate != null)
                return mDelegate.isUndoEnabled();
            else
                return super.isUndoEnabled();
        }
        
        public boolean isValueValid()
        {
            if(mDelegate != null)
                return mDelegate.isValueValid();
            else
                return super.isValueValid();
        }
        
        public void performCopy()
        {
            if(mDelegate != null)
                mDelegate.performCopy();
        }
        
        public void performCut()
        {
            if(mDelegate != null)
                mDelegate.performCut();
        }
        
        public void performDelete()
        {
            if(mDelegate != null)
                mDelegate.performDelete();
        }
        
        public void performFind()
        {
            mDelegate.performFind();
        }
        
        public void performPaste()
        {
            if(mDelegate != null)
                mDelegate.performPaste();
        }
        
        public void performRedo()
        {
            if(mDelegate != null)
                mDelegate.performRedo();
        }
        
        public void performSelectAll()
        {
            if(mDelegate != null)
                mDelegate.performSelectAll();
        }
        
        public void performUndo()
        {
            if(mDelegate != null)
                mDelegate.performUndo();
        }
        
        public void removeListener(ICellEditorListener listener)
        {
            mListeners.remove(listener);
            if(mDelegate != null)
                mDelegate.removeListener(listener);
        }
        
        public void removePropertyChangeListener(
                IPropertyChangeListener listener)
        {
            mPropertyChangeListeners.remove(listener);
            if(mDelegate != null)
                mDelegate.removePropertyChangeListener(listener);
        }
        
        public void setFocus()
        {
            if(mDelegate != null)
                mDelegate.setFocus();
        }
        
        public void setValidator(ICellEditorValidator validator)
        {
            mValidator = validator;
            if(mDelegate != null)
                mDelegate.setValidator(validator);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
         */
        protected Object doGetValue()
        {
            if(mDelegate != null)
                mValue = mDelegate.getValue();

            if(mValue == null)
                mValue = "";
            return mValue;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
         */
        protected void doSetValue(Object value)
        {
            if(value == null) {
                value = "";
            }
            mValue = value;
            if(mDelegate != null)
                mDelegate.setValue(value);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
         */
        protected void doSetFocus()
        {
        }
    }
}
