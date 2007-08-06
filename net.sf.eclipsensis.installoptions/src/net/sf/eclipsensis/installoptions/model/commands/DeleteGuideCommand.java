/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.rulers.*;

import org.eclipse.gef.commands.Command;

public class DeleteGuideCommand extends Command
{

    private InstallOptionsRuler mParent;

    private InstallOptionsGuide mGuide;

    private Map mOldWidgets;

    public DeleteGuideCommand(InstallOptionsGuide guide, InstallOptionsRuler parent)
    {
        super(InstallOptionsPlugin.getResourceString("delete.guide.command.name")); //$NON-NLS-1$
        this.mGuide = guide;
        this.mParent = parent;
    }

    public boolean canUndo()
    {
        return true;
    }

    public void execute()
    {
        mOldWidgets = new HashMap(mGuide.getMap());
        Iterator iter = mOldWidgets.keySet().iterator();
        while (iter.hasNext()) {
            mGuide.detachWidget((InstallOptionsWidget)iter.next());
        }
        mParent.removeGuide(mGuide);
    }

    public void undo()
    {
        mParent.addGuide(mGuide);
        Iterator iter = mOldWidgets.keySet().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
            mGuide.attachWidget(widget, ((Integer)mOldWidgets.get(widget)).intValue());
        }
    }
}
