/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.DirectEditPolicy;

public abstract class InstallOptionsDirectEditPolicy extends DirectEditPolicy
{
    protected EditPart mEditPart;
    
    /**
     * 
     */
    public InstallOptionsDirectEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }

    public boolean understandsRequest(Request request) 
    {
        if(((InstallOptionsEditDomain)mEditPart.getViewer().getEditDomain()).isReadOnly()) {
            return false;
        }
        return super.understandsRequest(request);
    }
}
