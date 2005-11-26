/*****************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.editor.codeassist.NSISInformationControlCreator;
import net.sf.eclipsensis.editor.codeassist.NSISInformationProvider;
import net.sf.eclipsensis.editor.outline.*;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
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
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class NSISEditor extends TextEditor implements INSISConstants, INSISHomeListener, ISelectionChangedListener
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
                IAction action = getAction("NSISAddBlockComment"); //$NON-NLS-1$
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
        setAction("ContentAssistProposal", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"insert.template.",this,NSISSourceViewer.INSERT_TEMPLATE,true); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_TEMPLATE_COMMAND_ID);
        setAction("NSISInsertTemplate", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"goto.help.",this,NSISSourceViewer.GOTO_HELP,true); //$NON-NLS-1$
        a.setActionDefinitionId(GOTO_HELP_COMMAND_ID);
        setAction("NSISGotoHelp", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"sticky.help.",this,ISourceViewer.INFORMATION,true); //$NON-NLS-1$
        a = new NSISStickyHelpAction(resourceBundle,"sticky.help.",(TextOperationAction)a); //$NON-NLS-1$
        a.setActionDefinitionId(STICKY_HELP_COMMAND_ID);
        setAction("NSISStickyHelp", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"insert.file.",this,NSISSourceViewer.INSERT_FILE,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_FILE_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.file.image"))); //$NON-NLS-1$
        setAction("NSISInsertFile", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"insert.directory.",this,NSISSourceViewer.INSERT_DIRECTORY,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_DIRECTORY_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.directory.image"))); //$NON-NLS-1$
        setAction("NSISInsertDirectory", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"insert.color.",this,NSISSourceViewer.INSERT_COLOR,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_COLOR_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.color.image"))); //$NON-NLS-1$
        setAction("NSISInsertColor", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"tabs.to.spaces.",this,NSISSourceViewer.TABS_TO_SPACES,false); //$NON-NLS-1$
        a.setActionDefinitionId(TABS_TO_SPACES_COMMAND_ID);
        setAction("NSISTabsToSpaces", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"toggle.comment.",this,NSISSourceViewer.TOGGLE_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(TOGGLE_COMMENT_COMMAND_ID);
        setAction("NSISToggleComment", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"add.block.comment.",this,NSISSourceViewer.ADD_BLOCK_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(ADD_BLOCK_COMMENT_COMMAND_ID);
        setAction("NSISAddBlockComment", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"remove.block.comment.",this,NSISSourceViewer.REMOVE_BLOCK_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(REMOVE_BLOCK_COMMENT_COMMAND_ID);
        setAction("NSISRemoveBlockComment", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle, "projection.expand.all.", this, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
        a.setEnabled(true);
        setAction("ExpandAll", a); //$NON-NLS-1$

        a= new TextOperationAction(resourceBundle, "projection.expand.", this, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
        a.setEnabled(true);
        setAction("Expand", a); //$NON-NLS-1$

        a= new TextOperationAction(resourceBundle, "projection.collapse.", this, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
        a.setEnabled(true);
        setAction("Collapse", a); //$NON-NLS-1$
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
        ((ProjectionViewer)getSourceViewer()).removePostSelectionChangedListener(this);
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
        addAction(menu, "ContentAssistProposal"); //$NON-NLS-1$
        addAction(menu, "NSISInsertTemplate"); //$NON-NLS-1$
        menu.add(new Separator());
        addAction(menu, "NSISTabsToSpaces"); //$NON-NLS-1$
        addAction(menu, "NSISToggleComment"); //$NON-NLS-1$
        addAction(menu, "NSISAddBlockComment"); //$NON-NLS-1$
        IAction action = getAction("NSISAddBlockComment"); //$NON-NLS-1$
        action.setEnabled(getSourceViewer().getSelectedRange().y > 0);

        addAction(menu, "NSISRemoveBlockComment"); //$NON-NLS-1$
        menu.add(new Separator());
        addAction(menu, "NSISInsertFile"); //$NON-NLS-1$
        addAction(menu, "NSISInsertDirectory"); //$NON-NLS-1$
        addAction(menu, "NSISInsertColor"); //$NON-NLS-1$
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
     */
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        fAnnotationAccess= createAnnotationAccess();
        fOverviewRuler= createOverviewRuler(getSharedColors());

        ISourceViewer viewer= new NSISSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
        // ensure decoration support has been created and configured.
        SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(viewer);
        decorationSupport.setCharacterPairMatcher(new NSISCharacterPairMatcher());
        decorationSupport.setMatchingCharacterPainterPreferenceKeys(INSISPreferenceConstants.MATCHING_DELIMITERS,
                                                                    INSISPreferenceConstants.MATCHING_DELIMITERS_COLOR);
        return viewer;
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
            if(file != null && file.exists() && file.isFile()) {
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
            NSISInformationProvider informationProvider = new NSISInformationProvider();
            IInformationControlCreator informationControlCreator = new NSISInformationControlCreator(null,SWT.V_SCROLL|SWT.H_SCROLL);
            informationProvider.setInformationPresenterControlCreator(informationControlCreator);
            mInformationPresenter = new InformationPresenter(informationControlCreator);
            mInformationPresenter.setInformationProvider(informationProvider,NSISPartitionScanner.NSIS_STRING);
            mInformationPresenter.setInformationProvider(informationProvider,IDocument.DEFAULT_CONTENT_TYPE);
            mInformationPresenter.setSizeConstraints(60, 5, true, true);
        }

        /*
         *  @see org.eclipse.jface.action.IAction#run()
         */
        public void run()
        {
            ISourceViewer sourceViewer = getSourceViewer();
            int offset = NSISTextUtility.computeOffset(sourceViewer,true);
            if(offset == -1) {
                mTextOperationAction.run();
            }
            else {
                mInformationPresenter.install(sourceViewer);
                mInformationPresenter.setOffset(offset); //wordRegion.getOffset());
                mInformationPresenter.showInformation();
            }
        }
    }
}
