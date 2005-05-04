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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class InstallOptionsButton extends InstallOptionsWidget
{
    public static String PROPERTY_TEXT = "net.sf.eclipsensis.installoptions.button_text"; //$NON-NLS-1$
    private static Image BUTTON_ICON = InstallOptionsPlugin.getImageManager().getImage("icons/button16.gif"); //$NON-NLS-1$
    protected static IPropertyDescriptor[] cNewDescriptors = null;

    private String mText = InstallOptionsPlugin.getResourceString("button.text.default"); //$NON-NLS-1$

    static {
        int n = cDescriptors.length;
        cNewDescriptors = new IPropertyDescriptor[n+1];
        System.arraycopy(cDescriptors,0,cNewDescriptors,0,n);
        cNewDescriptors[n++] = new TextPropertyDescriptor(PROPERTY_TEXT, InstallOptionsPlugin.getResourceString("text.property.name")); //$NON-NLS-1$;
    }

    public InstallOptionsButton()
    {
        super();
        mPosition = new Position(0,0,59,24);
    }

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return cNewDescriptors;
    }

    public Object getPropertyValue(Object propName)
    {
        if (PROPERTY_TEXT.equals(propName)) {
            return getText();
        }
        return super.getPropertyValue(propName);
    }
    
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(PROPERTY_TEXT)) {
            setText((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public String getText()
    {
        return mText;
    }

    public Image getIconImage()
    {
        return BUTTON_ICON;
    }

    public void setText(String s)
    {
        mText = s;
        firePropertyChange(PROPERTY_TEXT, null, mText);
    }

    public String toString()
    {
        return new StringBuffer(super.toString()).append(" ").append(InstallOptionsPlugin.getResourceString("button.template.name")).append("=").append(getText()).toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String getType()
    {
        return IInstallOptionsConstants.TYPE_BUTTON;
    }
}
