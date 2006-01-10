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
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class RegistryKeySelectionDialog extends StatusMessageDialog
{
    private static final String ROOT_KEY = "rootKey"; //$NON-NLS-1$
    private static final String SUB_KEY = "subKey"; //$NON-NLS-1$
    
    private static Integer[] cRootKeys = { new Integer(WinAPI.HKEY_CLASSES_ROOT), 
                                           new Integer(WinAPI.HKEY_CURRENT_USER),
                                           new Integer(WinAPI.HKEY_LOCAL_MACHINE),
                                           new Integer(WinAPI.HKEY_USERS),
                                           new Integer(WinAPI.HKEY_CURRENT_CONFIG),
                                           new Integer(EclipseNSISPlugin.getDefault().isNT()?
                                               WinAPI.HKEY_PERFORMANCE_DATA:
                                               WinAPI.HKEY_DYN_DATA)
                                         };
    private static ILabelProvider cRootKeyLabelProvider = new LabelProvider() {
        public String getText(Object element)
        {
            if(element instanceof Integer) {
                return getRootKeyName(((Integer)element).intValue());
            }
            return super.getText(element);
        }
    };

    private int mRootKey = 0;
    private String mSubKey = ""; //$NON-NLS-1$
    IDialogSettings mDialogSettings;

    public RegistryKeySelectionDialog(Shell parent)
    {
        super(parent);
        IDialogSettings dialogSettings = EclipseNSISPlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        setTitle(EclipseNSISPlugin.getResourceString("regkey.dialog.title")); //$NON-NLS-1$
    }

    private static String getRootKeyName(int rootKey)
    {
        switch(rootKey) {
            case WinAPI.HKEY_CLASSES_ROOT:
                return "HKEY_CLASSES_ROOT"; //$NON-NLS-1$
            case WinAPI.HKEY_CURRENT_CONFIG:
                return "HKEY_CURRENT_CONFIG"; //$NON-NLS-1$
            case WinAPI.HKEY_CURRENT_USER:
                return "HKEY_CURRENT_USER"; //$NON-NLS-1$
            case WinAPI.HKEY_DYN_DATA:
                return "HKEY_DYN_DATA"; //$NON-NLS-1$
            case WinAPI.HKEY_LOCAL_MACHINE:
                return "HKEY_LOCAL_MACHINE"; //$NON-NLS-1$
            case WinAPI.HKEY_PERFORMANCE_DATA:
                return "HKEY_PERFORMANCE_DATA"; //$NON-NLS-1$
            case WinAPI.HKEY_USERS:
                return "HKEY_USERS"; //$NON-NLS-1$
            default:
                return null;
        }
    }

    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Composite composite1 = new Composite(composite, SWT.NONE);
        GridLayout layout1 = new GridLayout(2,false);
        layout1.marginHeight = 0;
        layout1.marginWidth = 0;
        composite1.setLayout(layout1);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = convertWidthInCharsToPixels(65);
        composite1.setLayoutData(gd);

        Label l = new Label(composite1,SWT.NULL);
        l.setText(EclipseNSISPlugin.getResourceString("root.key.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        
        Combo c1 = new Combo(composite1,SWT.DROP_DOWN|SWT.READ_ONLY); 
        c1.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        ComboViewer cv = new ComboViewer(c1);
        cv.setContentProvider(new ArrayContentProvider());
        cv.setLabelProvider(cRootKeyLabelProvider);
        cv.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                mRootKey = (sel.isEmpty()?0:((Integer)sel.getFirstElement()).intValue());
                validate();
            }
        });
        
        l = new Label(composite1,SWT.NULL);
        l.setText(EclipseNSISPlugin.getResourceString("sub.key.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        Text t = new Text(composite1, SWT.SINGLE|SWT.BORDER);
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mSubKey = ((Text)e.widget).getText();
                validate();
            }
        });
        t.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        Dialog.applyDialogFont(composite);
        
        cv.setInput(cRootKeys);

        boolean shouldValidate = true;
        try {
            Integer rootKey = new Integer(mDialogSettings.getInt(ROOT_KEY));
            cv.setSelection(new StructuredSelection(rootKey));
            shouldValidate = false;
        }
        catch (NumberFormatException e) {
        }
        
        try {
            String subKey = mDialogSettings.get(SUB_KEY);
            if(subKey != null) {
                t.setText(subKey);
                shouldValidate = false;
            }
        }
        catch (NumberFormatException e) {
        }
        
        if(shouldValidate) {
            validate();
        }
        
        return composite;
    }
 
    public String getRegKey()
    {
        return new StringBuffer(getRootKeyName(mRootKey)).append("\\").append(mSubKey).toString(); //$NON-NLS-1$
    }

    protected void validate()
    {
        if(mRootKey == 0) {
            getStatus().setError(EclipseNSISPlugin.getResourceString("missing.rootkey.error")); //$NON-NLS-1$
            return;
        }
        else {
            if(!WinAPI.RegKeyExists(mRootKey, mSubKey)) {
                getStatus().setError(EclipseNSISPlugin.getFormattedString("invalid.key.error",  //$NON-NLS-1$
                                     new String[] {getRootKeyName(mRootKey),mSubKey}));
                return;
            }
        }
        getStatus().setOK();
    }

    protected void okPressed()
    {
        mDialogSettings.put(ROOT_KEY, mRootKey);
        mDialogSettings.put(SUB_KEY, mSubKey);
        super.okPressed();
    }
}
