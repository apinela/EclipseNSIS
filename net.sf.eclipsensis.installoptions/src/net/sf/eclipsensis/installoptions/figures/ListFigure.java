/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;

public class ListFigure extends EditableElementFigure implements IListItemsFigure
{
    private java.util.List mListItems;
    private java.util.List mSelected = new ArrayList();
    
    public ListFigure(GraphicalEditPart editPart)
    {
        super(editPart);
    }

    public void setListItems(java.util.List items)
    {
        mListItems = items;
    }

    public void setState(String state)
    {
        super.setState(state);
        mSelected.clear();
        mSelected.addAll(Arrays.asList(Common.tokenize(state,IInstallOptionsConstants.LIST_SEPARATOR)));
    }
    
    /**
     * @return
     */
    protected Control createSWTControl(Composite parent)
    {
        List list = new List(parent, getStyle());
        mSelected.retainAll(mListItems);
        for (Iterator iter = mListItems.iterator(); iter.hasNext();) {
            list.add((String)iter.next());
        }
        for (Iterator iter = mSelected.iterator(); iter.hasNext();) {
            int n = mListItems.indexOf(iter.next());
            if(n >= 0) {
                list.select(n);
            }
        }
        return list;
    }

    /**
     * @return
     */
    public int getDefaultStyle()
    {
        return SWT.BORDER|SWT.MULTI;
    }
}
