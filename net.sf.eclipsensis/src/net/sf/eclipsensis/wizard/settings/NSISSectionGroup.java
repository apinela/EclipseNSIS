/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSectionGroupDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.*;

public class NSISSectionGroup extends AbstractNSISInstallGroup
{
	private static final long serialVersionUID = 5806218807884563902L;

    public static final String TYPE = "SubSection"; //$NON-NLS-1$
    private static final String FORMAT = EclipseNSISPlugin.getResourceString("wizard.sectiongroup.format"); //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.sectiongroup.icon")); //$NON-NLS-1$

    private String mDescription= ""; //$NON-NLS-1$
    private String mCaption = ""; //$NON-NLS-1$
    private boolean mDefaultExpanded = false;
    private boolean mBold = false;

    static {
        NSISInstallElementFactory.register(TYPE, NSISKeywords.getInstance().getKeyword(TYPE), IMAGE, NSISSectionGroup.class);
        NSISKeywords.getInstance().addKeywordsListener(new INSISKeywordsListener() {
            public void keywordsChanged()
            {
                NSISInstallElementFactory.setTypeName(TYPE, NSISKeywords.getInstance().getKeyword(TYPE));
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes()
     */
    public void setChildTypes()
    {
        clearChildTypes();
        addChildType(NSISSection.TYPE);
        addChildType(NSISSectionGroup.TYPE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return MessageFormat.format(FORMAT, new Object[]{mCaption,NSISKeywords.getInstance().getKeyword(TYPE)});
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISSectionGroupDialog(wizard,this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
    }

    /**
     * @return Returns the caption.
     */
    public String getCaption()
    {
        return mCaption;
    }

    /**
     * @param caption The caption to set.
     */
    public void setCaption(String caption)
    {
        mCaption = caption;
    }

    /**
     * @return Returns the isBold.
     */
    public boolean isBold()
    {
        return mBold;
    }

    /**
     * @param bold The isBold to set.
     */
    public void setBold(boolean bold)
    {
        mBold = bold;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        mDescription = description;
    }

    public boolean isDefaultExpanded()
    {
        return mDefaultExpanded;
    }

    public void setDefaultExpanded(boolean defaultExpanded)
    {
        mDefaultExpanded = defaultExpanded;
    }

    protected Object getNodeValue(Node node, String name, Class clasz)
    {
        if(name.equals("description")) { //$NON-NLS-1$
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            NodeList nodeList = node.getChildNodes();
            int n = nodeList.getLength();
            for(int i=0; i < n; i++) {
                Node child = nodeList.item(i);
                if(child instanceof Text) {
                    buf.append(((Text)child).getNodeValue());
                }
            }
            return buf.toString();
        }
        else {
            return super.getNodeValue(node, name, clasz);
        }
    }

    protected Node createChildNode(Document document, String name, Object value)
    {
        if(name.equals("description")) { //$NON-NLS-1$
            value = document.createTextNode((String)value);
        }
        return super.createChildNode(document, name, value);
    }

    public String validate(boolean recursive)
    {
        if(Common.isEmpty(getCaption())) {
            return EclipseNSISPlugin.getResourceString("wizard.missing.sectiongroup.caption.error"); //$NON-NLS-1$
        }
        else {
            return super.validate(recursive);
        }
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + (mBold?1231:1237);
        result = PRIME * result + ((mCaption == null)?0:mCaption.hashCode());
        result = PRIME * result + (mDefaultExpanded?1231:1237);
        result = PRIME * result + ((mDescription == null)?0:mDescription.hashCode());
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NSISSectionGroup other = (NSISSectionGroup)obj;
        if (mBold != other.mBold)
            return false;
        if (mCaption == null) {
            if (other.mCaption != null)
                return false;
        }
        else if (!mCaption.equals(other.mCaption))
            return false;
        if (mDefaultExpanded != other.mDefaultExpanded)
            return false;
        if (mDescription == null) {
            if (other.mDescription != null)
                return false;
        }
        else if (!mDescription.equals(other.mDescription))
            return false;
        return true;
    }
}
