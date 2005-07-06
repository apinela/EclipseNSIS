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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.ui.rulers.RulerEditPart;
import org.eclipse.gef.internal.ui.rulers.RulerFigure;

public class InstallOptionsRulerEditPart extends RulerEditPart
{
    /**
     * @param model
     */
    public InstallOptionsRulerEditPart(Object model)
    {
        super(model);
    }

    protected GraphicalViewer getDiagramViewer()
    {
        return super.getDiagramViewer();
    }
    
    protected IFigure createFigure() 
    {
        RulerFigure ruler =  new InstallOptionsRulerFigure(this, isHorizontal(), getRulerProvider().getUnit());
        ruler.setInterval(100, 10);
        return ruler;
    }
}
