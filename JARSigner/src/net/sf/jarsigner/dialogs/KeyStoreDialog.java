/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.security.KeyStore;

import net.sf.eclipsensis.utilities.UtilitiesPlugin;
import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class KeyStoreDialog extends Dialog
{
    private String mKeyStoreName;
    private String mStorePassword;
    private KeyStore mKeyStore = null;

    public KeyStoreDialog(Shell parentShell, String keyStoreName, String storePassword)
    {
        super(parentShell);
        mKeyStoreName = keyStoreName;
        mStorePassword = storePassword;
    }

    @Override
	protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(JARSignerPlugin.getResourceString("keystore.dialog.title")); //$NON-NLS-1$
    }

    @Override
	protected Control createDialogArea(Composite parent)
    {
        Composite parent2 = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)parent2.getLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = false;
        Composite composite = new Composite(parent2,SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);

        Label l = new Label(composite, SWT.NONE);
        l.setText(JARSignerPlugin.getResourceString("key.store.location")); //$NON-NLS-1$
        l.setLayoutData(new GridData());
        final Text text = new Text(composite,SWT.BORDER);
        text.setText(mKeyStoreName);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mKeyStoreName = text.getText();
                getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(mKeyStoreName));
            }
        });
        Button b = new Button(composite,SWT.PUSH);
        b.setLayoutData(new GridData());
        b.setText(UtilitiesPlugin.getResourceString("browse.label")); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(getShell(),SWT.OPEN);
                dialog.setText(JARSignerPlugin.getResourceString("key.store.location.message")); //$NON-NLS-1$
                String file = text.getText();
                dialog.setFileName(Common.isEmpty(file)?JARSignerPlugin.getResourceString("key.store.name"):file); //$NON-NLS-1$
                file = dialog.open();
                if(file != null) {
                    text.setText(file);
                }
            }
        });

        l = new Label(composite, SWT.NONE);
        l.setText(JARSignerPlugin.getResourceString("store.pass")); //$NON-NLS-1$
        l.setLayoutData(new GridData());
        final Text text2 = new Text(composite,SWT.BORDER);
        text2.setText(mStorePassword);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        gd.widthHint = convertWidthInCharsToPixels(50);
        text2.setLayoutData(gd);
        text2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStorePassword = text2.getText();
            }
        });

        return parent2;
    }

    @Override
	protected void okPressed()
    {
        mKeyStore = JARSignerPlugin.loadKeyStore(mKeyStoreName,mStorePassword);
        if(mKeyStore != null) {
            super.okPressed();
        }
        else {
            MessageDialog.openError(getShell(),UtilitiesPlugin.getResourceString("error.title"), //$NON-NLS-1$
                    JARSignerPlugin.getResourceString("invalid.keystore")); //$NON-NLS-1$
        }
    }

    public KeyStore getKeyStore()
    {
        return mKeyStore;
    }

    public String getKeyStoreName()
    {
        return mKeyStoreName;
    }

    public String getStorePassword()
    {
        return mStorePassword;
    }
}
