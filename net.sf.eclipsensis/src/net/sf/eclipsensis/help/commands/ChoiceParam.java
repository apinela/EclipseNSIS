/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;

public class ChoiceParam extends ComboParam
{
    public static final String ATTR_DISPLAY = "display"; //$NON-NLS-1$
    public static final String TAG_CHOICE = "choice"; //$NON-NLS-1$
    protected Map mChoices;
    
    public ChoiceParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        super.init(node);
        loadChoices(node);
    }

    private void loadChoices(Node node)
    {
        mChoices = new LinkedHashMap();
        NodeList childNodes = node.getChildNodes();
        int count = childNodes.getLength();
        for(int i=0; i<count; i++) {
            Node child = childNodes.item(i);
            if(child.getNodeName().equals(TAG_CHOICE)) {
                NamedNodeMap attr = child.getAttributes();
                String value = XMLUtil.getStringValue(attr, ATTR_VALUE);
                String display = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(attr, ATTR_DISPLAY));
                mChoices.put(value, (display==null?value:display));
            }
        }
    }

    protected Map getComboValues()
    {
        return mChoices;
    }
}
