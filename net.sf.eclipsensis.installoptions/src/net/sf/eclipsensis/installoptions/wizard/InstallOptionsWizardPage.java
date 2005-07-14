/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

public class InstallOptionsWizardPage extends WizardNewFileCreationPage 
{
    private String[] mEditorIds = {IInstallOptionsConstants.INSTALLOPTIONS_DESIGN_EDITOR_ID,
                                   IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID};
	private IWorkbench	mWorkbench;
	
	// widgets
	private Button  mOpenFileCheckbox;
    private Combo mEditorIdCombo;
    
    /**
     * Creates the page for the readme creation wizard.
     *
     * @param workbench  the workbench on which the page should be created
     * @param selection  the current selection
     */
    public InstallOptionsWizardPage(IWorkbench workbench, IStructuredSelection selection) 
    {
    	super("installOptionsWizardPage", selection); //$NON-NLS-1$
    	this.setTitle(InstallOptionsPlugin.getResourceString("wizard.page.title")); //$NON-NLS-1$
    	this.setDescription(InstallOptionsPlugin.getResourceString("wizard.page.description")); //$NON-NLS-1$
    	mWorkbench = workbench;
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) 
    {
    	// inherit default container and name specification widgets
    	super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),IInstallOptionsConstants.PLUGIN_CONTEXT_PREFIX+"installoptions_wizard_context");
        
    	Composite composite = new Composite((Composite)getControl(),SWT.NULL);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
    	
    	this.setFileName(InstallOptionsPlugin.getResourceString("wizard.default.file.name")); //$NON-NLS-1$ //$NON-NLS-2$
    
    	// open file for editing checkbox
    	mOpenFileCheckbox = new Button(composite,SWT.CHECK);
    	mOpenFileCheckbox.setText(InstallOptionsPlugin.getResourceString("wizard.open.file.label")); //$NON-NLS-1$
    	mOpenFileCheckbox.setSelection(true);
        mOpenFileCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
        mEditorIdCombo = new Combo(composite,SWT.DROP_DOWN|SWT.READ_ONLY);
        mEditorIdCombo.add(InstallOptionsPlugin.getResourceString("wizard.design.editor.label")); //$NON-NLS-1$
        mEditorIdCombo.add(InstallOptionsPlugin.getResourceString("wizard.source.editor.label")); //$NON-NLS-1$
        mEditorIdCombo.select(0);
        
        MasterSlaveController msc = new MasterSlaveController(mOpenFileCheckbox);
        msc.addSlave(mEditorIdCombo);
        
    	setPageComplete(validatePage());
    	
    }

    /**
     * Creates a new file resource as requested by the user. If everything
     * is OK then answer true. If not, false will cause the dialog
     * to stay open.
     *
     * @return whether creation was successful
     * @see InstallOptionsWizard#performFinish()
     */
    public boolean finish() 
    {
    	// create the new file resource
    	IFile newFile = createNewFile();
    	if (newFile == null) {
    		return false;	// ie.- creation was unsuccessful
        }
    
    	// Since the file resource was created fine, open it for editing
    	// if requested by the user
    	try {
    		if (mOpenFileCheckbox.getSelection()) {
                String editorId = mEditorIds[mEditorIdCombo.getSelectionIndex()];
                try {
                    newFile.setPersistentProperty(IDE.EDITOR_KEY,editorId);
                }
                catch (CoreException e1) {
                    e1.printStackTrace();
                }
                
    			IWorkbenchWindow dwindow = mWorkbench.getActiveWorkbenchWindow();
    			IWorkbenchPage page = dwindow.getActivePage();
    			if (page != null) {
    				IDE.openEditor(page, newFile, true);
    			}
    		}
    	} 
        catch (PartInitException e) {
    		e.printStackTrace();
    		return false;
    	}
    	return true;
    }
    
    protected InputStream getInitialContents() 
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append("; ").append(InstallOptionsPlugin.getResourceString("wizard.file.header.comment")); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(INSISConstants.LINE_SEPARATOR);
        sb.append("; ").append(DateFormat.getDateTimeInstance().format(new Date())); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(INSISConstants.LINE_SEPARATOR).append(INSISConstants.LINE_SEPARATOR);
        sb.append("[").append(InstallOptionsModel.SECTION_SETTINGS).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(INSISConstants.LINE_SEPARATOR);
        sb.append(InstallOptionsModel.PROPERTY_NUMFIELDS).append("=0").append(INSISConstants.LINE_SEPARATOR); //$NON-NLS-1$

    	return new ByteArrayInputStream(sb.toString().getBytes());
    }

    /** (non-Javadoc)
     * Method declared on WizardNewFileCreationPage.
     */
    protected String getNewFileLabel() 
    {
    	return InstallOptionsPlugin.getResourceString("wizard.file.name.label"); //$NON-NLS-1$
    }
}
