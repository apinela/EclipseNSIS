/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.net.URL;
import java.util.HashMap;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Sunil.Kamath
 */
public class ImageManager
{
    private static HashMap cImageMap = new HashMap();

    public synchronized static ImageDescriptor getImageDescriptor(String location)
    {
        ImageDescriptor imageDescriptor = (ImageDescriptor)cImageMap.get(location);
        if(imageDescriptor == null) {
            URL url = EclipseNSISPlugin.getDefault().getBundle().getEntry(location);
            if(url != null) {
                imageDescriptor = ImageDescriptor.createFromURL(url);
            }
            cImageMap.put(location, imageDescriptor);
        }
        
        return imageDescriptor;
    }
}
