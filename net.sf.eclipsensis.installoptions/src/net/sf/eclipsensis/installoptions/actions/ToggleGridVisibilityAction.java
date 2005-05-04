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

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.Action;

public class ToggleGridVisibilityAction extends Action
{
    private GraphicalViewer mDiagramViewer;

    /**
     * @param diagramViewer
     */
    public ToggleGridVisibilityAction(GraphicalViewer diagramViewer)
    {
        super(GEFMessages.ToggleGrid_Label, AS_CHECK_BOX);
        this.mDiagramViewer = diagramViewer;
        setToolTipText(GEFMessages.ToggleGrid_Tooltip);
        setId(GEFActionConstants.TOGGLE_GRID_VISIBILITY);
        setActionDefinitionId(GEFActionConstants.TOGGLE_GRID_VISIBILITY);
        setChecked(isChecked());
    }

    public boolean isChecked()
    {
        Boolean val = (Boolean)mDiagramViewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    public void run()
    {
        boolean val = !isChecked();
        mDiagramViewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, new Boolean(val));
    }
}
