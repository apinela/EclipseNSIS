/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public abstract class InstallOptionsDirectEditManager extends DirectEditManager
{
    /**
     * @param source
     * @param editorType
     * @param locator
     */
    public InstallOptionsDirectEditManager(GraphicalEditPart source,
            Class editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    protected final CellEditor createCellEditorOn(Composite composite)
    {
        InstallOptionsModelTypeDef typeDef = ((InstallOptionsWidget)getEditPart().getModel()).getTypeDef();
        if(typeDef == null || !typeDef.getSettings().contains(getDirectEditProperty())) {
            return null;
        }
        else {
            return createCellEditor(composite);
        }
    }

    protected CellEditor createCellEditor(Composite composite)
    {
        return super.createCellEditorOn(composite);
    }

    protected final void initCellEditor()
    {
        InstallOptionsWidget control = (InstallOptionsWidget)getEditPart().getModel();
        IPropertyDescriptor descriptor = control.getPropertyDescriptor(getDirectEditProperty());
        if(descriptor instanceof PropertyDescriptor) {
            try {
                ICellEditorValidator validator = (ICellEditorValidator)WinAPI.GetObjectFieldValue(descriptor, "validator", "Lorg/eclipse/jface/viewers/ICellEditorValidator;"); //$NON-NLS-1$ //$NON-NLS-2$
                if (validator != null) {
                    getCellEditor().setValidator(validator);
                }
            }
            catch (Throwable t) {
                InstallOptionsPlugin.getDefault().log(t);
            }            
        }
        String initialText = getInitialText(control);
        getCellEditor().setValue(initialText);
        selectCellEditorText();
    }

    protected abstract String getInitialText(InstallOptionsWidget control);
    protected abstract void selectCellEditorText();
    protected abstract String getDirectEditProperty();
}
