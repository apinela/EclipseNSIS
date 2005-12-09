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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsListbox extends InstallOptionsListItems
{
    protected InstallOptionsListbox(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_LISTBOX;
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            SelectListItemsPropertyDescriptor descriptor = new SelectListItemsPropertyDescriptor();
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }

    public void setFlags(List flags)
    {
        if(!flags.contains(InstallOptionsModel.FLAGS_MULTISELECT)&&
           !flags.contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT)) {
            String state = getState();
            int n = state.indexOf(IInstallOptionsConstants.LIST_SEPARATOR);
            if(n >= 0) {
                state = state.substring(0,n);
                fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE, state));
            }
        }
        super.setFlags(flags);
    }

    private String validateState(String state)
    {
        List listItems = getListItems();
        ArrayList selected = new ArrayList(Arrays.asList(Common.tokenize(state,IInstallOptionsConstants.LIST_SEPARATOR,false)));
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            if(!listItems.contains(iter.next())) {
                iter.remove();
            }
        }
        return Common.flatten(selected.toArray(),IInstallOptionsConstants.LIST_SEPARATOR);
    }

    public void setListItems(List listItems)
    {
        super.setListItems(listItems);
        String oldState = getState();
        String newState = validateState(oldState);
        if(!Common.stringsAreEqual(newState,oldState)) {
            fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE, newState));
        }
    }

    protected class SelectListItemsPropertyDescriptor extends PropertyDescriptor implements PropertyChangeListener
    {
        private SelectListItemsCellEditor mEditor;
        private DisposeListener mListener = new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mEditor = null;
            }
         };

        public SelectListItemsPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$
            setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_STATE));
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
                setListItems((List)evt.getNewValue());
            }
            else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
                List list = (List)evt.getNewValue();
                setMultiSelect(list.contains(InstallOptionsModel.FLAGS_MULTISELECT)||
                               list.contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT));
            }
        }

        public void setListItems(List listItems)
        {
            if(mEditor != null) {
                mEditor.setListItems(listItems);
            }
        }

        public void setMultiSelect(boolean multiSelect)
        {
            if(mEditor != null) {
                mEditor.setMultiSelect(multiSelect);
            }
        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            if(mEditor == null) {
                mEditor = new SelectListItemsCellEditor(parent,getListItems(),
                        (getFlags().contains(InstallOptionsModel.FLAGS_MULTISELECT)||
                         getFlags().contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT)));
                ICellEditorValidator validator = getValidator();
                if(validator != null) {
                    mEditor.setValidator(validator);
                }
                mEditor.getControl().addDisposeListener(mListener);
            }
            return mEditor;
        }
    }

    protected class SelectListItemsCellEditor extends DialogCellEditor
    {
        private boolean mMultiSelect = false;
        private List mListItems;

        protected SelectListItemsCellEditor(Composite parent, List listItems, boolean multiSelect)
        {
            super(parent);
            setListItems(listItems);
            setMultiSelect(multiSelect);
        }

        public void setListItems(List listItems)
        {
            mListItems = listItems;
        }

        public void setMultiSelect(boolean multiSelect)
        {
            mMultiSelect = multiSelect;
        }

        protected Object openDialogBox(Control cellEditorWindow)
        {
            Object oldValue = getValue();
            List selected = new ArrayList(Arrays.asList(Common.tokenize((String)oldValue,IInstallOptionsConstants.LIST_SEPARATOR,false)));
            SelectListItemsDialog dialog = new SelectListItemsDialog(cellEditorWindow.getShell(), mListItems, selected, mMultiSelect, getType());
            dialog.setValidator(getValidator());
            int result = dialog.open();
            return (result == Window.OK?Common.flatten(dialog.getSelection().toArray(),IInstallOptionsConstants.LIST_SEPARATOR):oldValue);
        }
    }

    protected class SelectListItemsDialog extends Dialog
    {
        private List mValues;
        private List mSelection;
        private boolean mMultiSelect;
        private String mType;
        private ICellEditorValidator mValidator;
        private TableViewer mViewer;

        public SelectListItemsDialog(Shell parent, List values, List selection, boolean multiSelect, String type)
        {
            super(parent);
            mValues = new ArrayList(values);
            mSelection = new ArrayList(selection);
            mMultiSelect = multiSelect;
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
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getFormattedString("select.listitems.dialog.name", new String[]{mType})); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getShellImage());
        }

        public List getSelection()
        {
            return mSelection;
        }

        protected Control createDialogArea(Composite parent)
        {
            final Composite composite = (Composite)super.createDialogArea(parent);
            GridLayout layout = (GridLayout)composite.getLayout();
            layout.numColumns = 2;
            layout.makeColumnsEqualWidth = false;

            Table table = new Table(composite,SWT.BORDER | (mMultiSelect?SWT.MULTI:SWT.SINGLE) | SWT.FULL_SELECTION | SWT.V_SCROLL);
            initializeDialogUnits(table);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = convertWidthInCharsToPixels(40);
            data.heightHint = convertHeightInCharsToPixels(10);
            table.setLayoutData(data);
            table.setLinesVisible(true);
            new TableColumn(table,SWT.LEFT);
            table.addControlListener(new TableResizer());

            mViewer = new TableViewer(table);
            mViewer.setContentProvider(new CollectionContentProvider());
            mViewer.setLabelProvider(new LabelProvider());
            mViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    mSelection.clear();
                    mSelection.addAll(sel.toList());
                }
            });

            Composite buttons = new Composite(composite,SWT.NONE);
            buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            layout = new GridLayout(1,false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            buttons.setLayout(layout);

            Button selectAll = new Button(buttons,SWT.PUSH);
            selectAll.setText(InstallOptionsPlugin.getResourceString("select.all.label")); //$NON-NLS-1$
            selectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            selectAll.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mViewer.setSelection(new StructuredSelection(mValues));
                    mViewer.getTable().setFocus();
                }
            });
            selectAll.setEnabled(mMultiSelect);

            Button deselectAll = new Button(buttons,SWT.PUSH);
            deselectAll.setText(InstallOptionsPlugin.getResourceString("deselect.all.label")); //$NON-NLS-1$
            deselectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            deselectAll.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mViewer.setSelection(StructuredSelection.EMPTY);
                    mViewer.getTable().setFocus();
                }
            });

            mViewer.setInput(mValues);

            return composite;
        }

        public void create()
        {
            super.create();
            // Set the initial selection here because of Windows bug which creates blank rows
            // if the selection is set in createDialogArea
            mViewer.setSelection(new StructuredSelection(mSelection));
        }

        protected void okPressed()
        {
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                String error = validator.isValid(getSelection());
                if(!Common.isEmpty(error)) {
                    Common.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error, //$NON-NLS-1$
                                     InstallOptionsPlugin.getShellImage());
                    return;
                }
            }
            super.okPressed();
        }
    }
}

