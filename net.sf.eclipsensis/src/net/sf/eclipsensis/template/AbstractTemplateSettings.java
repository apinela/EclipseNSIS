/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;
import net.sf.eclipsensis.viewer.EmptyContentProvider;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public abstract class AbstractTemplateSettings extends Composite
{
    private AbstractTemplateManager mTemplateManager = null;

    private CheckboxTableViewer mTableViewer = null;
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
    public AbstractTemplateSettings(Composite parent, int style, AbstractTemplateManager manager)
    {
        super(parent, style);
        mTemplateManager = manager;
        createContents();
    }

    protected CheckboxTableViewer getTableViewer()
    {
        return mTableViewer;
    }

    protected AbstractTemplateManager getTemplateManager()
    {
        return mTemplateManager;
    }

    protected void createContents()
    {
        GC gc = new GC(this);
        gc.setFont(JFaceResources.getDialogFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        this.setLayout(layout);
        
        Composite innerParent= new Composite(this, SWT.NONE);
        GridLayout innerLayout= new GridLayout();
        innerLayout.numColumns= 2;
        innerLayout.marginHeight= 0;
        innerLayout.marginWidth= 0;
        innerParent.setLayout(innerLayout);
        GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan= 2;
        innerParent.setLayoutData(gd);

        Table table= new Table(innerParent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
        
        GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint= fontMetrics.getAverageCharWidth()*3;
        data.heightHint= fontMetrics.getHeight()*10;
        table.setLayoutData(data);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);        

        TableColumn[] columns= {new TableColumn(table, SWT.NONE)};      
        columns[0].setText(EclipseNSISPlugin.getResourceString("template.name.label")); //$NON-NLS-1$
        
        mTableViewer= new CheckboxTableViewer(table);       
        mTableViewer.setLabelProvider(new CollectionLabelProvider());
        mTableViewer.setContentProvider(new EmptyContentProvider() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
            public Object[] getElements(Object inputElement)
            {
                if(inputElement != null && inputElement instanceof AbstractTemplateManager) {
                    return ((AbstractTemplateManager)inputElement).getTemplates().toArray();
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
                if(element instanceof AbstractTemplate) {
                    return !((AbstractTemplate)element).isDeleted();
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
                AbstractTemplate oldTemplate= (AbstractTemplate)event.getElement();
                AbstractTemplate newTemplate= (AbstractTemplate)oldTemplate.clone();
                if(newTemplate.getType() == AbstractTemplate.TYPE_DEFAULT) {
                    newTemplate.setType(AbstractTemplate.TYPE_CUSTOM);
                }
                newTemplate.setEnabled(event.getChecked());
                getTemplateManager().updateTemplate(oldTemplate, newTemplate);
                mTableViewer.refresh(true);
                mTableViewer.setSelection(new StructuredSelection(newTemplate));
                doSelectionChanged();
            }
        });

        Composite buttons= new Composite(innerParent, SWT.NONE);
        buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);

        createButtons(buttons);

        Label label= new Label(this, SWT.NONE);
        label.setText(EclipseNSISPlugin.getResourceString("template.description.label")); //$NON-NLS-1$
        data= new GridData();
        data.horizontalSpan= 2;
        label.setLayoutData(data);
        
        mDescriptionText = new StyledText(this,SWT.BORDER|SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
        mDescriptionText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        mDescriptionText.setCursor(null);
        mDescriptionText.setCaret(null);
        data= new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalSpan= 2;
        data.heightHint= fontMetrics.getHeight()*5;
        mDescriptionText.setLayoutData(data);

        mTableViewer.setInput(mTemplateManager);
        mTableViewer.setAllChecked(false);
        mTableViewer.setCheckedElements(getEnabledTemplates());     

        updateButtons();
        table.addControlListener(new TableResizer());
        
        Dialog.applyDialogFont(this);
    }

    protected void createButtons(Composite parent)
    {
        mEditButton= new Button(parent, SWT.PUSH);
        mEditButton.setText(EclipseNSISPlugin.getResourceString("template.settings.edit.label")); //$NON-NLS-1$
        mEditButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mEditButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                edit();
            }
        });

        mRemoveButton= new Button(parent, SWT.PUSH);
        mRemoveButton.setText(EclipseNSISPlugin.getResourceString("template.settings.remove.label")); //$NON-NLS-1$
        mRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRemoveButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                remove();
            }
        });

        createSeparator(parent);
                
        mRestoreButton= new Button(parent, SWT.PUSH);
        mRestoreButton.setText(EclipseNSISPlugin.getResourceString("template.settings.restore.label")); //$NON-NLS-1$
        mRestoreButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRestoreButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                restoreDeleted();
            }
        });

        mRevertButton= new Button(parent, SWT.PUSH);
        mRevertButton.setText(EclipseNSISPlugin.getResourceString("template.settings.revert.label")); //$NON-NLS-1$
        mRevertButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRevertButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                revert();
            }
        });
        
        createSeparator(parent);

        mImportButton= new Button(parent, SWT.PUSH);
        mImportButton.setText(EclipseNSISPlugin.getResourceString("template.settings.import.label")); //$NON-NLS-1$
        mImportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mImportButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                import_();
            }
        });
        
        mExportButton= new Button(parent, SWT.PUSH);
        mExportButton.setText(EclipseNSISPlugin.getResourceString("template.settings.export.label")); //$NON-NLS-1$
        mExportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mExportButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                export();
            }
        });
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
        GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gd.heightHint= 4;
        separator.setLayoutData(gd);
        return separator;
    }

    private AbstractTemplate[] getEnabledTemplates() 
    {
        List enabled= new ArrayList();
        Collection coll = mTemplateManager.getTemplates();
        for (Iterator iter = coll.iterator(); iter.hasNext(); ) {
            AbstractTemplate template = (AbstractTemplate)iter.next();
            if (template.isEnabled() && !template.isDeleted()) {
                enabled.add(template);
            }
        }
        return (AbstractTemplate[]) enabled.toArray(new AbstractTemplate[enabled.size()]);
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
            AbstractTemplate template= (AbstractTemplate) selection.getFirstElement();
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
            if(mTemplateManager.canRevert((AbstractTemplate)it.next())) {
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

    private void edit() 
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        Object[] objects= selection.toArray();      
        if ((objects == null) || (objects.length != 1)) {
            return;
        }
        
        AbstractTemplate data= (AbstractTemplate)selection.getFirstElement();
        edit(data);
    }

    private void edit(AbstractTemplate oldTemplate) 
    {
        AbstractTemplate newTemplate = (AbstractTemplate)oldTemplate.clone();
        Dialog dialog= createDialog(newTemplate);
        if (dialog.open() == Window.OK) {
            mTemplateManager.updateTemplate(oldTemplate, newTemplate);
            mTableViewer.refresh(true);
            doSelectionChanged();
            mTableViewer.setChecked(newTemplate, newTemplate.isEnabled());
            mTableViewer.setSelection(new StructuredSelection(newTemplate));           
        }
    }

    private void import_() 
    {
        FileDialog dialog= new FileDialog(getShell());
        dialog.setText(EclipseNSISPlugin.getResourceString("template.settings.import.title")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("template.settings.import.extension")}); //$NON-NLS-1$
        String path= dialog.open();
        
        if (path == null) {
            return;
        }
        
        try {
            File file= new File(path);
            if (file.exists()) {
                InputStream input= new BufferedInputStream(new FileInputStream(file));
                Collection coll = mTemplateManager.getReaderWriter().import_(input);
                input.close();
                if(!Common.isEmptyCollection(coll)) {
                    for (Iterator iter=coll.iterator(); iter.hasNext(); ) {
                        mTemplateManager.addTemplate((AbstractTemplate)iter.next());
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
        dialog.setText(EclipseNSISPlugin.getResourceString("template.settings.export.title")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("template.settings.import.extension")}); //$NON-NLS-1$
        dialog.setFileName(EclipseNSISPlugin.getResourceString("template.settings.export.filename")); //$NON-NLS-1$
        String path= dialog.open();
        
        if (path == null) {
            return;
        }
        
        File file= new File(path);      

        if (!file.exists() || Common.openConfirm(getShell(),EclipseNSISPlugin.getFormattedString("template.settings.export.save.confirm",new Object[]{file.getAbsolutePath()}))) { //$NON-NLS-1$
            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                mTemplateManager.getReaderWriter().export(templates, os);
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
                AbstractTemplate template= (AbstractTemplate) elements.next();
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
                AbstractTemplate temp = (AbstractTemplate)mTemplateManager.revert((AbstractTemplate) iter.next());
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
    
    public void performDefaults() {
        mTemplateManager.resetToDefaults();
        mTableViewer.refresh(true);
        mTableViewer.setAllChecked(false);
        mTableViewer.setCheckedElements(getEnabledTemplates());     
    }

    public boolean performOk() {
        try {
            mTemplateManager.save();
            return true;
        } 
        catch (IOException e) {
            Common.openError(getShell(),e.getLocalizedMessage());
            return false;
        }
    }   
    
    protected abstract AbstractTemplate createTemplate(String name);
    protected abstract Dialog createDialog(AbstractTemplate newTemplate);
}
