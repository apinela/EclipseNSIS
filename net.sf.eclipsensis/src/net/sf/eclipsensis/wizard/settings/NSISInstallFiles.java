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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallFilesDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class NSISInstallFiles extends AbstractNSISInstallGroup implements INSISInstallFileSystemObject
{
	private static final long serialVersionUID = 1293912008528238512L;

    public static final String TYPE = EclipseNSISPlugin.getResourceString("wizard.files.type"); //$NON-NLS-1$
    private static final Image cImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.files.icon")); //$NON-NLS-1$
    public static final String FILEITEM_TYPE = "Files FileItem"; //$NON-NLS-1$
    private static final Image cItemImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("wizard.file.icon")); //$NON-NLS-1$
    
    private String mDestination = NSISKeywords.getKeyword("$INSTDIR"); //$NON-NLS-1$
    private int mOverwriteMode = OVERWRITE_ON;
    public static final char SEPARATOR = '\0'; 

    static {
        NSISInstallElementFactory.register(TYPE, NSISInstallFiles.class);
    }

    public NSISInstallFiles()
    {
        super();
        resetChildTypes();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes()
     */
    public void resetChildTypes()
    {
        mChildTypes.clear();
        mChildTypes.add(FILEITEM_TYPE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildTypes()
     */
    public String[] getChildTypes()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public void removeChild(INSISInstallElement child)
    {
        if(mChildren.size() > 1) {
            super.removeChild(child);
        }
        else {
            throw new UnsupportedOperationException(EclipseNSISPlugin.getResourceString("wizard.fileset.delete.exception")); //$NON-NLS-1$
        }
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
        return TYPE;
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
        return new NSISInstallFilesDialog(composite.getShell(),this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return cImage;
    }

    /**
     * @return Returns the destination.
     */
    public String getDestination()
    {
        return mDestination;
    }

    /**
     * @param destination The destination to set.
     */
    public void setDestination(String destination)
    {
        mDestination = destination;
    }
    
    /**
     * @return Returns the filenames.
     */
    public String getFiles()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(mChildren.size() > 0) {
            Iterator iter = mChildren.iterator();
            buf.append(((FileItem)iter.next()).getName());
            for(; iter.hasNext(); ) {
                buf.append(SEPARATOR).append(((FileItem)iter.next()).getName());
            }
        }
        return buf.toString();
    }

    /**
     * @param filenames The filenames to set.
     */
    public void setFiles(String filenames)
    {
        String[] files = Common.tokenize(filenames,SEPARATOR);
        List newList = Arrays.asList(files);
        for(Iterator iter=mChildren.iterator(); iter.hasNext(); ) {
            FileItem item = (FileItem)iter.next();
            if(!newList.contains(item)) {
                iter.remove();
            }
        }
        for(Iterator iter=newList.iterator(); iter.hasNext(); ) {
            FileItem item = new FileItem((String)iter.next());
            if(!mChildren.contains(item)) {
                addChild(item);
            }
        }
    }

    /**
     * @return Returns the overwriteMode.
     */
    public int getOverwriteMode()
    {
        return mOverwriteMode;
    }

    /**
     * @param overwriteMode The overwriteMode to set.
     */
    public void setOverwriteMode(int overwriteMode)
    {
        mOverwriteMode = overwriteMode;
    }
    
    public class FileItem extends AbstractNSISInstallItem
    {
        private static final long serialVersionUID = 3744853352840436396L;

        private String mName = null;
        
        private FileItem(String name)
        {
            super();
            mName = name;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            if(obj != null && obj instanceof FileItem) {
                return ((FileItem)obj).mName.equals(mName);
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return mName.hashCode();
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

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#edit(org.eclipse.swt.widgets.Composite)
         */
        public boolean edit(Composite composite)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
         */
        public String getDisplayName()
        {
            return getName();
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
         */
        public Image getImage()
        {
            return cItemImage;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
         */
        public String getType()
        {
            return FILEITEM_TYPE;
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
         */
        public boolean isEditable()
        {
            return false;
        }
    }
}
