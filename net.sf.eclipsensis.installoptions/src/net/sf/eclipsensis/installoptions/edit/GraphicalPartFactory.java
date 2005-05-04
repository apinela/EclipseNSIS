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

import net.sf.eclipsensis.installoptions.edit.button.InstallOptionsButtonEditPart;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsButton;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

public class GraphicalPartFactory implements EditPartFactory
{
    private static GraphicalPartFactory cInstance = null;
    
    public static final GraphicalPartFactory getInstance()
    {
        if(cInstance == null) {
            synchronized(GraphicalPartFactory.class) {
                if(cInstance == null) {
                    cInstance = new GraphicalPartFactory();
                }
            }
        }
        return cInstance;
    }
    private GraphicalPartFactory()
    {
    }

    public EditPart createEditPart(EditPart context, Object model)
    {
        EditPart child = null;

        /*
         * TODO Put my own code for creating edit parts
         * 
         * if (model instanceof LogicFlowContainer) child = new
         * LogicFlowContainerEditPart(); else if (model instanceof Wire) child =
         * new WireEditPart(); else if (model instanceof LED) child = new
         * LEDEditPart(); else if (model instanceof LogicLabel) child = new
         * LogicLabelEditPart(); else if (model instanceof Circuit) child = new
         * CircuitEditPart(); else if (model instanceof Gate) child = new
         * GateEditPart(); else if (model instanceof SimpleOutput) child = new
         * OutputEditPart(); //Note that subclasses of LogicDiagram have already
         * been matched above, like Circuit else 
         */
        if(model instanceof InstallOptionsButton) {
            child = new InstallOptionsButtonEditPart();
        }
        else if (model instanceof InstallOptionsDialog) {
            child = new InstallOptionsDialogEditPart();
        }
        child.setModel(model);
        return child;
    }

}
