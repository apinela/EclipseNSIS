/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;
import net.sf.eclipsensis.wizard.NSISWizardDisplayValues;
import net.sf.eclipsensis.wizard.settings.NSISInstallFiles;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISInstallFilesDialog extends AbstractNSISInstallItemDialog
{
    private static ArrayList cProperties = new ArrayList();
    private Collection mFiles = null;
    
    static {
        cProperties.add("destination");
        cProperties.add("files");
        cProperties.add("overwriteMode");
    }

    public NSISInstallFilesDialog(Shell parentShell, NSISInstallFiles item)
    {
        super(parentShell, item);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        mFiles = new LinkedHashSet(Arrays.asList(Common.tokenize(mStore.getString("files"),'\0')));
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    protected List getProperties()
    {
        return cProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 400;
        composite.setLayoutData(gd);
        
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.LEFT);
        label.setText(EclipseNSISPlugin.getResourceString("wizard.source.files.label"));
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);
        
        Composite composite2 = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.heightHint = 120;
        composite2.setLayoutData(gd);
        
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
  
        final Table table = new Table(composite2, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalSpan = 2;
        table.setLayoutData(gd);

        TableColumn tableColumn = new TableColumn(table,SWT.LEFT,0);
        tableColumn.setText(EclipseNSISPlugin.getResourceString("wizard.file.name.label"));
        
        composite2 = new Composite(composite2, SWT.NONE);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 1;
        composite2.setLayoutData(gd);
        
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Button addButton = new Button(composite2,SWT.PUSH);
        addButton.setText(EclipseNSISPlugin.getResourceString("wizard.add.files.label"));
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        addButton.setLayoutData(gd);

        final Button removeButton = new Button(composite2,SWT.PUSH);
        removeButton.setText(EclipseNSISPlugin.getResourceString("wizard.remove.files.label"));
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        removeButton.setLayoutData(gd);
        removeButton.setEnabled(false);

        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new CollectionLabelProvider());
        viewer.setInput(mFiles);

        final ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        addButton.addSelectionListener(new SelectionAdapter() {
            String filterPath = "";
            
            public void widgetSelected(SelectionEvent e) 
            {
                FileDialog dialog = new FileDialog(getShell(),SWT.OPEN|SWT.MULTI|SWT.PRIMARY_MODAL);
                dialog.setText(EclipseNSISPlugin.getResourceString("wizard.files.dialog.title"));
                dialog.setFilterNames(Common.loadArrayProperty(bundle,"wizard.source.file.filternames"));
                dialog.setFilterExtensions(Common.loadArrayProperty(bundle,"wizard.source.file.filters"));
                if(!Common.isEmpty(filterPath)) {
                    dialog.setFilterPath(filterPath);
                }
                if(dialog.open() != null) {
                    filterPath = dialog.getFilterPath();
                    String[] fileNames = dialog.getFileNames();
                    for (int i = 0; i < fileNames.length; i++) {
                        mFiles.add(new StringBuffer(filterPath).append("\\").append(fileNames[i]).toString());
                    }
                    viewer.refresh();
                    setComplete(validate());
                }
            }
        });

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    mFiles.remove(iter.next());
                }
                viewer.refresh();
                setComplete(validate());
            }
        });
        
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
            }
        });

        table.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) 
            {
                Table table  = (Table)e.widget;
                int width = table.getSize().x - 2*table.getBorderWidth();
                int lineWidth = table.getGridLineWidth();
                TableColumn[] columns = table.getColumns();
                width -= (columns.length-1)*lineWidth;
                for(int i=0; i<columns.length; i++) {
                    columns[i].setWidth(width/columns.length);
                }
            }
        });
        

        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,NSISKeywords.PREDEFINED_PATH_VARIABLES,mStore.getString("destination"),
                                                         false,"wizard.destination.label",true,null,false);
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination",c1.getText());
                setComplete(validate());
            }
        });
        gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.OVERWRITE_MODE_NAMES,mStore.getInt("overwriteMode"),
                true,"wizard.overwrite.label",true,null,false);
        c2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("overwrite",c2.getSelectionIndex());
            }
        });
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        StringBuffer buf = new StringBuffer("");
        if(mFiles.size() > 0) {
            Iterator iter = mFiles.iterator();
            buf.append(iter.next());
            for(; iter.hasNext(); ) {
                buf.append(NSISInstallFiles.SEPARATOR).append(iter.next());
            }
        }
        mStore.setValue("files",buf.toString());
        super.okPressed();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#validate()
     */
    protected boolean validate()
    {
        return mFiles.size() > 0 && Common.isValidNSISPrefixedPathName(mStore.getString("destination"));
    }
}
