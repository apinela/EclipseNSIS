/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.*;
import net.sf.eclipsensis.installoptions.edit.*;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.*;
import org.eclipse.ui.actions.ActionFactory;

public class InstallOptionsDesignMenuProvider extends org.eclipse.gef.ContextMenuProvider
{
    private ActionRegistry mActionRegistry;
    private InstallOptionsDesignEditor mEditor;
    private SetDialogSizeMenuManager mSetDialogSizeMenu;

    public InstallOptionsDesignMenuProvider(InstallOptionsDesignEditor editor, ActionRegistry registry)
    {
        this(editor.getGraphicalViewer(), registry);
        mEditor = editor;
    }

    public InstallOptionsDesignMenuProvider(EditPartViewer viewer, ActionRegistry registry)
    {
        super(viewer);
        setActionRegistry(registry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.ContextMenuProvider#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void buildContextMenu(IMenuManager manager)
    {
        GEFActionConstants.addStandardActionGroups(manager);

        IAction action;

        addContextMenu(manager, ActionFactory.UNDO.getId(), GEFActionConstants.GROUP_UNDO);
        addContextMenu(manager, ActionFactory.REDO.getId(), GEFActionConstants.GROUP_UNDO);
        addContextMenu(manager, ActionFactory.CUT.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ActionFactory.COPY.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ActionFactory.PASTE.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ActionFactory.DELETE.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ToggleEnablementAction.ID, GEFActionConstants.GROUP_EDIT);
        
        List selected = getViewer().getSelectedEditParts();
        if(selected.size() == 1) {
            EditPart editPart = (EditPart)selected.get(0);
            if(editPart instanceof InstallOptionsWidgetEditPart) {
                action = getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT);
                if (action != null && action.isEnabled()) {
                    IDirectEditLabelProvider labelProvider = (IDirectEditLabelProvider)((InstallOptionsWidgetEditPart)editPart).getAdapter(IDirectEditLabelProvider.class);
                    String label;
                    if(labelProvider != null) {
                        label = labelProvider.getDirectEditLabel();
                    }
                    else {
                        label = InstallOptionsPlugin.getResourceString("direct.edit.label"); //$NON-NLS-1$
                    }
                    action.setText(label);
                    action.setToolTipText(label);
                    manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
                }

                action = getActionRegistry().getAction(ExtendedEditAction.ID);
                if (action != null && action.isEnabled()) {
                    IExtendedEditLabelProvider labelProvider = (IExtendedEditLabelProvider)((InstallOptionsWidgetEditPart)editPart).getAdapter(IExtendedEditLabelProvider.class);
                    String label;
                    if(labelProvider != null) {
                        label = labelProvider.getExtendedEditLabel();
                    }
                    else {
                        label = InstallOptionsPlugin.getResourceString("extended.edit.label"); //$NON-NLS-1$
                    }
                    action.setText(label);
                    action.setToolTipText(label);
                    manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
                }
            }
        }

        // Alignment Actions
        MenuManager submenu = new MenuManager(InstallOptionsPlugin.getResourceString("align.submenu.name")); //$NON-NLS-1$

        addContextMenu(submenu, GEFActionConstants.ALIGN_LEFT, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_CENTER, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_RIGHT, null);
        submenu.add(new Separator());

        addContextMenu(submenu, GEFActionConstants.ALIGN_TOP, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_MIDDLE, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_BOTTOM, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("match.submenu.name")); //$NON-NLS-1$
        addContextMenu(submenu, GEFActionConstants.MATCH_WIDTH, null);
        addContextMenu(submenu, GEFActionConstants.MATCH_HEIGHT, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("arrange.submenu.name")); //$NON-NLS-1$

        addContextMenu(submenu, ArrangeAction.SEND_BACKWARD_ID, null);
        addContextMenu(submenu, ArrangeAction.SEND_TO_BACK_ID, null);
        addContextMenu(submenu, ArrangeAction.BRING_FORWARD_ID, null);
        addContextMenu(submenu, ArrangeAction.BRING_TO_FRONT_ID, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }
        
        if(mEditor != null && selected.size() == 0) {
            if(mSetDialogSizeMenu == null) {
                mSetDialogSizeMenu = new SetDialogSizeMenuManager(manager);
                mSetDialogSizeMenu.setEditor(mEditor);
            }
            mSetDialogSizeMenu.rebuild();
            if(!mSetDialogSizeMenu.isEmpty()) {
                manager.appendToGroup(GEFActionConstants.GROUP_EDIT, mSetDialogSizeMenu);
            }
        }
        addContextMenu(manager, RefreshDiagramAction.ID, GEFActionConstants.GROUP_EDIT);
        
        addContextMenu(manager, "net.sf.eclipsensis.installoptions.design_editor_prefs", GEFActionConstants.GROUP_REST); //$NON-NLS-1$
        addContextMenu(manager, ActionFactory.SAVE.getId(), GEFActionConstants.GROUP_SAVE);
    }

    /**
     * @param manager
     */
    private void addContextMenu(IMenuManager manager, String id, String group)
    {
        IAction action;
        action = getActionRegistry().getAction(id);
        if (action != null && action.isEnabled()) {
            if(group != null) {
                manager.appendToGroup(group, action);
            }
            else {
                manager.add(action);
            }
        }
    }

    private ActionRegistry getActionRegistry()
    {
        return mActionRegistry;
    }

    private void setActionRegistry(ActionRegistry registry)
    {
        mActionRegistry = registry;
    }

}