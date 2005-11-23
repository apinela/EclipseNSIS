/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *     
 * Based upon org.eclipse.debug.ui.CommonTab
 *     
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

public class CommonTab extends AbstractLaunchConfigurationTab 
{
    private Button mLocalRadioButton;
    private Button mSharedRadioButton;
    private Text mSharedLocationText;
    private Button mSharedLocationButton;
    protected Button mLaunchInBackgroundButton;
    private Map mLaunchGroups = new HashMap();
    private CheckboxTableViewer mFavoritesTable;
    private ModifyListener fBasicModifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
    };
    private Button mConsoleOutput;
    private Button mFileOutput;
    private Button mFileBrowse;
    private Text mFileText;
    private Button mVariables;
    private Button mAppend;
    private Button mWorkspaceBrowse;

    public void createControl(Composite parent) 
    {
        ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
        for (int i = 0; i < groups.length; i++) {
            mLaunchGroups.put(groups[i].getIdentifier(),groups[i]);
        }
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
// FIXME        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_COMMON_TAB);
        GridLayout topLayout = new GridLayout(1, false);
        topLayout.horizontalSpacing = 10;
        comp.setLayout(topLayout);
        comp.setFont(parent.getFont());
        
        Group group = new Group(comp, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        group.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        group.setLayoutData(gd);
        group.setText(EclipseNSISPlugin.getResourceString("launchconfig.saveas.group.name")); //$NON-NLS-1$
        group.setFont(comp.getFont());
                
        mLocalRadioButton = new Button(group, SWT.RADIO);
        mLocalRadioButton.setText(EclipseNSISPlugin.getResourceString("launchconfig.local.file.label")); //$NON-NLS-1$
        gd = new GridData();
        gd.horizontalSpan = 3;
        mLocalRadioButton.setLayoutData(gd);
        mSharedRadioButton = new Button(group, SWT.RADIO);
        mSharedRadioButton.setText(EclipseNSISPlugin.getResourceString("launchconfig.shared.file.label")); //$NON-NLS-1$
        mSharedRadioButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                handleSharedRadioButtonSelected();
            }
        });
        gd = new GridData();
        mSharedRadioButton.setLayoutData(gd);
                
        mSharedLocationText = new Text(group, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        mSharedLocationText.setLayoutData(gd);
        mSharedLocationText.addModifyListener(fBasicModifyListener);
        
        mSharedLocationButton = createPushButton(group, EclipseNSISPlugin.getResourceString("launchconfig.browse.label"), null); //$NON-NLS-1$
        mSharedLocationButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                handleSharedLocationButtonSelected();
            }
        }); 

        mLocalRadioButton.setSelection(true);
        setSharedEnabled(false);
        
        Group favComp = new Group(comp, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        favComp.setLayoutData(gd);
        GridLayout favLayout = new GridLayout();
        favComp.setLayout(favLayout);
        
        favComp.setText(EclipseNSISPlugin.getResourceString("launchconfig.favorites.group.name")); //$NON-NLS-1$
        favComp.setFont(parent.getFont());
        
        mFavoritesTable = CheckboxTableViewer.newCheckList(favComp, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        Control table = mFavoritesTable.getControl();
        gd = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gd);
        
        mFavoritesTable.setContentProvider(new FavoritesContentProvider());
        mFavoritesTable.setLabelProvider(new FavoritesLabelProvider());
        mFavoritesTable.addCheckStateListener(
            new ICheckStateListener() {
                public void checkStateChanged(CheckStateChangedEvent event) {
                    updateLaunchConfigurationDialog();
                }
            });
        
        createOutputCaptureComponent(comp);
        createLaunchInBackgroundComponent(comp);
        
        Dialog.applyDialogFont(parent);
    }
    
    private void createOutputCaptureComponent(Composite parent) 
    {
        Group group = new Group(parent, SWT.NONE);
        group.setText(EclipseNSISPlugin.getResourceString("launchconfig.stdout.group.name")); //$NON-NLS-1$
        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        gd.horizontalSpan = 1;
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout(5, false);
        group.setLayout(layout);
        group.setFont(parent.getFont());
        
        mConsoleOutput = createCheckButton(group, EclipseNSISPlugin.getResourceString("launchconfig.stdout.console.label")); //$NON-NLS-1$
        gd = new GridData(SWT.BEGINNING, SWT.NORMAL, true, false);
        gd.horizontalSpan = 5;
        mConsoleOutput.setLayoutData(gd);
        
        mConsoleOutput.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        mFileOutput = createCheckButton(group, EclipseNSISPlugin.getResourceString("launchconfig.stdout.file.name")); //$NON-NLS-1$
        mFileOutput.setLayoutData(new GridData(SWT.BEGINNING, SWT.NORMAL, false, false));
        
        mFileText = new Text(group, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.NORMAL, true, false);
        gd.horizontalSpan = 4;
        mFileText.setLayoutData(gd);
        
        Label spacer = new Label(group,SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.NORMAL, true, false);
        gd.horizontalSpan=2;
        spacer.setLayoutData(gd);
        mWorkspaceBrowse = createPushButton(group, EclipseNSISPlugin.getResourceString("launchconfig.browse.workspace.label"), null); //$NON-NLS-1$
        mFileBrowse = createPushButton(group, EclipseNSISPlugin.getResourceString("launchconfig.browse.filesystem.label"), null); //$NON-NLS-1$
        mVariables = createPushButton(group, EclipseNSISPlugin.getResourceString("launchconfig.variables.label"), null); //$NON-NLS-1$

        spacer = new Label(group,SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.NORMAL, false, false));
        mAppend = createCheckButton(group, EclipseNSISPlugin.getResourceString("launchconfig.stdout.append.label")); //$NON-NLS-1$
        gd = new GridData(SWT.LEFT, SWT.TOP, true, false);
        gd.horizontalSpan = 4;
        mAppend.setLayoutData(gd);
        
        mFileOutput.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = mFileOutput.getSelection();
                mFileText.setEnabled(enabled);
                mFileBrowse.setEnabled(enabled);
                mWorkspaceBrowse.setEnabled(enabled);
                mVariables.setEnabled(enabled);
                mAppend.setEnabled(enabled);
                updateLaunchConfigurationDialog();
            }
        });
        
        mAppend.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        mWorkspaceBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
                dialog.setTitle(EclipseNSISPlugin.getResourceString("launchconfig.stdout.select.resource.title")); //$NON-NLS-1$
                dialog.setMessage(EclipseNSISPlugin.getResourceString("launchconfig.stdout.select.resource.message")); //$NON-NLS-1$
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
                dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
                int buttonId = dialog.open();
                if (buttonId == IDialogConstants.OK_ID) {
                    IResource resource = (IResource) dialog.getFirstResult();
                    String arg = resource.getFullPath().toString();
                    String fileLoc = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
                    mFileText.setText(fileLoc);
                }
            }
        });
        
        mFileBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String filePath = mFileText.getText();
                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                
                filePath = dialog.open();
                if (filePath != null) {
                    mFileText.setText(filePath);
                }
            }
        });
        
        mFileText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        mVariables.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
                dialog.open();
                String variable = dialog.getVariableExpression();
                if (variable != null) {
                    mFileText.insert(variable);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {   
            }
        });
    }

    private void createLaunchInBackgroundComponent(Composite parent) 
    {
        mLaunchInBackgroundButton = createCheckButton(parent, EclipseNSISPlugin.getResourceString("launchconfig.launch.background.label")); //$NON-NLS-1$
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 1;
        mLaunchInBackgroundButton.setLayoutData(data);
        mLaunchInBackgroundButton.setFont(parent.getFont());
        mLaunchInBackgroundButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    
    private void handleSharedRadioButtonSelected() 
    {
        setSharedEnabled(isShared());
        updateLaunchConfigurationDialog();
    }
    
    private void setSharedEnabled(boolean enable) 
    {
        mSharedLocationText.setEnabled(enable);
        mSharedLocationButton.setEnabled(enable);
    }
    
    private boolean isShared() 
    {
        return mSharedRadioButton.getSelection();
    }
    
    private void handleSharedLocationButtonSelected() 
    {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
                                                                       getWorkspaceRoot(),
                                                                       false,
                                                                       EclipseNSISPlugin.getResourceString("launchconfig.saveas.location.prompt")); //$NON-NLS-1$
        
        String currentContainerString = mSharedLocationText.getText();
        IContainer currentContainer = getContainer(currentContainerString);
        if (currentContainer != null) {
            IPath path = currentContainer.getFullPath();
            dialog.setInitialSelections(new Object[] {path});
        }
        
        dialog.showClosedProjects(false);
        dialog.open();
        Object[] results = dialog.getResult();      
        if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
            IPath path = (IPath)results[0];
            String containerName = path.toOSString();
            mSharedLocationText.setText(containerName);
        }       
    }
    
    private IContainer getContainer(String path) 
    {
        Path containerPath = new Path(path);
        return (IContainer) getWorkspaceRoot().findMember(containerPath);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) 
    {    
        updateLocalSharedFromConfig(configuration);
        updateSharedLocationFromConfig(configuration);
        updateFavoritesFromConfig(configuration);
        updateLaunchInBackground(configuration);
        updateConsoleOutput(configuration);
    }
    
    private void updateConsoleOutput(ILaunchConfiguration configuration) 
    {
        boolean outputToConsole = true;
        String outputFile = null;
        boolean append = false;
        
        try {
            outputToConsole = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
            outputFile = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String)null);
            append = configuration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
        } catch (CoreException e) {
        }
        
        mConsoleOutput.setSelection(outputToConsole);
        mAppend.setSelection(append);
        boolean haveOutputFile= outputFile != null;
        if (haveOutputFile) {
            mFileText.setText(outputFile);
        }
        mFileOutput.setSelection(haveOutputFile);
        mFileText.setEnabled(haveOutputFile);
        mFileBrowse.setEnabled(haveOutputFile);
        mWorkspaceBrowse.setEnabled(haveOutputFile);
        mVariables.setEnabled(haveOutputFile);
        mAppend.setEnabled(haveOutputFile);
    }

    private void updateLaunchInBackground(ILaunchConfiguration configuration) 
    { 
        mLaunchInBackgroundButton.setSelection(isLaunchInBackground(configuration));
    }
    
    public static boolean isLaunchInBackground(ILaunchConfiguration configuration) 
    {
        boolean launchInBackground= true;
        try {
            launchInBackground= configuration.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
        } catch (CoreException ce) {
            EclipseNSISPlugin.getDefault().log(ce);
        }
        return launchInBackground;
    }
    
    private void updateLocalSharedFromConfig(ILaunchConfiguration config) 
    {
        boolean isShared = !config.isLocal();
        mSharedRadioButton.setSelection(isShared);
        mLocalRadioButton.setSelection(!isShared);
        setSharedEnabled(isShared);
    }
    
    private void updateSharedLocationFromConfig(ILaunchConfiguration config) 
    {
        mSharedLocationText.setText(""); //$NON-NLS-1$
        IFile file = config.getFile();
        if (file != null) {
            IContainer parent = file.getParent();
            if (parent != null) {
                String containerName = parent.getFullPath().toOSString();
                mSharedLocationText.setText(containerName);
            }
        }
    }
        
    private void updateFavoritesFromConfig(ILaunchConfiguration config) 
    {
        mFavoritesTable.setInput(config);
        mFavoritesTable.setCheckedElements(new Object[]{});
        try {
            List groups = config.getAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, new ArrayList());
            if (!groups.isEmpty()) {
                List list = new ArrayList();
                Iterator iterator = groups.iterator();
                while (iterator.hasNext()) {
                    String id = (String)iterator.next();
                    ILaunchGroup extension = (ILaunchGroup)mLaunchGroups.get(id);
                    list.add(extension);
                }
                mFavoritesTable.setCheckedElements(list.toArray());
            }
        } catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    private void updateConfigFromLocalShared(ILaunchConfigurationWorkingCopy config) 
    {
        if (isShared()) {
            String containerPathString = mSharedLocationText.getText();
            IContainer container = getContainer(containerPathString);
            config.setContainer(container);
        } 
        else {
            config.setContainer(null);
        }
    }
        
    private void updateConfigFromFavorites(ILaunchConfigurationWorkingCopy config) 
    {
        Object[] checked = mFavoritesTable.getCheckedElements();
        List groups = null;
        for (int i = 0; i < checked.length; i++) {
            ILaunchGroup group = (ILaunchGroup)checked[i];
            if (groups == null) {
                groups = new ArrayList();
            }
            groups.add(group.getIdentifier());
        }
        config.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, groups);
    }   
    
    private IWorkspaceRoot getWorkspaceRoot() 
    {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    public boolean isValid(ILaunchConfiguration config) 
    {
        setMessage(null);
        setErrorMessage(null);
        
        return validateLocalShared() && validateRedirectFile();
    }
    
    private boolean validateRedirectFile() 
    {
        if(mFileOutput.getSelection()) {
            int len = mFileText.getText().trim().length();
            if (len == 0) {
                setErrorMessage(EclipseNSISPlugin.getResourceString("launchconfig.stdout.file.missing.error")); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    private boolean validateLocalShared() 
    {
        if (isShared()) {
            String path = mSharedLocationText.getText().trim();
            IContainer container = getContainer(path);
            if (container == null || container.equals(ResourcesPlugin.getWorkspace().getRoot())) {
                setErrorMessage(EclipseNSISPlugin.getResourceString("launchconfig.saveas.invalid.location.error")); //$NON-NLS-1$
                return false;
            } 
            else if (!container.getProject().isOpen()) {
                setErrorMessage(EclipseNSISPlugin.getResourceString("launchconfig.saveas.closed.project.error")); //$NON-NLS-1$
                return false;               
            }
        }
        
        return true;        
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy config) 
    {
        config.setContainer(null);
        config.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) 
    {
        updateConfigFromLocalShared(configuration);
        updateConfigFromFavorites(configuration);
        setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, configuration, mLaunchInBackgroundButton.getSelection(), true);
        boolean captureOutput = false;
        if (mConsoleOutput.getSelection()) {
            captureOutput = true;
            configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, (String)null);
        } 
        else {
            configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
        }
        if (mFileOutput.getSelection()) {
            captureOutput = true;
            String file = mFileText.getText();
            configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, file);
            if(mAppend.getSelection()) {
                configuration.setAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, true);
            } 
            else {
                configuration.setAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, (String)null);
            }
        } 
        else {
            configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String)null);
        }
        
        if (!captureOutput) {
            configuration.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
        } 
        else {
            configuration.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, (String)null);
        }
    }

    public String getName() 
    {
        return EclipseNSISPlugin.getResourceString("launchconfig.common.tab.name"); //$NON-NLS-1$
    }
    
    public boolean canSave() 
    {
        return validateLocalShared();
    }

    public Image getImage() 
    {
        return EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("common.tab.icon")); //$NON-NLS-1$
    }

    private class FavoritesContentProvider implements IStructuredContentProvider 
    {
        public Object[] getElements(Object inputElement) 
        {
            ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
            List possibleGroups = new ArrayList();
            ILaunchConfiguration configuration = (ILaunchConfiguration)inputElement;
            for (int i = 0; i < groups.length; i++) {
                if (accepts(groups[i], configuration)) {
                    possibleGroups.add(groups[i]);
                } 
            }
            return possibleGroups.toArray();
        }

        private boolean accepts(ILaunchGroup group, ILaunchConfiguration configuration)
        {
            try {
                if (configuration.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false)) {
                    return false;
                }
                if (configuration.getType().supportsMode(group.getMode())) {
                    String launchCategory = null;
                    launchCategory = configuration.getCategory();
                    String category = group.getCategory();
                    if (launchCategory == null || category == null) {
                        return launchCategory == category;
                    }
                    return category.equals(launchCategory);
                }
            } catch (CoreException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            return false;
        }
        
        public void dispose() 
        {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
        {
        }
    }
    
    private class FavoritesLabelProvider implements ITableLabelProvider 
    {
        Map fImages = new HashMap();
        
        public Image getColumnImage(Object element, int columnIndex) 
        {
            Image image = (Image)fImages.get(element);
            if (image == null) {
                ImageDescriptor descriptor = ((ILaunchGroup)element).getImageDescriptor();
                if (descriptor != null) {
                    image = descriptor.createImage();
                    fImages.put(element, image);
                }
            }
            return image;
        }

        public String getColumnText(Object element, int columnIndex) 
        {
            String label = ((ILaunchGroup)element).getLabel();
            return removeAccelerators(label);
        }

        private String removeAccelerators(String label) 
        {
            String title = label;
            if (title != null) {
                // strip out any '&' (accelerators)
                int index = title.indexOf('&');
                if (index == 0) {
                    title = title.substring(1);
                } else if (index > 0) {
                    //DBCS languages use "(&X)" format
                    if (title.charAt(index - 1) == '(' && title.length() >= index + 3 && title.charAt(index + 2) == ')') {
                        String first = title.substring(0, index - 1);
                        String last = title.substring(index + 3);
                        title = first + last;
                    } else if (index < (title.length() - 1)) {
                        String first = title.substring(0, index);
                        String last = title.substring(index + 1);
                        title = first + last;
                    }
                }
            }
            return title;
        }

        public void addListener(ILabelProviderListener listener) 
        {
        }

        public void dispose() 
        {
            Iterator images = fImages.values().iterator();
            while (images.hasNext()) {
                Image image = (Image)images.next();
                image.dispose();
            }
        }

        public boolean isLabelProperty(Object element, String property) 
        {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) 
        {
        }
    }
    
    public void activated(ILaunchConfigurationWorkingCopy workingCopy) 
    {
    }

    public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) 
    {
    }
}