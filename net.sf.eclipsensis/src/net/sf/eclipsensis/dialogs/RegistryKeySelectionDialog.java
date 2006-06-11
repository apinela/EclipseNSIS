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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class RegistryKeySelectionDialog extends StatusMessageDialog
{
    private static final int[] ROOT_KEYS = {WinAPI.HKEY_CLASSES_ROOT,
                                           WinAPI.HKEY_CURRENT_USER,
                                           WinAPI.HKEY_LOCAL_MACHINE,
                                           WinAPI.HKEY_USERS,
                                           WinAPI.HKEY_CURRENT_CONFIG};
    
    private static int cRootKey = 0;
    private static String cSubKey = null;
    
    private static final Image REGKEY_IMAGE;
    private static final Image OPEN_REGKEY_IMAGE;
    private static final Image REGROOT_IMAGE;
    
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
                boolean expanded = false;
                if (parentItem == null) {
                    key = mRegistryRoot;
                    expanded = true;
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
                if(expanded) {
                    item.setExpanded(expanded);
                }
                if(key instanceof RegistryRoot) {
                    restoreSelection(item.getParent());
                }
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
        return composite;
    }

    /**
     * @param tree
     */
    private void restoreSelection(final Tree tree)
    {
        if(cRootKey != 0) {
            boolean exists = true;
            if(cSubKey != null) {
                exists = WinAPI.RegKeyExists(cRootKey, cSubKey);
            }
            if(exists) {
                final TreeItem item = tree.getItem(0);
                final String regKey = (cSubKey==null?getRootKeyName(cRootKey):new StringBuffer(getRootKeyName(cRootKey)).append("\\").append(cSubKey).toString());
                item.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        BusyIndicator.showWhile(item.getDisplay(),new Runnable() {
                            public void run()
                            {
                                try {
                                    tree.setRedraw(false);
                                    select(item, regKey);
                                }
                                finally {
                                    tree.setRedraw(true);
                                    tree.update();
                                }
                            }
                        });
                    }
                });
            }
        }
    }
 
    protected void okPressed()
    {
        if(mRegKey == null) {
            cRootKey = 0;
            cSubKey = null;
        }
        else {
            RegistryKey rootKey = mRegKey.getRootKey();
            if(rootKey != null) {
                cRootKey = rootKey.getHandle();
                String fullName = mRegKey.toString();
                int n = fullName.indexOf('\\');
                if(n > 0) {
                    cSubKey = fullName.substring(n+1);
                }
                else {
                    cSubKey = null;
                }
            }
            else {
                cRootKey = 0;
                cSubKey = null;
            }
        }
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

    private static final RegistryKey[] EMPTY_ARRAY = new RegistryKey[0];
    
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
            setName(name);
        }
        
        protected void setName(String name)
        {
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

        public int getHandle()
        {
            return mHandle;
        }

        public RegistryKey getParent()
        {
            return mParent;
        }
        
        public int find(String name)
        {
            if(mChildCount < 0) {
                open();
            }
            return Arrays.binarySearch(mChildren,name,cSearchComparator);
        }

        public void open()
        {
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
                        }
                        Arrays.sort(mChildren,cSearchComparator);
                    }
                    else {
                        mChildren = EMPTY_ARRAY;
                    }
                }
                else {
                    mChildCount = 0;
                    mChildren = EMPTY_ARRAY;
                }
            }
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
        
        public RegistryKey getRootKey()
        {
            if(mParent instanceof RegistryRoot) {
                return this;
            }
            else {
                return (mParent==null?null:mParent.getRootKey());
            }
        }
    }
    
    private class RegistryRoot extends RegistryKey
    {
        public RegistryRoot()
        {
            super(null, -1,null);
            String name = null;
            try {
                name = WinAPI.RegQueryStrValue(WinAPI.HKEY_CLASSES_ROOT,"CLSID\\{20D04FE0-3AEA-1069-A2D8-08002B30309D}","LocalizedString");
                if(Common.isEmpty(name)) {
                    name = WinAPI.RegQueryStrValue(WinAPI.HKEY_CLASSES_ROOT,"CLSID\\{20D04FE0-3AEA-1069-A2D8-08002B30309D}","");
                }
                if(!Common.isEmpty(name)) {
                    if(name.charAt(0)=='@') {
                        int n = name.lastIndexOf(',');
                        String library;
                        int id = -1;
                        if(n > 0) {
                            library = name.substring(1,n);
                            try {
                                id = Math.abs(Integer.parseInt(name.substring(n+1)));
                            }
                            catch(NumberFormatException nfe) {
                                id = -1;
                            }
                        }
                        else {
                            library = name.substring(1);
                            id = 0;
                        }
                        if(id >= 0) {
                            String resourceString;
                            try {
                                resourceString = WinAPI.LoadResourceString(library, id);
                            }
                            catch(Exception ex) {
                                resourceString = null;
                            }
                            if(!Common.isEmpty(resourceString)) {
                                name = resourceString;
                            }
                        }
                    }
                }
            }
            catch(Exception ex) {
                name = null;
            }
            finally {
                if(Common.isEmpty(name)) {
                    name = EclipseNSISPlugin.getResourceString("regkey.dialog.regroot.label"); //$NON-NLS-1$
                }
            }
            setName(name);
            mChildren = new RegistryKey[ROOT_KEYS.length];
            for (int i = 0; i < mChildren.length; i++) {
                mChildren[i] = new RegistryKey(this, ROOT_KEYS[i], getRootKeyName(ROOT_KEYS[i]));
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
