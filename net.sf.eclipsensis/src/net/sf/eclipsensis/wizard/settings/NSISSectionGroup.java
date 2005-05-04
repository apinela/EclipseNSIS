/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSectionGroupDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.*;

public class NSISSectionGroup extends AbstractNSISInstallGroup
{
	private static final long serialVersionUID = 5806218807884563902L;

    public static final String TYPE = NSISKeywords.getKeyword(EclipseNSISPlugin.getResourceString("wizard.sectiongroup.type")); //$NON-NLS-1$
    private static final String FORMAT = EclipseNSISPlugin.getResourceString("wizard.sectiongroup.format"); //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.sectiongroup.icon")); //$NON-NLS-1$

    private String mDescription= ""; //$NON-NLS-1$
    private String mCaption = ""; //$NON-NLS-1$
    private boolean mDefaultExpanded = false;
    private boolean mBold = false;

    static {
        NSISInstallElementFactory.register(TYPE, IMAGE, NSISSectionGroup.class);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes()
     */
    public void setChildTypes()
    {
        clearChildTypes();
        addChildType(NSISSection.TYPE);
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
        return MessageFormat.format(FORMAT, new Object[]{mCaption,NSISKeywords.getKeyword(TYPE)});
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
            value = document.createTextNode((String)value); //$NON-NLS-1$
        }
        return super.createChildNode(document, name, value);
    }
}
