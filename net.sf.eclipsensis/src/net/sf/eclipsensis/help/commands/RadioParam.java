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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class RadioParam extends GroupParam
{
    public static final String SETTING_RADIO_SELECTED = "radioSelected"; //$NON-NLS-1$
    private static final Integer DEFAULT_RADIO_SELECTED = new Integer(0);
    
    public RadioParam(Node node)
    {
        super(node);
    }

    protected NSISParamEditor createParamEditor()
    {
        return new RadioParamEditor();
    }

    protected class RadioParamEditor extends GroupParamEditor
    {
        private Button[] mRadioButtons;

        protected void appendParamText(StringBuffer buf)
        {
            if(!Common.isEmptyArray(mRadioButtons)) {
                for (int i = 0; i < mRadioButtons.length; i++) {
                    if(isValid(mRadioButtons[i])) {
                        if(mRadioButtons[i].getSelection()) {
                            mParamEditors[i].appendText(buf);
                            return;
                        }
                    }
                    else {
                        return;
                    }
                }
            }
        }

        protected Control createParamControl(Composite parent)
        {
            if(!Common.isEmptyArray(mChildParams)) {
                mRadioButtons = new Button[mChildParams.length];
            }
            return super.createParamControl(parent);
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            if(!Common.isEmptyArray(mRadioButtons)) {
                int i = ((Integer)getSettingValue(SETTING_RADIO_SELECTED, Integer.class, DEFAULT_RADIO_SELECTED)).intValue();
                if(i < mRadioButtons.length) {
                    if(isValid(mRadioButtons[i])) {
                        mRadioButtons[i].setSelection(true);
                        return;
                    }
                }

                if(isValid(mRadioButtons[0])) {
                    mRadioButtons[0].setSelection(true);
                }
            }
        }
        
        protected int getLayoutNumColumns()
        {
            return 1+super.getLayoutNumColumns();
        }

        protected void createChildParamControl(Composite parent, final int index)
        {
            mRadioButtons[index] = new Button(parent,SWT.RADIO);
            mRadioButtons[index].setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            super.createChildParamControl(parent, index);
            mRadioButtons[index].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mParamEditors[index].setEnabled(mRadioButtons[index].getSelection());
                }
            });
            mParamEditors[index].setEnabled(mRadioButtons[index].getSelection());
        }

        protected void updateState(boolean state)
        {
            super.updateState(state);
            if(!Common.isEmptyArray(mRadioButtons)) {
                for (int i = 0; i < mRadioButtons.length; i++) {
                    if(isValid(mRadioButtons[i])) {
                        mRadioButtons[i].setEnabled(state);
                        if(state) {
                            mParamEditors[i].setEnabled(mRadioButtons[i].getSelection());
                        }
                        else {
                            mParamEditors[i].setEnabled(state);
                        }
                    }
                }
            }
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(!Common.isEmptyArray(mRadioButtons) && getSettings() != null) {
                for (int i = 0; i < mRadioButtons.length; i++) {
                    if(isValid(mRadioButtons[i])) {
                        if(mRadioButtons[i].getSelection()) {
                            getSettings().put(SETTING_RADIO_SELECTED, new Integer(i));
                            return;
                        }
                    }
                }
            }
        }

        protected String validateParam()
        {
            if(!Common.isEmptyArray(mRadioButtons)) {
                for (int i = 0; i < mRadioButtons.length; i++) {
                    if(isValid(mRadioButtons[i])) {
                        if(mRadioButtons[i].getSelection()) {
                            return mParamEditors[i].validate();
                        }
                    }
                }
                return EclipseNSISPlugin.getResourceString("radio.param.error"); //$NON-NLS-1$
            }
            return null;
        }
    }
}
