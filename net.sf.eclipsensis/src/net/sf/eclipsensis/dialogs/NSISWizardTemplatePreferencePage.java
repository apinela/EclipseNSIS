/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;
import net.sf.eclipsensis.viewer.EmptyContentProvider;
import net.sf.eclipsensis.wizard.NSISTemplateWizard;
import net.sf.eclipsensis.wizard.template.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

public class NSISWizardTemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private NSISWizardTemplateManager mTemplateManager = null;
    private NSISWizardTemplateReaderWriter mReaderWriter = null;

    private CheckboxTableViewer mTableViewer = null;
    private Button mAddButton = null;
    private Button mEditButton = null;
    private Button mImportButton = null;
    private Button mExportButton = null;
    private Button mRemoveButton = null;
    private Button mRestoreButton = null;
    private Button mRevertButton = null;
    private StyledText mDescriptionText = null;

    /**
     * 
     */
    public NSISWizardTemplatePreferencePage()
    {
        super();
        setDescription(EclipseNSISPlugin.getResourceString("wizard.template.preferences.description")); //$NON-NLS-1$
        mTemplateManager = new NSISWizardTemplateManager();
    }

    /*
     * @see PreferencePage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        WorkbenchHelp.setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_scrtmpltprefs_context"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite ancestor)
    {
        Composite parent= new Composite(ancestor, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        parent.setLayout(layout);               

        Composite innerParent= new Composite(parent, SWT.NONE);
        GridLayout innerLayout= new GridLayout();
        innerLayout.numColumns= 2;
        innerLayout.marginHeight= 0;
        innerLayout.marginWidth= 0;
        innerParent.setLayout(innerLayout);
        GridData gd= new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan= 2;
        innerParent.setLayoutData(gd);

        Table table= new Table(innerParent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        
        GridData data= new GridData(GridData.FILL_BOTH);
        data.widthHint= convertWidthInCharsToPixels(3);
        data.heightHint= convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);        

        TableLayout tableLayout= new TableLayout();
        table.setLayout(tableLayout);

        TableColumn column= new TableColumn(table, SWT.NONE);      
        column.setText(EclipseNSISPlugin.getResourceString("wizard.template.name.label")); //$NON-NLS-1$
        
        mTableViewer= new CheckboxTableViewer(table);       
        mTableViewer.setLabelProvider(new CollectionLabelProvider());
        mTableViewer.setContentProvider(new EmptyContentProvider() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
            public Object[] getElements(Object inputElement)
            {
                if(inputElement != null && inputElement instanceof NSISWizardTemplateManager) {
                    return ((NSISWizardTemplateManager)inputElement).getTemplates().toArray();
                }
                return super.getElements(inputElement);
            }
        });

        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        mTableViewer.setSorter(new ViewerSorter(collator));
        
        ViewerFilter filter = new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element instanceof NSISWizardTemplate) {
                    return !((NSISWizardTemplate)element).isDeleted();
                }
                return true;
            }
        };
        mTableViewer.addFilter(filter);

        mTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                edit();
            }
        });
        
        mTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                doSelectionChanged();
            }
        });

        mTableViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                NSISWizardTemplate template= (NSISWizardTemplate)event.getElement();
                if(template.getType() == NSISWizardTemplate.TYPE_DEFAULT) {
                    template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                }
                template.setEnabled(event.getChecked());
                mTableViewer.refresh(true);
                mTableViewer.setSelection(new StructuredSelection(template));
                doSelectionChanged();
            }
        });

        Composite buttons= new Composite(innerParent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);
        
        mAddButton= new Button(buttons, SWT.PUSH);
        mAddButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.new.label")); //$NON-NLS-1$
        mAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mAddButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                add();
            }
        });

        mEditButton= new Button(buttons, SWT.PUSH);
        mEditButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.edit.label")); //$NON-NLS-1$
        mEditButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mEditButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                edit();
            }
        });

        mRemoveButton= new Button(buttons, SWT.PUSH);
        mRemoveButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.remove.label")); //$NON-NLS-1$
        mRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRemoveButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                remove();
            }
        });

        createSeparator(buttons);
                
        mRestoreButton= new Button(buttons, SWT.PUSH);
        mRestoreButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.restore.label")); //$NON-NLS-1$
        mRestoreButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRestoreButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                restoreDeleted();
            }
        });

        mRevertButton= new Button(buttons, SWT.PUSH);
        mRevertButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.revert.label")); //$NON-NLS-1$
        mRevertButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRevertButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                revert();
            }
        });
        
        createSeparator(buttons);

        mImportButton= new Button(buttons, SWT.PUSH);
        mImportButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.import.label")); //$NON-NLS-1$
        mImportButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mImportButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                import_();
            }
        });
        
        mExportButton= new Button(buttons, SWT.PUSH);
        mExportButton.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.export.label")); //$NON-NLS-1$
        mExportButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mExportButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                export();
            }
        });

        Label label= new Label(parent, SWT.NONE);
        label.setText(EclipseNSISPlugin.getResourceString("wizard.template.description.label")); //$NON-NLS-1$
        data= new GridData();
        data.horizontalSpan= 2;
        label.setLayoutData(data);
        
        mDescriptionText = new StyledText(parent,SWT.BORDER|SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
        mDescriptionText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        mDescriptionText.setCursor(null);
        mDescriptionText.setCaret(null);
        data= new GridData(GridData.FILL_BOTH);
        data.horizontalSpan= 2;
        data.heightHint= convertHeightInCharsToPixels(5);
        mDescriptionText.setLayoutData(data);

        mTableViewer.setInput(mTemplateManager);
        mTableViewer.setAllChecked(false);
        mTableViewer.setCheckedElements(getEnabledTemplates());     

        updateButtons();
        configureTableResizing(innerParent, buttons, table, column);
        
        Dialog.applyDialogFont(parent);     
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }
    
    private NSISWizardTemplateReaderWriter getReaderWriter()
    {
        if(mReaderWriter == null) {
            synchronized(this) {
                if(mReaderWriter == null) {
                    mReaderWriter = new NSISWizardTemplateReaderWriter();
                }
            }
        }
        
        return mReaderWriter;
    }

    /**
     * Creates a separator between buttons
     * @param parent
     * @return
     */
    private Label createSeparator(Composite parent) 
    {
        Label separator= new Label(parent, SWT.NONE);
        separator.setVisible(false);
        GridData gd= new GridData();
        gd.horizontalAlignment= GridData.FILL;
        gd.verticalAlignment= GridData.BEGINNING;
        gd.heightHint= 4;
        separator.setLayoutData(gd);
        return separator;
    }

    private NSISWizardTemplate[] getEnabledTemplates() 
    {
        List enabled= new ArrayList();
        Collection coll = mTemplateManager.getTemplates();
        for (Iterator iter = coll.iterator(); iter.hasNext(); ) {
            NSISWizardTemplate template = (NSISWizardTemplate)iter.next();
            if (template.isEnabled() && !template.isDeleted()) {
                enabled.add(template);
            }
        }
        return (NSISWizardTemplate[]) enabled.toArray(new NSISWizardTemplate[enabled.size()]);
    }
    
    private void doSelectionChanged() 
    {      
        updateViewerInput();
        updateButtons();
    }
    
    /**
     * Updates the description.
     */
    protected void updateViewerInput() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        if (selection.size() == 1) {
            NSISWizardTemplate template= (NSISWizardTemplate) selection.getFirstElement();
            mDescriptionText.setText(template.getDescription());
        } 
        else {        
            mDescriptionText.setText(""); //$NON-NLS-1$
        }
    }

    /**
     * Updates the buttons.
     */
    protected void updateButtons() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();
        int selectionCount= selection.size();
        int itemCount= mTableViewer.getTable().getItemCount();
        boolean canRestore= mTemplateManager.canRestore();
        boolean canRevert= false;
        for (Iterator it= selection.iterator(); it.hasNext();) {
            if(mTemplateManager.canRevert((NSISWizardTemplate)it.next())) {
                canRevert= true;
                break;
            }
        }
        
        mEditButton.setEnabled(selectionCount == 1);
        mExportButton.setEnabled(selectionCount > 0);
        mRemoveButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
        mRestoreButton.setEnabled(canRestore);
        mRevertButton.setEnabled(canRevert);
    }
    
    private void add() 
    {
        NSISWizardTemplate template = new NSISWizardTemplate(""); //$NON-NLS-1$
        Dialog dialog= new NSISTemplateWizardDialog(getShell(),new NSISTemplateWizard(template));
        if (dialog.open() != Window.CANCEL) {
            mTemplateManager.addTemplate(template);
            mTableViewer.refresh(true);
            mTableViewer.setChecked(template, template.isEnabled());
            mTableViewer.setSelection(new StructuredSelection(template));           
        }
    }

    private void edit() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        Object[] objects= selection.toArray();      
        if ((objects == null) || (objects.length != 1)) {
            return;
        }
        
        NSISWizardTemplate data= (NSISWizardTemplate)selection.getFirstElement();
        edit(data);
    }

    private void edit(NSISWizardTemplate oldTemplate) 
    {
        NSISWizardTemplate newTemplate;
        try {
            newTemplate = (NSISWizardTemplate)oldTemplate.clone();
            Dialog dialog= new NSISTemplateWizardDialog(getShell(),new NSISTemplateWizard(newTemplate));
            if (dialog.open() == Window.OK) {
                mTemplateManager.updateTemplate(oldTemplate, newTemplate);
                mTableViewer.refresh(true);
                doSelectionChanged();
                mTableViewer.setChecked(newTemplate, newTemplate.isEnabled());
                mTableViewer.setSelection(new StructuredSelection(newTemplate));           
            }
        }
        catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
        
    /**
     * Correctly resizes the table so no phantom columns appear
     */
    private static void configureTableResizing(final Composite parent, final Composite buttons, final Table table, final TableColumn column1)
    {
        parent.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area= parent.getClientArea();
                Point preferredSize= table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width= area.width - 2 * table.getBorderWidth();
                if (preferredSize.y > area.height) {
                    Point vBarSize = table.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }
                width -= buttons.getSize().x;
                Point oldSize= table.getSize();
                if (oldSize.x > width) {
                    column1.setWidth(width);
                    table.setSize(width, area.height);
                } else {
                    table.setSize(width, area.height);
                    column1.setWidth(width);
                 }
            }
        });
    }

    private void import_() 
    {
        FileDialog dialog= new FileDialog(getShell());
        dialog.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.import.title")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("wizard.template.preferences.import.extension")}); //$NON-NLS-1$
        String path= dialog.open();
        
        if (path == null) {
            return;
        }
        
        try {
            File file= new File(path);
            if (file.exists()) {
                InputStream input= new BufferedInputStream(new FileInputStream(file));
                Collection coll = getReaderWriter().import_(input);
                input.close();
                if(!Common.isEmptyCollection(coll)) {
                    for (Iterator iter=coll.iterator(); iter.hasNext(); ) {
                        mTemplateManager.addTemplate((NSISWizardTemplate)iter.next());
                    }
                    
                    mTableViewer.refresh();
                    mTableViewer.setAllChecked(false);
                    mTableViewer.setCheckedElements(getEnabledTemplates());
                }
            }
        } 
        catch (Exception e) {
            Common.openError(getShell(), e.getLocalizedMessage());
        }
    }
    
    private void export() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();
        Collection templates= selection.toList();
        FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
        dialog.setText(EclipseNSISPlugin.getResourceString("wizard.template.preferences.export.title")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("wizard.template.preferences.import.extension")}); //$NON-NLS-1$
        dialog.setFileName(EclipseNSISPlugin.getResourceString("wizard.template.preferences.export.filename")); //$NON-NLS-1$
        String path= dialog.open();
        
        if (path == null) {
            return;
        }
        
        File file= new File(path);      

        if (!file.exists() || Common.openConfirm(getShell(),EclipseNSISPlugin.getFormattedString("wizard.template.preferences.export.save.confirm",new Object[]{file.getAbsolutePath()}))) { //$NON-NLS-1$
            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                getReaderWriter().export(templates, os);
            } 
            catch (Exception e) {
                Common.openError(getShell(),e.getLocalizedMessage());
            }
            finally {
                if(os != null) {
                    try {
                        os.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private void remove() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        if(!selection.isEmpty()) {
            Iterator elements= selection.iterator();
            while (elements.hasNext()) {
                NSISWizardTemplate template= (NSISWizardTemplate) elements.next();
                mTemplateManager.removeTemplate(template);
            }
    
            mTableViewer.refresh(true);
        }
    }
    
    private void restoreDeleted() 
    {
        mTemplateManager.restore();
        mTableViewer.refresh(true);
        mTableViewer.setCheckedElements(getEnabledTemplates());
        updateButtons();
    }
    
    private void revert() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        if(!selection.isEmpty()) {
            ArrayList list = new ArrayList();
            for (Iterator iter= selection.iterator(); iter.hasNext(); ) {
                NSISWizardTemplate temp = mTemplateManager.revert((NSISWizardTemplate) iter.next());
                if(temp != null) {
                    list.add(temp);
                }
            }
    
            mTableViewer.refresh(true);
            mTableViewer.setSelection(new StructuredSelection(list));
            doSelectionChanged();
            mTableViewer.setCheckedElements(getEnabledTemplates());
            mTableViewer.getTable().setFocus();
        }
    }
    
    /*
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        mTemplateManager.resetToDefaults();
        mTableViewer.refresh(true);
        mTableViewer.setAllChecked(false);
        mTableViewer.setCheckedElements(getEnabledTemplates());     
    }

    /*
     * @see PreferencePage#performOk()
     */ 
    public boolean performOk() {
        try {
            mTemplateManager.save();
        } 
        catch (IOException e) {
            Common.openError(getShell(),e.getLocalizedMessage());
            return false;
        }

        return super.performOk();
    }   
}
