/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;


import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.util.CommonImages;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

public class InstallOptionsSourceActionContributor extends TextEditorActionContributor
{
    private MenuManager mInstallOptionsMenu;
    private List mRetargetActions = new ArrayList();
    private RetargetAction mExportHTMLAction;
    private RetargetAction mCreateControlAction;
    private RetargetAction mEditControlAction;
    private RetargetAction mDeleteControlAction;
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
        
        String label = InstallOptionsPlugin.getResourceString("switch.design.editor.action.name"); //$NON-NLS-1$);
        mSwitchEditorAction = new RetargetAction(SwitchEditorAction.ID, label);
        mSwitchEditorAction.setToolTipText(label);
        mSwitchEditorAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.icon"))); //$NON-NLS-1$
        mSwitchEditorAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.disabled.icon"))); //$NON-NLS-1$
        registerRetargetAction(mSwitchEditorAction);

        mExportHTMLAction = new RetargetAction(InstallOptionsSourceEditor.EXPORT_HTML_ACTION,"E&xport as HTML");
        mExportHTMLAction.setToolTipText("Export the InstallOptions script as an HTML file");
        mExportHTMLAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPORT_HTML_ICON));
        mExportHTMLAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPORT_HTML_DISABLED_ICON));        
        registerRetargetAction(mExportHTMLAction);
        
        mCreateControlAction = new RetargetAction("net.sf.eclipsensis.installoptions.create_control","&Create Control");
        mCreateControlAction.setToolTipText("Create an InstallOptions control in the source editor");
        mCreateControlAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.control.icon"))); //$NON-NLS-1$
        mCreateControlAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.control.disabled.icon"))); //$NON-NLS-1$
        registerRetargetAction(mCreateControlAction);
        
        mEditControlAction = new RetargetAction("net.sf.eclipsensis.installoptions.edit_control","&Edit Control");
        mEditControlAction.setToolTipText("Edit an InstallOptions control in the source editor");
        mEditControlAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("edit.control.icon"))); //$NON-NLS-1$
        mEditControlAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("edit.control.disabled.icon"))); //$NON-NLS-1$
        registerRetargetAction(mEditControlAction);
        
        mDeleteControlAction = new RetargetAction("net.sf.eclipsensis.installoptions.delete_control","&Delete Control");
        mDeleteControlAction.setToolTipText("Delete an InstallOptions control in the source editor");
        mDeleteControlAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.icon"))); //$NON-NLS-1$
        mDeleteControlAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.disabled.icon"))); //$NON-NLS-1$
        registerRetargetAction(mDeleteControlAction);
        
        mPreviewClassicAction = new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_CLASSIC);
        registerRetargetAction(mPreviewClassicAction);
        mPreviewMUIAction = new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_MUI);
        registerRetargetAction(mPreviewMUIAction);
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
        mInstallOptionsMenu.add(mCreateControlAction);
        mInstallOptionsMenu.add(mEditControlAction);
        mInstallOptionsMenu.add(mDeleteControlAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(new PreviewSubMenuManager(previewRetargetActions));
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mExportHTMLAction);
        mInstallOptionsMenu.add(mSwitchEditorAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mHelpAction);

        mLanguageContributionItem = new LanguageComboContributionItem(getPage());
    }

    /**
     * 
     */
    private void registerRetargetAction(RetargetAction action)
    {
        getPage().addPartListener(action);
        mRetargetActions.add(action);
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
        tbm.add(mCreateControlAction);
        tbm.add(mEditControlAction);
        tbm.add(mDeleteControlAction);
        tbm.add(new Separator());
        tbm.add(mPreviewGroupAction);
        tbm.add(mLanguageContributionItem);
        tbm.add(new Separator());
        tbm.add(mExportHTMLAction);
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
        for (Iterator iter = mRetargetActions.iterator(); iter.hasNext();) {
            String id = ((IAction)iter.next()).getId();
			bars.setGlobalActionHandler(id,(editor == null?null:editor.getAction(id)));
        }
        bars.updateActionBars();
    }

    public void dispose()
    {
        for (Iterator iter = mRetargetActions.iterator(); iter.hasNext();) {
            RetargetAction action = (RetargetAction)iter.next();
            getPage().removePartListener(action);
            action.dispose();
        }
        mPreviewGroupAction.dispose();
        mLanguageContributionItem.dispose();
        super.dispose();
    }
}
