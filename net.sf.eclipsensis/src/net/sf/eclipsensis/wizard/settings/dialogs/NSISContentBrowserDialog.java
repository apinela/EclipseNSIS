/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.Arrays;
import java.util.HashSet;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class NSISContentBrowserDialog extends Dialog
{
    private NSISWizardSettings mSettings = null;
    private INSISInstallElement mElement = null;
    
    /**
     * @param parentShell
     */
    public NSISContentBrowserDialog(Shell parentShell, NSISWizardSettings settings)
    {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        mSettings = settings;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText(EclipseNSISPlugin.getResourceString("wizard.content.browser.title"));
        Composite parent = newShell.getParent();
        if(parent != null) {
            Point point = parent.toDisplay(0,0);
            newShell.setBounds(point.x,point.y,400,400);
        }
        else {
            newShell.setSize(400,400);
        }
        super.configureShell(newShell);
    }

    /**
     * @return Returns the selected element.
     */
    public INSISInstallElement getSelectedElement()
    {
        return mElement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    protected void cancelPressed()
    {
        mElement = null;
        super.cancelPressed();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.select.shortcut.message",true,null,false);
        
        ViewerFilter vf = new ViewerFilter() {
            private HashSet mTypes = new HashSet(Arrays.asList(new String[]{
                                                    NSISWizardSettings.INSTALLER_TYPE,
                                                    NSISSubSection.TYPE,
                                                    NSISSection.TYPE,
                                                    NSISInstallDirectory.TYPE,
                                                    NSISInstallFile.TYPE,
                                                    NSISInstallFiles.TYPE,
                                                    NSISInstallFiles.FILEITEM_TYPE
                                                 }));
            
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element instanceof INSISInstallElement) {
                    INSISInstallElement installElement = (INSISInstallElement)element;
                    if(mTypes.contains(installElement.getType())) {
                        if(installElement instanceof NSISInstallDirectory) {
                            return !((NSISInstallDirectory)installElement).isCopyFolderContents();
                        }
                        else {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        final Tree tree = new Tree(composite,SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        tree.setLayoutData(gd);
        final TreeViewer tv = new TreeViewer(tree);
        tv.setLabelProvider(new NSISInstallElementLabelProvider());
        tv.setContentProvider(new NSISInstallElementTreeContentProvider(mSettings));
        tv.addSelectionChangedListener(new ISelectionChangedListener(){
            private HashSet mTypes = new HashSet(Arrays.asList(new String[]{
                    NSISInstallDirectory.TYPE,
                    NSISInstallFile.TYPE,
                    NSISInstallFiles.FILEITEM_TYPE
                 }));

            public void selectionChanged(SelectionChangedEvent event) 
            {
                Button okButton = getButton(IDialogConstants.OK_ID);
                boolean enabled = false;
                mElement = null;
                ISelection sel = event.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    IStructuredSelection ssel = (IStructuredSelection)sel;
                    if(ssel.size() == 1) {
                        Object obj = ssel.getFirstElement();
                        if(obj instanceof INSISInstallElement && mTypes.contains(((INSISInstallElement)obj).getType())) {
                            mElement = (INSISInstallElement)obj;
                            enabled = true;
                        }
                    }
                }
                okButton.setEnabled(enabled);
            }
        });
        tv.setAutoExpandLevel(2);
        tv.addFilter(vf);
        tv.setInput(mSettings);

        return composite;
    }
}