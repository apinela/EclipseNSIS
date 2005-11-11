/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.checkbox;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.edit.button.InstallOptionsButtonEditPart;
import net.sf.eclipsensis.installoptions.figures.CheckBoxFigure;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsCheckBox;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsCheckBoxEditPart extends InstallOptionsButtonEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsCheckBox model = (InstallOptionsCheckBox)getModel();
            String state = model.getState();
            if(state.equals(InstallOptionsModel.STATE_CHECKED)) {
                mNewValue = InstallOptionsModel.STATE_UNCHECKED;
            }
            else {
                mNewValue = InstallOptionsModel.STATE_CHECKED;
            }
            return true;
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

    protected String getDirectEditLabelProperty()
    {
        return "checkbox.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new CheckBoxFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new CheckBoxCellEditorLocator((CheckBoxFigure)figure);
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsCheckBoxEditManager(part, clasz, locator);
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsCheckBoxExtendedEditPolicy(this));
    }

    protected String getExtendedEditLabelProperty()
    {
        return "checkbox.extended.edit.label"; //$NON-NLS-1$
    }

    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("checkbox.type.name"); //$NON-NLS-1$
    }

    protected void handleFlagAdded(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_RIGHT)) {
            ((CheckBoxFigure)getFigure()).setLeftText(true);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagAdded(flag);
        }
    }

    protected void handleFlagRemoved(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_RIGHT)) {
            ((CheckBoxFigure)getFigure()).setLeftText(false);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagRemoved(flag);
        }
    }

    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            CheckBoxFigure figure2 = (CheckBoxFigure)getFigure();
            figure2.setState(InstallOptionsModel.STATE_CHECKED.equals(evt.getNewValue()));
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }
}
