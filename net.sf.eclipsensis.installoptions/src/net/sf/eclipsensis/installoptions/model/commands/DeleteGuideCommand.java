/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsRuler;

import org.eclipse.gef.commands.Command;

public class DeleteGuideCommand extends Command
{

    private InstallOptionsRuler mParent;

    private InstallOptionsGuide mGuide;

    private Map mOldParts;

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
        mOldParts = new HashMap(mGuide.getMap());
        Iterator iter = mOldParts.keySet().iterator();
        while (iter.hasNext()) {
            mGuide.detachPart((InstallOptionsWidget)iter.next());
        }
        mParent.removeGuide(mGuide);
    }

    public void undo()
    {
        mParent.addGuide(mGuide);
        Iterator iter = mOldParts.keySet().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget part = (InstallOptionsWidget)iter.next();
            mGuide.attachPart(part, ((Integer)mOldParts.get(part)).intValue());
        }
    }
}
