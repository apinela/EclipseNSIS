/*******************************************************************************
 * Copyright (c) 2005-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.proxy;

import java.net.PasswordAuthentication;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AuthenticationDialog extends Dialog
{
    protected Text mUserName;
    protected Text mPassword;

    protected String mHost;
    protected String mRealm;
    protected PasswordAuthentication mAuthentication = null;

    public static PasswordAuthentication getAuthentication(final String host, final String realm)
    {
        final PasswordAuthentication[] authentication = new PasswordAuthentication[1];

        Runnable r = new Runnable() {
            public void run() {
                authentication[0] = AuthenticationDialog.askForAuthentication(host, realm);
            }
        };

        if (Display.getCurrent() != null) {
            r.run();
        } else {
            Display.getDefault().syncExec(r);
        }
        return authentication[0];
    }

    protected static PasswordAuthentication askForAuthentication(String host, String message)
    {
        AuthenticationDialog ui = new AuthenticationDialog(null, host, message);
        ui.open();
        return ui.getAuthentication();
    }

    protected AuthenticationDialog(Shell parentShell, String host, String message)
    {
        super(parentShell);
        this.mHost = host;
        this.mRealm = message;
        setBlockOnOpen(true);
    }
    /**
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(EclipseNSISUpdatePlugin.getResourceString("authentication.dialog.title")); //$NON-NLS-1$
    }

    public void create()
    {
        super.create();
        mUserName.selectAll();
        mUserName.setFocus();
    }

    protected Control createDialogArea(Composite parent)
    {
        Composite main = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        main.setLayout(layout);
        main.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

        Label label = new Label(main, SWT.NONE);
        label.setText(EclipseNSISUpdatePlugin.getResourceString("proxy.server.label")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        label = new Label(main, SWT.BORDER);
        label.setText(mHost);
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        label = new Label(main, SWT.NONE);
        label.setText(EclipseNSISUpdatePlugin.getResourceString("realm.label")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        label = new Label(main, SWT.BORDER);
        label.setText(mRealm);
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        label = new Label(main, SWT.NONE);
        label.setText(EclipseNSISUpdatePlugin.getResourceString("user.name.label")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        mUserName = new Text(main, SWT.BORDER);
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
        mUserName.setLayoutData(data);

        label = new Label(main, SWT.NONE);
        label.setText(EclipseNSISUpdatePlugin.getResourceString("password.label")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        mPassword = new Text(main, SWT.BORDER | SWT.PASSWORD);
        data = new GridData(SWT.FILL,SWT.FILL,true,false);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
        mPassword.setLayoutData(data);

        return main;
    }

    public PasswordAuthentication getAuthentication() {
        return mAuthentication;
    }

    protected void okPressed()
    {
        mAuthentication = new PasswordAuthentication(mUserName.getText(), mPassword.getText().toCharArray());
        super.okPressed();
    }
}
