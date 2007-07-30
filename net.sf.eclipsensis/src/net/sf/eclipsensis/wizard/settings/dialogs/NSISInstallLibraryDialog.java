/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallLibrary;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstallLibraryDialog extends AbstractNSISInstallItemDialog
{
    private static final String TLB_EXTENSION = ".tlb"; //$NON-NLS-1$

    private static ArrayList cProperties = new ArrayList();

    static {
        cProperties.add("destination"); //$NON-NLS-1$
        cProperties.add("name"); //$NON-NLS-1$
        cProperties.add("shared"); //$NON-NLS-1$
        cProperties.add("libType"); //$NON-NLS-1$
        cProperties.add("protected"); //$NON-NLS-1$
        cProperties.add("reboot"); //$NON-NLS-1$
        cProperties.add("refreshShell"); //$NON-NLS-1$
        cProperties.add("unloadLibraries"); //$NON-NLS-1$
        cProperties.add("removeOnUninstall"); //$NON-NLS-1$
        cProperties.add("ignoreVersion"); //$NON-NLS-1$
        cProperties.add("x64"); //$NON-NLS-1$
    }

    public NSISInstallLibraryDialog(NSISWizard wizard, NSISInstallLibrary item)
    {
        super(wizard, item);
        mStore.setDefault("shared", true); //$NON-NLS-1$
        mStore.setDefault("libType", LIBTYPE_DLL); //$NON-NLS-1$
        mStore.setDefault("protected", true); //$NON-NLS-1$
        mStore.setDefault("reboot", true); //$NON-NLS-1$
        mStore.setDefault("refreshShell", false); //$NON-NLS-1$
        mStore.setDefault("unloadLibraries", false); //$NON-NLS-1$
        mStore.setDefault("removeOnUninstall", true); //$NON-NLS-1$
        mStore.setDefault("ignoreVersion", false); //$NON-NLS-1$
        mStore.setDefault("x64", false); //$NON-NLS-1$
    }

    protected String checkForErrors()
    {
        if (!IOUtility.isValidFile(IOUtility.decodePath(mStore.getString("name")))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.name"); //$NON-NLS-1$
        }
        else if (!IOUtility.isValidNSISPathName(mStore.getString("destination"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.destination"); //$NON-NLS-1$
        }
        else {
            return ""; //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Text t = NSISWizardDialogUtil.createFileBrowser(composite, mStore.getString("name"), false, //$NON-NLS-1$
                Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(), "wizard.library.filternames"), //$NON-NLS-1$
                Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(), "wizard.library.filters"), //$NON-NLS-1$
                "wizard.library.label", true, null, true); //$NON-NLS-1$
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite, NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES), mStore.getString("destination"), //$NON-NLS-1$
                false, "wizard.destination.label", true, null, true); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination", c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        GridData gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;

        final Combo c2 = NSISWizardDialogUtil.createCombo(composite, NSISWizardDisplayValues.LIBTYPES, mStore.getInt("libType"), //$NON-NLS-1$
                true, "wizard.lib.type.label", true, null, false); //$NON-NLS-1$
        c2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("libType", c2.getSelectionIndex()); //$NON-NLS-1$
                validate();
            }
        });
        gd = (GridData)c2.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;

        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                String name = t.getText().trim();
                mStore.setValue("name", name); //$NON-NLS-1$
                if (name.regionMatches(true, name.length() - TLB_EXTENSION.length(), TLB_EXTENSION, 0, TLB_EXTENSION.length())) {
                    if (c2.getSelectionIndex() != INSISWizardConstants.LIBTYPE_TLB) {
                        c2.select(INSISWizardConstants.LIBTYPE_TLB);
                        return;
                    }
                }
                validate();
            }
        });

        final Button cb1 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.shared.library.label", mStore.getBoolean("shared"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("shared", cb1.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        final Button cb2 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.upgrade.reboot.label", mStore.getBoolean("reboot"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("reboot", cb2.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        final Button cb3 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.protected.library.label", mStore.getBoolean("protected"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("protected", cb3.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        boolean flag = NSISPreferences.INSTANCE.getNSISVersion().compareTo(INSISVersions.VERSION_2_26) >= 0 && mWizard.getSettings().getTargetPlatform() != INSISWizardConstants.TARGET_PLATFORM_ANY;

        if (flag) {
            final Button cb4 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.x64.library.label", mStore.getBoolean("x64"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
            cb4.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mStore.setValue("x64", cb4.getSelection()); //$NON-NLS-1$
                    validate();
                }
            });
        }

        final Button cb5 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.refresh.shell.label", mStore.getBoolean("refreshShell"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb5.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("refreshShell", cb5.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        final Button cb6 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.unload.libraries.label", mStore.getBoolean("unloadLibraries"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb6.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("unloadLibraries", cb6.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        if (flag) {
            final Button cb7 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.ignore.version.label", mStore.getBoolean("x64"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
            cb7.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mStore.setValue("ignoreVersion", cb7.getSelection()); //$NON-NLS-1$
                    validate();
                }
            });
        }

        if (mWizard.getSettings().isCreateUninstaller()) {
            final Button cb8 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.remove.on.uninstall.label", mStore.getBoolean("removeOnUninstall"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
            cb8.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mStore.setValue("removeOnUninstall", cb8.getSelection()); //$NON-NLS-1$
                    validate();
                }
            });
        }

        return composite;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_librarydlg_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    protected List getProperties()
    {
        return cProperties;
    }

    protected boolean hasRequiredFields()
    {
        return true;
    }
}
