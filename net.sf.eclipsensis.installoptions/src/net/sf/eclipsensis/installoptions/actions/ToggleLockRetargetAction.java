/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.actions.LabelRetargetAction;

public class ToggleLockRetargetAction extends LabelRetargetAction
{
    /**
     * @param part
     */
    public ToggleLockRetargetAction()
    {
        super(ToggleLockAction.ID, ""); //$NON-NLS-1$
        String label = InstallOptionsPlugin.getResourceString("lock.action.name"); //$NON-NLS-1$
        setToolTipText(label);
        setImageDescriptor(ToggleLockAction.LOCK_IMAGE);
        setHoverImageDescriptor(ToggleLockAction.LOCK_IMAGE);
        setDisabledImageDescriptor(ToggleLockAction.LOCK_DISABLED_IMAGE);
    }

    protected void propagateChange(PropertyChangeEvent event)
    {
        if(!event.getProperty().equals(Action.TEXT)) {
            //Weird stuff happens when TEXT propagates
            super.propagateChange(event);
        }
    }
}
