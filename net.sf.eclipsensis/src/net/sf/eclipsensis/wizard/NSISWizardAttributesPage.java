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

import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.MasterSlaveEnabler;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

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
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardAttributesPage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.attributes.title"), //$NON-NLS-1$
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
        if(mSettings.isEnableLanguageSupport() && mSettings.getLanguages().size() == 0) {
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
        
        final Button b = NSISWizardDialogUtil.createCheckBox(group,"enable.language.support.label",mSettings.isEnableLanguageSupport(),true,null,false); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mSettings.setEnableLanguageSupport(selection);
                validateField(LANG_CHECK);
            }
        });

        MasterSlaveController m = new MasterSlaveController(b);
        
        final Composite composite2 = new Composite(group, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = WIDTH_HINT;
        data.heightHint = 150;
        composite2.setLayoutData(data);
        
        GridLayout layout = new GridLayout(21,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
        
        final java.util.List selectedLanguages = mSettings.getLanguages();
        selectedLanguages.clear();
        NSISLanguageManager nsisLanguageManager = NSISLanguageManager.getInstance();
        NSISLanguage defaultLanguage = nsisLanguageManager.getDefaultLanguage();
        if(defaultLanguage != null) {
            selectedLanguages.add(defaultLanguage);
        }
        final java.util.List availableLanguages = nsisLanguageManager.getLanguages();
        availableLanguages.removeAll(selectedLanguages);
        
        Label l = NSISWizardDialogUtil.createLabel(composite2, "available.languages.label", true, m, false); //$NON-NLS-1$
        ((GridData)l.getLayoutData()).horizontalSpan = 10;
        l = NSISWizardDialogUtil.createLabel(composite2, "selected.languages.label", true, m, true); //$NON-NLS-1$
        ((GridData)l.getLayoutData()).horizontalSpan = 11;
        
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
        allRightButton.setText(EclipseNSISPlugin.getResourceString("all.right.label"));

        final Button rightButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        rightButton.setLayoutData(data);
        rightButton.setText(EclipseNSISPlugin.getResourceString("right.label"));

        final Button leftButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        leftButton.setLayoutData(data);
        leftButton.setText(EclipseNSISPlugin.getResourceString("left.label"));
        
        final Button allLeftButton = new Button(composite3,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        allLeftButton.setLayoutData(data);
        allLeftButton.setText(EclipseNSISPlugin.getResourceString("all.left.label"));
        
        final List selectedLangList = new List(composite2,SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.MULTI);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan=8;
        selectedLangList.setLayoutData(data);
        m.addSlave(selectedLangList);
        
        final ListViewer lv2 = new ListViewer(selectedLangList);
        lv2.setContentProvider(collectionContentProvider);
        lv2.setInput(selectedLanguages);

        final Composite composite4 = new Composite(composite2,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite4.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_CENTER);
        data.horizontalSpan=3;
        composite4.setLayoutData(data);
        
        final Button upButton = new Button(composite4,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        upButton.setLayoutData(data);
        upButton.setText(EclipseNSISPlugin.getResourceString("up.label"));
        m.addSlave(upButton);
        
        final Button downButton = new Button(composite4,SWT.PUSH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        downButton.setLayoutData(data);
        downButton.setText(EclipseNSISPlugin.getResourceString("down.label"));
        m.addSlave(downButton);

        final Button b2 = NSISWizardDialogUtil.createCheckBox(group,"select.language.label",mSettings.isSelectLanguage(),true,m,false); //$NON-NLS-1$
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mSettings.setSelectLanguage(b2.getSelection());
            }
        });

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control c)
            {
                if(c == allRightButton) {
                    return mSettings.isEnableLanguageSupport() && (lv1.getList().getItemCount() > 0);
                }
                else if(c == rightButton) {
                    return mSettings.isEnableLanguageSupport() && (!lv1.getSelection().isEmpty());
                }
                else if(c == allLeftButton) {
                    return mSettings.isEnableLanguageSupport() && (lv2.getList().getItemCount() > 0);
                }
                else if(c == leftButton) {
                    return mSettings.isEnableLanguageSupport() && (!lv2.getSelection().isEmpty());
                }
                else if(c == upButton) {
                    return mSettings.isEnableLanguageSupport() && canMoveUp(lv2);
                }
                else if(c == downButton) {
                    return mSettings.isEnableLanguageSupport() && canMoveDown(lv2);
                }
                else if(c == b2) {
                    return (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT) && mSettings.isEnableLanguageSupport() && (selectedLanguages.size() > 1);
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
                 IStructuredSelection sel = (IStructuredSelection)lv2.getSelection();
                 if(!sel.isEmpty()) {
                     move(lv2, sel.toList(),false);
                 }
                 langRunnable.run();
             }
         });
         downButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent se) 
             {
                 IStructuredSelection sel = (IStructuredSelection)lv2.getSelection();
                 if(!sel.isEmpty()) {
                     move(lv2, sel.toList(),true);
                 }
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
    }
    
    /**
     * @param lv
     * @param list
     */
    private void move(ListViewer lv, java.util.List list, boolean down)
    {
        java.util.List input = (java.util.List)lv.getInput();
        java.util.List newList = moveUp((down?reverse(input):input),list);
        input.clear();
        input.addAll((down?reverse(newList):newList));
        lv.setSelection(new StructuredSelection(list));
        lv.refresh();
        lv.reveal(list.get(down?list.size()-1:0));
    }

    private java.util.List reverse(java.util.List p)
    {
        java.util.List reverse= new ArrayList(p.size());
        for (int i= p.size()-1; i >= 0; i--) {
            reverse.add(p.get(i));
        }
        return reverse;
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

    private java.util.List moveUp(java.util.List elements, java.util.List move) 
    {
        int nElements= elements.size();
        java.util.List res= new ArrayList(nElements);
        Object floating= null;
        for (int i= 0; i < nElements; i++) {
            Object curr= elements.get(i);
            if (move.contains(curr)) {
                res.add(curr);
            } else {
                if (floating != null) {
                    res.add(floating);
                }
                floating= curr;
            }
        }
        if (floating != null) {
            res.add(floating);
        }
        return res;
    }   
    

    private boolean canMoveUp(ListViewer listViewer)
    {
        int[] indices = listViewer.getList().getSelectionIndices();
        for (int i= 0; i < indices.length; i++) {
            if (indices[i] != i) {
                return true;
            }
        }
        return false;
    }

    private boolean canMoveDown(ListViewer listViewer)
    {
        int[] indc= listViewer.getList().getSelectionIndices();
        int k= ((java.util.List)listViewer.getInput()).size() - 1;
        for (int i= indc.length - 1; i >= 0 ; i--, k--) {
            if (indc[i] != k) {
                return true;
            }
        }
        return false;
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
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                c.setText(mSettings.getInstallDir());
                b2.setEnabled(mSettings.getInstallerType() != INSTALLER_TYPE_SILENT);
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
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                t.setText(mSettings.getStartMenuGroup());
                b2.setEnabled(mse.canEnable(b2));
            }
        });
    }
}
