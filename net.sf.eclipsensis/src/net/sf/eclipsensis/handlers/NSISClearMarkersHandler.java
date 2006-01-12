/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.handlers;

import java.util.regex.Pattern;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditorUtilities;

import org.eclipse.core.runtime.IPath;

public class NSISClearMarkersHandler extends NSISHandler
{
    protected void handleScript(IPath path)
    {
        NSISEditorUtilities.clearMarkers(path);
    }

    protected Pattern createExtensionPattern()
    {
        return Pattern.compile(INSISConstants.NSI_WILDCARD_EXTENSION,Pattern.CASE_INSENSITIVE);
    }
}
