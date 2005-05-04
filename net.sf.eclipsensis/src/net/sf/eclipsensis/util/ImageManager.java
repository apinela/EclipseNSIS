/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageManager
{
    private AbstractUIPlugin mPlugin;
    private ImageRegistry mImageRegistry;

    public ImageManager(AbstractUIPlugin plugin)
    {
        mPlugin = plugin;
        if(mPlugin != null) {
            mImageRegistry = mPlugin.getImageRegistry();
        }
        else {
            Display.getDefault().syncExec(new Runnable() {
                public void run()
                {
                    mImageRegistry = new ImageRegistry();
                }
            });
        }
    }

    public synchronized ImageDescriptor getImageDescriptor(String location)
    {
        return getImageDescriptor(makeLocationURL(location));
    }

    /**
     * @param location
     * @return
     */
    private URL makeLocationURL(String location)
    {
        if(mPlugin == null || Common.isEmpty(location)) {
            return null;
        }
        else {
            return mPlugin.getBundle().getEntry(location);
        }
    }

    public synchronized ImageDescriptor getImageDescriptor(URL url)
    {
        String urlString = (url != null?url.toString():""); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = null;
        if(mImageRegistry != null) {
            imageDescriptor = mImageRegistry.getDescriptor(urlString);
            if(imageDescriptor == null) {
                imageDescriptor = createImageDescriptor(url);
                mImageRegistry.put(urlString.toLowerCase(), imageDescriptor);
            }
        }
        
        return imageDescriptor;
    }

    /**
     * @param location
     * @return
     */
    private  ImageDescriptor createImageDescriptor(URL url)
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

    public synchronized Image getImage(String location) 
    {
        return getImage(makeLocationURL(location));
    }

    public synchronized boolean containsImage(URL url) 
    {
        return (mImageRegistry.get(url.toString().toLowerCase()) != null);
    }

    public synchronized void putImage(URL url, Image image) 
    {
        mImageRegistry.put(url.toString().toLowerCase(),image);
    }

    public synchronized  Image getImage(URL url) 
    {
        Image image = null;
        if(url != null) {
            String urlString = url.toString().toLowerCase();
            image = mImageRegistry.get(urlString);
            if(image == null) {
                mImageRegistry.put(urlString,createImageDescriptor(url));
                image = mImageRegistry.get(urlString);
                if(image == null) {
                    mImageRegistry.remove(urlString);
                    mImageRegistry.put(urlString, ImageDescriptor.getMissingImageDescriptor());
                    image = mImageRegistry.get(urlString);
                }
            }
        }
        return image;
    }
}
