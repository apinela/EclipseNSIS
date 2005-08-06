/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.annotation;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

public class InstallOptionsAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory
{
    public IAnnotationModel createAnnotationModel(IPath location)
    {
        IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
        try {
            String editorId = file.getPersistentProperty(IDE.EDITOR_KEY);
            if(IInstallOptionsConstants.INSTALLOPTIONS_DESIGN_EDITOR_ID.equals(editorId) ||
               IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID.equals(editorId)) {
                return new AnnotationModel();
            }
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
        return super.createAnnotationModel(location);
    }
}
