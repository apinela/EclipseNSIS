/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;


import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

public class InstallOptionsSourceActionContributor extends TextEditorActionContributor
{
    private MenuManager mInstallOptionsMenu;
    private RetargetAction mSwitchEditorAction;
    private DropDownAction mPreviewGroupAction;
    private PreviewRetargetAction mPreviewClassicAction;
    private PreviewRetargetAction mPreviewMUIAction;
    private LanguageComboContributionItem mLanguageContributionItem;
    private InstallOptionsWizardAction mWizardAction;
    private InstallOptionsHelpAction mHelpAction;
    
    public void buildActions()
    {
        mWizardAction = new InstallOptionsWizardAction();
        mHelpAction = new InstallOptionsHelpAction();
        mInstallOptionsMenu = new MenuManager(InstallOptionsPlugin.getResourceString("installoptions.menu.name")); //$NON-NLS-1$
        String label = InstallOptionsPlugin.getResourceString("switch.design.editor.action.name"); //$NON-NLS-1$
        mSwitchEditorAction = new RetargetAction(SwitchEditorAction.ID, label); //$NON-NLS-1$
        mSwitchEditorAction.setToolTipText(label);
        mSwitchEditorAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.icon"))); //$NON-NLS-1$
        mSwitchEditorAction.setActionDefinitionId(IInstallOptionsConstants.SWITCH_EDITOR_COMMAND_ID);
        getPage().addPartListener(mSwitchEditorAction);

        mPreviewClassicAction = new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_CLASSIC);
        getPage().addPartListener(mPreviewClassicAction);
        mPreviewMUIAction = new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_MUI);
        getPage().addPartListener(mPreviewMUIAction);
        PreviewRetargetAction[] previewRetargetActions = new PreviewRetargetAction[] {
                                                     mPreviewClassicAction,
                                                     mPreviewMUIAction
                                                 };
        mPreviewGroupAction = new DropDownAction(IInstallOptionsConstants.PREVIEW_GROUP,
                                                 InstallOptionsPlugin.getDefault().getPreferenceStore(),
                                                 previewRetargetActions);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("preview.action.icon")); //$NON-NLS-1$
        mPreviewGroupAction.setImageDescriptor(imageDescriptor);
        mPreviewGroupAction.setHoverImageDescriptor(imageDescriptor);
        mPreviewGroupAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("preview.action.disabled.icon"))); //$NON-NLS-1$
        mPreviewGroupAction.setDetectCurrent(false);
        getPage().addPartListener(mPreviewGroupAction);

        mInstallOptionsMenu.add(mWizardAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(new PreviewSubMenuManager(previewRetargetActions));
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mSwitchEditorAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mHelpAction);
        
        mLanguageContributionItem = new LanguageComboContributionItem(getPage());
    }

    public void contributeToMenu(IMenuManager menu)
    {
        super.contributeToMenu(menu);
        menu.insertBefore(IWorkbenchActionConstants.M_WINDOW, mInstallOptionsMenu);
    }

    public void contributeToToolBar(IToolBarManager tbm)
    {
        tbm.add(mWizardAction);
        tbm.add(new Separator());
        tbm.add(mPreviewGroupAction);
        tbm.add(mLanguageContributionItem);
        tbm.add(new Separator());
        tbm.add(mSwitchEditorAction);
        tbm.add(new Separator());
        tbm.add(mHelpAction);
    }

    public void init(IActionBars bars)
    {
        buildActions();
        super.init(bars);
    }
    
    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        IActionBars bars = getActionBars();
        ITextEditor editor = (part instanceof ITextEditor?(ITextEditor)part:null);
        setGlobalActionHandler(bars, editor, SwitchEditorAction.ID);
        setGlobalActionHandler(bars, editor, PreviewAction.PREVIEW_CLASSIC_ID);
        setGlobalActionHandler(bars, editor, PreviewAction.PREVIEW_MUI_ID);
        bars.updateActionBars();
    }

    private void setGlobalActionHandler(IActionBars bars, ITextEditor editor, String id)
    {
        bars.setGlobalActionHandler(id,(editor == null?null:editor.getAction(id)));
    }
    
    public void dispose()
    {
        getPage().removePartListener(mSwitchEditorAction);
        getPage().removePartListener(mPreviewClassicAction);
        getPage().removePartListener(mPreviewMUIAction);
        getPage().removePartListener(mPreviewGroupAction);
        mSwitchEditorAction.dispose();
        mPreviewGroupAction.dispose();
        mLanguageContributionItem.dispose();
        super.dispose();
    }
}
