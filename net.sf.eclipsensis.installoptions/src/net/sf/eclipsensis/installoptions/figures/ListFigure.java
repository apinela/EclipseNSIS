/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
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
        mSelected.addAll(Arrays.asList(Common.tokenize(state,IInstallOptionsConstants.LIST_SEPARATOR,false)));
    }

    /**
     * @return
     */
    protected Control createSWTControl(Composite parent, int style)
    {
        List list = new List(parent, style);
        java.util.List selected = getSelected();
        java.util.List listItems = getListItems();
        GC gc = new GC(list);
        int maxHeight = list.getItemHeight()*listItems.size();
        int maxWidth = 0;
        for (Iterator iter = listItems.iterator(); iter.hasNext();) {
            String item = (String)iter.next();
            maxWidth = Math.max(maxWidth,gc.stringExtent(item).x);
            list.add(item);
        }
        gc.dispose();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            String item = (String)iter.next();
            int n = -1;
            int m = 0;
            for (Iterator iterator = listItems.iterator(); iterator.hasNext();) {
                if(Common.stringsAreEqual((String)iterator.next(), item, true)) {
                    n = m;
                    break;
                }
                m++;
            }
            if(n >= 0) {
                list.select(n);
            }
        }
        if(isHScroll() || isVScroll()) {
            list.setBounds(-bounds.width-10,-bounds.height-10,bounds.width,bounds.height);
            Rectangle rect = list.getClientArea();
            int borderWidth = list.getBorderWidth();
            boolean hsVisible = (rect.height < (bounds.height - 2*borderWidth));
            boolean vsVisible = (rect.width < (bounds.width - 2*borderWidth));
            boolean showHScrollbar = false;
            boolean showVScrollbar = false;

            ScrollBar hbar = list.getHorizontalBar();
            ScrollBar vbar = list.getVerticalBar();

            if(isHScroll() && !hsVisible) {
                showHScrollbar = true;
                hsVisible = true;
                if(isVScroll() && !vsVisible) {
                    rect.height -= hbar.getSize().y;
                    vsVisible = (rect.width < (bounds.width - 2*borderWidth));
                    if(vsVisible) {
                        rect.width -= vbar.getSize().x;
                    }
                }
                if(rect.width >= maxWidth) {
                    hbar.setEnabled(false);
                }
            }
            if(isVScroll() & !vsVisible) {
                showVScrollbar = true;
                vsVisible = true;
                if(isHScroll() && !hsVisible) {
                    rect.width -= vbar.getSize().x;
                    hsVisible = (rect.height < (bounds.height - 2*borderWidth));
                    if(hsVisible) {
                        rect.height -= hbar.getSize().y;
                    }
                }
                if(rect.height >= maxHeight) {
                    vbar.setEnabled(false);
                }
            }
            if(isHScroll() && showHScrollbar) {
                WinAPI.SendMessage(list.handle, WinAPI.LB_SETHORIZONTALEXTENT, rect.width + 10, 0);
            }
            if(isVScroll() && showVScrollbar) {
                int height=list.getItemHeight();
                int count = bounds.height/height+10;
                for(int i=listItems.size(); i<count; i++) {
                    list.add(""); //$NON-NLS-1$
                }
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
