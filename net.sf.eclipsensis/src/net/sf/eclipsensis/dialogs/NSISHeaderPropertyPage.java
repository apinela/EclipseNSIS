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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.NSISHeaderAssociationManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class NSISHeaderPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
    private static final IFilter IFILE_FILTER = new IFilter() {
        public boolean select(Object toTest)
        {
            if(toTest instanceof IFile) {
                String ext = ((IFile)toTest).getFileExtension();
                return (ext != null && ext.equalsIgnoreCase(INSISConstants.NSI_EXTENSION));
            }
            return false;
        }
    };

    private NSISHeaderAssociationManager mHeaderAssociationManager = NSISHeaderAssociationManager.getInstance();
    private Label mAssociatedScriptLabel = null;
    private Text mNSISScriptName = null;
    private Button mBrowseButton = null;

    public NSISHeaderPropertyPage()
    {
    }

    protected Control createContents(Composite parent)
    {
        setDescription(EclipseNSISPlugin.getResourceString("header.properties.description")); //$NON-NLS-1$

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        composite.setLayout(layout);

        mAssociatedScriptLabel = new Label(composite,SWT.NONE);
        mAssociatedScriptLabel.setText(EclipseNSISPlugin.getResourceString("nsis.script.label")); //$NON-NLS-1$
        mAssociatedScriptLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));

        mNSISScriptName = new Text(composite, SWT.BORDER); //$NON-NLS-1$
        IFile file = mHeaderAssociationManager.getAssociatedScript((IFile)getElement());
        mNSISScriptName.setText(file==null?"":file.getFullPath().toString()); //$NON-NLS-1$
        mNSISScriptName.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        mBrowseButton = new Button(composite,SWT.PUSH);
        mBrowseButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        mBrowseButton.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        mBrowseButton.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        mBrowseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                String scriptName = mNSISScriptName.getText();
                IFile file = null;
                try {
                    file = (scriptName != null?ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(scriptName)):null);
                }
                catch (Exception e1) {
                    EclipseNSISPlugin.getDefault().log(e1);
                    file = null;
                }
                while (true) {
                    FileSelectionDialog dialog = new FileSelectionDialog(getShell(), file, IFILE_FILTER);
                    dialog.setDialogMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
                    dialog.setHelpAvailable(false);
                    if (dialog.open() == Window.OK) {
                        if(!validateScript(dialog.getFile())) {
                            continue;
                        }
                        mNSISScriptName.setText(dialog.getFile().getFullPath().toString()); //$NON-NLS-1$
                    }
                    break;
                }
            }
        });
        //TODO Add contextual help
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(control,getContextId());
        return composite;
    }

    protected void performDefaults()
    {
        super.performDefaults();
        mNSISScriptName.setText(""); //$NON-NLS-1$
    }

    private boolean validateScript(IFile file)
    {
        if (file != null) {
            if (Common.stringsAreEqual(INSISConstants.NSI_EXTENSION, file.getFileExtension(), true)) {
                return true;
            }
        }
        Common.openError(getShell(), EclipseNSISPlugin.getResourceString("not.valid.script.error"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
        return false;
    }

    public boolean performOk()
    {
        if(super.performOk()) {
            String nsisScriptName = mNSISScriptName.getText();
            IFile header = (IFile)getElement();
            if(Common.isEmpty(nsisScriptName)) {
                mHeaderAssociationManager.disassociateFromScript(header);
            }
            else {
                IPath path = new Path(nsisScriptName);
                if(!path.isAbsolute()) {
                    Common.openError(getShell(), EclipseNSISPlugin.getResourceString("not.valid.script.error"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                    return false;
                }
                IFile associatedScript = null;
                if(validateScript(associatedScript = ResourcesPlugin.getWorkspace().getRoot().getFile(path))) {
                    mHeaderAssociationManager.associateWithScript(header, associatedScript);
                    return true;
                }
            }
        }
        return false;
    }
}
