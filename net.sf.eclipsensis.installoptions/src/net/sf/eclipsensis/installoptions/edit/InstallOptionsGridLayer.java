/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;

public class InstallOptionsGridLayer extends GridLayer implements IInstallOptionsConstants
{
    private String mStyle = GRID_STYLE_LINES;
    public static final String PROPERTY_GRID_STYLE="net.sf.eclipsensis.installoptions.grid_style"; //$NON-NLS-1$

    protected void paintGrid(Graphics g)
    {
        if(GRID_STYLE_DOTS.equals(mStyle)) {
            setForegroundColor(ColorConstants.black);
            Rectangle clip = g.getClip(Rectangle.SINGLETON);
            
            if (gridX > 0 && gridY > 0) {
                if (origin.x >= clip.x) {
                    while (origin.x - gridX >= clip.x) {
                        origin.x -= gridX;
                    }
                }
                else {
                    while (origin.x < clip.x) {
                        origin.x += gridX;
                    }
                }
                if (origin.y >= clip.y) {
                    while (origin.y - gridY >= clip.y) {
                        origin.y -= gridY;
                    }
                }
                else {
                    while (origin.y < clip.y) {
                        origin.y += gridY;
                    }
                }
                for (int i = origin.x; i < clip.x + clip.width; i += gridX) {
                    for (int j = origin.y; j < clip.y + clip.height; j += gridY) {
                        g.drawPoint(i, j);
                    }
                }
            }
            
        }
        else {
            setForegroundColor(ColorConstants.lightGray);
            super.paintGrid(g);
        }
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
