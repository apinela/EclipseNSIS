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

import net.sf.eclipsensis.util.NSISCompileTestUtility;

import org.eclipse.core.runtime.IPath;

public class NSISCompileTestHandler extends NSISHandler
{
    protected void handleScript(IPath script)
    {
        NSISCompileTestUtility.INSTANCE.compile(script, true);
    }
}