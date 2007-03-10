/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class NSISAssociatedHeadersPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
    private TableViewer mViewer;
    private Button mReparentWarning;
    private Collection mOriginalHeaders;
    private HashSet mHeaders;
    private NSISHeaderAssociationManager mHeaderAssociationManager = NSISHeaderAssociationManager.getInstance();

    public NSISAssociatedHeadersPropertyPage()
    {
    }

    public void createControl(Composite parent)
    {
        //TODO Set description
//        setDescription(getPageDescription());
        super.createControl(parent);
        //TODO Set context help
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),getContextId());
    }

    protected Control createContents(Composite parent)
    {
        mOriginalHeaders = mHeaderAssociationManager.getAssociatedHeaders((IFile)getElement());
        mHeaders = new HashSet();
        for (Iterator iter = mOriginalHeaders.iterator(); iter.hasNext();) {
            IFile header = (IFile)iter.next();
            if(IOUtility.isValidFile(header)) {
                mHeaders.add(header);
            }
        }
        final IFilter filter = new IFilter() {
            public boolean select(Object toTest)
            {
                if(toTest instanceof IFile) {
                    String ext = ((IFile)toTest).getFileExtension();
                    if (ext != null && ext.equalsIgnoreCase(INSISConstants.NSH_EXTENSION)) {
                        return mHeaders != null && !mHeaders.contains(toTest);
                    }
                }
                return false;
            }
        };

        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = layout.marginWidth = 0;
        composite.setLayout(layout);

        Label l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("associated.headers.title")); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
        data.horizontalSpan = 2;
        l.setLayoutData(data);

        Table table = new Table(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn column = new TableColumn(table,SWT.LEFT,0);
        column.setText(EclipseNSISPlugin.getResourceString("associated.headers.column.label")); //$NON-NLS-1$
        table.addControlListener(new TableResizer());

        mViewer = new TableViewer(table);
        mViewer.setContentProvider(new CollectionContentProvider());
        mViewer.setLabelProvider(new CollectionLabelProvider() {
            public String getColumnText(Object element, int columnIndex)
            {
                if(element instanceof IFile) {
                    return ((IFile)element).getFullPath().toString();
                }
                return null;
            }
        });
        mViewer.setComparator(new ViewerComparator() {
            public int compare(Viewer viewer, Object e1, Object e2)
            {
                if(e1 instanceof IFile && e2 instanceof IFile) {
                    return ((IFile)e1).getFullPath().toString().compareTo(((IFile)e2).getFullPath().toString());
                }
                return super.compare(viewer, e1, e2);
            }
        });
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.verticalSpan = 2;
        table.setLayoutData(data);
        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setImage(CommonImages.ADD_ICON);
        addButton.setToolTipText(EclipseNSISPlugin.getResourceString("add.associated.header.toolip")); //$NON-NLS-1$
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0)
            {
                FileSelectionDialog dialog = new FileSelectionDialog(getShell(), null, filter);
                dialog.setDialogMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
                dialog.setHelpAvailable(false);
                if (dialog.open() == Window.OK) {
                    IFile file = dialog.getFile();
                    if(!mHeaders.contains(file)) {
                        mHeaders.add(file);
                        mViewer.refresh(false);
                    }
                }
            }
        });
        addButton.setLayoutData(new GridData(SWT.FILL,SWT.TOP,false,false));

        final Button removeButton = new Button(composite, SWT.PUSH);
        removeButton.setImage(CommonImages.DELETE_ICON);
        removeButton.setToolTipText(EclipseNSISPlugin.getResourceString("remove.associated.header.toolip")); //$NON-NLS-1$
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0)
            {
                IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                if(!sel.isEmpty()) {
                    mHeaders.removeAll(sel.toList());
                    mViewer.refresh(false);
                }
            }
        });
        data = new GridData(SWT.FILL,SWT.TOP,false,false);
        data.verticalSpan = 2;
        removeButton.setLayoutData(data);

        Composite c = new Composite(composite,SWT.NONE);
        data = new GridData(SWT.FILL,SWT.FILL,true,false);
        c.setLayoutData(data);
        layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        c.setLayout(layout);

        mReparentWarning = new Button(c,SWT.CHECK);
        mReparentWarning.setLayoutData(new GridData(SWT.FILL,SWT.TOP,false,false));

        l = new Label(c,SWT.WRAP);
        l.setText(EclipseNSISPlugin.getResourceString("show.associated.header.warning.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,true,false));

        mViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
            }
        });

        mViewer.setInput(mHeaders);
        return composite;
    }

    protected void performDefaults()
    {
        if(mViewer != null && mHeaders != null) {
            mHeaders.clear();
            mViewer.refresh(false);
        }
        super.performDefaults();
    }

    public boolean performOk()
    {
        IFile file = (IFile)getElement();
        Set removedHeaders = new HashSet(mOriginalHeaders);
        removedHeaders.removeAll(mHeaders);
        Set addedHeaders = new HashSet(mHeaders);
        addedHeaders.removeAll(mOriginalHeaders);
        for (Iterator iter = removedHeaders.iterator(); iter.hasNext();) {
            mHeaderAssociationManager.disassociateFromScript((IFile)iter.next());
        }
        for (Iterator iter = addedHeaders.iterator(); iter.hasNext();) {
            mHeaderAssociationManager.associateWithScript((IFile)iter.next(), file);
        }
        return super.performOk();
    }

}
