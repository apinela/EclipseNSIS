/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * Contributes interesting NSIS actions to the desktop's Edit menu and the toolbar.
 */
public class NSISActionContributor extends TextEditorActionContributor implements INSISConstants
{
	protected RetargetTextEditorAction mContentAssistProposal;
    protected RetargetTextEditorAction mTabsToSpaces;
    protected RetargetTextEditorAction mInsertFile;
    protected RetargetTextEditorAction mInsertDirectory;
    protected RetargetTextEditorAction mInsertColor;
//	protected RetargetTextEditorAction mContentAssistTip;

	/**
	 * Default constructor.
	 */
	public NSISActionContributor() 
    {
		super();
		ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        mContentAssistProposal= new RetargetTextEditorAction(bundle, "content.assist.proposal."); //$NON-NLS-1$
		mContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        
        mTabsToSpaces= new RetargetTextEditorAction(bundle, "tabs.to.spaces."); //$NON-NLS-1$
        mTabsToSpaces.setActionDefinitionId(TABS_TO_SPACES_COMMAND_ID);
        
        mInsertFile= new RetargetTextEditorAction(bundle, "insert.file."); //$NON-NLS-1$
        mInsertFile.setImageDescriptor(ImageManager.getImageDescriptor(bundle.getString("insert.file.image"))); //$NON-NLS-1$
        mInsertFile.setActionDefinitionId(INSERT_FILE_COMMAND_ID);
        
        mInsertDirectory= new RetargetTextEditorAction(bundle, "insert.directory."); //$NON-NLS-1$
        mInsertDirectory.setImageDescriptor(ImageManager.getImageDescriptor(bundle.getString("insert.directory.image"))); //$NON-NLS-1$
        mInsertDirectory.setActionDefinitionId(INSERT_DIRECTORY_COMMAND_ID);
        
        mInsertColor= new RetargetTextEditorAction(bundle, "insert.color."); //$NON-NLS-1$
        mInsertColor.setImageDescriptor(ImageManager.getImageDescriptor(bundle.getString("insert.color.image"))); //$NON-NLS-1$
        mInsertColor.setActionDefinitionId(INSERT_COLOR_COMMAND_ID);
        
/*		mContentAssistTip= new RetargetTextEditorAction(EclipseNSISPlugin.getDefault().getResourceBundle(), "ContentAssistTip."); //$NON-NLS-1$
		mContentAssistTip.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
*/	}
	
	/*
	 * @see IEditorActionBarContributor#init(IActionBars)
	 */
	public void init(IActionBars bars) 
    {
		super.init(bars);
		
		IMenuManager menuManager= bars.getMenuManager();
		IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(mContentAssistProposal);
            editMenu.add(mTabsToSpaces);
            editMenu.add(new Separator());
            editMenu.add(mInsertFile);
            editMenu.add(mInsertDirectory);
            editMenu.add(mInsertColor);
//			editMenu.add(mContentAssistTip);
		}	
	}
	
	private void doSetActiveEditor(IEditorPart part) 
    {
		super.setActiveEditor(part);

		ITextEditor editor= null;
		if (part instanceof ITextEditor)
			editor= (ITextEditor) part;

		mContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal")); //$NON-NLS-1$
        mTabsToSpaces.setAction(getAction(editor, "NSISTabsToSpaces")); //$NON-NLS-1$
        mInsertFile.setAction(getAction(editor, "NSISInsertFile")); //$NON-NLS-1$
        mInsertDirectory.setAction(getAction(editor, "NSISInsertDirectory")); //$NON-NLS-1$
        mInsertColor.setAction(getAction(editor, "NSISInsertColor")); //$NON-NLS-1$
//		mContentAssistTip.setAction(getAction(editor, "ContentAssistTip")); //$NON-NLS-1$
	}
	
	/*
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) 
    {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}
	
	/*
	 * @see IEditorActionBarContributor#dispose()
	 */
	public void dispose() 
    {
		doSetActiveEditor(null);
		super.dispose();
	}
}
