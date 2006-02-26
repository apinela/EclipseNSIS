/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.IOUtility;

import org.w3c.dom.Node;

public class LocalSaveFileParam extends LocalFileParam
{
    public LocalSaveFileParam(Node node)
    {
        super(node);
    }

    protected LocalFilesystemObjectParamEditor createLocalFilesystemObjectParamEditor()
    {
        return new LocalSaveFileParamEditor();
    }

    protected class LocalSaveFileParamEditor extends LocalFileParamEditor
    {
        protected boolean isSave()
        {
            return true;
        }

        protected String validateLocalFilesystemObjectParam()
        {
            if(isValid(mFileText)) {
                String file = IOUtility.decodePath(mFileText.getText());
                if(file.length() == 0 && isAllowBlank()) {
                    return null;
                }
                if(IOUtility.isValidPathName(file)) {
                    return null;
                }
                return EclipseNSISPlugin.getResourceString("local.save.file.param.error"); //$NON-NLS-1$
            }
            return null;
        }
    }
}
