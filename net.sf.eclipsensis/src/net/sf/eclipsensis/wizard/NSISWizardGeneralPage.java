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

import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardGeneralPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardGeneral"; //$NON-NLS-1$

    private static final int APPNAME_CHECK=1;
    private static final int PUBURL_CHECK=2;
    private static final int INSTFILE_CHECK=4;
    private static final int INSTICON_CHECK=8;
    private static final int UNINSTFILE_CHECK=16;
    private static final int UNINSTICON_CHECK=32;
    private static final int ALL_CHECK=APPNAME_CHECK|PUBURL_CHECK|INSTFILE_CHECK|INSTICON_CHECK|UNINSTFILE_CHECK|UNINSTICON_CHECK;

    private static String[] cInstallFileErrors = {"empty.installer.file.error"}; //$NON-NLS-1$
    private static String[] cUninstallFileErrors = {"empty.uninstaller.file.error"}; //$NON-NLS-1$

    
    /**
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardGeneralPage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.general.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.general.description")); //$NON-NLS-1$
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

    private boolean validatePage(int flag)
    {
        boolean b = ((flag & APPNAME_CHECK) == 0 || validateAppName()) && 
               ((flag & PUBURL_CHECK) == 0 || validateEmptyOrValidURL(mSettings.getUrl(),null)) &&
               ((flag & INSTFILE_CHECK) == 0 || validatePathName(mSettings.getOutFile(),cInstallFileErrors)) &&
               ((flag & INSTICON_CHECK) == 0 || validateEmptyOrValidFile(mSettings.getInstallIcon(),null)) &&
               ((flag & UNINSTFILE_CHECK) == 0 || !mSettings.isCreateUninstaller() || validateFileName(mSettings.getUninstallFile(),cUninstallFileErrors)) &&
               ((flag & UNINSTICON_CHECK) == 0 || !mSettings.isCreateUninstaller() || validateEmptyOrValidFile(mSettings.getUninstallIcon(),null));
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
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

        createApplicationGroup(composite);
        createInstallerGroup(composite);
        createUninstallerGroup(composite);
        
        validatePage(ALL_CHECK);
    }
    
    private void createApplicationGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "application.group.label", null, true); //$NON-NLS-1$

        Text t = NSISWizardDialogUtil.createText(group, mSettings.getName(), "application.name.label", true, null, true); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               String newName = ((Text)e.widget).getText();
               String installDir = mSettings.getInstallDir();
               if(Common.isEmpty(installDir) || installDir.equalsIgnoreCase("$PROGRAMFILES\\"+mSettings.getName())) { //$NON-NLS-1$
                   mSettings.setInstallDir("$PROGRAMFILES\\"+newName); //$NON-NLS-1$
               }
               String startMenuGroup = mSettings.getStartMenuGroup();
               if(Common.isEmpty(startMenuGroup) || startMenuGroup.equalsIgnoreCase(mSettings.getName())) {
                   mSettings.setStartMenuGroup(newName);
               }
               mSettings.setName(newName);
               validateField(APPNAME_CHECK);
           }
        });
        
        t = NSISWizardDialogUtil.createText(group, mSettings.getVersion(), "application.version.label", true, null, false); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setVersion(((Text)e.widget).getText());
           }
        });

        t = NSISWizardDialogUtil.createText(group, mSettings.getCompany(), "publisher.name.label", true, null, false); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setCompany(((Text)e.widget).getText());
           }
        });

        t = NSISWizardDialogUtil.createText(group, mSettings.getUrl(), "publisher.url.label", true, null, false); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setUrl(((Text)e.widget).getText());
               validateField(PUBURL_CHECK);
           }
        });
        
    }

    private void createInstallerGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 3, "installer.group.label", null, true); //$NON-NLS-1$
        
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Text t = NSISWizardDialogUtil.createFileBrowser(group, mSettings.getOutFile(), true, 
                                   Common.loadArrayProperty(bundle,"installer.file.filternames"),  //$NON-NLS-1$
                                   Common.loadArrayProperty(bundle,"installer.file.filters"), "installer.file.label", //$NON-NLS-1$ //$NON-NLS-2$
                                   true, null,true);
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setOutFile(((Text)e.widget).getText());
               validateField(INSTFILE_CHECK);
           }
        });
    
        final Text t2 = NSISWizardDialogUtil.createImageBrowser(group, mSettings.getInstallIcon(), new Point(36,36), 
                                          Common.loadArrayProperty(bundle,"installer.icon.filternames"),  //$NON-NLS-1$
                                          Common.loadArrayProperty(bundle,"installer.icon.filters"), "installer.icon.label", //$NON-NLS-1$ //$NON-NLS-2$ 
                                          true, null, false);
        t2.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setInstallIcon(t2.getText());
               validateField(INSTICON_CHECK);
           }
        });
    
        Button[] radio = NSISWizardDialogUtil.createRadioGroup(group, NSISWizardDisplayValues.INSTALLER_TYPE_NAMES, 
                                           mSettings.getInstallerType(),"installer.type.label", //$NON-NLS-1$
                                           true, null,false);
        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Button button = (Button)e.widget;
                if(button.getSelection()) {
                    Integer index = (Integer)button.getData();
                    mSettings.setInstallerType(index.intValue());
                }
            }
        };
        for (int i = 0; i < radio.length; i++) {
            radio[i].addSelectionListener(sa);
        }

        Combo c = NSISWizardDialogUtil.createCombo(group, NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES, 
                        mSettings.getCompressorType(),true,"compressor.label", //$NON-NLS-1$
                        true, null,false);
        c.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          mSettings.setCompressorType(((Combo)e.widget).getSelectionIndex());
        }
        });
    }

    private void createUninstallerGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 3, "uninstaller.group.label", null, true); //$NON-NLS-1$
        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.uninstaller.label",mSettings.isCreateUninstaller(), //$NON-NLS-1$
                                        true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mSettings.setCreateUninstaller(selection);
                validatePage(ALL_CHECK);
            }
        });
        
        MasterSlaveController m = new MasterSlaveController(b);
        
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Text t = NSISWizardDialogUtil.createText(group, mSettings.getUninstallFile(), "uninstaller.file.label", true, //$NON-NLS-1$
                            m,true);
        t.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setUninstallFile(((Text)e.widget).getText());
               validateField(UNINSTFILE_CHECK);
           }
        });
    
        final Text t2 = NSISWizardDialogUtil.createImageBrowser(group, mSettings.getInstallIcon(), new Point(36,36), 
                                          Common.loadArrayProperty(bundle,"uninstaller.icon.filternames"),  //$NON-NLS-1$
                                          Common.loadArrayProperty(bundle,"uninstaller.icon.filters"), "uninstaller.icon.label", //$NON-NLS-1$ //$NON-NLS-2$ 
                                          true, m, false);
        t2.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mSettings.setUninstallIcon(t2.getText());
               validateField(UNINSTICON_CHECK);
           }
        });
    }
    
    private boolean validateAppName()
    {
        if(!Common.isEmpty(mSettings.getName())) {
            return true;
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("application.name.error")); //$NON-NLS-1$
            return false;
        }
    }
}