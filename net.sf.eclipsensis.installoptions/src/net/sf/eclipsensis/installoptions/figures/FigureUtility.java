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

import net.sf.eclipsensis.installoptions.model.Position;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;

public class FigureUtility
{
    private static long div(long dividend, long divisor)
    {
        if (dividend < 0) {
            return Math.round((double)(dividend - divisor + 1) / (double)divisor);
        }
        return Math.round((double)dividend / (double)divisor);
    }
    
    private static int muldiv(int v, int multiplicand, int divisor)
    {
        long l = muldiv((long)v, multiplicand, divisor);
        if (l > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        if (l < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return (int)l;
    }
    
    private static long muldiv(long v, int multiplicand, int divisor)
    {
        if (multiplicand == 0) {
            return 0;
        }
        if (v > (Long.MAX_VALUE / multiplicand - multiplicand))
        {
            v = div(v, divisor);
            if (v > Long.MAX_VALUE / multiplicand)
                return Long.MAX_VALUE;
            return v * multiplicand;
        }
        v *= multiplicand;
        return div(v, divisor);
    }

    public static int dialogUnitsToPixelsX(int dlgUnits, Font f)
    {
        return muldiv(dlgUnits, getFontAverageCharWidth(f), 4);
    }

    public static int dialogUnitsToPixelsY(int dlgUnits, Font f)
    {
        return muldiv(dlgUnits, getFontHeight(f), 8);
    }

    public static int pixelsToDialogUnitsX(int pixUnits, Font f)
    {
        return (int)div((pixUnits * 4), getFontAverageCharWidth(f));
    }

    public static int pixelsToDialogUnitsY(int pixUnits, Font f)
    {
        return (int)div((pixUnits * 8), getFontHeight(f));
    }
    
    public static Point dialogUnitsToPixels(Point p, Font f)
    {
        return new Point(dialogUnitsToPixelsX(p.x,f),
                         dialogUnitsToPixelsY(p.y,f));
    }
    
    public static Point pixelsToDialogUnits(Point p, Font f)
    {
        return new Point(pixelsToDialogUnitsX(p.x,f),
                         pixelsToDialogUnitsY(p.y,f));
    }
    
    public static Dimension dialogUnitsToPixels(Dimension d, Font f)
    {
        return new Dimension(dialogUnitsToPixelsX(d.width,f),
                             dialogUnitsToPixelsY(d.height,f));
    }
    
    public static Dimension pixelsToDialogUnits(Dimension d, Font f)
    {
        return new Dimension(pixelsToDialogUnitsX(d.width,f),
                             pixelsToDialogUnitsY(d.height,f));
    }
    
    public static Rectangle dialogUnitsToPixels(Rectangle r, Font f)
    {
        return new Rectangle(dialogUnitsToPixelsX(r.x,f),
                             dialogUnitsToPixelsY(r.y,f),
                             dialogUnitsToPixelsX(r.width,f),
                             dialogUnitsToPixelsY(r.height,f));
    }
    
    public static Rectangle pixelsToDialogUnits(Rectangle r, Font f)
    {
        return new Rectangle(pixelsToDialogUnitsX(r.x,f),
                             pixelsToDialogUnitsY(r.y,f),
                             pixelsToDialogUnitsX(r.width,f),
                             pixelsToDialogUnitsY(r.height,f));
    }
    
    public static Position dialogUnitsToPixels(Position p, Font f)
    {
        return new Position(dialogUnitsToPixelsX(p.left,f),
                            dialogUnitsToPixelsY(p.top,f),
                            dialogUnitsToPixelsX(p.right,f),
                            dialogUnitsToPixelsY(p.bottom,f));
    }
    
    public static Position pixelsToDialogUnits(Position p, Font f)
    {
        return new Position(pixelsToDialogUnitsX(p.left,f),
                            pixelsToDialogUnitsY(p.top,f),
                            pixelsToDialogUnitsX(p.right,f),
                            pixelsToDialogUnitsY(p.bottom,f));
    }

    private static int getFontHeight(Font f)
    {
        FontMetrics fm = FigureUtilities.getFontMetrics(f);
        return fm.getHeight();
    }
    
    private static int getFontAverageCharWidth(Font f)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        for(int i= (int)'A'; i<=(int)'Z'; i++) {
            buf.append((char)i);
        }
        for(int i= (int)'a'; i<=(int)'z'; i++) {
            buf.append((char)i);
        }
        Dimension dim = FigureUtilities.getStringExtents(buf.toString(),f);
        return (int)div(dim.width,52);
    }
}