package net.sf.eclipsensis.editor.text;


import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;


public class NSISSyntaxStyle
{
    public RGB mForeground = null;
    public RGB mBackground = null;
    public boolean mBold;
    public boolean mItalic;

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

    /**
     * 
     */
    public NSISSyntaxStyle()
    {
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer("");
        RGB rgb = mForeground;
        if(rgb != null) {
            buf.append(StringConverter.asString(rgb));
        }
        buf.append("|");
        rgb = mBackground;
        if(rgb != null) {
            buf.append(StringConverter.asString(rgb));
        }
        buf.append("|");
        buf.append(StringConverter.asString(mBold));
        buf.append("|");
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
}