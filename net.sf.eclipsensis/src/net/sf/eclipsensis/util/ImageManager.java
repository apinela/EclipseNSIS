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

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ImageManager
{
    private static ImageRegistry cImageRegistry = new ImageRegistry();

    public synchronized static ImageDescriptor getImageDescriptor(String location)
    {
        ImageDescriptor imageDescriptor = cImageRegistry.getDescriptor(location);
        if(imageDescriptor == null) {
            imageDescriptor = createImageDescriptor(location);
            cImageRegistry.put(location, imageDescriptor);
        }
        
        return imageDescriptor;
    }

    /**
     * @param location
     * @return
     */
    private static ImageDescriptor createImageDescriptor(String location)
    {
        ImageDescriptor imageDescriptor;
        URL url = EclipseNSISPlugin.getDefault().getBundle().getEntry(location);
        if(url != null) {
            imageDescriptor = ImageDescriptor.createFromURL(url);
        }
        else {
            imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        return imageDescriptor;
    }

    public synchronized static Image getImage(String location) {
        Image image = null;
        if(!Common.isEmpty(location)) {
            image = cImageRegistry.get(location);
            if(image == null) {
                cImageRegistry.put(location,createImageDescriptor(location));
                image = cImageRegistry.get(location);
                if(image == null) {
                    cImageRegistry.remove(location);
                    cImageRegistry.put(location, ImageDescriptor.getMissingImageDescriptor());
                    image = cImageRegistry.get(location);
                }
            }
        }
        return image;
    }
}
