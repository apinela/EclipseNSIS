/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.ini.INISection;

public class InstallOptionsVLine extends InstallOptionsLine
{
    public InstallOptionsVLine(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_VLINE;
    }

    public boolean isHorizontal()
    {
        return false;
    }
}
