/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISWizardTemplateDialog;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SaveAsDialog;

public class NSISWizardCompletionPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardCompletion"; //$NON-NLS-1$
    
    private static final int PROGRAM_FILE_CHECK=1;
    private static final int README_FILE_CHECK=2;
    private static final int SAVE_PATH_CHECK=4;
    private static final int ALL_CHECK=PROGRAM_FILE_CHECK|README_FILE_CHECK|SAVE_PATH_CHECK;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardCompletionPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.completion.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.completion.description")); //$NON-NLS-1$
    }

    private boolean validateField(int flag)
    {
        if(validatePage(flag)) {
            return validatePage(ALL_CHECK & ~flag);
        }
        else {
            return false;
        }
    }
    
    private boolean validateSavePath()
    {
        String pathname = mWizard.getSettings().getSavePath();
        if(Common.isEmpty(pathname)) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("empty.save.location.error")); //$NON-NLS-1$
            return false;
        }
        else if(!Path.EMPTY.isValidPath(pathname)) {
            setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.save.location.error"),new String[]{pathname})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    public boolean validatePage(int flag)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        boolean b = (((flag & PROGRAM_FILE_CHECK) == 0) || validateNSISPath(settings.getRunProgramAfterInstall())&&
                     ((flag & README_FILE_CHECK) == 0) || validateNSISPath(settings.getOpenReadmeAfterInstall())&&
                     ((flag & SAVE_PATH_CHECK) == 0) || validateSavePath());
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
        }
        return b;
    }

    /**
     * @return
     */
    private boolean validateNSISPath(String pathname)
    {
        boolean b = Common.isEmpty(pathname) || Common.isValidNSISPathName(pathname);
        if(!b) {
            setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.nsis.pathname.error"),new String[]{pathname})); //$NON-NLS-1$
        }
        return b;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
    
        createMiscInstallerSettingsGroup(composite);
        createPostInstallationActionsGroup(composite);
        createMiscUninstallerSettingsGroup(composite);
        createScriptSaveSettingsGroup(composite);
        Composite composite2 = new Composite(composite, SWT.NONE);
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginWidth = 0;
        composite2.setLayout(layout2);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);

        composite2.setLayoutData(data);
        
        final ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Label l3 = NSISWizardDialogUtil.createLabel(composite2,"save.wizard.template.label", true, null, false); //$NON-NLS-1$
        data = (GridData)l3.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
        data.grabExcessHorizontalSpace = false;
        
        final Button button = new Button(composite2, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("save.wizard.template.button.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                NSISWizardTemplateDialog dialog = new NSISWizardTemplateDialog(getShell(),NSISWizardTemplateDialog.MODE_SAVE);
                dialog.setTemplateName(mWizard.getTemplateName());
                if(dialog.open() == Window.OK) {
                    String templateName = dialog.getTemplateName();
                    if(!Common.isEmpty(templateName)) {
                        try {
                            mWizard.saveTemplate(templateName);
                        }
                        catch(IOException ioe) {
                            MessageDialog.openError(getShell(),bundle.getString("error.title"),ioe.toString()); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        button.setLayoutData(data);

        validatePage(ALL_CHECK);
    }

    /**
     * @param composite
     */
    private void createMiscInstallerSettingsGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "miscellaneous.installer.settings.group.label",null,false); //$NON-NLS-1$

        final Button b1 = NSISWizardDialogUtil.createCheckBox(group, "show.installer.details.label", //$NON-NLS-1$
                              settings.isShowInstDetails(),(settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        b1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setShowInstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "autoclose.installer.label", //$NON-NLS-1$
                settings.isAutoCloseInstaller(),(settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setAutoCloseInstaller(((Button)e.widget).getSelection());
            }
        });
        
        final Button b3 = NSISWizardDialogUtil.createCheckBox(group, "uninstaller.shortcut.startmenu.label", //$NON-NLS-1$
                                        settings.isCreateUninstallerStartMenuShortcut(),
                                        settings.isCreateUninstaller(), null, false);
        b3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCreateUninstallerStartMenuShortcut(((Button)e.widget).getSelection());
            }
        });
        
        final Button b4 = NSISWizardDialogUtil.createCheckBox(group, "uninstaller.entry.control.panel.label", //$NON-NLS-1$
                                        settings.isCreateUninstallerControlPanelEntry(),
                                        settings.isCreateUninstaller(), null, false);
        b4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCreateUninstallerControlPanelEntry(((Button)e.widget).getSelection());
            }
        });

        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                boolean b = settings.getInstallerType() != INSTALLER_TYPE_SILENT;
                b1.setEnabled(b);
                b2.setEnabled(b);
                b = settings.isCreateUninstaller();
                b3.setEnabled(b);
                b4.setEnabled(b);
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                b1.setSelection(settings.isShowInstDetails());
                b1.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);
                b2.setSelection(settings.isAutoCloseInstaller());
                b2.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);
                b3.setSelection(settings.isCreateUninstallerStartMenuShortcut());
                b3.setEnabled(settings.isCreateUninstaller());
                b4.setSelection(settings.isCreateUninstallerControlPanelEntry());
                b4.setEnabled(settings.isCreateUninstaller());
            }});
    }

    /**
     * @param composite
     */
    private void createPostInstallationActionsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Group group = NSISWizardDialogUtil.createGroup(parent, 3, "post.installation.actions.group.label",null,false); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();

        final Combo c1 = NSISWizardDialogUtil.createContentBrowser(group, "run.program.label", settings.getRunProgramAfterInstall(), mWizard, true, null, false); //$NON-NLS-1$
        final Text t = NSISWizardDialogUtil.createText(group,settings.getRunProgramAfterInstallParams(),"run.params.label",true,null,false); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setRunProgramAfterInstallParams(((Text)e.widget).getText());
            }
        });

        Label l = (Label)t.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            ((GridData)l.getLayoutData()).horizontalIndent=8;
        }
        
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setRunProgramAfterInstall(c1.getText());
                boolean b = validatePage(PROGRAM_FILE_CHECK);
                t.setEnabled(b);
                if(b) {
                    validatePage(ALL_CHECK & ~PROGRAM_FILE_CHECK);
                }
            }
        });
        
        final Combo c2 = NSISWizardDialogUtil.createContentBrowser(group, "open.readme.label", settings.getOpenReadmeAfterInstall(), mWizard, true, null, false); //$NON-NLS-1$
        c2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setOpenReadmeAfterInstall(c2.getText());
                validateField(README_FILE_CHECK);
            }
        });


        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                c1.setText(settings.getRunProgramAfterInstall());
                t.setText(settings.getRunProgramAfterInstallParams());
                c2.setText(settings.getOpenReadmeAfterInstall());
            }});
    }

    /**
     * @param composite
     */
    private void createScriptSaveSettingsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Group group = NSISWizardDialogUtil.createGroup(parent, 3, "script.save.settings.group.label",null,true); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();

        final Text t = NSISWizardDialogUtil.createText(group, settings.getSavePath().toString(),"workbench.save.location.label",true,null,true); //$NON-NLS-1$
        ((GridData)t.getLayoutData()).horizontalSpan = 1;
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setSavePath(((Text)e.widget).getText());
                validateField(SAVE_PATH_CHECK);
            }
        });
        
        Button b = new Button(group,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        b.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                SaveAsDialog dialog = new SaveAsDialog(getShell());
                String savePath = mWizard.getSettings().getSavePath();
                if(Common.isEmpty(savePath)) {
                    savePath = EclipseNSISPlugin.getResourceString("default.save.name"); //$NON-NLS-1$
                }
                IPath path = new Path(savePath);
                if(path.isAbsolute()) {
                    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                    dialog.setOriginalFile(file);
                }
                else {
                    dialog.setOriginalName(path.toString());
                }
                dialog.setTitle(EclipseNSISPlugin.getResourceString("save.location.title")); //$NON-NLS-1$
                dialog.create();
                dialog.setMessage(EclipseNSISPlugin.getResourceString("save.location.message")); //$NON-NLS-1$
                int returnCode = dialog.open();
                if(returnCode == Window.OK) {
                    t.setText(dialog.getResult().toString());
                }
            }
        });
        
        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "make.paths.relative.label", //$NON-NLS-1$
                settings.isMakePathsRelative(),true, null, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMakePathsRelative(((Button)e.widget).getSelection());
            }
        });
        
        final Button b3 = NSISWizardDialogUtil.createCheckBox(group, "compile.label", //$NON-NLS-1$
                                        settings.isCompileScript(),
                                        true, null, false);
        ((GridData)b3.getLayoutData()).horizontalSpan = 1;
        b3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCompileScript(((Button)e.widget).getSelection());
            }
        });

        MasterSlaveController m = new MasterSlaveController(b3);
        final Button b4 = NSISWizardDialogUtil.createCheckBox(group, "test.label", //$NON-NLS-1$
                settings.isTestScript(),
                b3.getSelection(), m, false);
        ((GridData)b4.getLayoutData()).horizontalSpan = 2;
        b4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setTestScript(((Button)e.widget).getSelection());
            }
        });
 
        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                t.setText(settings.getSavePath().toString());
                b2.setSelection(settings.isMakePathsRelative());
                b3.setSelection(settings.isCompileScript());
                b4.setSelection(settings.isTestScript());
            }});
    }

    /**
     * @param composite
     */
    private void createMiscUninstallerSettingsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        NSISWizardSettings settings = mWizard.getSettings();

        final Group group = NSISWizardDialogUtil.createGroup(parent, 1, "miscellaneous.uninstaller.settings.group.label",null,false); //$NON-NLS-1$
        group.setEnabled(settings.isCreateUninstaller());
        
        final Button b = NSISWizardDialogUtil.createCheckBox(group, "silent.uninstaller", //$NON-NLS-1$
                settings.isSilentUninstaller(),true, null, false);
        b.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setSilentUninstaller(((Button)e.widget).getSelection());
            }
        });
        final MasterSlaveController m = new MasterSlaveController(b,true);
        
        final Button b1 = NSISWizardDialogUtil.createCheckBox(group, "show.uninstaller.details.label", //$NON-NLS-1$
                              settings.isShowUninstDetails(),true, m, false);
        b1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setShowUninstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "autoclose.uninstaller.label", //$NON-NLS-1$
                settings.isAutoCloseUninstaller(),true, m, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setAutoCloseUninstaller(((Button)e.widget).getSelection());
            }
        });
        m.updateSlaves();
        
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                group.setEnabled(mWizard.getSettings().isCreateUninstaller());
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                group.setEnabled(settings.isCreateUninstaller());
                b.setSelection(settings.isSilentUninstaller());
                b1.setSelection(settings.isShowUninstDetails());
                b2.setSelection(settings.isAutoCloseUninstaller());
                m.updateSlaves();
            }});
    }
}
