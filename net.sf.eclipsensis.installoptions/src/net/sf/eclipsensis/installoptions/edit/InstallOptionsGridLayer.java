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
        try {
            g.pushState();
            Rectangle clip = g.getClip(Rectangle.SINGLETON);
            double clipX = clip.x / mDpuX;
            double clipWidth = clip.width / mDpuX;
            double clipY = clip.y / mDpuY;
            double clipHeight = clip.height / mDpuY;
            double originX = origin.x;
            double originY = origin.y;
            int distanceX = gridX;
            int distanceY = this.gridY;

            if (distanceX > 0) {
                if (originX >= clipX) {
                    while (originX - distanceX >= clipX) {
                        originX -= distanceX;
                    }
                }
                else {
                    while (originX < clipX) {
                        originX += distanceX;
                    }
                }
            }
            if (distanceY > 0) {
                if (originY >= clipY) {
                    while (originY - distanceY >= clipY) {
                        originY -= distanceY;
                    }
                }
                else {
                    while (originY < clipY) {
                        originY += distanceY;
                    }
                }
            }

            if (GRID_STYLE_DOTS.equals(mStyle)) {
                g.setForegroundColor(ColorConstants.black);
                if (distanceY > 0 && distanceY > 0) {
                    for (double i = originY; i < clipY + clipHeight; i += distanceY) {
                        for (double j = originX; j < clipX + clipWidth; j += distanceX) {
                            int x = FigureUtility.dialogUnitsToPixelsX((int)j,Display.getDefault().getSystemFont());//(int)(i * mDpuY);
                            int y = FigureUtility.dialogUnitsToPixelsY((int)i,Display.getDefault().getSystemFont());//(int)(i * mDpuY);
                            g.drawPoint(x,y);
                        }
                    }
                }
            }
            else {
                g.setForegroundColor(ColorConstants.lightGray);
                if (distanceX > 0) {
                    for (double i = originY; i < clipY + clipHeight; i += distanceY) {
                        int y = FigureUtility.dialogUnitsToPixelsY((int)i,Display.getDefault().getSystemFont());//(int)(i * mDpuY);
                        g.drawLine(clip.x, y, clip.x + clip.width, y);
                    }
                }
                if (distanceY > 0) {
                    for (double i = originX; i < clipX + clipWidth; i += distanceX) {
                        int x = FigureUtility.dialogUnitsToPixelsX((int)i,Display.getDefault().getSystemFont());//(int)(i * mDpuY);
                        g.drawLine(x, clip.y, x, clip.y + clip.height);
                    }
                }
            }
        }
        finally {
            g.popState();
            g.restoreState();
        }
    }

    public String getStyle()
    {
        return mStyle;
    }

    public void setStyle(String style)
    {
        if (style == null || !IInstallOptionsConstants.GRID_STYLE_DOTS.equals(style)) {
            style = GRID_STYLE_LINES;
        }
        if (!style.equals(mStyle)) {
            mStyle = style;
            repaint();
        }
    }
}
