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
import java.text.Collator;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.ListViewerUpDownMover;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
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

    private boolean validateStartMenuGroup()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if(settings.isCreateStartMenuGroup()) {
            String startMenuGroup = settings.getStartMenuGroup();
            String[] parts = Common.tokenize(startMenuGroup,File.separatorChar);
            if(Common.isEmptyArray(parts)) {
                setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.start.menu.group.error",new String[]{startMenuGroup})); //$NON-NLS-1$
                return false;
            }
            for (int i = 0; i < parts.length; i++) {
                if(!IOUtility.isValidFileName(parts[i])) {
                    setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.start.menu.group.error",new String[]{startMenuGroup})); //$NON-NLS-1$
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateLanguages()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if(settings.isEnableLanguageSupport() && settings.getLanguages().size() == 0) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("invalid.languages.error")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            boolean b = ((flag & INSTDIR_CHECK) == 0 || validateNSISPathName(mWizard.getSettings().getInstallDir(),cInstallDirErrors)) &&
                        ((flag & SMGRP_CHECK) == 0 || validateStartMenuGroup()) &&
                        ((flag & LANG_CHECK) == 0 || validateLanguages());
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizattrib_context"; //$NON-NLS-1$
    }

    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        createInstallationDirectoryGroup(composite);
        createStartMenuGroupGroup(composite);

        createLanguagesGroup(composite);

        validatePage(ALL_CHECK);
        return composite;
    }

    protected void createLanguagesGroup(Composite parent)
    {
        final Group group = NSISWizardDialogUtil.createGroup(parent, 1, "language.support.group.label",null,false); //$NON-NLS-1$
        GridData data = ((GridData)group.getLayoutData());
        data.verticalAlignment = SWT.FILL;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

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

        final Composite listsComposite = new Composite(group, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        listsComposite.setLayoutData(data);

        GridLayout layout = new GridLayout(2,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        listsComposite.setLayout(layout);

        java.util.List selectedLanguages = settings.getLanguages();
        if(selectedLanguages.isEmpty()) {
            NSISLanguage defaultLanguage = NSISLanguageManager.getInstance().getDefaultLanguage();
            if(defaultLanguage != null) {
                selectedLanguages.add(defaultLanguage);
            }
        }
        java.util.List availableLanguages = NSISLanguageManager.getInstance().getLanguages();
        availableLanguages.removeAll(selectedLanguages);

        Composite leftComposite = new Composite(listsComposite,SWT.NONE);
        leftComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        leftComposite.setLayout(layout);

        Label l = NSISWizardDialogUtil.createLabel(leftComposite, "available.languages.label", true, m, false); //$NON-NLS-1$
        ((GridData)NSISWizardDialogUtil.getLayoutControl(l).getLayoutData()).horizontalSpan = 2;

        final List availableLangList = new List(leftComposite,SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.MULTI);
        data = new GridData(SWT.FILL,SWT.FILL,true,true);
        Dialog.applyDialogFont(availableLangList);
        data.heightHint = Common.calculateControlSize(availableLangList,0,10).y;
        availableLangList.setLayoutData(data);
        m.addSlave(availableLangList);

        final ListViewer lv1 = new ListViewer(availableLangList);
        CollectionContentProvider collectionContentProvider = new CollectionContentProvider();
        lv1.setContentProvider(collectionContentProvider);
        lv1.setInput(availableLanguages);
        lv1.setSorter(new ViewerSorter(cLanguageCollator));

        final Composite buttonsComposite1 = new Composite(leftComposite,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttonsComposite1.setLayout(layout);
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonsComposite1.setLayoutData(data);

        final Button allRightButton = new Button(buttonsComposite1,SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        allRightButton.setLayoutData(data);
        allRightButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("all.right.icon"))); //$NON-NLS-1$
        allRightButton.setToolTipText(EclipseNSISPlugin.getResourceString("all.right.tooltip")); //$NON-NLS-1$

        final Button rightButton = new Button(buttonsComposite1,SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        rightButton.setLayoutData(data);
        rightButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("right.icon"))); //$NON-NLS-1$
        rightButton.setToolTipText(EclipseNSISPlugin.getResourceString("right.tooltip")); //$NON-NLS-1$

        final Button leftButton = new Button(buttonsComposite1,SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        leftButton.setLayoutData(data);
        leftButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("left.icon"))); //$NON-NLS-1$
        leftButton.setToolTipText(EclipseNSISPlugin.getResourceString("left.tooltip")); //$NON-NLS-1$

        final Button allLeftButton = new Button(buttonsComposite1,SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        allLeftButton.setLayoutData(data);
        allLeftButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("all.left.icon"))); //$NON-NLS-1$
        allLeftButton.setToolTipText(EclipseNSISPlugin.getResourceString("all.left.tooltip")); //$NON-NLS-1$

        Composite rightComposite = new Composite(listsComposite,SWT.NONE);
        rightComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        rightComposite.setLayout(layout);

        l = NSISWizardDialogUtil.createLabel(rightComposite, "selected.languages.label", true, m, isScriptWizard()); //$NON-NLS-1$
        ((GridData)NSISWizardDialogUtil.getLayoutControl(l).getLayoutData()).horizontalSpan = 2;

        final List selectedLangList = new List(rightComposite,SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.MULTI);
        data = new GridData(SWT.FILL,SWT.FILL,true,true);
        Dialog.applyDialogFont(selectedLangList);
        data.heightHint = Common.calculateControlSize(selectedLangList,0,10).y;
        selectedLangList.setLayoutData(data);
        m.addSlave(selectedLangList);

        final ListViewer lv2 = new ListViewer(selectedLangList);
        lv2.setContentProvider(collectionContentProvider);
        lv2.setInput(selectedLanguages);

        final ListViewerUpDownMover mover = new ListViewerUpDownMover(){
            protected java.util.List getAllElements()
            {
                Collection collection = (Collection)((ListViewer)getViewer()).getInput();
                if(collection instanceof java.util.List) {
                    return (java.util.List)collection;
                }
                else {
                    return new ArrayList(collection);
                }
            }

            protected void updateStructuredViewerInput(Object input, java.util.List elements, java.util.List move, boolean isDown)
            {
                ((Collection)input).clear();
                ((Collection)input).addAll(elements);
            }
        };

        mover.setViewer(lv2);

        final Composite buttonsComposite2 = new Composite(rightComposite,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttonsComposite2.setLayout(layout);
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonsComposite2.setLayoutData(data);

        final Button upButton = new Button(buttonsComposite2,SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        upButton.setLayoutData(data);
        upButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        upButton.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        m.addSlave(upButton);

        final Button downButton = new Button(buttonsComposite2,SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        downButton.setLayoutData(data);
        downButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
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
            public void enabled(Control control, boolean flag) { }

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

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if(isCurrentPage()) {
                    b2.setEnabled(mse.canEnable(b2));
                }
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

        listsComposite.addListener(SWT.Resize,new Listener() {
            boolean init = false;
            public void handleEvent (Event e)
            {
                if(!init) {
                    //Stupid hack so that the height hint doesn't get changed on the first resize,
                    //i.e., when the page is drawn for the first time.
                    init = true;
                }
                else {
                    Point size = listsComposite.getSize();
                    GridLayout layout = (GridLayout)listsComposite.getLayout();
                    int heightHint = size.y - 2*layout.marginHeight;
                    ((GridData)availableLangList.getLayoutData()).heightHint = heightHint;
                    ((GridData)selectedLangList.getLayoutData()).heightHint = heightHint;
                    int totalWidth = size.x - 2*layout.marginWidth - 3*layout.horizontalSpacing;
                    int listWidth =  (int)(totalWidth*0.4);
                    int buttonWidth = (int)(0.5*(totalWidth-2*listWidth));
                    size = availableLangList.computeSize(listWidth,SWT.DEFAULT);
                    int delta = 0;
                    if(size.x > listWidth) {
                        delta = size.x-listWidth;
                        listWidth = listWidth - delta;
                    }
                    ((GridData)availableLangList.getLayoutData()).widthHint =listWidth;
                    ((GridData)buttonsComposite1.getLayoutData()).widthHint = totalWidth - 2*(listWidth + delta) - buttonWidth;
                    ((GridData)selectedLangList.getLayoutData()).widthHint = listWidth;
                    ((GridData)buttonsComposite2.getLayoutData()).widthHint = buttonWidth;
                    listsComposite.layout();
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
                java.util.List availableLanguages = NSISLanguageManager.getInstance().getLanguages();
                if(selectedLanguages.isEmpty()) {
                    NSISWizardWelcomePage welcomePage = (NSISWizardWelcomePage)mWizard.getPage(NSISWizardWelcomePage.NAME);
                    if(welcomePage != null) {
                        if(!welcomePage.isCreateFromTemplate()) {
                            NSISLanguage defaultLanguage = NSISLanguageManager.getInstance().getDefaultLanguage();
                            if(defaultLanguage != null && availableLanguages.contains(defaultLanguage)) {
                                selectedLanguages.add(defaultLanguage);
                            }
                        }
                    }
                }
                lv2.setInput(selectedLanguages);
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
        Group group = NSISWizardDialogUtil.createGroup(parent, 2, "installation.directory.group.label",null,false); //$NON-NLS-1$

        NSISWizardSettings settings = mWizard.getSettings();

        final Combo c = NSISWizardDialogUtil.createCombo(group,
                                            NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES),
                                            settings.getInstallDir(), false, "installation.directory.label", //$NON-NLS-1$
                                            true, null, isScriptWizard());
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

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if(isCurrentPage()) {
                    NSISWizardSettings settings = mWizard.getSettings();
                    c.setText(settings.getInstallDir());
                    b2.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);
                }
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
                NSISWizardContentsPage page = (NSISWizardContentsPage)mWizard.getPage(NSISWizardContentsPage.NAME);
                if(page != null) {
                    page.setPageComplete(page.validatePage(0xFFFF));
                    page.refresh();
                    mWizard.getContainer().updateButtons();
                }
            }
        });

        final MasterSlaveController m = new MasterSlaveController(b);
        final Text t = NSISWizardDialogUtil.createText(group, settings.getStartMenuGroup(), "startmenu.group.label", //$NON-NLS-1$
                            true, m, isScriptWizard());
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
        final MasterSlaveController m2 = new MasterSlaveController(b2);
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag)
            {
                m2.updateSlaves(flag);
            }

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

        final Button b3 = NSISWizardDialogUtil.createCheckBox(group, "disable.startmenu.shortcuts.label", //$NON-NLS-1$
                settings.isDisableStartMenuShortcuts(), b2.isEnabled(), m2, false);
        b3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setDisableStartMenuShortcuts(((Button)e.widget).getSelection());
            }
        });

        m.setEnabler(b2,mse);
        m.updateSlaves();
        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                t.setText(mWizard.getSettings().getStartMenuGroup());
                b2.setEnabled(mse.canEnable(b2));
                b3.setEnabled(b2.isEnabled());
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
                b3.setSelection(settings.isDisableStartMenuShortcuts());
                b3.setEnabled(b2.isEnabled());

                m.updateSlaves();
            }
        });
    }
}
