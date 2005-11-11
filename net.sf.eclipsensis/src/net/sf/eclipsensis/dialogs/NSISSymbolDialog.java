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

import java.util.Collection;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISSymbolDialog extends StatusMessageDialog
{
    private String mName = ""; //$NON-NLS-1$
    private String mValue = ""; //$NON-NLS-1$
    private Text mNameText = null;
    private Text mValueText = null;
    private Collection mExistingSymbols = null;

    /**
     * @param parentShell
     */
    public NSISSymbolDialog(Shell parentShell, String name, String value)
    {
        super(parentShell);
        mName = name;
        mValue = value;
    }

    public NSISSymbolDialog(Shell parentShell)
    {
        this(parentShell,"",""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param existingSymbols The existingSymbols to set.
     */
    public void setExistingSymbols(Collection existingSymbols)
    {
        mExistingSymbols = existingSymbols;
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
     * @see net.sf.eclipsensis.dialogs.StatusMessageDialog#createControl(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        mNameText = createText(composite, EclipseNSISPlugin.getResourceString("symbols.name.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("symbols.name.tooltip"),mName); //$NON-NLS-1$

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

        mNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                validate();
            }
        });
        mNameText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);

        mValueText = createText(composite, EclipseNSISPlugin.getResourceString("symbols.value.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("symbols.value.tooltip"),mValue); //$NON-NLS-1$
        mValueText.setTextLimit(INSISConstants.DIALOG_TEXT_LIMIT);
        Dialog.applyDialogFont(composite);
        return composite;
    }

    protected Text createText(Composite composite, String labelText, String tooltipText, String value)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(labelText);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setToolTipText(tooltipText);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = convertWidthInCharsToPixels(40);
        text.setLayoutData(gridData);
        text.setText(value);

        return text;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return mValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        mName = mNameText.getText();
        if(!Common.isEmptyCollection(mExistingSymbols) && mExistingSymbols.contains(mName)) {
            if(!Common.openConfirm(getShell(), EclipseNSISPlugin.getFormattedString("symbol.overwrite.confirm", //$NON-NLS-1$
                                                                  new String[]{mName}),
                                   EclipseNSISPlugin.getShellImage())) {
                return;
            }
        }

        mValue = mValueText.getText();
        super.okPressed();
    }

    private void validate()
    {
        DialogStatus status = getStatus();
        String name = mNameText.getText();
        if(Common.isEmpty(name)) {
            status.setError(EclipseNSISPlugin.getResourceString("symbol.name.blank.error")); //$NON-NLS-1$
        }
        else {
            status.setOK();
        }
        refreshStatus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create()
    {
        super.create();
        validate();
    }
}
