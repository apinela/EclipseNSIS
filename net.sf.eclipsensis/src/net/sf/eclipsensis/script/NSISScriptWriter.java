/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import java.io.PrintWriter;
import java.io.Writer;

public class NSISScriptWriter extends PrintWriter
{
    private int mSpacesPerIndent = 4;
    private int mIndentSize = 0;
    private int mPosition = 0;
    private boolean mIndenting = true;

    /**
      * @param out
      */
    public NSISScriptWriter(Writer out) {
        super(out);
    }

    /**
     * @param out
     * @param autoFlush
     */
    public NSISScriptWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public void setIndentSize(int i)
    {
        mIndentSize = i;
    }

    public int getIndentSize()
    {
        return mIndentSize;
    }

    public void indent()
    {
        mIndentSize++;
    }

    public void unindent()
    {
        mIndentSize--;
        if(mIndentSize < 0) {
            mIndentSize = 0;
        }
    }

    public void print(String s)
    {
        printIndent();
        super.print(s);
        mPosition += (s!=null?s.length():4);
    }

    /**
     * @param s
     */
    private void printIndent() {
        if(mPosition == 0)
        {
            int i = mIndentSize * mSpacesPerIndent;
            if(mIndenting) {
                for(int j = 0; j < i; j++) {
                    super.print(' ');
                }
            }

            mPosition += i;
        }
    }

    public void println()
    {
        super.println();
        mPosition = 0;
    }

    public void println(String s)
    {
        print(s);
        println();
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(boolean)
     */
    public void print(boolean b)
    {
        print(b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(char)
     */
    public void print(char c)
    {
        printIndent();
        super.print(c);
        mPosition++;
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(char[])
     */
    public void print(char[] s)
    {
        printIndent();
        super.print(s);
        mPosition += s.length;
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(double)
     */
    public void print(double d)
    {
        print(String.valueOf(d));
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(float)
     */
    public void print(float f)
    {
        print(String.valueOf(f));
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(int)
     */
    public void print(int i)
    {
        print(String.valueOf(i));
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(long)
     */
    public void print(long l)
    {
        print(String.valueOf(l));
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#print(java.lang.Object)
     */
    public void print(Object obj)
    {
        print(String.valueOf(obj));
    }

    public void printValue(String value)
    {
        if(value != null) {
            boolean quoted = false;
            char[] chars= value.toCharArray();
            if(chars.length == 0) {
                quoted = true;
            }
            else {
                if(chars[0] != '"' && chars[chars.length-1] != '"') {
                    for (int i = 0; i < chars.length; i++) {
                        if(Character.isWhitespace(chars[i])) {
                            quoted = true;
                            break;
                        }
                    }
                }
            }
            print(new StringBuffer((quoted?"\"":"")).append(value).append((quoted?"\"":"")).toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
    }

    /**
     * @return Returns the stopIndenting.
     */
    public boolean isIndenting()
    {
        return mIndenting;
    }

    /**
     * @param stopIndenting The stopIndenting to set.
     */
    public void setIndenting(boolean stopIndenting)
    {
        mIndenting = stopIndenting;
    }
}
