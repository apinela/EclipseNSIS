/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.pathrequest;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

public class InstallOptionsDirRequestEditPart extends InstallOptionsPathRequestEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "dirrequest.direct.edit.label"; //$NON-NLS-1$
    }

    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("dirrequest.type.name"); //$NON-NLS-1$
    }
}