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

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.ui.views.properties.IPropertySource;

public class FigureUtility
{
    private static Map cFontSizes = new HashMap();

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
        if (l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (l < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
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
            if (v > Long.MAX_VALUE / multiplicand) {
                return Long.MAX_VALUE;
            }
            return v * multiplicand;
        }
        v *= multiplicand;
        return div(v, divisor);
    }

    public static int dialogUnitsToPixelsX(int dlgUnits, Font f)
    {
        return muldiv(dlgUnits, getFontSize(f).width, 4);
    }

    public static int dialogUnitsToPixelsY(int dlgUnits, Font f)
    {
        return muldiv(dlgUnits, getFontSize(f).height, 8);
    }

    public static int pixelsToDialogUnitsX(int pixUnits, Font f)
    {
        return (int)div((pixUnits * 4), getFontSize(f).width);
    }

    public static int pixelsToDialogUnitsY(int pixUnits, Font f)
    {
        return (int)div((pixUnits * 8), getFontSize(f).height);
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
        for(int i= 'A'; i<='Z'; i++) {
            buf.append((char)i);
        }
        for(int i= 'a'; i<='z'; i++) {
            buf.append((char)i);
        }
        Dimension dim = FigureUtilities.getStringExtents(buf.toString(),f);
        return (int)div(dim.width,52);
    }

    private static synchronized Dimension getFontSize(Font f)
    {
        Dimension size = (Dimension)cFontSizes.get(f);
        if(size == null) {
            size = new Dimension(getFontAverageCharWidth(f),getFontHeight(f));
            cFontSizes.put(f,size);
        }

        return size;
    }

    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    //XXX Remove once the cause (and fix) is known.
    public static abstract class NTFigure extends ScrollBarsFigure
    {
        private boolean mDisabled = false;
        private boolean mHScroll = false;
        private boolean mVScroll = false;
        private ScrollBar mHScrollBar;
        private ScrollBar mVScrollBar;
        private Label mGlassPanel;

        private Rectangle mChildBounds = new Rectangle(0,0,0,0);

        public NTFigure(IPropertySource propertySource)
        {
            super();
            setOpaque(true);
            setLayoutManager(new XYLayout());
            mHScrollBar = new ScrollBar();
            mHScrollBar.setHorizontal(true);
            mHScrollBar.setVisible(false);
            add(mHScrollBar);
            mVScrollBar = new ScrollBar();
            mVScrollBar.setHorizontal(false);
            add(mVScrollBar);
            mGlassPanel = new Label();
            mGlassPanel.setOpaque(false);
            add(mGlassPanel);
            createChildFigures();
            init(propertySource);
        }

        protected void init(IPropertySource propertySource)
        {
            List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
            setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
            setHScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_HSCROLL));
            setVScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_VSCROLL));
            setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
        }

        public void setDisabled(boolean disabled)
        {
            if(mDisabled != disabled) {
                mDisabled = disabled;
                refresh();
            }
        }

        public boolean isDisabled()
        {
            return mDisabled;
        }

        public void setHScroll(boolean hScroll)
        {
            if(mHScroll != hScroll) {
                mHScroll = hScroll;
                refresh();
            }
        }

        public void setVScroll(boolean vScroll)
        {
            if(mVScroll != vScroll) {
                mVScroll = vScroll;
                refresh();
            }
        }

        public boolean isHScroll()
        {
            return mHScroll;
        }

        public boolean isVScroll()
        {
            return mVScroll;
        }

        public void refresh()
        {
            updateBounds(bounds);
            layout();
            revalidate();
        }

        private void updateBounds(Rectangle newBounds)
        {
            Rectangle childBounds = new Rectangle(0,0,newBounds.width,newBounds.height);
            setConstraint(mGlassPanel, childBounds.getCopy());
            int hbarHeight = WinAPI.GetSystemMetrics (WinAPI.SM_CYHSCROLL);
            int vbarWidth = WinAPI.GetSystemMetrics (WinAPI.SM_CXVSCROLL);
            mHScrollBar.setVisible(mHScroll);
            if(mHScroll) {
                setConstraint(mHScrollBar, new Rectangle(0,newBounds.height-hbarHeight,
                                                        newBounds.width-(mVScroll?vbarWidth:0), hbarHeight));
                childBounds.height -= hbarHeight;
            }
            mVScrollBar.setVisible(mVScroll);
            if(mVScroll) {
                setConstraint(mVScrollBar, new Rectangle(newBounds.width-vbarWidth,0,
                                                         vbarWidth, newBounds.height-(mHScroll?hbarHeight:0)));
                childBounds.width -= vbarWidth;
            }
            if(!mChildBounds.equals(childBounds)) {
                setChildConstraints(childBounds);
                mChildBounds = childBounds;
            }
        }

        public void setBounds(Rectangle newBounds)
        {
            if(!bounds.getSize().equals(newBounds.getSize())) {
                updateBounds(newBounds);
            }
            super.setBounds(newBounds);
        }

        protected boolean supportsScrollBars()
        {
            return true;
        }

        protected abstract void setChildConstraints(Rectangle bounds);
        protected abstract void createChildFigures();
    }
}

