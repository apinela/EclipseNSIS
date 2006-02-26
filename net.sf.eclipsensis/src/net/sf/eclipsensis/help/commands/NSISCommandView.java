/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;

public class NSISCommandView extends ViewPart implements INSISHomeListener
{
    private ListViewer mViewer;

    public void dispose()
    {
        NSISPreferences.INSTANCE.removeListener(this);
        super.dispose();
    }

    public void createPartControl(Composite parent)
    {
        List list = new List(parent,SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL);
        mViewer = new ListViewer(list);
        mViewer.setContentProvider(new ArrayContentProvider());
        mViewer.setLabelProvider(new LabelProvider());
        NSISPreferences.INSTANCE.addListener(this);
        mViewer.addDragSupport(DND.DROP_COPY, 
            new Transfer[]{NSISCommandTransfer.getInstance()},
            new DragSourceAdapter() {
                public void dragStart(DragSourceEvent e)
                {
                    IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                    if (!(editor instanceof NSISEditor)) {
                        e.doit = false;
                    }
                }
    
                public void dragSetData(DragSourceEvent e)
                {
                    IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                    if(sel != null && !sel.isEmpty()) {
                        e.data = sel.getFirstElement();
                    }
                    else {
                        e.data = ""; //$NON-NLS-1$
                    }
                }
    
                public void dragFinished(DragSourceEvent e)
                {
                }
            }
        );
        mViewer.getList().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e)
            {
                if( (e.character == SWT.CR || e.character == SWT.LF) && e.stateMask == 0) {
                    insertCommand(mViewer.getSelection());
                }
            }
        });
        mViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                insertCommand(event.getSelection());
            }
        });
        updateInput();
    }

    public void setFocus()
    {
        mViewer.getControl().setFocus();
    }

    public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
    {
        Display.getDefault().asyncExec(new Runnable() {
            public void run()
            {
                updateInput();
            }
        });
    }

    private void updateInput()
    {
        if(mViewer != null) {
            List list = mViewer.getList();
            if(list != null && !list.isDisposed()) {
                ISelection sel = mViewer.getSelection();
                String[] commands;
                try {
                    commands = NSISCommandManager.getCommands();
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    commands = null;
                }                
                mViewer.setInput(commands);
                if(!sel.isEmpty()) {
                    mViewer.setSelection(sel);
                }
            }
        }
    }

    /**
     * @param sel
     */
    private void insertCommand(ISelection sel)
    {
        if(sel instanceof IStructuredSelection && !sel.isEmpty()) {
            String command = (String)((IStructuredSelection)sel).getFirstElement();
            try {
                IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                if(editor instanceof NSISEditor) {
                    ((NSISEditor)editor).insertCommand(command);
                }
            }
            catch(Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
            }
        }
    }
}
