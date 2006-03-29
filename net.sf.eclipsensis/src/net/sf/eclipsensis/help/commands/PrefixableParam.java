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

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class PrefixableParam extends SimpleParam
{
    public static final String ATTR_ALLOW_BLANK = "allowBlank"; //$NON-NLS-1$
    public static final String ATTR_PREFIX = "prefix"; //$NON-NLS-1$
    private String mPrefix;
    private boolean mAllowBlank;

    public PrefixableParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        super.init(node);
        NamedNodeMap attributes = node.getAttributes();
        String prefix = XMLUtil.getStringValue(attributes, ATTR_PREFIX);
        if(!Common.isEmpty(prefix)) {
            setPrefix(prefix);
        }
        mAllowBlank = XMLUtil.getBooleanValue(attributes, ATTR_ALLOW_BLANK,false);
    }

    public boolean isAllowBlank()
    {
        return mAllowBlank;
    }

    public String getPrefix()
    {
        return mPrefix;
    }

    public void setPrefix(String prefix)
    {
        mPrefix = prefix;
    }
    
    protected final SimpleParamEditor createSimpleParamEditor(INSISParamEditor parentEditor)
    {
        return createPrefixableParamEditor(parentEditor);
    }

    protected final String getDefaultValue()
    {
        if(mAllowBlank) {
            return ""; //$NON-NLS-1$
        }
        return getDefaultValue2();
    }

    protected String getDefaultValue2()
    {
        return ""; //$NON-NLS-1$
    }

    protected abstract PrefixableParamEditor createPrefixableParamEditor(INSISParamEditor parentEditor);

    protected abstract class PrefixableParamEditor extends SimpleParamEditor
    {
        public static final String DATA_BUTTON = "BUTTON"; //$NON-NLS-1$

        public PrefixableParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        protected final void appendSimpleParamText(StringBuffer buf)
        {
            String text = getParamText();
            if(text != null) {
                boolean shouldQuote = Common.shouldQuote(text);
                if(!Common.isEmpty(getPrefix())) {
                    text = getPrefix()+text;
                }
                buf.append(" ").append(shouldQuote?Common.quote(text):text); //$NON-NLS-1$
            }
        }
        
        protected abstract String getParamText();
    }
}
