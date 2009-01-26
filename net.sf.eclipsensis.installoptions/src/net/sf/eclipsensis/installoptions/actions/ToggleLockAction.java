/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.commands.ToggleLockCommand;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleLockAction extends SelectionAction
{
    public static final String ID = "net.sf.eclipsensis.installoptions.toggle_lock"; //$NON-NLS-1$
    static final ImageDescriptor LOCK_IMAGE = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("lock.icon")); //$NON-NLS-1$
    static final ImageDescriptor LOCK_DISABLED_IMAGE = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("lock.disabled.icon")); //$NON-NLS-1$
    static final ImageDescriptor UNLOCK_IMAGE = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("unlock.icon")); //$NON-NLS-1$
    static final ImageDescriptor UNLOCK_DISABLED_IMAGE = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("unlock.disabled.icon")); //$NON-NLS-1$

    private boolean mShouldLock = true;

    /**
     * @param part
     */
    public ToggleLockAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(false);
        updateLabels();
    }

    /**
     * Initializes this action's text and images.
     */
    protected void init()
    {
        super.init();
        setId(ID);
        setEnabled(false);
    }

    public Command createToggleLockCommand(List objects)
    {
        if (Common.isEmptyCollection(objects)) {
            return null;
        }

        ToggleLockCommand cmd = null;
        Iterator iter = objects.iterator();
        InstallOptionsWidget part = getPart(iter.next());
        if(part != null) {
            List list = new ArrayList();
            boolean shouldLock = !part.isLocked();
            list.add(part);
            while (iter.hasNext()) {
                part = getPart(iter.next());
                if(part != null) {
                    if(shouldLock != part.isLocked()) {
                        list.add(part);
                        continue;
                    }
                }
                return null;
            }
            if(mShouldLock != shouldLock) {
                mShouldLock = shouldLock;
                updateLabels();
            }
            cmd = new ToggleLockCommand((InstallOptionsWidget[])list.toArray(new InstallOptionsWidget[list.size()]),
                    mShouldLock);
        }
        return cmd;
    }

    private void updateLabels()
    {
        String label = InstallOptionsPlugin.getResourceString((mShouldLock?"lock.action.name":"unlock.action.name")); //$NON-NLS-1$ //$NON-NLS-2$
        setToolTipText(label);
        setImageDescriptor(mShouldLock?LOCK_IMAGE:UNLOCK_IMAGE);
        setHoverImageDescriptor(mShouldLock?LOCK_IMAGE:UNLOCK_IMAGE);
        setDisabledImageDescriptor(mShouldLock?LOCK_DISABLED_IMAGE:UNLOCK_DISABLED_IMAGE);
        setText(label);
    }

    private InstallOptionsWidget getPart(Object part)
    {
        if(part instanceof InstallOptionsWidgetEditPart) {
            return (InstallOptionsWidget)((InstallOptionsWidgetEditPart)part).getModel();
        }
        else {
            return null;
        }
    }

    protected boolean calculateEnabled() {
        Command cmd = createToggleLockCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    public void run() {
        execute(createToggleLockCommand(getSelectedObjects()));
        setEnabled(calculateEnabled());
    }
}
