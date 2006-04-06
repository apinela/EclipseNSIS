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
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class MessageBoxOptionsParam extends NSISParam
{
    public static final String SETTING_BUTTONS = "buttons"; //$NON-NLS-1$
    public static final String SETTING_ICON = "icon"; //$NON-NLS-1$
    public static final String SETTING_DEFAULT = "default"; //$NON-NLS-1$
    
    public MessageBoxOptionsParam(Node node)
    {
        super(node);
    }

    protected NSISParamEditor createParamEditor(INSISParamEditor parentEditor)
    {
        return new MessageBoxOptionsParamEditor(parentEditor);
    }

    protected class MessageBoxOptionsParamEditor extends NSISParamEditor
    {
        private Combo mButtonsCombo = null;
        private Combo mIconCombo = null;
        private Combo mDefaultCombo = null;
        private Button[] mOthersButtons = null;

        public MessageBoxOptionsParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void clear()
        {
            resetCombo(mButtonsCombo);
            resetCombo(mIconCombo);
            resetCombo(mDefaultCombo);
            if(!Common.isEmptyArray(mOthersButtons)) {
                for (int i = 0; i < mOthersButtons.length; i++) {
                    if(isValid(mOthersButtons[i])) {
                        mOthersButtons[i].setSelection(false);
                    }
                }
            }
            super.clear();
        }
        
        private void resetCombo(Combo combo)
        {
            if(isValid(combo)) {
                combo.clearSelection();
            }
        }

        protected String validateParam()
        {
            return null;
        }

        protected void appendParamText(StringBuffer buf)
        {
            StringBuffer buf2 = new StringBuffer(""); //$NON-NLS-1$
            buf.append(" "); //$NON-NLS-1$
            if(isValid(mButtonsCombo)) {
                appendOptionText(buf2,mButtonsCombo.getText());
            }
            if(isValid(mIconCombo)) {
                appendOptionText(buf2,mIconCombo.getText());
            }
            if(isValid(mDefaultCombo)) {
                appendOptionText(buf2,mDefaultCombo.getText());
            }
            if(!Common.isEmptyArray(mOthersButtons)) {
                for (int i = 0; i < mOthersButtons.length; i++) {
                    if(isValid(mOthersButtons[i]) && mOthersButtons[i].getSelection()) {
                        appendOptionText(buf2, mOthersButtons[i].getText());
                    }
                }
            }
            buf.append(maybeQuote(buf2.toString()));
        }

        private void appendOptionText(StringBuffer buf, String text)
        {
            if(!Common.isEmpty(text)) {
                if(buf.length() > 0) {
                    buf.append("|"); //$NON-NLS-1$
                }
                buf.append(text);
            }
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(getSettings() != null) {
                if(isValid(mButtonsCombo)) {
                    getSettings().put(SETTING_BUTTONS, mButtonsCombo.getText());
                }
                if(isValid(mIconCombo)) {
                    getSettings().put(SETTING_ICON, mIconCombo.getText());
                }
                if(isValid(mDefaultCombo)) {
                    getSettings().put(SETTING_DEFAULT, mDefaultCombo.getText());
                }
                if(!Common.isEmptyArray(mOthersButtons)) {
                    for (int i = 0; i < mOthersButtons.length; i++) {
                        if(isValid(mOthersButtons[i])) {
                            getSettings().put(mOthersButtons[i].getText(), Boolean.valueOf(mOthersButtons[i].getSelection()));
                        }
                    }
                }
            }
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Group(parent,SWT.NONE);
            GridLayout gridLayout = new GridLayout(2,false);
            gridLayout.marginWidth = gridLayout.marginHeight = 2;
            parent.setLayout(gridLayout);
            mButtonsCombo = createCombo(parent,EclipseNSISPlugin.getResourceString("messagebox.param.buttons.label"),NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.MESSAGEBOX_OPTION_BUTTON_PARAMETERS)); //$NON-NLS-1$
            mIconCombo = createCombo(parent,EclipseNSISPlugin.getResourceString("messagebox.param.icon.label"),NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.MESSAGEBOX_OPTION_ICON_PARAMETERS)); //$NON-NLS-1$
            mDefaultCombo = createCombo(parent,EclipseNSISPlugin.getResourceString("messagebox.param.default.label"),NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.MESSAGEBOX_OPTION_DEFAULT_PARAMETERS)); //$NON-NLS-1$
            String[] others = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.MESSAGEBOX_OPTION_OTHER_PARAMETERS);
            if(!Common.isEmptyArray(others)) {
                Group buttons = new Group(parent,SWT.None);
                buttons.setText(EclipseNSISPlugin.getResourceString("messagebox.param.other.options.label")); //$NON-NLS-1$
                GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
                data.horizontalSpan = 2;
                buttons.setLayoutData(data);
                buttons.setLayout(new GridLayout(2,false));
                mOthersButtons = new Button[others.length];
                for (int i = 0; i < others.length; i++) {
                    mOthersButtons[i] = createCheckbox(buttons, others[i]);
                }
            }
            return parent;
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            initCombo(mButtonsCombo, SETTING_BUTTONS);
            initCombo(mIconCombo, SETTING_ICON);
            initCombo(mDefaultCombo, SETTING_DEFAULT);
            if(!Common.isEmptyArray(mOthersButtons)) {
                for (int i = 0; i < mOthersButtons.length; i++) {
                    if(isValid(mOthersButtons[i])) {
                        mOthersButtons[i].setSelection(((Boolean)getSettingValue(mOthersButtons[i].getText(), Boolean.class, Boolean.FALSE)).booleanValue());
                    }
                }
            }
        }

        /**
         * 
         */
        private void initCombo(Combo combo, String setting)
        {
            if(isValid(combo)) {
                String selected = (String)getSettingValue(setting, String.class, null);
                int count = combo.getItemCount();
                for(int i=0; i<count; i++) {
                    if(combo.getItem(i).equals(selected)) {
                        combo.select(i);
                        break;
                    }
                }
            }
        }

        private Combo createCombo(Composite parent, String label, String[] strings)
        {
            Label l = new Label(parent,SWT.NONE);
            l.setText(label);
            l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            Combo combo = new Combo(parent,SWT.DROP_DOWN|SWT.READ_ONLY);
            combo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            combo.add(""); //$NON-NLS-1$
            if(!Common.isEmptyArray(strings)) {
                for (int i = 0; i < strings.length; i++) {
                    combo.add(strings[i]);
                }
            }
            return combo;
        }

        private Button createCheckbox(Composite parent, String label)
        {
            Button button = new Button(parent,SWT.CHECK);
            button.setText(label);
            button.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            return button;
        }
    }
}
