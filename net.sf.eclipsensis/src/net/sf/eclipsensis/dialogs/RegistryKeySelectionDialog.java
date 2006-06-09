/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.Arrays;
import java.util.Comparator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class RegistryKeySelectionDialog extends StatusMessageDialog
{
    private static final int[] cRootKeys = {WinAPI.HKEY_CLASSES_ROOT,
                                           WinAPI.HKEY_CURRENT_USER,
                                           WinAPI.HKEY_LOCAL_MACHINE,
                                           WinAPI.HKEY_USERS,
                                           WinAPI.HKEY_CURRENT_CONFIG};
    private static final String REG_KEY = "regKey"; //$NON-NLS-1$
    
    //For backward compatability
    private static final String ROOT_KEY = "rootKey"; //$NON-NLS-1$
    private static final String SUB_KEY = "subKey"; //$NON-NLS-1$
    
    private static final Image REGKEY_IMAGE;
    private static final Image OPEN_REGKEY_IMAGE;
    private static final Image REGROOT_IMAGE;
    
    private IDialogSettings mDialogSettings;
    private RegistryRoot mRegistryRoot = new RegistryRoot();
    private RegistryKey mRegKey=null;
    private String mText = null;

    static {
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        REGKEY_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.key.image")); //$NON-NLS-1$
        OPEN_REGKEY_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.key.open.image")); //$NON-NLS-1$
        REGROOT_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.root.image")); //$NON-NLS-1$
    }

    public RegistryKeySelectionDialog(Shell parent)
    {
        super(parent);
        setHelpAvailable(false);
        IDialogSettings dialogSettings = EclipseNSISPlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        setTitle(EclipseNSISPlugin.getResourceString("regkey.dialog.title")); //$NON-NLS-1$
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

    public boolean close()
    {
        new Thread(new Runnable() {
            public void run()
            {
                mRegistryRoot.close();
            }
            
        }).start();
        return super.close();
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
                return ""; //$NON-NLS-1$
        }
    }

    public String getRegKey()
    {
        return (mRegKey != null?mRegKey.toString():""); //$NON-NLS-1$
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
        final Tree tree = new Tree(composite,SWT.VIRTUAL|SWT.BORDER);
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        data.widthHint = data.heightHint = 400;
        tree.setLayoutData(data);
        tree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                final TreeItem item = (TreeItem)event.item;
                TreeItem parentItem = item.getParentItem();
                RegistryKey key;
                if (parentItem == null) {
                    key = mRegistryRoot;
                    item.getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            item.setExpanded(true);
                        }
                    });
                } 
                else 
                {
                    RegistryKey parent = (RegistryKey)parentItem.getData();
                    key = parent.getChildren()[parentItem.indexOf(item)];
                }
                item.setData(key);
                item.setText(key.getName());
                item.setImage(key.getImage());
                int childCount = key.getChildCount();
                item.setItemCount(childCount<0?1:childCount);
            }
        });
        tree.addTreeListener(new TreeListener() {
            public void treeExpanded(TreeEvent e)
            {
                final RegistryKey key = (RegistryKey)e.item.getData();
                BusyIndicator.showWhile(e.display,new Runnable() {
                    public void run()
                    {
                        key.open();
                    }
                });
                TreeItem item = (TreeItem)e.item;
                item.setItemCount(key.getChildCount());
                if(key.getChildCount() > 0) {
                    item.setImage(key.getExpandedImage());
                }
            }

            public void treeCollapsed(TreeEvent e)
            {
                RegistryKey key = (RegistryKey)e.item.getData();
                TreeItem item = (TreeItem)e.item;
                item.setImage(key.getImage());
            }
        });
        tree.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                okPressed();
            }

            public void widgetSelected(SelectionEvent e)
            {
                TreeItem item = (TreeItem)e.item;
                saveSelection(item);
            }
        });
        tree.setItemCount(1); // The registry root "My Computer"
        tree.update();
        restoreSelection(tree);
        return composite;
    }

    /**
     * @param tree
     */
    private void restoreSelection(Tree tree)
    {
        String regKey = mDialogSettings.get(REG_KEY);
        if(regKey == null) {
            try {
                Integer rootKey = new Integer(mDialogSettings.getInt(ROOT_KEY));
                String subKey = mDialogSettings.get(SUB_KEY);
                if(!Common.isEmpty(subKey)) {
                    StringBuffer buf = new StringBuffer(getRootKeyName(rootKey.intValue()));
                    if(buf.length() > 0) {
                        buf.append("\\").append(subKey); //$NON-NLS-1$
                    }
                    regKey = buf.toString();
                }
                else {
                    regKey = getRootKeyName(rootKey.intValue());
                }
            }
            catch (NumberFormatException e) {
                regKey = ""; //$NON-NLS-1$
            }
        }

        if(!Common.isEmpty(regKey)) {
            int n = regKey.indexOf('\\');
            String rootKey;
            String subKey;
            if(n > 0) {
                subKey = regKey.substring(n+1);
                rootKey = regKey.substring(0,n);
            }
            else {
                subKey = null;
                rootKey = regKey;
            }
            int hRootKey = 0;
            for (int i = 0; i < cRootKeys.length; i++) {
                if(Common.stringsAreEqual(rootKey,getRootKeyName(cRootKeys[i]),true)) {
                    hRootKey = cRootKeys[i];
                    break;
                }
            }
            if(hRootKey != 0) {
                boolean exists = true;
                if(subKey != null) {
                    exists = WinAPI.RegKeyExists(hRootKey, subKey);
                }
                if(exists) {
                    final TreeItem item = tree.getItem(0);
                    final String finalRegKey = regKey;
                    item.getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            BusyIndicator.showWhile(item.getDisplay(),new Runnable() {
                                public void run()
                                {
                                    select(item, finalRegKey);
                                }
                            });
                        }
                    });
                }
            }
        }
    }
 
    protected void okPressed()
    {
        mDialogSettings.put(REG_KEY, getRegKey());
        super.okPressed();
    }
    
    /**
     * @param item
     * @param regKey
     */
    private boolean select(final TreeItem item, final String regKey)
    {
        String keyName;
        String subKeyName;
        int n = regKey.indexOf('\\');
        if(n > 0) {
            subKeyName = regKey.substring(n+1);
            keyName = regKey.substring(0,n);
        }
        else {
            keyName = regKey;
            subKeyName = null;
        }
        RegistryKey key = (RegistryKey)item.getData();
        int index = key.find(keyName);
        if(index >= 0) {
            TreeItem childItem = item.getItem(index);
            if(subKeyName != null) {
                boolean isExpanded = item.getExpanded();
                if(!isExpanded) {
                    item.setExpanded(true);
                }
                key = key.getChildren()[index];
                key.open();
                if(childItem.getItemCount() != key.getChildCount()) {
                    childItem.setItemCount(key.getChildCount());
                }
                boolean result = select(childItem, subKeyName);
                if(!result) {
                    item.setExpanded(isExpanded);
                }
                return result;
            }
            else {
                Tree tree = childItem.getParent();
                tree.showItem(childItem);
                tree.setSelection(childItem);
                tree.update();
                saveSelection(childItem);
                return true;
            }
        }
        return false;
    }

    /**
     * @param item
     */
    private void saveSelection(TreeItem item)
    {
        mRegKey = (RegistryKey)item.getData();
        String string = ""; //$NON-NLS-1$
        if(mRegKey != null) {
            string = mRegKey.toString();
        }
        DialogStatus status;
        if(Common.isEmpty(string)) {
            status = new DialogStatus(IStatus.ERROR,EclipseNSISPlugin.getResourceString("regkey.dialog.select.regkey.error")); //$NON-NLS-1$
        }
        else {
            string = new StringBuffer(mRegistryRoot.getName()).append("\\").append(string).toString(); //$NON-NLS-1$
            status = new DialogStatus(IStatus.OK,string);
        }
        updateStatus(status);
    }

    private static Comparator cSearchComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            return getName(o1).compareTo(getName(o2));
        }
        
        private String getName(Object o)
        {
            if(o instanceof String) {
                return ((String)o).toLowerCase();
            }
            else if(o instanceof RegistryKey) {
                return ((RegistryKey)o).getName().toLowerCase();
            }
            return null;
        }
    };

    private class RegistryKey
    {
        
        protected int mHandle = 0;
        protected String mName = ""; //$NON-NLS-1$
        protected int mChildCount = -1;
        protected RegistryKey[] mChildren = null;
        protected RegistryKey mParent = null;
        private String mString = null;
        
        public RegistryKey(RegistryKey parent, int handle, String name)
        {
            this(parent, name);
            mHandle = handle;
        }

        public RegistryKey(RegistryKey parent, String name)
        {
            mParent = parent;
            mName = name;
        }

        public Image getImage()
        {
            return REGKEY_IMAGE;
        }
        
        public Image getExpandedImage()
        {
            return OPEN_REGKEY_IMAGE;
        }

        public int getChildCount()
        {
            return mChildCount;
        }

        public RegistryKey[] getChildren()
        {
            return mChildren;
        }

        public String getName()
        {
            return mName;
        }

        public RegistryKey getParent()
        {
            return mParent;
        }
        
        public int find(String name)
        {
            if(mChildCount < 0) {
                return open(name);
            }
            return Arrays.binarySearch(mChildren,name,cSearchComparator);
        }

        private int open(String name)
        {
            int result = -1;
            if(mHandle == 0) {
                mHandle = WinAPI.RegOpenKeyEx(getParent().mHandle,getName(),0,WinAPI.KEY_QUERY_VALUE|WinAPI.KEY_ENUMERATE_SUB_KEYS);
            }
            if(mChildCount < 0) {
                if(mHandle != 0) {
                    int[] sizes = {0,0};
                    WinAPI.RegQueryInfoKey(mHandle,sizes);
                    mChildCount = sizes[0];
                    if(mChildCount > 0) {
                        mChildren = new RegistryKey[mChildCount];
                        for (int i = 0; i < mChildren.length; i++) {
                            String subKey = WinAPI.RegEnumKeyEx(mHandle,i,sizes[1]);
                            mChildren[i] = new RegistryKey(this,subKey);
                            if(result < 0 && name != null && Common.stringsAreEqual(name, subKey, true)) {
                                result = i;
                            }
                        }
                        Arrays.sort(mChildren,cSearchComparator);
                    }
                }
                else {
                    mChildCount = 0;
                }
            }
            return result;
        }
        
        public void open()
        {
            open(null);
        }
        
        public void close()
        {
            if(mChildCount > 0) {
                if(!Common.isEmptyArray(mChildren)) {
                    for (int i=0; i<mChildren.length; i++) {
                        mChildren[i].close();
                    }
                }
                mChildCount = 0;
                mChildren = null;
            }
            if(!(getParent() instanceof RegistryRoot) && mHandle > 0) {
                WinAPI.RegCloseKey(mHandle);
                mHandle = 0;
            }            
        }

        public int hashCode()
        {
            return mName.hashCode();
        }

        public boolean equals(Object o)
        {
            if(o != this) {
                if(o instanceof RegistryKey) {
                    RegistryKey r = (RegistryKey)o;
                    if(Common.objectsAreEqual(getParent(),r.getParent())) {
                        return Common.stringsAreEqual(r.getName(),getName(),true);
                    }
                }
                return false;
            }
            return true;
        }
        
        public String toString()
        {
            if(mString == null) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                expandName(buf);
                mString = buf.toString();
            }
            return mString;
        }
        
        protected void expandName(StringBuffer buf)
        {
            if(mParent != null) {
                mParent.expandName(buf);
                if(buf.length() > 0) {
                    buf.append("\\"); //$NON-NLS-1$
                }
            }
            buf.append(mName);
        }
    }
    
    private class RegistryRoot extends RegistryKey
    {
        public RegistryRoot()
        {
            super(null, -1, EclipseNSISPlugin.getResourceString("regkey.dialog.regroot.label")); //$NON-NLS-1$
            mChildren = new RegistryKey[cRootKeys.length];
            for (int i = 0; i < mChildren.length; i++) {
                mChildren[i] = new RegistryKey(this, cRootKeys[i], getRootKeyName(cRootKeys[i]));
            }
            mChildCount = mChildren.length;
        }

        protected void expandName(StringBuffer buf)
        {
        }

        public Image getImage()
        {
            return REGROOT_IMAGE;
        }
        
        public Image getExpandedImage()
        {
            return REGROOT_IMAGE;
        }
    }
}
