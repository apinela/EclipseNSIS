/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISSymbolDialog extends Dialog implements IDialogConstants
{
    private NSISSettingsPage mSettingsPage = null;
    private String mName = ""; //$NON-NLS-1$
    private String mValue = ""; //$NON-NLS-1$
    private Text mNameText = null;
    private Text mValueText = null;
    private boolean mEditMode = true;
    
    /**
     * @param parentShell
     */
    public NSISSymbolDialog(NSISSettingsPage settingsPage, String name, String value)
    {
        this(settingsPage);
        mName = name;
        mValue = value;
        mEditMode = !Common.isEmpty(mName);
    }

    public NSISSymbolDialog(NSISSettingsPage settingsPage)
    {
        super(settingsPage.getShell());
        mSettingsPage = settingsPage;
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(EclipseNSISPlugin.getResourceString((Common.isEmpty(mName)?"add.symbol.dialog.title": //$NON-NLS-1$
                                                                                    "edit.symbol.dialog.title"))); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.numColumns = 2;

        GridData data = (GridData)composite.getLayoutData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.widthHint = 300;
        
        mNameText = createText(composite, EclipseNSISPlugin.getResourceString("symbols.mName.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("symbols.mName.tooltip"),mName); //$NON-NLS-1$
/*        mNameText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) 
            {
                getButton(OK_ID).setEnabled(!Common.isEmpty(mNameText.getText()));
            }
        });
*/
        mNameText.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) 
            {
                char[] chars = e.text.toCharArray();
                for(int i=0; i< chars.length; i++) {
                    if(Character.isWhitespace(chars[i])) {
                        e.doit = false;
                        return;
                    }
                }
                e.text = e.text.toUpperCase();
            }
        });
        
        mNameText.addKeyListener(new KeyAdapter() {
           public void keyReleased(KeyEvent e)
           {
               getButton(OK_ID).setEnabled(!Common.isEmpty(mNameText.getText()));
           }
        });
        mNameText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        
        mValueText = createText(composite, EclipseNSISPlugin.getResourceString("symbols.value.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("symbols.value.tooltip"),mValue); //$NON-NLS-1$
        mValueText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        return composite;
    }
    
    protected Text createText(Composite composite, String labelText, String tooltipText, String value)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(labelText);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        
        Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setToolTipText(tooltipText);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        text.setLayoutData(data);
        text.setText(value);
        
        return text;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        String name = mNameText.getText();
        if(!Common.isEmpty(name)) {
            if(mSettingsPage.validateSaveSymbol(mName,name,mValueText.getText(),mEditMode)) {
                super.okPressed();
            }
        }
    }
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(Composite)
     */
    protected Control createButtonBar(Composite parent) {
        Control control = super.createButtonBar(parent);
        Button okButton = getButton(OK_ID);
        okButton.setEnabled(!Common.isEmpty(mName));
        return control;
    }
}
