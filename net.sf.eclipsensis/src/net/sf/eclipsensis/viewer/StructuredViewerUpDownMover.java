/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.viewer;

import java.util.List;

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.UpDownMover;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

public abstract class StructuredViewerUpDownMover extends UpDownMover
{
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#updateElements(java.util.List, java.util.List, boolean)
     */
    protected final void updateElements(List elements, List move, boolean isDown)
    {
        StructuredViewer viewer = (StructuredViewer)getInput();
        updateStructuredViewerInput(viewer.getInput(), elements);
        viewer.refresh();
        if(!Common.isEmptyCollection(move)) {
            viewer.setSelection(new StructuredSelection(move));
            viewer.reveal(move.get(isDown?move.size()-1:0));
        }
    }

    protected abstract void updateStructuredViewerInput(Object input, List elements);
}