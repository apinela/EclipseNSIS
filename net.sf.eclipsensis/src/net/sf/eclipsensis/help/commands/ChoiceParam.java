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

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;

public class ChoiceParam extends ComboParam
{
    public static final String ATTR_DISPLAY = "display"; //$NON-NLS-1$
    public static final String TAG_CHOICE = "choice"; //$NON-NLS-1$
    protected ComboEntry[] mChoices;
    
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
        List list = new ArrayList();
        NodeList childNodes = node.getChildNodes();
        int count = childNodes.getLength();
        for(int i=0; i<count; i++) {
            Node child = childNodes.item(i);
            if(child.getNodeName().equals(TAG_CHOICE)) {
                NamedNodeMap attr = child.getAttributes();
                String value = XMLUtil.getStringValue(attr, ATTR_VALUE);
                String display = XMLUtil.getStringValue(attr, ATTR_DISPLAY);
                list.add(new ComboEntry(value, EclipseNSISPlugin.getResourceString(display==null?value:display)));
            }
        }
        mChoices = (ComboEntry[])list.toArray(new ComboEntry[list.size()]);
    }

    protected ComboEntry[] getComboEntries()
    {
        return mChoices;
    }
}