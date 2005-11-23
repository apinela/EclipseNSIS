/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.*;

public class NSISInstallElementLabelProvider extends LabelProvider
{
    private static ImageData cErrorImageData = EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("error.decoration.icon")).getImageData(); //$NON-NLS-1$

    public NSISInstallElementLabelProvider()
    {
        super();
    }

    public NSISInstallElementLabelProvider(boolean withErrors)
    {
        this();
    }

    public Image getImage(Object element) {
        if(element instanceof INSISInstallElement) {
            Image image = ((INSISInstallElement)element).getImage();
            if(((INSISInstallElement)element).validate(false) != null) {
                image = decorateImage(image, (INSISInstallElement)element);
            }
            return image;
        }
        else {
            return super.getImage(element);
        }
    }

    public String getText(Object element) {
        if(element instanceof INSISInstallElement) {
            return ((INSISInstallElement)element).getDisplayName();
        }
        else {
            return super.getText(element);
        }
    }

    public boolean isLabelProperty(Object element, String property) {
        if(element instanceof INSISInstallElement) {
            return false;
        }
        else {
            return super.isLabelProperty(element, property);
        }
    }

    private Image decorateImage(final Image image, INSISInstallElement element)
    {
        String name = Integer.toString(image.hashCode())+"$error"; //$NON-NLS-1$
        Image image2 = EclipseNSISPlugin.getImageManager().getImage(name);
        if(image2 == null) {
            EclipseNSISPlugin.getImageManager().putImageDescriptor(name,
                    new CompositeImageDescriptor(){
                        protected void drawCompositeImage(int width, int height)
                        {
                            drawImage(image.getImageData(),0,0);
                            drawImage(cErrorImageData,0,getSize().y-cErrorImageData.height);
                        }

                        protected Point getSize()
                        {
                            return new Point(image.getBounds().width,image.getBounds().height);
                        }
                    });
            image2 = EclipseNSISPlugin.getImageManager().getImage(name);
        }
        return image2;
    }
}
