/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.wizard.INSISWizardConstants;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NSISWizardTemplateDialog extends Dialog implements INSISWizardConstants
{
    public static final int MODE_LOAD = 0;
    public static final int MODE_SAVE = 1;
    
    private static final int DELETE_ID = IDialogConstants.CLIENT_ID + 1;
    
    private int mMode = MODE_LOAD;
    private String mTemplateName = ""; //$NON-NLS-1$
    private ListViewer mTemplatesViewer;
    private Text mTemplateText;

    /**
     * @param parentShell
     */
    public NSISWizardTemplateDialog(Shell parentShell, int mode)
    {
        super(parentShell);
        mMode = mode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText(EclipseNSISPlugin.getResourceString(mMode==MODE_LOAD?
                                                             "wizard.template.dialog.load.title": //$NON-NLS-1$
                                                             "wizard.template.dialog.save.title")); //$NON-NLS-1$
        super.configureShell(newShell);
    }

    /**
     * @return Returns the mode.
     */
    int getMode()
    {
        return mMode;
    }

    /**
     * @param mode The mode to set.
     */
    void setMode(int mode)
    {
        mMode = mode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        Button b = createButton(parent, DELETE_ID, 
                    EclipseNSISPlugin.getResourceString("wizard.template.dialog.delete.button.label"), //$NON-NLS-1$
                    false);
        boolean empty = mTemplatesViewer.getSelection().isEmpty();
        b.setEnabled(!empty);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                String text = mTemplateText.getText();
                if(!Common.isEmpty(text)) {
                    File f = new File(EclipseNSISPlugin.getPluginStateLocation(),text+WIZARD_TEMPLATE_EXTENSION);
                    if(f.exists()) {
                        if(MessageDialog.openQuestion(getShell(),EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                           MessageFormat.format(EclipseNSISPlugin.getResourceString("wizard.template.dialog.delete.confirmation"), //$NON-NLS-1$
                                                 new String[]{text}))) {
                           if(f.delete()) {
                               mTemplatesViewer.remove(text);
                               mTemplateText.setText(""); //$NON-NLS-1$
                           }
                        }
                    }
                }
            }
        });
        super.createButtonsForButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(mMode == MODE_SAVE?!empty:!Common.isEmpty(mTemplateName));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        ((GridLayout)composite.getLayout()).numColumns=1;
        ((GridData)composite.getLayoutData()).heightHint = 200;
        
        mTemplateText = NSISWizardDialogUtil.createText(composite,mTemplateName,1,true,null);
        mTemplateText.setEditable(mMode == MODE_SAVE);
        String[] files = NSISWizard.getTemplateFolder().list(new FilenameFilter(){
            public boolean accept(File dir, String name) 
            {
                return (new File(dir, name).isFile() && name.toLowerCase().endsWith(WIZARD_TEMPLATE_EXTENSION));
            }
        });
        mTemplateText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                String text = mTemplateText.getText();
                getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(text) && Common.isValidFileName(text));
                getButton(DELETE_ID).setEnabled(!Common.isEmpty(text) && (Collections.binarySearch((ArrayList)mTemplatesViewer.getInput(),text,String.CASE_INSENSITIVE_ORDER)>=0));
            }
        });
        ArrayList templates = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            templates.add(files[i].substring(0,files[i].length()-WIZARD_TEMPLATE_EXTENSION.length()));
        }
        Collections.sort(templates, String.CASE_INSENSITIVE_ORDER);

        List templatesList = new List(composite, SWT.BORDER|SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL);
        GridData data = new GridData(GridData.FILL_BOTH);
        templatesList.setLayoutData(data);
        
        mTemplatesViewer = new ListViewer(templatesList);
        CollectionContentProvider collectionContentProvider = new CollectionContentProvider();
        mTemplatesViewer.setContentProvider(collectionContentProvider);
        mTemplatesViewer.setInput(templates);
        Collator coll = Collator.getInstance();
        coll.setStrength(Collator.PRIMARY);
        mTemplatesViewer.setSorter(new ViewerSorter(coll));
        int n = -1;
        if(!Common.isEmpty(mTemplateName) && (n = Collections.binarySearch(templates,mTemplateName,String.CASE_INSENSITIVE_ORDER)) >= 0) {
            mTemplatesViewer.setSelection(new StructuredSelection(templates.get(n)));
            mTemplateName = (String)((IStructuredSelection)mTemplatesViewer.getSelection()).getFirstElement();
        }
        
        templatesList.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e)
            {
                ISelection sel = mTemplatesViewer.getSelection();
                if(!sel.isEmpty()) {
                    mTemplateText.setText((String)((IStructuredSelection)sel).getFirstElement());
                }
                else {
                    mTemplateText.setText(""); //$NON-NLS-1$
                }
//                getButton(IDialogConstants.OK_ID).setEnabled(!sel.isEmpty());
//                getButton(DELETE_ID).setEnabled(!sel.isEmpty());
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                okPressed();
            }
        });
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        String text = mTemplateText.getText();
        if(Common.isEmpty(text)) {
            MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                    EclipseNSISPlugin.getResourceString(mMode==MODE_LOAD?
                                                    "wizard.template.dialog.load.select.error": //$NON-NLS-1$
                                                    "wizard.template.dialog.save.select.error")); //$NON-NLS-1$
            return;
        }
        else {
            File f = new File(NSISWizard.getTemplateFolder(),text+WIZARD_TEMPLATE_EXTENSION);
            if(mMode == MODE_LOAD) {
                if(!f.exists()) {
                    MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                            EclipseNSISPlugin.getResourceString("wizard.template.dialog.load.error")); //$NON-NLS-1$
                    return;
                }
            }
            else {
                if(f.exists()) {
                    if(!MessageDialog.openQuestion(getShell(),EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                        MessageFormat.format(EclipseNSISPlugin.getResourceString("wizard.template.dialog.save.warning"), //$NON-NLS-1$
                                             new String[]{text}))) {
                        return;
                    }
                }
            }
        }
        mTemplateName = text;
        super.okPressed();
    }
    
    /**
     * @return Returns the templateName.
     */
    public String getTemplateName()
    {
        return mTemplateName;
    }
    
    /**
     * @param templateName The templateName to set.
     */
    public void setTemplateName(String templateName)
    {
        mTemplateName = templateName;
    }
}
