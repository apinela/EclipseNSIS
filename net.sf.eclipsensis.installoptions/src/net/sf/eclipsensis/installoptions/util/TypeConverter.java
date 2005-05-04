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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

public abstract class TypeConverter
{
    private TypeConverter()
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
