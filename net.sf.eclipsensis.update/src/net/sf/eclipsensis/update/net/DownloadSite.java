/*******************************************************************************
 * Copyright (c) 2005-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import org.eclipse.swt.graphics.Image;

public class DownloadSite
{
    private Image mImage;
    private String mLocation;
    private String mContinent;
    private String mName;

    DownloadSite(Image image, String location, String continent, String name)
    {
        mImage = image;
        mLocation = location;
        mContinent = continent;
        mName = name;
    }

    public String getContinent()
    {
        return mContinent;
    }

    public Image getImage()
    {
        return mImage;
    }

    public String getLocation()
    {
        return mLocation;
    }

    public String getName()
    {
        return mName;
    }
}