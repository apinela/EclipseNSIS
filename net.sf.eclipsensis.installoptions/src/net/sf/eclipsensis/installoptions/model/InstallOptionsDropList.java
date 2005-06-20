/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class InstallOptionsDropList extends InstallOptionsCombobox
{
    public static Image DROPLIST_ICON = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("droplist.type.small.icon")); //$NON-NLS-1$
    
    public InstallOptionsDropList()
    {
        this(InstallOptionsModel.TYPE_DROPLIST);
    }
    
    /**
     * @param type
     */
    public InstallOptionsDropList(String type)
    {
        super(type);
    }

    public Image getIconImage()
    {
        return DROPLIST_ICON;
    }
    
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            ComboStatePropertyDescriptor descriptor = (ComboStatePropertyDescriptor)super.createPropertyDescriptor(name);
            descriptor.setStyle(SWT.DROP_DOWN|SWT.READ_ONLY);
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }
    
    public void setListItems(List listItems)
    {
        super.setListItems(listItems);
        String oldState = getState();
        String newState = (!getListItems().contains(oldState)?"":oldState); //$NON-NLS-1$
        if(!Common.stringsAreEqual(oldState,newState)) {
            fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE,newState));
        }
    }
}

