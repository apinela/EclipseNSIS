/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listitems;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.sf.eclipsensis.installoptions.edit.IExtendedEditSupport;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.figures.IListItemsFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsListItems;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.properties.dialogs.ListItemsDialog;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.jface.window.Window;

public abstract class InstallOptionsListItemsEditPart extends InstallOptionsEditableElementEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsListItems model = (InstallOptionsListItems)getModel();
            ListItemsDialog dialog = new ListItemsDialog(getViewer().getControl().getShell(),
                                                         model.getListItems(), model.getType());
            dialog.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_LISTITEMS));
            if (dialog.open() == Window.OK) {
                mNewValue = dialog.getValues();
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

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsListItemsExtendedEditPolicy(this));
    }

    public Object getAdapter(Class key)
    {
        if(IExtendedEditSupport.class.equals(key)) {
            return mExtendedEditSupport;
        }
        return super.getAdapter(key);
    }

    protected final IInstallOptionsFigure createInstallOptionsFigure()
    {
        return createListItemsFigure();
    }

    public void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            IListItemsFigure figure2 = (IListItemsFigure)getFigure();
            figure2.setListItems((List)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }

    protected abstract IListItemsFigure createListItemsFigure();
}