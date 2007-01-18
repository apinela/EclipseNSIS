/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.jface.text.templates.*;

public class NSISTemplateTranslator extends TemplateTranslator implements INSISTemplateConstants
{
    private String mErrorMessage = null;

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateTranslator#getErrorMessage()
     */
    public String getErrorMessage()
    {
        return mErrorMessage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateTranslator#translate(java.lang.String)
     */
    public TemplateBuffer translate(String string) throws TemplateException
    {
        StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$

        int state= TEXT;
        mErrorMessage= null;
        Map map = new CaseInsensitiveMap();

        int n=0;
        int offset = -1;
        for (int i= 0; i != string.length(); i++) {
            char ch= string.charAt(i);

            switch (state) {
            case TEXT:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        state= ESCAPE;
                        break;
                    default:
                        buffer.append(ch);
                        n++;
                        break;
                }
                break;
            case ESCAPE:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        buffer.append(ch);
                        n++;
                        state= TEXT;
                        break;
                    default:
                        if(!Character.isLetter(ch)) {
                            mErrorMessage= EclipseNSISPlugin.getResourceString("template.invalid.variable.character.error"); //$NON-NLS-1$
                            throw new TemplateException(mErrorMessage);
                        }
                        offset = n;
                        state= IDENTIFIER;
                        buffer.append(ch);
                        n++;
                }
                break;
            case IDENTIFIER:
                switch (ch) {
                case IDENTIFIER_BOUNDARY:
                    String name = buffer.substring(offset,n);
                    List list = (List)map.get(name);
                    if(list == null) {
                        list = new ArrayList();
                        map.put(name,list);
                    }
                    list.add(new Integer(offset));
                    state= TEXT;
                    break;
                default:
                    if (!Character.isLetterOrDigit(ch) && ch != '_') {
                        // illegal identifier character
                        mErrorMessage= EclipseNSISPlugin.getResourceString("template.invalid.variable.character.error"); //$NON-NLS-1$
                        throw new TemplateException(mErrorMessage);
                    }
                    buffer.append(ch);
                    n++;
                    break;
                }
                break;
            }
        }

        switch (state) {
            case TEXT:
                break;
            default:
                throw new TemplateException(EclipseNSISPlugin.getResourceString("template.incomplete.variable.error")); //$NON-NLS-1$
        }

        String translatedString= buffer.toString();
        TemplateVariable[] variables= new TemplateVariable[map.size()];
        int i=0;
        for(Iterator iter=map.keySet().iterator(); iter.hasNext(); ) {
            String name = (String)iter.next();
            List list = (List)map.get(name);
            int[] offsets = new int[list.size()];
            for (int j = 0; j < offsets.length; j++) {
                offsets[j] = ((Integer)list.get(j)).intValue();
            }
            variables[i++] = createVariable(name, name, offsets);
        }

        return new TemplateBuffer(translatedString, variables);
    }
}
