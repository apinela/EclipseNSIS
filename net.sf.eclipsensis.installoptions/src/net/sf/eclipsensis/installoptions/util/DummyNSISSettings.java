/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import net.sf.eclipsensis.settings.NSISSettings;

public class DummyNSISSettings extends NSISSettings
{
    public boolean showStatistics()
    {
        return false;
    }

    public boolean getBoolean(String name)
    {
        return false;
    }

    public int getInt(String name)
    {
        return 0;
    }

    public String getString(String name)
    {
        return ""; //$NON-NLS-1$
    }

    public Object loadObject(String name)
    {
        return null;
    }

    public void removeBoolean(String name)
    {
    }

    public void removeInt(String name)
    {
    }

    public void removeString(String name)
    {
    }

    public void removeObject(String name)
    {
    }

    public void setValue(String name, boolean value)
    {
    }

    public void setValue(String name, int value)
    {
    }

    public void setValue(String name, String value)
    {
    }

    public void storeObject(String name, Object object)
    {
    }

    public String getName()
    {
        return null;
    }
}
