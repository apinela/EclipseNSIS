/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class NSISParam
{
    public static final String ATTR_OPTIONAL = "optional"; //$NON-NLS-1$
    public static final String SETTING_OPTIONAL = ATTR_OPTIONAL; //$NON-NLS-1$
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String TAG_PARAM = "param"; //$NON-NLS-1$
    public static final String ATTR_VALUE = "value"; //$NON-NLS-1$
    
    private String mName;
    private boolean mOptional;
    private MessageFormat mErrorFormat;
    
    public NSISParam(Node node)
    {
        init(node);
    }

    /**
     * @param node
     */
    protected void init(Node node)
    {
        mErrorFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("param.error.format")); //$NON-NLS-1$
        NamedNodeMap attributes = node.getAttributes();
        String name = XMLUtil.getStringValue(attributes, ATTR_NAME);
        if(!Common.isEmpty(name)) {
            setName(name);
        }
        setOptional(XMLUtil.getBooleanValue(attributes, ATTR_OPTIONAL));
    }

    public String getName()
    {
        return mName;
    }

    public boolean isOptional()
    {
        return mOptional;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public void setOptional(boolean optional)
    {
        mOptional = optional;
    }

    public INSISParamEditor createEditor()
    {
        return createParamEditor();
    }
    
    protected abstract NSISParamEditor createParamEditor();

    protected abstract class NSISParamEditor implements INSISParamEditor
    {
        protected Control mControl = null;
        protected Button mOptionalButton = null;
        protected Label mNameLabel = null;
        protected List mDependents = null;
        private boolean mEnabled = true;
        private Map mSettings = null;
        private boolean mInit = false;

        public NSISParamEditor()
        {
        }

        public boolean isEnabled()
        {
            return mEnabled;
        }

        public Control getControl()
        {
            return mControl;
        }

        public void initEditor()
        {
            if(mInit) {
                return;
            }
            mInit = true;
            initParamEditor();
            updateEnabled();
        }

        protected void initParamEditor()
        {
            if(isValid(mOptionalButton)) {
                mOptionalButton.setSelection(((Boolean)getSettingValue(SETTING_OPTIONAL, Boolean.class, Boolean.FALSE)).booleanValue());
            }
        }

        protected boolean createMissing()
        {
            return true;
        }

        protected Object getSettingValue(String name, Class clasz, Object defaultValue)
        {
            Object value;
            if(getSettings() != null) {
                value = getSettings().get(name);
                if(value != null) {
                    if(!clasz.isAssignableFrom(value.getClass())) {
                        value = defaultValue;
                    }
                }
                else {
                    value = defaultValue;
                }
            }
            else {
                value = defaultValue;
            }
            return value;
        }

        public Control createControl(Composite parent)
        {
            if(isValid(mControl)) {
                throw new RuntimeException(EclipseNSISPlugin.getResourceString("create.editor.error")); //$NON-NLS-1$
            }
            int availableGridCells = ((GridLayout)parent.getLayout()).numColumns;
            int n = 0;
            Control[] children = parent.getChildren();
            if(!Common.isEmptyArray(children)) {
                for (int i = 0; i < children.length; i++) {
                    GridData layoutData = (GridData)children[i].getLayoutData();
                    n += (layoutData).horizontalSpan;
                }
            }
            availableGridCells -= n % availableGridCells;
            boolean emptyName = Common.isEmpty(getName());
            int neededGridCells = (isOptional()?1:0)+(!emptyName?1:0)+1;
            if(neededGridCells > availableGridCells) {
                parent = new Composite(parent, SWT.None);
                GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
                data.horizontalSpan = availableGridCells;
                GridLayout layout = new GridLayout(neededGridCells,false);
                layout.marginHeight = layout.marginWidth = 0;
                parent.setLayout(layout);
            }
            
            int horizontalSpan = 1;
            
            if(isOptional()) {
                mOptionalButton = new Button(parent,SWT.CHECK);
                GridData gridData = new GridData(SWT.FILL,SWT.CENTER,false,false);
                mOptionalButton.setLayoutData(gridData);
                if(!emptyName) {
                    mOptionalButton.setText(getName());
                    gridData.horizontalSpan = 2;
                }
            }
            else {
                if(availableGridCells == 3 || (availableGridCells == 2 && emptyName)) {
                    if(createMissing()) {
                        Label l = new Label(parent,SWT.None);
                        GridData data = new GridData(SWT.FILL,SWT.CENTER,false,false);
                        data.widthHint = 12;
                        l.setLayoutData(data);
                    }
                    else {
                        horizontalSpan++;
                    }
                }
            }
            if(!emptyName) {
                if(!isOptional()) {
                    mNameLabel = new Label(parent,SWT.NONE);
                    mNameLabel.setText(getName());
                    mNameLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
                }
            }
            else {
                if(availableGridCells == 3) {
                    if(createMissing()) {
                        Label l = new Label(parent,SWT.None);
                        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
                    }
                    else {
                        horizontalSpan++;
                    }
                }
            }
            mControl = createParamControl(parent);
            GridData gridData = new GridData(SWT.FILL,(mControl instanceof Composite?SWT.FILL:SWT.CENTER),true,false);
            gridData.horizontalSpan = horizontalSpan;
            mControl.setLayoutData(gridData);
            if(mOptionalButton != null) {
                mOptionalButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e)
                    {
                        updateState(mOptionalButton.getSelection());
                    }
                });
            }
            mControl.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    mControl = null;
                }
            });
            return mControl;
        }
        
        public final boolean isSelected()
        {
            if(isValid(mControl) && mControl.isEnabled()) {
                if(isOptional()) {
                    if(isValid(mOptionalButton)) {
                        return mOptionalButton.getSelection();
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return true;
                }
            }
            else {
                return false;
            }
        }

        public final List getDependents()
        {
            return mDependents;
        }

        public final void setDependents(List dependents)
        {
            mDependents = dependents;
            updateDependents(mEnabled && isSelected());
        }

        protected boolean isValid(Control ctl)
        {
            return ctl != null && !ctl.isDisposed();
        }

        protected void updateState(boolean state)
        {
            if(isValid(mNameLabel)) {
                mNameLabel.setEnabled(state);
            }
            if(isValid(mControl)) {
                mControl.setEnabled(state);
            }
            updateDependents(state);
        }

        /**
         * @param state
         */
        protected void updateDependents(boolean state)
        {
            if(!Common.isEmptyCollection(mDependents)) {
                for (Iterator iter = mDependents.iterator(); iter.hasNext();) {
                    ((INSISParamEditor)iter.next()).setEnabled(state);
                }
            }
        }

        public final void setEnabled(boolean enabled)
        {
            mEnabled = enabled;
            updateEnabled();
        }

        /**
         * @param enabled
         */
        private void updateEnabled()
        {
            boolean enabled = mEnabled;
            if(isValid(mOptionalButton)) {
                mOptionalButton.setEnabled(enabled);
                if(enabled) {
                    enabled = mOptionalButton.getSelection();
                }
            }
            updateState(enabled);
        }
        
        public final void appendText(StringBuffer buf)
        {
            if(getSettings() != null) {
                saveSettings();
            }
            if(isSelected()) {
                appendParamText(buf);
            }
        }

        public final String validate()
        {
            if(isSelected()) {
                String error = validateParam();
                if(error != null) {
                    if(!Common.isEmpty(getName())) {
                        error = mErrorFormat.format(new String[] {getName(),error});
                    }
                    return error;
                }
            }
            return null;
        }
        
        public NSISParam getParam()
        {
            return NSISParam.this;
        }

        public Map getSettings()
        {
            return mSettings;
        }

        public void setSettings(Map settings)
        {
            mSettings = settings;
        }
        
        public void saveSettings()
        {
            if(isValid(mOptionalButton) && getSettings() != null) {
                getSettings().put(SETTING_OPTIONAL, Boolean.valueOf(mOptionalButton.getSelection()));
            }
        }

        protected abstract String validateParam();
        protected abstract void appendParamText(StringBuffer buf);
        protected abstract Control createParamControl(Composite parent); 
    }
}
