/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.CaseInsensitiveSet;

import org.eclipse.gef.EditPart;

public class InstallOptionsModelTypeDef
{
    private static final Class[] cModelParamTypes = new Class[]{INISection.class};
    private String mType;
    private String mName;
    private String mDescription;
    private String mLargeIcon;
    private String mSmallIcon;
    private String mDisplayProperty;
    private Constructor mModelConstructor;
    private Constructor mEditPartConstructor;
    private Set mFlags = null;
    private Set mSettings = null;

    InstallOptionsModelTypeDef(String type, String name, String description, String smallIcon, String largeIcon, String displayProperty, String model, String part)
    {
        mType = type;
        mName = InstallOptionsPlugin.getResourceString(name);
        mDescription = InstallOptionsPlugin.getResourceString(description);
        mSmallIcon = InstallOptionsPlugin.getResourceString(smallIcon);
        mLargeIcon = InstallOptionsPlugin.getResourceString(largeIcon);
        mDisplayProperty = displayProperty;
        mModelConstructor = createConstructor(model, cModelParamTypes);
        mEditPartConstructor = createConstructor(part, null);
    }

    private Constructor createConstructor(String name, Class[] paramTypes)
    {
        Constructor constructor = null;

        try {
            Class clasz = Class.forName(name);
            constructor = clasz.getDeclaredConstructor(paramTypes);
        }
        catch (Exception e) {
            constructor = null;
            InstallOptionsPlugin.getDefault().log(e);
        }

        return constructor;
    }

    public String getDisplayProperty()
    {
        return mDisplayProperty;
    }

    public String getSmallIcon()
    {
        return mSmallIcon;
    }

    public String getType()
    {
        return mType;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public String getLargeIcon()
    {
        return mLargeIcon;
    }

    public String getName()
    {
        return mName;
    }

    public InstallOptionsElement createModel(INISection section)
    {
        InstallOptionsElement model = null;
        if(mModelConstructor != null) {
            try {
                model = (InstallOptionsElement)mModelConstructor.newInstance(new Object[]{section});
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
                model = null;
            }
        }
        return model;
    }

    public EditPart createEditPart()
    {
        EditPart part = null;
        if(mEditPartConstructor != null) {
            try {
                part = (EditPart)mEditPartConstructor.newInstance(null);
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
                part = null;
            }
        }
        return part;
    }

    public Collection getFlags()
    {
        return (mFlags == null?Collections.EMPTY_SET:Collections.unmodifiableSet(mFlags));
    }

    void setFlags(Collection flags)
    {
        if(mFlags == null) {
            if(flags == null) {
                return;
            }
            mFlags = new CaseInsensitiveSet();
        }
        mFlags.clear();
        if(flags != null) {
            mFlags.addAll(flags);
        }
    }

    public Collection getSettings()
    {
        return (mSettings == null?Collections.EMPTY_SET:Collections.unmodifiableSet(mSettings));
    }

    void setSettings(Collection settings)
    {
        if(mSettings == null) {
            if(settings == null) {
                return;
            }
            mSettings = new CaseInsensitiveSet();
        }
        mSettings.clear();
        if(settings != null) {
            mSettings.addAll(settings);
        }
    }

}