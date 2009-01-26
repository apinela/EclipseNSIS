/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.RegistryKeyBrowser.RegistryKey;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class RegistryValueSelectionDialog extends StatusMessageDialog
{
    private RegistryValue mRegValue = null;
    private String mText = null;

    private RegistryKeyBrowser mBrowser;
    private TableViewer mValuesViewer;

    public RegistryValueSelectionDialog(Shell parent)
    {
        super(parent);
        setHelpAvailable(false);
        setTitle(EclipseNSISPlugin.getResourceString("regval.dialog.title")); //$NON-NLS-1$
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String text)
    {
        mText = text;
    }

    protected int getMessageLabelStyle()
    {
        return SWT.NONE;
    }

    public RegistryValue getRegistryValue()
    {
        saveSelection();
        return mRegValue;
    }

    /**
     *
     */
    private void saveSelection()
    {
        if(mValuesViewer != null && mValuesViewer.getTable() != null && !mValuesViewer.getTable().isDisposed()) {
            IStructuredSelection sel = (IStructuredSelection)mValuesViewer.getSelection();
            if(sel != null && !sel.isEmpty()) {
                mRegValue = (RegistryValue)sel.getFirstElement();
            }
            else {
                mRegValue = null;
            }
        }
    }

    public void setRegistryValue(RegistryValue regValue)
    {
        mRegValue = regValue;
        select();
    }

    private void select()
    {
        if(mRegValue != null) {
            if(mBrowser != null && !mBrowser.isDisposed()) {
                mBrowser.select(mRegValue.getRegKey());
            }
        }
    }

    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        if(mText != null) {
            Label l =  new Label(composite,SWT.WRAP);
            l.setText(mText);
            l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        }
        SashForm sashForm = new SashForm(composite, SWT.HORIZONTAL|SWT.SMOOTH);

        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        data.widthHint = 600;
        data.heightHint = 400;
        sashForm.setLayoutData(data);

        mBrowser = new RegistryKeyBrowser(sashForm,SWT.BORDER);

        final Table table = new Table(sashForm,SWT.BORDER);
        TableColumn col1 = new TableColumn(table,SWT.NONE);
        col1.setText(EclipseNSISPlugin.getResourceString("reg.value.dialog.name.label")); //$NON-NLS-1$
        col1.setWidth(150);
        TableColumn col2 = new TableColumn(table,SWT.NONE);
        col2.setText(EclipseNSISPlugin.getResourceString("reg.value.dialog.type.label")); //$NON-NLS-1$
        col2.setWidth(100);
        TableColumn col3 = new TableColumn(table,SWT.NONE);
        col3.setText(EclipseNSISPlugin.getResourceString("reg.value.dialog.data.label")); //$NON-NLS-1$
        col3.setWidth(1000);
        table.setHeaderVisible(true);
        mValuesViewer = new TableViewer(table);
        mValuesViewer.setContentProvider(new CollectionContentProvider());
        mValuesViewer.setLabelProvider(new CollectionLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex)
            {
                if(element instanceof RegistryValue && columnIndex == 0) {
                    switch(((RegistryValue)element).getType()) {
                        case WinAPI.REG_BINARY:
                        case WinAPI.REG_DWORD:
                            return CommonImages.REG_DWORD_IMAGE;
                        default:
                            return CommonImages.REG_SZ_IMAGE;
                    }
                }
                return super.getColumnImage(element, columnIndex);
            }

            public String getColumnText(Object element, int columnIndex)
            {
                if(element instanceof RegistryValue) {
                    RegistryValue regValue = (RegistryValue)element;
                    String value = regValue.getValue();
                    int type = regValue.getType();
                    String data = regValue.getData();
                    boolean isDefault = (value==null || value.length() == 0);
                    switch(columnIndex) {
                        case 0:
                            return isDefault?EclipseNSISPlugin.getResourceString("reg.value.dialog.default.value"):value; //$NON-NLS-1$
                        case 1:
                            switch(type) {
                                case WinAPI.REG_SZ:
                                    return "REG_SZ"; //$NON-NLS-1$
                                case WinAPI.REG_BINARY:
                                    return "REG_BINARY"; //$NON-NLS-1$
                                case WinAPI.REG_DWORD:
                                    return "REG_DWORD"; //$NON-NLS-1$
                                case WinAPI.REG_EXPAND_SZ:
                                    return "REG_EXPAND_SZ"; //$NON-NLS-1$
                            }
                        case 2:
                            switch(type) {
                                case WinAPI.REG_BINARY:
                                    if(data == null) {
                                        return EclipseNSISPlugin.getResourceString("reg.value.dialog.zero.length.binary"); //$NON-NLS-1$
                                    }
                                    else {
                                        StringBuffer buf = new StringBuffer();
                                        int len = data.length()-1;
                                        for(int i=0; i<len; i++) {
                                            if(i > 0) {
                                                buf.append(" "); //$NON-NLS-1$
                                            }
                                            buf.append(data.charAt(i++)).append(data.charAt(i));
                                        }
                                        return buf.toString();
                                    }
                                case WinAPI.REG_DWORD:
                                    int intData;
                                    try {
                                        intData = Integer.parseInt(data);
                                    }
                                    catch(NumberFormatException e) {
                                        intData = 0;
                                    }
                                    return new StringBuffer("0x").append(Common.leftPad(Integer.toHexString(intData).toLowerCase(),8,'0')).append( //$NON-NLS-1$
                                            " (").append(intData).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                                default:
                                    if(type == WinAPI.REG_SZ && isDefault) {
                                        if(data == null) {
                                            return EclipseNSISPlugin.getResourceString("reg.value.dialog.unset.value"); //$NON-NLS-1$
                                        }
                                    }
                                    return data;
                            }
                    }
                }
                return super.getColumnText(element, columnIndex);
            }
        });
        mBrowser.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                loadRegistryValues();
                if(mRegValue != null) {
                    mValuesViewer.setSelection(new StructuredSelection(mRegValue));
                }
            }
        });
        mValuesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                ISelection selection = mValuesViewer.getSelection();
                getButton(IDialogConstants.OK_ID).setEnabled(selection != null && !selection.isEmpty());
            }
        });
        select();
        sashForm.setWeights(new int[] {1,2});
        return composite;
    }

    public void create()
    {
        super.create();
        ISelection selection = mValuesViewer.getSelection();
        getButton(IDialogConstants.OK_ID).setEnabled(selection != null && !selection.isEmpty());
    }

    protected void cancelPressed()
    {
        mRegValue = null;
        super.cancelPressed();
    }

    protected void okPressed()
    {
        saveSelection();
        super.okPressed();
    }

    /**
     *
     */
    private void loadRegistryValues()
    {
        RegistryKey regKey = mBrowser.getSelectedKey();
        List list;
        if(regKey != null) {
            list = (List)regKey.getAttribute("REG_VALUES"); //$NON-NLS-1$
            if(list == null) {
                String regkeyName = mBrowser.getSelection();
                list = new ArrayList();
                int hKey = regKey.getHandle();
                if(hKey != 0) {
                    boolean gotDefault = false;
                    int count = WinAPI.GetRegValuesCount(hKey);
                    if(count > 0) {
                        for(int i=0; i<count; i++) {
                            RegistryValue regValue = new RegistryValue(regkeyName, null);
                            if(WinAPI.RegEnumValue(hKey,i,regValue)) {
                                switch(regValue.getType()) {
                                    case WinAPI.REG_SZ:
                                        String value = regValue.getValue();
                                        if(value == null || value.length() == 0) {
                                            gotDefault = true;
                                            list.add(0,regValue);
                                            break;
                                        }
                                    case WinAPI.REG_BINARY:
                                    case WinAPI.REG_DWORD:
                                    case WinAPI.REG_EXPAND_SZ:
                                        list.add(regValue);
                                }
                            }
                        }
                    }
                    if(!gotDefault) {
                        list.add(0,new RegistryValue(regkeyName,null,WinAPI.REG_SZ,null));
                    }
                }
                regKey.setAttribute("REG_VALUES", list); //$NON-NLS-1$
            }
        }
        else {
            list = Collections.EMPTY_LIST;
        }
        if(!Common.objectsAreEqual(list, mValuesViewer.getInput())) {
            mValuesViewer.setInput(list);
        }
    }

    public static class RegistryValue
    {
        private String mRegKey;
        private String mValue;
        private int mType = WinAPI.REG_SZ;
        private String mData = ""; //$NON-NLS-1$

        public RegistryValue(String regKey, String value)
        {
            mRegKey = regKey;
            setValue(value);
        }

        private RegistryValue(String regKey, String value, int type, String data)
        {
            this(regKey, value);
            setType(type);
            mData = data;
        }

        protected void set(String value, int type, byte[] data)
        {
            setValue(value);
            setType(type);
            switch(type) {
                case WinAPI.REG_BINARY:
                    if(Common.isEmptyArray(data)) {
                        mData = null;
                    }
                    else {
                        mData = bytesToHexString(data);
                    }
                    break;
                case WinAPI.REG_DWORD:
                    if(Common.isEmptyArray(data) || data.length > 8) {
                        mData = "0"; //$NON-NLS-1$
                    }
                    else {
                        mData = String.valueOf(Integer.parseInt(bytesToHexString((byte[])Common.flipArray(data)),16));
                    }
                    break;
                case WinAPI.REG_SZ:
                case WinAPI.REG_EXPAND_SZ:
                case WinAPI.REG_MULTI_SZ:
                    if(Common.isEmptyArray(data)) {
                        mData = ""; //$NON-NLS-1$
                    }
                    else {
                        mData = new String(data,0,(data[data.length-1]==0?data.length-1:data.length));
                    }
                    break;
            }
        }

        /**
         * @param value
         */
        private void setValue(String value)
        {
            mValue = value;
        }

        /**
         * @param type
         */
        private void setType(int type)
        {
            switch(type) {
                case WinAPI.REG_SZ:
                case WinAPI.REG_BINARY:
                case WinAPI.REG_DWORD:
                case WinAPI.REG_EXPAND_SZ:
                case WinAPI.REG_MULTI_SZ:
                    mType = type;
                    break;
                default:
                    mType = WinAPI.REG_SZ;
            }
        }

        private String bytesToHexString(byte[] data)
        {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < data.length; i++) {
                int b = data[i]<0?256+data[i]:(int)data[i];
                int hi = b/16;
                int lo = b%16;
                buf.append((char)(hi>9?'a'+hi-10:'0'+hi-0));
                buf.append((char)(lo>9?'a'+lo-10:'0'+lo-0));
            }
            return buf.toString();
        }

        public String getData()
        {
            return mData;
        }

        public String getRegKey()
        {
            return mRegKey;
        }

        public int getType()
        {
            return mType;
        }

        public String getValue()
        {
            return mValue;
        }

        public int hashCode()
        {
            int result = 31 + ((mRegKey == null)?0:mRegKey.hashCode());
            result = 31 * result + ((mValue == null)?0:mValue.hashCode());
            return result;
        }

        public boolean equals(Object obj)
        {
            if(this != obj) {
                if(obj instanceof RegistryValue) {
                    RegistryValue rv = (RegistryValue)obj;
                    return Common.stringsAreEqual(mRegKey,rv.getRegKey(),true) && Common.stringsAreEqual(mValue,rv.getValue(),true);
                }
                return false;
            }
            return true;
        }
    }
}
