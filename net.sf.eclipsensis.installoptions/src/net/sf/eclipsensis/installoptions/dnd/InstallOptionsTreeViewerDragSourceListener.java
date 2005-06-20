/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dnd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;


public class InstallOptionsTreeViewerDragSourceListener extends AbstractTransferDragSourceListener
{
    private List modelSelection;

    public InstallOptionsTreeViewerDragSourceListener(EditPartViewer viewer) 
    {
        super(viewer, InstallOptionsTreeViewerTransfer.getInstance());
    }

    public void dragSetData(DragSourceEvent event) 
    {
        event.data = getViewer().getSelectedEditParts();
    }

    public void dragStart(DragSourceEvent event) 
    {
        InstallOptionsTreeViewerTransfer.getInstance().setViewer(getViewer());
        List selection = getViewer().getSelectedEditParts();
        InstallOptionsTreeViewerTransfer.getInstance().setObject(selection);
        saveModelSelection(selection);
    }

    public void dragFinished(DragSourceEvent event) 
    {
        InstallOptionsTreeViewerTransfer.getInstance().setObject(null);
        InstallOptionsTreeViewerTransfer.getInstance().setViewer(null);
        revertModelSelection();
    }

    protected void revertModelSelection() 
    {
        List list = new ArrayList();
        for (int i = 0; i < modelSelection.size(); i++) {
            list.add(getViewer().getEditPartRegistry().get(modelSelection.get(i)));
        }
        getViewer().setSelection(new StructuredSelection(list));
    }

    protected void saveModelSelection(List editPartSelection) 
    {
        modelSelection = new ArrayList();
        for (int i = 0; i < editPartSelection.size(); i++) {
            EditPart editpart = (EditPart)editPartSelection.get(i);
            modelSelection.add(editpart.getModel());
        }
    }
}
