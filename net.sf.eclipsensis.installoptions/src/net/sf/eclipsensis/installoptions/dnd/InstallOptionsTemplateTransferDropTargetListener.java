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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElementFactory;
import net.sf.eclipsensis.installoptions.template.*;

import org.eclipse.gef.*;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;

public class InstallOptionsTemplateTransferDropTargetListener extends TemplateTransferDropTargetListener
{
    /**
     * @param viewer
     */
    public InstallOptionsTemplateTransferDropTargetListener(
            EditPartViewer viewer)
    {
        super(viewer);
    }

    protected Request createTargetRequest() 
    {
        Request request = super.createTargetRequest();
        if(request instanceof CreateRequest) {
            CreateRequest req = (CreateRequest)request;
            if(InstallOptionsTemplateCreationFactory.TYPE.equals(req.getNewObjectType())) {
                req.setType(IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE);
            }
        }
        return request;
    }

    protected CreationFactory getFactory(Object type) 
    {
        if (type instanceof String) {
            return InstallOptionsElementFactory.getFactory((String)type);
        }
        else if(type instanceof InstallOptionsTemplate) {
            return InstallOptionsTemplateManager.INSTANCE.getTemplateFactory((InstallOptionsTemplate)type);
        }
        return null;
    }
    
    protected void handleDrop() {
        super.handleDrop();
        selectAddedObjects();
    }

    protected void selectAddedObjects() 
    {
        Object model = getCreateRequest().getNewObject();
        if (model == null || !model.getClass().isArray()) {
            return;
        }
        EditPartViewer viewer = getViewer();
        viewer.getControl().forceFocus();
        Object[] models = (Object[])model;
        List selection = new ArrayList();
        for (int i = 0; i < models.length; i++) {
            Object editpart = viewer.getEditPartRegistry().get(models[i]);
            if (editpart instanceof EditPart) {
                selection.add(editpart);
            }
        }
        if (selection.size() > 0) {
            //Force a layout first.
            getViewer().flush();
            viewer.setSelection(new StructuredSelection(selection));
        }
    }

    public boolean isEnabled(DropTargetEvent event)
    {
        if(((InstallOptionsEditDomain)getViewer().getEditDomain()).isReadOnly()) {
            return false;
        }
        return super.isEnabled(event);
    }
}
