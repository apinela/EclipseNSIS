/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.util.ResourceBundle;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardGeneralPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardGeneral"; //$NON-NLS-1$

    private static final int APPNAME_CHECK=0x1;
    private static final int PUBURL_CHECK=0x10;
    private static final int INSTFILE_CHECK=0x100;
    private static final int INSTICON_CHECK=0x1000;
    private static final int UNINSTFILE_CHECK=0x10000;
    private static final int UNINSTICON_CHECK=0x100000;

    private static String[] cInstallFileErrors = {"empty.installer.file.error"}; //$NON-NLS-1$
    private static String[] cUninstallFileErrors = {"empty.uninstaller.file.error"}; //$NON-NLS-1$


    /**
     * @param pageName
     * @param title
     */
    public NSISWizardGeneralPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.general.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.general.description")); //$NON-NLS-1$
    }

    protected boolean hasRequiredFields()
    {
        return isScriptWizard();
    }

    private boolean validateField(int flag)
    {
        if(validatePage(flag)) {
            return validatePage(VALIDATE_ALL & ~flag);
        }
        else {
            return false;
        }
    }

    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();

            boolean b = ((flag & APPNAME_CHECK) == 0 || validateAppName()) &&
                   ((flag & PUBURL_CHECK) == 0 || validateEmptyOrValidURL(settings.getUrl(),null)) &&
                   ((flag & INSTFILE_CHECK) == 0 || validatePathName(IOUtility.decodePath(settings.getOutFile()),cInstallFileErrors)) &&
                   ((flag & INSTICON_CHECK) == 0 || validateEmptyOrValidFile(IOUtility.decodePath(settings.getIcon()),null)) &&
                   ((flag & UNINSTFILE_CHECK) == 0 || !settings.isCreateUninstaller() || validateFileName(IOUtility.decodePath(settings.getUninstallFile()),cUninstallFileErrors)) &&
                   ((flag & UNINSTICON_CHECK) == 0 || !settings.isCreateUninstaller() || validateEmptyOrValidFile(IOUtility.decodePath(settings.getUninstallIcon()),null));
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizgeneral_context"; //$NON-NLS-1$
    }

    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        createApplicationGroup(composite);
        createInstallerGroup(composite);
        createUninstallerGroup(composite);

        validatePage(VALIDATE_ALL);

        return composite;
    }

    private void createApplicationGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "application.group.label", null, true); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();
        final String programFiles =NSISKeywords.getInstance().getKeyword("$PROGRAMFILES"); //$NON-NLS-1$

        final Text t = NSISWizardDialogUtil.createText(group, settings.getName(), "application.name.label", true, null, isScriptWizard()); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               NSISWizardSettings settings = mWizard.getSettings();

               String newName = ((Text)e.widget).getText();
               String installDir = settings.getInstallDir();
               if(Common.isEmpty(installDir) || installDir.equalsIgnoreCase(new StringBuffer(programFiles).append("\\").append(settings.getName()).toString())) { //$NON-NLS-1$
                   settings.setInstallDir(new StringBuffer(programFiles).append("\\").append(newName).toString()); //$NON-NLS-1$
               }
               String startMenuGroup = settings.getStartMenuGroup();
               if(Common.isEmpty(startMenuGroup) || startMenuGroup.equalsIgnoreCase(settings.getName())) {
                   settings.setStartMenuGroup(newName);
               }
               settings.setName(newName);
               validateField(APPNAME_CHECK);
           }
        });

        final Text t2 = NSISWizardDialogUtil.createText(group, settings.getVersion(), "application.version.label", true, null, false); //$NON-NLS-1$
        t2.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setVersion(((Text)e.widget).getText());
           }
        });

        final Text t3 = NSISWizardDialogUtil.createText(group, settings.getCompany(), "publisher.name.label", true, null, false); //$NON-NLS-1$
        t3.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setCompany(((Text)e.widget).getText());
           }
        });

        final Text t4 = NSISWizardDialogUtil.createText(group, settings.getUrl(), "publisher.url.label", true, null, false); //$NON-NLS-1$
        t4.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setUrl(((Text)e.widget).getText());
               validateField(PUBURL_CHECK);
           }
        });

        final Combo c2;
        if(INSISVersions.VERSION_2_26.compareTo(NSISPreferences.INSTANCE.getNSISVersion()) <= 0) {
            c2 = NSISWizardDialogUtil.createCombo(group,NSISWizardDisplayValues.TARGET_PLATFORMS,
            			settings.getTargetPlatform(), true, "target.platform.label", true, null, false);  //$NON-NLS-1$
            c2.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mWizard.getSettings().setTargetPlatform(c2.getSelectionIndex());
                }
            });
        }
        else {
            c2 = null;
            settings.setTargetPlatform(TARGET_PLATFORM_ANY);
        }

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                t.setText(newSettings.getName());
                t2.setText(newSettings.getVersion());
                t3.setText(newSettings.getCompany());
                t4.setText(newSettings.getUrl());


                if(c2 != null && newSettings.getTargetPlatform() < c2.getItemCount()) {
                    c2.select(newSettings.getTargetPlatform());
                }
                else {
                    newSettings.setTargetPlatform(TARGET_PLATFORM_ANY);
                }
            }});
    }

    private void createInstallerGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 3, "installer.group.label", null, true); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Text t = NSISWizardDialogUtil.createFileBrowser(group, settings.getOutFile(), true,
                                   Common.loadArrayProperty(bundle,"installer.file.filternames"),  //$NON-NLS-1$
                                   Common.loadArrayProperty(bundle,"installer.file.filters"), "installer.file.label", //$NON-NLS-1$ //$NON-NLS-2$
                                   true, null,isScriptWizard());
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setOutFile(((Text)e.widget).getText());
               validateField(INSTFILE_CHECK);
           }
        });

        final Text t2 = NSISWizardDialogUtil.createImageBrowser(group, settings.getIcon(), new Point(32,32),
                                          Common.loadArrayProperty(bundle,"installer.icon.filternames"),  //$NON-NLS-1$
                                          Common.loadArrayProperty(bundle,"installer.icon.filters"), "installer.icon.label", //$NON-NLS-1$ //$NON-NLS-2$
                                          true, null, false);
        t2.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setIcon(t2.getText());
               validateField(INSTICON_CHECK);
           }
        });
        NSISWizardDialogUtil.loadImage(t2);

        final Button[] radio = NSISWizardDialogUtil.createRadioGroup(group, NSISWizardDisplayValues.INSTALLER_TYPE_NAMES,
                                           settings.getInstallerType(),"installer.type.label", //$NON-NLS-1$
                                           true, null,false);
        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Button button = (Button)e.widget;
                if(button.getSelection()) {
                    Integer index = (Integer)button.getData();
                    mWizard.getSettings().setInstallerType(index.intValue());
                }
            }
        };
        for (int i = 0; i < radio.length; i++) {
            radio[i].addSelectionListener(sa);
        }

        final Button cb;
        NSISWizardDialogUtil.createLabel(group,"compressor.label", true, null, false); //$NON-NLS-1$
        Composite composite = new Composite(group,SWT.NONE);
        GridData gridData = new GridData(SWT.FILL,SWT.CENTER,true,false);
        gridData.horizontalSpan = 2;
        composite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Combo c = NSISWizardDialogUtil.createCombo(composite, 2,
                        NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES,
                        NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES[settings.getCompressorType()],
                        true, true, null);

        String solidKeyword = NSISKeywords.getInstance().getKeyword("/SOLID"); //$NON-NLS-1$
        if(NSISKeywords.getInstance().isValidKeyword(solidKeyword)) {
            ((GridData)c.getLayoutData()).horizontalSpan = 1;
            int index = c.getSelectionIndex();
            cb = NSISWizardDialogUtil.createCheckBox(composite,"solid.compression.text",settings.isSolidCompression(), //$NON-NLS-1$
                                                     (index >= 0 && index != MakeNSISRunner.COMPRESSOR_DEFAULT),
                                                     null,false);
            cb.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mWizard.getSettings().setSolidCompression(((Button)e.widget).getSelection());
                }
            });
            ((GridData)cb.getLayoutData()).horizontalSpan = 1;
        }
        else {
            cb = null;
        }

        final Combo c2;
        if(INSISVersions.VERSION_2_21.compareTo(NSISPreferences.INSTANCE.getNSISVersion()) <= 0) {
            String[] execLevels = NSISWizardDisplayValues.EXECUTION_LEVELS;
            if(INSISVersions.VERSION_2_22.compareTo(NSISPreferences.INSTANCE.getNSISVersion()) > 0) {
                execLevels = (String[])Common.subArray(execLevels,0,execLevels.length - 1);
            }

            c2 = NSISWizardDialogUtil.createCombo(group,execLevels,settings.getExecutionLevel(),
                        true, "execution.level.label", true, null, false);  //$NON-NLS-1$
            c2.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mWizard.getSettings().setExecutionLevel(c2.getSelectionIndex());
                }
            });
        }
        else {
            c2 = null;
            settings.setExecutionLevel(EXECUTION_LEVEL_NONE);
        }
        c.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                int index = ((Combo)e.widget).getSelectionIndex();
                mWizard.getSettings().setCompressorType(index);
                if(cb != null) {
                    cb.setEnabled(index >= 0 && index != MakeNSISRunner.COMPRESSOR_DEFAULT);
                }
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                t.setText(newSettings.getOutFile());
                t2.setText(newSettings.getIcon());
                int n = newSettings.getInstallerType();
                if(!Common.isEmptyArray(radio)) {
                    for (int i = 0; i < radio.length; i++) {
                        radio[i].setSelection((i == n));
                    }
                }
                n = newSettings.getCompressorType();
                if(n >= 0 && n < NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES.length) {
                    c.setText(NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES[n]);
                }
                else {
                    c.clearSelection();
                    c.setText(""); //$NON-NLS-1$
                }
                if(cb != null) {
                    cb.setSelection(newSettings.isSolidCompression());
                    int index = c.getSelectionIndex();
                    cb.setEnabled(index >= 0 && index != MakeNSISRunner.COMPRESSOR_DEFAULT);
                }

                if(c2 != null && newSettings.getExecutionLevel() < c2.getItemCount()) {
                    c2.select(newSettings.getExecutionLevel());
                }
                else {
                    newSettings.setExecutionLevel(EXECUTION_LEVEL_NONE);
                }
            }
        });
    }

    private void createUninstallerGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group group = NSISWizardDialogUtil.createGroup(parent, 3, "uninstaller.group.label", null, true); //$NON-NLS-1$
        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.uninstaller.label",settings.isCreateUninstaller(), //$NON-NLS-1$
                                        true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = b.getSelection();
                mWizard.getSettings().setCreateUninstaller(selection);
                validatePage(VALIDATE_ALL);
            }
        });

        final MasterSlaveController m = new MasterSlaveController(b);

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Text t = NSISWizardDialogUtil.createText(group, settings.getUninstallFile(), "uninstaller.file.label", true, //$NON-NLS-1$
                            m,isScriptWizard());
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setUninstallFile(((Text)e.widget).getText());
               validateField(UNINSTFILE_CHECK);
           }
        });

        final Text t2 = NSISWizardDialogUtil.createImageBrowser(group, settings.getUninstallIcon(), new Point(32,32),
                                          Common.loadArrayProperty(bundle,"uninstaller.icon.filternames"),  //$NON-NLS-1$
                                          Common.loadArrayProperty(bundle,"uninstaller.icon.filters"), "uninstaller.icon.label", //$NON-NLS-1$ //$NON-NLS-2$
                                          true, m, false);
        t2.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setUninstallIcon(t2.getText());
               validateField(UNINSTICON_CHECK);
           }
        });
        NSISWizardDialogUtil.loadImage(t2);

        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                b.setSelection(newSettings.isCreateUninstaller());
                t.setText(newSettings.getUninstallFile());
                t2.setText(newSettings.getUninstallIcon());
                m.updateSlaves();
            }});
    }

    private boolean validateAppName()
    {
        if(!Common.isEmpty(mWizard.getSettings().getName())) {
            return true;
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("application.name.error")); //$NON-NLS-1$
            return false;
        }
    }
}
