/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.PreviewAction;
import net.sf.eclipsensis.installoptions.actions.SwitchEditorAction;
import net.sf.eclipsensis.installoptions.builder.InstallOptionsNature;
import net.sf.eclipsensis.installoptions.dialogs.InstallOptionsWidgetEditorDialog;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.gef.Disposable;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class InstallOptionsSourceEditor extends TextEditor implements IInstallOptionsEditor, IINIFileListener, IProjectionListener
{
    public static final String EXPORT_HTML_ACTION = "net.sf.eclipsensis.installoptions.export_html";
    private static final String FOLDING_COLLAPSE = "net.sf.eclipsensis.installoptions.folding_collapse";
    private static final String FOLDING_EXPAND = "net.sf.eclipsensis.installoptions.folding_expand";
    private static final String FOLDING_EXPAND_ALL = "net.sf.eclipsensis.installoptions.folding_expand_all";
    private static final String FOLDING_TOGGLE = "net.sf.eclipsensis.installoptions.folding_toggle";
    private static final String[] KEY_BINDING_SCOPES = new String[] { IInstallOptionsConstants.EDITING_INSTALLOPTIONS_SOURCE_CONTEXT_ID };
    private static final String MARKER_CATEGORY = "__installoptions_marker"; //$NON-NLS-1$
    private IPositionUpdater mMarkerPositionUpdater = new DefaultPositionUpdater(MARKER_CATEGORY);
    private ResourceTracker mResourceListener = new ResourceTracker();
    private Map mMarkerPositions = new HashMap();
    private boolean mSwitching = false;
    private INIFile mINIFile = new INIFile();
    private SelectionSynchronizer mSelectionSynchronizer = new SelectionSynchronizer();
    private OutlinePage mOutlinePage = null;
    private ProjectionAnnotationModel mAnnotationModel;
    private Annotation[] mAnnotations = null;
    private String mJobFamily = getClass().getName()+System.currentTimeMillis();
    private IModelListener mModelListener = new IModelListener()
    {
        public void modelChanged()
        {
            if(mINIFile != null) {
                mINIFile.validate(true);
            }
        }
    };
    private GotoMarker mGotoMarker = null;
    private JobScheduler mJobScheduler = InstallOptionsPlugin.getDefault().getJobScheduler();

    public InstallOptionsSourceEditor()
    {
        super();
        InstallOptionsPlugin.checkEditorAssociation();
        setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[]{
                InstallOptionsPlugin.getDefault().getPreferenceStore(),
                EditorsUI.getPreferenceStore()
        }));
        setHelpContextId(PLUGIN_CONTEXT_PREFIX + "installoptions_sourceeditor_context"); //$NON-NLS-1$;
    }

    public boolean canSwitch()
    {
        boolean valid = !mINIFile.hasErrors();
        if(!valid) {
            Common.openError(getSite().getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                    InstallOptionsPlugin.getFormattedString("editor.switch.error", //$NON-NLS-1$
                                                            new String[]{((IFileEditorInput)getEditorInput()).getFile().getName()}),
                                    InstallOptionsPlugin.getShellImage());
        }
        return valid;
    }

    public void prepareForSwitch()
    {
        if(!mSwitching) {
            mSwitching = true;
            ((IInstallOptionsEditorInput)getEditorInput()).prepareForSwitch();
        }
    }

    public INIFile getINIFile()
    {
        return mINIFile;
    }

    public void iniFileChanged(INIFile iniFile, int event)
    {
        iniFile.validate();
        updateAnnotations();
        if(mOutlinePage != null) {
            mOutlinePage.update();
        }
        updateActions();
    }
    
    private INISection getCurrentSection()
    {
        INISection section = null;
        if(mINIFile != null) {
            ISelection sel = getSourceViewer().getSelectionProvider().getSelection();
            if(sel instanceof ITextSelection && !sel.isEmpty()) {
                section = mINIFile.findSection(((ITextSelection)sel).getOffset(), ((ITextSelection)sel).getLength());
            }
        }
        return section;
    }

    protected void updateActions()
    {
        boolean hasErrors = (mINIFile != null && mINIFile.hasErrors());
        enableAction(SwitchEditorAction.ID,!hasErrors);
        enableAction(PreviewAction.PREVIEW_CLASSIC_ID,!hasErrors);
        enableAction(PreviewAction.PREVIEW_MUI_ID,!hasErrors);
        enableAction("net.sf.eclipsensis.installoptions.create_control",!hasErrors);
        
        INISection section = getCurrentSection();
        
        enableAction("net.sf.eclipsensis.installoptions.edit_control",!hasErrors && section != null && section.isInstallOptionsField());
        enableAction("net.sf.eclipsensis.installoptions.delete_control",!hasErrors && section != null && section.isInstallOptionsField());
    }
    
    private void enableAction(String id, boolean enabled)
    {
        IAction action = getAction(id);
        if(action != null) {
            action.setEnabled(enabled);
        }
    }

    protected void createActions()
    {
        super.createActions();
        IAction action = new SwitchEditorAction(this, INSTALLOPTIONS_DESIGN_EDITOR_ID);
        setAction(action.getId(),action);
        action = new PreviewAction(PREVIEW_CLASSIC, this);
        setAction(action.getId(),action);
        action = new PreviewAction(PREVIEW_MUI, this);
        setAction(action.getId(),action);

        action = new Action() {
            private HTMLExporter mHTMLExporter;
            
            public void run()
            {
                if(mHTMLExporter == null) {
                    mHTMLExporter = new HTMLExporter(InstallOptionsSourceEditor.this,getSourceViewer());
                }
                mHTMLExporter.exportHTML();
            }
        };
        action.setEnabled(true);
        action.setId(EXPORT_HTML_ACTION);
        setAction(action.getId(),action);
        
        action = new CreateControlAction();
        action.setEnabled(true);
        action.setId("net.sf.eclipsensis.installoptions.create_control");
        action.setActionDefinitionId("net.sf.eclipsensis.installoptions.commands.CreateControl");
        setAction(action.getId(),action);
        
        action = new EditControlAction();
        action.setEnabled(true);
        action.setId("net.sf.eclipsensis.installoptions.edit_control");
        action.setActionDefinitionId("net.sf.eclipsensis.installoptions.commands.EditControl");
        setAction(action.getId(),action);
        
        action = new DeleteControlAction();
        action.setEnabled(true);
        action.setId("net.sf.eclipsensis.installoptions.delete_control");
        action.setActionDefinitionId("net.sf.eclipsensis.installoptions.commands.DeleteControl");
        setAction(action.getId(),action);
        
        ResourceBundle resourceBundle = InstallOptionsPlugin.getDefault().getResourceBundle();
        action = new TextOperationAction(resourceBundle, "projection.toggle.", this, ProjectionViewer.TOGGLE, true); //$NON-NLS-1$
        action.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
        action.setEnabled(true);
        setAction(FOLDING_TOGGLE, action);

        action = new TextOperationAction(resourceBundle, "projection.expand.all.", this, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
        action.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
        action.setEnabled(true);
        setAction(FOLDING_EXPAND_ALL, action);

        action= new TextOperationAction(resourceBundle, "projection.expand.", this, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
        action.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
        action.setEnabled(true);
        setAction(FOLDING_EXPAND, action);

        action= new TextOperationAction(resourceBundle, "projection.collapse.", this, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
        action.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
        action.setEnabled(true);
        setAction(FOLDING_COLLAPSE, action);

//        a= new TextOperationAction(resourceBundle, "projection.collapse.all.", this, ProjectionViewer.COLLAPSE_ALL, true); //$NON-NLS-1$
//        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
//        a.setEnabled(true);
//        setAction("CollapseAll", a); //$NON-NLS-1$
//
//        a= new TextOperationAction(resourceBundle, "projection.restore.", this, ProjectionViewer.RESTORE, true); //$NON-NLS-1$
//        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_RESTORE);
//        a.setEnabled(true);
//        setAction("CollapseAll", a); //$NON-NLS-1$

        action = getAction(ITextEditorActionConstants.CONTEXT_PREFERENCES);
        if(action != null) {
            final Shell shell;
            if (getSourceViewer() != null) {
                shell= getSourceViewer().getTextWidget().getShell();
            }
            else {
                shell= null;
            }
            IAction action2= new ActionWrapper(action, new Runnable() {
                public void run() {
                    String[] preferencePages= collectContextMenuPreferencePages();
                    if (preferencePages.length > 0 && (shell == null || !shell.isDisposed())) {
                        PreferencesUtil.createPreferenceDialogOn(shell, preferencePages[0], preferencePages, InstallOptionsSourceEditor.class).open();
                    }
                }
            });
            setAction(ITextEditorActionConstants.CONTEXT_PREFERENCES, action2);
        }
        
        updateActions();
    }

    protected void rulerContextMenuAboutToShow(IMenuManager menu) {
        super.rulerContextMenuAboutToShow(menu);
        IMenuManager foldingMenu= new MenuManager("F&olding", "projection");
        menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);

        IAction action= getAction(FOLDING_TOGGLE);
        foldingMenu.add(action);
        action= getAction(FOLDING_EXPAND_ALL);
        foldingMenu.add(action);
    }

    protected String[] collectContextMenuPreferencePages()
    {
        String[] pages = {IInstallOptionsConstants.INSTALLOPTIONS_PREFERENCE_PAGE_ID};
        return (String[])Common.joinArrays(new Object[]{pages,super.collectContextMenuPreferencePages()});
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
     */
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(KEY_BINDING_SCOPES);
    }

    public void dispose()
    {
        InstallOptionsModel.INSTANCE.removeModelListener(mModelListener);
        mJobScheduler.cancelJobs(mJobFamily);
        IInstallOptionsEditorInput input = (IInstallOptionsEditorInput)getEditorInput();
        Object source = input.getSource();
        if(source instanceof IFile) {
            IFile file = (IFile)source;
            file.getWorkspace().removeResourceChangeListener(mResourceListener);
        }
        mMarkerPositions.clear();
        IDocument document = getDocumentProvider().getDocument(input);
        document.removePositionUpdater(mMarkerPositionUpdater);
        if(document.containsPositionCategory(MARKER_CATEGORY)) {
            try {
                document.removePositionCategory(MARKER_CATEGORY);
            }
            catch (BadPositionCategoryException e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }
        mINIFile.disconnect(document);
        mINIFile.removeListener(this);
        ((ProjectionViewer)getSourceViewer()).removeProjectionListener(this);
        ((TextViewer)getSourceViewer()).removePostSelectionChangedListener(mSelectionSynchronizer);
        getSourceViewer().getSelectionProvider().removeSelectionChangedListener(mSelectionSynchronizer);
        IAction action = super.getAction(PreviewAction.PREVIEW_CLASSIC_ID);
        if(action instanceof Disposable) {
            ((Disposable)action).dispose();
        }
        action = super.getAction(PreviewAction.PREVIEW_MUI_ID);
        if(action instanceof Disposable) {
            ((Disposable)action).dispose();
        }
        super.dispose();
    }

    protected void doSetInput(IEditorInput input) throws CoreException
    {
        IInstallOptionsEditorInput editorInput = (IInstallOptionsEditorInput)getEditorInput();
        if(editorInput != null) {
            Object source = editorInput.getSource();
            if(source instanceof IFile) {
                IFile file = (IFile)source;
                file.getWorkspace().removeResourceChangeListener(mResourceListener);
            }
            mMarkerPositions.clear();
            IDocumentProvider provider = getDocumentProvider();
            if(provider != null) {
                IDocument document = provider.getDocument(editorInput);
                if(document != null) {
                    document.removePositionUpdater(mMarkerPositionUpdater);
                    if(document.containsPositionCategory(MARKER_CATEGORY)) {
                        try {
                            document.removePositionCategory(MARKER_CATEGORY);
                        }
                        catch (BadPositionCategoryException e) {
                            InstallOptionsPlugin.getDefault().log(e);
                        }
                    }
                    provider.disconnect(editorInput);
                    mINIFile.disconnect(document);
                }
            }
        }
        if(input != null) {
            if(!(input instanceof IInstallOptionsEditorInput)) {
                if(input instanceof IFileEditorInput) {
                    IFile file = ((IFileEditorInput)input).getFile();
                    InstallOptionsNature.addNature(file.getProject());
                    input = new InstallOptionsEditorInput((IFileEditorInput)input);
                }
                else if (input instanceof IPathEditorInput){
                    input = new InstallOptionsExternalFileEditorInput((IPathEditorInput)input);
                }
                setDocumentProvider(((IInstallOptionsEditorInput)input).getDocumentProvider());
                super.doSetInput(input);
            }
            else {
                setDocumentProvider(((IInstallOptionsEditorInput)input).getDocumentProvider());
                super.doSetInput(input);
                ((IInstallOptionsEditorInput)input).completedSwitch();
            }
        }
        input = getEditorInput();
        if(input != null) {
            Object source = ((IInstallOptionsEditorInput)input).getSource();
            IFile file = null;
            if(source instanceof IFile) {
                file = (IFile)source;
                file.getWorkspace().addResourceChangeListener(mResourceListener);
            }
            IDocument document = getDocumentProvider().getDocument(input);
            document.addPositionCategory(MARKER_CATEGORY);
            document.addPositionUpdater(mMarkerPositionUpdater);
            mINIFile.connect(document);
            if(file != null) {
                for(Iterator iter=InstallOptionsMarkerUtility.getMarkers(file).iterator(); iter.hasNext(); ) {
                    IMarker marker = (IMarker)iter.next();
                    addMarkerPosition(document, marker);
                }
            }
        }
    }

    private void removeMarkerPosition(IDocument document, IMarker marker)
    {
        if(marker != null) {
            Position p = (Position)mMarkerPositions.remove(marker);
            if(document != null) {
                try {
                    document.removePosition(MARKER_CATEGORY, p);
                }
                catch (BadPositionCategoryException e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
        }
    }

    private void addMarkerPosition(IDocument document, IMarker marker)
    {
        if(document != null && marker != null) {
            int start = InstallOptionsMarkerUtility.getMarkerIntAttribute(marker,IMarker.CHAR_START);
            int end = InstallOptionsMarkerUtility.getMarkerIntAttribute(marker,IMarker.CHAR_END);
            Position p;
            if(start < 0 || end < 0) {
                int line = InstallOptionsMarkerUtility.getMarkerIntAttribute(marker,IMarker.LINE_NUMBER);
                if(line > 0) {
                    IRegion region;
                    try {
                        region = document.getLineInformation(line-1);
                        p = new Position(region.getOffset(),region.getLength());
                    }
                    catch (BadLocationException e) {
                        InstallOptionsPlugin.getDefault().log(e);
                        return;
                    }
                }
                else {
                    return;
                }
            }
            else {
                p = new Position(start,end-start+1);
            }
            try {
                document.addPosition(MARKER_CATEGORY, p);
                mMarkerPositions.put(marker,p);
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
                return;
            }
        }
    }

    protected void initializeEditor()
    {
        super.initializeEditor();
        setSourceViewerConfiguration(new InstallOptionsSourceViewerConfiguration());
    }

    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
//        fAnnotationAccess= createAnnotationAccess();
        fOverviewRuler= createOverviewRuler(getSharedColors());

        ISourceViewer viewer= new InstallOptionsSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);

        return viewer;
    }

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        ProjectionViewer sourceViewer = (ProjectionViewer)getSourceViewer();
        ProjectionSupport projectionSupport = new ProjectionSupport(sourceViewer,getAnnotationAccess(),getSharedColors());
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.task"); //$NON-NLS-1$
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.bookmark"); //$NON-NLS-1$
        projectionSupport.install();
        if(sourceViewer.canDoOperation(ProjectionViewer.TOGGLE)) {
            sourceViewer.doOperation(ProjectionViewer.TOGGLE);
        }
        sourceViewer.getSelectionProvider().addSelectionChangedListener(mSelectionSynchronizer);
        ((TextViewer)sourceViewer).addPostSelectionChangedListener(mSelectionSynchronizer);
        sourceViewer.addProjectionListener(this);
        mINIFile.addListener(this);
        mAnnotationModel = sourceViewer.getProjectionAnnotationModel();
        updateAnnotations();
        InstallOptionsModel.INSTANCE.addModelListener(mModelListener);
    }

    public void projectionDisabled()
    {
    }

    public void projectionEnabled()
    {
        updateProjectionAnnotations(new NullProgressMonitor());
    }

    private IStatus updateProjectionAnnotations(IProgressMonitor monitor)
    {
        ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
        if (viewer != null && viewer.isProjectionMode()) {
            HashMap annotations = new HashMap();
            INISection[] sections = mINIFile.getSections();
            for (int i = 0; i < sections.length; i++) {
                if(monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                Position position = sections[i].calculatePosition();
                annotations.put(new ProjectionAnnotation(),new Position(position.offset,position.length));
            }
            mAnnotationModel.modifyAnnotations(mAnnotations,annotations,null);
            mAnnotations = (Annotation[])annotations.keySet().toArray(new Annotation[annotations.size()]);
        }
        return Status.OK_STATUS;
    }

    private void updateAnnotations()
    {
        mJobScheduler.cancelJobs(mJobFamily);
        mJobScheduler.scheduleJob(mJobFamily, InstallOptionsPlugin.getResourceString("annotations.update.job.name"), //$NON-NLS-1$
                new IJobStatusRunnable(){
                    public IStatus run(IProgressMonitor monitor)
                    {
                        IStatus status = updateProjectionAnnotations(monitor);
                        if(!status.isOK()) {
                            return status;
                        }

                        ISourceViewer viewer = getSourceViewer();
                        if(viewer != null) {
                            AnnotationModel model = (AnnotationModel)viewer.getAnnotationModel();
                            if(model != null) {
                                model.removeAllAnnotations();
                                if(monitor.isCanceled()) {
                                    return Status.CANCEL_STATUS;
                                }
                                if(mINIFile.hasErrors() || mINIFile.hasWarnings()) {
                                    INIProblem[] problems = mINIFile.getProblems();
                                    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                                    for (int i = 0; i < problems.length; i++) {
                                        if(monitor.isCanceled()) {
                                            return Status.CANCEL_STATUS;
                                        }
                                        INIProblem problem = problems[i];
                                        if(problems[i].getLine() > 0) {
                                            try {
                                                String name;
                                                if(problem.getType() == INIProblem.TYPE_ERROR) {
                                                    name = IInstallOptionsConstants.INSTALLOPTIONS_ERROR_ANNOTATION_NAME;
                                                }
                                                else if(problem.getType() == INIProblem.TYPE_WARNING) {
                                                    name = IInstallOptionsConstants.INSTALLOPTIONS_WARNING_ANNOTATION_NAME;
                                                }
                                                else {
                                                    continue;
                                                }
                                                IRegion region = doc.getLineInformation(problem.getLine()-1);
                                                model.addAnnotation(new Annotation(name,false,problem.getMessage()),
                                                        new Position(region.getOffset(),region.getLength()));
                                            }
                                            catch (BadLocationException e) {
                                                InstallOptionsPlugin.getDefault().log(e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        return Status.OK_STATUS;
                    }
                });
    }

    public Object getAdapter(Class type)
    {
        if (type == IContentOutlinePage.class) {
            if(mOutlinePage == null || mOutlinePage.getControl() == null || mOutlinePage.getControl().isDisposed()) {
                mOutlinePage = new OutlinePage();
            }
            return mOutlinePage;
        }
        else if(type == IGotoMarker.class) {
            if(mGotoMarker == null) {
                mGotoMarker = new GotoMarker(super.getAdapter(type));
            }
            return mGotoMarker;
        }
        return super.getAdapter(type);
    }

    private class CreateControlAction extends Action
    {
        public void run()
        {
            INIFile iniFile = mINIFile.copy();
            doRun(iniFile, getSection(iniFile));
        }

        protected INISection getSection(INIFile iniFile)
        {
            return null;
        }

        /**
         * @param iniFile
         */
        protected void doRun(INIFile iniFile, INISection section)
        {
            InstallOptionsWidgetEditorDialog dialog = new InstallOptionsWidgetEditorDialog(getSite().getShell(), iniFile, section);
            if (dialog.open() == Window.OK) {
                if(section == null) {
                    section = dialog.getSection();
                }
                updateDocument(iniFile, section);
            }
        }

        /**
         * @param iniFile
         * @param section
         */
        protected void updateDocument(INIFile iniFile, INISection section)
        {
            List dirtyList = new ArrayList();
            for (Iterator iter = iniFile.getChildren().iterator(); iter.hasNext();) {
                INILine line = (INILine)iter.next();
                if (line instanceof INISection && ((INISection)line).isDirty()) {
                    dirtyList.add(line);
                }
            }
            boolean isDelete = false;
            if(section != null && !dirtyList.contains(section)) {
                isDelete = true;
                dirtyList.add(section);
            }
            if (dirtyList.size() > 0) {
                Collections.sort(dirtyList, new Comparator() {

                    private Position getPosition(INISection section)
                    {
                        Position p = section.getPosition();
                        return (p == null?IInstallOptionsConstants.MAX_POSITION:p);
                    }

                    public int compare(Object o1, Object o2)
                    {
                        Position p1 = getPosition((INISection)o1);
                        Position p2 = getPosition((INISection)o2);
                        int n = p2.getOffset() - p1.getOffset();
                        if (n == 0) {
                            n = p2.getLength() - p1.getLength();
                        }
                        return n;
                    }
                });
                IDocument document = getDocumentProvider().getDocument(getEditorInput());
                IUndoManager undoManager = ((InstallOptionsSourceViewer)getSourceViewer()).getUndoManager();
                try {
                    undoManager.beginCompoundChange();
                    Iterator iter = dirtyList.iterator();
                    while (iter.hasNext()) {
                        INISection sec = (INISection)iter.next();
                        Position p = sec.getPosition();
                        if(p == null) {
                            document.replace(document.getLength(), 0, sec.toString());
                        }
                        else {
                            if(sec == section && isDelete) {
                                document.replace(p.getOffset(), p.getLength(), "");
                            }
                            else {
                                document.replace(p.getOffset(), p.getLength(), sec.toString());
                            }
                        }
                    }
                }
                catch (BadLocationException e) {
                    e.printStackTrace();
                }
                finally {
                    undoManager.endCompoundChange();
                }
                if (!isDelete) {
                    INISection[] sections = mINIFile.findSections(section.getName());
                    if (sections != null && sections.length == 1) {
                        Position p = sections[0].getPosition();
                        getSelectionProvider().setSelection(new TextSelection(p.getOffset(), p.getLength()));
//                        getSourceViewer().revealRange(p.getOffset(), p.getLength());
                    }
                }                
            }
        }
    }

    private class EditControlAction extends CreateControlAction
    {
        protected INISection getSection(INIFile iniFile)
        {
            INISection section = null;
            INISection currSection = getCurrentSection();
            if (currSection != null && !mINIFile.hasErrors()) {
                for(Iterator iter = iniFile.getChildren().iterator(); iter.hasNext(); ) {
                    INILine line = (INILine)iter.next();
                    if (line instanceof INISection) {
                        INISection sec2 = (INISection)line;
                        if(Common.stringsAreEqual(sec2.getName(), currSection.getName())) {
                            section = sec2;
                            break;
                        }
                    }
                }
            }
            return section;
        }

        protected void doRun(INIFile iniFile, INISection section)
        {
            if (section != null) {
                doRun2(iniFile, section);
            }                
        }

        /**
         * @param iniFile
         * @param section
         */
        protected void doRun2(INIFile iniFile, INISection section)
        {
            super.doRun(iniFile, section);
        }
    }

    private class DeleteControlAction extends EditControlAction
    {
        protected void doRun2(INIFile iniFile, INISection section)
        {
            boolean show = InstallOptionsPlugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_DELETE_CONTROL_WARNING);
            if(!show || WinAPI.GetKeyState(WinAPI.VK_SHIFT)<0) {
                MessageDialogWithToggle dialog = new MessageDialogWithToggle(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        getPartName(),
                        InstallOptionsPlugin.getShellImage(),
                        section.getName()+" will be deleted.",
                        MessageDialog.WARNING,
                        new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0,
                        "Display only if Shift key is down", show); //$NON-NLS-1$
                dialog.open();
                if(dialog.getReturnCode() != IDialogConstants.CANCEL_ID) {
                    InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(PREFERENCE_DELETE_CONTROL_WARNING,dialog.getToggleState());
                }
                else {
                    return;
                }
            }
            iniFile.removeChild(section);
            iniFile.update(INILine.VALIDATE_FIX_ERRORS);
            updateDocument(iniFile, section);
        }
    }

    private class ActionWrapper implements IAction
    {
        private IAction mDelegate;
        private Runnable mRunnable;

        public ActionWrapper(IAction delegate, Runnable runnable)
        {
            super();
            mDelegate = delegate;
            mRunnable = runnable;
        }

        public void addPropertyChangeListener(IPropertyChangeListener listener)
        {
            mDelegate.addPropertyChangeListener(listener);
        }

        public int getAccelerator()
        {
            return mDelegate.getAccelerator();
        }

        public String getActionDefinitionId()
        {
            return mDelegate.getActionDefinitionId();
        }

        public String getDescription()
        {
            return mDelegate.getDescription();
        }

        public ImageDescriptor getDisabledImageDescriptor()
        {
            return mDelegate.getDisabledImageDescriptor();
        }

        public HelpListener getHelpListener()
        {
            return mDelegate.getHelpListener();
        }

        public ImageDescriptor getHoverImageDescriptor()
        {
            return mDelegate.getHoverImageDescriptor();
        }

        public String getId()
        {
            return mDelegate.getId();
        }

        public ImageDescriptor getImageDescriptor()
        {
            return mDelegate.getImageDescriptor();
        }

        public IMenuCreator getMenuCreator()
        {
            return mDelegate.getMenuCreator();
        }

        public int getStyle()
        {
            return mDelegate.getStyle();
        }

        public String getText()
        {
            return mDelegate.getText();
        }

        public String getToolTipText()
        {
            return mDelegate.getToolTipText();
        }

        public boolean isChecked()
        {
            return mDelegate.isChecked();
        }

        public boolean isEnabled()
        {
            return mDelegate.isEnabled();
        }

        public boolean isHandled()
        {
            return mDelegate.isHandled();
        }

        public void removePropertyChangeListener(IPropertyChangeListener listener)
        {
            mDelegate.removePropertyChangeListener(listener);
        }

        public void runWithEvent(Event event)
        {
            run();
        }

        public void setAccelerator(int keycode)
        {
            mDelegate.setAccelerator(keycode);
        }

        public void setActionDefinitionId(String id)
        {
            mDelegate.setActionDefinitionId(id);
        }

        public void setChecked(boolean checked)
        {
            mDelegate.setChecked(checked);
        }

        public void setDescription(String text)
        {
            mDelegate.setDescription(text);
        }

        public void setDisabledImageDescriptor(ImageDescriptor newImage)
        {
            mDelegate.setDisabledImageDescriptor(newImage);
        }

        public void setEnabled(boolean enabled)
        {
            mDelegate.setEnabled(enabled);
        }

        public void setHelpListener(HelpListener listener)
        {
            mDelegate.setHelpListener(listener);
        }

        public void setHoverImageDescriptor(ImageDescriptor newImage)
        {
            mDelegate.setHoverImageDescriptor(newImage);
        }

        public void setId(String id)
        {
            mDelegate.setId(id);
        }

        public void setImageDescriptor(ImageDescriptor newImage)
        {
            mDelegate.setImageDescriptor(newImage);
        }

        public void setMenuCreator(IMenuCreator creator)
        {
            mDelegate.setMenuCreator(creator);
        }

        public void setText(String text)
        {
            mDelegate.setText(text);
        }

        public void setToolTipText(String text)
        {
            mDelegate.setToolTipText(text);
        }

        public void run()
        {
            mRunnable.run();
        }
    }

    private class OutlinePage extends ContentOutlinePage
    {
        private String mJobFamily = getClass().getName()+System.currentTimeMillis();

        public void createControl(Composite parent)
        {
            super.createControl(parent);

            TreeViewer viewer = getTreeViewer();
            viewer.setContentProvider(new ITreeContentProvider(){

                public Object[] getChildren(Object parentElement)
                {
                    if(parentElement instanceof INIFile) {
                        return ((INIFile)parentElement).getSections();
                    }
                    return null;
                }

                public Object getParent(Object element)
                {
                    return null;
                }

                public boolean hasChildren(Object element)
                {
                    return !Common.isEmptyArray(getChildren(element));
                }

                public Object[] getElements(Object inputElement)
                {
                    return getChildren(inputElement);
                }

                public void dispose()
                {
                }

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
                {
                }
            });
            viewer.setLabelProvider(new OutlineLabelProvider());
            viewer.addSelectionChangedListener(mSelectionSynchronizer);
            viewer.setInput(mINIFile);
            Point sel = getSourceViewer().getSelectedRange();
            mSelectionSynchronizer.selectionChanged(new SelectionChangedEvent(getSourceViewer().getSelectionProvider(),
                                                    new TextSelection(sel.x,sel.y)));
        }

        public void dispose()
        {
            mJobScheduler.cancelJobs(mJobFamily);
            TreeViewer viewer = getTreeViewer();
            if(viewer != null) {
                viewer.removeSelectionChangedListener(mSelectionSynchronizer);
            }
            super.dispose();
            mOutlinePage = null;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.installoptions.ini.IINIFileListener#iniFileChanged(net.sf.eclipsensis.installoptions.ini.INIFile)
         */
        public void update()
        {
            mJobScheduler.cancelJobs(mJobFamily);
            mJobScheduler.scheduleUIJob(mJobFamily, InstallOptionsPlugin.getResourceString("outline.update.job.name"), //$NON-NLS-1$
                          new IJobStatusRunnable(){
                              public IStatus run(IProgressMonitor monitor)
                              {
                                  try {
                                      final TreeViewer viewer = getTreeViewer();
                                      if(viewer != null && mINIFile != null) {
                                          viewer.refresh(mINIFile);
                                      }
                                      return Status.OK_STATUS;
                                  }
                                  catch(Exception e) {
                                      InstallOptionsPlugin.getDefault().log(e);
                                      return new Status(IStatus.ERROR,IInstallOptionsConstants.PLUGIN_ID,-1,e.getMessage(),e);
                                  }
                              }
                          });
        }

        public void setSelection(ISelection selection)
        {
            TreeViewer viewer = getTreeViewer();
            if (viewer != null) {
                viewer.setSelection(selection, true);
            }
        }
    }

    private class GotoMarker implements IGotoMarker
    {
        private IGotoMarker mDelegate;

        public GotoMarker(Object o)
        {
            if(o instanceof IGotoMarker) {
                mDelegate = (IGotoMarker)o;
            }
        }

        public void gotoMarker(IMarker marker)
        {
            Position p = (Position)mMarkerPositions.get(marker);
            if(p != null) {
                if(!p.isDeleted()) {
                    selectAndReveal(p.getOffset(),p.getLength());
                }
                return;
            }
            if(mDelegate != null) {
                mDelegate.gotoMarker(marker);
            }
        }
    }

    private static class OutlineLabelProvider extends LabelProvider
    {
        private static final String MISSING_DISPLAY_NAME = InstallOptionsPlugin.getResourceString("missing.outline.display.name"); //$NON-NLS-1$

        private static ImageData cErrorImageData = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("error.decoration.icon")).getImageData(); //$NON-NLS-1$
        private static ImageData cWarningImageData = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("warning.decoration.icon")).getImageData(); //$NON-NLS-1$
        private static Image cUnknownImage = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("unknown.icon")); //$NON-NLS-1$
        private static Image cSectionImage = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("inisection.icon")); //$NON-NLS-1$

        public String getText(Object element)
        {
            if(element instanceof INISection) {
                String name = ((INISection)element).getName();
                Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(name);
                if(m.matches()) {
                    INIKeyValue[] values = ((INISection)element).findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                    if(!Common.isEmptyArray(values)) {
                        String type = values[0].getValue();
                        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(type);
                        if(typeDef != null) {
                            if(!typeDef.getName().equals(InstallOptionsModel.TYPE_UNKNOWN)) {
                                type = typeDef.getName();
                            }
                            String displayName = ""; //$NON-NLS-1$
                            values = ((INISection)element).findKeyValues(typeDef.getDisplayProperty());
                            if(!Common.isEmptyArray(values)) {
                                displayName = values[0].getValue();
                            }
                            return InstallOptionsPlugin.getFormattedString("source.outline.display.name.format",  //$NON-NLS-1$
                                    new String[]{name, type, (Common.isEmpty(displayName)?MISSING_DISPLAY_NAME:displayName)});
                        }
                    }
                }
                return InstallOptionsPlugin.getFormattedString("source.outline.section.name.format", new String[]{name}); //$NON-NLS-1$
            }
            return super.getText(element);
        }

        public Image getImage(Object element) {
            if(element instanceof INISection) {
                Image image = null;
                String name = ((INISection)element).getName();
                if(name.equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
                    image = InstallOptionsDialog.INSTALLOPTIONS_ICON;
                }
                else {
                    Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(name);
                    if(m.matches()) {
                        INIKeyValue[] values = ((INISection)element).findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                        if(!Common.isEmptyArray(values)) {
                            String type = values[0].getValue();
                            InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(type);
                            if(typeDef != null) {
                                image = InstallOptionsPlugin.getImageManager().getImage(typeDef.getSmallIcon());
                            }
                        }
                    }
                    else {
                        name="section"; //$NON-NLS-1$
                        image = cSectionImage;
                    }
                }

                if(image == null) {
                    name = "unknown"; //$NON-NLS-1$
                    image = cUnknownImage;
                }
                return decorateImage(name, image,(INISection)element);
            }
            return super.getImage(element);
        }

        private Image decorateImage(String name, final Image image, INISection element)
        {
            final ImageData data;
            if(element.hasErrors()) {
                name = name.toLowerCase() + "$error"; //$NON-NLS-1$
                data = cErrorImageData;
            }
            else if(element.hasWarnings()) {
                name = name.toLowerCase() + "$warning"; //$NON-NLS-1$
                data = cWarningImageData;
            }
            else {
                return image;
            }
            Image image2 = InstallOptionsPlugin.getImageManager().getImage(name);
            if(image2 == null) {
                InstallOptionsPlugin.getImageManager().putImageDescriptor(name,
                        new CompositeImageDescriptor(){
                            protected void drawCompositeImage(int width, int height)
                            {
                                drawImage(image.getImageData(),0,0);
                                drawImage(data,0,getSize().y-data.height);
                            }

                            protected Point getSize()
                            {
                                return new Point(image.getBounds().width,image.getBounds().height);
                            }
                        });
                image2 = InstallOptionsPlugin.getImageManager().getImage(name);
            }
            return image2;
        }
    }

    private class ResourceTracker implements IResourceChangeListener, IResourceDeltaVisitor
    {
        public void resourceChanged(IResourceChangeEvent event)
        {
            IResourceDelta delta = event.getDelta();
            try {
                if (delta != null) {
                    delta.accept(this);
                }
            }
            catch (CoreException exception) {
            }
        }

        public boolean visit(IResourceDelta delta)
        {
            if (delta == null
                    || !delta.getResource().equals(
                            ((IFileEditorInput)getEditorInput()).getFile())) {
                return true;
            }

            IDocument doc;
            try {
                doc = getDocumentProvider().getDocument(getEditorInput());
            }
            catch(Exception ex) {
                InstallOptionsPlugin.getDefault().log(ex);
                doc = null;
            }
            if (delta.getKind() == IResourceDelta.REMOVED) {
                mMarkerPositions.clear();
            }
            else if (delta.getKind() == IResourceDelta.CHANGED) {
                IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
                if(!Common.isEmptyArray(markerDeltas)) {
                    for (int i = 0; i < markerDeltas.length; i++) {
                        IMarker marker = markerDeltas[i].getMarker();
                        switch(markerDeltas[i].getKind()) {
                            case IResourceDelta.REMOVED:
                                removeMarkerPosition(doc, marker);
                                break;
                            case IResourceDelta.CHANGED:
                                removeMarkerPosition(doc, marker);
                                addMarkerPosition(doc, marker);
                                break;
                            case IResourceDelta.ADDED:
                                addMarkerPosition(doc, marker);
                                break;
                        }
                    }
                }
            }
            return false;
        }
    }

    private class SelectionSynchronizer implements ISelectionChangedListener
    {
        private boolean mIsDispatching = false;

        public void selectionChanged(SelectionChangedEvent event)
        {
            if (!mIsDispatching) {
                mIsDispatching = true;
                try {
                    ISelection sel = event.getSelection();
                    if(sel instanceof IStructuredSelection) {
                        //From outline viewer
                        IStructuredSelection ssel = (IStructuredSelection)sel;
                        if(ssel.size() == 1) {
                            INISection section = (INISection)ssel.getFirstElement();
                            Position pos = section.calculatePosition();
                            getSourceViewer().getSelectionProvider().setSelection(new TextSelection(pos.getOffset(),pos.getLength()));
                            getSourceViewer().revealRange(pos.getOffset(),pos.getLength());
                        }
                    }
                    else if(sel instanceof ITextSelection) {
                        if(mOutlinePage != null && mOutlinePage.getControl() != null && !mOutlinePage.getControl().isDisposed()) {
                            ITextSelection tsel = (ITextSelection)sel;
                            INISection section = mINIFile.findSection(tsel.getOffset(),tsel.getLength());
                            if(section != null) {
                                mOutlinePage.setSelection(new StructuredSelection(section));
                            }
                            else {
                                mOutlinePage.setSelection(StructuredSelection.EMPTY);
                            }
                            enableAction("net.sf.eclipsensis.installoptions.edit_control",(section != null && section.isInstallOptionsField())&&(mINIFile != null && !mINIFile.hasErrors()));
                            enableAction("net.sf.eclipsensis.installoptions.delete_control",(section != null && section.isInstallOptionsField())&&(mINIFile != null && !mINIFile.hasErrors()));
                        }
                    }
                }
                finally {
                    mIsDispatching = false;
                }
            }
        }
    }
}
