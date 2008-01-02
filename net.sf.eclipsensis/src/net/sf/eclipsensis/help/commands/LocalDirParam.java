/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
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
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class LocalDirParam extends LocalFilesystemObjectParam
{
    public static final String SETTING_DIR = "dir"; //$NON-NLS-1$

    public LocalDirParam(Node node)
    {
        super(node);
    }

    protected LocalFilesystemObjectParamEditor createLocalFilesystemObjectParamEditor(INSISParamEditor parentEditor)
    {
        return new LocalDirParamEditor(parentEditor);
    }

    protected class LocalDirParamEditor extends LocalFilesystemObjectParamEditor
    {
        protected Text mDirText = null;

        public LocalDirParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void clear()
        {
            if(Common.isValid(mDirText)) {
                mDirText.setText(""); //$NON-NLS-1$
            }
            super.clear();
        }

        protected String validateLocalFilesystemObjectParam()
        {
            if(Common.isValid(mDirText)) {
                String dir = IOUtility.decodePath(mDirText.getText());
                if(dir.length() == 0 ) {
                    if(isAllowBlank()) {
                        return null;
                    }
                    else {
                        return EclipseNSISPlugin.getResourceString("string.param.error"); //$NON-NLS-1$
                    }
                }
                if(IOUtility.isValidPathName(dir)) {
                    if(IOUtility.isValidDirectory(new File(dir))) {
                        return null;
                    }
                }
                return EclipseNSISPlugin.getResourceString("local.dir.param.error"); //$NON-NLS-1$
            }
            return null;
        }

        protected String getPrefixableParamText()
        {
            if(Common.isValid(mDirText)) {
                String dir = IOUtility.decodePath(mDirText.getText());
                return IOUtility.encodePath(dir);
            }
            return null;
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(Common.isValid(mDirText) && getSettings() != null) {
                getSettings().put(SETTING_DIR, mDirText.getText());
            }
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            if(Common.isValid(mDirText)) {
                mDirText.setText((String)getSettingValue(SETTING_DIR, String.class, "")); //$NON-NLS-1$
            }
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent.setLayout(layout);
            mDirText = new Text(parent,SWT.BORDER);
            setToolTip(mDirText);
            mDirText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
            final Button b = new Button(parent,SWT.PUSH);
            b.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    DirectoryDialog dialog = new DirectoryDialog(b.getShell());
                    String dir = IOUtility.decodePath(mDirText.getText());
                    if(!Common.isEmpty(dir)) {
                        dialog.setFilterPath(dir);
                    }
                    dir = dialog.open();
                    if(dir != null) {
                        mDirText.setText(IOUtility.encodePath(dir));
                    }
                }
            });
            return parent;
        }
    }
}
