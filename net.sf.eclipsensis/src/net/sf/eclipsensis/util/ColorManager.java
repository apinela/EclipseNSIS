/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;


import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Manager for colors used in the NSIS plugin
 */
public class ColorManager
{
    public static final RGB GREY = new RGB(0xbe,0xbe,0xbe);
    public static final RGB RED = new RGB(0xff,0,0);
    public static final RGB MAGENTA = new RGB(0xff,0,0xff);
    public static final RGB ORANGE = new RGB(0xff,0xa5,0);
    public static final RGB PINK = new RGB(0xff,0xc0,0xcb);
    public static final RGB PURPLE = new RGB(0xa0,0x20,0xf0);
    public static final RGB ORCHID = new RGB(0xda,0x70,0xd6);
    public static final RGB DARK_OLIVE_GREEN = new RGB(0x55,0x6b,0x2f);
    public static final RGB NAVY_BLUE = new RGB(0,0,0x80);
    public static final RGB TURQUOISE = new RGB(0x40,0xe0,0xd0);
    public static final RGB DARK_SEA_GREEN = new RGB(0x8f,0xbc,0x8f);
    public static final RGB WHITE = new RGB(0xff, 0xff, 0xff);
    public static final RGB BLACK = new RGB(0, 0, 0);
    public static final RGB CHOCOLATE = new RGB(0xd2,0x69,0x1e);
    public static final RGB TEAL = new RGB(0x0,0x80,0x80);

    private static ColorRegistry mColorTable = new ColorRegistry();

    /**
     * Return the Color that is stored in the Color table as rgb.
     */
    public static Color getColor(RGB rgb)
    {
        Color color = null;
        if(rgb != null) {
            String rgbName = rgb.toString();
            color = (Color) mColorTable.get(rgbName);
            if (color == null) {
                synchronized(ColorManager.class) {
                    color = (Color) mColorTable.get(rgbName);
                    if (color == null) {
                        mColorTable.put(rgbName, rgb);
                        color = (Color) mColorTable.get(rgbName);
                    }
                }
            }
        }
        return color;
    }

    /**
     * Return the Color that is stored in the Color table as rgb.
     */
    public static Color getNegativeColor(RGB rgb)
    {
        return getColor(new RGB(255 & ~rgb.red, 255 & ~rgb.green, 255 & ~rgb.blue));
    }
    
    /**
     * Return the Color that is stored in the Color table as rgb.
     */
    public static Color getNegativeColor(Color color)
    {
        return getNegativeColor(color.getRGB());
    }
    
    /**
     * @param rgb
     * @return
     */
    public static String rgbToHex(RGB rgb)
    {
        return new StringBuffer(Common.leftPad(Integer.toHexString(rgb.red),2,'0')).append(
                                Common.leftPad(Integer.toHexString(rgb.green),2,'0')).append(
                                Common.leftPad(Integer.toHexString(rgb.blue),2,'0')).toString().toUpperCase();
    }
}