/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SaveAsDialog;

public class NSISWizardCompletionPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardCompletion"; //$NON-NLS-1$

    private static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    private static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.filtername")}; //$NON-NLS-1$
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

    protected boolean hasRequiredFields()
    {
        return isScriptWizard();
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
        else if(Path.EMPTY.isValidPath(pathname)) {
            IPath path = new Path(pathname);
            path = path.removeLastSegments(1);
            if(mWizard.getSettings().isSaveExternal()) {
                File file = new File(path.toOSString());
                if(IOUtility.isValidDirectory(file)) {
                    return true;
                }
            }
            else {
                IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                if(resource != null && (resource instanceof IFolder || resource instanceof IProject)) {
                    return true;
                }
            }
        }
        setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.save.location.error",new String[]{pathname})); //$NON-NLS-1$
        return false;
    }

    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();

            boolean b = ((flag & PROGRAM_FILE_CHECK) == 0 || validateNSISPath(settings.getRunProgramAfterInstall()))&&
                        ((flag & README_FILE_CHECK) == 0 || validateNSISPath(settings.getOpenReadmeAfterInstall()))&&
                        ((flag & SAVE_PATH_CHECK) == 0 || validateSavePath());
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    /**
     * @return
     */
    private boolean validateNSISPath(String pathname)
    {
        boolean b = Common.isEmpty(pathname) || IOUtility.isValidNSISPathName(pathname);
        if(!b) {
            setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.nsis.pathname.error",new String[]{pathname})); //$NON-NLS-1$
        }
        return b;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizcomplete_context"; //$NON-NLS-1$
    }

    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        createMiscInstallerSettingsGroup(composite);
        createPostInstallationActionsGroup(composite);
        createMiscUninstallerSettingsGroup(composite);
        createScriptSaveSettingsGroup(composite);

        validatePage(ALL_CHECK);

        return composite;
    }

    /**
     * @param composite
     */
    private void createMiscInstallerSettingsGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "miscellaneous.installer.settings.group.label",null,false); //$NON-NLS-1$
        ((GridLayout)group.getLayout()).makeColumnsEqualWidth = true;
        final Button b1 = NSISWizardDialogUtil.createCheckBox(group, "show.installer.details.label", //$NON-NLS-1$
                              settings.isShowInstDetails(),(settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        GridData data = (GridData)b1.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        b1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setShowInstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "autoclose.installer.label", //$NON-NLS-1$
                settings.isAutoCloseInstaller(),(settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        data = (GridData)b2.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
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

        addPageChangedRunnable(new Runnable() {
            public void run()
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
        final Group group = NSISWizardDialogUtil.createGroup(parent, 3, "post.installation.actions.group.label",null,false); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();

        final Combo c1 = NSISWizardDialogUtil.createContentBrowser(group, "run.program.label", settings.getRunProgramAfterInstall(), mWizard, true, null, false); //$NON-NLS-1$
        final Text t = NSISWizardDialogUtil.createText(group,settings.getRunProgramAfterInstallParams(),"run.params.label",true,null,false); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setRunProgramAfterInstallParams(((Text)e.widget).getText());
                validateField(PROGRAM_FILE_CHECK);
            }
        });

        Label l = (Label)t.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            ((GridData)NSISWizardDialogUtil.getLayoutControl(l).getLayoutData()).horizontalIndent=8;
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
        final Group group = NSISWizardDialogUtil.createGroup(parent, 1, "script.save.settings.group.label",null,true); //$NON-NLS-1$
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        GridLayout layout = new GridLayout(1,false);
        group.setLayout(layout);

        Composite c = new Composite(group,SWT.None);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        final Button[] radioButtons = NSISWizardDialogUtil.createRadioGroup(c,new String[] {EclipseNSISPlugin.getResourceString("workspace.save.label"),
                                                              EclipseNSISPlugin.getResourceString("filesystem.save.label")},
                                              mWizard.getSettings().isSaveExternal()?1:0,"save.label",true,null,false);
        final Text t = NSISWizardDialogUtil.createText(c, mWizard.getSettings().getSavePath().toString(),"save.location.label",true,null,isScriptWizard()); //$NON-NLS-1$
        ((GridData)t.getLayoutData()).horizontalSpan = 1;
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setSavePath(((Text)e.widget).getText());
                validateField(SAVE_PATH_CHECK);
                if(isScriptWizard()) {
                    ((NSISScriptWizard)mWizard).setCheckOverwrite(mWizard.getSettings().getSavePath().length() > 0);
                }
            }
        });
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                boolean saveExternal = radioButtons[1].getSelection();
                if(saveExternal != mWizard.getSettings().isSaveExternal()) {
                    mWizard.getSettings().setSaveExternal(saveExternal);
                    t.setText("");
                }
            }
        };
        radioButtons[0].addSelectionListener(selectionAdapter);
        radioButtons[1].addSelectionListener(selectionAdapter);

        Button b = new Button(c,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        b.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String savePath = mWizard.getSettings().getSavePath();
                if(Common.isEmpty(savePath)) {
                    savePath = EclipseNSISPlugin.getResourceString("default.save.name"); //$NON-NLS-1$
                }
                if(mWizard.getSettings().isSaveExternal()) {
                    FileDialog dialog = new FileDialog(getShell(),SWT.SAVE);
                    dialog.setFileName(savePath);
                    dialog.setFilterExtensions(FILTER_EXTENSIONS);
                    dialog.setFilterNames(FILTER_NAMES);
                    dialog.setText(EclipseNSISPlugin.getResourceString("save.location.title")); //$NON-NLS-1$
                    savePath = dialog.open();
                    if(savePath != null) {
                        t.setText(savePath);
                    }
                }
                else {
                    SaveAsDialog dialog = new SaveAsDialog(getShell());
                    IPath path = new Path(savePath);
                    if(path.isAbsolute()) {
                        try {
                            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                            dialog.setOriginalFile(file);
                        }
                        catch (Exception e1) {
                        }
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
                        if(isScriptWizard()) {
                            ((NSISScriptWizard)mWizard).setCheckOverwrite(false);
                        }
                    }
                }
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "make.paths.relative.label", //$NON-NLS-1$
                mWizard.getSettings().isMakePathsRelative(),true, null, false);
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMakePathsRelative(((Button)e.widget).getSelection());
            }
        });

        c = new Composite(group,SWT.None);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        layout = new GridLayout(2,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        final Button b3 = NSISWizardDialogUtil.createCheckBox(c, "compile.label", //$NON-NLS-1$
                                        mWizard.getSettings().isCompileScript(),
                                        true, null, false);
        ((GridData)b3.getLayoutData()).horizontalSpan = 1;
        b3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCompileScript(((Button)e.widget).getSelection());
            }
        });

        MasterSlaveController m = new MasterSlaveController(b3);
        final Button b4 = NSISWizardDialogUtil.createCheckBox(c, "test.label", //$NON-NLS-1$
                mWizard.getSettings().isTestScript(),
                b3.getSelection(), m, false);
        ((GridData)b4.getLayoutData()).horizontalSpan = 1;
        b4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setTestScript(((Button)e.widget).getSelection());
            }
        });

        if(isScriptWizard()) {
            final NSISScriptWizard scriptWizard = (NSISScriptWizard)mWizard;
            final Button button = NSISWizardDialogUtil.createCheckBox(group,"save.wizard.template.label",scriptWizard.isSaveAsTemplate(),true,null,false); //$NON-NLS-1$
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    scriptWizard.setSaveAsTemplate(button.getSelection());
                }
            });
            scriptWizard.addSettingsListener(new INSISWizardSettingsListener() {
                public void settingsChanged()
                {
                    scriptWizard.setSaveAsTemplate(false);
                    button.setSelection(false);
                }
            });
        }

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();

                t.setText(settings.getSavePath().toString());
                radioButtons[0].setSelection(!settings.isSaveExternal());
                radioButtons[1].setSelection(settings.isSaveExternal());
                b2.setSelection(settings.isMakePathsRelative());
                b3.setSelection(settings.isCompileScript());
                b4.setSelection(settings.isTestScript());
            }
       });
    }

    /**
     * @param composite
     */
    private void createMiscUninstallerSettingsGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        final Group group = NSISWizardDialogUtil.createGroup(parent, 2, "miscellaneous.uninstaller.settings.group.label",null,false); //$NON-NLS-1$
        ((GridLayout)group.getLayout()).makeColumnsEqualWidth = true;
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
        GridData data = (GridData)b1.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        b1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setShowUninstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "autoclose.uninstaller.label", //$NON-NLS-1$
                settings.isAutoCloseUninstaller(),true, m, false);
        data = (GridData)b2.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        b2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setAutoCloseUninstaller(((Button)e.widget).getSelection());
            }
        });
        m.updateSlaves();

        addPageChangedRunnable(new Runnable() {
            public void run()
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
