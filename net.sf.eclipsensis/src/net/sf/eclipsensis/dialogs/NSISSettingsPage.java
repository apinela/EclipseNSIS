/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class NSISSettingsPage	extends PropertyPage implements IWorkbenchPreferencePage, INSISConstants
{
    private NSISSettings mSettings = null;

    protected Button mHdrInfo = null;
    protected Button mLicense = null;
    protected Button mNoConfig = null;
    protected Button mNoCD = null;
    protected Combo mVerbosity = null;
    protected Combo mCompressor = null;
    protected TableViewer mInstructions = null;
    protected TableViewer mSymbols = null;
    
    private TabFolder mFolder = null;
    private Group mGroup = null;

    public NSISSettingsPage() 
    {
		setDescription(getPageDescription());
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected final Control createContents(Composite parent)
    {
        mSettings = loadSettings();
        mFolder = new TabFolder(parent, SWT.NONE);
        
        TabItem item = new TabItem(mFolder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("general.tab.text")); //$NON-NLS-1$
        item.setToolTipText(EclipseNSISPlugin.getResourceString("general.tab.tooltip")); //$NON-NLS-1$
        item.setControl(createGeneralPage(mFolder));
        
        item = new TabItem(mFolder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("symbols.tab.text")); //$NON-NLS-1$
        item.setToolTipText(EclipseNSISPlugin.getResourceString("symbols.tab.tooltip")); //$NON-NLS-1$
        item.setControl(createSymbolsViewer(mFolder));

        enableControls(canEnableControls());

        mFolder.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        try {
                            TabItem item = mFolder.getSelection()[0];
                            if(!item.getControl().isEnabled()) {
                                mFolder.setSelection(0);
                            }
                        }
                        catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        return mFolder;
    }
    
    private Control createGeneralPage(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NULL);
        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        
        Composite child = createEnablerControl(composite);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        child.setLayoutData(data);

        mGroup = createNSISOptionsGroup(composite);
        data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        mGroup.setLayoutData(data);
        
        createInstructionsViewer(composite);
        return composite;
    }
    
    private void enableComposite(Composite composite, boolean state)
    {
        Control[] controls = composite.getChildren();
        for(int i=0; i<controls.length; i++) {
            if(controls[i] instanceof Composite) {
                enableComposite((Composite)controls[i],state);
            }
            controls[i].setEnabled(state);
        }
    }
    
    protected final void enableControls(boolean state)
    {
        enableComposite(mGroup,state);
        enableComposite(mInstructions.getControl().getParent(),state);
        if(state) {
            //Hack to properly enable the buttons
            mInstructions.setSelection(mInstructions.getSelection());
        }
        TabItem[] tabItems = mFolder.getItems();
        for(int i=1; i<tabItems.length; i++) {
            tabItems[i].getControl().setEnabled(state);
        }
    }
    
    private Group createNSISOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("nsis.options.group.text")); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = 5;
        group.setLayout(layout);
        
        mHdrInfo = createCheckBox(group, EclipseNSISPlugin.getResourceString("hdrinfo.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("hdrinfo.tooltip"), //$NON-NLS-1$
                                  mSettings.getHdrInfo());
        
        mLicense = createCheckBox(group, EclipseNSISPlugin.getResourceString("license.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("license.tooltip"), //$NON-NLS-1$
                                  mSettings.getLicense());
        
        mNoConfig = createCheckBox(group, EclipseNSISPlugin.getResourceString("noconfig.text"), //$NON-NLS-1$
                                   EclipseNSISPlugin.getResourceString("noconfig.tooltip"), //$NON-NLS-1$
                                   mSettings.getNoConfig());

        mNoCD = createCheckBox(group, EclipseNSISPlugin.getResourceString("nocd.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("nocd.tooltip"), //$NON-NLS-1$
                               mSettings.getNoCD());

        Composite composite  = new Composite(group,SWT.NULL);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);

        layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        mVerbosity = createCombo(composite,EclipseNSISPlugin.getResourceString("verbosity.text"),EclipseNSISPlugin.getResourceString("verbosity.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 VERBOSITY_ARRAY, mSettings.getVerbosity());
        
        mCompressor = createCombo(composite,EclipseNSISPlugin.getResourceString("compressor.text"),EclipseNSISPlugin.getResourceString("compressor.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 COMPRESSOR_DISPLAY_ARRAY,mSettings.getCompressor());

        return group;
    }
    
    protected Button createCheckBox(Composite parent, String text, String tooltipText, boolean state)
    {
        Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
        button.setText(text);
        button.setToolTipText(tooltipText);
        button.setSelection(state);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        button.setLayoutData(data);
        return button;
    }

    protected Combo createCombo(Composite composite, String text, String tooltipText,
                                String[] list, int selected)
    {
        Label label = new Label(composite, SWT.LEFT);
        label.setText(text);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = false;
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);

        Combo combo = new Combo(composite, SWT.DROP_DOWN|SWT.READ_ONLY);
        combo.setToolTipText(tooltipText);
        for(int i=0; i<list.length; i++) {
            combo.add(list[i]);
        }
        combo.select(selected);

        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = false;
        combo.setLayoutData(data);
        return combo;
    }

    protected TableViewer createTableViewer(Composite composite, Object input, IContentProvider contentProvider,
                                            ILabelProvider labelProvider, String description, String[] columnNames, 
                                            String addText, String addTooltip, String editText, 
                                            String editTooltip, String removeText, String removeTooltip,
                                            SelectionAdapter addAdapter, SelectionAdapter editAdapter,
                                            SelectionAdapter removeAdapter)
    {
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);
  
        Label label = new Label(composite, SWT.LEFT);
        label.setText(description);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        
        final Table table = new Table(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.BORDER);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        for(int i=0; i<columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table,SWT.LEFT,i);
            tableColumn.setText(columnNames[i]);
        }

        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        data.verticalSpan = 3;
        data.verticalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        table.setLayoutData(data);
        Button addButton = createButton(composite,addText,addTooltip);
        if(addAdapter != null) {
            addButton.addSelectionListener(addAdapter);
        }
        final Button editButton = createButton(composite,editText,editTooltip);
        editButton.setEnabled(false);
        if(editAdapter != null) {
            editButton.addSelectionListener(editAdapter);
        }
        final Button removeButton = createButton(composite,removeText,removeTooltip);
        removeButton.setEnabled(false);
        if(removeAdapter != null) {
            removeButton.addSelectionListener(removeAdapter);
        }
        
        TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider((contentProvider==null?new WorkbenchContentProvider():contentProvider));
        viewer.setLabelProvider((labelProvider == null?new WorkbenchLabelProvider():labelProvider));
        viewer.setInput(input);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
                editButton.setEnabled((selection != null && selection.size()==1));
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
        
        return viewer;
    }
    
    protected Button createButton(Composite parent, String text, String tooltipText)
    {
        Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
        button.setText(text);
        button.setToolTipText(tooltipText);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.verticalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = false;
        button.setLayoutData(data);
        return button;
    }
    
    private Control createSymbolsViewer(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NULL);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                NSISSymbolDialog dialog = new NSISSymbolDialog(NSISSettingsPage.this);
                dialog.open();
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Map.Entry entry = (Map.Entry)((IStructuredSelection)mSymbols.getSelection()).getFirstElement();
                new NSISSymbolDialog(NSISSettingsPage.this,(String)entry.getKey(),(String)entry.getValue()).open();
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Map map = (Map)mSymbols.getInput();
                IStructuredSelection selection = (IStructuredSelection)mSymbols.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    map.remove(((Map.Entry)iter.next()).getKey());
                }
                mSymbols.refresh();
            }
        };
        mSymbols = createTableViewer(composite, mSettings.getSymbols(), new MapContentProvider(), new MapLabelProvider(),
                                     EclipseNSISPlugin.getResourceString("symbols.description"), //$NON-NLS-1$
                                     new String[] {
                                         EclipseNSISPlugin.getResourceString("symbols.name.text"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.value.text")}, //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("symbols.add.text"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("symbols.add.tooltip"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("symbols.edit.text"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("symbols.edit.tooltip"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("symbols.remove.text"), //$NON-NLS-1$
                                     EclipseNSISPlugin.getResourceString("symbols.remove.tooltip"), //$NON-NLS-1$
                                     addAdapter,editAdapter,removeAdapter);
        return composite;
    }

    private Control createInstructionsViewer(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NULL);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                new NSISInstructionDialog(NSISSettingsPage.this).open();
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                String text = ((String)((IStructuredSelection)mInstructions.getSelection()).getFirstElement()).trim();
                new NSISInstructionDialog(NSISSettingsPage.this,text).open();
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Collection collection = (Collection)mInstructions.getInput();
                IStructuredSelection selection = (IStructuredSelection)mInstructions.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    collection.remove(iter.next());
                }
                mInstructions.refresh();
            }
        };

        mInstructions = createTableViewer(composite, mSettings.getInstructions(),
                                      new CollectionContentProvider(), new CollectionLabelProvider(),
                                      EclipseNSISPlugin.getResourceString("instructions.description"), //$NON-NLS-1$
                                      new String[]{EclipseNSISPlugin.getResourceString("instructions.instruction.text")}, //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.add.text"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.add.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.edit.text"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.edit.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.remove.text"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.remove.tooltip"), //$NON-NLS-1$
                                      addAdapter,editAdapter,removeAdapter);
        ((GridLayout)composite.getLayout()).marginWidth = 0;
        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        composite.setLayoutData(data);
        
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        if(super.performOk()) {
            mSettings.setHdrInfo(mHdrInfo.getSelection());
            mSettings.setLicense(mLicense.getSelection());
            mSettings.setNoConfig(mNoConfig.getSelection());
            mSettings.setNoCD(mNoCD.getSelection());
            mSettings.setVerbosity(mVerbosity.getSelectionIndex());
            mSettings.setCompressor(mCompressor.getSelectionIndex());
            mSettings.setInstructions((ArrayList)mInstructions.getInput());
            mSettings.setSymbols((Properties)mSymbols.getInput());
            mSettings.store();            
        }
        return false;
    }
    
    public final boolean validateSaveSymbol(String oldName, String newName, String newValue, boolean isEdit)
    {
        Map map = (Map)mSymbols.getInput();
        if(!isEdit || !oldName.equals(newName)) {
            if(map.containsKey(newName)) {
                if(MessageDialog.openConfirm(getShell(),
                                                 EclipseNSISPlugin.getResourceString("warning.title"), //$NON-NLS-1$
                                                 MessageFormat.format(EclipseNSISPlugin.getResourceString("symbol.overwrite.warning"), //$NON-NLS-1$
                                                                      new String[]{newName}))) {
                    if(isEdit) {
                        map.remove(oldName);
                    }
                }
                else {
                    return false;
                }
            }
        }
        map.put(newName,newValue);
        mSymbols.refresh();
        return true;
    }
    
    public final boolean validateSaveInstruction(String oldInstruction, String newInstruction, boolean isEdit)
    {
        Collection collection = (Collection)mInstructions.getInput();
        if(!Common.isEmpty(newInstruction)) {
            if(isEdit) {
                if(!oldInstruction.equals(newInstruction)) {
                    collection.remove(oldInstruction);
                }
                else {
                    return true;
                }
            }
            collection.add(newInstruction);
            mInstructions.refresh();
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * @return Returns the settings.
     */
    public NSISSettings getSettings()
    {
        return mSettings;
    }

    protected abstract String getPageDescription();
    
    protected abstract boolean canEnableControls();

    protected abstract NSISSettings loadSettings();

    /**
     * @param composite
     * @return
     */
    protected abstract Composite createEnablerControl(Composite parent);
}