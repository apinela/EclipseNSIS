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
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

public abstract class TypeConverter
{
    public TypeConverter()
    {
    }
    
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
            return TypeConverter.asString(((Point)o).x,((Point)o).y);
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
            return (Boolean)o;
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
            return (Integer)o;
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
            return (String[])((String[])o).clone();
        }
    };
    
    
    public static final TypeConverter STRING_LIST_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return Common.flatten((String[])((List)o).toArray(Common.EMPTY_STRING_ARRAY),IInstallOptionsConstants.LIST_SEPARATOR);
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
                    e.printStackTrace();
                }
            }
            return (List)(new ArrayList((List)o)).clone();
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
            return (String)o;
        }
    };
    
    public static final TypeConverter DIMENSION_CONVERTER = new TypeConverter() {
        public String asString(Object o)
        {
            return TypeConverter.asString(((Dimension)o).width,((Dimension)o).height);
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
    
    private static final String asString(int x, int y)
    {
        return new StringBuffer("").append(x).append(",").append(y).toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
