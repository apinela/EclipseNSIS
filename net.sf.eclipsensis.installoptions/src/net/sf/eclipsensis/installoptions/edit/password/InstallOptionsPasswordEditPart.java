/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.password;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.text.InstallOptionsTextEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.PasswordFigure;

import org.eclipse.swt.widgets.Composite;

public class InstallOptionsPasswordEditPart extends InstallOptionsTextEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "password.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new PasswordFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("password.type.name"); //$NON-NLS-1$
    }
}
