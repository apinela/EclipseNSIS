/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import java.io.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

public class NSISWizardTemplate implements Serializable
{
	private static final long serialVersionUID = 5904505162934330711L;

    private static final String PREFIX = "template"; //$NON-NLS-1$
    private static final String SUFFIX = ".ser"; //$NON-NLS-1$
    
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_CUSTOM = 1;
    public static final int TYPE_USER = 2;
    
    private String mName = null;
    private String mDescription = null;
    private String mFileName = null;
    private boolean mEnabled = true;
    private boolean mDeleted = false;
    private transient int mType = TYPE_DEFAULT;
    
    private transient byte[] mSettings = null;
    private transient boolean mSettingsChanged = false;

    /**
     * @param name
     * @param description
     */
    public NSISWizardTemplate(String name, String description)
    {
        this();
        mName = name;
        mDescription = (description==null?"":description); //$NON-NLS-1$
        mFileName = Common.generateUniqueName(PREFIX,SUFFIX);
        mType = TYPE_USER;
    }
    
    public NSISWizardTemplate(NSISWizardTemplate template)
    {
        mName = template.mName;
        mDescription = template.mDescription;
        mEnabled = template.mEnabled;
        mDeleted = template.mDeleted;
        mType = template.mType;
        mFileName = template.mFileName;
        mSettings = (template.mSettings==null?null:(byte[])template.mSettings.clone());
        mSettingsChanged = false;
    }
    
    NSISWizardTemplate()
    {
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String name)
    {
        this(name,""); //$NON-NLS-1$
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return mDescription;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @return Returns the available.
     */
    public boolean isEnabled()
    {
        return mEnabled;
    }

    /**
     * @param enabled The available to set.
     */
    public void setEnabled(boolean enabled)
    {
        mEnabled = enabled;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        mDescription = description;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mName = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getName();
    }
    /**
     * @return Returns the settings.
     */
    public NSISWizardSettings getSettings()
    {
        if(mSettings == null) {
            synchronized(this) {
                if(mSettings == null) {
                    loadSettings();
                }
            }
        }
        try {
            return (mSettings==null?new NSISWizardSettings():(NSISWizardSettings)Common.readObject(new ByteArrayInputStream(mSettings)));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * @param settings The settings to set.
     */
    public void setSettings(NSISWizardSettings settings)
    {
        if(settings == null) {
            mSettings = null;
        }
        else {
            BufferedOutputStream bos = null;
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                bos = new BufferedOutputStream(baos);
                Common.writeObject(bos,settings);
                mSettings = baos.toByteArray(); 
            }
            catch (IOException e) {
                e.printStackTrace();
                mSettings = null;
            }
            finally {
                if(baos != null) {
                    try {
                        baos.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if(bos != null) {
                    try {
                        bos.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        mSettingsChanged = true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if(obj instanceof NSISWizardTemplate) {
            return mFileName.equals(((NSISWizardTemplate)obj).mFileName);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return mFileName.hashCode();
    }
    
    boolean isLoaded()
    {
        return (mSettings != null);
    }
    
    void loadSettings()
    {
        File file = new File(NSISWizardTemplateManager.getLocation(this),mFileName);
        if(file.exists()) {
            InputStream is = null;
            try {
                byte[] bytes = new byte[(int)file.length()];

                is = new BufferedInputStream(new FileInputStream(file));
                // Read in the bytes
                int offset = 0;
                int numRead = 0;
                while (offset < bytes.length
                       && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                    offset += numRead;
                }

                if (offset < bytes.length) {
                    throw new IOException(EclipseNSISPlugin.getFormattedString("wizard.template.read.settings.error", //$NON-NLS-1$
                                                              new Object[]{file.getAbsolutePath()}));
                }

                // Close the input stream and return bytes
                mSettings = bytes;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
    
    void deleteSettings()
    {
        File file = new File(NSISWizardTemplateManager.getLocation(this),mFileName);
        if(file.exists()) {
            file.delete();
        }
        mSettings = null;
    }
    
    void saveSettings() throws IOException
    {
        if(mSettingsChanged) {
            try {
                if(mSettings == null) {
                    deleteSettings();
                }
                else {
                    File file = new File(NSISWizardTemplateManager.getLocation(this),mFileName);
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(file));
                        os.write(mSettings);
                    }
                    finally {
                        if(os != null) {
                            os.close();
                        }
                    }
                }
                mSettingsChanged = false;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @return Returns the deleted.
     */
    public boolean isDeleted()
    {
        return mDeleted;
    }
    /**
     * @param deleted The deleted to set.
     */
    void setDeleted(boolean deleted)
    {
        mDeleted = deleted;
    }
    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return mType;
    }
    
    /**
     * @param type The type to set.
     */
    void setType(int type)
    {
        mType = type;
    }
    
    /**
     * @return Returns the fileName.
     */
    public String getFileName()
    {
        return mFileName;
    }
    
    /**
     * @param fileName The fileName to set.
     */
    void setFileName(String fileName)
    {
        mFileName = fileName;
    }
    
    /**
     * @param settingsChanged The settingsChanged to set.
     */
    void setSettingsChanged(boolean settingsChanged)
    {
        mSettingsChanged = settingsChanged;
    }
    
    boolean isDifferentFrom(NSISWizardTemplate template)
    {
        return !mName.equals(template.mName) ||
               !mDescription.equals(template.mDescription) ||
               !mFileName.equals(template.mFileName) ||
               mEnabled != template.mEnabled || 
               mSettingsChanged != template.mSettingsChanged;
    }
}
