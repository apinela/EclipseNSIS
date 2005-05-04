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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.SetDialogSizeMenuManager;

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
        addContextMenu(manager,GEFActionConstants.DIRECT_EDIT, GEFActionConstants.GROUP_EDIT);

        // Alignment Actions
        MenuManager submenu = new MenuManager(InstallOptionsPlugin.getResourceString("alignment.submenu.name")); //$NON-NLS-1$

        addContextMenu(submenu, GEFActionConstants.ALIGN_LEFT, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_CENTER, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_RIGHT, null);
        submenu.add(new Separator());

        addContextMenu(submenu, GEFActionConstants.ALIGN_TOP, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_MIDDLE, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_BOTTOM, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_REST, submenu);
        }

        if(mEditor != null) {
            if(mSetDialogSizeMenu == null) {
                mSetDialogSizeMenu = new SetDialogSizeMenuManager(manager);
                mSetDialogSizeMenu.setEditor(mEditor);
            }
            mSetDialogSizeMenu.rebuild();
            if(!mSetDialogSizeMenu.isEmpty()) {
                manager.appendToGroup(GEFActionConstants.GROUP_REST, mSetDialogSizeMenu);
            }
        }
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