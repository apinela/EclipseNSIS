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
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSectionDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.*;

public class NSISSection extends AbstractNSISInstallGroup
{
	private static final long serialVersionUID = -971949137266423189L;

    public static final String TYPE = NSISKeywords.getInstance().getKeyword(EclipseNSISPlugin.getResourceString("wizard.section.type")); //$NON-NLS-1$

    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.section.icon")); //$NON-NLS-1$
    private static final String FORMAT = EclipseNSISPlugin.getResourceString("wizard.section.format"); //$NON-NLS-1$

    private String mDescription = null;
    private String mName = null;
    private boolean mBold = false;
    private boolean mHidden = false;
    private boolean mDefaultUnselected = false;

    static {
        NSISInstallElementFactory.register(TYPE, NSISKeywords.getInstance().getKeyword(TYPE), IMAGE, NSISSection.class);
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
        addChildType(NSISInstallFile.TYPE);
        addChildType(NSISInstallFiles.TYPE);
        addChildType(NSISInstallDirectory.TYPE);
        addChildType(NSISInstallShortcut.TYPE);
        addChildType(NSISInstallRegistryKey.TYPE);
        addChildType(NSISInstallRegistryValue.TYPE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return MessageFormat.format(FORMAT,new Object[]{mName,NSISKeywords.getInstance().getKeyword(TYPE)}).trim();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISSectionDialog(wizard,this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
    }

    /**
     * @return Returns the bold.
     */
    public boolean isBold()
    {
        return mBold;
    }
    /**
     * @param bold The bold to set.
     */
    public void setBold(boolean bold)
    {
        mBold = bold;
    }
    /**
     * @return Returns the defaultUnselected.
     */
    public boolean isDefaultUnselected()
    {
        return mDefaultUnselected;
    }
    /**
     * @param defaultUnselected The defaultUnselected to set.
     */
    public void setDefaultUnselected(boolean defaultUnselected)
    {
        mDefaultUnselected = defaultUnselected;
    }
    /**
     * @return Returns the hidden.
     */
    public boolean isHidden()
    {
        return mHidden;
    }
    /**
     * @param hidden The hidden to set.
     */
    public void setHidden(boolean hidden)
    {
        mHidden = hidden;
    }
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mName = name;
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
}
