/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SaveAsDialog;

public class NSISWizardCompletionPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardCompletion"; //$NON-NLS-1$
    
    private static final int PROGRAM_FILE_CHECK=1;
    private static final int README_FILE_CHECK=2;
    private static final int SAVE_PATH_CHECK=4;
    private static final int ALL_CHECK=PROGRAM_FILE_CHECK|README_FILE_CHECK|SAVE_PATH_CHECK;

    /**
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardCompletionPage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.completion.title"), //$NON-NLS-1$
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
        String pathname = mSettings.getSavePath();
        if(Common.isEmpty(pathname)) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("empty.save.location.error"));
            return false;
        }
        else if(Path.EMPTY.isValidPath(pathname)) {
            setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.save.location.error"),new String[]{pathname})); //$NON-NLS-1$
        }
        return true;
    }

    private boolean validatePage(int flag)
    {
        boolean b = (((flag & PROGRAM_FILE_CHECK) == 0) || validateNSISPath(mSettings.getRunProgramAfterInstall())||
                     ((flag & README_FILE_CHECK) == 0) || validateNSISPath(mSettings.getOpenReadmeAfterInstall())||
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
        boolean b = Common.isEmpty(pathname) || Common.isValidNSISPrefixedPathName(pathname);
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

        validatePage(ALL_CHECK);
    }

    /**
     * @param composite
     */
    private void createMiscInstallerSettingsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "miscellaneous.installer.settings.group.label",null,false); //$NON-NLS-1$

        final Button b1 = NSISWizardDialogUtil.createCheckBox(group, "show.installer.details.label", //$NON-NLS-1$
                              mSettings.isShowInstDetails(),(mSettings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        b1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setShowInstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "autoclose.installer.label", //$NON-NLS-1$
                mSettings.isAutoCloseInstaller(),(mSettings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setAutoCloseInstaller(((Button)e.widget).getSelection());
            }
        });
        
        final Button b3 = NSISWizardDialogUtil.createCheckBox(group, "uninstaller.shortcut.startmenu.label", //$NON-NLS-1$
                                        mSettings.isCreateUninstallerStartMenuShortcut(),
                                        mSettings.isCreateUninstaller(), null, false);
        b3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setCreateUninstallerStartMenuShortcut(((Button)e.widget).getSelection());
            }
        });
        
        final Button b4 = NSISWizardDialogUtil.createCheckBox(group, "uninstaller.entry.control.panel.label", //$NON-NLS-1$
                                        mSettings.isCreateUninstallerControlPanelEntry(),
                                        mSettings.isCreateUninstaller(), null, false);
        b4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setCreateUninstallerControlPanelEntry(((Button)e.widget).getSelection());
            }
        });

        NSISWizard wizard = (NSISWizard)getWizard();
        wizard.addNSISWizardListener(new NSISWizardAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                if(page != null && page.getName().equals(NAME) && forward) {
                    boolean b = mSettings.getInstallerType() != INSTALLER_TYPE_SILENT;
                    b1.setEnabled(b);
                    b2.setEnabled(b);
                    b = mSettings.isCreateUninstaller();
                    b3.setEnabled(b);
                    b4.setEnabled(b);
                }
            }
        });
    }

    /**
     * @param composite
     */
    private void createPostInstallationActionsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Group group = NSISWizardDialogUtil.createGroup(parent, 3, "post.installation.actions.group.label",null,false); //$NON-NLS-1$
        
        final Combo c1 = NSISWizardDialogUtil.createContentBrowser(group, "run.program.label", mSettings.getRunProgramAfterInstall(), mSettings, true, null, false);
        final Text t = NSISWizardDialogUtil.createText(group,mSettings.getRunProgramAfterInstallParams(),"run.params.label",true,null,false);
        Label l = (Label)t.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            ((GridData)l.getLayoutData()).horizontalIndent=8;
        }
        
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mSettings.setRunProgramAfterInstall(c1.getText());
                boolean b = validatePage(PROGRAM_FILE_CHECK);
                t.setEnabled(b);
                if(b) {
                    validatePage(ALL_CHECK & ~PROGRAM_FILE_CHECK);
                }
            }
        });
        
        final Combo c2 = NSISWizardDialogUtil.createContentBrowser(group, "open.readme.label", mSettings.getOpenReadmeAfterInstall(), mSettings, true, null, false);
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mSettings.setOpenReadmeAfterInstall(c2.getText());
                validateField(README_FILE_CHECK);
            }
        });
    }

    /**
     * @param composite
     */
    private void createScriptSaveSettingsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Group group = NSISWizardDialogUtil.createGroup(parent, 3, "script.save.settings.group.label",null,true); //$NON-NLS-1$
        
        final Text t = NSISWizardDialogUtil.createText(group, mSettings.getSavePath().toString(),"workbench.save.location.label",true,null,true);
        ((GridData)t.getLayoutData()).horizontalSpan = 1;
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mSettings.setSavePath(((Text)e.widget).getText());
                validateField(SAVE_PATH_CHECK);
            }
         });
        
        Button b = new Button(group,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("browse.text"));
        b.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip"));
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                SaveAsDialog dialog = new SaveAsDialog(getShell());
                String savePath = mSettings.getSavePath();
                dialog.setOriginalName((Common.isEmpty(savePath)?EclipseNSISPlugin.getResourceString("default.save.name"):savePath));
                dialog.setTitle(EclipseNSISPlugin.getResourceString("save.location.title"));
                dialog.create();
                dialog.setMessage(EclipseNSISPlugin.getResourceString("save.location.message"));
                int returnCode = dialog.open();
                if(returnCode == Window.OK) {
                    t.setText(dialog.getResult().toString());
                }
            }
        });
        
        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "make.paths.relative.label", //$NON-NLS-1$
                mSettings.isMakePathsRelative(),true, null, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setMakePathsRelative(((Button)e.widget).getSelection());
            }
        });
        
        final Button b3 = NSISWizardDialogUtil.createCheckBox(group, "compile.label", //$NON-NLS-1$
                                        mSettings.isCompileScript(),
                                        true, null, false);
        b3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setCompileScript(((Button)e.widget).getSelection());
            }
        });
    }

    /**
     * @param composite
     */
    private void createMiscUninstallerSettingsGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Group group = NSISWizardDialogUtil.createGroup(parent, 1, "miscellaneous.uninstaller.settings.group.label",null,false); //$NON-NLS-1$
        group.setEnabled(mSettings.isCreateUninstaller());
        
        final Button b1 = NSISWizardDialogUtil.createCheckBox(group, "show.uninstaller.details.label", //$NON-NLS-1$
                              mSettings.isShowUninstDetails(),true, null, false);
        b1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setShowUninstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "autoclose.uninstaller.label", //$NON-NLS-1$
                mSettings.isAutoCloseUninstaller(),true, null, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setAutoCloseUninstaller(((Button)e.widget).getSelection());
            }
        });

        NSISWizard wizard = (NSISWizard)getWizard();
        wizard.addNSISWizardListener(new NSISWizardAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                if(page != null && page.getName().equals(NAME) && forward) {
                    group.setEnabled(mSettings.isCreateUninstaller());
                }
            }
        });
    }
}
