/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class RegistryKeyBrowser extends Composite
{
    private static final int[] ROOT_KEYS = {WinAPI.HKEY_CLASSES_ROOT, WinAPI.HKEY_CURRENT_USER, WinAPI.HKEY_LOCAL_MACHINE, WinAPI.HKEY_USERS, WinAPI.HKEY_CURRENT_CONFIG};

    private static final Image REGKEY_IMAGE;
    private static final Image OPEN_REGKEY_IMAGE;
    private static final Image REGROOT_IMAGE;

    private static final Comparator SEARCH_COMPARATOR = new Comparator() {
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

    static {
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        REGKEY_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.key.image")); //$NON-NLS-1$
        OPEN_REGKEY_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.key.open.image")); //$NON-NLS-1$
        REGROOT_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.root.image")); //$NON-NLS-1$
    }

    private static String getRootKeyName(int rootKey)
    {
        switch (rootKey)
        {
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

    private static int getRootKey(String rootKey)
    {
        if (rootKey.equalsIgnoreCase("HKEY_CLASSES_ROOT") || rootKey.equalsIgnoreCase("HKCR")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_CLASSES_ROOT;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_CURRENT_CONFIG") || rootKey.equalsIgnoreCase("HKCC")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_CURRENT_CONFIG;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_CURRENT_USER") || rootKey.equalsIgnoreCase("HKCU")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_CURRENT_USER;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_DYN_DATA") || rootKey.equalsIgnoreCase("HKDD")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_DYN_DATA;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_LOCAL_MACHINE") || rootKey.equalsIgnoreCase("HKLM")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_LOCAL_MACHINE;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_PERFORMANCE_DATA") || rootKey.equalsIgnoreCase("HKPD")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_PERFORMANCE_DATA;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_USERS") || rootKey.equalsIgnoreCase("HKU")) { //$NON-NLS-1$ //$NON-NLS-2$
            return WinAPI.HKEY_USERS;
        }
        else {
            return 0;
        }
    }

    private RegistryRoot mRegistryRoot = new RegistryRoot();
    private String mSelection = null;
    private RegistryKey mRegistryKey = null;
    private Tree mTree = null;

    public RegistryKeyBrowser(Composite parent, int style)
    {
        super(parent, checkStyle(style));
        create();
    }

    static int checkStyle (int style) {
        int mask = SWT.BORDER | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        return style & mask;
    }

    private void create()
    {
        Listener listener = new Listener() {
            public void handleEvent(Event event)
            {
                switch (event.type) {
                    case SWT.Dispose:
                        new Thread(new Runnable() {
                            public void run()
                            {
                                ((RegistryKey)mRegistryRoot).close();
                            }

                        },EclipseNSISPlugin.getResourceString("registry.unloader.thread.name")).start(); //$NON-NLS-1$
                        break;
                    case SWT.Resize:
                        internalLayout (false);
                        break;
                }
            }
        };
        addListener(SWT.Dispose,listener);
        addListener(SWT.Resize,listener);

        int style = getStyle() & ~SWT.BORDER;
        mTree = new Tree(this,SWT.VIRTUAL|style);
        mTree.addListener(SWT.SetData, new Listener() {
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
                    updateSelection();
                }
            }
        });
        mTree.addTreeListener(new TreeListener() {
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
        mTree.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event)
            {
                Event e = new Event ();
                e.time = event.time;
                e.stateMask = event.stateMask;
                notifyListeners (SWT.DefaultSelection, e);
            }

            public void widgetSelected(SelectionEvent event)
            {
                TreeItem item = (TreeItem)event.item;
                saveSelection(item, event);
            }
        });
        mTree.setItemCount(1); // The registry root "My Computer"
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
                saveSelection(childItem, null);
                return true;
            }
        }
        return false;
    }

    private void saveSelection(TreeItem item, SelectionEvent event)
    {
        String oldSelection = mSelection;
        RegistryKey oldRegKey = mRegistryKey;

        mRegistryKey = (RegistryKey)item.getData();
        mSelection = null;
        if(mRegistryKey != null) {
            mSelection = mRegistryKey.toString();
        }
        Event e = new Event ();
        e.time = (event == null?(int)System.currentTimeMillis():event.time);
        e.stateMask = (event == null?0:event.stateMask);
        e.doit = (event == null?true:event.doit);
        notifyListeners (SWT.Selection, e);
        if(event != null) {
            event.doit = e.doit;
        }
        if(!e.doit) {
            mSelection = oldSelection;
            mRegistryKey = oldRegKey;
        }
    }

    public void updateSelection()
    {
        if(mTree != null && mSelection != null) {
            int n = mSelection.indexOf("\\"); //$NON-NLS-1$
            int rootKey;
            String subKey;
            if(n > 0) {
                rootKey = getRootKey(mSelection.substring(0,n));
                subKey = mSelection.substring(n+1);
            }
            else {
                rootKey = getRootKey(mSelection);
                subKey = null;
            }
            if(rootKey != 0) {
                boolean exists = true;
                if(subKey != null) {
                    exists = WinAPI.RegKeyExists(rootKey, subKey);
                }
                if(exists) {
                    final TreeItem item = mTree.getItem(0);
                    final String regKey = (subKey==null?getRootKeyName(rootKey):new StringBuffer(getRootKeyName(rootKey)).append("\\").append(subKey).toString()); //$NON-NLS-1$
                    item.getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            BusyIndicator.showWhile(item.getDisplay(),new Runnable() {
                                public void run()
                                {
                                    try {
                                        mTree.setRedraw(false);
                                        select(item, regKey);
                                    }
                                    finally {
                                        mTree.setRedraw(true);
                                        mTree.update();
                                    }
                                }
                            });
                        }
                    });
                }
                else {
                    mRegistryKey = null;
                    mSelection = null;
                }
            }
        }
    }

    public void redraw ()
    {
        super.redraw();
        mTree.redraw();
    }

    public void setToolTipText (String string)
    {
        checkWidget();
        super.setToolTipText(string);
        mTree.setToolTipText (string);
    }

    public void redraw (int x, int y, int width, int height, boolean all)
    {
        super.redraw(x, y, width, height, true);
    }

    public void setBackground (Color color)
    {
        super.setBackground(color);
        if (mTree != null) {
            mTree.setBackground(color);
        }
    }

    public void setForeground (Color color)
    {
        super.setForeground(color);
        if (mTree != null) {
            mTree.setForeground(color);
        }
    }

    public void setFont (Font font)
    {
        super.setFont(font);
        if (mTree != null) {
            mTree.setFont(font);
        }
        internalLayout (true);
    }

    void internalLayout (boolean changed)
    {
        Rectangle rect = getClientArea();
        int width = rect.width;
        int height = rect.height;
        mTree.setBounds (0, 0, width, height);
    }

    public void setEnabled (boolean enabled)
    {
        super.setEnabled(enabled);
        if (mTree != null) {
            mTree.setVisible (false);
        }
    }

    public boolean setFocus ()
    {
        checkWidget();
        return mTree.setFocus ();
    }

    public void setLayout (Layout layout)
    {
        checkWidget ();
        return;
    }

    public boolean isFocusControl ()
    {
        checkWidget();
        if (mTree.isFocusControl ()) {
            return true;
        }
        return super.isFocusControl ();
    }

    public Control [] getChildren ()
    {
        checkWidget();
        return new Control [0];
    }

    public void addSelectionListener(SelectionListener listener)
    {
        checkWidget();
        if (listener == null) {
            SWT.error (SWT.ERROR_NULL_ARGUMENT);
        }
        TypedListener typedListener = new TypedListener (listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection,typedListener);
    }

    public RegistryKey getSelectedKey()
    {
        return mRegistryKey;
    }

    public String getSelection()
    {
        checkWidget();
        return mSelection;
    }

    public void removeSelectionListener(SelectionListener listener)
    {
        checkWidget();
        if (listener == null) {
            SWT.error (SWT.ERROR_NULL_ARGUMENT);
        }
        removeListener(SWT.Selection, listener);
        removeListener(SWT.DefaultSelection,listener);
    }

    public void select(String regKey)
    {
        checkWidget();
        mRegistryKey = null;
        mSelection = regKey;
        if(mTree != null && !mTree.isDisposed()) {
            updateSelection();
        }
    }

    public void deselect()
    {
        select(null);
    }

    public Point computeSize(int wHint, int hHint, boolean changed)
    {
        checkWidget ();
        Point size = mTree.computeSize(wHint, hHint, changed);
        int borderWidth = getBorderWidth ();
        int height = size.y;
        int width = size.x;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        return new Point (width + 2*borderWidth, height + 2*borderWidth);
    }

    public class RegistryKey
    {
        protected int mHandle = 0;
        protected String mName = ""; //$NON-NLS-1$
        protected int mChildCount = -1;
        protected RegistryKey[] mChildren = null;
        protected RegistryKey mParent = null;
        private String mString = null;
        private Map mAttributes = null;

        private RegistryKey(RegistryKey parent, int handle, String name)
        {
            this(parent, name);
            mHandle = handle;
        }

        public RegistryKey(RegistryKey parent, String name)
        {
            mParent = parent;
            setName(name);
        }

        Object getAttribute(String name)
        {
            if(mAttributes != null) {
                return mAttributes.get(name);
            }
            return null;
        }

        synchronized void setAttribute(String name, Object value)
        {
            if(mAttributes == null) {
                mAttributes = new HashMap();
            }
            mAttributes.put(name, value);
        }

        void setName(String name)
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
            if(mHandle == 0) {
                mHandle = WinAPI.RegOpenKeyEx(getParent().mHandle,getName(),0,WinAPI.KEY_QUERY_VALUE|WinAPI.KEY_ENUMERATE_SUB_KEYS);
            }
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
            return Arrays.binarySearch(mChildren,name,SEARCH_COMPARATOR);
        }

        private void open()
        {
            if(mChildCount < 0) {
                int handle = getHandle();
                if(handle != 0) {
                    int[] sizes = {0,0};
                    WinAPI.RegQueryInfoKey(handle,sizes);
                    mChildCount = sizes[0];
                    if(mChildCount > 0) {
                        mChildren = new RegistryKey[mChildCount];
                        for (int i = 0; i < mChildren.length; i++) {
                            String subKey = WinAPI.RegEnumKeyEx(handle,i,sizes[1]);
                            mChildren[i] = new RegistryKey(this,subKey);
                        }
                        Arrays.sort(mChildren,SEARCH_COMPARATOR);
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

        private void close()
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
            setName(Common.getMyComputerLabel());
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
