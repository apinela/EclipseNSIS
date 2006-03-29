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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class MultiChoiceParam extends ChoiceParam
{
    public MultiChoiceParam(Node node)
    {
        super(node);
    }

    protected PrefixableParamEditor createPrefixableParamEditor(INSISParamEditor parentEditor)
    {
        return new MultiChoiceParamEditor(parentEditor);
    }

    protected class MultiChoiceParamEditor extends PrefixableParamEditor
    {
        public static final String DATA_CHOICE = "CHOICE"; //$NON-NLS-1$
        protected Button[] mChoiceButtons = null;
        
        public MultiChoiceParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void reset()
        {
            if(!Common.isEmptyArray(mChoiceButtons)) {
                for (int i = 0; i < mChoiceButtons.length; i++) {
                    if(isValid(mChoiceButtons[i])) {
                        mChoiceButtons[i].setSelection(false);
                    }
                }
            }
            super.reset();
        }

        protected String getParamText()
        {
            if(!Common.isEmptyArray(mChoiceButtons)) {
                boolean first = true;
                StringBuffer buf = null;
                for (int i = 0; i < mChoiceButtons.length; i++) {
                    if(isValid(mChoiceButtons[i]) && mChoiceButtons[i].getSelection()) {
                        if(!first) {
                            buf.append("|"); //$NON-NLS-1$
                        }
                        else {
                            buf = new StringBuffer(""); //$NON-NLS-1$
                            first = false;
                        }
                        buf.append(mChoiceButtons[i].getData(DATA_CHOICE));
                    }
                }
                return (buf != null?buf.toString():null);
            }
            return null;
        }

        protected void updateState(boolean state)
        {
            super.updateState(state);
            if(!Common.isEmptyArray(mChoiceButtons)) {
                for (int i = 0; i < mChoiceButtons.length; i++) {
                    if(isValid(mChoiceButtons[i])) {
                        mChoiceButtons[i].setEnabled(state);
                    }
                }
            }
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(!Common.isEmptyArray(mChoiceButtons) && getSettings() != null) {
                for (int i = 0; i < mChoiceButtons.length; i++) {
                    if(isValid(mChoiceButtons[i])) {
                        getSettings().put(mChoiceButtons[i].getData(DATA_CHOICE), Boolean.valueOf(mChoiceButtons[i].getSelection()));
                    }
                }
            }
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(1,true);
            layout.marginHeight = layout.marginWidth = 0;
            parent.setLayout(layout);
            ComboEntry[] choices = getComboEntries();
            if(!Common.isEmptyArray(choices)) {
                layout.numColumns = Math.min(4,choices.length);
                mChoiceButtons = new Button[choices.length];
                for (int i=0; i<choices.length; i++) {
                    Button b = new Button(parent,SWT.CHECK);
                    b.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
                    b.setText(choices[i].getDisplay());
                    b.setData(DATA_CHOICE,choices[i].getValue());
                    mChoiceButtons[i++] = b;
                }
            }
            return parent;
        }
        protected void initParamEditor()
        {
            super.initParamEditor();
            if(!Common.isEmptyArray(mChoiceButtons)) {
                for (int i = 0; i < mChoiceButtons.length; i++) {
                    if(isValid(mChoiceButtons[i])) {
                        mChoiceButtons[i].setSelection(((Boolean)getSettingValue((String)mChoiceButtons[i].getData(DATA_CHOICE), Boolean.class, Boolean.FALSE)).booleanValue());
                    }
                }
            }
        }

        public String validateParam()
        {
            if(!Common.isEmptyArray(mChoiceButtons)) {
                for (int i = 0; i < mChoiceButtons.length; i++) {
                    if(!isValid(mChoiceButtons[i])) {
                        break;
                    }
                    if(mChoiceButtons[i].getSelection()) {
                        return null;
                    }
                }
            }
            return EclipseNSISPlugin.getResourceString("multi.choice.param.error"); //$NON-NLS-1$
        }
    }
}
