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
    private static ImageRegistry cImageRegistry = EclipseNSISPlugin.getDefault().getImageRegistry();

    public synchronized static ImageDescriptor getImageDescriptor(String location)
    {
        return getImageDescriptor(makeLocationURL(location));
    }

    /**
     * @param location
     * @return
     */
    private static URL makeLocationURL(String location)
    {
        if(Common.isEmpty(location)) {
            return null;
        }
        else {
            return EclipseNSISPlugin.getDefault().getBundle().getEntry(location);
        }
    }

    public synchronized static ImageDescriptor getImageDescriptor(URL url)
    {
        String urlString = (url != null?url.toString():""); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = cImageRegistry.getDescriptor(urlString);
        if(imageDescriptor == null) {
            imageDescriptor = createImageDescriptor(url);
            cImageRegistry.put(urlString, imageDescriptor);
        }
        
        return imageDescriptor;
    }

    /**
     * @param location
     * @return
     */
    private static ImageDescriptor createImageDescriptor(URL url)
    {
        ImageDescriptor imageDescriptor;
        if(url != null) {
            imageDescriptor = ImageDescriptor.createFromURL(url);
        }
        else {
            imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        return imageDescriptor;
    }

    public synchronized static Image getImage(String location) {
        return getImage(makeLocationURL(location));
    }

    public synchronized static Image getImage(URL url) {
        Image image = null;
        if(url != null) {
            String urlString = url.toString();
            image = cImageRegistry.get(urlString);
            if(image == null) {
                cImageRegistry.put(urlString,createImageDescriptor(url));
                image = cImageRegistry.get(urlString);
                if(image == null) {
                    cImageRegistry.remove(urlString);
                    cImageRegistry.put(urlString, ImageDescriptor.getMissingImageDescriptor());
                    image = cImageRegistry.get(urlString);
                }
            }
        }
        return image;
    }
}
