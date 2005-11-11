/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import java.lang.reflect.Method;
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

public abstract class TypeConverter
{
    public abstract String asString(Object o);
    public abstract Object asType(String s);
    public abstract Object makeCopy(Object o);

    public String asString(Object o, Object defaultValue)
    {
        try {
            return asString(o);
        }
        catch(Exception ex) {
            return asString(defaultValue);
        }
    }

    public Object asType(String s, Object defaultValue)
    {
        try {
            return asType(s);
        }
        catch(Exception ex) {
            return makeCopy(defaultValue);
        }
    }

    public static final TypeConverter POINT_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return Common.flatten(new String[]{Integer.toString(((Point)o).x),Integer.toString(((Point)o).y)},',');
        }

        public Object asType(String s)
        {
            Point p = null;
            int n = s.indexOf(","); //$NON-NLS-1$
            if(n > 0) {
                p = new Point();
                p.x = Integer.parseInt(s.substring(0,n));
                p.y = Integer.parseInt(s.substring(n+1));
            }
            return p;
        }

        public Object makeCopy(Object o)
        {
            return new Point((Point)o);
        }
    };

    public static final TypeConverter BOOLEAN_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return ((Boolean)o).toString();
        }

        public Object asType(String s)
        {
            return (s == null?null:Boolean.valueOf(s));
        }

        public Object makeCopy(Object o)
        {
            return o;
        }
    };

    public static final TypeConverter RGB_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            StringBuffer buf = new StringBuffer("0x"); //$NON-NLS-1$
            RGB rgb = (RGB)o;
            buf.append(ColorManager.rgbToHex(rgb));
            return buf.toString();
        }

        public Object asType(String s)
        {
            if(s != null && s.startsWith("0x") && s.length()==8) { //$NON-NLS-1$
                RGB rgb = ColorManager.hexToRGB(s.substring(2));
                if( (rgb.red >= 0 && rgb.red <= 255) &&
                    (rgb.green >= 0 && rgb.green <= 255) &&
                    (rgb.blue >= 0 && rgb.blue <= 255)) {
                    return rgb;
                 }

            }
            return null;
        }

        public Object makeCopy(Object o)
        {
            RGB rgb = (RGB)o;
            return new RGB(rgb.red,rgb.green,rgb.blue);
        }
    };

    public static final TypeConverter INTEGER_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return ((Integer)o).toString();
        }

        public Object asType(String s)
        {
            return (Common.isEmpty(s)?null:Integer.valueOf(s));
        }

        public Object makeCopy(Object o)
        {
            return o;
        }
    };

    public static final TypeConverter STRING_ARRAY_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return Common.flatten((String[])o,IInstallOptionsConstants.LIST_SEPARATOR);
        }

        public Object asType(String s)
        {
            return Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR);
        }

        public Object makeCopy(Object o)
        {
            return ((String[])o).clone();
        }
    };

    public static final TypeConverter STRING_LIST_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return Common.flatten(((List)o).toArray(Common.EMPTY_STRING_ARRAY),IInstallOptionsConstants.LIST_SEPARATOR);
        }

        public Object asType(String s)
        {
            return new ArrayList(Arrays.asList(Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR)));
        }

        public Object makeCopy(Object o)
        {
            if(o instanceof Cloneable) {
                try {
                    Method method = o.getClass().getMethod("clone",null); //$NON-NLS-1$
                    return method.invoke(o,null);
                }
                catch (Exception e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
            return (new ArrayList((List)o)).clone();
        }
    };

    public static final TypeConverter STRING_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return (String)o;
        }

        public Object asType(String s)
        {
            return s;
        }

        public Object makeCopy(Object o)
        {
            return o;
        }
    };

    public static final TypeConverter INI_STRING_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            if(o != null) {
                char[] chars = ((String)o).toCharArray();
                boolean escaped = false;
                for (int i = 0; i < chars.length; i++) {
                    if(escaped) {
                        switch(chars[i]) {
                            case 'r':
                                buf.append("\r"); //$NON-NLS-1$
                                break;
                            case 'n':
                                buf.append("\n"); //$NON-NLS-1$
                                break;
                            case 't':
                                buf.append("\t"); //$NON-NLS-1$
                                break;
                            case '\\':
                                buf.append("\\"); //$NON-NLS-1$
                                break;
                            default:
                                buf.append("\\").append(chars[i]); //$NON-NLS-1$
                        }
                        escaped = false;
                    }
                    else {
                        if(chars[i] == '\\') {
                            escaped = true;
                        }
                        else {
                            buf.append(chars[i]);
                        }
                    }
                }
                if(escaped) {
                    buf.append("\\"); //$NON-NLS-1$
                }
            }

            return buf.toString();
        }

        public Object asType(String s)
        {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            if(s != null) {
                char[] chars = s.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    switch(chars[i]) {
                        case '\r':
                            buf.append("\\r"); //$NON-NLS-1$
                            break;
                        case '\n':
                            buf.append("\\n"); //$NON-NLS-1$
                            break;
                        case '\t':
                            buf.append("\\t"); //$NON-NLS-1$
                            break;
                        case '\\':
                            buf.append("\\\\"); //$NON-NLS-1$
                            break;
                        default:
                            buf.append(chars[i]);
                    }
                }
            }

            return buf.toString();
        }

        public Object makeCopy(Object o)
        {
            return o;
        }
    };

    public static final TypeConverter DIMENSION_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return Common.flatten(new String[]{Integer.toString(((Dimension)o).width),Integer.toString(((Dimension)o).height)},',');
        }

        public Object asType(String s)
        {
            Dimension d = null;
            int n = s.indexOf(","); //$NON-NLS-1$
            if(n > 0) {
                d = new Dimension();
                d.width = Integer.parseInt(s.substring(0,n));
                d.height = Integer.parseInt(s.substring(n+1));
            }
            return d;
        }

        public Object makeCopy(Object o)
        {
            return new Dimension((Dimension)o);
        }
    };
}
