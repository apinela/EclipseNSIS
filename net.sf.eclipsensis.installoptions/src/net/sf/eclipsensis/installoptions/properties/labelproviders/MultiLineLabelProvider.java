/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.labelproviders;

import org.eclipse.jface.viewers.LabelProvider;

public class MultiLineLabelProvider extends LabelProvider
{
    public static final MultiLineLabelProvider INSTANCE;
    
    static {
        INSTANCE = new MultiLineLabelProvider();
    }

    private MultiLineLabelProvider()
    {
        super();
    }

    public String getText(Object element) 
    {
        if(element instanceof String) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            char[] chars = ((String)element).toCharArray();
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
            
            return buf.toString();
        }
        else {
            return super.getText(element);
        }
    }
}