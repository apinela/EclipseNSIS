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
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.help.commands.*;
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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
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
    private HTMLExporter mHTMLExporter;

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

        TextOperationAction textAction = new TextOperationAction(resourceBundle,"sticky.help.",this,ISourceViewer.INFORMATION,true); //$NON-NLS-1$
        a = new NSISStickyHelpAction(resourceBundle,"sticky.help.",textAction); //$NON-NLS-1$
        a.setActionDefinitionId(STICKY_HELP_COMMAND_ID);
        setAction(INSISEditorConstants.STICKY_HELP, a); 
        a = new NSISPopupStickyHelpAction(resourceBundle,"popup.sticky.help.",textAction); //$NON-NLS-1$
        setAction(INSISEditorConstants.POPUP_STICKY_HELP, a); 

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

        a = new TextOperationAction(resourceBundle,"insert.regfile.",this,NSISSourceViewer.IMPORT_REGFILE,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_REGFILE_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.regfile.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_REGFILE, a); 

        a = new TextOperationAction(resourceBundle,"insert.regkey.",this,NSISSourceViewer.IMPORT_REGKEY,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_REGKEY_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.regkey.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_REGKEY, a); 

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

        a= new TextOperationAction(resourceBundle, "projection.collapse.all.", this, ProjectionViewer.COLLAPSE_ALL, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_COLLAPSE_ALL, a); //$NON-NLS-1$
    }

    protected void rulerContextMenuAboutToShow(IMenuManager menu) {
        super.rulerContextMenuAboutToShow(menu);
        IMenuManager foldingMenu= new MenuManager(EclipseNSISPlugin.getResourceString("folding.menu.label"), "net.sf.eclipsensis.projection"); //$NON-NLS-1$ //$NON-NLS-2$
        menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);

        IAction action= getAction(INSISEditorConstants.FOLDING_TOGGLE);
        foldingMenu.add(action);
        action= getAction(INSISEditorConstants.FOLDING_EXPAND_ALL);
        foldingMenu.add(action);
        action= getAction(INSISEditorConstants.FOLDING_COLLAPSE_ALL);
        foldingMenu.add(action);
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
        NSISPopupStickyHelpAction a = (NSISPopupStickyHelpAction)getAction(INSISEditorConstants.POPUP_STICKY_HELP);
        if (a != null) {
            a.setClickPoint(getSourceViewer().getTextWidget().getDisplay().getCursorLocation());
        }        
        addAction(menu, INSISEditorConstants.POPUP_STICKY_HELP); 
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
        addAction(menu, INSISEditorConstants.INSERT_REGFILE); 
        addAction(menu, INSISEditorConstants.INSERT_REGKEY); 
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
        ISelectionChangedListener listener = new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                openCommandView();
            }
        };
        viewer.addSelectionChangedListener(listener);
        viewer.addPostSelectionChangedListener(listener);
        
        //Add support for Drag & Drop
        final StyledText text2 = viewer.getTextWidget();
        DropTarget target = new DropTarget(text2, DND.DROP_DEFAULT | DND.DROP_COPY);
        target.setTransfer(new Transfer[]{NSISCommandTransfer.INSTANCE, FileTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent e)
            {
                if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType) || 
                   FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
                    //Don't want default feedback- we will do it ourselves
                    e.feedback = DND.FEEDBACK_NONE;
                    if (e.detail == DND.DROP_DEFAULT) {
                        e.detail = DND.DROP_COPY;
                    }
                }                
            }

            public void dragOperationChanged(DropTargetEvent e)
            {
                if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType) || 
                   FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
                    //Don't want default feedback- we will do it ourselves
                    e.feedback = DND.FEEDBACK_NONE;
                    if (e.detail == DND.DROP_DEFAULT) {
                        e.detail = DND.DROP_COPY;
                    }
                }                
            }

            public void dragOver(DropTargetEvent e)
            {
                if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType) || 
                   FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
                    //Don't want default feedback- we will do it ourselves
                    e.feedback = DND.FEEDBACK_NONE;
                    text2.setFocus();
                    Point location = text2.getDisplay().map(null, text2, e.x, e.y);
                    location.x = Math.max(0, location.x);
                    location.y = Math.max(0, location.y);
                    int offset;
                    try {
                        offset = text2.getOffsetAtLocation(new Point(location.x, location.y));
                    }
                    catch (IllegalArgumentException ex) {
                        try {
                            offset = text2.getOffsetAtLocation(new Point(0, location.y));
                        }
                        catch (IllegalArgumentException ex2) {
                            offset = text2.getCharCount();
                            Point maxLocation = text2.getLocationAtOffset(offset);
                            if (location.y >= maxLocation.y) {
                                if (location.x < maxLocation.x) {
                                    offset = text2.getOffsetAtLocation(new Point(location.x, maxLocation.y));
                                }
                            }
                        }
                    }
                    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                    offset = getCaretOffsetForInsertCommand(doc, offset);
                    
                    text2.setCaretOffset(offset);
                }
            }

            public void drop(DropTargetEvent e)
            {
                if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType)) {
                    insertCommand((NSISCommand)e.data, false);
                }
                else if(FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
                    insertFiles((String[])e.data);
                }
            }
        });
        return viewer;
    }
    
    private void insertFiles(String[] files)
    {
        ISourceViewer viewer = getSourceViewer();
        if(!Common.isEmptyArray(files) && viewer != null) {
            StyledText styledText = viewer.getTextWidget();
            if(styledText != null && !styledText.isDisposed()) {
                Point sel = styledText.getSelection();
                try {
                    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                    int offset = getCaretOffsetForInsertCommand(doc, sel.x);
                    styledText.setCaretOffset(offset);
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    String delim = doc.getLineDelimiter(styledText.getLineAtOffset(offset));
                    String fileKeyword = NSISKeywords.getInstance().getKeyword("File"); //$NON-NLS-1$
                    String recursiveKeyword = NSISKeywords.getInstance().getKeyword("/r"); //$NON-NLS-1$
                    RegistryImporter importer = null;
                    NSISEditorRegistryImportStrategy strategy = null;

                    for (int i = 0; i < files.length; i++) {
                        if(IOUtility.isValidFile(files[i])) {
                            if(files[i].regionMatches(true, files[i].length()-REG_FILE_EXTENSION.length(), REG_FILE_EXTENSION, 0, REG_FILE_EXTENSION.length())) {
                                if(importer == null || strategy == null) {
                                    importer = new RegistryImporter();
                                    strategy = new NSISEditorRegistryImportStrategy();
                                }
                                else {
                                    strategy.reset();
                                }
                                try {
                                    importer.importRegFile(styledText.getShell(), files[i], strategy);
                                    buf.append(strategy.getText()).append(delim);
                                    continue;
                                }
                                catch (Exception e) {
                                    EclipseNSISPlugin.getDefault().log(e);
                                }                                
                            }
                            buf.append(fileKeyword).append(" ").append( //$NON-NLS-1$
                                        IOUtility.resolveFileName(files[i], this)).append(
                                        delim);
                        }
                        else {
                            buf.append(fileKeyword).append(" ").append(recursiveKeyword).append( //$NON-NLS-1$
                                    " ").append(IOUtility.resolveFileName(files[i], this)).append(delim); //$NON-NLS-1$
                        }
                    }
                    String text = buf.toString();
                    doc.replace(offset, 0, text);
                    styledText.setCaretOffset(offset + text.length());
                }
                catch (Exception e) {
                    Common.openError(styledText.getShell(), e.getMessage(), EclipseNSISPlugin.getShellImage());
                    styledText.setSelection(sel);
                }
            }
        }
    }

    private int getCaretOffsetForInsertCommand(IDocument doc, int offset)
    {
        if(doc != null) {
            ITypedRegion[][] regions = NSISTextUtility.getNSISLines(doc, offset);
            if(Common.isEmptyArray(regions)) {
                try {
                    ITypedRegion partition = NSISTextUtility.getNSISPartitionAtOffset(doc, offset);
                    if(partition.getType().equals(NSISPartitionScanner.NSIS_SINGLELINE_COMMENT) ||
                       partition.getType().equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                        offset = partition.getOffset();
                        if(offset > 0) {
                            offset--;
                            partition = NSISTextUtility.getNSISPartitionAtOffset(doc, offset);
                            if(partition.getType().equals(NSISPartitionScanner.NSIS_SINGLELINE_COMMENT) ||
                               partition.getType().equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                                offset = offset+1;
                            }
                            else {
                                int line1 = doc.getLineOfOffset(offset);
                                int line2 = doc.getLineOfOffset(offset+1);
                                if(line1 == line2) {
                                    offset = getCaretOffsetForInsertCommand(doc, offset);
                                }
                                else {
                                    IRegion info  = doc.getLineInformation(line1);
                                    String s = doc.get(info.getOffset()+info.getLength()-1,1);
                                    if(s.charAt(0) == INSISConstants.LINE_CONTINUATION_CHAR) {
                                        offset = getCaretOffsetForInsertCommand(doc,info.getOffset()+info.getLength()-1);
                                    }
                                    else {
                                        offset++;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (BadLocationException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else {
                offset = regions[0][0].getOffset();
            }
        }
        return offset;
    }
  
    private void insertCommand(NSISCommand command, boolean updateOffset)
    {
        ISourceViewer viewer = getSourceViewer();
        if(viewer != null) {
            StyledText styledText = viewer.getTextWidget();
            if(styledText != null && !styledText.isDisposed()) {
                if(command != null) {
                    String text = null;
                    if (command.hasParameters()) {
                        NSISCommandDialog dlg = new NSISCommandDialog(styledText.getShell(), command);
                        int code = dlg.open();
                        if (code == Window.OK) {
                            text = dlg.getCommandText();
                        }
                    }
                    else {
                        text = command.getName();
                    }
                    if (!Common.isEmpty(text)) {
                        Point sel = styledText.getSelection();
                        try {
                            IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                            if (updateOffset) {
                                int offset = getCaretOffsetForInsertCommand(doc, sel.x);
                                styledText.setCaretOffset(offset);
                                styledText.setFocus();
                            }
                            int offset = styledText.getCaretOffset();
                            text = text + doc.getLineDelimiter(styledText.getLineAtOffset(offset));
                            doc.replace(offset, 0, text);
                            styledText.setCaretOffset(offset + text.length());
                        }
                        catch (Exception e) {
                            Common.openError(styledText.getShell(), e.getMessage(), EclipseNSISPlugin.getShellImage());
                            if (updateOffset) {
                                styledText.setSelection(sel);
                            }
                        }
                    }
                }
            }
        }
    }

    public void insertCommand(NSISCommand command)
    {
        insertCommand(command, true);
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
            protected void execute(IProgressMonitor monitor)
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

    private void openCommandView()
    {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if(activePage != null) {
                        IViewPart view = activePage.findView(COMMANDS_VIEW_ID);
                        if(view == null) {
                            activePage.showView(COMMANDS_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
                        }
                    }
                }
                catch(PartInitException pie) {
                    EclipseNSISPlugin.getDefault().log(pie);
                }
            }
        });
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
        else if(mOutlineContentProvider != null) {
            mOutlineContentProvider.refresh();
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

    public void exportHTML()
    {
        if(isDirty()) {
            if(!Common.openConfirm(getSourceViewer().getTextWidget().getShell(), 
                    EclipseNSISPlugin.getFormattedString("export.html.save.confirmation", //$NON-NLS-1$
                    new Object[] {((IPathEditorInput)getEditorInput()).getPath().lastSegment()}), 
                    EclipseNSISPlugin.getShellImage())) {
                return;
            }
            IProgressMonitor monitor = getProgressMonitor();
            doSave(monitor);
            if(monitor.isCanceled()) {
                return;
            }
        }
        if(mHTMLExporter == null) {
            mHTMLExporter = new HTMLExporter(this, getSourceViewer());
        }
        mHTMLExporter.exportHTML();
    }

    private class NSISStickyHelpAction extends TextEditorAction
    {
        private final TextOperationAction mTextOperationAction;
        private InformationPresenter mInformationPresenter;

        public NSISStickyHelpAction(ResourceBundle resourceBundle, String prefix, final TextOperationAction textOperationAction) 
        {
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
                int offset = computeOffset(sourceViewer);
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

        /**
         * @param sourceViewer
         * @return
         */
        protected int computeOffset(ISourceViewer sourceViewer)
        {
            return NSISTextUtility.computeOffset(sourceViewer,NSISTextUtility.COMPUTE_OFFSET_HOVER_LOCATION);
        }
    }

    private class NSISPopupStickyHelpAction extends NSISStickyHelpAction
    {
        private Point mClickPoint = null;
        public NSISPopupStickyHelpAction(ResourceBundle resourceBundle, String prefix, TextOperationAction textOperationAction)
        {
            super(resourceBundle, prefix, textOperationAction);
        }
        
        public void setClickPoint(Point p)
        {
            mClickPoint = p;
        }

        protected int computeOffset(ISourceViewer sourceViewer)
        {
            if(mClickPoint != null && sourceViewer != null && sourceViewer.getTextWidget() != null) {
                Point p = sourceViewer.getTextWidget().toControl(mClickPoint);
                return NSISTextUtility.computeOffsetAtLocation(sourceViewer, p.x, p.y);
            }
            else {
                return -1;
            }
        }
    }
}
