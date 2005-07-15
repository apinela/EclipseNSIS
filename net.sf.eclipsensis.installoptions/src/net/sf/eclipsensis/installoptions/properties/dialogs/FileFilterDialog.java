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
import net.sf.eclipsensis.installoptions.model.InstallOptionsFileRequest;
import net.sf.eclipsensis.installoptions.properties.labelproviders.FileFilterLabelProvider;
import net.sf.eclipsensis.installoptions.util.FileFilter;
import net.sf.eclipsensis.installoptions.util.FilePattern;
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

public class FileFilterDialog extends Dialog
{
    private List mFilter;
    private FileFilter mCurrent = null;
    private ICellEditorValidator mValidator;
    
    /**
     * @param parentShell
     */
    public FileFilterDialog(Shell parentShell, List filter)
    {
        super(parentShell);
        mFilter = new ArrayList();
        for(Iterator iter = filter.iterator(); iter.hasNext(); ) {
            FileFilter f = (FileFilter)iter.next();
            mFilter.add(new FileFilter(f));
        }
    }
    
    public ICellEditorValidator getValidator()
    {
        return mValidator;
    }
    
    public void setValidator(ICellEditorValidator validator)
    {
        mValidator = validator;
    }
    
    public List getFilter()
    {
        return mFilter;
    }

    protected void configureShell(Shell newShell)
    {
        newShell.setText(InstallOptionsPlugin.getResourceString("filter.dialog.name")); //$NON-NLS-1$
        super.configureShell(newShell);
    }
    
    protected void okPressed()
    {
        ICellEditorValidator validator = getValidator();
        if(validator != null) {
            String error = validator.isValid(getFilter());
            if(!Common.isEmpty(error)) {
                MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error); //$NON-NLS-1$
                return;
            }
        }
        super.okPressed();
    }
    
    protected Control createDialogArea(Composite parent)
    {
        GridLayout layout;
        Composite composite = (Composite)super.createDialogArea(parent);
        
        final Group group1 = new Group(composite,SWT.SHADOW_ETCHED_IN);
        group1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group1.setLayout(new GridLayout(2, false));
        group1.setText(InstallOptionsPlugin.getResourceString("filter.summary.group.name")); //$NON-NLS-1$
        final Table table = new Table(group1,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        final TableColumn[] columns = new TableColumn[2];
        columns[0] = new TableColumn(table,SWT.LEFT);
        columns[0].setText(InstallOptionsPlugin.getResourceString("filter.description")); //$NON-NLS-1$
        columns[1] = new TableColumn(table,SWT.LEFT);
        columns[1].setText(InstallOptionsPlugin.getResourceString("filter.patterns")); //$NON-NLS-1$
        
        final TableViewer viewer1 = new TableViewer(table);
        viewer1.setContentProvider(new CollectionContentProvider());
        viewer1.setLabelProvider(new FileFilterLabelProvider());
        
        final Composite buttons = new Composite(group1,SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);
        
        final Button add = new Button(buttons,SWT.PUSH);
        add.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.icon"))); //$NON-NLS-1$
        add.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        add.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        add.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                FileFilter f = new FileFilter(InstallOptionsPlugin.getResourceString("default.filter.description"), //$NON-NLS-1$
                                      new FilePattern[]{new FilePattern(InstallOptionsPlugin.getResourceString("default.filter.pattern"))}); //$NON-NLS-1$
                List list = (List)viewer1.getInput();
                if(list != null) {
                    list.add(f);
                    viewer1.refresh(false);
                    viewer1.setSelection(new StructuredSelection(f));
                }
            }
        });
        
        final Button del = new Button(buttons, SWT.PUSH);
        del.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.icon"))); //$NON-NLS-1$
        del.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        del.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        del.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                List list = (List)viewer1.getInput();
                if(list != null) {
                    IStructuredSelection selection= (IStructuredSelection) viewer1.getSelection();
                    if(!selection.isEmpty()) {
                        for(Iterator iter=selection.toList().iterator(); iter.hasNext(); ) {
                            list.remove(iter.next());
                        }
                        viewer1.refresh(false);
                    }
                }
            }
        });
        del.setEnabled(!viewer1.getSelection().isEmpty());

        final TableViewerUpDownMover mover = new TableViewerUpDownMover() {
            protected List getAllElements()
            {
                return (List)((TableViewer)getViewer()).getInput();
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((List)input).clear();
                ((List)input).addAll(elements);
            }
        };
        mover.setViewer(viewer1);

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
        
        group1.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area= group1.getClientArea();
                Point preferredSize= table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width= area.width - 2 * table.getBorderWidth();
                if (preferredSize.y > area.height) {
                    Point vBarSize = table.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }

                int buttonsWidth = buttons.getSize().x;
                if(buttonsWidth == 0) {
                    buttonsWidth = buttons.computeSize(SWT.DEFAULT,SWT.DEFAULT).x;
                }
                
                width -= buttonsWidth;
                width -= ((GridLayout)group1.getLayout()).horizontalSpacing;
                width -= 2*((GridLayout)group1.getLayout()).marginWidth;
                int columnWidth = width/2;
                Point oldSize= table.getSize();
                if (oldSize.x <= width) {
                    table.setSize(width, area.height);
                }
                
                columns[0].setWidth(width - columnWidth);
                columns[1].setWidth(columnWidth);

                if (oldSize.x > width) {
                    table.setSize(width, area.height);
                }
            }
        });
        
        final Group group2 = new Group(composite,SWT.SHADOW_ETCHED_IN);
        group2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group2.setLayout(new GridLayout(1, false));
        group2.setText(InstallOptionsPlugin.getResourceString("filter.detail.group.name")); //$NON-NLS-1$

        boolean isNull = (mCurrent==null);
        Composite composite2 = new Composite(group2,SWT.NONE);
        composite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Label label = new Label(composite2,SWT.NONE);
        label.setText(InstallOptionsPlugin.getResourceString("filter.description")); //$NON-NLS-1$
        label.setLayoutData(new GridData());
        label.setEnabled(!isNull);
        
        final Text text = new Text(composite2,SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if(mCurrent != null) {
                    mCurrent.setDescription(text.getText());
                    viewer1.update(mCurrent,null);
                }
            }
        });
        text.setEnabled(!isNull);

        final Label label2 = new Label(group2,SWT.NONE);
        label2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label2.setText(InstallOptionsPlugin.getResourceString("filter.patterns")); //$NON-NLS-1$
        label2.setEnabled(!isNull);

        composite2 = new Composite(group2,SWT.NONE);
        composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
        
        final Table table2 = new Table(composite2,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
        table2.setLayoutData(new GridData(GridData.FILL_BOTH));
        table2.setLinesVisible(true);
        table2.setEnabled(!isNull);
        new TableColumn(table2,SWT.LEFT);
        final TextCellEditor textEditor = new TextCellEditor(table2);
        ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                e.doit = e.text.indexOf(IInstallOptionsConstants.LIST_SEPARATOR) < 0 && e.text.indexOf(InstallOptionsFileRequest.FILTER_SEPARATOR) < 0;
                        
            }
        });
        textEditor.setValidator(new ICellEditorValidator(){
            public String isValid(Object value)
            {
                if(!Common.isEmpty((String)value)) {
                    return null;
                }
                else {
                    return InstallOptionsPlugin.getResourceString("empty.filter.pattern.error"); //$NON-NLS-1$
                }
            }
        });
        
        final TableViewer viewer2 = new TableViewer(table2);
        viewer2.setColumnProperties(new String[]{"pattern"}); //$NON-NLS-1$
        viewer2.setContentProvider(new ArrayContentProvider());
        viewer2.setLabelProvider(new LabelProvider());
        viewer2.setCellEditors(new CellEditor[]{textEditor});
        viewer2.setCellModifier(new ICellModifier(){
            public boolean canModify(Object element, String property)
            {
                return true;
            }

            public Object getValue(Object element, String property)
            {
                return ((FilePattern)element).getPattern();
            }

            public void modify(Object element, String property, Object value)
            {
                if(value == null) {
                    Common.openError(getShell(),textEditor.getErrorMessage());
                }
                else {
                    FilePattern pattern = (FilePattern)((TableItem)element).getData();
                    pattern.setPattern((String)value);
                    viewer2.update(pattern,null);
                    viewer1.update(mCurrent,null);
                }
            }
        });

        final Composite buttons2 = new Composite(composite2,SWT.NONE);
        buttons2.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons2.setLayout(layout);
        
        final Button add2 = new Button(buttons2,SWT.PUSH);
        add2.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.icon"))); //$NON-NLS-1$
        add2.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        add2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        add2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if(mCurrent != null) {
                    FilePattern[] patterns = (FilePattern[])viewer2.getInput();
                    patterns = (FilePattern[])Common.resizeArray(patterns,patterns.length+1);
                    patterns[patterns.length-1] = new FilePattern(InstallOptionsPlugin.getResourceString("default.filter.pattern")); //$NON-NLS-1$
                    mCurrent.setPatterns(patterns);
                    viewer2.setInput(patterns);
                    viewer2.setSelection(new StructuredSelection(patterns[patterns.length-1]));
                    viewer1.update(mCurrent,null);
                }
            }
        });
        add2.setEnabled(!isNull);
        
        final Button del2 = new Button(buttons2, SWT.PUSH);
        del2.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.icon"))); //$NON-NLS-1$
        del2.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        del2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        del2.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) 
            {
                if(mCurrent != null) {
                    FilePattern[] patterns = (FilePattern[])viewer2.getInput();
                    int[] indices = table2.getSelectionIndices();
                    FilePattern[] patterns2 = (FilePattern[])Common.resizeArray(patterns, patterns.length-indices.length);
                    int j=0;
                    int k=0;
                    for (int i = 0; i < patterns.length; i++) {
                        if(j >= indices.length || i != indices[j]) {
                            patterns2[k++] = patterns[i];
                        }
                        else {
                            j++;
                        }
                    }
                    mCurrent.setPatterns(patterns2);
                    viewer2.setInput(patterns2);
                    viewer1.update(mCurrent,null);
                }
            }
        });
        IStructuredSelection sel = (IStructuredSelection)viewer2.getSelection();
        FilePattern[] patterns = (FilePattern[])viewer2.getInput();
        int len = (Common.isEmptyArray(patterns)?0:patterns.length);
        del2.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);

        final TableViewerUpDownMover mover2 = new TableViewerUpDownMover() {
            protected List getAllElements()
            {
                if(mCurrent != null) {
                    return new ArrayList(Arrays.asList((FilePattern[])((TableViewer)getViewer()).getInput()));
                }
                return Collections.EMPTY_LIST;
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                if(mCurrent != null) {
                    FilePattern[] patterns = (FilePattern[])input;
                    for (int i = 0; i < patterns.length; i++) {
                        patterns[i] = (FilePattern)elements.get(i);
                    }
                    viewer2.refresh();
                    viewer1.update(mCurrent,null);
                }
            }
        };
        mover2.setViewer(viewer2);

        final Button up2 = new Button(buttons2,SWT.PUSH);
        up2.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        up2.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        up2.setEnabled(!isNull && mover2.canMoveUp());
        up2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        up2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mover2.moveUp();
            }
        });

        final Button down2 = new Button(buttons2, SWT.PUSH);
        down2.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
        down2.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        down2.setEnabled(!isNull && mover2.canMoveDown());
        down2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        down2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mover2.moveDown();
            }
        });
        
        group2.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area= group2.getClientArea();
                Point preferredSize= table2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width= area.width - 2 * table2.getBorderWidth();
                if (preferredSize.y > area.height) {
                    Point vBarSize = table2.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }
                int buttonsWidth = buttons2.getSize().x;
                if(buttonsWidth == 0) {
                    buttonsWidth = buttons2.computeSize(SWT.DEFAULT,SWT.DEFAULT).x;
                }
                
                width -= buttonsWidth;
                width -= ((GridLayout)group2.getLayout()).horizontalSpacing;
                width -= 2*((GridLayout)group2.getLayout()).marginWidth;
                table2.setSize(width, area.height);
                TableColumn[] columns = table2.getColumns();
                if(!Common.isEmptyArray(columns)) {
                    columns[0].setWidth(width);
                }
            }
        });
        
        viewer2.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                FilePattern[] patterns = (FilePattern[])viewer2.getInput();
                int len = (patterns==null?0:patterns.length);
                del2.setEnabled(!sel.isEmpty() && sel.size() != len && len > 1);
                up2.setEnabled(mover2.canMoveUp());
                down2.setEnabled(mover2.canMoveDown());
            }
        });
        
        viewer1.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                del.setEnabled(!sel.isEmpty());
                up.setEnabled(mover.canMoveUp());
                down.setEnabled(mover.canMoveDown());
                mCurrent = null;
                if(!sel.isEmpty()) {
                    if(sel.size() == 1) {
                        mCurrent = (FileFilter)sel.getFirstElement();
                    }
                }
                boolean isNull = (mCurrent==null);
                text.setText((isNull?"":mCurrent.getDescription())); //$NON-NLS-1$
                viewer2.setInput((isNull?null:mCurrent.getPatterns()));
                label.setEnabled(!isNull);
                text.setEnabled(!isNull);
                label2.setEnabled(!isNull);
                table2.setEnabled(!isNull);
                add2.setEnabled(!isNull);
                FilePattern[] patterns = (FilePattern[])viewer2.getInput();
                int len = (Common.isEmptyArray(patterns)?0:patterns.length);
                del2.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);
                up2.setEnabled(!isNull && mover2.canMoveUp());
                down2.setEnabled(!isNull && mover2.canMoveDown());
            }
        });
        
        applyDialogFont(composite);
        ((GridData)composite.getLayoutData()).widthHint = convertWidthInCharsToPixels(80);
        viewer1.setInput(mFilter);
        return composite;
    }
}