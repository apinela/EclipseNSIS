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

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.*;
import net.sf.eclipsensis.installoptions.dialogs.GridSnapGlueSettingsDialog;
import net.sf.eclipsensis.installoptions.dnd.*;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.ini.INIFile;
import net.sf.eclipsensis.installoptions.model.DialogSizeManager;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.properties.CustomPropertySheetEntry;
import net.sf.eclipsensis.installoptions.rulers.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.gef.ui.palette.*;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.*;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.stackview.CommandStackInspectorPage;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.gef.ui.views.palette.PaletteViewerPage;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class InstallOptionsDesignEditor extends EditorPart implements IInstallOptionsEditor, CommandStackListener, ISelectionListener
{
    private boolean mDisposed = false;
    private DefaultEditDomain mEditDomain;
    private GraphicalViewer mGraphicalViewer;
    private ActionRegistry mActionRegistry;
    private SelectionSynchronizer mSynchronizer;
    private List mSelectionActions = new ArrayList();
    private List mStackActions = new ArrayList();
    private List mPropertyActions = new ArrayList();
    private PaletteViewerProvider mPaletteProvider;
    private FlyoutPaletteComposite mPalette;
    private CustomPalettePage mPalettePage;
    private boolean mSwitching = false;
    private INIFile mINIFile = new INIFile();
    private KeyHandler mSharedKeyHandler;
    private Map[] mCachedMarkers;
    /** 
     * The number of reentrances into error correction code while saving.
     * @since 2.0
     */
    private int mErrorCorrectionOnSave;

    private PaletteRoot mRoot;

    private OutlinePage mOutlinePage;

    private boolean mEditorSaving = false;

    private IPartListener mPartListener = new IPartListener() {
        // If an open, unsaved file was deleted, query the user to either do a
        // "Save As"
        // or close the editor.
        public void partActivated(IWorkbenchPart part)
        {
            if (part != InstallOptionsDesignEditor.this) {
                return;
            }
            if (!((IFileEditorInput)getEditorInput()).getFile().exists()) {
                Shell shell = getSite().getShell();
                String title = InstallOptionsPlugin.getResourceString("file.deleted.error.title"); //$NON-NLS-1$
                String message = InstallOptionsPlugin.getResourceString("file.deleted.error.message"); //$NON-NLS-1$
                String[] buttons = {InstallOptionsPlugin.getResourceString("save.button.name"), InstallOptionsPlugin.getResourceString("close.button.name")}; //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialog dialog = new MessageDialog(shell, title, null,
                        message, MessageDialog.QUESTION, buttons, 0);
                if (dialog.open() == 0) {
                    if (!performSaveAs()) {
                        partActivated(part);
                    }
                }
                else {
                    closeEditor(false);
                }
            }
        }

        public void partBroughtToTop(IWorkbenchPart part)
        {
        }

        public void partClosed(IWorkbenchPart part)
        {
            part.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(InstallOptionsDesignEditor.this);
        }

        public void partDeactivated(IWorkbenchPart part)
        {
        }

        public void partOpened(final IWorkbenchPart part)
        {
            part.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(InstallOptionsDesignEditor.this);
            part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if(mINIFile.hasErrors()) {
                        MessageDialog.openError(getSite().getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                InstallOptionsPlugin.getFormattedString("editor.switch.error", //$NON-NLS-1$
                                                        new String[]{((IFileEditorInput)getEditorInput()).getFile().getName()}));
                        getActionRegistry().getAction(SwitchEditorAction.ID).run();
                    }
                }
            });
        }
    };

    private InstallOptionsDialog mInstallOptionsDialog = null;

    private boolean mSavePreviouslyNeeded = false;

    private ResourceTracker mResourceListener = new ResourceTracker();

    private InstallOptionsRulerComposite mRulerComposite;

    protected static final String PALETTE_DOCK_LOCATION = "PaletteDockLocation"; //$NON-NLS-1$

    protected static final String PALETTE_SIZE = "PaletteSize"; //$NON-NLS-1$

    protected static final String PALETTE_STATE = "PaletteState"; //$NON-NLS-1$

    protected static final int DEFAULT_PALETTE_SIZE = 130;

    static {
        InstallOptionsPlugin.getDefault().getPreferenceStore().setDefault(PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
    }

    public InstallOptionsDesignEditor()
    {
        setEditDomain(new InstallOptionsEditDomain(this));
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
     */
    protected void firePropertyChange(int property) 
    {
        super.firePropertyChange(property);
        updateActions(mPropertyActions);
    }

    /**
     * Lazily creates and returns the action registry.
     * @return the action registry
     */
    public ActionRegistry getActionRegistry() 
    {
        if (mActionRegistry == null) {
            mActionRegistry = new ActionRegistry();
        }
        return mActionRegistry;
    }

    /**
     * Returns the command stack.
     * @return the command stack
     */
    protected CommandStack getCommandStack() 
    {
        return getEditDomain().getCommandStack();
    }

    /**
     * Returns the edit domain.
     * @return the edit domain
     */
    protected DefaultEditDomain getEditDomain() 
    {
        return mEditDomain;
    }

    /**
     * Returns the graphical viewer.
     * @return the graphical viewer
     */
    public GraphicalViewer getGraphicalViewer() 
    {
        return mGraphicalViewer;
    }

    /**
     * Returns the list of {@link IAction IActions} dependent on property changes in the
     * Editor.  These actions should implement the {@link UpdateAction} interface so that they
     * can be updated in response to property changes.  An example is the "Save" action.
     * @return the list of property-dependant actions
     */
    protected List getPropertyActions() 
    {
        return mPropertyActions;
    }

    /**
     * Returns the list of {@link IAction IActions} dependent on changes in the workbench's
     * {@link ISelectionService}. These actions should implement the {@link UpdateAction}
     * interface so that they can be updated in response to selection changes.  An example is
     * the Delete action.
     * @return the list of selection-dependant actions
     */
    protected List getSelectionActions() 
    {
        return mSelectionActions;
    }

    /**
     * Returns the selection syncronizer object. The synchronizer can be used to sync the
     * selection of 2 or more EditPartViewers.
     * @return the syncrhonizer
     */
    protected SelectionSynchronizer getSelectionSynchronizer() 
    {
        if (mSynchronizer == null) {
            mSynchronizer = new SelectionSynchronizer();
        }
        return mSynchronizer;
    }

    /**
     * Returns the list of {@link IAction IActions} dependant on the CommmandStack's state. 
     * These actions should implement the {@link UpdateAction} interface so that they can be
     * updated in response to command stack changes.  An example is the "undo" action.
     * @return the list of stack-dependant actions
     */
    protected List getStackActions() 
    {
        return mStackActions;
    }

    /**
     * Hooks the GraphicalViewer to the rest of the Editor.  By default, the viewer
     * is added to the SelectionSynchronizer, which can be used to keep 2 or more
     * EditPartViewers in sync.  The viewer is also registered as the ISelectionProvider
     * for the Editor's PartSite.
     */
    protected void hookGraphicalViewer() 
    {
        getSelectionSynchronizer().addViewer(getGraphicalViewer());
        getSite().setSelectionProvider(getGraphicalViewer());
        getGraphicalViewer().addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged(SelectionChangedEvent event)
            {
                showPropertiesView();
            }
        });
    }

    /**
     * Sets the site and input for this editor then creates and initializes the actions.
     * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException 
    {
        setSite(site);
        setInput(input);
        getCommandStack().addCommandStackListener(this);
//        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
        initializeActionRegistry();
    }

    /**
     * Initializes the ActionRegistry.  This registry may be used by {@link
     * ActionBarContributor ActionBarContributors} and/or {@link ContextMenuProvider
     * ContextMenuProviders}.
     * <P>This method may be called on Editor creation, or lazily the first time {@link
     * #getActionRegistry()} is called.
     */
    protected void initializeActionRegistry() 
    {
        createActions();
        updateActions(mPropertyActions);
        updateActions(mStackActions);
    } 

    /**
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) 
    {
        // If not the active editor, ignore selection changed.
        IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if(activeEditor != null) {
            Object adapter = activeEditor.getAdapter(getClass());
            if (this.equals(adapter)) {
                updateActions(mSelectionActions);
            }
        }
    }

    /**
     * Sets the ActionRegistry for this EditorPart.
     * @param registry the registry
     */
    public void setActionRegistry(ActionRegistry registry) 
    {
        mActionRegistry = registry;
    } 

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() 
    {
        getGraphicalViewer().getControl().setFocus();
    }

    /**
     * Sets the graphicalViewer for this EditorPart.
     * @param viewer the graphical viewer
     */
    protected void setGraphicalViewer(GraphicalViewer viewer) 
    {
        getEditDomain().addViewer(viewer);
        this.mGraphicalViewer = viewer;
    }

    /**
     * A convenience method for updating a set of actions defined by the given List of action
     * IDs. The actions are found by looking up the ID in the {@link #getActionRegistry()
     * action registry}. If the corresponding action is an {@link UpdateAction}, it will have
     * its <code>update()</code> method called.
     * @param actionIds the list of IDs to update
     */
    protected void updateActions(List actionIds) 
    {
        ActionRegistry registry = getActionRegistry();
        Iterator iter = actionIds.iterator();
        while (iter.hasNext()) {
            IAction action = registry.getAction(iter.next());
            if (action instanceof UpdateAction) {
                ((UpdateAction)action).update();
            }
        }
    }

    /**
     * @see GraphicalEditor#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) 
    {
        mPalette = new FlyoutPaletteComposite(parent, SWT.NONE, getSite().getPage(),
                getPaletteViewerProvider(), getPalettePreferences());
        createGraphicalViewer(mPalette);
        mPalette.setGraphicalControl(getGraphicalControl());
        if (mPalettePage != null) {
            mPalette.setExternalViewer(mPalettePage.getPaletteViewer());
            mPalettePage = null;
        }
    }

    /**
     * Returns the palette viewer provider that is used to create palettes for the view and
     * the flyout.  Creates one if it doesn't already exist.
     * 
     * @return  the PaletteViewerProvider that can be used to create PaletteViewers for
     *          this editor
     * @see #createPaletteViewerProvider()
     */
    protected final PaletteViewerProvider getPaletteViewerProvider() 
    {
        if (mPaletteProvider == null) {
            mPaletteProvider = createPaletteViewerProvider();
        }
        return mPaletteProvider;
    }

    /**
     * Sets the edit domain for this editor.
     * 
     * @param   editDomain  The new EditDomain
     */
    protected void setEditDomain(DefaultEditDomain editDomain) 
    {
        mEditDomain = editDomain;
        getEditDomain().setPaletteRoot(getPaletteRoot());
    }

    protected void closeEditor(boolean save)
    {
        getSite().getPage().closeEditor(this, save);
    }

    public void commandStackChanged(EventObject event)
    {
        if (isDirty()) {
            if (!savePreviouslyNeeded()) {
                setSavePreviouslyNeeded(true);
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        }
        else {
            setSavePreviouslyNeeded(false);
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        updateActions(mStackActions);
    }

    protected void configureGraphicalViewer()
    {
        getGraphicalViewer().getControl().setBackground(ColorConstants.listBackground);
        ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();

        InstallOptionsRootEditPart root = new InstallOptionsRootEditPart();
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            List zoomLevels = new ArrayList(3);
            zoomLevels.add(ZoomManager.FIT_ALL);
            zoomLevels.add(ZoomManager.FIT_WIDTH);
            zoomLevels.add(ZoomManager.FIT_HEIGHT);
            root.getZoomManager().setZoomLevelContributions(zoomLevels);
    
            IAction zoomIn = new ZoomInAction(root.getZoomManager());
            IAction zoomOut = new ZoomOutAction(root.getZoomManager());
            getActionRegistry().registerAction(zoomIn);
            getActionRegistry().registerAction(zoomOut);
            getSite().getKeyBindingService().registerAction(zoomIn);
            getSite().getKeyBindingService().registerAction(zoomOut);
        }

        viewer.setRootEditPart(root);
       
        viewer.setEditPartFactory(GraphicalPartFactory.getInstance());
        ContextMenuProvider provider = new InstallOptionsDesignMenuProvider(this,//viewer,
                                                                    getActionRegistry());
        viewer.setContextMenu(provider);
        getSite().registerContextMenu("net.sf.eclipsensis.installoptions.editor.installoptionseditor.contextmenu", //$NON-NLS-1$
                provider, viewer);
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer)
                .setParent(getCommonKeyHandler()));
        IFile file = ((IFileEditorInput)getEditorInput()).getFile();
        loadProperties(file);

        // Actions
        IAction showRulers = new ToggleRulerVisibilityAction(getGraphicalViewer());
        getActionRegistry().registerAction(showRulers);

        IAction showGrid = new ToggleGridVisibilityAction(getGraphicalViewer());
        getActionRegistry().registerAction(showGrid);
        
        IAction showGuides = new ToggleGuideVisibilityAction(getGraphicalViewer());
        getActionRegistry().registerAction(showGuides);
    }

    protected CustomPalettePage createPalettePage()
    {
        return new CustomPalettePage(getPaletteViewerProvider());
    }

    protected PaletteViewerProvider createPaletteViewerProvider()
    {
        return new PaletteViewerProvider(getEditDomain()) {
            protected void configurePaletteViewer(PaletteViewer viewer) {
                super.configurePaletteViewer(viewer);
                viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
            }
        };
    }

    public void dispose()
    {
        InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
        IFile file = input.getFile();
        file.getWorkspace().removeResourceChangeListener(mResourceListener);
        if(input != null) {
            IDocumentProvider provider = input.getDocumentProvider();
            if(provider != null) {
                IDocument doc = provider.getDocument(input);
                if(doc != null) {
                    if(isSwitching()) {
                        try {
                            updateDocument(doc);
                            InstallOptionsMarkerUtility.updateMarkers(file, mINIFile);
                            file.setSessionProperty(IInstallOptionsConstants.FILEPROPERTY_PROBLEM_MARKERS, mCachedMarkers);
                        }
                        catch (CoreException e1) {
                            e1.printStackTrace();
                        }
                    }
                    else {
                        if(isDirty()) {
                            InstallOptionsMarkerUtility.updateMarkers(file, mCachedMarkers);
                        }
                    }
                    input.getDocumentProvider().disconnect(input);
                    mINIFile.disconnect(doc);
                }
            }
        }

        mPartListener = null;
        saveProperties(file);
        getCommandStack().removeCommandStackListener(this);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        getEditDomain().setActiveTool(null);
        ((InstallOptionsEditDomain)getEditDomain()).setFile(null);
        getActionRegistry().dispose();
        super.dispose();
        mDisposed = true;
    }

    public boolean isDisposed()
    {
        return mDisposed;
    }

    protected void handleExceptionOnSave(InstallOptionsEditorInput input, IDocumentProvider p, 
                                         CoreException exception, IProgressMonitor progressMonitor) 
    {
        try {
            ++ mErrorCorrectionOnSave;
            
            Shell shell= getSite().getShell();
            
            boolean isSynchronized= false;
            
            if (p instanceof IDocumentProviderExtension3)  {
                IDocumentProviderExtension3 p3= (IDocumentProviderExtension3) p;
                isSynchronized= p3.isSynchronized(input);
            } else  {
                long modifiedStamp= p.getModificationStamp(input);
                long synchStamp= p.getSynchronizationStamp(input);
                isSynchronized= (modifiedStamp == synchStamp);
            }
            
            if (mErrorCorrectionOnSave == 1 && !isSynchronized) {
                
                String title= InstallOptionsPlugin.getResourceString("outofsync.error.save.title"); //$NON-NLS-1$
                String msg= InstallOptionsPlugin.getResourceString("outofsync.error.save.message"); //$NON-NLS-1$
                
                if (MessageDialog.openQuestion(shell, title, msg))
                    performSave(input, p, true, progressMonitor);
                else {
                    if (progressMonitor != null) {
                        progressMonitor.setCanceled(true);
                    }
                }
            } 
            else {
                String title= InstallOptionsPlugin.getResourceString("error.save.title"); //$NON-NLS-1$
                String msg= InstallOptionsPlugin.getFormattedString("error.save.message", new Object[] { exception.getMessage() }); //$NON-NLS-1$
                ErrorDialog.openError(shell, title, msg, exception.getStatus());
                
                if (progressMonitor != null) {
                    progressMonitor.setCanceled(true);
                }
            }
        } 
        finally {
            -- mErrorCorrectionOnSave;
        }
    }

    public void doSave(IProgressMonitor progressMonitor)
    {
        try {
            InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
            if(input != null) {
                IDocumentProvider provider = input.getDocumentProvider();
                if(provider != null) {
                    IDocument doc = provider.getDocument(input);
                    INIFile iniFile = updateDocument(doc);
                    performSave(input, provider, true, progressMonitor);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void editorSaved(InstallOptionsEditorInput input)
    {
        IFile file = input.getFile();
        saveProperties(file);
        InstallOptionsMarkerUtility.updateMarkers(file, mINIFile);
        mCachedMarkers = InstallOptionsMarkerUtility.getMarkerAttributes(file);
    }
    
    public void doRevertToSaved() 
    {
        try {
            InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
            if(input != null) {
                IDocumentProvider provider = input.getDocumentProvider();
                if(provider != null) {
                    performRevert(input, provider);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void performRevert(InstallOptionsEditorInput input, IDocumentProvider provider) 
    {
        if (provider == null) {
            return;
        }
            
        try {
            mEditorSaving = true;
            provider.aboutToChange(getEditorInput());
            provider.resetDocument(getEditorInput());
            editorSaved(input);
        } 
        catch (CoreException x) {
            IStatus status= x.getStatus();
            if (status == null || status.getSeverity() != IStatus.CANCEL ) {
                Shell shell= getSite().getShell();
                String title= InstallOptionsPlugin.getResourceString("error.revert.title"); //$NON-NLS-1$
                String msg= InstallOptionsPlugin.getResourceString("error.revert.message"); //$NON-NLS-1$
                ErrorDialog.openError(shell, title, msg, x.getStatus());
            }
        } 
        finally {
            provider.changed(getEditorInput());
            mEditorSaving = false;
            loadInstallOptionsDialog();
            getCommandStack().flush();
        }
    }

    protected void performSave(InstallOptionsEditorInput input, IDocumentProvider provider, boolean overwrite, IProgressMonitor progressMonitor) 
    {
        if (provider == null) {
            return;
        }
        
        try {
            mEditorSaving = true;
            provider.aboutToChange(input);
            provider.saveDocument(progressMonitor, input, provider.getDocument(input), overwrite);
            editorSaved(input);
            getCommandStack().markSaveLocation();
        } 
        catch (CoreException x) 
        {
            IStatus status= x.getStatus();
            if (status == null || status.getSeverity() != IStatus.CANCEL) {
                handleExceptionOnSave(input, provider, x, progressMonitor);
            }
        } 
        finally {
            provider.changed(input);
            mEditorSaving = false;
        }
    }

    private INIFile updateDocument(IDocument doc) throws CoreException
    {
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(doc != null && dialog != null && dialog.canUpdateINIFile()) {
            INIFile iniFile = dialog.updateINIFile();
            iniFile.updateDocument(doc);
            return iniFile;
        }
        return null;
    }

    public void doSaveAs()
    {
        performSaveAs();
    }
    
    public Object getAdapter(Class type)
    {
        if(type == getClass()) {
            return this;
        }
        if (type == InstallOptionsDialog.class) {
            return mInstallOptionsDialog;
        }
        if (type == CommandStackInspectorPage.class) {
            return new CommandStackInspectorPage(getCommandStack());
        }
        if (type == IContentOutlinePage.class) {
            mOutlinePage = new OutlinePage(new InstallOptionsTreeViewer());
            return mOutlinePage;
        }
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            if (type == ZoomManager.class) {
                return getGraphicalViewer().getProperty(ZoomManager.class.toString());
            }
        }
        if (type == PalettePage.class) {
            if (mPalette == null) {
                mPalettePage = createPalettePage();
                return mPalettePage;
            }
            return createPalettePage();
        }
        if (type == org.eclipse.ui.views.properties.IPropertySheetPage.class) {
            PropertySheetPage page = new PropertySheetPage();
            page.setRootEntry(new CustomPropertySheetEntry((InstallOptionsEditDomain)getEditDomain()));
            return page;
        }
        if (type == EditDomain.class) {
            return getEditDomain();
        }
        if (type == GraphicalViewer.class) {
            return getGraphicalViewer();
        }
        if (type == CommandStack.class) {
            return getCommandStack();
        }
        if (type == ActionRegistry.class) {
            return getActionRegistry();
        }
        if (type == EditPart.class && getGraphicalViewer() != null) {
            return getGraphicalViewer().getRootEditPart();
        }
        if (type == IFigure.class && getGraphicalViewer() != null) {
            return ((GraphicalEditPart)getGraphicalViewer().getRootEditPart()).getFigure();
        }
        return super.getAdapter(type);
    }

    protected Control getGraphicalControl()
    {
        return mRulerComposite;
    }

    /**
     * Returns the KeyHandler with common bindings for both the Outline and
     * Graphical Views. For example, delete is a common action.
     */
    protected KeyHandler getCommonKeyHandler()
    {
        if (mSharedKeyHandler == null) {
            mSharedKeyHandler = new KeyHandler();
            mSharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
                                getActionRegistry().getAction(ActionFactory.DELETE.getId()));
            mSharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
                                getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
        }
        return mSharedKeyHandler;
    }

    private InstallOptionsDialog getInstallOptionsDialog()
    {
        return mInstallOptionsDialog;
    }

    protected FlyoutPreferences getPalettePreferences()
    {
        return new FlyoutPreferences() {
            public int getDockLocation()
            {
                return InstallOptionsPlugin.getDefault().getPreferenceStore().getInt(PALETTE_DOCK_LOCATION);
            }

            public int getPaletteState()
            {
                return InstallOptionsPlugin.getDefault().getPreferenceStore().getInt(PALETTE_STATE);
            }

            public int getPaletteWidth()
            {
                return InstallOptionsPlugin.getDefault().getPreferenceStore().getInt(PALETTE_SIZE);
            }

            public void setDockLocation(int location)
            {
                InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(PALETTE_DOCK_LOCATION, location);
            }

            public void setPaletteState(int state)
            {
                InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(PALETTE_STATE, state);
            }

            public void setPaletteWidth(int width)
            {
                InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(PALETTE_SIZE, width);
            }
        };
    }

    protected PaletteRoot getPaletteRoot()
    {
        if (mRoot == null) {
            mRoot = InstallOptionsPaletteProvider.createPalette();
        }
        return mRoot;
    }

    public void gotoMarker(IMarker marker)
    {
    }


    protected void initializeGraphicalViewer()
    {
        mPalette.hookDropTargetListener(getGraphicalViewer());
        getGraphicalViewer().setContents(getInstallOptionsDialog());
        getGraphicalViewer().addDropTargetListener((TransferDropTargetListener)new InstallOptionsTemplateTransferDropTargetListener(getGraphicalViewer()));
    }

    protected void createActions()
    {
        ActionRegistry registry = getActionRegistry();

        ToggleDialogSizeVisibilityAction dialogAction = new ToggleDialogSizeVisibilityAction(this);
        registry.registerAction(dialogAction);
        
        IAction action;
        
        action = new Action(InstallOptionsPlugin.getResourceString("grid.snap.glue.action.name")) { //$NON-NLS-1$
            public void run()
            {
                IFileEditorInput fileEditorInput = (IFileEditorInput)getEditorInput();
                if(fileEditorInput != null) {
                    new GridSnapGlueSettingsDialog(getSite().getShell(),getGraphicalViewer()).open();
                }
            }
        };
        action.setId(IInstallOptionsConstants.GRID_SNAP_GLUE_SETTINGS_ACTION_ID);
        action.setToolTipText(InstallOptionsPlugin.getResourceString("grid.snap.glue.action.tooltip")); //$NON-NLS-1$
        registry.registerAction(action);

        action = new UndoAction(this);
        registry.registerAction(action);
        getStackActions().add(action.getId());
        
        action = new RedoAction(this);
        registry.registerAction(action);
        getStackActions().add(action.getId());
        
        action = new SelectAllAction(this);
        registry.registerAction(action);
        
        action = new DeleteAction((IWorkbenchPart)this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        
        action = new SaveAction(this);
        registry.registerAction(action);
        getPropertyActions().add(action.getId());
        
        action = new RevertToSavedAction(this);
        registry.registerAction(action);
        getPropertyActions().add(action.getId());
        
        action = new PrintAction(this);
        registry.registerAction(action);

        action = new CutAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new CopyAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, SEND_BACKWARD);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, SEND_TO_BACK);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, BRING_FORWARD);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, BRING_TO_FRONT);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new PasteAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new MatchWidthAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new MatchHeightAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new DirectEditAction((IWorkbenchPart)this);
        String label = InstallOptionsPlugin.getResourceString("direct.edit.label"); //$NON-NLS-1$
        action.setText(label);
        action.setToolTipText(label);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new ExtendedEditAction((IWorkbenchPart)this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,
                PositionConstants.LEFT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.RIGHT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.TOP);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.BOTTOM);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.CENTER);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.MIDDLE);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        
        action = new SwitchEditorAction(this, INSTALLOPTIONS_SOURCE_EDITOR_ID);
        registry.registerAction(action);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse.swt.widgets.Composite)
     */
    protected void createGraphicalViewer(Composite parent)
    {
        mRulerComposite = new InstallOptionsRulerComposite(parent, SWT.NONE);
        GraphicalViewer viewer = new ScrollingGraphicalViewer();
        viewer.createControl(mRulerComposite);
        setGraphicalViewer(viewer);
        configureGraphicalViewer();
        hookGraphicalViewer();
        initializeGraphicalViewer();
        mRulerComposite.setGraphicalViewer((ScrollingGraphicalViewer)getGraphicalViewer());
        getGraphicalViewer().getControl().setBackground(getGraphicalViewer().getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    protected FigureCanvas getEditor()
    {
        return (FigureCanvas)getGraphicalViewer().getControl();
    }

    public boolean isDirty()
    {
        return isSaveOnCloseNeeded();
    }

    public boolean isSaveAsAllowed()
    {
        return true;
    }

    public boolean isSaveOnCloseNeeded()
    {
        InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
        return (input != null && input.getDocumentProvider().canSaveDocument(input)) || (getInstallOptionsDialog() != null && getInstallOptionsDialog().canUpdateINIFile());
    }

    private Object loadPreference(String name, TypeConverter converter, Object defaultValue)
    {
        IPreferenceStore store = InstallOptionsPlugin.getDefault().getPreferenceStore();
        Object o = null;
        try {
            o = converter.asType(store.getString(name));
        }
        catch(Exception ex) {
            o = null;
        }
        if(o == null) {
            o = converter.makeCopy(defaultValue);
        }
        return o;
    }

    private Object loadFileProperty(IFile file, QualifiedName name, TypeConverter converter, Object defaultValue)
    {
        defaultValue = loadPreference(name.getLocalName(),converter, defaultValue);
        Object o = null;
        try {
            o = converter.asType(file.getPersistentProperty(name), defaultValue);
        }
        catch (Exception e) {
            o = null;
        }
        if(o == null) {
            o = defaultValue;
        }
        return o;
    }

    protected void loadProperties(IFile file)
    {
        // Ruler properties
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(dialog != null) {
            GraphicalViewer viewer = getGraphicalViewer();

            if(viewer != null) {
                InstallOptionsRuler ruler = dialog.getRuler(PositionConstants.WEST);
                RulerProvider provider = null;
                if (ruler != null) {
                    provider = new InstallOptionsRulerProvider(ruler);
                    provider.setUnit(InstallOptionsRulerProvider.UNIT_DLU);
                }
                viewer.setProperty(RulerProvider.PROPERTY_VERTICAL_RULER,provider);
                
                ruler = dialog.getRuler(PositionConstants.NORTH);
                provider = null;
                if (ruler != null) {
                    provider = new InstallOptionsRulerProvider(ruler);
                    provider.setUnit(InstallOptionsRulerProvider.UNIT_DLU);
                }
                viewer.setProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER, provider);
                
                viewer.setProperty(RulerProvider.PROPERTY_RULER_VISIBILITY,
                        loadFileProperty(file, FILEPROPERTY_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                                SHOW_RULERS_DEFAULT));
        
                // Snap to Geometry property
                viewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED,
                        loadFileProperty(file, FILEPROPERTY_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                                SNAP_TO_GEOMETRY_DEFAULT));
        
                // Grid properties
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED,
                        loadFileProperty(file, FILEPROPERTY_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                                SNAP_TO_GRID_DEFAULT));
                viewer.setProperty(InstallOptionsGridLayer.PROPERTY_GRID_STYLE,
                        loadFileProperty(file, FILEPROPERTY_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                                GRID_STYLE_DEFAULT));
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_ORIGIN,
                        loadFileProperty(file, FILEPROPERTY_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                                GRID_ORIGIN_DEFAULT));
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING,
                        loadFileProperty(file, FILEPROPERTY_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                                GRID_SPACING_DEFAULT));
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE,
                        loadFileProperty(file, FILEPROPERTY_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                                SHOW_GRID_DEFAULT));
        
                // Guides properties
                viewer.setProperty(PROPERTY_SNAP_TO_GUIDES,
                        loadFileProperty(file, FILEPROPERTY_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                                SNAP_TO_GUIDES_DEFAULT));
                viewer.setProperty(PROPERTY_GLUE_TO_GUIDES,
                        loadFileProperty(file, FILEPROPERTY_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                                GLUE_TO_GUIDES_DEFAULT));
                viewer.setProperty(ToggleGuideVisibilityAction.PROPERTY_GUIDE_VISIBILITY,
                        loadFileProperty(file, FILEPROPERTY_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                                SHOW_GUIDES_DEFAULT));
                
                if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
                    // Zoom
                    ZoomManager manager = (ZoomManager)viewer.getProperty(ZoomManager.class.toString());
                    if (manager != null) {
                        manager.setZoomAsText((String)loadFileProperty(file, FILEPROPERTY_ZOOM,TypeConverter.STRING_CONVERTER,
                                ZOOM_DEFAULT));
                    }
                }
            }
            
            dialog.setDialogSize((Dimension)loadFileProperty(file, FILEPROPERTY_DIALOG_SIZE,TypeConverter.DIMENSION_CONVERTER,
                            DialogSizeManager.getDefaultDialogSizeDimension()));
            dialog.setShowDialogSize(((Boolean)loadFileProperty(file, FILEPROPERTY_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                            SHOW_DIALOG_SIZE_DEFAULT)).booleanValue());
        }
    }

    protected boolean performSaveAs()
    {
        Shell shell = getSite().getWorkbenchWindow().getShell();
        SaveAsDialog dialog = new SaveAsDialog(shell);
        InstallOptionsEditorInput input = (InstallOptionsEditorInput)getEditorInput();
        IFile original = input.getFile();
        dialog.setOriginalFile(original);
        dialog.create();
        
        IDocumentProvider provider= input.getDocumentProvider();
        if (provider == null) {
            // editor has programmatically been  closed while the dialog was open
            return false;
        }
        
        if (provider.isDeleted(input) && original != null) {
            String message= InstallOptionsPlugin.getFormattedString("warning.save.delete", new Object[] { original.getName() }); //$NON-NLS-1$
            dialog.setErrorMessage(null);
            dialog.setMessage(message, IMessageProvider.WARNING);
        }
        
        if (dialog.open() == Window.CANCEL) {
            return false;
        }
        IPath path = dialog.getResult();
        if (path == null) {
            return false;
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IFile file = workspace.getRoot().getFile(path);
        final IEditorInput newInput= new FileEditorInput(file);
        
        boolean success= false;
        if(!file.exists()) {
            try {
                IDocument doc = provider.getDocument(input);
                INIFile iniFile = updateDocument(doc);
                provider.aboutToChange(newInput);
                provider.saveDocument(new NullProgressMonitor(), newInput, doc, true);
                saveProperties(file);
                InstallOptionsMarkerUtility.updateMarkers(file, iniFile);
                success= true;
                
            } catch (CoreException x) {
                IStatus status= x.getStatus();
                if (status == null || status.getSeverity() != IStatus.CANCEL) {
                    String title= InstallOptionsPlugin.getResourceString("error.saveas.title"); //$NON-NLS-1$
                    String msg= InstallOptionsPlugin.getFormattedString("error.save.message", new Object[] { x.getMessage() }); //$NON-NLS-1$
                    
                    if (status != null) {
                        switch (status.getSeverity()) {
                            case IStatus.INFO:
                                MessageDialog.openInformation(shell, title, msg);
                            break;
                            case IStatus.WARNING:
                                MessageDialog.openWarning(shell, title, msg);
                            break;
                            default:
                                MessageDialog.openError(shell, title, msg);
                        }
                    } else {
                        MessageDialog.openError(shell, title, msg);
                    }
                }
            } finally {
                provider.changed(newInput);
            }
    
            if(success) {
                try {
                    superSetInput(createInput(file));
                    getCommandStack().markSaveLocation();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * @param file
     * @return
     */
    private IEditorInput createInput(IFile file)
    {
        return new FileEditorInput(file);
    }

    private boolean savePreviouslyNeeded()
    {
        return mSavePreviouslyNeeded;
    }

    private void saveFileProperty(IFile file, QualifiedName name, TypeConverter converter, Object value, Object defaultValue)
    {
        try {
            file.setPersistentProperty(name, converter.asString(value, defaultValue));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void saveProperties(IFile file)
    {
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(dialog != null) {
            GraphicalViewer viewer = getGraphicalViewer();
            saveFileProperty(file, FILEPROPERTY_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY),
                    SHOW_RULERS_DEFAULT);
    
            // Snap to Geometry property
            saveFileProperty(file, FILEPROPERTY_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED),
                    SNAP_TO_GEOMETRY_DEFAULT);
    
            // Grid properties
            saveFileProperty(file, FILEPROPERTY_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED),
                    SNAP_TO_GRID_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                    viewer.getProperty(InstallOptionsGridLayer.PROPERTY_GRID_STYLE),
                    GRID_STYLE_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                    viewer.getProperty(SnapToGrid.PROPERTY_GRID_ORIGIN),
                    GRID_ORIGIN_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                    viewer.getProperty(SnapToGrid.PROPERTY_GRID_SPACING),
                    GRID_SPACING_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE),
                    SHOW_GRID_DEFAULT);
    
            // Guides properties
            saveFileProperty(file, FILEPROPERTY_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(PROPERTY_SNAP_TO_GUIDES),
                    SNAP_TO_GUIDES_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(PROPERTY_GLUE_TO_GUIDES),
                    GLUE_TO_GUIDES_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                    viewer.getProperty(ToggleGuideVisibilityAction.PROPERTY_GUIDE_VISIBILITY),
                    SHOW_GUIDES_DEFAULT);
    
            saveFileProperty(file, FILEPROPERTY_DIALOG_SIZE,TypeConverter.DIMENSION_CONVERTER,
                    dialog.getDialogSize(),
                    DialogSizeManager.getDefaultDialogSizeDimension());
            saveFileProperty(file, FILEPROPERTY_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                    Boolean.valueOf(dialog.isShowDialogSize()),
                    SHOW_DIALOG_SIZE_DEFAULT);
            
            if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
                // Zoom
                ZoomManager manager = (ZoomManager)viewer.getProperty(ZoomManager.class.toString());
                if (manager != null) {
                    saveFileProperty(file, FILEPROPERTY_ZOOM,TypeConverter.STRING_CONVERTER,
                            manager.getZoomAsText(),
                            ZOOM_DEFAULT);
                }
            }
        }
    }

    public void setInput(IEditorInput input)
    {
        superSetInput(input);
        input = getEditorInput();
        IDocument document = ((InstallOptionsEditorInput)input).getDocumentProvider().getDocument(input);
        mINIFile.connect(document);
        loadInstallOptionsDialog();
    }

    private void loadInstallOptionsDialog()
    {
        InstallOptionsDialog dialog = new InstallOptionsDialog();
        if(!mINIFile.hasErrors()) {
            dialog.loadINIFile(mINIFile);
        }
        setInstallOptionsDialog(dialog);
        if (!mEditorSaving) {
            loadProperties(((IFileEditorInput)getEditorInput()).getFile());
            if (getGraphicalViewer() != null) {
                getGraphicalViewer().setContents(dialog);
            }
            if (mOutlinePage != null) {
                mOutlinePage.setContents(dialog);
            }
        }
    }

    private void setInstallOptionsDialog(InstallOptionsDialog diagram)
    {
        mInstallOptionsDialog = diagram;
    }

    private void setSavePreviouslyNeeded(boolean value)
    {
        mSavePreviouslyNeeded = value;
    }

    protected void superSetInput(IEditorInput input)
    {
        IEditorInput oldInput = getEditorInput();
        if (oldInput != null) {
            ((InstallOptionsEditDomain)getEditDomain()).setFile(null);
            IFile file = ((IFileEditorInput)oldInput).getFile();
            file.getWorkspace().removeResourceChangeListener(mResourceListener);
            if(isDirty()) {
                InstallOptionsMarkerUtility.updateMarkers(file,mCachedMarkers);
            }
            ((InstallOptionsEditorInput)oldInput).getDocumentProvider().disconnect(oldInput);
        }
        if(input != null) {
            try {
                if(!(input instanceof InstallOptionsEditorInput)) {
                    input = new InstallOptionsEditorInput((IFileEditorInput)input);
                    mCachedMarkers = InstallOptionsMarkerUtility.getMarkerAttributes(((IFileEditorInput)input).getFile());
                    ((InstallOptionsEditorInput)input).getDocumentProvider().connect(input);
                }
                else {
                    ((InstallOptionsEditorInput)input).getDocumentProvider().connect(input);
                    ((InstallOptionsEditorInput)input).completedSwitch();
                    mCachedMarkers = (Map[])((IFileEditorInput)input).getFile().getSessionProperty(IInstallOptionsConstants.FILEPROPERTY_PROBLEM_MARKERS);
                    ((IFileEditorInput)input).getFile().setSessionProperty(IInstallOptionsConstants.FILEPROPERTY_PROBLEM_MARKERS, null);
                }
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }

        super.setInput(input);

        input = getEditorInput();
        if (input != null) {
            IFile file = ((IFileEditorInput)input).getFile();
            ((InstallOptionsEditDomain)getEditDomain()).setFile(file);
            file.getWorkspace().addResourceChangeListener(mResourceListener);
            setPartName(file.getName());
        }
    }

    protected void setSite(IWorkbenchPartSite site)
    {
        super.setSite(site);
        getSite().getWorkbenchWindow().getPartService().addPartListener(mPartListener);
    }
    
    private boolean isSwitching()
    {
        return mSwitching;
    }
    
    public boolean canSwitch()
    {
        return true;
    }
    
    public void prepareForSwitch()
    {
        if(!mSwitching) {
            mSwitching = true;
            ((InstallOptionsEditorInput)getEditorInput()).prepareForSwitch();
        }
    }

    private void showPropertiesView()
    {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if(activePage != null) {
                        IViewPart view = (IViewPart)activePage.findView(IPageLayout.ID_PROP_SHEET);
                        if(view == null) {
                            activePage.showView(IPageLayout.ID_PROP_SHEET, null, IWorkbenchPage.VIEW_VISIBLE);
                        }
                        else {
                            activePage.bringToTop(view);
                        }
                    }
                }
                catch(PartInitException pie) {
                    pie.printStackTrace();
                }
            }
        });
    }

    protected class CustomPalettePage extends PaletteViewerPage 
    {
        /**
         * Constructor
         * @param provider  the provider used to create a PaletteViewer
         */
        public CustomPalettePage(PaletteViewerProvider provider) 
        {
            super(provider);
        }
        /**
         * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
         */
        public void createControl(Composite parent) 
        {
            super.createControl(parent);
            if (mPalette != null) {
                mPalette.setExternalViewer(viewer);
            }
        }
        /**
         * @see org.eclipse.ui.part.IPage#dispose()
         */
        public void dispose() 
        {
            if (mPalette != null) {
                mPalette.setExternalViewer(null);
            }
            super.dispose();
        }
        /**
         * @return  the PaletteViewer created and displayed by this page
         */
        public PaletteViewer getPaletteViewer() 
        {
            return viewer;
        }
    }

    private class OutlinePage extends ContentOutlinePage implements IAdaptable
    {
        private PageBook mPageBook;

        private Control mOutline;

        private Canvas mOverview;

        private IAction mShowOutlineAction, mShowOverviewAction;

        static final int ID_OUTLINE = 0;

        static final int ID_OVERVIEW = 1;

        private Thumbnail mThumbnail;

        private DisposeListener mDisposeListener;

        public OutlinePage(final EditPartViewer viewer)
        {
            super(viewer);
        }

        public void init(IPageSite pageSite)
        {
            super.init(pageSite);
            ActionRegistry registry = getActionRegistry();
            IActionBars bars = pageSite.getActionBars();
            String id = ActionFactory.UNDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.REDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.CUT.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.COPY.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.PASTE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.DELETE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            bars.updateActionBars();
        }

        protected void configureOutlineViewer()
        {
            getViewer().setEditDomain(getEditDomain());
            getViewer().setEditPartFactory(TreePartFactory.getInstance());
            ContextMenuProvider provider = new InstallOptionsDesignMenuProvider(getViewer(),
                    getActionRegistry());
            getViewer().setContextMenu(provider);
            getSite().registerContextMenu(
                    "net.sf.eclipsensis.installoptions.editor.outline.contextmenu", //$NON-NLS-1$
                    provider, getSite().getSelectionProvider());
            getViewer().setKeyHandler(getCommonKeyHandler());
            IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
            mShowOutlineAction = new Action() {
                public void run()
                {
                    showPage(ID_OUTLINE);
                }
            };
            mShowOutlineAction.setDescription(InstallOptionsPlugin.getResourceString("show.outline.action.description")); //$NON-NLS-1$
            mShowOutlineAction.setToolTipText(InstallOptionsPlugin.getResourceString("show.outline.action.tooltip")); //$NON-NLS-1$
            mShowOutlineAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("outline.icon"))); //$NON-NLS-1$
            tbm.add(mShowOutlineAction);
            mShowOverviewAction = new Action() {
                public void run()
                {
                    showPage(ID_OVERVIEW);
                }
            };
            mShowOverviewAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("overview.icon"))); //$NON-NLS-1$
            mShowOverviewAction.setDescription(InstallOptionsPlugin.getResourceString("show.overview.action.description")); //$NON-NLS-1$
            mShowOverviewAction.setToolTipText(InstallOptionsPlugin.getResourceString("show.overview.action.tooltip")); //$NON-NLS-1$
            tbm.add(mShowOverviewAction);
            showPage(ID_OUTLINE);
        }

        public void createControl(Composite parent)
        {
            mPageBook = new PageBook(parent, SWT.NONE);
            mOutline = getViewer().createControl(mPageBook);
            mOverview = new Canvas(mPageBook, SWT.NONE);
            mPageBook.showPage(mOutline);
            configureOutlineViewer();
            hookOutlineViewer();
            initializeOutlineViewer();
        }

        public void dispose()
        {
            unhookOutlineViewer();
            if (mThumbnail != null) {
                mThumbnail.deactivate();
                mThumbnail = null;
            }
            super.dispose();
            InstallOptionsDesignEditor.this.mOutlinePage = null;
        }

        public Object getAdapter(Class type)
        {
            if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
                if (type == ZoomManager.class) {
                    return getGraphicalViewer().getProperty(ZoomManager.class.toString());
                }
            }
            return null;
        }

        public Control getControl()
        {
            return mPageBook;
        }

        protected void hookOutlineViewer()
        {
            getSelectionSynchronizer().addViewer(getViewer());
        }

        protected void initializeOutlineViewer()
        {
            setContents(getInstallOptionsDialog());
        }

        protected void initializeOverview()
        {
            LightweightSystem lws = new LightweightSystem(mOverview);
            RootEditPart rep = getGraphicalViewer().getRootEditPart();
            if (rep instanceof ScalableFreeformRootEditPart) {
                ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart)rep;
                mThumbnail = new ScrollableThumbnail((Viewport)root.getFigure());
                mThumbnail.setBorder(new MarginBorder(3));
                mThumbnail.setSource(root
                        .getLayer(LayerConstants.PRINTABLE_LAYERS));
                lws.setContents(mThumbnail);
                mDisposeListener = new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        if (mThumbnail != null) {
                            mThumbnail.deactivate();
                            mThumbnail = null;
                        }
                    }
                };
                getEditor().addDisposeListener(mDisposeListener);
            }
        }

        public void setContents(Object contents)
        {
            getViewer().setContents(contents);
        }

        protected void showPage(int id)
        {
            if (id == ID_OUTLINE) {
                mShowOutlineAction.setChecked(true);
                mShowOverviewAction.setChecked(false);
                mPageBook.showPage(mOutline);
                if (mThumbnail != null) {
                    mThumbnail.setVisible(false);
                }
            }
            else if (id == ID_OVERVIEW) {
                if (mThumbnail == null) {
                    initializeOverview();
                }
                mShowOutlineAction.setChecked(false);
                mShowOverviewAction.setChecked(true);
                mPageBook.showPage(mOverview);
                mThumbnail.setVisible(true);
            }
        }

        protected void unhookOutlineViewer()
        {
            getSelectionSynchronizer().removeViewer(getViewer());
            if (mDisposeListener != null && getEditor() != null && !getEditor().isDisposed()) {
                getEditor().removeDisposeListener(mDisposeListener);
            }
        }
    }

    // This class listens to changes to the file system in the workspace, and
    // makes changes accordingly.
    // 1) An open, saved file gets deleted -> close the editor
    // 2) An open file gets renamed or moved -> change the editor's input
    // accordingly
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
                // What should be done here?
            }
        }

        public boolean visit(IResourceDelta delta)
        {
            if (delta == null
                    || !delta.getResource().equals(
                            ((IFileEditorInput)getEditorInput()).getFile()))
                return true;

            if (delta.getKind() == IResourceDelta.REMOVED) {
                Display display = getSite().getShell().getDisplay();
                if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) { 
                    // If the file was deleted
                    // NOTE: The case where an open, unsaved file is deleted is
                    // being handled by the
                    // PartListener added to the Workbench in the initialize()
                    // method.
                    display.asyncExec(new Runnable() {
                        public void run()
                        {
                            if (!isDirty()) {
                                closeEditor(false);
                            }
                        }
                    });
                }
                else { // else if it was moved or renamed
                    final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getMovedToPath());
                    display.asyncExec(new Runnable() {
                        public void run()
                        {
                            superSetInput(createInput(newFile));
                        }
                    });
                }
            }
            else if (delta.getKind() == IResourceDelta.CHANGED) {
                if (!mEditorSaving) {
                    // the file was overwritten somehow (could have been
                    // replaced by another
                    // version in the respository)
                    final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getFullPath());
                    Display display = getSite().getShell().getDisplay();
                    display.asyncExec(new Runnable() {
                        public void run()
                        {
                            setInput(createInput(newFile));
                            getCommandStack().flush();
                        }
                    });
                }
            }
            return false;
        }
    }

    private class InstallOptionsTreeViewer extends TreeViewer
    {
        public InstallOptionsTreeViewer()
        {
            super();
            addDragSourceListener(new InstallOptionsTreeViewerDragSourceListener(this));
            addDropTargetListener(new InstallOptionsTreeViewerDropTargetListener(this));
        }
        
        public void addDragSourceListener(TransferDragSourceListener listener)
        {
            if(listener instanceof InstallOptionsTreeViewerDragSourceListener) {
                super.addDragSourceListener(listener);
            }
        }
        
        public void addDropTargetListener(TransferDropTargetListener listener)
        {
            if(listener instanceof InstallOptionsTreeViewerDropTargetListener) {
                super.addDropTargetListener(listener);
            }
        }
    }
}