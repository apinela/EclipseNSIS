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

import org.eclipse.jface.viewers.TableViewer;

public abstract class TableViewerUpDownMover extends StructuredViewerUpDownMover
{
    private TableViewer mTableViewer;
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#setInput(java.lang.Object)
     */
    public void setInput(Object input)
    {
        mTableViewer = (TableViewer)input;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#getInput()
     */
    public Object getInput()
    {
        return mTableViewer;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#getSelectedIndices()
     */
    protected int[] getSelectedIndices()
    {
        return mTableViewer.getTable().getSelectionIndices();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#getSize()
     */
    protected int getSize()
    {
        return mTableViewer.getTable().getItemCount();
    }
}
