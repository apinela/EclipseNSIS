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

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstructionDialog extends StatusMessageDialog
{
    private static String[] cInstructionList;

    private String mInstruction = ""; //$NON-NLS-1$
    private Combo mInstructionCombo = null;
    private Text mParametersText = null;
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
    public NSISInstructionDialog(Shell parentShell, String instruction)
    {
        super(parentShell);
        mInstruction = instruction;
    }

    public NSISInstructionDialog(Shell parentShell)
    {
        this(parentShell,""); //$NON-NLS-1$
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
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayout(new GridLayout(2,false));
        
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
        mInstructionCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                char[] chars = mInstructionCombo.getText().toCharArray();
                if(!Common.isEmptyArray(chars)) {
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    int pos = mInstructionCombo.getSelection().x;
                    for (int i = 0; i < chars.length; i++) {
                        if(Character.isLetterOrDigit(chars[i]) || (i==0 && chars[i]=='!')) {
                            buf.append(chars[i]);
                        }
                        else {
                            if(i <= pos && pos > 0) {
                                pos--;
                            }
                        }
                    }
                    if(buf.length() != chars.length) {
                        mInstructionCombo.setText(buf.toString());
                        mInstructionCombo.setSelection(new Point(pos,pos));
                        return;
                    }
                }
                validate();
            }
        });
        
        mParametersText = createText(composite, EclipseNSISPlugin.getResourceString("instructions.parameters.text"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("instructions.parameters.tooltip"),parameters); //$NON-NLS-1$
        mParametersText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        return composite;
    }
    
    public void create()
    {
        super.create();
        validate();
    }
    
    private void validate()
    {
        DialogStatus status = getStatus();
        if(Common.isEmpty(mInstructionCombo.getText())) {
            status.setError(EclipseNSISPlugin.getResourceString("instruction.blank.error")); //$NON-NLS-1$
        }
        else {
            status.setOK();
        }
        refreshStatus();
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
        
        Combo combo = new Combo(composite, SWT.DROP_DOWN|SWT.BORDER);
        combo.setToolTipText(tooltipText);
        if(!Common.isEmptyArray(list)) {
            for(int i=0; i<list.length; i++) {
                combo.add(list[i]);
            }
        }
        
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
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertWidthInCharsToPixels(40);
        text.setLayoutData(data);
        text.setText(value);
        
        return text;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        mInstruction = new StringBuffer(mInstructionCombo.getText().trim()).append(" ").append(mParametersText.getText().trim()).toString().trim(); //$NON-NLS-1$
        super.okPressed();
    }

    /**
     * @return Returns the instruction.
     */
    public String getInstruction()
    {
        return mInstruction;
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
