/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.combobox;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsCombobox;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.properties.dialogs.ListItemsDialog;
import net.sf.eclipsensis.installoptions.properties.editors.EditableComboBoxCellEditor;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.window.Window;

public class InstallOptionsComboboxEditPart extends InstallOptionsEditableElementEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsCombobox model = (InstallOptionsCombobox)getModel();
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
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsComboboxExtendedEditPolicy());
    }
    
    public Object getAdapter(Class key)
    {
        if(IExtendedEditSupport.class.equals(key)) {
            return mExtendedEditSupport;
        }
        return super.getAdapter(key);
    }
    
    protected String getDirectEditLabelProperty()
    {
        return "combobox.direct.edit.label"; //$NON-NLS-1$
    }
    
    protected String getExtendedEditLabelProperty()
    {
        return "combobox.extended.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        return new ComboboxFigure(this);
    }

    public void initFigure(IInstallOptionsFigure figure2)
    {
        IListItemsFigure figure3 = (IListItemsFigure)figure2;
        InstallOptionsCombobox control = (InstallOptionsCombobox)getInstallOptionsEditableElement();
        figure3.setListItems(control.getListItems());
        super.initFigure(figure2);
    }

    public void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_LISTITEMS)) {//$NON-NLS-1$
            IListItemsFigure figure2 = (IListItemsFigure)getFigure();
            figure2.setListItems((List)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }
    
    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("combobox.type.name"); //$NON-NLS-1$
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsComboboxEditManager(part,clasz,locator);
    }

    protected Class getCellEditorClass()
    {
        return EditableComboBoxCellEditor.class;
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new ComboboxCellEditorLocator((ComboboxFigure)figure);
    }
}
