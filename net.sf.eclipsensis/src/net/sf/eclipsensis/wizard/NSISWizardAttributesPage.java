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

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.MasterSlaveEnabler;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardAttributesPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardAttributes"; //$NON-NLS-1$
    
    private static int LANGUAGES_COLUMNS_COUNT = 5;
    private static Properties cLanguageMap = new Properties();
    private static final int INSTDIR_CHECK=1;
    private static final int SMGRP_CHECK=2;
    private static final int LANG_CHECK=4;
    private static final int ALL_CHECK=INSTDIR_CHECK|SMGRP_CHECK|LANG_CHECK;
    private static String[] cInstallDirErrors = {"empty.installation.directory.error"}; //$NON-NLS-1$
    
    private LinkedHashSet mLanguages = new LinkedHashSet();

    static {
        try {
            
            ResourceBundle bundle = ResourceBundle.getBundle(NSISWizardAttributesPage.class.getPackage().getName()+".NSISLanguages"); //$NON-NLS-1$
            for(Enumeration enum = bundle.getKeys(); enum.hasMoreElements(); ) {
                String key = (String)enum.nextElement();
                cLanguageMap.put(key,bundle.getString(key));
            }
        }
        catch(MissingResourceException mre) {
        }
    }

    /**
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardAttributesPage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.attributes.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.attributes.description")); //$NON-NLS-1$
        loadLanguages();
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

    private boolean validateStartMenuGroup()
    {
        String startMenuGroup = mSettings.getStartMenuGroup();
        if(mSettings.isCreateStartMenuGroup() && !Common.isValidFileName(startMenuGroup)) {
            setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.start.menu.group.error"),new String[]{startMenuGroup})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private boolean validateLanguages()
    {
        String startMenuGroup = mSettings.getStartMenuGroup();
        if(mSettings.isCreateMultilingual() && mSettings.getLanguages().size() == 0) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("invalid.languages.error")); //$NON-NLS-1$
            return false;
        }
        return true;
    }
    
    private boolean validatePage(int flag)
    {
        boolean b = ((flag & INSTDIR_CHECK) == 0 || validateNSISPrefixedPathName(mSettings.getInstallDir(),cInstallDirErrors)) &&
                    ((flag & SMGRP_CHECK) == 0 || validateStartMenuGroup()) &&
                    ((flag & LANG_CHECK) == 0 || validateLanguages());
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
        }
        return b;
    }

    private void loadLanguages()
    {
        mLanguages.clear();
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            File nsisHome = new File(NSISPreferences.getPreferences().getNSISHome());
            if(nsisHome.exists()) {
                File langDir = new File(nsisHome,INSISConstants.LANGUAGE_FILES_LOCATION);
                if(langDir.exists()) {
                    File[] langFiles = langDir.listFiles(new FileFilter() {
                       public boolean accept(File pathName)
                       {
                           return (pathName != null && pathName.isFile() && pathName.getName().toLowerCase().endsWith(INSISConstants.LANGUAGE_FILES_FILTER));
                       }
                    });
                    for (int i = 0; i < langFiles.length; i++) {
                        String name = langFiles[i].getName();
                        mLanguages.add(name.substring(0,name.length()-4));
                    }
                }
            }
        }
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
    
        createInstallationDirectoryGroup(composite);
        createStartMenuGroupGroup(composite);
        
        createLanguagesGroup(composite); //$NON-NLS-1$

        validatePage(ALL_CHECK);
    }

    protected void createLanguagesGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "i18n.group.label",null,false); //$NON-NLS-1$

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.multilingual.label",mSettings.isCreateMultilingual(),true,null,false); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mSettings.setCreateMultilingual(selection);
                validateField(LANG_CHECK);
            }
        });

        MasterSlaveController m = new MasterSlaveController(b);
        
        Label l = NSISWizardDialogUtil.createLabel(group, "choose.languages.label", true, m, true); //$NON-NLS-1$

        Color bgColor = getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

        final ScrolledComposite sc = new ScrolledComposite(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        sc.setBackground(bgColor);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = WIDTH_HINT;
        data.heightHint = 150;
        sc.setLayoutData(data);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        sc.setLayout(layout);
        m.addSlave(sc);
        
        final Composite composite2 = new Composite(sc, SWT.NONE);
        composite2.setBackground(bgColor);
        data = new GridData(GridData.FILL_BOTH);
        composite2.setLayoutData(data);
        
        final GridLayout layout2 = new GridLayout(LANGUAGES_COLUMNS_COUNT,true);
        composite2.setLayout(layout2);
        sc.setContent(composite2);
 
        final Collection selectedItems = mSettings.getLanguages();
        selectedItems.clear();
        Locale locale = Locale.getDefault();
        String language = locale.getDisplayLanguage(Locale.US);
        if(mLanguages.contains(language)) {
            selectedItems.add(language);
        }
        else {
            language = (String)cLanguageMap.get(locale.toString());
            if(mLanguages.contains(language)) {
                selectedItems.add(language);
            }
            else {
                language = EclipseNSISPlugin.getResourceString("wizard.default.language"); //$NON-NLS-1$
                if(mLanguages.contains(language)) {
                    selectedItems.add(language);
                }
            }
        }
    
        SelectionListener selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Button button = (Button)e.getSource();
                if(button.getSelection()) {
                    selectedItems.add(button.getText());
                }
                else {
                    selectedItems.remove(button.getText());
                }
                validateField(LANG_CHECK);
            }
        };
        for (Iterator iter = mLanguages.iterator(); iter.hasNext(); ) {
            String item = (String)iter.next();
            Button button = new Button(composite2, SWT.CHECK | SWT.LEFT);
            button.setBackground(bgColor);
            button.setText(item);
            button.setSelection(selectedItems.contains(item));
            button.addSelectionListener(selectionListener);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        }
        
        final Point size = composite2.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        composite2.setSize(size);
        final int averageWidth = (size.x-2*layout2.marginWidth)/LANGUAGES_COLUMNS_COUNT;
        
        sc.addListener (SWT.Resize,  new Listener () {
            public void handleEvent (Event e) {
                Point size2 = sc.getSize();
                ScrollBar verticalBar = sc.getVerticalBar();
                int sbWidth=(verticalBar.isVisible()?verticalBar.getSize().x:0);
                int newWidth = Math.max(size.x,size2.x-sbWidth);
                int n = (int)Math.floor(((newWidth - 2*layout2.marginWidth)/averageWidth));
                n = Math.max(n,5);
                newWidth = averageWidth*n+2*layout2.marginWidth;
                int newHeight = (int)Math.round(size.x*size.y/newWidth + .5);
                composite2.setSize(newWidth, newHeight);
                if(layout2.numColumns != n) {
                    layout2.numColumns = n;
                    composite2.layout();
                }
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group,"select.language.label",mSettings.isSelectLanguage(),true,m,false); //$NON-NLS-1$
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mSettings.setSelectLanguage(b2.getSelection());
            }
        });

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                if(b2 == control) {
                    return (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT) && mSettings.isCreateMultilingual();
                }
                else {
                    return true;
                }
            }
        };
        ((NSISWizard)getWizard()).addNSISWizardListener(new NSISWizardAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                if(page != null && page.getName().equals(NAME) && forward) {
                    b2.setEnabled(mse.canEnable(b2));
                }
            }
        });

        m.setEnabler(b2,mse);
        m.updateSlaves();
    }

    /**
     * @param composite
     */
    private void createInstallationDirectoryGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "installation.directory.group.label",null,false); //$NON-NLS-1$

        final Combo c = NSISWizardDialogUtil.createCombo(group, NSISKeywords.PREDEFINED_PATH_VARIABLES, mSettings.getInstallDir(), false, "installation.directory.label", //$NON-NLS-1$
                              true, null, true);
        GridData gd = (GridData)c.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        c.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Combo)e.widget).getText();
                mSettings.setInstallDir(text);
                validateField(INSTDIR_CHECK);
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "change.installation.directory.label", //$NON-NLS-1$
                                        mSettings.isChangeInstallDir(),
                                        (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setChangeInstallDir(((Button)e.widget).getSelection());
            }
        });
        NSISWizard wizard = (NSISWizard)getWizard();
        wizard.addNSISWizardListener(new NSISWizardAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                if(page != null && page.getName().equals(NAME) && forward) {
                    c.setText(mSettings.getInstallDir());
                    b2.setEnabled(mSettings.getInstallerType() != INSTALLER_TYPE_SILENT);
                }
            }
        });
    }

    /**
     * @param composite
     */
    private void createStartMenuGroupGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "startmenu.group.group.label",null,false); //$NON-NLS-1$

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.startmenu.group.label",mSettings.isCreateStartMenuGroup(),  //$NON-NLS-1$
                                        true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mSettings.setCreateStartMenuGroup(selection);
                validateField(SMGRP_CHECK);
            }
        });

        MasterSlaveController m = new MasterSlaveController(b);
        final Text t = NSISWizardDialogUtil.createText(group, mSettings.getStartMenuGroup(), "startmenu.group.label", //$NON-NLS-1$
                            true, m, true);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mSettings.setStartMenuGroup(text);
                validateField(SMGRP_CHECK);
            }
        });
        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "change.startmenu.group.label", //$NON-NLS-1$
                                        mSettings.isChangeStartMenuGroup(),
                                        (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT), m, false);
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setChangeStartMenuGroup(((Button)e.widget).getSelection());
            }
        });
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                if(b2 == control) {
                    return (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT) && mSettings.isCreateStartMenuGroup();
                }
                else {
                    return true;
                }
            }
        };
        m.setEnabler(b2,mse);
        m.updateSlaves();
        NSISWizard wizard = (NSISWizard)getWizard();
        wizard.addNSISWizardListener(new NSISWizardAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                if(page != null && page.getName().equals(NAME) && forward) {
                    t.setText(mSettings.getStartMenuGroup());
                    b2.setEnabled(mse.canEnable(b2));
                }
            }
        });
    }
}
