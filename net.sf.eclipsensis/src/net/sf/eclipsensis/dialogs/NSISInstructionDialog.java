/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISPluginListener;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NSISInstructionDialog extends Dialog implements IDialogConstants
{
    private static String[] cInstructionList;

    private NSISSettingsPage mSettingsPage = null;
    private String mInstruction = ""; //$NON-NLS-1$
    private Combo mInstructionCombo = null;
    private Text mParametersText = null;
    private boolean mEditMode = true;
    private static INSISKeywordsListener cKeywordsListener  = new INSISKeywordsListener() {

        public void keywordsChanged()
        {
            loadInstructionList();
        }
        
    };
    private static IEclipseNSISPluginListener cShutdownListener = new IEclipseNSISPluginListener() {
        public void stopped()
        {
            NSISKeywords.removeKeywordsListener(cKeywordsListener);
        }
    };
    
    static {
        loadInstructionList();
        NSISKeywords.addKeywordsListener(cKeywordsListener);
        EclipseNSISPlugin.getDefault().addListener(cShutdownListener);
    }
    
    /**
     * 
     */
    private static void loadInstructionList()
    {
        cInstructionList = new String[NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS.length+
                                      NSISKeywords.INSTALLER_ATTRIBUTES.length];
        System.arraycopy(NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS,0,
                        cInstructionList,0,NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS.length);
        System.arraycopy(NSISKeywords.INSTALLER_ATTRIBUTES,0,
                        cInstructionList,NSISKeywords.SINGLELINE_COMPILETIME_COMMANDS.length,
                        NSISKeywords.INSTALLER_ATTRIBUTES.length);
    }

    /**
     * @param parentShell
     */
    public NSISInstructionDialog(NSISSettingsPage settingsPage, String instruction)
    {
        this(settingsPage);
        mInstruction = instruction;
        mEditMode = !Common.isEmpty(mInstruction);
    }

    public NSISInstructionDialog(NSISSettingsPage settingsPage)
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
        newShell.setText(EclipseNSISPlugin.getResourceString((Common.isEmpty(mInstruction)?"add.instruction.dialog.title": //$NON-NLS-1$
                                                                                           "edit.instruction.dialog.title"))); //$NON-NLS-1$
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
        String instruction;
        String parameters;
        int n = mInstruction.indexOf(" "); //$NON-NLS-1$
        if(n > 0) {
            instruction = mInstruction.substring(0,n);
            parameters = mInstruction.substring(n+1);
        }
        else {
            instruction = mInstruction;
            parameters = ""; //$NON-NLS-1$
        }
        
        mInstructionCombo = createCombo(composite, EclipseNSISPlugin.getResourceString("instructions.instruction.text"), //$NON-NLS-1$
                                        EclipseNSISPlugin.getResourceString("instructions.instruction.tooltip"), //$NON-NLS-1$
                                        cInstructionList,instruction);
        mInstructionCombo.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        mInstructionCombo.addKeyListener(new KeyAdapter() {
           public void keyReleased(KeyEvent e)
           {
               getButton(OK_ID).setEnabled(!Common.isEmpty(mInstructionCombo.getText()));
           }
        });
        mInstructionCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                getButton(OK_ID).setEnabled(!Common.isEmpty(mInstructionCombo.getText()));
            }
         });
        
        mParametersText = createText(composite, EclipseNSISPlugin.getResourceString("instructions.parameters.text"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("instructions.parameters.tooltip"),parameters); //$NON-NLS-1$
        mParametersText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        return composite;
    }
    
    protected Combo createCombo(Composite composite, String text, String tooltipText,
                                String[] list, String value)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = false;
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        
        Combo combo = new Combo(composite, SWT.DROP_DOWN);
        combo.setToolTipText(tooltipText);
        if(!Common.isEmptyArray(list)) {
            for(int i=0; i<list.length; i++) {
                combo.add(list[i]);
            }
        }
        combo.setText(value);
        
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = false;
        combo.setLayoutData(data);
        return combo;
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
        String instruction = mInstructionCombo.getText().trim();
        if(!Common.isEmpty(instruction)) {
            if(mSettingsPage.validateSaveInstruction(mInstruction,new StringBuffer(instruction).append(" ").append( //$NON-NLS-1$
                                                       mParametersText.getText().trim()).toString().trim(),
                                                       mEditMode)) {
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
        okButton.setEnabled(!Common.isEmpty(mInstruction));
        return control;
    }
}
