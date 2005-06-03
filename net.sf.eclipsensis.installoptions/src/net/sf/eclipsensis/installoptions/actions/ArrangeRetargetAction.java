/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.LabelRetargetAction;

public class ArrangeRetargetAction extends LabelRetargetAction
{
    public ArrangeRetargetAction(int type)
    {
        super(null,null);
        String prefix;
        switch(type) {
            case IInstallOptionsConstants.SEND_BACKWARD:
                prefix = "send.backward"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.SEND_TO_BACK:
                prefix = "send.to.back"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.BRING_FORWARD:
                prefix = "bring.forward"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.BRING_TO_FRONT:
            default:
                prefix = "bring.to.front"; //$NON-NLS-1$
                break;
        }
        
        setId(prefix);
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".tooltip")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
    }
}
