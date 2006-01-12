/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.ui.rulers.RulerDragTracker;
import org.eclipse.gef.internal.ui.rulers.RulerEditPart;

public class InstallOptionsRulerDragTracker extends RulerDragTracker
{
    public InstallOptionsRulerDragTracker(RulerEditPart source)
    {
        super(source);
    }

    protected boolean isCreationValid()
    {
        if(getCurrentPosition() < 0) {
            return false;
        }
        GraphicalViewer viewer = (GraphicalViewer)source.getViewer().getProperty(GraphicalViewer.class.toString());
        if(viewer != null && ((InstallOptionsEditDomain)viewer.getEditDomain()).isReadOnly()) {
            return false;
        }
        return super.isCreationValid();
    }
}
