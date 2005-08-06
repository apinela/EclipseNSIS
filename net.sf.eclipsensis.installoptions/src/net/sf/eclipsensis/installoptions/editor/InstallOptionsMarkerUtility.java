/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class InstallOptionsMarkerUtility
{
    public static Collection getMarkers(IFile file)
    {
        List list = new ArrayList();
        if(file != null) {
            try {
                IMarker[] markers = file.findMarkers(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID, 
                                                     false,IResource.DEPTH_ZERO);
                for (int i = 0; i < markers.length; i++) {
                    list.add(markers[i]);
                }
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    
    public static int getMarkerIntAttribute(IMarker marker, String attribute)
    {
        try {
            Integer i = (Integer)marker.getAttribute(attribute);
            return i.intValue();
        }
        catch(Exception ex) {
            return -1;
        }
    }
}
