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
import net.sf.eclipsensis.installoptions.builder.InstallOptionsNature;
import net.sf.eclipsensis.installoptions.dialogs.GridSnapGlueSettingsDialog;
import net.sf.eclipsensis.installoptions.dnd.*;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.ini.INIFile;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.CustomPropertySheetPage;
import net.sf.eclipsensis.installoptions.rulers.*;
import net.sf.eclipsensis.installoptions.template.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.template.AbstractTemplateSettings;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.gef.ui.palette.*;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.customize.PaletteSettingsDialog;
import org.eclipse.gef.ui.parts.*;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.gef.ui.views.palette.PaletteViewerPage;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;
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
    private boolean mCreatedEmptyPart = true;
    private IModelListener mModelListener = new IModelListener()
    {
        public void modelChanged()
        {
            GraphicalViewer viewer = getGraphicalViewer();
            if(viewer != null) {
                ISelection sel = viewer.getSelection();
                viewer.setSelection(sel);
            }
            InstallOptionsDialog dialog = getInstallOptionsDialog();
            if(dialog != null && dialog.canUpdateINIFile()) {
                dialog.updateINIFile();
            }
            mINIFile.validate(true);
            if(mINIFile.hasErrors()) {
                IWorkbenchWindow window = InstallOptionsDesignEditor.this.getSite().getWorkbenchWindow();
                if(PlatformUI.getWorkbench().getActiveWorkbenchWindow() == window) {
                    if(window.getPartService().getActivePart() == InstallOptionsDesignEditor.this) {
                        checkPerformSwitch();
                    }
                }
            }
        }
    };

    /** 
     * The number of reentrances into error correction code while saving.
     * @since 2.0
     */
    private int mErrorCorrectionOnSave;

    private PaletteRoot mRoot;

    private OutlinePage mOutlinePage;
//    private boolean mEditorOpened = false;
    private boolean mEditorSaving = false;

    private IWindowListener mWindowListener = new IWindowListener() {
        public void windowActivated(IWorkbenchWindow window)
        {
            if(window == InstallOptionsDesignEditor.this.getSite().getWorkbenchWindow()) {
                if(window.getPartService().getActivePart() == InstallOptionsDesignEditor.this) {
                    checkPerformSwitch();
                }
            }
        }

        public void windowDeactivated(IWorkbenchWindow window)
        {
        }

        public void windowClosed(IWorkbenchWindow window)
        {
        }

        public void windowOpened(IWorkbenchWindow window)
        {
        }
    };

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
            checkPerformSwitch();
        }

        public void partBroughtToTop(IWorkbenchPart part)
        {
        }

        public void partClosed(IWorkbenchPart part)
        {
            if (part != InstallOptionsDesignEditor.this) {
                return;
            }
            IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
            window.getSelectionService().removeSelectionListener(InstallOptionsDesignEditor.this);
            part.getSite().getWorkbenchWindow().getWorkbench().removeWindowListener(mWindowListener);
        }

        public void partDeactivated(IWorkbenchPart part)
        {
        }

        public void partOpened(final IWorkbenchPart part)
        {
            if (part != InstallOptionsDesignEditor.this) {
                return;
            }
            IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
            window.getSelectionService().addSelectionListener(InstallOptionsDesignEditor.this);
            part.getSite().getWorkbenchWindow().getWorkbench().addWindowListener(mWindowListener);
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
    private static IPreferenceStore cPreferenceStore = InstallOptionsPlugin.getDefault().getPreferenceStore();

    static {
        cPreferenceStore.setDefault(PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
    }

    public InstallOptionsDesignEditor()
    {
        InstallOptionsPlugin.checkEditorAssociation();
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
        site.getKeyBindingService().setScopes(new String[]{EDITING_INSTALLOPTIONS_DESIGN_CONTEXT_ID});
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
        if(!mCreatedEmptyPart) {
            // If not the active editor, ignore selection changed.
            IEditorPart activeEditor = getSite().getPage().getActiveEditor();
            if(activeEditor != null) {
                Object adapter = activeEditor.getAdapter(getClass());
                if (this.equals(adapter)) {
                    updateActions(mSelectionActions);
                }
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
        if(!mCreatedEmptyPart) {
            getGraphicalViewer().getControl().setFocus();
        }
    }

    /**
     * Sets the graphicalViewer for this EditorPart.
     * @param viewer the graphical viewer
     */
    protected void setGraphicalViewer(GraphicalViewer viewer) 
    {
        this.mGraphicalViewer = viewer;
        getEditDomain().addViewer(viewer);
        getEditDomain().setPaletteRoot(getPaletteRoot());
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
        if(!mINIFile.hasErrors()) {
            mPalette = new FlyoutPaletteComposite(parent, SWT.NONE, getSite().getPage(),
                    getPaletteViewerProvider(), getPalettePreferences());
            createGraphicalViewer(mPalette);
            mPalette.setGraphicalControl(getGraphicalControl());
            if (mPalettePage != null) {
                mPalette.setExternalViewer(mPalettePage.getPaletteViewer());
                mPalettePage = null;
            }
            InstallOptionsModel.INSTANCE.addModelListener(mModelListener);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,PLUGIN_CONTEXT_PREFIX+"installoptions_designeditor_context"); //$NON-NLS-1$
            mCreatedEmptyPart = false;
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
        viewer.setRootEditPart(root);
       
        viewer.setEditPartFactory(GraphicalPartFactory.INSTANCE);
        ContextMenuProvider provider = new InstallOptionsDesignMenuProvider(this,//viewer,
                                                                    getActionRegistry());
        viewer.setContextMenu(provider);
        ((IEditorSite)getSite()).registerContextMenu("net.sf.eclipsensis.installoptions.editor.installoptionseditor.contextmenu", //$NON-NLS-1$
                provider, viewer, false);
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
            protected void configurePaletteViewer(PaletteViewer viewer) 
            {
                super.configurePaletteViewer(viewer);
                viewer.setContextMenu(new CustomPaletteContextMenuProvider(viewer));
                viewer.addDragSourceListener(new InstallOptionsTemplateTransferDragSourceListener(viewer));
            }

            public PaletteViewer createPaletteViewer(Composite parent)
            {
                PaletteViewer paletteViewer = super.createPaletteViewer(parent);
                paletteViewer.setPaletteViewerPreferences(new PaletteViewerPreferences(cPreferenceStore));
                return paletteViewer;
            }
        };
    }

    public void dispose()
    {
        InstallOptionsModel.INSTANCE.removeModelListener(mModelListener);
        boolean hasErrors = mINIFile.hasErrors();
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
                        }
                        catch (CoreException e1) {
                            e1.printStackTrace();
                        }
                    }
                    input.getDocumentProvider().disconnect(input);
                    mINIFile.disconnect(doc);
                }
            }
        }

        mPartListener = null;
        if(!hasErrors) {
            saveProperties(file);
        }
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
                    updateDocument(doc);
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

    private void updateDocument(IDocument doc) throws CoreException
    {
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(doc != null && dialog != null && dialog.canUpdateINIFile()) {
            dialog.updateINIFile();
            mINIFile.updateDocument(doc);
        }
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
        if (type == IContentOutlinePage.class) {
            mOutlinePage = new OutlinePage(new CustomTreeViewer());
            return mOutlinePage;
        }
        if (type == PalettePage.class) {
            if (mPalette == null) {
                mPalettePage = createPalettePage();
                return mPalettePage;
            }
            return createPalettePage();
        }
        if (type == org.eclipse.ui.views.properties.IPropertySheetPage.class) {
            PropertySheetPage page = new CustomPropertySheetPage((InstallOptionsEditDomain)getEditDomain());
            page.setRootEntry(new UndoablePropertySheetEntry(getEditDomain().getCommandStack()));//new CustomPropertySheetEntry((InstallOptionsEditDomain)getEditDomain()));
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
                return cPreferenceStore.getInt(PALETTE_DOCK_LOCATION);
            }

            public int getPaletteState()
            {
                return cPreferenceStore.getInt(PALETTE_STATE);
            }

            public int getPaletteWidth()
            {
                return cPreferenceStore.getInt(PALETTE_SIZE);
            }

            public void setDockLocation(int location)
            {
                cPreferenceStore.setValue(PALETTE_DOCK_LOCATION, location);
            }

            public void setPaletteState(int state)
            {
                cPreferenceStore.setValue(PALETTE_STATE, state);
            }

            public void setPaletteWidth(int width)
            {
                cPreferenceStore.setValue(PALETTE_SIZE, width);
            }
        };
    }

    protected PaletteRoot getPaletteRoot()
    {
        if (mRoot == null) {
            mRoot = InstallOptionsPaletteProvider.createPalette(getGraphicalViewer());
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
        IAction action;
        
        action = new RefreshDiagramAction(this);
        registry.registerAction(action);
        
        action = new ToggleDialogSizeVisibilityAction(this);
        registry.registerAction(action);
        
        action = new ToggleEnablementAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

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

        action = new CreateTemplateAction(this);
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
        action.setActionDefinitionId(IInstallOptionsConstants.SWITCH_EDITOR_COMMAND_ID);
        registry.registerAction(action);
        getEditorSite().getKeyBindingService().registerAction(action);

        final Shell shell;
        if (getGraphicalViewer() != null)
            shell= getGraphicalViewer().getControl().getShell();
        else
            shell= null;
        action = new Action(){
            public void run() {
                String[] preferencePages= {IInstallOptionsConstants.INSTALLOPTIONS_PREFERENCE_PAGE_ID};
                if (preferencePages.length > 0 && (shell == null || !shell.isDisposed()))
                    PreferencesUtil.createPreferenceDialogOn(shell, preferencePages[0], preferencePages, InstallOptionsDesignEditor.class).open();
            }
        };
        action.setId("net.sf.eclipsensis.installoptions.design_editor_prefs"); //$NON-NLS-1$
        action.setText(InstallOptionsPlugin.getResourceString("preferences.action.name")); //$NON-NLS-1$
        action.setToolTipText(InstallOptionsPlugin.getResourceString("preferences.action.tooltip")); //$NON-NLS-1$
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
        GraphicalViewer viewer = new ScrollingGraphicalViewer() {
            public void addDropTargetListener(final TransferDropTargetListener listener)
            {
                if(listener.getTransfer() instanceof TemplateTransfer) {
                    super.addDropTargetListener(new TransferDropTargetListener() {
                        public void dragEnter(DropTargetEvent event) 
                        {
                            listener.dragEnter(event);
                        }
                        
                        public void dragLeave(DropTargetEvent event) {
                            listener.dragLeave(event);
                        }
                        
                        public void dragOperationChanged(DropTargetEvent event) 
                        {
                            listener.dragOperationChanged(event);
                        }
                        
                        public void dragOver(DropTargetEvent event) 
                        {
                            listener.dragOver(event);
                        }
    
                        public void drop(DropTargetEvent event) 
                        {
                            listener.drop(event);
                        }
    
                        public void dropAccept(DropTargetEvent event) 
                        {
                            listener.dropAccept(event);
                        }
                        
                        public Transfer getTransfer() {
                            return InstallOptionsTemplateTransfer.INSTANCE;
                        }
                        
                        public boolean isEnabled(DropTargetEvent event) 
                        {
                            return listener.isEnabled(event);
                        }
                    });
                }
                else {
                    super.addDropTargetListener(listener);
                }
            }
        };
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
        return (input != null && input.getDocumentProvider().canSaveDocument(input)) || getCommandStack().isDirty();
    }

    private Object loadPreference(String name, TypeConverter converter, Object defaultValue)
    {
        Object o = null;
        try {
            o = converter.asType(cPreferenceStore.getString(name));
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
                }
                viewer.setProperty(RulerProvider.PROPERTY_VERTICAL_RULER,provider);
                
                ruler = dialog.getRuler(PositionConstants.NORTH);
                provider = null;
                if (ruler != null) {
                    provider = new InstallOptionsRulerProvider(ruler);
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
                updateDocument(doc);
                provider.aboutToChange(newInput);
                provider.saveDocument(new NullProgressMonitor(), newInput, doc, true);
                saveProperties(file);
                success= true;
                
            } 
            catch (CoreException x) {
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
        if(dialog != null && file.exists()) {
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
        }
    }

    public void setInput(IEditorInput input)
    {
        superSetInput(input);
        input = getEditorInput();
        if(input != null) {
            IDocument document = ((InstallOptionsEditorInput)input).getDocumentProvider().getDocument(input);
            mINIFile.connect(document);
            loadInstallOptionsDialog();
        }
    }

    private void loadInstallOptionsDialog()
    {
        InstallOptionsDialog dialog;
        if(!mINIFile.hasErrors()) {
            dialog = InstallOptionsDialog.loadINIFile(mINIFile);
        }
        else {
            dialog = new InstallOptionsDialog(null);
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
            ((InstallOptionsEditorInput)oldInput).getDocumentProvider().disconnect(oldInput);
        }
        if(input != null) {
            try {
                if(!(input instanceof InstallOptionsEditorInput)) {
                    IFile file = ((IFileEditorInput)input).getFile();
                    InstallOptionsNature.addNature(file.getProject());
                    input = new InstallOptionsEditorInput((IFileEditorInput)input);
                    ((InstallOptionsEditorInput)input).getDocumentProvider().connect(input);
                }
                else {
                    ((InstallOptionsEditorInput)input).getDocumentProvider().connect(input);
                    ((InstallOptionsEditorInput)input).completedSwitch();
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

    private void checkPerformSwitch()
    {
        if(mINIFile.hasErrors() && !isSwitching()) {
            prepareForSwitch();
            getSite().getShell().getDisplay().asyncExec(new Runnable(){
                public void run()
                {
                    MessageDialog.openError(getSite().getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                            InstallOptionsPlugin.getFormattedString("editor.switch.error", //$NON-NLS-1$
                                                    new String[]{((IFileEditorInput)getEditorInput()).getFile().getName()}));
                    getActionRegistry().getAction(SwitchEditorAction.ID).run();
                }
            });
        }
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

    private class OutlinePage extends ContentOutlinePage
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
            getViewer().setEditPartFactory(TreePartFactory.INSTANCE);
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
                            ((IFileEditorInput)getEditorInput()).getFile())) {
                return true;
            }

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
                    if(Common.isEmptyArray(delta.getMarkerDeltas())) {
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
            }
            return false;
        }
    }

    private class CustomTreeViewer extends TreeViewer
    {
        public CustomTreeViewer()
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

    private class CustomPaletteContextMenuProvider extends PaletteContextMenuProvider
    {
        public CustomPaletteContextMenuProvider(PaletteViewer palette)
        {
            super(palette);
        }

        public void buildContextMenu(IMenuManager menu)
        {
            super.buildContextMenu(menu);
            IContributionItem[] items = menu.getItems();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(items[i] instanceof ActionContributionItem) {
                        IAction action = ((ActionContributionItem)items[i]).getAction();
                        if(action.getClass().equals(SettingsAction.class)) {
                            menu.remove(items[i]);
                            break;
                        }
                    }
                }
            }
            menu.appendToGroup(GEFActionConstants.GROUP_REST, new PaletteSettingsAction(getPaletteViewer()));
            
            EditPart selectedPart = (EditPart)getPaletteViewer().getSelectedEditParts().get(0);
            Object model = selectedPart.getModel();
            if (model instanceof CombinedTemplateCreationEntry) {
                final Object template = ((CombinedTemplateCreationEntry)model).getTemplate();
                if(template instanceof InstallOptionsTemplate) {
                    menu.appendToGroup(GEFActionConstants.MB_ADDITIONS, new EditTemplateAction((InstallOptionsTemplate)template));
                    menu.appendToGroup(GEFActionConstants.MB_ADDITIONS, new DeleteTemplateAction((InstallOptionsTemplate)template));
                }
            }
        }
        
    }

    private class PaletteSettingsAction extends SettingsAction
    {
        private PaletteViewer mPaletteViewer;
        
        public PaletteSettingsAction(PaletteViewer palette)
        {
            super(palette);
            mPaletteViewer = palette;
        }

        public void run() 
        {
            Dialog settings = new CustomPaletteSettingsDialog(mPaletteViewer.getControl().getShell(), 
                                                              mPaletteViewer.getPaletteViewerPreferences());
            settings.open();
        }
    }

    private class PaletteViewerPreferences extends DefaultPaletteViewerPreferences
    {
        public PaletteViewerPreferences(IPreferenceStore store)
        {
            super(store);
            getPreferenceStore().setDefault(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED, true);
        }
        
        public void setUnloadCreationToolWhenFinished(boolean value)
        {
            getPreferenceStore().setValue(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED, value);
        }
        
        public boolean getUnloadCreationToolWhenFinished()
        {
            return getPreferenceStore().getBoolean(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED);
        }

        protected void handlePreferenceStorePropertyChanged(String property)
        {
            if(property.equals(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)) {
                firePropertyChanged(property,
                        new Boolean(getUnloadCreationToolWhenFinished()));
            }
            else {
                super.handlePreferenceStorePropertyChanged(property);
            }
        }
    }
    
    private class CustomPaletteSettingsDialog extends PaletteSettingsDialog
    {
        protected static final String CACHE_UNLOAD_CREATION_TOOL_WHEN_FINISHED = "unload creation tool when finished"; //$NON-NLS-1$
        protected static final int UNLOAD_CREATION_TOOL_WHEN_FINISHED_ID = CLIENT_ID + 1;
        
        private org.eclipse.gef.ui.palette.PaletteViewerPreferences mPrefs;
        private AbstractTemplateSettings mTemplateSettings;
        
        public CustomPaletteSettingsDialog(Shell parentShell, org.eclipse.gef.ui.palette.PaletteViewerPreferences prefs)
        {
            super(parentShell, prefs);
            mPrefs = prefs;
        }

        protected void cacheSettings()
        {
            super.cacheSettings();
            if(mPrefs instanceof PaletteViewerPreferences) {
                settings.put(CACHE_UNLOAD_CREATION_TOOL_WHEN_FINISHED, ((PaletteViewerPreferences)mPrefs).getUnloadCreationToolWhenFinished()?Boolean.TRUE:Boolean.FALSE);
            }
        }

        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getResourceString("settings.dialog.title")); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.icon"))); //$NON-NLS-1$
        }

        protected Control createDialogArea(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
            layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
            layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            applyDialogFont(composite);
            
            TabFolder mFolder = new TabFolder(parent, SWT.NONE);
            mFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            Dialog.applyDialogFont(mFolder);
            TabItem item = new TabItem(mFolder, SWT.NONE);
            item.setText(InstallOptionsPlugin.getResourceString("palette.settings.tab.name")); //$NON-NLS-1$
            item.setControl(createPaletteTab(mFolder));
            item = new TabItem(mFolder, SWT.NONE);
            item.setText(InstallOptionsPlugin.getResourceString("templates.settings.tab.name")); //$NON-NLS-1$
            item.setControl(createTemplatesTab(mFolder));
            
            return parent;
        }
        
        private Control createTemplatesTab(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(1,false));
            
            mTemplateSettings = new AbstractTemplateSettings(composite, SWT.NONE, InstallOptionsTemplateManager.INSTANCE) {
                protected AbstractTemplate createTemplate(String name)
                {
                    return new InstallOptionsTemplate(name);
                }

                protected Dialog createDialog(final AbstractTemplate template)
                {
                    InstallOptionsTemplateDialog dialog = new InstallOptionsTemplateDialog(getShell(), (InstallOptionsTemplate)template) {
                        protected void okPressed()
                        {
                            createUpdateTemplate();
                            AbstractTemplate t = getTemplate();
                            if(template != t) {
                                template.setName(t.getName());
                                template.setDescription(t.getDescription());
                                template.setEnabled(t.isEnabled());
                            }
                            setReturnCode(OK);
                            close();
                        }
                    };
                    return dialog;
                }
                
            };
            mTemplateSettings.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            
            Button b = new Button(composite,SWT.PUSH);
            b.setText(InstallOptionsPlugin.getResourceString("restore.defaults.label")); //$NON-NLS-1$
            b.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false, false));
            b.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected(SelectionEvent e)
                {
                    mTemplateSettings.performDefaults();
                }
            });
            return composite;
        }

        private Control createPaletteTab(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);
            
            GridLayout layout = (GridLayout)composite.getLayout();

            Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
            GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            data.horizontalSpan = layout.numColumns;
            label.setLayoutData(data);

            Control child = createCreationToolOptions(composite);
            data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            data.horizontalSpan = layout.numColumns;
            data.horizontalIndent = 5;
            child.setLayoutData(data);
            
            return composite;
        }

        /**
         * @param composite
         * @return
         */
        private Control createCreationToolOptions(Composite composite)
        {
            composite = new Composite(composite, SWT.NONE);
            composite.setLayout(new GridLayout(1,false));

            Label label = new Label(composite, SWT.NONE);
            label.setText(InstallOptionsPlugin.getResourceString("creation.tools.options")); //$NON-NLS-1$
            GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            label.setLayoutData(data);
            
            Button b = createButton(composite, UNLOAD_CREATION_TOOL_WHEN_FINISHED_ID,
                    InstallOptionsPlugin.getResourceString("unload.creation.tool.when.finished.label"), //$NON-NLS-1$
                    SWT.CHECK,null);
            ((GridData)b.getLayoutData()).horizontalIndent = 5;
            b.setSelection(((PaletteViewerPreferences)mPrefs).getUnloadCreationToolWhenFinished());
            return composite;
        }

        protected void restoreSettings()
        {
            super.restoreSettings();
            if(mPrefs instanceof PaletteViewerPreferences) {
                ((PaletteViewerPreferences)mPrefs).setUnloadCreationToolWhenFinished(((Boolean)settings.get(CACHE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)).booleanValue());
            }
        }

        protected void okPressed()
        {
            if(mTemplateSettings.performOk()) {
                super.okPressed();
            }
        }

        protected void buttonPressed(int buttonId)
        {
            if(buttonId == UNLOAD_CREATION_TOOL_WHEN_FINISHED_ID) {
                Button b = getButton(buttonId);
                handleUnloadCreationToolWhenFinishedChanged(b.getSelection());
            }
            else {
                super.buttonPressed(buttonId);
            }
        }
        
        private void handleUnloadCreationToolWhenFinishedChanged(boolean value)
        {
            if(mPrefs instanceof PaletteViewerPreferences) {
                ((PaletteViewerPreferences)mPrefs).setUnloadCreationToolWhenFinished(value);
            }
        }
    }
}