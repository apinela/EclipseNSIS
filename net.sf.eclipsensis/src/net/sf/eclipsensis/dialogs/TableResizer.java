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
import org.eclipse.swt.widgets.*;

public class TableResizer extends ControlAdapter
{
    private double[] mWeights;
    
    public TableResizer()
    {
        this(null);
    }

    public TableResizer(double[] weights)
    {
        super();
        mWeights = weights;
    }
    
    public void controlResized(ControlEvent e) 
    {
        Table table = (Table)e.widget;
        int width = table.getClientArea().width;
        int lineWidth = table.getGridLineWidth();
        TableColumn[] columns = table.getColumns();
        width -= (columns.length-1)*lineWidth;
        
        double[] weights;
        if(Common.isEmptyArray(mWeights)) {
            weights = new double[columns.length];
            Arrays.fill(weights, 1.0);
        }
        else {
            if(mWeights.length != columns.length) {
                weights = (double[])Common.resizeArray(mWeights,columns.length);
                if(columns.length > mWeights.length) {
                    Arrays.fill(weights,mWeights.length,columns.length,1.0);
                }
            }
            else {
                weights = mWeights;
            }
        }
        double sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
        }
        int sumWidth = 0;
        for(int i=0; i<(columns.length-1); i++) {
            int width2 = (int)((weights[i]/sum)*width);
            sumWidth += width2;
            columns[i].setWidth(width2);
        }
        columns[columns.length-1].setWidth(width-sumWidth);
    }
}
