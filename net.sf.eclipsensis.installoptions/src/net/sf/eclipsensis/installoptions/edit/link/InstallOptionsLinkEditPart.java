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
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.LinkFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsLink;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySource;

public class InstallOptionsLinkEditPart extends InstallOptionsLabelEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsLink model = (InstallOptionsLink)getModel();
            ColorDialog dialog = new ColorDialog(getViewer().getControl().getShell());
            RGB value = (RGB)model.getTxtColor();
            if (value != null) {
                dialog.setRGB(value);
            }
            else {
                dialog.setRGB(InstallOptionsLink.DEFAULT_TXTCOLOR);
            }
            if (dialog.open() != null) {
                mNewValue = dialog.getRGB();
                return true;
            }
            else {
                return false;
            }
        }

        public Object getNewValue()
        {
            return mNewValue;
        }
        
    };

    public Object getAdapter(Class key)
    {
        if(IExtendedEditSupport.class.equals(key)) {
            return mExtendedEditSupport;
        }
        return super.getAdapter(key);
    }
    
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsLinkExtendedEditPolicy(this));
    }
    
    protected String getExtendedEditLabelProperty()
    {
        return "link.extended.edit.label"; //$NON-NLS-1$
    }

    protected String getDirectEditLabelProperty()
    {
        return "link.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        if(cIsNT) {
            return new NTLinkFigure(getInstallOptionsWidget());
        }
        else {
            return new LinkFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
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

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
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

    protected class NTLinkFigure extends NTLabelFigure
    {
        private RGB mTxtColor;

        public NTLinkFigure(IPropertySource propertySource)
        {
            super(propertySource);
            setTxtColor((RGB)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TXTCOLOR));
       }
        
        public RGB getTxtColor()
        {
            return mTxtColor==null?InstallOptionsLink.DEFAULT_TXTCOLOR:mTxtColor;
        }

        public void setTxtColor(RGB txtColor)
        {
            mTxtColor = txtColor;
            refresh();
        }
    }
}
