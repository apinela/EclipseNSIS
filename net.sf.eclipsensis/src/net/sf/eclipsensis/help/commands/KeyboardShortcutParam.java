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

import java.util.ArrayList;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class KeyboardShortcutParam extends PrefixableParam
{
    public static final String SETTING_KEY = "key"; //$NON-NLS-1$
    
    private static final String[] cModifiers = {"CTRL","ALT","SHIFT","EXT"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private static final String[] cKeys;
    
    static {
        ArrayList list = new ArrayList();
        for(char c = 'A'; c <= 'Z'; c++) {
            list.add(Character.toString(c));
        }
        for(int i = 0; i < 10; i++) {
            list.add(Integer.toString(i));
        }
        for(int i = 1; i <= 24; i++) {
            list.add("F"+i); //$NON-NLS-1$
        }
        cKeys = (String[])list.toArray(new String[list.size()]);
    }
    
    public KeyboardShortcutParam(Node node)
    {
        super(node);
    }

    protected PrefixableParamEditor createPrefixableParamEditor(INSISParamEditor parentEditor)
    {
        return new KeyboardShortcutParamEditor(parentEditor);
    }

    protected class KeyboardShortcutParamEditor extends PrefixableParamEditor
    {
        private Button[] mModifierButtons;
        private Combo mKeyCombo;

        public KeyboardShortcutParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void clear()
        {
            if(isValid(mKeyCombo)) {
                mKeyCombo.clearSelection();
            }
            if(!Common.isEmptyArray(mModifierButtons)) {
                for (int i = 0; i < mModifierButtons.length; i++) {
                    if(isValid(mModifierButtons[i])) {
                        mModifierButtons[i].setSelection(false);
                    }
                }
            }
            super.clear();
        }

        protected void updateState(boolean state)
        {
            if(isValid(mKeyCombo)) {
                mKeyCombo.setEnabled(state);
            }
            if(!Common.isEmptyArray(mModifierButtons)) {
                for (int i = 0; i < mModifierButtons.length; i++) {
                    if(isValid(mModifierButtons[i])) {
                        mModifierButtons[i].setEnabled(state);
                    }
                }
            }
            super.updateState(state);
        }

        protected String getPrefixableParamText()
        {
            if(!Common.isEmptyArray(mModifierButtons) && isValid(mKeyCombo)) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                for (int i = 0; i < mModifierButtons.length; i++) {
                    if(isValid(mModifierButtons[i]) && mModifierButtons[i].getSelection()) {
                        buf.append(mModifierButtons[i].getText()).append("|"); //$NON-NLS-1$
                    }
                }
                buf.append(mKeyCombo.getText());
                return buf.toString();
            }
            return null;
        }

        protected String validateParam()
        {
            boolean found = false;
            if(!Common.isEmptyArray(mModifierButtons)) {
                int i = 0;
                for (; i < mModifierButtons.length; i++) {
                    if(isValid(mModifierButtons[i]) && mModifierButtons[i].getSelection()) {
                        found = true;
                        break;
                    }
                }
            }
            if(isValid(mKeyCombo)) {
                if(!found) {
                    if(mKeyCombo.getText().length() > 0 || isAllowBlank()) {
                        return null;
                    }
                }
                else {
                    if(mKeyCombo.getText().length() > 0) {
                        return null;
                    }
                }
                return EclipseNSISPlugin.getResourceString("shortcut.key.param.error"); //$NON-NLS-1$
            }
            return null;
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(getSettings() != null) {
                if(isValid(mKeyCombo)) {
                    getSettings().put(SETTING_KEY, mKeyCombo.getText());
                }
                if(!Common.isEmptyArray(mModifierButtons)) {
                    for (int i = 0; i < mModifierButtons.length; i++) {
                        if(isValid(mModifierButtons[i])) {
                            getSettings().put(mModifierButtons[i].getText(), Boolean.valueOf(mModifierButtons[i].getSelection()));
                        }
                    }
                }
            }
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            if(!Common.isEmptyArray(mModifierButtons)) {
                for (int i = 0; i < mModifierButtons.length; i++) {
                    if(isValid(mModifierButtons[i])) {
                        mModifierButtons[i].setSelection(((Boolean)getSettingValue(cModifiers[i], Boolean.class, Boolean.FALSE)).booleanValue());
                    }
                }
            }
            if(isValid(mKeyCombo)) {
                String key = (String)getSettingValue(SETTING_KEY, String.class, null);
                int count = mKeyCombo.getItemCount();
                for (int i = 0; i < count; i++) {
                    if(mKeyCombo.getItem(i).equals(key)) {
                        mKeyCombo.select(i);
                        break;
                    }
                }
            }
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(cModifiers.length+1,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent.setLayout(layout);
            mModifierButtons = new Button[cModifiers.length];
            for (int i = 0; i < cModifiers.length; i++) {
                mModifierButtons[i] = new Button(parent,SWT.CHECK);
                mModifierButtons[i].setText(cModifiers[i]);
                mModifierButtons[i].setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            }
            mKeyCombo = new Combo(parent,SWT.DROP_DOWN|SWT.READ_ONLY);
            mKeyCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            for (int i = 0; i < cKeys.length; i++) {
                mKeyCombo.add(cKeys[i]);
            }
            return parent;
        }
        
    }
}
