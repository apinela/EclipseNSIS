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
import net.sf.eclipsensis.IEclipseNSISPluginListener;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSectionDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISSection extends AbstractNSISInstallGroup
{
	private static final long serialVersionUID = -971949137266423189L;

    public static String TYPE = null;

    private static Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.section.icon")); //$NON-NLS-1$
    private static String cFormat = EclipseNSISPlugin.getResourceString("wizard.section.format"); //$NON-NLS-1$

    private static INSISKeywordsListener cKeywordsListener  = new INSISKeywordsListener() {

        public void keywordsChanged()
        {
            loadType();
        }
        
    };
    private static IEclipseNSISPluginListener cShutdownListener = new IEclipseNSISPluginListener() {
        public void stopped()
        {
            NSISKeywords.removeKeywordsListener(cKeywordsListener);
        }
    };
    
    private String mDescription = null;
    private String mName = null;
    private boolean mBold = false;
    private boolean mHidden = false;
    private boolean mDefaultUnselected = false;

    static {
        loadType();
        EclipseNSISPlugin.getDefault().addListener(cShutdownListener);
        NSISKeywords.addKeywordsListener(cKeywordsListener);
    }
    
    private static void loadType()
    {
        if(TYPE != null) {
            NSISInstallElementFactory.unregister(TYPE, NSISSection.class);
        }
        TYPE = NSISKeywords.getKeyword(EclipseNSISPlugin.getResourceString("wizard.section.type")); //$NON-NLS-1$
        NSISInstallElementFactory.register(TYPE, NSISSection.class);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes()
     */
    public void setChildTypes()
    {
        mChildTypes.clear();
        mChildTypes.add(NSISInstallFile.TYPE);
        mChildTypes.add(NSISInstallFiles.TYPE);
        mChildTypes.add(NSISInstallDirectory.TYPE);
        mChildTypes.add(NSISInstallShortcut.TYPE);
        mChildTypes.add(NSISInstallRegistryKey.TYPE);
        mChildTypes.add(NSISInstallRegistryValue.TYPE);
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
        return MessageFormat.format(cFormat,new Object[]{mName,TYPE}).trim();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(Composite composite)
    {
        return new NSISSectionDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
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
}
