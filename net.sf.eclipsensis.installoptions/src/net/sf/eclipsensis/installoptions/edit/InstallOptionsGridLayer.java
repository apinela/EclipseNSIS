/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class InstallOptionsGridLayer extends GridLayer implements IInstallOptionsConstants
{
    private String mStyle = GRID_STYLE_LINES;
    public static final String PROPERTY_GRID_STYLE="net.sf.eclipsensis.installoptions.grid_style"; //$NON-NLS-1$
    private double mDpuX;
    private double mDpuY;

    public InstallOptionsGridLayer()
    {
        super();
        Font f = Display.getDefault().getSystemFont();
        mDpuX = ((double)FigureUtility.dialogUnitsToPixelsX(1000,f))/1000;
        mDpuY = ((double)FigureUtility.dialogUnitsToPixelsY(1000,f))/1000;
    }

    protected void paintGrid(Graphics g)
    {
        int distanceX = (int)(this.gridX*mDpuX);
        int distanceY = (int)(this.gridY*mDpuY);
        Point origin = new Point((int)(this.origin.x*mDpuX),(int)(this.origin.y*mDpuY));
        Rectangle clip = g.getClip(Rectangle.SINGLETON);
        if (distanceX > 0) {
            int n=1;
            int x = origin.x;
            if (origin.x >= clip.x) {
                while (x - distanceX >= clip.x) {
                    x = origin.x - (int)((n++)*gridX*mDpuX);
                }
            }
            else {
                while (x < clip.x) {
                    x = origin.x + (int)((n++)*gridX*mDpuX);
                }
            }
            origin.x = x;
        }
        if(distanceY > 0) {
            int n=1;
            int y = origin.y;
            if (origin.y >= clip.y) {
                while (y - distanceX >= clip.y) {
                    y = origin.y - (int)((n++)*gridY*mDpuY);
                }
            }
            else {
                while (y < clip.y) {
                    y = origin.y + (int)((n++)*gridY*mDpuY);
                }
            }
            origin.y = y;
        }

        g.pushState();
        if(GRID_STYLE_DOTS.equals(mStyle)) {
            g.setForegroundColor(ColorConstants.black);
            if (gridX > 0 && gridY > 0) {
                int n = (int)Math.round(origin.x/(gridX*mDpuX));
                int x = origin.x;
                while(x < clip.x+clip.width) {
                    x = (int)((n++)*gridX*mDpuX);

                    int m = (int)Math.round(origin.y/(gridY*mDpuY));
                    int y = origin.y;
                    while(y < clip.y+clip.height) {
                        y = (int)((m++)*gridY*mDpuY);
                        g.drawPoint(x, y);
                    }
                }
            }

        }
        else {
            g.setForegroundColor(ColorConstants.lightGray);
            if(gridX > 0) {
                int n = (int)Math.round(origin.x/(gridX*mDpuX));
                int x = origin.x;
                while(x < clip.x+clip.width) {
                    x = (int)((n++)*gridX*mDpuX);
                    g.drawLine(x, clip.y, x, clip.y + clip.height);
                }
            }
            if(gridY > 0) {
                int n = (int)Math.round(origin.y/(gridY*mDpuY));
                int y = origin.y;
                while(y < clip.y+clip.height) {
                    y = (int)((n++)*gridY*mDpuY);
                    g.drawLine(clip.x, y, clip.x + clip.width, y);
                }
            }
        }
        g.popState();
        g.restoreState();
    }

    public String getStyle()
    {
        return mStyle;
    }

    public void setStyle(String style)
    {
        if (style == null || !InstallOptionsGridLayer.GRID_STYLE_DOTS.equals(style)) {
            style = GRID_STYLE_LINES;
        }
        if (!style.equals(mStyle)) {
            mStyle = style;
            repaint();
        }
    }
}
