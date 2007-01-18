/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import java.util.regex.Pattern;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.makensis.MakeNSISResults;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class NSISClearMarkersAction extends NSISScriptAction
{
    private IResourceChangeListener mResourceChangeListener = new IResourceChangeListener() {
        public void resourceChanged(IResourceChangeEvent event)
        {
            if(mInput != null) {
                IMarkerDelta[] deltas = event.findMarkerDeltas(INSISConstants.PROBLEM_MARKER_ID, true);
                if(deltas != null) {
                    for (int i = 0; i < deltas.length; i++) {
                        if(mInput.equals(deltas[i].getResource().getFullPath())) {
                            updateActionState();
                            return;
                        }
                    }
                }
            }
        }
    };
    
    public NSISClearMarkersAction()
    {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(mResourceChangeListener);
    }

    protected Pattern createExtensionPattern()
    {
        return Pattern.compile(NSI_WILDCARD_EXTENSION,Pattern.CASE_INSENSITIVE);
    }

    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(mResourceChangeListener);
        super.dispose();
    }

    protected void started(IPath script)
    {
        if(mAction != null && mAction.isEnabled() && mInput != null && 
           script.toString().equalsIgnoreCase(mInput.toString())) {
            mAction.setEnabled(false);
        }
    }

    protected void stopped(IPath script, MakeNSISResults results)
    {
        if(mAction != null) {
            if(mInput != null && script.toString().equalsIgnoreCase(mInput.toString())) {
                if(!results.isCanceled()) {
                    return;
                }
            }
            mAction.setEnabled(isEnabled());
        }
    }

    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            if(mInput != null) {
                return NSISEditorUtilities.hasMarkers(mInput);
            }
        }
        return false;
    }

    public void run(IAction action)
    {
        NSISEditorUtilities.clearMarkers(mInput);
    }
}
