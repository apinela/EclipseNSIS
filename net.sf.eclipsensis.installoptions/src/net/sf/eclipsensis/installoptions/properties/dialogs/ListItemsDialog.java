/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.TableViewerUpDownMover;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ListItemsDialog extends Dialog
{
    private List mValues;
    private String mType;
    private ICellEditorValidator mValidator;
    
    public ListItemsDialog(Shell parent, List values, String type)
    {
        super(parent);
        mValues = new ArrayList(values);
        mType = type;
    }

    public ICellEditorValidator getValidator()
    {
        return mValidator;
    }
    
    public void setValidator(ICellEditorValidator validator)
    {
        mValidator = validator;
    }
    
    protected void configureShell(Shell newShell)
    {
        newShell.setText(InstallOptionsPlugin.getFormattedString("listitems.dialog.name", new String[]{mType})); //$NON-NLS-1$
        super.configureShell(newShell);
    }

    public List getValues()
    {
        return mValues;
    }

    protected Control createDialogArea(Composite parent)
    {
        final Composite composite = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        
        final Table table = new Table(composite,SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
        initializeDialogUnits(table);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(40);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);
        table.setLinesVisible(true);
        new TableColumn(table,SWT.LEFT);
        
        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new LabelProvider());
        final TextCellEditor textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                e.doit = e.text.indexOf(IInstallOptionsConstants.LIST_SEPARATOR) < 0;
                        
            }
        });
        viewer.setColumnProperties(new String[]{"item"}); //$NON-NLS-1$
        viewer.setCellEditors(new CellEditor[]{textEditor});
        viewer.setCellModifier(new ICellModifier(){
            public boolean canModify(Object element, String property)
            {
                return true;
            }

            public Object getValue(Object element, String property)
            {
                return element;
            }

            public void modify(Object element, String property, Object value)
            {
                if(value == null) {
                    Common.openError(getShell(),textEditor.getErrorMessage());
                }
                else {
                    TableItem ti = (TableItem)element;
                    Table t = ti.getParent();
                    int n = t.getSelectionIndex();
                    List list = (List)viewer.getInput();
                    if(n < list.size()) {
                        list.set(n,value);
                    }
                    else {
                        list.add(value);
                    }
                    viewer.refresh(true);
                    viewer.setSelection(new StructuredSelection(value));
                }
            }
        });
        
        final Composite buttons = new Composite(composite,SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttons.setLayout(layout);
        
        final Button add = new Button(buttons,SWT.PUSH);
        add.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.icon"))); //$NON-NLS-1$
        add.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        add.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        add.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                List list = (List)viewer.getInput();
                if(list != null) {
                    String item = InstallOptionsPlugin.getResourceString("default.listitem.label");
                    list.add(item);
                    viewer.refresh(false);
                    viewer.setSelection(new StructuredSelection(item));
                }
            }
        });
        
        final Button del = new Button(buttons, SWT.PUSH);
        del.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.icon"))); //$NON-NLS-1$
        del.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        del.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        del.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                List list = (List)viewer.getInput();
                if(list != null) {
                    IStructuredSelection selection= (IStructuredSelection) viewer.getSelection();
                    if(!selection.isEmpty()) {
                        for(Iterator iter=selection.toList().iterator(); iter.hasNext(); ) {
                            list.remove(iter.next());
                        }
                        viewer.refresh(false);
                    }
                }
            }
        });
        del.setEnabled(!viewer.getSelection().isEmpty());

        final TableViewerUpDownMover mover = new TableViewerUpDownMover() {
            protected List getAllElements()
            {
                return (List)((TableViewer)getViewer()).getInput();
            }

            protected List getMoveElements()
            {
                IStructuredSelection sel = (IStructuredSelection)((TableViewer)getViewer()).getSelection();
                if(!sel.isEmpty()) {
                    return sel.toList();
                }
                else {
                    return Collections.EMPTY_LIST;
                }
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((List)input).clear();
                ((List)input).addAll(elements);
            }
        };
        mover.setViewer(viewer);

        final Button up = new Button(buttons,SWT.PUSH);
        up.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        up.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        up.setEnabled(mover.canMoveUp());
        up.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        up.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mover.moveUp();
            }
        });

        final Button down = new Button(buttons, SWT.PUSH);
        down.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
        down.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        down.setEnabled(mover.canMoveDown());
        down.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        down.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveDown();
            }
        });
        

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                del.setEnabled(!sel.isEmpty());
                up.setEnabled(mover.canMoveUp());
                down.setEnabled(mover.canMoveDown());
            }
        });

        composite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area= composite.getClientArea();
                Point preferredSize= table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width= area.width - 2 * table.getBorderWidth();
                if (preferredSize.y > area.height) {
                    Point vBarSize = table.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }
                width -= buttons.getSize().x;
                width -= ((GridLayout)composite.getLayout()).horizontalSpacing;
                Point oldSize= table.getSize();
                if (oldSize.x <= width) {
                    table.setSize(width, area.height);
                }
                
                TableColumn[] columns = table.getColumns();
                columns[0].setWidth(width);
                if (oldSize.x > width) {
                    table.setSize(width, area.height);
                }
            }
        });
        viewer.setInput(mValues);
        return composite;
    }
    
    protected void okPressed()
    {
        ICellEditorValidator validator = getValidator();
        if(validator != null) {
            String error = validator.isValid(getValues());
            if(!Common.isEmpty(error)) {
                MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error);
                return;
            }
        }
        super.okPressed();
    }
}