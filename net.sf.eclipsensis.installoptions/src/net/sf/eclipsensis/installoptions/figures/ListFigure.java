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
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.views.properties.IPropertySource;

public class ListFigure extends EditableElementFigure implements IListItemsFigure
{
    private java.util.List mListItems;
    private java.util.List mSelected;
    
    public ListFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }
    
    public ListFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }
    
    protected void init(IPropertySource propertySource)
    {
        setListItems((java.util.List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_LISTITEMS));
        super.init(propertySource);
   }

    public java.util.List getSelected()
    {
        return mSelected==null?Collections.EMPTY_LIST:mSelected;
    }

    public java.util.List getListItems()
    {
        return mListItems==null?Collections.EMPTY_LIST:mListItems;
    }
    
    public void setListItems(java.util.List items)
    {
        mListItems = items;
    }

    public void setState(String state)
    {
        super.setState(state);
        if(mSelected == null) {
            mSelected = new ArrayList();
        }
        mSelected.clear();
        mSelected.addAll(Arrays.asList(Common.tokenize(state,IInstallOptionsConstants.LIST_SEPARATOR)));
    }
    
    /**
     * @return
     */
    protected Control createSWTControl(Composite parent, int style)
    {
        List list = new List(parent, style);
        java.util.List selected = getSelected();
        java.util.List listItems = getListItems();
        selected.retainAll(listItems);
        for (Iterator iter = listItems.iterator(); iter.hasNext();) {
            list.add((String)iter.next());
        }
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            int n = listItems.indexOf(iter.next());
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

    protected String getTheme()
    {
        return "LISTVIEW"; //$NON-NLS-1$
    }

    protected int getThemePartId()
    {
        return WinAPI.LVP_LISTITEM;
    }

    protected int getThemeStateId()
    {
        if(mDisabled) {
            return WinAPI.LIS_DISABLED;
        }
        return WinAPI.LIS_NORMAL;
    }

    protected boolean isNeedsTheme()
    {
        return true;
    }
}
