/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.Arrays;

import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableResizer extends ControlAdapter
{
    private double[] mWeights;
    private double[] mCachedWeights;
    private double mTotalWeight;
    
    public TableResizer()
    {
        this(null);
    }

    public TableResizer(double[] weights)
    {
        super();
        mWeights = weights;
        mCachedWeights = null;
        mTotalWeight = 0;
    }
    
    public void controlResized(ControlEvent e) 
    {
        Table table = (Table)e.widget;
        int width = table.getClientArea().width;
        int lineWidth = table.getGridLineWidth();
        TableColumn[] columns = table.getColumns();
        if(!Common.isEmptyArray(columns)) {
            width -= (columns.length-1)*lineWidth;
            
            if(mCachedWeights == null || columns.length != mCachedWeights.length) {
                if(Common.isEmptyArray(mWeights)) {
                    mCachedWeights = new double[columns.length];
                    Arrays.fill(mCachedWeights, 1.0);
                }
                else {
                    if(mWeights.length != columns.length) {
                        mCachedWeights = (double[])Common.resizeArray(mWeights,columns.length);
                        if(columns.length > mWeights.length) {
                            Arrays.fill(mCachedWeights,mWeights.length,columns.length,1.0);
                        }
                    }
                    else {
                        mCachedWeights = mWeights;
                    }
                }
                mTotalWeight = 0;
                for (int i = 0; i < mCachedWeights.length; i++) {
                    mTotalWeight += mCachedWeights[i];
                }
            }
            int sumWidth = 0;
            for(int i=0; i<(columns.length-1); i++) {
                int width2 = (int)((mCachedWeights[i]/mTotalWeight)*width);
                sumWidth += width2;
                columns[i].setWidth(width2);
            }
            columns[columns.length-1].setWidth(width-sumWidth);
            table.redraw();
        }
    }
}
