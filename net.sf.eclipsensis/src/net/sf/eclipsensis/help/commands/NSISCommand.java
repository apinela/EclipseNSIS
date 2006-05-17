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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NSISCommand
{
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String ATTR_CATEGORY = "category"; //$NON-NLS-1$

    private String mName;
    private GroupParam mParam;
    private String mCategory;

    public NSISCommand(Node node)
    {
        NamedNodeMap attributes = node.getAttributes();
        mName = XMLUtil.getStringValue(attributes,ATTR_NAME);
        XMLUtil.removeValue(attributes, ATTR_NAME);
        mParam = loadParam(node);
        mCategory = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(attributes, ATTR_CATEGORY));
    }

    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @param node
     * @return
     */
    private GroupParam loadParam(Node node)
    {
        GroupParam groupParam = new GroupParam(node);
        NSISParam[] mChildParams = groupParam.getChildParams();
        if(mChildParams != null && mChildParams.length == 1 && mChildParams[0] instanceof GroupParam) {
            GroupParam param = (GroupParam)mChildParams[0];
            if(groupParam.isOptional() == param.isOptional() && Common.stringsAreEqual(groupParam.getName(), param.getName())) {
                groupParam = param;
            }
        }
        return groupParam;
    }

    public String getName()
    {
        return mName;
    }

    public INSISParamEditor createEditor()
    {
        return mParam.createEditor(null);
    }
    
    public boolean hasParameters()
    {
        if(mParam != null) {
            return !Common.isEmptyArray(mParam.getChildParams());
        }
        return false;
    }
}