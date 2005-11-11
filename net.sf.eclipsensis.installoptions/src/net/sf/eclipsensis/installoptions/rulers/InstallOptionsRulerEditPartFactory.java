/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.ui.rulers.RulerEditPartFactory;

public class InstallOptionsRulerEditPartFactory extends RulerEditPartFactory
{
    /**
     * @param primaryViewer
     */
    public InstallOptionsRulerEditPartFactory(GraphicalViewer primaryViewer)
    {
        super(primaryViewer);
    }

    protected EditPart createRulerEditPart(EditPart parentEditPart, Object model) {
        return new InstallOptionsRulerEditPart(model);
    }

    protected EditPart createGuideEditPart(EditPart parentEditPart, Object model)
    {
        return new InstallOptionsGuideEditPart(model);
    }
}
