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

import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.ListViewerUpDownMover;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;

public class NSISWizardAttributesPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardAttributes"; //$NON-NLS-1$
    
    private static final int INSTDIR_CHECK=1;
    private static final int SMGRP_CHECK=2;
    private static final int LANG_CHECK=4;
    private static final int ALL_CHECK=INSTDIR_CHECK|SMGRP_CHECK|LANG_CHECK;
    private static String[] cInstallDirErrors = {"empty.installation.directory.error"}; //$NON-NLS-1$

    private static Collator cLanguageCollator = Collator.getInstance(Locale.US);

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardAttributesPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.attributes.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.attributes.description")); //$NON-NLS-1$
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
        NSISWizardSettings settings = mWizard.getSettings();

        String startMenuGroup = settings.getStartMenuGroup();
        if(settings.isCreateStartMenuGroup() && !Common.isValidFileName(startMenuGroup)) {
            setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.start.menu.group.error"),new String[]{startMenuGroup})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private boolean validateLanguages()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        String startMenuGroup = settings.getStartMenuGroup();
        if(settings.isEnableLanguageSupport() && settings.getLanguages().size() == 0) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("invalid.languages.error")); //$NON-NLS-1$
            return false;
        }
        return true;
    }
    
    public boolean validatePage(int flag)
    {
        boolean b = ((flag & INSTDIR_CHECK) == 0 || validateNSISPathName(mWizard.getSettings().getInstallDir(),cInstallDirErrors)) &&
                    ((flag & SMGRP_CHECK) == 0 || validateStartMenuGroup()) &&
                    ((flag & LANG_CHECK) == 0 || validateLanguages());
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
    
        createInstallationDirectoryGroup(composite);
        createStartMenuGroupGroup(composite);
        
        createLanguagesGroup(composite); //$NON-NLS-1$

        validatePage(ALL_CHECK);
    }

    protected void createLanguagesGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Group group = NSISWizardDialogUtil.createGroup(parent, 1, "language.support.group.label",null,false); //$NON-NLS-1$
        GridData data = ((GridData)group.getLayoutData());
        data.verticalAlignment = GridData.FILL;
        data.grabExcessVerticalSpace = true;
        
        NSISWizardSettings settings = mWizard.getSettings();

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"enable.language.support.label",settings.isEnableLanguageSupport(),true,null,false); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mWizard.getSettings().setEnableLanguageSupport(selection);
                validateField(LANG_CHECK);
            }
        });

        final MasterSlaveController m = new MasterSlaveController(b);
        
        final Composite composite2 = new Composite(group, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = WIDTH_HINT;
        data.heightHint = 150;
        composite2.setLayoutData(data);
        
        GridLayout layout = new GridLayout(20,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
        
        java.util.List selectedLanguages = settings.getLanguages();
        selectedLanguages.clear();
        final NSISLanguageManager nsisLanguageManager = NSISLanguageManager.getInstance();
        NSISLanguage defaultLanguage = nsisLanguageManager.getDefaultLanguage();
        if(defaultLanguage != null) {
            selectedLanguages.add(defaultLanguage);
        }
        java.util.List availableLanguages = nsisLanguageManager.getLanguages();
        availableLanguages.removeAll(selectedLanguages);
        
        Label l = NSISWizardDialogUtil.createLabel(composite2, "available.languages.label", true, m, false); //$NON-NLS-1$
        ((GridData)l.getLayoutData()).horizontalSpan = 10;
        l = NSISWizardDialogUtil.createLabel(composite2, "selected.languages.label", true, m, true); //$NON-NLS-1$
        ((GridData)l.getLayoutData()).horizontalSpan = 10;
        
        final List availableLangList = new List(composite2,SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.MULTI);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan=8;
        availableLangList.setLayoutData(data);
        m.addSlave(availableLangList);
        
        final ListViewer lv1 = new ListViewer(availableLangList);
        CollectionContentProvider collectionContentProvider = new CollectionContentProvider();
        lv1.setContentProvider(collectionContentProvider);
        lv1.setInput(availableLanguages);
        lv1.setSorter(new ViewerSorter(cLanguageCollator));

        final Composite composite3 = new Composite(composite2,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite3.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_CENTER);
        data.horizontalSpan=2;
        composite3.setLayoutData(data);

        final Button allRightButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        allRightButton.setLayoutData(data);
        allRightButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("all.right.icon"))); //$NON-NLS-1$
        allRightButton.setToolTipText(EclipseNSISPlugin.getResourceString("all.right.tooltip")); //$NON-NLS-1$

        final Button rightButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        rightButton.setLayoutData(data);
        rightButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("right.icon"))); //$NON-NLS-1$
        rightButton.setToolTipText(EclipseNSISPlugin.getResourceString("right.tooltip")); //$NON-NLS-1$

        final Button leftButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        leftButton.setLayoutData(data);
        leftButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("left.icon"))); //$NON-NLS-1$
        leftButton.setToolTipText(EclipseNSISPlugin.getResourceString("left.tooltip")); //$NON-NLS-1$
        
        final Button allLeftButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        allLeftButton.setLayoutData(data);
        allLeftButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("all.left.icon"))); //$NON-NLS-1$
        allLeftButton.setToolTipText(EclipseNSISPlugin.getResourceString("all.left.tooltip")); //$NON-NLS-1$
        
        final List selectedLangList = new List(composite2,SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.MULTI);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan=8;
        selectedLangList.setLayoutData(data);
        m.addSlave(selectedLangList);
        
        final ListViewer lv2 = new ListViewer(selectedLangList);
        lv2.setContentProvider(collectionContentProvider);
        lv2.setInput(selectedLanguages);

        final ListViewerUpDownMover mover = new ListViewerUpDownMover(){
            protected java.util.List getAllElements()
            {
                Collection collection = (Collection)((ListViewer)getInput()).getInput();
                if(collection instanceof java.util.List) {
                    return (java.util.List)collection;
                }
                else {
                    return new ArrayList(collection);
                }
            }

            protected java.util.List getMoveElements()
            {
                IStructuredSelection sel = (IStructuredSelection)((ListViewer)getInput()).getSelection();
                if(!sel.isEmpty()) {
                    return sel.toList();
                }
                else {
                    return Collections.EMPTY_LIST;
                }
            }

            protected void updateStructuredViewerInput(Object input, java.util.List elements)
            {
                ((Collection)input).clear();
                ((Collection)input).addAll(elements);
            }
        };
        
        mover.setInput(lv2);

        final Composite composite4 = new Composite(composite2,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite4.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_CENTER);
        data.horizontalSpan=2;
        composite4.setLayoutData(data);
        
        final Button upButton = new Button(composite4,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        upButton.setLayoutData(data);
        upButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        upButton.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        m.addSlave(upButton);
        
        final Button downButton = new Button(composite4,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        downButton.setLayoutData(data);
        downButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
        downButton.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        m.addSlave(downButton);

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group,"select.language.label",settings.isSelectLanguage(),true,m,false); //$NON-NLS-1$
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mWizard.getSettings().setSelectLanguage(b2.getSelection());
            }
        });

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control c)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(c == allRightButton) {
                    return settings.isEnableLanguageSupport() && (lv1.getList().getItemCount() > 0);
                }
                else if(c == rightButton) {
                    return settings.isEnableLanguageSupport() && (!lv1.getSelection().isEmpty());
                }
                else if(c == allLeftButton) {
                    return settings.isEnableLanguageSupport() && (lv2.getList().getItemCount() > 0);
                }
                else if(c == leftButton) {
                    return settings.isEnableLanguageSupport() && (!lv2.getSelection().isEmpty());
                }
                else if(c == upButton) {
                    return settings.isEnableLanguageSupport() && mover.canMoveUp();
                }
                else if(c == downButton) {
                    return settings.isEnableLanguageSupport() && mover.canMoveDown();
                }
                else if(c == b2) {
                    java.util.List selectedLanguages = (java.util.List)lv2.getInput();
                    return (settings.getInstallerType() != INSTALLER_TYPE_SILENT) && settings.isEnableLanguageSupport() && (selectedLanguages.size() > 1);
                }
                else {
                    return true;
                }
            }
        };
        m.addSlave(rightButton, mse);
        m.addSlave(allRightButton, mse);
        m.addSlave(leftButton, mse);
        m.addSlave(allLeftButton, mse);
        m.setEnabler(upButton, mse);
        m.setEnabler(downButton, mse);
        m.setEnabler(b2,mse);
        
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                b2.setEnabled(mse.canEnable(b2));
            }
        });
        final Runnable langRunnable = new Runnable() {
            public void run()
            {
                lv1.refresh(false);
                lv2.refresh(false);
                allRightButton.setEnabled(mse.canEnable(allRightButton));
                allLeftButton.setEnabled(mse.canEnable(allLeftButton));
                rightButton.setEnabled(mse.canEnable(rightButton));
                leftButton.setEnabled(mse.canEnable(leftButton));
                upButton.setEnabled(mse.canEnable(upButton));
                downButton.setEnabled(mse.canEnable(downButton));
                b2.setEnabled(mse.canEnable(b2));
                setPageComplete(validateField(LANG_CHECK));
            }
        };
        
        rightButton.addSelectionListener(new SelectionAdapter() {
           public void widgetSelected(SelectionEvent se) 
           {
               moveAcross(lv1,lv2,((IStructuredSelection)lv1.getSelection()).toList());
               langRunnable.run();
           }
        });
        allRightButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) 
            {
                moveAcross(lv1,lv2,(java.util.List)lv1.getInput());
                langRunnable.run();
            }
        });
        leftButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) 
            {
                moveAcross(lv2,lv1,((IStructuredSelection)lv2.getSelection()).toList());
                langRunnable.run();
            }
         });
        allLeftButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) 
            {
                moveAcross(lv2,lv1,(java.util.List)lv2.getInput());
                langRunnable.run();
            }
        });
        upButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) 
            {
                mover.moveUp();
                langRunnable.run();
            }
        });
        downButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) 
            {
                mover.moveDown();
                langRunnable.run();
            }
        });
        
        lv1.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                rightButton.setEnabled(mse.canEnable(rightButton));
                allRightButton.setEnabled(mse.canEnable(allRightButton));
            }
        });
        lv1.getList().addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event) {
                IStructuredSelection sel = (IStructuredSelection)lv1.getSelection();
                if(!sel.isEmpty()) {
                    moveAcross(lv1,lv2,sel.toList());
                    lv2.reveal(sel.getFirstElement());
                    langRunnable.run();
                }
            }
        });
        lv2.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                leftButton.setEnabled(mse.canEnable(leftButton));
                allLeftButton.setEnabled(mse.canEnable(allLeftButton));
                upButton.setEnabled(mse.canEnable(upButton));
                downButton.setEnabled(mse.canEnable(downButton));
                b2.setEnabled(mse.canEnable(b2));
            }
        });
        lv2.getList().addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event) {
                IStructuredSelection sel = (IStructuredSelection)lv2.getSelection();
                if(!sel.isEmpty()) {
                    moveAcross(lv2,lv1,sel.toList());
                    lv1.reveal(sel.getFirstElement());
                    langRunnable.run();
                }
            }
        });
        
        m.updateSlaves();

        final int diff = group.computeSize(SWT.DEFAULT,SWT.DEFAULT).y-150;
        group.addListener(SWT.Resize,new Listener() {
            boolean init = false;
            public void handleEvent (Event e)
            {
                if(!init) {
                    //Stupid hack so that the height hint doesn't get changed on the first resize,
                    //i.e., when the page is drawn for the first time.
                    init = true;
                }
                else {
                    ((GridData)composite2.getLayoutData()).heightHint=group.getSize().y-diff;
                }
            }
        });
        
        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                b.setSelection(settings.isEnableLanguageSupport());
                m.updateSlaves();
                b2.setSelection(settings.isSelectLanguage());
                java.util.List selectedLanguages = settings.getLanguages();
                lv2.setInput(selectedLanguages);
                java.util.List availableLanguages = nsisLanguageManager.getLanguages();
                availableLanguages.removeAll(selectedLanguages);
                lv1.setInput(availableLanguages);
            }
        });
    }

    private void moveAcross(ListViewer fromLV, ListViewer toLV, java.util.List move)
    {
        java.util.List from = (java.util.List)fromLV.getInput();
        java.util.List to = (java.util.List)toLV.getInput();
        to.addAll(move);
        from.removeAll(move);
        fromLV.refresh(false);
        toLV.refresh(false);
    }

    /**
     * @param composite
     */
    private void createInstallationDirectoryGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "installation.directory.group.label",null,false); //$NON-NLS-1$

        NSISWizardSettings settings = mWizard.getSettings();
        
        final Combo c = NSISWizardDialogUtil.createCombo(group, NSISKeywords.PATH_CONSTANTS_AND_VARIABLES, 
                              settings.getInstallDir(), false, "installation.directory.label", //$NON-NLS-1$
                              true, null, true);
        GridData gd = (GridData)c.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        c.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Combo)e.widget).getText();
                mWizard.getSettings().setInstallDir(text);
                validateField(INSTDIR_CHECK);
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "change.installation.directory.label", //$NON-NLS-1$
                settings.isChangeInstallDir(),
                (settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setChangeInstallDir(((Button)e.widget).getSelection());
            }
        });
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                c.setText(settings.getInstallDir());
                b2.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                c.setText(settings.getInstallDir());
                b2.setSelection(settings.isChangeInstallDir());
                b2.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);
            }});
    }

    /**
     * @param composite
     */
    private void createStartMenuGroupGroup(Composite parent)
    {
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "startmenu.group.group.label",null,false); //$NON-NLS-1$

        NSISWizardSettings settings = mWizard.getSettings();

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.startmenu.group.label",settings.isCreateStartMenuGroup(),  //$NON-NLS-1$
                                        true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mWizard.getSettings().setCreateStartMenuGroup(selection);
                validateField(SMGRP_CHECK);
            }
        });

        final MasterSlaveController m = new MasterSlaveController(b);
        final Text t = NSISWizardDialogUtil.createText(group, settings.getStartMenuGroup(), "startmenu.group.label", //$NON-NLS-1$
                            true, m, true);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setStartMenuGroup(text);
                validateField(SMGRP_CHECK);
            }
        });
        final Button b2 = NSISWizardDialogUtil.createCheckBox(group, "change.startmenu.group.label", //$NON-NLS-1$
                                        settings.isChangeStartMenuGroup(),
                                        (settings.getInstallerType() != INSTALLER_TYPE_SILENT), m, false);
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setChangeStartMenuGroup(((Button)e.widget).getSelection());
            }
        });
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(b2 == control) {
                    return (settings.getInstallerType() != INSTALLER_TYPE_SILENT) && settings.isCreateStartMenuGroup();
                }
                else {
                    return true;
                }
            }
        };
        m.setEnabler(b2,mse);
        m.updateSlaves();
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                t.setText(mWizard.getSettings().getStartMenuGroup());
                b2.setEnabled(mse.canEnable(b2));
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                b.setSelection(settings.isCreateStartMenuGroup());
                t.setText(settings.getStartMenuGroup());
                b2.setSelection(settings.isChangeStartMenuGroup());
                b2.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);

                m.updateSlaves();
            }});
    }
}
