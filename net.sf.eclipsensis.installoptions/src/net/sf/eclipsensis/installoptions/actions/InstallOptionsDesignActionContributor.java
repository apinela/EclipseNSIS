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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.WorkbenchImages;

public class InstallOptionsDesignActionContributor extends ActionBarContributor
{
    private MenuManager mInstallOptionsMenu;
    private SetDialogSizeMenuManager mSetDialogSizeMenu;
    private IEditorPart mEditor;
    private ZoomComboContributionItem mZoomComboContributionItem;
    private InstallOptionsXYStatusContribution mXYStatusContribution;

    protected void buildActions() 
    {
        RetargetAction retargetAction;
        
        addRetargetAction(new UndoRetargetAction());
        addRetargetAction(new RedoRetargetAction());

        retargetAction = new RetargetAction(ActionFactory.CUT.getId(),InstallOptionsPlugin.getResourceString("cut.action.name")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("cut.action.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        retargetAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        addRetargetAction(retargetAction);
        
        retargetAction = new RetargetAction(ActionFactory.COPY.getId(),GEFMessages.CopyAction_Label);
        retargetAction.setToolTipText(GEFMessages.CopyAction_Tooltip);
        retargetAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        retargetAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        addRetargetAction(retargetAction);
        
        retargetAction = new RetargetAction(ActionFactory.PASTE.getId(),GEFMessages.PasteAction_Label);
        retargetAction.setToolTipText(GEFMessages.PasteAction_Tooltip);
        retargetAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        retargetAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        addRetargetAction(retargetAction);

        addRetargetAction(new DeleteRetargetAction());
        
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
        
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            addRetargetAction(new ZoomInRetargetAction());
            addRetargetAction(new ZoomOutRetargetAction());
        }
        
        retargetAction = new RetargetAction(SwitchEditorAction.ID, InstallOptionsPlugin.getResourceString("switch.source.editor.action.name")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);
        
        addRetargetAction(new RetargetAction(IInstallOptionsConstants.GRID_SNAP_GLUE_SETTINGS_ACTION_ID,InstallOptionsPlugin.getResourceString("grid.snap.glue.action.name"))); //$NON-NLS-1$

        addRetargetAction(new MatchWidthRetargetAction());
        addRetargetAction(new MatchHeightRetargetAction());
        
        retargetAction = new RetargetAction(
                ToggleDialogSizeVisibilityAction.ID, 
                InstallOptionsPlugin.getResourceString("show.dialog.size.action.name"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.dialog.size.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);
        
        retargetAction = new RetargetAction(
                GEFActionConstants.TOGGLE_RULER_VISIBILITY, 
                GEFMessages.ToggleRulerVisibility_Label, IAction.AS_CHECK_BOX);
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.rulers.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);
        
        retargetAction = new RetargetAction(
                GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY, 
                GEFMessages.ToggleSnapToGeometry_Label, IAction.AS_CHECK_BOX);
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY, 
                GEFMessages.ToggleGrid_Label, IAction.AS_CHECK_BOX);
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.grid.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(ToggleGuideVisibilityAction.ID, 
                InstallOptionsPlugin.getResourceString("show.guides.action.name"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.guides.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            mZoomComboContributionItem = new ZoomComboContributionItem(getPage(), IInstallOptionsConstants.ZOOM_LEVEL_CONTRIBUTIONS);
        }
        
//      addRetargetAction(new DirectEditRetargetAction());

        mInstallOptionsMenu = new MenuManager(InstallOptionsPlugin.getResourceString("installoptions.menu.name")); //$NON-NLS-1$
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            mInstallOptionsMenu.add(getAction(GEFActionConstants.ZOOM_IN));
            mInstallOptionsMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
            mInstallOptionsMenu.add(new Separator());
        }
        mInstallOptionsMenu.add(getAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY));
        mInstallOptionsMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
        mInstallOptionsMenu.add(getAction(ToggleDialogSizeVisibilityAction.ID));
        mInstallOptionsMenu.add(getAction(ToggleGuideVisibilityAction.ID));
        mInstallOptionsMenu.add(getAction(IInstallOptionsConstants.GRID_SNAP_GLUE_SETTINGS_ACTION_ID));
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(getAction(GEFActionConstants.MATCH_WIDTH));
        mInstallOptionsMenu.add(getAction(GEFActionConstants.MATCH_HEIGHT));
        mInstallOptionsMenu.add(new Separator());
        mSetDialogSizeMenu = new SetDialogSizeMenuManager(mInstallOptionsMenu);
        mInstallOptionsMenu.add(mSetDialogSizeMenu);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(getAction(SwitchEditorAction.ID));
        
        mXYStatusContribution = new InstallOptionsXYStatusContribution();
    }

    public void setActiveEditor(IEditorPart editor)
    {
        mEditor = editor;
        mSetDialogSizeMenu.setEditor(editor);
        mXYStatusContribution.editorChanged(editor);
        super.setActiveEditor(editor);
    }

    protected void declareGlobalActionKeys() 
    {
        addGlobalActionKey(ActionFactory.PRINT.getId());
        addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
    }

    public void contributeToToolBar(IToolBarManager tbm) 
    {
        tbm.add(getAction(ActionFactory.UNDO.getId()));
        tbm.add(getAction(ActionFactory.REDO.getId()));
        tbm.add(new Separator());
        tbm.add(getAction(ActionFactory.CUT.getId()));
        tbm.add(getAction(ActionFactory.COPY.getId()));
        tbm.add(getAction(ActionFactory.PASTE.getId()));
        tbm.add(getAction(ActionFactory.DELETE.getId()));
        
        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
        tbm.add(getAction(GEFActionConstants.ALIGN_CENTER));
        tbm.add(getAction(GEFActionConstants.ALIGN_RIGHT));
        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
        tbm.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
        tbm.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
        
        tbm.add(new Separator());   
        tbm.add(getAction(GEFActionConstants.MATCH_WIDTH));
        tbm.add(getAction(GEFActionConstants.MATCH_HEIGHT));
        
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            tbm.add(new Separator());   
            tbm.add(mZoomComboContributionItem);
        }
    }

    public void contributeToMenu(IMenuManager menubar) 
    {
        menubar.insertBefore(IWorkbenchActionConstants.M_WINDOW, mInstallOptionsMenu);
    }

    public void contributeToStatusLine(IStatusLineManager statusLineManager) 
    {
        statusLineManager.add(mXYStatusContribution);
    }
}
