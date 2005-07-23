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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.SwitchEditorAction;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class InstallOptionsSourceEditor extends TextEditor implements IInstallOptionsEditor, IINIFileListener
{
    private boolean mSwitching = false;
    private INIFile mINIFile = new INIFile();
    private SelectionSynchronizer mSelectionSynchronizer = new SelectionSynchronizer();
    private OutlinePage mOutlinePage = null;
    private Map[] mCachedMarkers;
    private ProjectionAnnotationModel mAnnotationModel;
    private Annotation[] mAnnotations = null;
    
    public InstallOptionsSourceEditor()
    {
        super();
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
            MessageDialog.openError(getSite().getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                    InstallOptionsPlugin.getFormattedString("editor.switch.error", //$NON-NLS-1$
                                                            new String[]{((IFileEditorInput)getEditorInput()).getFile().getName()}));
        }
        return valid;
    }
    
    private boolean isSwitching()
    {
        return mSwitching;
    }
    
    public void prepareForSwitch()
    {
        if(!mSwitching) {
            mSwitching = true;
            ((InstallOptionsEditorInput)getEditorInput()).prepareForSwitch();
        }
    }

    public INIFile getINIFile()
    {
        return mINIFile;
    }
    
    public void iniFileChanged(INIFile iniFile)
    {
        IFileEditorInput input = (IFileEditorInput)getEditorInput();
        if(input != null) {
            InstallOptionsMarkerUtility.updateMarkers(input.getFile(), iniFile);
        }
        updateAnnotations();
        if(mOutlinePage != null) {
            mOutlinePage.update();
        }
    }

    protected void createActions()
    {
        super.createActions();
        IAction action = new SwitchEditorAction(this, INSTALLOPTIONS_DESIGN_EDITOR_ID);
        action.setActionDefinitionId(SWITCH_EDITOR_COMMAND_ID);
        setAction(action.getId(),action);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
     */
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] { IInstallOptionsConstants.EDITING_INSTALLOPTIONS_SOURCE_CONTEXT_ID });
    }

    public void dispose()
    {
        InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
        if(isSwitching()) {
             try {
                ((IFileEditorInput)input).getFile().setSessionProperty(IInstallOptionsConstants.FILEPROPERTY_PROBLEM_MARKERS, mCachedMarkers);
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
        else {
            if(isDirty()) {
                InstallOptionsMarkerUtility.updateMarkers(input.getFile(),mCachedMarkers);
            }
        }
        IDocument document = getDocumentProvider().getDocument(input);
        mINIFile.disconnect(document);
        mINIFile.removeListener(this);
        ((TextViewer)getSourceViewer()).addPostSelectionChangedListener(mSelectionSynchronizer);
        getSourceViewer().getSelectionProvider().removeSelectionChangedListener(mSelectionSynchronizer);
        super.dispose();
    }
    
    protected void doSetInput(IEditorInput input) throws CoreException
    {
        InstallOptionsEditorInput editorInput = (InstallOptionsEditorInput)getEditorInput();
        if(editorInput != null) {
            if(isDirty()) {
                InstallOptionsMarkerUtility.updateMarkers(editorInput.getFile(),mCachedMarkers);
            }
            IDocumentProvider provider = getDocumentProvider();
            if(provider != null) {
                IDocument document = provider.getDocument(editorInput);
                if(document != null) {
                    provider.disconnect(editorInput);
                    mINIFile.disconnect(document);
                }
            }
        }
        IFile file = ((IFileEditorInput)input).getFile();
        if(input != null) {
            if(!(input instanceof InstallOptionsEditorInput)) {
                input = new InstallOptionsEditorInput((IFileEditorInput)input);
                mCachedMarkers = InstallOptionsMarkerUtility.getMarkerAttributes(file);
                setDocumentProvider(((InstallOptionsEditorInput)input).getDocumentProvider());
                super.doSetInput(input);
            }
            else {
                mCachedMarkers = (Map[])file.getSessionProperty(IInstallOptionsConstants.FILEPROPERTY_PROBLEM_MARKERS);
                file.setSessionProperty(IInstallOptionsConstants.FILEPROPERTY_PROBLEM_MARKERS,null);
                setDocumentProvider(((InstallOptionsEditorInput)input).getDocumentProvider());
                super.doSetInput(input);
                ((InstallOptionsEditorInput)input).completedSwitch();
            }
        }
        input = getEditorInput();
        if(input != null) {
            IDocument document = getDocumentProvider().getDocument(input);
            mINIFile.connect(document);
            file = ((IFileEditorInput)input).getFile();
            if(Common.isEmptyArray(file.findMarkers(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID,
                                    false,IResource.DEPTH_ZERO))) {
                InstallOptionsMarkerUtility.updateMarkers(file, mINIFile);
            }
        }
    }
    
    protected void editorSaved()
    {
        super.editorSaved();
        mCachedMarkers = InstallOptionsMarkerUtility.getMarkerAttributes(((IFileEditorInput)getEditorInput()).getFile());
    }
    
    protected void initializeEditor()
    {
        super.initializeEditor();
        setSourceViewerConfiguration(new InstallOptionsSourceViewerConfiguration());
    }
    
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        fAnnotationAccess= createAnnotationAccess();
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
        mINIFile.addListener(this);
        mAnnotationModel = sourceViewer.getProjectionAnnotationModel();
        updateAnnotations();
    }

    private void updateAnnotations()
    {
        scheduleUIJob(InstallOptionsPlugin.getResourceString("outline.update.job.name"),getClass(), //$NON-NLS-1$
                new StatusRunnable(){
                    public IStatus run(IProgressMonitor monitor)
                    {
                        HashMap annotations = new HashMap();
                        INISection[] sections = mINIFile.getSections();
                        for (int i = 0; i < sections.length; i++) {
                            Position position = sections[i].getPosition();
                            annotations.put(new ProjectionAnnotation(),new Position(position.offset,position.length));
                        }
                        mAnnotationModel.modifyAnnotations(mAnnotations,annotations,null);
                        mAnnotations = (Annotation[])annotations.keySet().toArray(new Annotation[annotations.size()]);
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
        return super.getAdapter(type);
    }

    private void scheduleUIJob(String jobName, final Class clasz, final StatusRunnable runnable)
    {
        Job[] jobs = Platform.getJobManager().find(clasz);
        for (int i = 0; i < jobs.length; i++) {
            if(jobs[i].getState() != Job.RUNNING) {
                jobs[i].cancel();
            }
        }
        Job job = new UIJob(jobName){ //$NON-NLS-1$
                public boolean belongsTo(Object family)
                {
                    return clasz == family;
                }

                public IStatus runInUIThread(IProgressMonitor monitor)
                {
                    return runnable.run(monitor);
                }
            };
        job.schedule();
    }

    private class OutlinePage extends ContentOutlinePage
    {
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
//            mINIFile.addListener(this);
            viewer.addSelectionChangedListener(mSelectionSynchronizer);
            viewer.setInput(mINIFile);
            Point sel = getSourceViewer().getSelectedRange();
            mSelectionSynchronizer.selectionChanged(new SelectionChangedEvent(getSourceViewer().getSelectionProvider(),
                                                    new TextSelection(sel.x,sel.y)));
        }

        public void dispose()
        {
            TreeViewer viewer = getTreeViewer();
            if(viewer != null) {
                viewer.removeSelectionChangedListener(mSelectionSynchronizer);
            }
//            mINIFile.removeListener(this);
            super.dispose();
            mOutlinePage = null;
        }
        
        /* (non-Javadoc)
         * @see net.sf.eclipsensis.installoptions.ini.IINIFileListener#iniFileChanged(net.sf.eclipsensis.installoptions.ini.INIFile)
         */
        public void update()
        {
            scheduleUIJob(InstallOptionsPlugin.getResourceString("outline.update.job.name"),getClass(), //$NON-NLS-1$
                          new StatusRunnable(){
                              public IStatus run(IProgressMonitor monitor)
                              {
                                  try {
                                      TreeViewer viewer = getTreeViewer();
                                      if(viewer != null && mINIFile != null) {
                                          viewer.refresh(mINIFile);
                                      }
                                      return Status.OK_STATUS;
                                  }
                                  catch(Exception e) {
                                      e.printStackTrace();
                                      return new Status(IStatus.ERROR,IInstallOptionsConstants.PLUGIN_NAME,-1,e.getMessage(),e);
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
    
    private class OutlineLabelProvider extends LabelProvider
    {
        private ImageData mErrorImageData = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("error.decoration.icon")).getImageData(); //$NON-NLS-1$
        private ImageData mWarningImageData = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("warning.decoration.icon")).getImageData(); //$NON-NLS-1$
        private Image mUnknownImage = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("unknown.icon")); //$NON-NLS-1$
        private Image mSectionImage = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("inisection.icon")); //$NON-NLS-1$
        
        public String getText(Object element)
        {
            if(element instanceof INISection) {
                return ((INISection)element).getName();
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
                            if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_LABEL)) {
                                image = InstallOptionsLabel.LABEL_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_LINK)) {
                                image = InstallOptionsLink.LINK_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_BUTTON)) {
                                image = InstallOptionsButton.BUTTON_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_CHECKBOX)) {
                                image = InstallOptionsCheckBox.CHECKBOX_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_RADIOBUTTON)) {
                                image = InstallOptionsRadioButton.RADIOBUTTON_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_FILEREQUEST)) {
                                image = InstallOptionsFileRequest.FILEREQUEST_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_DIRREQUEST)) {
                                image = InstallOptionsDirRequest.DIRREQUEST_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_ICON)) {
                                image = InstallOptionsIcon.ICON_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_BITMAP)) {
                                image = InstallOptionsBitmap.BITMAP_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_GROUPBOX)) {
                                image = InstallOptionsGroupBox.GROUPBOX_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_TEXT)) {
                                image = InstallOptionsText.TEXT_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_PASSWORD)) {
                                image = InstallOptionsPassword.PASSWORD_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_COMBOBOX)) {
                                image = InstallOptionsCombobox.COMBOBOX_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_DROPLIST)) {
                                image = InstallOptionsDropList.DROPLIST_ICON;
                            }
                            else if (type.equalsIgnoreCase(InstallOptionsModel.TYPE_LISTBOX)) {
                                image = InstallOptionsListbox.LISTBOX_ICON;
                            }
                        }
                    }
                    else {
                        name="section"; //$NON-NLS-1$
                        image = mSectionImage;
                    }
                }
                
                if(image == null) {
                    name = "unknown"; //$NON-NLS-1$
                    image = mUnknownImage;
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
                data = mErrorImageData;
            }
            else if(element.hasWarnings()) {
                name = name.toLowerCase() + "$warning"; //$NON-NLS-1$
                data = mWarningImageData;
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
                            Position pos = section.getPosition();
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
                        }
                    }
                }
                finally {
                    mIsDispatching = false;
                }
            }
        }
    }
    
    private abstract class StatusRunnable
    {
        public abstract IStatus run(IProgressMonitor monitor);
    }
}
