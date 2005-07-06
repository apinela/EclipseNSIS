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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

public class InstallOptionsSourceActionContributor extends TextEditorActionContributor
{
    private MenuManager mInstallOptionsMenu;
    private RetargetAction mSwitchEditorAction;
    
    public InstallOptionsSourceActionContributor()
    {
        super();
        mInstallOptionsMenu = new MenuManager(InstallOptionsPlugin.getResourceString("installoptions.menu.name")); //$NON-NLS-1$
        mSwitchEditorAction = new RetargetAction(SwitchEditorAction.ID, InstallOptionsPlugin.getResourceString("switch.design.editor.action.name")); //$NON-NLS-1$
        mSwitchEditorAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.icon"))); //$NON-NLS-1$
        mSwitchEditorAction.setActionDefinitionId(IInstallOptionsConstants.SWITCH_EDITOR_COMMAND_ID);

        mInstallOptionsMenu.add(mSwitchEditorAction);
    }

    public void contributeToMenu(IMenuManager menu)
    {
        super.contributeToMenu(menu);
        menu.insertBefore(IWorkbenchActionConstants.M_WINDOW, mInstallOptionsMenu);
    }

    public void init(IActionBars bars)
    {
        super.init(bars);
        getPage().addPartListener(mSwitchEditorAction);
    }
    
    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        IActionBars bars = getActionBars();
        ITextEditor editor = (part instanceof ITextEditor?(ITextEditor)part:null);
        bars.setGlobalActionHandler(SwitchEditorAction.ID,(editor == null?null:editor.getAction(SwitchEditorAction.ID)));
        bars.updateActionBars();
    }
    
    public void dispose()
    {
        getPage().removePartListener(mSwitchEditorAction);
        mSwitchEditorAction.dispose();
        super.dispose();
    }
}
