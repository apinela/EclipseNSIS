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
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class NSISEditor extends TextEditor implements INSISConstants, IPropertyChangeListener, ISelectionChangedListener
{
    private static HashSet cEditors = new HashSet();
    
    private HashSet mActions = new HashSet();
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
        setHelpContextId(NSIS_EDITOR_CONTEXT);
    }
    
    public void selectionChanged(SelectionChangedEvent event)
    {
        Object source = event.getSource();
        ISelection selection = event.getSelection();
        ISourceViewer sourceViewer = getSourceViewer();
        if(source.equals(sourceViewer) && selection instanceof ITextSelection) {
            IAction action = getAction("NSISAddBlockComment"); //$NON-NLS-1$
            if(action != null) {
                action.setEnabled(sourceViewer.getSelectedRange().y > 0);
            }
    
            if(mOutlineContentProvider != null) {
                if(mMutex.acquireWithoutBlocking(source)) {
                    try {
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
                    finally {
                        mMutex.release(source);
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
                        boolean moveCursor = true;
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

                if(mMutex.acquireWithoutBlocking(source)) {
                    try {
                        Position selectPosition = element.getSelectPosition();
                        if(selectPosition != null) {
                            sourceViewer.setSelectedRange(selectPosition.getOffset(),selectPosition.getLength());
                        }
                    }
                    finally {
                        mMutex.release(source);
                    }
                }
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
        NSISPreferences.getPreferences().getPreferenceStore().addPropertyChangeListener(this);
        if(viewer.canDoOperation(ProjectionViewer.TOGGLE)) {
            viewer.doOperation(ProjectionViewer.TOGGLE);
        }
        mOutlineContentProvider = new NSISOutlineContentProvider(this);
        getSelectionProvider().addSelectionChangedListener(this);
        viewer.addPostSelectionChangedListener(this);
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
        a.setImageDescriptor(ImageManager.getImageDescriptor(resourceBundle.getString("insert.file.image"))); //$NON-NLS-1$
        setAction("NSISInsertFile", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"insert.directory.",this,NSISSourceViewer.INSERT_DIRECTORY,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_DIRECTORY_COMMAND_ID);
        a.setImageDescriptor(ImageManager.getImageDescriptor(resourceBundle.getString("insert.directory.image"))); //$NON-NLS-1$
        setAction("NSISInsertDirectory", a); //$NON-NLS-1$

        a = new TextOperationAction(resourceBundle,"insert.color.",this,NSISSourceViewer.INSERT_COLOR,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_COLOR_COMMAND_ID);
        a.setImageDescriptor(ImageManager.getImageDescriptor(resourceBundle.getString("insert.color.image"))); //$NON-NLS-1$
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
        NSISPreferences.getPreferences().getPreferenceStore().removePropertyChangeListener(this);
        super.dispose();
    }
    
    public void doSetInput(IEditorInput input) throws CoreException 
    {
        IEditorInput oldInput = getEditorInput();
        super.doSetInput(input);
        if(oldInput != null) {
            cEditors.remove(this);
        }
        if(input != null) {
            cEditors.add(this);
        }
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
            if (adapter != null)
                return adapter;
        }
        
        return super.getAdapter(required);
    }
        
    /* (non-Javadoc)
     * Method declared on AbstractTextEditor
     */
    protected void initializeEditor() 
    {
        super.initializeEditor();
        IPreferenceStore preferenceStore = NSISPreferences.getPreferences().getPreferenceStore();
        setPreferenceStore(preferenceStore);
        setSourceViewerConfiguration(new NSISEditorSourceViewerConfiguration(preferenceStore));
    }
    
    public void addAction(NSISAction action)
    {
        mActions.add(action);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
            updateActionsState();
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
            e.printStackTrace();
        }
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

    public static void updatePresentations()
    {
        for(Iterator iter=cEditors.iterator(); iter.hasNext(); ) {
            NSISEditor editor = (NSISEditor)iter.next();
            ISourceViewer sourceViewer = editor.getSourceViewer();
            if(sourceViewer instanceof NSISSourceViewer) {
                NSISSourceViewer viewer = (NSISSourceViewer)sourceViewer;
                if(viewer.mustProcessPropertyQueue()) {
                    viewer.processPropertyQueue();
                }
            }
        }
    }

    public static Collection getEditors()
    {
        return Collections.unmodifiableSet(cEditors);
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
    
    private static class Mutex
    {
        private static int cCounter = 0;
        private Object mOwner = null;
        private int mLockCount = 0;
        private final int mId = newId();

        public int getId()
        {
            return mId;
        }

        private static int newId()
        {
            synchronized(Mutex.class) {
                cCounter++;
                return cCounter;
            }
        }

        /**
         * Acquire the mutex. The mutex can be acquired multiple times
         * by the same thread, provided that it is released as many
         * times as it is acquired. The calling thread blocks until
         * it has acquired the mutex. (There is no timeout).
         *
         * @see release
         * @see acquireWithoutBlocking
         */
        public synchronized void acquire(Object owner, long timeout) throws InterruptedException
        {
            while( acquireWithoutBlocking(owner) == false) {
                this.wait(timeout);
            }
        }

        /**
         * Attempts to acquire the mutex. Returns false (and does not
         * block) if it can't get it.
         *
         * @see release
         * @see acquire
         */
        public synchronized boolean acquireWithoutBlocking(Object owner)
        {
            // Try to get the mutex. Return true if you got it.
            if( mOwner == null ) {   
                mOwner = owner;
                mLockCount = 1;
                return true;
            }

            if( mOwner == owner ) {
                ++mLockCount;
                return true;
            }

            return false;
        }

        /**
         * Release the mutex. The mutex has to be released as many times
         * as it was acquired to actually unlock the resource. The mutex
         * must be released by the thread that acquired it
         *
         * @throws Mutex.OwnershipException (a RuntimeException) if a thread
         *      other than the current owner tries to release the mutex.
         */
         public synchronized void release(Object owner)
         {
             if( mOwner == owner ) {
                 if( --mLockCount <= 0 ) {   
                     mOwner = null;
                     notify();
                 }
             }
         }
    }
}
