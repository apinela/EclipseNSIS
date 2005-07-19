/*******************************************************************************
 * Copyright (c) 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.io.File;
import java.security.KeyStoreException;

import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class JARVerifierOptionsDialog extends AbstractJAROptionsDialog
{
    private static final String CERTS = "certs"; //$NON-NLS-1$
    
    /**
     * @param parentShell
     * @throws KeyStoreException 
     */
    public JARVerifierOptionsDialog(Shell parentShell, boolean multi) throws KeyStoreException
    {
        super(parentShell, multi);
    }

    protected void init()
    {
        super.init();
        mValues.put(CERTS,mDialogSettings.getBoolean(CERTS)?Boolean.TRUE:Boolean.FALSE);
    }

    protected String getDialogTitle()
    {
        return JARSignerPlugin.getResourceString("jarverifier.dialog.title"); //$NON-NLS-1$
    }

    protected Control createDialogArea(Composite parent)
    {
        parent = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)parent.getLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = false;
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);

        final Button b1 = makeCheckBox(composite,VERBOSE,VERBOSE, false); //$NON-NLS-1$

        final Button b2 =makeCheckBox(composite,CERTS,CERTS, false); //$NON-NLS-1$
        ((GridData)b2.getLayoutData()).horizontalIndent = 10;

        String keystore = getDefaultKeyStore();
        if(JARSignerPlugin.isEmpty(keystore)) {
            keystore = ".keystore"; //$NON-NLS-1$
        }
        final Text t = makeFileBrowser(composite,"key.store.location", KEY_STORE,  //$NON-NLS-1$
                new FileSelectionAdapter("key.storer.location.message",keystore,false), //$NON-NLS-1$ //$NON-NLS-2$
                false); //$NON-NLS-1$
        GridData gd = (GridData)t.getLayoutData();
        gd.widthHint = convertWidthInCharsToPixels(50);
        final Label l = (Label)t.getData(ATTR_LABEL);
        ((GridData)l.getLayoutData()).horizontalIndent = 10;
        final Button b3 = (Button)t.getData(ATTR_BUTTON);
        final Runnable r = new Runnable() {
            public void run()
            {
                boolean state = b1.getSelection();
                b2.setEnabled(state);
                t.setEnabled(state);
                l.setEnabled(state);
                b3.setEnabled(state);
            }
        };
        
        b1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                r.run();
            }
        });
        
        makeFileBrowser(composite,"tools.jar.location", TOOLS_JAR,  //$NON-NLS-1$
                new FileSelectionAdapter("tools.jar.location.message",JARSignerPlugin.getResourceString("tools.jar.name"),false), //$NON-NLS-1$ //$NON-NLS-2$
                true); //$NON-NLS-1$
        
        if(mMulti) {
            makeCheckBox(composite,IGNORE_ERRORS,IGNORE_ERRORS,false); //$NON-NLS-1$
        }
        
        r.run();
        return parent;
    }
    
    protected boolean isValid()
    {
        boolean state = true;
        
        if(!JARSignerPlugin.isEmpty(getKeyStore())) {
            File f = new File(getKeyStore());
            state = (f.exists() && f.isFile());
        }

        return state;
    }

    protected void okPressed()
    {
        mDialogSettings.put(CERTS,isCerts());
        super.okPressed();
    }

    public boolean isCerts()
    {
        return ((Boolean)mValues.get(CERTS)).booleanValue();
    }
}
