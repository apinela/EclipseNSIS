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

import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogTreeEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

public class TreePartFactory implements EditPartFactory
{
    private static TreePartFactory cInstance = null;
    
    public static final TreePartFactory getInstance()
    {
        if(cInstance == null) {
            synchronized(TreePartFactory.class) {
                if(cInstance == null) {
                    cInstance = new TreePartFactory();
                }
            }
        }
        return cInstance;
    }
    
    private TreePartFactory()
    {
    }
    
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof InstallOptionsDialog) {
            return new InstallOptionsDialogTreeEditPart(model);
        }
        else {
            return new InstallOptionsTreeEditPart(model);
        }
    }
}
