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

import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElementFactory;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;
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

    protected CreationFactory getFactory(Object type) 
    {
        if (type instanceof String) {
            return InstallOptionsElementFactory.getFactory((String)type);
        }
        return null;
    }
    
    public boolean isEnabled(DropTargetEvent event)
    {
        if(((InstallOptionsEditDomain)getViewer().getEditDomain()).isReadOnly()) {
            return false;
        }
        return super.isEnabled(event);
    }
}
