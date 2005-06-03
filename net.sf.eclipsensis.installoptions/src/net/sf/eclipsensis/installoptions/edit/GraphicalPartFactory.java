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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

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
                    protected String getDirectEditLabelProperty()
                    {
                        return "filerequest.direct.edit.label"; //$NON-NLS-1$
                    }

                    protected String getTypeName()
                    {
                        return InstallOptionsPlugin.getResourceString("filerequest.type.name"); //$NON-NLS-1$
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
