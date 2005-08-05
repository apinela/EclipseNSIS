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
import net.sf.eclipsensis.installoptions.ini.INIFile;
import net.sf.eclipsensis.installoptions.ini.INIProblem;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

public class InstallOptionsMarkerUtility
{
    public static Map[] updateMarkers(final IFile file, final INIFile iniFile)
    {
        return updateMarkers(file, iniFile, false);
    }
    
    public static Map[] updateMarkers(final IFile file, final INIFile iniFile, final boolean persistent)
    {
        final ArrayList list = new ArrayList();
        if(file != null) {
            WorkspaceModifyOperation op = new WorkspaceModifyOperation(file)
            {
                protected void execute(IProgressMonitor monitor)throws CoreException
                {
                    try {
                        deleteMarkers(file);
                        if(iniFile != null) {
                            INIProblem[] problems = iniFile.getProblems();
                            for (int i = 0; i < problems.length; i++) {
                                IMarker marker = file.createMarker(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID);
                                marker.setAttribute(IMarker.SEVERITY, 
                                        problems[i].getType()==INIProblem.TYPE_WARNING?
                                                IMarker.SEVERITY_WARNING:
                                                IMarker.SEVERITY_ERROR);
                                marker.setAttribute(IMarker.MESSAGE, problems[i].getMessage());
                                if(problems[i].getLine() > 0) {
                                    marker.setAttribute(IMarker.LINE_NUMBER, problems[i].getLine());
                                }
                                marker.setAttribute(IDE.EDITOR_ID_ATTR,IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID);
                                list.add(marker.getAttributes());
                            }
                        }
                        if(persistent) {
                            try {
                                file.setPersistentProperty(IInstallOptionsConstants.RESOURCEPROPERTY_BUILD_TIMESTAMP, Long.toString(System.currentTimeMillis()));
                            }
                            catch (CoreException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    finally {
                        monitor.done();
                    }
                }
            };
            try {
                op.run(null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (Map[])list.toArray(new Map[list.size()]);
    }

    public static void updateMarkers(final IFile file, final Map[] attributes)
    {
        if(file != null) {
            WorkspaceModifyOperation op = new WorkspaceModifyOperation(file)
            {
                protected void execute(IProgressMonitor monitor)throws CoreException
                {
                    try {
                        deleteMarkers(file);
                        if(!Common.isEmptyArray(attributes)) {
                            for (int i = 0; i < attributes.length; i++) {
                                IMarker marker = file.createMarker(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID);
                                marker.setAttributes(attributes[i]);
                            }
                        }
                    }
                    catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            };
            try {
                op.run(null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Map[] getMarkerAttributes(IFile file)
    {
        List list = new ArrayList();
        if(file != null) {
            try {
                IMarker[] markers = file.findMarkers(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID,
                                                    false,IResource.DEPTH_ZERO);
                for (int i = 0; i < markers.length; i++) {
                    list.add(markers[i].getAttributes());
                }
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return (Map[])list.toArray(new Map[list.size()]);
    }

    public static void deleteMarkers(IFile file) throws CoreException
    {
        file.deleteMarkers(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
    }
}
