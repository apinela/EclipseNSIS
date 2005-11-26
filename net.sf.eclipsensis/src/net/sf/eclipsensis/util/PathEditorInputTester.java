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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IPathEditorInput;


public class PathEditorInputTester extends PropertyTester
{
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
        if(receiver instanceof IPathEditorInput && "pathExtensionEquals".equals(property)) {
            IPath path = ((IPathEditorInput)receiver).getPath();
            if(path != null) {
                String extension = path.getFileExtension();
                if(Common.isEmpty(extension)) {
                    if(expectedValue == null) {
                        return true;
                    }
                    else if(expectedValue instanceof String && Common.isEmpty((String)expectedValue)) {
                        return true;
                    }
                }
                else {
                    return (expectedValue instanceof String && extension.equalsIgnoreCase((String)expectedValue));
                }
            }
        }
        return false;
    }
}
