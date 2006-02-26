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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.viewer.MapContentProvider;
import net.sf.eclipsensis.viewer.MapLabelProvider;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public abstract class ComboParam extends PrefixableParam
{
    public static final String SETTING_SELECTED = "selected"; //$NON-NLS-1$

    public ComboParam(Node node)
    {
        super(node);
    }

    protected PrefixableParamEditor createPrefixableParamEditor()
    {
        return new ComboParamEditor();
    }
    
    protected boolean isUserEditable()
    {
        return false;
    }
    
    protected String validateUserValue(String value)
    {
        return null;
    }
    
    protected abstract Map getComboValues();
    
    protected class ComboParamEditor extends PrefixableParamEditor
    {
        protected ComboViewer mChoicesViewer = null;

        protected String getParamText()
        {
            if(mChoicesViewer != null && isValid(mChoicesViewer.getCombo())) {
                IStructuredSelection sel = (IStructuredSelection)mChoicesViewer.getSelection();
                if(!sel.isEmpty()) {
                    return ((Map.Entry)sel.getFirstElement()).getKey().toString();
                }
                else if(isAllowBlank()) {
                    return ""; //$NON-NLS-1$
                }
            }
            return null;
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(mChoicesViewer != null && isValid(mChoicesViewer.getCombo()) && getSettings() != null) {
                IStructuredSelection sel = (IStructuredSelection)mChoicesViewer.getSelection();
                if(!sel.isEmpty()) {
                    getSettings().put(SETTING_SELECTED, ((Map.Entry)sel.getFirstElement()).getKey().toString());
                }
                else if(isUserEditable()) {
                    getSettings().put(SETTING_SELECTED, mChoicesViewer.getCombo().getText());
                }
                else {
                    getSettings().put(SETTING_SELECTED, null);
                }
            }
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            Combo combo;
            if(mChoicesViewer != null && isValid(combo = mChoicesViewer.getCombo())) {
                String selected = (String)getSettingValue(SETTING_SELECTED, String.class, null);
                Map.Entry entry = null;
                Map comboValues = (Map)mChoicesViewer.getInput();
                for(Iterator iter=comboValues.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry e = (Entry)iter.next();
                    if(e.getKey().equals(selected)) {
                        entry = e;
                        break;
                    }
                }
                if(entry != null) {
                    mChoicesViewer.setSelection(new StructuredSelection(entry));
                }
                else if(selected != null && isUserEditable()) {
                    combo.setText(selected);
                }
            }
        }

        protected Control createParamControl(Composite parent)
        {
            Composite container = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 0;
            container.setLayout(layout);
            
            int style = SWT.DROP_DOWN;
            if(!isUserEditable ()) {
                style |= SWT.READ_ONLY;
            }
            Combo combo = new Combo(container,style);
            combo.setLayoutData(new GridData(isUserEditable()?SWT.FILL:SWT.LEFT,SWT.CENTER,isUserEditable(),false));
            mChoicesViewer = new ComboViewer(combo);
            mChoicesViewer.setContentProvider(new MapContentProvider());
            mChoicesViewer.setLabelProvider(new MapLabelProvider());
            Map comboValues = getComboValues();
            mChoicesViewer.setInput(comboValues);
            return container;
        }

        protected void updateState(boolean state)
        {
            super.updateState(state);
            if(mChoicesViewer != null && isValid(mChoicesViewer.getCombo())) {
                mChoicesViewer.getCombo().setEnabled(state);
            }
        }

        public String validateParam()
        {
            if(mChoicesViewer != null && isValid(mChoicesViewer.getCombo())) {
                IStructuredSelection sel = (IStructuredSelection)mChoicesViewer.getSelection();
                if(!sel.isEmpty()) {
                    return null;
                }
                if(isUserEditable()) {
                    String value = mChoicesViewer.getCombo().getText();
                    if(value.length() > 0) {
                        return validateUserValue(value);
                    }
                }
                if(isAllowBlank()) {
                    return null;
                }
            }
            return EclipseNSISPlugin.getResourceString("combo.param.error"); //$NON-NLS-1$
        }
    }
}
