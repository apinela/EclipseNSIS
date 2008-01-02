/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallFiles;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstallFilesDialog extends AbstractNSISInstallItemDialog
{
    private static ArrayList cProperties = new ArrayList();
    private Collection mFiles = null;

    static {
        cProperties.add("destination"); //$NON-NLS-1$
        cProperties.add("files"); //$NON-NLS-1$
        cProperties.add("overwriteMode"); //$NON-NLS-1$
    }

    public NSISInstallFilesDialog(NSISWizard wizard, NSISInstallFiles item)
    {
        super(wizard, item);
        mStore.setDefault("overwriteMode",OVERWRITE_ON); //$NON-NLS-1$
        setShellStyle(getShellStyle() | SWT.RESIZE);
        mFiles = new LinkedHashSet(Arrays.asList(Common.tokenize(mStore.getString("files"),'\0'))); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    protected List getProperties()
    {
        return cProperties;
    }

    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_filesetdlg_context"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        Dialog.applyDialogFont(composite);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = Common.calculateControlSize(composite,60,0).x;
        composite.setLayoutData(gd);

        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label label = NSISWizardDialogUtil.createLabel(composite,SWT.LEFT,"wizard.source.files.label",true,null,true); //$NON-NLS-1$
        ((GridData)label.getLayoutData()).horizontalSpan = 2;

        Composite composite2 = new Composite(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        gd.heightHint = convertHeightInCharsToPixels(10);
        composite2.setLayoutData(gd);

        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        Table table = new Table(composite2, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalSpan = 2;
        table.setLayoutData(gd);

        TableColumn[] columns = {new TableColumn(table,SWT.LEFT,0)};
        columns[0].setText(EclipseNSISPlugin.getResourceString("wizard.file.name.label")); //$NON-NLS-1$

        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new CollectionLabelProvider());
        viewer.setInput(mFiles);

        final TableViewerUpDownMover mover = new TableViewerUpDownMover(){
            protected List getAllElements()
            {
                Collection collection = (Collection)((TableViewer)getViewer()).getInput();
                if(collection instanceof List) {
                    return (List)collection;
                }
                else {
                    return new ArrayList(collection);
                }
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((Collection)input).clear();
                ((Collection)input).addAll(elements);
            }
        };

        mover.setViewer(viewer);

        composite2 = new Composite(composite2, SWT.NONE);
        gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        gd.horizontalSpan = 1;
        composite2.setLayoutData(gd);

        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Button addButton = new Button(composite2,SWT.PUSH);
        addButton.setToolTipText(EclipseNSISPlugin.getResourceString("wizard.add.files.tooltip")); //$NON-NLS-1$
        addButton.setImage(CommonImages.ADD_ICON);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        addButton.setLayoutData(gd);

        final Button removeButton = new Button(composite2,SWT.PUSH);
        removeButton.setToolTipText(EclipseNSISPlugin.getResourceString("wizard.remove.files.tooltip")); //$NON-NLS-1$
        removeButton.setImage(CommonImages.DELETE_ICON);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        removeButton.setLayoutData(gd);
        removeButton.setEnabled(false);

        final Button upButton = new Button(composite2,SWT.PUSH);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        upButton.setLayoutData(gd);
        upButton.setImage(CommonImages.UP_ICON);
        upButton.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        upButton.setEnabled(mover.canMoveUp());

        final Button downButton = new Button(composite2,SWT.PUSH);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        downButton.setLayoutData(gd);
        downButton.setImage(CommonImages.DOWN_ICON);
        downButton.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        downButton.setEnabled(mover.canMoveDown());

        final ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        addButton.addSelectionListener(new SelectionAdapter() {
            String filterPath = ""; //$NON-NLS-1$

            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(getShell(),SWT.OPEN|SWT.MULTI|SWT.PRIMARY_MODAL);
                dialog.setText(EclipseNSISPlugin.getResourceString("wizard.files.dialog.title")); //$NON-NLS-1$
                dialog.setFilterNames(Common.loadArrayProperty(bundle,"wizard.source.file.filternames")); //$NON-NLS-1$
                dialog.setFilterExtensions(Common.loadArrayProperty(bundle,"wizard.source.file.filters")); //$NON-NLS-1$
                if(!Common.isEmpty(filterPath)) {
                    dialog.setFilterPath(filterPath);
                }
                if(dialog.open() != null) {
                    filterPath = dialog.getFilterPath();
                    String[] fileNames = dialog.getFileNames();
                    for (int i = 0; i < fileNames.length; i++) {
                        mFiles.add(IOUtility.encodePath(new StringBuffer(filterPath).append("\\").append(fileNames[i]).toString())); //$NON-NLS-1$
                    }
                    viewer.refresh();
                    validate();
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
                validate();
            }
        });

        upButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se)
            {
                mover.moveUp();
            }
        });

        downButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se)
            {
                mover.moveDown();
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
                upButton.setEnabled(mover.canMoveUp());
                downButton.setEnabled(mover.canMoveDown());
            }
        });
        table.addControlListener(new TableResizer());

        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,
                                                         NSISWizardUtil.getPathConstantsAndVariables(mWizard.getSettings().getTargetPlatform()),
                                                         mStore.getString("destination"), //$NON-NLS-1$
                                                         false,"wizard.destination.label",true,null,true); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination",c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.OVERWRITE_MODE_NAMES,mStore.getInt("overwriteMode"), //$NON-NLS-1$
                true,"wizard.overwrite.label",true,null,false); //$NON-NLS-1$
        c2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("overwrite",c2.getSelectionIndex()); //$NON-NLS-1$
            }
        });

        return composite;
    }

    protected boolean hasRequiredFields()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(mFiles.size() > 0) {
            Iterator iter = mFiles.iterator();
            buf.append(iter.next());
            for(; iter.hasNext(); ) {
                buf.append(NSISInstallFiles.SEPARATOR).append(iter.next());
            }
        }
        mStore.setValue("files",buf.toString()); //$NON-NLS-1$
        super.okPressed();
    }

    protected String checkForErrors()
    {
        if(mFiles.size() == 0) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.fileset"); //$NON-NLS-1$
        }
        else if(!NSISWizardUtil.isValidNSISPathName(mWizard.getSettings().getTargetPlatform(), mStore.getString("destination"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.fileset.destination"); //$NON-NLS-1$
        }
        else {
            return ""; //$NON-NLS-1$
        }
    }
}
