/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.pathrequest;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.IExtendedEditSupport;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy;
import net.sf.eclipsensis.installoptions.model.InstallOptionsFileRequest;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.commands.ModifyFilterCommand;
import net.sf.eclipsensis.installoptions.properties.dialogs.FileFilterDialog;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;

public class InstallOptionsFileRequestEditPart extends InstallOptionsPathRequestEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsFileRequest model = (InstallOptionsFileRequest)getModel();
            FileFilterDialog dialog = new FileFilterDialog(getViewer().getControl().getShell(), model.getFilter());
            dialog.setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_FILTER));
            if (dialog.open() != Window.OK) {
                mNewValue = dialog.getFilter();
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

    protected String getDirectEditLabelProperty()
    {
        return "filerequest.direct.edit.label"; //$NON-NLS-1$
    }

    protected String getExtendedEditLabelProperty()
    {
        return "filerequest.extended.edit.label"; //$NON-NLS-1$
    }

    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("filerequest.type.name"); //$NON-NLS-1$
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsExtendedEditPolicy(this) {
            protected Command getExtendedEditCommand(ExtendedEditRequest request)
            {
                ModifyFilterCommand command = new ModifyFilterCommand((InstallOptionsFileRequest)request.getEditPart().getModel(), (List)request.getNewValue());
                return command;
            }

            protected String getExtendedEditProperty()
            {
                return InstallOptionsModel.PROPERTY_FILTER;
            }
        });
    }
}