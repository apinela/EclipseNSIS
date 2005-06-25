/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.TableViewerUpDownMover;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsFileRequest extends InstallOptionsPathRequest
{
    public static Image FILEREQUEST_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("filerequest.type.small.icon")); //$NON-NLS-1$
    private static final char FILTER_SEPARATOR = ';';
    private static final TypeConverter FILTER_LIST_CONVERTER = new TypeConverter(){
        public String asString(Object o)
        {
            return Common.flatten(((List)o).toArray(),IInstallOptionsConstants.LIST_SEPARATOR);
        }

        public Object asType(String s)
        {
            List list = new ArrayList();
            String[] tokens = Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR);
            for (int i = 0; i < (tokens.length-1); i+= 2) {
                String description = tokens[i];
                String[] temp = Common.tokenize(tokens[i+1],FILTER_SEPARATOR);
                Pattern[] patterns = new Pattern[temp.length];
                for (int j = 0; j < patterns.length; j++) {
                    patterns[j] = new Pattern(temp[j]);
                }
                list.add(new Filter(description, patterns));
            }
            return list;
        }

        public Object makeCopy(Object o)
        {
            List list = new ArrayList();
            for(Iterator iter=((List)o).iterator(); iter.hasNext(); ) {
                list.add(((Filter)iter.next()).clone());
            }
            return null;
        }
    };

    private static final LabelProvider cLabelProvider = new LabelProvider(){
        public String getText(Object element) 
        {
            if(element instanceof List) {
                return FILTER_LIST_CONVERTER.asString(element);
            }
            else {
                return super.getText(element);
            }
        }
    };

    private List mFilter = new ArrayList();

    public InstallOptionsFileRequest()
    {
        super(InstallOptionsModel.TYPE_FILEREQUEST);
    }

    public Image getIconImage()
    {
        return FILEREQUEST_ICON;
    }

    public Object clone() throws CloneNotSupportedException
    {
        InstallOptionsFileRequest clone = (InstallOptionsFileRequest)super.clone();
        List list = new ArrayList();
        for (Iterator iter = mFilter.iterator(); iter.hasNext();) {
            list.add(((Filter)iter.next()).clone());
        }
        clone.setFilter(list);
        return clone;
    }
    
    protected void addPropertyName(List list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
        }
        else if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_FILTER)) {
            list.add(InstallOptionsModel.PROPERTY_FILTER);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    protected TypeConverter getTypeConverter(String property)
    {
        if(property.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            return FILTER_LIST_CONVERTER;
        }
        else {
            return super.getTypeConverter(property);
        }
    }
    
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            PropertyDescriptor descriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_FILTER, InstallOptionsPlugin.getResourceString("filter.property.name")){ //$NON-NLS-1$
                public CellEditor createPropertyEditor(Composite parent) 
                {
                    FilterCellEditor editor = new FilterCellEditor(parent);
                    editor.setValidator(getValidator());
                    return editor;
                }
            };
            descriptor.setLabelProvider(cLabelProvider);
            descriptor.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_FILTER));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_FILTER.equals(propName)) {
            return getFilter();
        }
        return super.getPropertyValue(propName);
    }

    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            setFilter((List)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }
    
    public List getFilter()
    {
        return mFilter;
    }

    public void setFilter(List filter)
    {
        if(!mFilter.equals(filter)) {
            List oldFilter = mFilter;
            mFilter = filter;
            firePropertyChange(InstallOptionsModel.PROPERTY_FILTER, oldFilter, mFilter);
            setDirty(true);
        }
    }
 
    private final class FilterCellEditor extends DialogCellEditor
    {
        private FilterCellEditor(Composite parent)
        {
            super(parent);
        }

        protected void updateContents(Object value) 
        {
            Label label = getDefaultLabel();
            if (label != null) {
                label.setText(cLabelProvider.getText(value));
            }
        }

        protected Object openDialogBox(Control cellEditorWindow)
        {
            FilterEditorDialog dialog = new FilterEditorDialog(cellEditorWindow.getShell(),getFilter());
            dialog.setValidator(getValidator());
            int result = dialog.open();
            return (result == Window.OK?dialog.getFilter():getFilter());
        }
    }

    private class FilterEditorDialog extends Dialog
    {
        private List mFilter;
        private Filter mCurrent = null;
        private ICellEditorValidator mValidator;
        
        /**
         * @param parentShell
         */
        protected FilterEditorDialog(Shell parentShell, List filter)
        {
            super(parentShell);
            mFilter = new ArrayList();
            for(Iterator iter = filter.iterator(); iter.hasNext(); ) {
                Filter f = (Filter)iter.next();
                mFilter.add(new Filter(f));
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
            viewer1.setLabelProvider(new FilterLabelProvider());
            
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
                    Filter f = new Filter(InstallOptionsPlugin.getResourceString("default.filter.description"), //$NON-NLS-1$
                                          new Pattern[]{new Pattern(InstallOptionsPlugin.getResourceString("default.filter.pattern"))}); //$NON-NLS-1$
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
                    e.doit = e.text.indexOf(IInstallOptionsConstants.LIST_SEPARATOR) < 0 && e.text.indexOf(FILTER_SEPARATOR) < 0;
                            
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
                    return ((Pattern)element).getPattern();
                }

                public void modify(Object element, String property, Object value)
                {
                    if(value == null) {
                        Common.openError(getShell(),textEditor.getErrorMessage());
                    }
                    else {
                        Pattern pattern = (Pattern)((TableItem)element).getData();
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
                        Pattern[] patterns = (Pattern[])viewer2.getInput();
                        patterns = (Pattern[])Common.resizeArray(patterns,patterns.length+1);
                        patterns[patterns.length-1] = new Pattern(InstallOptionsPlugin.getResourceString("default.filter.pattern")); //$NON-NLS-1$
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
                        Pattern[] patterns = (Pattern[])viewer2.getInput();
                        int[] indices = table2.getSelectionIndices();
                        Pattern[] patterns2 = (Pattern[])Common.resizeArray(patterns, patterns.length-indices.length);
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
            Pattern[] patterns = (Pattern[])viewer2.getInput();
            int len = (Common.isEmptyArray(patterns)?0:patterns.length);
            del2.setEnabled(!isNull && !sel.isEmpty() && sel.size() != len && len > 1);

            final TableViewerUpDownMover mover2 = new TableViewerUpDownMover() {
                protected List getAllElements()
                {
                    if(mCurrent != null) {
                        return new ArrayList(Arrays.asList((Pattern[])((TableViewer)getViewer()).getInput()));
                    }
                    return Collections.EMPTY_LIST;
                }

                protected List getMoveElements()
                {
                    if(mCurrent != null) {
                        IStructuredSelection sel = (IStructuredSelection)((TableViewer)getViewer()).getSelection();
                        if(!sel.isEmpty()) {
                            return sel.toList();
                        }
                    }
                    return Collections.EMPTY_LIST;
                }

                protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
                {
                    if(mCurrent != null) {
                        Pattern[] patterns = (Pattern[])input;
                        for (int i = 0; i < patterns.length; i++) {
                            patterns[i] = (Pattern)elements.get(i);
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
                    Pattern[] patterns = (Pattern[])viewer2.getInput();
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
                            mCurrent = (Filter)sel.getFirstElement();
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
                    Pattern[] patterns = (Pattern[])viewer2.getInput();
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
    
    private class FilterLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex)
        {
            if(element instanceof Filter) {
                switch(columnIndex) {
                    case 0:
                        return ((Filter)element).getDescription();
                    case 1:
                        return ((Filter)element).getPatternString();
                }
            }
            return getText(element);
        }
    }
    
    private static class Filter implements Cloneable
    {
        private static final Pattern[] EMPTY_PATTERN_ARRAY = new Pattern[0];
        private String mDescription;
        private Pattern[] mPatterns = EMPTY_PATTERN_ARRAY;
        
        public Filter()
        {
        }

        public Filter(String description, Pattern[] patterns)
        {
            this();
            setDescription(description);
            setPatterns(patterns);
        }
        
        public Filter(Filter filter)
        {
            this();
            setDescription(filter.getDescription());
            Pattern[] patterns = filter.getPatterns();
            Pattern[] patterns2 = null;
            if(!Common.isEmptyArray(patterns)) {
                patterns2 = new Pattern[patterns.length];
                for (int i = 0; i < patterns.length; i++) {
                    patterns2[i] = (Pattern)patterns[i].clone();
                }
            }
            setPatterns(patterns2);
        }

        public Object clone()
        {
            return new Filter(this);
        }

        public String getDescription()
        {
            return mDescription;
        }

        public void setDescription(String description)
        {
            mDescription = description;
        }

        public Pattern[] getPatterns()
        {
            return mPatterns;
        }

        public void setPatterns(Pattern[] patterns)
        {
            mPatterns = (patterns==null?EMPTY_PATTERN_ARRAY:patterns);
        }
        
        public String toString()
        {
            StringBuffer buf = new StringBuffer(mDescription);
            buf.append(IInstallOptionsConstants.LIST_SEPARATOR); //$NON-NLS-1$
            buf.append(getPatternString());
            return buf.toString();
        }
        
        public String getPatternString()
        {
            return Common.flatten(mPatterns,FILTER_SEPARATOR);
        }
    }
    
    private static class Pattern implements Cloneable
    {
        private String mPattern;
        
        public Pattern(String pattern)
        {
            mPattern = pattern;
        }
        
        public String toString()
        {
            return getPattern();
        }
        
        public String getPattern()
        {
            return mPattern;
        }
        
        public void setPattern(String pattern)
        {
            mPattern = pattern;
        }
        
        public Object clone()
        {
            return new Pattern(mPattern);
        }
    }
}
