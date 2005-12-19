/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.TableViewerUpDownMover;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class NSISSettingsEditorPage implements DisposeListener
{
    protected NSISSettings mSettings = null;
    private List mListeners = new ArrayList();
    private Control mControl = null;

    public NSISSettingsEditorPage(NSISSettings settings)
    {
        mSettings = settings;
    }
    
    public void addListener(INSISSettingsEditorPageListener listener)
    {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(INSISSettingsEditorPageListener listener)
    {
        mListeners.remove(listener);
    }

    protected void fireChanged()
    {
        INSISSettingsEditorPageListener[] listeners = (INSISSettingsEditorPageListener[])mListeners.toArray(new INSISSettingsEditorPageListener[mListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].settingsChanged();
        }
    }


    protected void enableComposite(Composite composite, boolean state)
    {
        Control[] controls = composite.getChildren();
        for(int i=0; i<controls.length; i++) {
            if(controls[i] instanceof Composite) {
                enableComposite((Composite)controls[i],state);
            }
            controls[i].setEnabled(state);
        }
    }
    
    protected Button createCheckBox(Composite parent, String text, String tooltipText, boolean state)
    {
        Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
        button.setText(text);
        button.setToolTipText(tooltipText);
        button.setSelection(state);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        button.setLayoutData(data);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                fireChanged();
            }
        });
        return button;
    }

    protected Combo createCombo(Composite composite, String text, String tooltipText,
                                String[] list, int selected)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        label.setLayoutData(data);

        Combo combo = new Combo(composite, SWT.DROP_DOWN|SWT.READ_ONLY);
        combo.setToolTipText(tooltipText);
        if(!Common.isEmptyArray(list)) {
            for(int i=0; i<list.length; i++) {
                combo.add(list[i]);
            }
        }
        combo.select(selected);
        combo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e)
            {
                fireChanged();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                fireChanged();
            }
        });
        combo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                fireChanged();
            }
        });
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        combo.setLayoutData(data);
        return combo;
    }

    protected TableViewer createTableViewer(Composite composite, final Object input, IContentProvider contentProvider,
                                            ILabelProvider labelProvider, String description, String[] columnNames,
                                            String addTooltip, String editTooltip, String removeTooltip,
                                            SelectionListener addAdapter, SelectionListener editAdapter,
                                            SelectionListener removeAdapter, IDoubleClickListener doubleClickListener,
                                            final TableViewerUpDownMover mover)
    {
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.LEFT);
        label.setText(description);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        label.setLayoutData(data);

        Table table = new Table(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);

        if(!Common.isEmptyArray(columnNames))  {
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            TableColumn[] columns = new TableColumn[columnNames.length];
            
            for(int i=0; i<columnNames.length; i++) {
                columns[i] = new TableColumn(table,SWT.LEFT,i);
                columns[i].setText(columnNames[i]);
            }
            table.addControlListener(new TableResizer());
        }
        TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider((contentProvider==null?new WorkbenchContentProvider():contentProvider));
        viewer.setLabelProvider((labelProvider == null?new WorkbenchLabelProvider():labelProvider));
        viewer.setInput(input);
        mover.setViewer(viewer);

        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.verticalSpan = 5;
        table.setLayoutData(data);
        Button addButton = createButton(composite,EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.icon")),addTooltip); //$NON-NLS-1$
        if(addAdapter != null) {
            addButton.addSelectionListener(addAdapter);
        }
        final Button editButton = createButton(composite,EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("edit.icon")),editTooltip); //$NON-NLS-1$
        editButton.setEnabled(false);
        if(editAdapter != null) {
            editButton.addSelectionListener(editAdapter);
        }
        final Button removeButton = createButton(composite,EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.icon")),removeTooltip); //$NON-NLS-1$
        removeButton.setEnabled(false);
        if(removeAdapter != null) {
            removeButton.addSelectionListener(removeAdapter);
        }
        final Button upButton = createButton(composite,EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon")), //$NON-NLS-1$
                                                       EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        upButton.setEnabled(mover.canMoveUp());
        upButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveUp();
            }
        });

        final Button downButton = createButton(composite,EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("down.icon")), //$NON-NLS-1$
                                                         EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        downButton.setEnabled(mover.canMoveDown());
        downButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveDown();
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
                editButton.setEnabled((selection != null && selection.size()==1));
                upButton.setEnabled(mover.canMoveUp());
                downButton.setEnabled(mover.canMoveDown());
            }
        });

        viewer.addDoubleClickListener(doubleClickListener);

        return viewer;
    }

    protected Button createButton(Composite parent, Object object, String tooltipText)
    {
        Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
        if(object instanceof Image) {
            button.setImage((Image)object);
        }
        else {
            button.setText(object.toString());
        }
        button.setToolTipText(tooltipText);
        GridData data = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        button.setLayoutData(data);
        return button;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public final boolean performApply()
    {
        if(!performApply(mSettings)) {
            return false;
        }
        return true;
    }

    public Control getControl()
    {
        return mControl;
    }

    public void widgetDisposed(DisposeEvent e)
    {
        mControl = null;
    }

    public Control create(Composite parent)
    {
        if(mControl != null) {
            mControl.removeDisposeListener(this);
            mControl.dispose();
        }
        mControl = createControl(parent);
        mControl.addDisposeListener(this);
        return mControl;
    }

    protected abstract Control createControl(Composite parent);
    public abstract void enableControls(boolean state);
    protected abstract boolean performApply(NSISSettings settings);
    public abstract void reset();
    public abstract void setDefaults();
    public abstract boolean canEnableControls();
}
