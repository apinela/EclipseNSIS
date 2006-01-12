/*******************************************************************************
 * Copyright (c) 2005-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class JARVerifierOptionsDialog extends AbstractJAROptionsDialog
{
    private static final String CERTS = "certs"; //$NON-NLS-1$

    private List mControlsList;
    /**
     * @param parentShell
     */
    public JARVerifierOptionsDialog(Shell parentShell, List selection)
    {
        super(parentShell, selection);
    }

    protected void init()
    {
        mControlsList = new ArrayList();
        super.init();
        setValue(CERTS,getDialogSettings().getBoolean(CERTS)?Boolean.TRUE:Boolean.FALSE);
    }

    protected String getDialogTitle()
    {
        return JARSignerPlugin.getResourceString("jarverifier.dialog.title"); //$NON-NLS-1$
    }

    protected void createValuesDialogArea(Composite parent)
    {
    }

    protected void createFlagsDialogArea(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        applyDialogFont(composite);

        String keystore = getDefaultKeyStore();
        if(Common.isEmpty(keystore)) {
            keystore = ".keystore"; //$NON-NLS-1$
        }
        Text t = makeFileBrowser(composite,JARSignerPlugin.getResourceString("key.store.location"), KEY_STORE,  //$NON-NLS-1$
                new FileSelectionAdapter(JARSignerPlugin.getResourceString("key.store.location.message"),keystore,true), //$NON-NLS-1$
                false);
        gd = (GridData)t.getLayoutData();
        gd.widthHint = convertWidthInCharsToPixels(50);
        mControlsList.add(t);

        Label l = (Label)t.getData(ATTR_LABEL);
        mControlsList.add(l);

        Button b = (Button)t.getData(ATTR_BUTTON);
        mControlsList.add(b);

        b = makeCheckBox(composite,JARSignerPlugin.getResourceString(CERTS),CERTS, false);
        mControlsList.add(b);
    }

    protected void valueChanged(String name, Object oldValue, Object newValue)
    {
        if(name.equals(VERBOSE)) {
            boolean state = ((Boolean)newValue).booleanValue();
            updateControlsState(state);
        }
        super.valueChanged(name, oldValue, newValue);
    }

    private void updateControlsState(boolean state)
    {
        for(Iterator iter=mControlsList.iterator(); iter.hasNext(); ) {
            ((Control)iter.next()).setEnabled(state);
        }
    }

    public void create()
    {
        super.create();
        updateControlsState(isVerbose());
    }

    protected void okPressed()
    {
        getDialogSettings().put(CERTS,isCerts());
        super.okPressed();
    }

    public boolean isCerts()
    {
        return ((Boolean)getValues().get(CERTS)).booleanValue();
    }
}
