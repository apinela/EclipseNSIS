/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class FileSelectionDialog extends TitleAreaDialog
{
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final int VIEWER_WIDTH = 200;
    private static final int VIEWER_HEIGHT = 300;
    
    private IFilter mFilter = null;
    private IFile mFile = null;
    
    public FileSelectionDialog(Shell parentShell, IFile file, IFilter filter)
    {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        mFile = file;
        mFilter = filter;
    }
    
    public FileSelectionDialog(Shell parentShell, IFile file)
    {
        this(parentShell, file, null);
    }
    
    public FileSelectionDialog(Shell parentShell, IFilter filter)
    {
        this(parentShell, null, filter);
    }
    
    public FileSelectionDialog(Shell parentShell)
    {
        this(parentShell, null, null);
    }

    public IFile getFile()
    {
        return mFile;
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(EclipseNSISPlugin.getResourceString("fileselection.dialog.title")); //$NON-NLS-1$
        shell.setImage(EclipseNSISPlugin.getShellImage());
    }

    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);
        setTitle(EclipseNSISPlugin.getResourceString("fileselection.dialog.header")); //$NON-NLS-1$
        setTitleImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("file.selection.dialog.icon"))); //$NON-NLS-1$
        setMessage(EclipseNSISPlugin.getResourceString("fileselection.dialog.message")); //$NON-NLS-1$

        Button button = getButton(IDialogConstants.OK_ID);
        if(button != null) {
            button.setEnabled(mFile != null);
        }
        return contents;
    }
    protected Control createDialogArea(Composite parent)
    {
        parent = (Composite)super.createDialogArea(parent);
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        GridLayout layout = new GridLayout(1,true);
        composite.setLayout(layout);

        SashForm form = new SashForm(composite,SWT.HORIZONTAL);
        form.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        
        layout = new GridLayout(1,true);
        form.setLayout(layout);
        
        composite = new Composite(form,SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(1,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Label l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("fileselection.parent.folder.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        Tree tree = new Tree(composite,SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
        gridData.widthHint = VIEWER_WIDTH;
        gridData.heightHint = VIEWER_HEIGHT;
        tree.setLayoutData(gridData);
        final TreeViewer tv = new TreeViewer(tree);
        tv.setContentProvider(new ContainerContentProvider());
        tv.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        tv.setSorter(new ViewerSorter());

        
        composite = new Composite(form,SWT.None);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(1,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("fileselection.file.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        Table table = new Table(composite,SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER|SWT.FULL_SELECTION);
        gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
        gridData.widthHint = VIEWER_WIDTH;
        gridData.heightHint = VIEWER_HEIGHT;
        table.setLayoutData(gridData);
        table.setLinesVisible(false);
        final TableViewer tv2 = new TableViewer(table);
        tv2.setContentProvider(new FilesContentProvider());
        tv2.setLabelProvider(new FilesLabelProvider());
        tv2.setSorter(new ViewerSorter());
        if(mFilter != null) {
            tv2.addFilter(new ViewerFilter() {
                public boolean select(Viewer viewer, Object parentElement, Object element)
                {
                    return mFilter.select(element);
                }
            });
        }
        
        tv.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                tv2.setInput(selection.getFirstElement()); // allow null
            }
        });
        tv.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) 
            {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object item = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (tv.getExpandedState(item)) {
                        tv.collapseToLevel(item, 1);
                    }
                    else {
                        tv.expandToLevel(item, 1);
                    }
                }
            }
        });
        
        tv2.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if(selection != null && !selection.isEmpty()) {
                    mFile = (IFile)selection.getFirstElement();
                }
                else {
                    mFile = null;
                }
                Button button = getButton(IDialogConstants.OK_ID);
                if(button != null) {
                    button.setEnabled(selection != null && !selection.isEmpty());
                }
            }
        });
        tv2.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) 
            {
                if(mFile != null) {
                    setReturnCode(Window.OK);
                    close();
                }
            }
        });
        
        tv.setInput(ResourcesPlugin.getWorkspace());
        if(mFile != null) {
            tv.setSelection(new StructuredSelection(mFile.getParent()));
            tv2.setSelection(new StructuredSelection(mFile));
        }
        else {
            tv2.setSelection(new StructuredSelection());
        }
        return parent;
    }

    private class ContainerContentProvider implements ITreeContentProvider
    {
        public void dispose() 
        {
        }

        public Object[] getChildren(Object element) 
        {
            if (element instanceof IWorkspace) {
                return ((IWorkspace) element).getRoot().getProjects();
            } 
            else if (element instanceof IContainer) {
                IContainer container = (IContainer) element;
                if (container.isAccessible()) {
                    try {
                        List children = new ArrayList();
                        IResource[] members = container.members();
                        for (int i = 0; i < members.length; i++) {
                            if (members[i].getType() != IResource.FILE) {
                                children.add(members[i]);
                            }
                        }
                        return children.toArray();
                    } 
                    catch (CoreException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
            return EMPTY_ARRAY;
        }

        public Object[] getElements(Object element) 
        {
            return getChildren(element);
        }

        public Object getParent(Object element) 
        {
            if (element instanceof IResource) {
                return ((IResource) element).getParent();
            }
            return null;
        }

        public boolean hasChildren(Object element) 
        {
            return getChildren(element).length > 0;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
        {
        }
    }
    
    private class FilesLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        private ILabelProvider mLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
        
        public Image getColumnImage(Object element, int columnIndex)
        {
            return mLabelProvider.getImage(element);
        }

        public String getColumnText(Object element, int columnIndex)
        {
            return mLabelProvider.getText(element);
        }
    }

    private class FilesContentProvider implements IStructuredContentProvider
    {
        public void dispose() 
        {
        }

        public Object[] getElements(Object element) 
        {
            if (element instanceof IContainer) {
                IContainer container = (IContainer) element;
                if (container.isAccessible()) {
                    try {
                        List children = new ArrayList();
                        IResource[] members = container.members();
                        for (int i = 0; i < members.length; i++) {
                            if (members[i].getType() == IResource.FILE) {
                                children.add(members[i]);
                            }
                        }
                        return children.toArray();
                    } 
                    catch (CoreException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
            return EMPTY_ARRAY;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
        {
        }
    }
}
