/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.template.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.*;

public class NSISWizardTemplate extends AbstractTemplate
{
    private static final long serialVersionUID = 5904505162934330711L;

    private NSISWizardSettings mSettings = null;

    NSISWizardTemplate()
    {
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String name)
    {
        this(null, name);
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String id, String name)
    {
        super(id, name);
    }

    /**
     * @param name
     * @param description
     */
    public NSISWizardTemplate(String id, String name, String description)
    {
        super(id, name, description);
    }

    public Object clone()
    {
        NSISWizardTemplate template = (NSISWizardTemplate)super.clone();
        try {
            template.mSettings = (mSettings==null?null:(NSISWizardSettings)mSettings.clone());
        }
        catch (CloneNotSupportedException e) {
            EclipseNSISPlugin.getDefault().log(e);
            template.mSettings = new NSISWizardSettings();
        }
        return template;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        afterImport();
    }

    /**
     *
     */
    protected void afterImport() throws InvalidTemplateException
    {
        if(mSettings != null) {
            ArrayList languages = mSettings.getLanguages();
            if (!Common.isEmptyCollection(languages)) {
                for (ListIterator iter = languages.listIterator(); iter.hasNext();) {
                    NSISLanguage lang = (NSISLanguage)iter.next();
                    NSISLanguage lang2 = NSISLanguageManager.getInstance().getLanguage(lang.getName());
                    if (lang2 != null) {
                        iter.set(lang2);
                    }
                    else {
                        iter.remove();
                    }
                }
            }
            AbstractNSISInstallGroup installer = (AbstractNSISInstallGroup)mSettings.getInstaller();
            if (installer != null) {
                installer.setExpanded(true, true);
                installer.resetChildTypes(true);
                installer.resetChildren(true);
            }
        }
        else {
            throw new InvalidTemplateException();
        }
    }

    /**
     * @return Returns the settings.
     */
    public synchronized NSISWizardSettings getSettings()
    {
        if(mSettings == null) {
            mSettings = new NSISWizardSettings();
        }
        return mSettings;
    }

    /**
     * @param settings The settings to set.
     */
    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
    }

}
