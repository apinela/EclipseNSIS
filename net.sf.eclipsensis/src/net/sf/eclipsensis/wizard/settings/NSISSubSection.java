/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSubSectionDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISSubSection extends AbstractNSISInstallGroup
{
    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.subsection.type"); //$NON-NLS-1$
    private static String cFormat = EclipseNSISPlugin.getResourceString("wizard.subsection.format"); //$NON-NLS-1$
    private static final Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.subsection.icon")); //$NON-NLS-1$

    private String mDescription= ""; //$NON-NLS-1$
    private String mCaption = ""; //$NON-NLS-1$
    private boolean mIsExpanded = false;
    private boolean mIsBold = false;

    static {
        NSISInstallElementFactory.register(TYPE, NSISSubSection.class);
    }
    
    public NSISSubSection()
    {
        super();
        mChildTypes.add(NSISSection.TYPE);
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
        return MessageFormat.format(cFormat, new Object[]{mCaption});
    }

    public boolean edit(Composite composite)
    {
        return new NSISSubSectionDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
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
        return mIsBold;
    }

    /**
     * @param isBold The isBold to set.
     */
    public void setBold(boolean isBold)
    {
        mIsBold = isBold;
    }

    /**
     * @return Returns the isExpanded.
     */
    public boolean isExpanded()
    {
        return mIsExpanded;
    }

    /**
     * @param isExpanded The isExpanded to set.
     */
    public void setExpanded(boolean isExpanded)
    {
        mIsExpanded = isExpanded;
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
}
