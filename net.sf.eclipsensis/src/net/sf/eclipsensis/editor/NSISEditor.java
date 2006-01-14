/*****************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.actions.NSISAction;
import net.sf.eclipsensis.actions.NSISScriptAction;
import net.sf.eclipsensis.editor.outline.*;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class NSISEditor extends TextEditor implements INSISConstants, INSISHomeListener, ISelectionChangedListener, IProjectionListener
{
    private Set mActions = new HashSet();
    private ProjectionSupport mProjectionSupport;
    private NSISContentOutlinePage mOutlinePage;
    private NSISOutlineContentProvider mOutlineContentProvider;
    private Position mCurrentPosition = null;
    private Mutex mMutex = new Mutex();

    /**
     *
     */
    public NSISEditor()
    {
        super();
        setHelpContextId(PLUGIN_CONTEXT_PREFIX + "nsis_editor_context"); //$NON-NLS-1$;
    }

    public void selectionChanged(SelectionChangedEvent event)
    {
        Object source = event.getSource();
        ISelection selection = event.getSelection();
        ISourceViewer sourceViewer = getSourceViewer();
        boolean acquiredMutex = mMutex.acquireWithoutBlocking(source);
        try {
            if(source.equals(sourceViewer) && selection instanceof ITextSelection) {
                IAction action = getAction(INSISEditorConstants.ADD_BLOCK_COMMENT); 
                if(action != null) {
                    action.setEnabled(sourceViewer.getSelectedRange().y > 0);
                }

                if(mOutlineContentProvider != null) {
                    if(acquiredMutex) {
                        ITextSelection textSelection = (ITextSelection)selection;
                        IStructuredSelection sel = StructuredSelection.EMPTY;
                        NSISOutlineElement element = mOutlineContentProvider.findElement(textSelection.getOffset(),textSelection.getLength());
                        if(element != null) {
                            Position position = element.getPosition();
                            if(position.equals(mCurrentPosition)) {
                                return;
                            }
                            else {
                                if(mOutlinePage != null) {
                                    if(!mOutlinePage.isDisposed()) {
                                        sel = new StructuredSelection(element);
                                        mOutlinePage.setSelection(sel);
                                        return;
                                    }
                                    else {
                                        mOutlinePage = null;
                                    }
                                }

                                mCurrentPosition = position;
                                setHighlightRange(mCurrentPosition.getOffset(),
                                                  mCurrentPosition.getLength(),
                                                  false);
                            }
                        }
                    }
                    else {
                        return;
                    }
                }
                mCurrentPosition = null;
            }
            else if(source instanceof TreeViewer) {
                if (selection.isEmpty()) {
                    mCurrentPosition = null;
                    resetHighlightRange();
                }
                else {
                    NSISOutlineElement element = (NSISOutlineElement) ((IStructuredSelection) selection).getFirstElement();
                    Position position = element.getPosition();
                    if(mCurrentPosition == null || !position.equals(mCurrentPosition)) {
                        mCurrentPosition = position;
                        try {
                            boolean moveCursor = acquiredMutex;
//                        ISelection sel = getSelectionProvider().getSelection();
//                        if(sel != null && sel instanceof ITextSelection) {
//                            int offset = ((ITextSelection)sel).getOffset();
//                            if(position.includes(offset)) {
//                                moveCursor = false;
//                            }
//                        }

                            setHighlightRange(mCurrentPosition.getOffset(),
                                              mCurrentPosition.getLength(),
                                              moveCursor);
                        }
                        catch (IllegalArgumentException x) {
                            resetHighlightRange();
                        }
                    }

                    if(acquiredMutex) {
                        Position selectPosition = element.getSelectPosition();
                        if(selectPosition != null) {
                            sourceViewer.setSelectedRange(selectPosition.getOffset(),selectPosition.getLength());
                        }
                    }
                }
            }
        }
        finally {
            if(acquiredMutex) {
                mMutex.release(source);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
     */
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] { NSIS_EDITOR_CONTEXT_ID });
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
        mProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.task"); //$NON-NLS-1$
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.bookmark"); //$NON-NLS-1$
        mProjectionSupport.install();
        NSISPreferences.INSTANCE.addListener(this);
        if(viewer.canDoOperation(ProjectionViewer.TOGGLE)) {
            viewer.doOperation(ProjectionViewer.TOGGLE);
        }
        mOutlineContentProvider = new NSISOutlineContentProvider(this);
        getSelectionProvider().addSelectionChangedListener(this);
        viewer.addPostSelectionChangedListener(this);
        updateAnnotations();
    }

    protected void createActions()
    {
        super.createActions();
        ResourceBundle resourceBundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        IAction a= new TextOperationAction(resourceBundle, "content.assist.proposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
        a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction(INSISEditorConstants.CONTENT_ASSIST_PROPOSAL, a); 

        a = new TextOperationAction(resourceBundle,"insert.template.",this,NSISSourceViewer.INSERT_TEMPLATE,true); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_TEMPLATE_COMMAND_ID);
        setAction(INSISEditorConstants.INSERT_TEMPLATE, a); 

        a = new TextOperationAction(resourceBundle,"goto.help.",this,NSISSourceViewer.GOTO_HELP,true); //$NON-NLS-1$
        a.setActionDefinitionId(GOTO_HELP_COMMAND_ID);
        setAction(INSISEditorConstants.GOTO_HELP, a); 

        a = new TextOperationAction(resourceBundle,"sticky.help.",this,ISourceViewer.INFORMATION,true); //$NON-NLS-1$
        a = new NSISStickyHelpAction(resourceBundle,"sticky.help.",(TextOperationAction)a); //$NON-NLS-1$
        a.setActionDefinitionId(STICKY_HELP_COMMAND_ID);
        setAction(INSISEditorConstants.STICKY_HELP, a); 

        a = new TextOperationAction(resourceBundle,"insert.file.",this,NSISSourceViewer.INSERT_FILE,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_FILE_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.file.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_FILE, a); 

        a = new TextOperationAction(resourceBundle,"insert.directory.",this,NSISSourceViewer.INSERT_DIRECTORY,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_DIRECTORY_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.directory.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_DIRECTORY, a); 

        a = new TextOperationAction(resourceBundle,"insert.color.",this,NSISSourceViewer.INSERT_COLOR,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_COLOR_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.color.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_COLOR, a); 

        a = new TextOperationAction(resourceBundle,"import.regfile.",this,NSISSourceViewer.IMPORT_REGFILE,false); //$NON-NLS-1$
        a.setActionDefinitionId(IMPORT_REGFILE_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("import.regfile.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.IMPORT_REGFILE, a); 

        a = new TextOperationAction(resourceBundle,"import.regkey.",this,NSISSourceViewer.IMPORT_REGKEY,false); //$NON-NLS-1$
        a.setActionDefinitionId(IMPORT_REGKEY_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("import.regkey.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.IMPORT_REGKEY, a); 

        a = new TextOperationAction(resourceBundle,"tabs.to.spaces.",this,NSISSourceViewer.TABS_TO_SPACES,false); //$NON-NLS-1$
        a.setActionDefinitionId(TABS_TO_SPACES_COMMAND_ID);
        setAction(INSISEditorConstants.TABS_TO_SPACES, a); 

        a = new TextOperationAction(resourceBundle,"toggle.comment.",this,NSISSourceViewer.TOGGLE_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(TOGGLE_COMMENT_COMMAND_ID);
        setAction(INSISEditorConstants.TOGGLE_COMMENT, a); 

        a = new TextOperationAction(resourceBundle,"add.block.comment.",this,NSISSourceViewer.ADD_BLOCK_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(ADD_BLOCK_COMMENT_COMMAND_ID);
        setAction(INSISEditorConstants.ADD_BLOCK_COMMENT, a); 

        a = new TextOperationAction(resourceBundle,"remove.block.comment.",this,NSISSourceViewer.REMOVE_BLOCK_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(REMOVE_BLOCK_COMMENT_COMMAND_ID);
        setAction(INSISEditorConstants.REMOVE_BLOCK_COMMENT, a); 

        a= new TextOperationAction(resourceBundle, "projection.toggle.", this, ProjectionViewer.TOGGLE, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_TOGGLE, a); 

        a = new TextOperationAction(resourceBundle, "projection.expand.all.", this, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_EXPAND_ALL, a); 

        a= new TextOperationAction(resourceBundle, "projection.expand.", this, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_EXPAND, a); 

        a= new TextOperationAction(resourceBundle, "projection.collapse.", this, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_COLLAPSE, a); 

//        a= new TextOperationAction(resourceBundle, "projection.collapse.all.", this, ProjectionViewer.COLLAPSE_ALL, true); //$NON-NLS-1$
//        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
//        a.setEnabled(true);
//        setAction("CollapseAll", a); //$NON-NLS-1$
//
//        a= new TextOperationAction(resourceBundle, "projection.restore.", this, ProjectionViewer.RESTORE, true); //$NON-NLS-1$
//        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_RESTORE);
//        a.setEnabled(true);
//        setAction("CollapseAll", a); //$NON-NLS-1$
    }

    public void dispose()
    {
        mCurrentPosition = null;
        if (mOutlinePage != null) {
            mOutlinePage.setInput(null);
            mOutlinePage.dispose();
            mOutlinePage = null;
        }
        if (mOutlineContentProvider != null) {
            mOutlineContentProvider.inputChanged(getEditorInput(),null);
            mOutlineContentProvider.dispose();
            mOutlineContentProvider = null;
        }
        getSelectionProvider().removeSelectionChangedListener(this);
        ProjectionViewer viewer = ((ProjectionViewer)getSourceViewer());
        viewer.removeProjectionListener(this);
        viewer.removePostSelectionChangedListener(this);
        NSISPreferences.INSTANCE.removeListener(this);
        super.dispose();
    }

    public void doSetInput(IEditorInput input) throws CoreException
    {
        IEditorInput oldInput = getEditorInput();
        super.doSetInput(input);
        if (mOutlinePage != null) {
            mCurrentPosition = null;
            mOutlinePage.setInput(input);
        }
        else if (mOutlineContentProvider != null){
            mOutlineContentProvider.inputChanged(oldInput,null);
        }
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        menu.add(new Separator());
        addAction(menu, INSISEditorConstants.CONTENT_ASSIST_PROPOSAL); 
        addAction(menu, INSISEditorConstants.INSERT_TEMPLATE); 
        menu.add(new Separator());
        addAction(menu, INSISEditorConstants.TABS_TO_SPACES); 
        addAction(menu, INSISEditorConstants.TOGGLE_COMMENT); 
        addAction(menu, INSISEditorConstants.ADD_BLOCK_COMMENT); 
        IAction action = getAction(INSISEditorConstants.ADD_BLOCK_COMMENT); 
        action.setEnabled(getSourceViewer().getSelectedRange().y > 0);

        addAction(menu, INSISEditorConstants.REMOVE_BLOCK_COMMENT); 
        menu.add(new Separator());
        addAction(menu, INSISEditorConstants.INSERT_FILE); 
        addAction(menu, INSISEditorConstants.INSERT_DIRECTORY); 
        addAction(menu, INSISEditorConstants.INSERT_COLOR); 
        addAction(menu, INSISEditorConstants.IMPORT_REGFILE); 
        addAction(menu, INSISEditorConstants.IMPORT_REGKEY); 
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
     */
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        fAnnotationAccess= createAnnotationAccess();
        fOverviewRuler= createOverviewRuler(getSharedColors());

        NSISSourceViewer viewer= new NSISSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
        // ensure decoration support has been created and configured.
        SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(viewer);
        decorationSupport.setCharacterPairMatcher(new NSISCharacterPairMatcher());
        decorationSupport.setMatchingCharacterPainterPreferenceKeys(INSISPreferenceConstants.MATCHING_DELIMITERS,
                                                                    INSISPreferenceConstants.MATCHING_DELIMITERS_COLOR);
        viewer.addProjectionListener(this);
        return viewer;
    }

    public void projectionDisabled()
    {
    }

    public void projectionEnabled()
    {
        if(mOutlineContentProvider != null) {
            mOutlineContentProvider.refresh();
        }
    }

    /*
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int, int)
     */
    protected void adjustHighlightRange(int offset, int length)
    {
        ISourceViewer viewer= getSourceViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }

    public Point getSelectedRange()
    {
        ISourceViewer sourceViewer = getSourceViewer();
        if(sourceViewer != null) {
            return sourceViewer.getSelectedRange();
        }
        else {
            return new Point(0,0);
        }
    }

    public Object getAdapter(Class required)
    {
        ISourceViewer sourceViewer = getSourceViewer();
        if (IContentOutlinePage.class.equals(required)) {
            if (mOutlinePage == null || mOutlinePage.isDisposed()) {
                mCurrentPosition = null;
                mOutlinePage= new NSISContentOutlinePage(this);
                if (getEditorInput() != null) {
                    mOutlinePage.setInput(getEditorInput());
                }
            }
            return mOutlinePage;
        }

        if (mProjectionSupport != null) {
            Object adapter= mProjectionSupport.getAdapter(sourceViewer, required);
            if (adapter != null) {
                return adapter;
            }
        }

        return super.getAdapter(required);
    }

    /* (non-Javadoc)
     * Method declared on AbstractTextEditor
     */
    protected void initializeEditor()
    {
        super.initializeEditor();
        IPreferenceStore preferenceStore = NSISPreferences.INSTANCE.getPreferenceStore();
        preferenceStore = new ChainedPreferenceStore(new IPreferenceStore[]{preferenceStore, EditorsUI.getPreferenceStore()});
        setPreferenceStore(preferenceStore);
        setSourceViewerConfiguration(new NSISEditorSourceViewerConfiguration(preferenceStore));
    }

    public void addAction(NSISAction action)
    {
        mActions.add(action);
    }

    public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
    {
        if(monitor != null) {
            monitor.subTask(EclipseNSISPlugin.getResourceString("updating.actions.message")); //$NON-NLS-1$
        }
        updateActionsState();
    }

    void updatePresentation()
    {
        try {
            ISourceViewer sourceViewer = getSourceViewer();
            if(sourceViewer instanceof NSISSourceViewer) {
                NSISSourceViewer viewer = (NSISSourceViewer)sourceViewer;
                if(viewer.mustProcessPropertyQueue()) {
                    viewer.processPropertyQueue();
                }
            }
        }
        catch(Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
        }
    }

    public void updateActionsState()
    {
        if(equals(getEditorSite().getPage().getActiveEditor())) {
            for(Iterator iter=mActions.iterator(); iter.hasNext(); ) {
                IActionDelegate action = (IActionDelegate)iter.next();
                if(action instanceof NSISScriptAction) {
                    ((NSISScriptAction)action).updateActionState();
                }
            }
        }
    }

    protected String[] collectContextMenuPreferencePages()
    {
        String[] pages = {EDITOR_PREFERENCE_PAGE_ID, TEMPLATES_PREFERENCE_PAGE_ID, TASKTAGS_PREFERENCE_PAGE_ID};
        return (String[])Common.joinArrays(new Object[]{pages,super.collectContextMenuPreferencePages()});
    }

    protected void performSaveAs(IProgressMonitor progressMonitor)
    {
        super.performSaveAs(progressMonitor);
        updateTaskTagMarkers(new NSISTaskTagUpdater());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorSaved()
     */
    protected void editorSaved()
    {
        super.editorSaved();
        updateOutlinePage();
        updateActionsState();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation()
        {
            protected void execute(IProgressMonitor monitor)throws CoreException
            {
                NSISTaskTagUpdater taskTagUpdater = new NSISTaskTagUpdater();
                updateTaskTagMarkers(taskTagUpdater);
            }
        };
        try {
            op.run(null);
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    IAnnotationModel getAnnotationModel()
    {
        ISourceViewer viewer = getSourceViewer();
        if(viewer != null) {
            return viewer.getAnnotationModel();
        }
        return null;
    }

    /**
     * @param file
     */
    public void updateTaskTagMarkers(NSISTaskTagUpdater taskTagUpdater)
    {
        IEditorInput editorInput = getEditorInput();
        if(editorInput instanceof IFileEditorInput) {
            taskTagUpdater.updateTaskTags(((IFileEditorInput)editorInput).getFile(), getSourceViewer().getDocument());
        }
    }

    /**
     *
     */
    private void updateOutlinePage()
    {
        if (mOutlinePage != null) {
            mCurrentPosition = null;
            mOutlinePage.update();
        }
    }

    private void updateAnnotations()
    {
        IEditorInput input = getEditorInput();
        if(input instanceof IPathEditorInput && !(input instanceof IFileEditorInput)){
            File file = new File(((IPathEditorInput)input).getPath().toOSString());
            if(IOUtility.isValidFile(file)) {
                MakeNSISResults results = NSISCompileTestUtility.INSTANCE.getCachedResults(file);
                if(results != null) {
                    NSISEditorUtilities.updateAnnotations(this, results);
                }
            }
        }
    }

    /**
     * @return Returns the outlineContentProvider.
     */
    public NSISOutlineContentProvider getOutlineContentProvider()
    {
        return mOutlineContentProvider;
    }

    private class NSISStickyHelpAction extends TextEditorAction
    {
        private final TextOperationAction mTextOperationAction;
        private InformationPresenter mInformationPresenter;

        public NSISStickyHelpAction(ResourceBundle resourceBundle, String prefix, final TextOperationAction textOperationAction) {
            super(resourceBundle, prefix, NSISEditor.this);
            if (textOperationAction == null) {
                throw new IllegalArgumentException();
            }
            mTextOperationAction= textOperationAction;
        }

        private InformationPresenter getInformationPresenter()
        {
            if(mInformationPresenter == null) {
                mInformationPresenter = NSISEditorUtilities.createStickyHelpInformationPresenter();
            }
            return mInformationPresenter;
        }
        /*
         *  @see org.eclipse.jface.action.IAction#run()
         */
        public void run()
        {
            if(NSISHelpURLProvider.getInstance().isNSISHelpAvailable()) {
                ISourceViewer sourceViewer = getSourceViewer();
                int offset = NSISTextUtility.computeOffset(sourceViewer,true);
                if(offset == -1) {
                    mTextOperationAction.run();
                }
                else {
                    InformationPresenter informationPresenter = getInformationPresenter();
                    informationPresenter.install(sourceViewer);
                    informationPresenter.setOffset(offset); //wordRegion.getOffset());
                    informationPresenter.showInformation();
                }
            }
        }
    }
}
