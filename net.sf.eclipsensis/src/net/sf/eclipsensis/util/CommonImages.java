/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.swt.graphics.Image;

public class CommonImages
{
    public static final Image ADD_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.icon")); //$NON-NLS-1$
    public static final Image ADD_DISABLED_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.disabled.icon")); //$NON-NLS-1$
    public static final Image ADD_SMALL_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.small.icon")); //$NON-NLS-1$
    public static final Image EDIT_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("edit.icon")); //$NON-NLS-1$
    public static final Image EDIT_DISABLED_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("edit.disabled.icon")); //$NON-NLS-1$
    public static final Image DELETE_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.icon")); //$NON-NLS-1$
    public static final Image DELETE_SMALL_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.small.icon")); //$NON-NLS-1$
    public static final Image DELETE_DISABLED_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.disabled.icon")); //$NON-NLS-1$
    public static final Image EXPANDALL_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("expandall.icon")); //$NON-NLS-1$
    public static final Image EXPANDALL_DISABLED_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("expandall.disabled.icon")); //$NON-NLS-1$
    public static final Image COLLAPSEALL_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("collapseall.icon")); //$NON-NLS-1$
    public static final Image COLLAPSEALL_DISABLED_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("collapseall.disabled.icon")); //$NON-NLS-1$
    public static final Image UP_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon")); //$NON-NLS-1$
    public static final Image DOWN_ICON = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("down.icon")); //$NON-NLS-1$

    private CommonImages()
    {
        
    }
}
