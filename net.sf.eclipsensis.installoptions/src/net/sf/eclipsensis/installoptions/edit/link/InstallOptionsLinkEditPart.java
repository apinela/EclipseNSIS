/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.link;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.LinkFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.graphics.RGB;

public class InstallOptionsLinkEditPart extends InstallOptionsLabelEditPart
{
    protected String getDirectEditLabelProperty()
    {
        return "link.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        return new LinkFigure(this);
    }

    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_TXTCOLOR)) {//$NON-NLS-1$
            LinkFigure figure2 = (LinkFigure)getFigure();
            figure2.setTxtColor((RGB)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsUneditableElementEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsLinkEditManager(part, clasz, locator);
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("link.type.name"); //$NON-NLS-1$
    }
}
