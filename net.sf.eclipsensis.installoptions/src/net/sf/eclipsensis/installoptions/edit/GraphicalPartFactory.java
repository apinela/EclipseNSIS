/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.button.InstallOptionsButtonEditPart;
import net.sf.eclipsensis.installoptions.edit.checkbox.InstallOptionsCheckBoxEditPart;
import net.sf.eclipsensis.installoptions.edit.combobox.InstallOptionsComboboxEditPart;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.edit.droplist.InstallOptionsDropListEditPart;
import net.sf.eclipsensis.installoptions.edit.groupbox.InstallOptionsGroupBoxEditPart;
import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart;
import net.sf.eclipsensis.installoptions.edit.link.InstallOptionsLinkEditPart;
import net.sf.eclipsensis.installoptions.edit.listbox.InstallOptionsListboxEditPart;
import net.sf.eclipsensis.installoptions.edit.password.InstallOptionsPasswordEditPart;
import net.sf.eclipsensis.installoptions.edit.pathrequest.InstallOptionsPathRequestEditPart;
import net.sf.eclipsensis.installoptions.edit.picture.InstallOptionsPictureEditPart;
import net.sf.eclipsensis.installoptions.edit.radiobutton.InstallOptionsRadioButtonEditPart;
import net.sf.eclipsensis.installoptions.edit.text.InstallOptionsTextEditPart;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.ModifyFilterCommand;
import net.sf.eclipsensis.installoptions.properties.dialogs.FileFilterDialog;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;

public class GraphicalPartFactory implements EditPartFactory
{
    private static GraphicalPartFactory cInstance = null;
    
    public static final GraphicalPartFactory getInstance()
    {
        if(cInstance == null) {
            synchronized(GraphicalPartFactory.class) {
                if(cInstance == null) {
                    cInstance = new GraphicalPartFactory();
                }
            }
        }
        return cInstance;
    }
    
    private GraphicalPartFactory()
    {
    }

    public EditPart createEditPart(EditPart context, Object model)
    {
        EditPart child = null;

        if(model != null) {
            Class clasz = model.getClass();
            if(clasz.equals(InstallOptionsLabel.class)) {
                child = new InstallOptionsLabelEditPart();
            }
            else if(clasz.equals(InstallOptionsBitmap.class)) {
                child = new InstallOptionsPictureEditPart() {
                    protected String getDirectEditLabelProperty()
                    {
                        return "bitmap.direct.edit.label"; //$NON-NLS-1$
                    }

                    protected String getTypeName()
                    {
                        return InstallOptionsPlugin.getResourceString("bitmap.type.name"); //$NON-NLS-1$
                    }
                };
            }
            else if(clasz.equals(InstallOptionsIcon.class)) {
                child = new InstallOptionsPictureEditPart() {
                    protected String getDirectEditLabelProperty()
                    {
                        return "icon.direct.edit.label"; //$NON-NLS-1$
                    }

                    protected String getTypeName()
                    {
                        return InstallOptionsPlugin.getResourceString("icon.type.name"); //$NON-NLS-1$
                    }
                };
            }
            else if(clasz.equals(InstallOptionsLink.class)) {
                child = new InstallOptionsLinkEditPart();
            }
            else if(clasz.equals(InstallOptionsButton.class)) {
                child = new InstallOptionsButtonEditPart();
            }
            else if(clasz.equals(InstallOptionsCheckBox.class)) {
                child = new InstallOptionsCheckBoxEditPart();
            }
            else if(clasz.equals(InstallOptionsRadioButton.class)) {
                child = new InstallOptionsRadioButtonEditPart();
            }
            else if(clasz.equals(InstallOptionsFileRequest.class)) {
                child = new InstallOptionsPathRequestEditPart() {
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
                                ModifyFilterCommand command = new ModifyFilterCommand((InstallOptionsWidgetEditPart)request.getEditPart(), (List)request.getNewValue());
                                return command;
                            }
                        });
                    }
                };
            }
            else if(clasz.equals(InstallOptionsDirRequest.class)) {
                child = new InstallOptionsPathRequestEditPart() {
                    protected String getDirectEditLabelProperty()
                    {
                        return "dirrequest.direct.edit.label"; //$NON-NLS-1$
                    }

                    protected String getTypeName()
                    {
                        return InstallOptionsPlugin.getResourceString("dirrequest.type.name"); //$NON-NLS-1$
                    }
                };
            }
            else if (clasz.equals(InstallOptionsGroupBox.class)) {
                child = new InstallOptionsGroupBoxEditPart();
            }
            else if (clasz.equals(InstallOptionsText.class)) {
                child = new InstallOptionsTextEditPart();
            }
            else if (clasz.equals(InstallOptionsPassword.class)) {
                child = new InstallOptionsPasswordEditPart();
            }
            else if (clasz.equals(InstallOptionsDropList.class)) {
                child = new InstallOptionsDropListEditPart();
            }
            else if (clasz.equals(InstallOptionsCombobox.class)) {
                child = new InstallOptionsComboboxEditPart();
            }
            else if (clasz.equals(InstallOptionsListbox.class)) {
                child = new InstallOptionsListboxEditPart();
            }
            else if (clasz.equals(InstallOptionsDialog.class)) {
                child = new InstallOptionsDialogEditPart();
            }
            child.setModel(model);
        }
        return child;
    }

}
