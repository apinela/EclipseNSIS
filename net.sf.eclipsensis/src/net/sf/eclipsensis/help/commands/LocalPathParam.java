/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class LocalPathParam extends LocalFileParam
{
    public static final String SETTING_PATH = "path"; //$NON-NLS-1$
    
    public LocalPathParam(Node node)
    {
        super(node);
    }

    protected LocalFilesystemObjectParamEditor createLocalFilesystemObjectParamEditor(INSISParamEditor parentEditor)
    {
        return new LocalPathParamEditor(parentEditor);
    }
    
    protected class LocalPathParamEditor extends LocalFilesystemObjectParamEditor
    {
        protected Text mPathText = null;

        public LocalPathParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void clear()
        {
            if(Common.isValid(mPathText)) {
                mPathText.setText(""); //$NON-NLS-1$
            }
            super.clear();
        }

        protected String getPrefixableParamText()
        {
            if(Common.isValid(mPathText)) {
                String path = IOUtility.decodePath(mPathText.getText());
                return IOUtility.encodePath(path);
            }
            return null;
        }

        protected void updateState(boolean state)
        {
            if(Common.isValid(mPathText)) {
                mPathText.setEnabled(state);
                Button b = (Button)mPathText.getData(DATA_BUTTON);
                if(Common.isValid(b)) {
                    b.setEnabled(state);
                }
            }
            super.updateState(state);
        }

        protected String validateLocalFilesystemObjectParam()
        {
            if(Common.isValid(mPathText)) {
                String path = IOUtility.decodePath(mPathText.getText());
                if(path.length() == 0 ) { 
                    if(isAllowBlank()) {
                        return null;
                    }
                    else {
                        return EclipseNSISPlugin.getResourceString("string.param.error"); //$NON-NLS-1$
                    }
                }
                if(IOUtility.isValidPathName(path)) {
                    File file = new File(path);
                    if(file.isAbsolute()) {
                        if(IOUtility.isValidDirectory(file)||IOUtility.isValidFile(file)) {
                            return null;
                        }
                    }
                    else {
                        return null;
                    }
                }
                else if(IOUtility.isValidPathSpec(path)) {
                    File file = new File(path);
                    if(file.isAbsolute()) {
                        if(WinAPI.ValidateWildcard(path)) {
                            return null;
                        }
                    }
                    else {
                        return null;
                    }
                }
                return EclipseNSISPlugin.getResourceString("local.path.param.error"); //$NON-NLS-1$
            }
            return null;
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(Common.isValid(mPathText) && getSettings() != null) {
                getSettings().put(SETTING_PATH, mPathText.getText());
            }
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            if(Common.isValid(mPathText)) {
                mPathText.setText((String)getSettingValue(SETTING_PATH, String.class, "")); //$NON-NLS-1$
            }
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(3,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent.setLayout(layout);
            mPathText = new Text(parent,SWT.BORDER);
            setToolTip(mPathText);
            mPathText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
            
            final Button b = new Button(parent,SWT.ARROW|SWT.DOWN);
            b.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));

            final Menu menu = new Menu(parent.getShell(),SWT.POP_UP);
            MenuItem mi = new MenuItem(menu,SWT.PUSH);
            mi.setText(EclipseNSISPlugin.getResourceString("browse.file.text")); //$NON-NLS-1$
            mi.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    File f = new File(IOUtility.decodePath(mPathText.getText()));

                    FileDialog dialog = new FileDialog(b.getShell(),SWT.OPEN);
                    if(!Common.isEmptyArray(mFilters)) {
                        dialog.setFilterExtensions(mFilters);
                        dialog.setFilterNames(mFilterNames);
                    }
                    if(IOUtility.isValidFile(f)) {
                        dialog.setFileName(f.getAbsolutePath());
                    }
                    else if(IOUtility.isValidDirectory(f)) {
                        dialog.setFilterPath(f.getAbsolutePath());
                    }
                    String path = dialog.open();
                    if(path != null) {
                        mPathText.setText(IOUtility.encodePath(path));
                    }
                }
            });

            mi = new MenuItem(menu,SWT.PUSH);
            mi.setText(EclipseNSISPlugin.getResourceString("browse.dir.text")); //$NON-NLS-1$
            mi.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    File f = new File(IOUtility.decodePath(mPathText.getText()));
                    DirectoryDialog dialog = new DirectoryDialog(b.getShell());
                    if(IOUtility.isValidDirectory(f)) {
                        dialog.setFilterPath(f.getAbsolutePath());
                    }
                    else if(IOUtility.isValidFile(f)) {
                        dialog.setFilterPath(f.getParentFile().getAbsolutePath());
                    }
                    String path = dialog.open();
                    if(path != null) {
                        mPathText.setText(IOUtility.encodePath(path));
                    }
                }
            });
            
            b.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if(menu.isVisible()) {
                        menu.setVisible(false);
                    }
                    else {
                        Rectangle rect = b.getBounds();
                        Point pt = new Point (rect.x, rect.y + rect.height);
                        pt = b.getParent().toDisplay (pt);
                        menu.setLocation(pt.x, pt.y);
                        menu.setVisible(true);
                    }
                }
            });
            
            mPathText.setData(DATA_BUTTON, b);
            return parent;
        }
    }
}
