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

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class NSISSettingsEditor
{
    private NSISSettings mSettings = null;

    private TabFolder mFolder = null;
    protected Button mHdrInfo = null;
    protected Button mLicense = null;
    protected Button mNoConfig = null;
    protected Button mNoCD = null;
    protected Combo mVerbosity = null;
    protected Combo mCompressor = null;
    protected Button mSolidCompression = null;
    protected TableViewer mInstructions = null;
    protected TableViewer mSymbols = null;
    
    private Group mGroup = null;
    private List mListeners = new ArrayList();

    public void addListener(INSISSettingsEditorListener listener)
    {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(INSISSettingsEditorListener listener)
    {
        mListeners.remove(listener);
    }

    public boolean isValid()
    {
        return true;
    }
    
    protected void fireChanged()
    {
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            ((INSISSettingsEditorListener)iter.next()).settingsChanged();
        }
    }

    public Control createControl(Composite parent)
    {
        final TabFolder folder = new TabFolder(parent, SWT.NONE);

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("general.tab.text")); //$NON-NLS-1$
        item.setToolTipText(EclipseNSISPlugin.getResourceString("general.tab.tooltip")); //$NON-NLS-1$
        item.setControl(createGeneralPage(folder));

        item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("symbols.tab.text")); //$NON-NLS-1$
        item.setToolTipText(EclipseNSISPlugin.getResourceString("symbols.tab.tooltip")); //$NON-NLS-1$
        item.setControl(createSymbolsPage(folder));

        folder.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        try {
                            TabItem item = folder.getSelection()[0];
                            if(!item.getControl().isEnabled()) {
                                folder.setSelection(0);
                            }
                        }
                        catch(Exception ex) {
                            EclipseNSISPlugin.getDefault().log(ex);
                        }
                    }
                });

        Dialog.applyDialogFont(folder);
        mFolder = folder;
        return mFolder;
    }

    protected Control createGeneralPage(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        Composite child = createMasterControl(composite);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        child.setLayoutData(data);

        mGroup = createNSISOptionsGroup(composite);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
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
    
    public void updateControlsState()
    {
        enableControls(canEnableControls());
    }

    protected void enableControls(boolean state)
    {
        if(mGroup != null) {
            enableComposite(mGroup,state);
            if(state) {
                //Hack to properly enable the buttons
                mSymbols.setSelection(mSymbols.getSelection());
                setSolidCompressionState();
            }
        }
        if(mInstructions != null) {
            enableComposite(mInstructions.getControl().getParent(),state);
            if(state) {
                //Hack to properly enable the buttons
                mInstructions.setSelection(mInstructions.getSelection());
            }
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
        group.setLayout(layout);

        NSISSettings settings = getSettings();
        mHdrInfo = createCheckBox(group, EclipseNSISPlugin.getResourceString("hdrinfo.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("hdrinfo.tooltip"), //$NON-NLS-1$
                                  settings.getHdrInfo());

        mLicense = createCheckBox(group, EclipseNSISPlugin.getResourceString("license.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("license.tooltip"), //$NON-NLS-1$
                                  settings.getLicense());

        mNoConfig = createCheckBox(group, EclipseNSISPlugin.getResourceString("noconfig.text"), //$NON-NLS-1$
                                   EclipseNSISPlugin.getResourceString("noconfig.tooltip"), //$NON-NLS-1$
                                   settings.getNoConfig());

        mNoCD = createCheckBox(group, EclipseNSISPlugin.getResourceString("nocd.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("nocd.tooltip"), //$NON-NLS-1$
                               settings.getNoCD());

        Composite composite  = new Composite(group,SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        mVerbosity = createCombo(composite,EclipseNSISPlugin.getResourceString("verbosity.text"),EclipseNSISPlugin.getResourceString("verbosity.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 INSISPreferenceConstants.VERBOSITY_ARRAY, settings.getVerbosity());
        Label l = new Label(composite,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        mCompressor = createCombo(composite,EclipseNSISPlugin.getResourceString("compressor.text"),EclipseNSISPlugin.getResourceString("compressor.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY,settings.getCompressor());
        mSolidCompression = createCheckBox(composite, EclipseNSISPlugin.getResourceString("solid.compression.text"), //$NON-NLS-1$
                                          EclipseNSISPlugin.getResourceString("solid.compression.tooltip"), //$NON-NLS-1$
                                          settings.getSolidCompression());
        mSolidCompression.setVisible(NSISPreferences.INSTANCE.isSolidCompressionSupported());
        mCompressor.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                setSolidCompressionState();
            }
        });
        return group;
    }

    private void setSolidCompressionState()
    {
        int n = mCompressor.getSelectionIndex();
        mSolidCompression.setEnabled(n != MakeNSISRunner.COMPRESSOR_DEFAULT && n != MakeNSISRunner.COMPRESSOR_BEST);
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
            table.addControlListener(createTableControlListener());
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

    protected ControlAdapter createTableControlListener()
    {
        return new TableResizer();
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

    protected Control createSymbolsPage(final Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditSymbol(parent.getShell(),"",""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Map.Entry entry = (Map.Entry)((IStructuredSelection)mSymbols.getSelection()).getFirstElement();
                addOrEditSymbol(parent.getShell(),(String)entry.getKey(),(String)entry.getValue());
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Map map = (Map)mSymbols.getInput();
                IStructuredSelection selection = (IStructuredSelection)mSymbols.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    map.remove(((Map.Entry)iter.next()).getKey());
                    fireChanged();
                }
                mSymbols.refresh();
            }
        };

        TableViewerUpDownMover mover = new TableViewerUpDownMover() {

            protected List getAllElements()
            {
                return new ArrayList(((LinkedHashMap)((TableViewer)getViewer()).getInput()).entrySet());
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((LinkedHashMap)input).clear();
                for(Iterator iter=elements.iterator(); iter.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    ((LinkedHashMap)input).put(entry.getKey(),entry.getValue());
                    fireChanged();
                }
            }

        };

        IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                Map.Entry entry = (Map.Entry)((IStructuredSelection)event.getSelection()).getFirstElement();
                addOrEditSymbol(parent.getShell(),(String)entry.getKey(),(String)entry.getValue());
            }
        };

        mSymbols = createTableViewer(composite, getSettings().getSymbols(), new MapContentProvider(), new MapLabelProvider(),
                                     EclipseNSISPlugin.getResourceString("symbols.description"), //$NON-NLS-1$
                                     new String[] {
                                         EclipseNSISPlugin.getResourceString("symbols.name.text"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.value.text")}, //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.add.tooltip"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.edit.tooltip"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.remove.tooltip"), //$NON-NLS-1$
                                     addAdapter,editAdapter,removeAdapter, doubleClickListener,
                                     mover);
        return composite;
    }

    private Control createInstructionsViewer(final Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditInstruction(parent.getShell(),""); //$NON-NLS-1$
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditInstruction(parent.getShell(),((String)((IStructuredSelection)mInstructions.getSelection()).getFirstElement()).trim());
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Collection collection = (Collection)mInstructions.getInput();
                IStructuredSelection selection = (IStructuredSelection)mInstructions.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    collection.remove(iter.next());
                    fireChanged();
                }
                mInstructions.refresh();
            }
        };

        TableViewerUpDownMover mover = new TableViewerUpDownMover() {

            protected List getAllElements()
            {
                return (ArrayList)((TableViewer)getViewer()).getInput();
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((ArrayList)input).clear();
                ((ArrayList)input).addAll(elements);
                fireChanged();
            }
        };

        IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {

                addOrEditInstruction(parent.getShell(),((String)((IStructuredSelection)event.getSelection()).getFirstElement()).trim());
            }
        };

        mInstructions = createTableViewer(composite, getSettings().getInstructions(),
                                      new CollectionContentProvider(), new CollectionLabelProvider(),
                                      EclipseNSISPlugin.getResourceString("instructions.description"), //$NON-NLS-1$
                                      new String[]{EclipseNSISPlugin.getResourceString("instructions.instruction.text")}, //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.add.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.edit.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.remove.tooltip"), //$NON-NLS-1$
                                      addAdapter,editAdapter,removeAdapter, doubleClickListener, mover);
        ((GridLayout)composite.getLayout()).marginWidth = 0;
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);

        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public final boolean performApply()
    {
        NSISSettings settings = getSettings();
        if(mFolder != null) {
            if(!performApply(settings)) {
                return false;
            }
        }
        settings.store();
        return true;
    }

    protected boolean performApply(NSISSettings settings)
    {
        settings.setHdrInfo(mHdrInfo.getSelection());
        settings.setLicense(mLicense.getSelection());
        settings.setNoConfig(mNoConfig.getSelection());
        settings.setNoCD(mNoCD.getSelection());
        settings.setVerbosity(mVerbosity.getSelectionIndex());
        settings.setCompressor(mCompressor.getSelectionIndex());
        settings.setSolidCompression(mSolidCompression.getSelection());
        settings.setInstructions((ArrayList)mInstructions.getInput());
        settings.setSymbols((LinkedHashMap)mSymbols.getInput());
        return true;
    }
    
    public void reset()
    {
        NSISSettings settings = getSettings();
        mHdrInfo.setSelection(settings.getHdrInfo());
        mLicense.setSelection(settings.getLicense());
        mNoConfig.setSelection(settings.getNoConfig());
        mNoCD.setSelection(settings.getNoCD());
        mVerbosity.select(settings.getVerbosity());
        mCompressor.select(settings.getCompressor());
        mSolidCompression.setSelection(settings.getSolidCompression());
        mInstructions.setInput(settings.getInstructions());
        mSymbols.setInput(settings.getSymbols());
    }
    
    public void setDefaults()
    {
        NSISSettings settings = getSettings();
        mHdrInfo.setSelection(settings.getDefaultHdrInfo());
        mLicense.setSelection(settings.getDefaultLicense());
        mNoConfig.setSelection(settings.getDefaultNoConfig());
        mNoCD.setSelection(settings.getDefaultNoCD());
        mVerbosity.select(settings.getDefaultVerbosity());
        mCompressor.select(settings.getDefaultCompressor());
        mSolidCompression.setSelection(settings.getDefaultSolidCompression());
        mInstructions.setInput(settings.getDefaultInstructions());
        mSymbols.setInput(settings.getDefaultSymbols());
    }

    private void addOrEditSymbol(Shell shell, String oldName, String oldValue)
    {
        Map map = (Map)mSymbols.getInput();
        NSISSymbolDialog dialog = new NSISSymbolDialog(shell,oldName, oldValue);
        Collection coll = new HashSet(map.keySet());
        coll.remove(oldName);
        dialog.setExistingSymbols(coll);
        if(dialog.open() == Window.OK) {
            String newName = dialog.getName();
            boolean dirty = false;
            if(!Common.isEmpty(oldName)) {
                if(!oldName.equals(newName)) {
                    map.remove(oldName);
                    dirty = true;
                }
            }
            else {
                dirty = true;
            }
            String newValue = dialog.getValue();
            if(!oldValue.equals(newValue)) {
                dirty = true;
            }
            map.put(newName,newValue);
            mSymbols.refresh(true);
            if(dirty) {
                fireChanged();
            }
        }
    }

    private void addOrEditInstruction(Shell shell, String oldInstruction)
    {
        NSISInstructionDialog dialog = new NSISInstructionDialog(shell,oldInstruction);
        if(dialog.open() == Window.OK) {
            String newInstruction = dialog.getInstruction();
            Collection collection = (Collection)mInstructions.getInput();
            if(!Common.isEmpty(oldInstruction)) {
                if(!oldInstruction.equals(newInstruction)) {
                    collection.remove(oldInstruction);
                    fireChanged();
                }
            }
            else {
                fireChanged();
            }
            collection.add(newInstruction);
            mInstructions.refresh(true);
        }
    }

    /**
     * @return Returns the settings.
     */
    public NSISSettings getSettings()
    {
        if(mSettings == null) {
            mSettings = loadSettings();
        }
        return mSettings;
    }

    protected abstract boolean canEnableControls();

    protected abstract Composite createMasterControl(Composite parent);

    protected abstract NSISSettings loadSettings();
}
