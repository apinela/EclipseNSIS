/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.IPropertySectionCreator;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.PathRequestPropertySectionCreator;

public abstract class InstallOptionsPathRequest extends InstallOptionsEditableElement
{
    protected InstallOptionsPathRequest(INISection section)
    {
        super(section);
    }

    protected int getDefaultMaxLen()
    {
        return 260;
    }

    protected Position getDefaultPosition()
    {
        return new Position(0,0,122,13);
    }

    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new PathRequestPropertySectionCreator(this);
    }
}
