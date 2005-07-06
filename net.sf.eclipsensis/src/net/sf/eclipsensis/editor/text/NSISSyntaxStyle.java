/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;


import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


public class NSISSyntaxStyle implements Cloneable
{
    private RGB mForeground = null;
    private RGB mBackground = null;
    private boolean mBold;
    private boolean mItalic;

    /**
     * @param foreground
     * @param background
     * @param bold
     * @param italic
     */
    public NSISSyntaxStyle(RGB foreground, RGB background, boolean bold,
            boolean italic)
    {
        mForeground = foreground;
        mBackground = background;
        mBold = bold;
        mItalic = italic;
    }

    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return new NSISSyntaxStyle(mForeground,mBackground,mBold,mItalic);
        }
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof NSISSyntaxStyle) {
            NSISSyntaxStyle style = (NSISSyntaxStyle)obj;
            if(mBold == style.mBold && mItalic == style.mItalic) {
                return rgbsAreEqual(mForeground,style.mForeground) &&
                       rgbsAreEqual(mBackground,style.mBackground);
            }
        }
        return false;
    }

    private boolean rgbsAreEqual(RGB rgb1, RGB rgb2) 
    {
        if(rgb1 == null && rgb2 == null) {
            return true;
        }
        else if(rgb1 != null && rgb2 != null) {
            return rgb1.equals(rgb2);
        }
        else {
            return false;
        }
    }
    public int hashCode()
    {
        int hashCode = 0;
        if(mForeground != null) {
            hashCode += (mForeground.hashCode() << 16);
        }
        if(mForeground != null) {
            hashCode += (mForeground.hashCode() << 8);
        }
        hashCode += (mBold?1 << 4:0);
        hashCode += (mItalic?1:0);
        return hashCode;
    }

    private NSISSyntaxStyle()
    {
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        RGB rgb = mForeground;
        if(rgb != null) {
            buf.append(StringConverter.asString(rgb));
        }
        buf.append("|"); //$NON-NLS-1$
        rgb = mBackground;
        if(rgb != null) {
            buf.append(StringConverter.asString(rgb));
        }
        buf.append("|"); //$NON-NLS-1$
        buf.append(StringConverter.asString(mBold));
        buf.append("|"); //$NON-NLS-1$
        buf.append(StringConverter.asString(mItalic));
        
        return buf.toString();
    }

    public static NSISSyntaxStyle parse(String text)
    {
        NSISSyntaxStyle style = new NSISSyntaxStyle();
        String[] tokens = Common.tokenize(text,'|');
        int len = tokens.length;
        if(len > 0) {
            if(!Common.isEmpty(tokens[0])) {
                style.mForeground = StringConverter.asRGB(tokens[0]);
            }
            if(len > 1) {
                if(!Common.isEmpty(tokens[1])) {
                    style.mBackground = StringConverter.asRGB(tokens[1]);
                }
                if(len > 2) {
                    style.mBold = StringConverter.asBoolean(tokens[2]);
                    if(len > 3) {
                        style.mItalic = StringConverter.asBoolean(tokens[3]);
                    }
                }
            }
        }
        return style;
    }
    
    public TextAttribute createTextAttribute()
    {
        int style = (mBold?SWT.BOLD:0) | (mItalic?SWT.ITALIC:0);
        return new TextAttribute(ColorManager.getColor(mForeground),
                                 ColorManager.getColor(mBackground),
                                 style);
    }

    public RGB getBackground()
    {
        return mBackground;
    }

    public void setBackground(RGB background)
    {
        mBackground = background;
    }

    public boolean isBold()
    {
        return mBold;
    }

    public void setBold(boolean bold)
    {
        mBold = bold;
    }

    public RGB getForeground()
    {
        return mForeground;
    }

    public void setForeground(RGB foreground)
    {
        mForeground = foreground;
    }

    public boolean isItalic()
    {
        return mItalic;
    }

    public void setItalic(boolean italic)
    {
        mItalic = italic;
    }
}