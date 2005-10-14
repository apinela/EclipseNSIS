/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.handlers;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.*;

public abstract class NSISHandler extends AbstractHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        Widget w = ((Event)event.getTrigger()).widget;
        if(w instanceof Tree) {
            TreeItem[] items = ((Tree)w).getSelection();
            for (int i = 0; i < items.length; i++) {
                Object object = items[i].getData();
                if(object instanceof IFile && ((IFile)object).getFileExtension().equalsIgnoreCase(INSISConstants.NSI_EXTENSION)) {
                    handleScript(((IFile)object).getFullPath());
                }
            }
        }
        return null;
    }
    
    protected abstract void handleScript(IPath path);
}
