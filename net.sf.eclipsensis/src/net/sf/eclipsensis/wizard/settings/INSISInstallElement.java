/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.io.Serializable;

import net.sf.eclipsensis.util.INodeConvertible;
import net.sf.eclipsensis.wizard.INSISWizardConstants;
import net.sf.eclipsensis.wizard.NSISWizard;

import org.eclipse.swt.graphics.Image;

public interface INSISInstallElement extends INSISWizardConstants, Serializable, INodeConvertible
{
    public static final String NODE = "element"; //$NON-NLS-1$
    public static final String CHILD_NODE = "attribute"; //$NON-NLS-1$
    public static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

    public String getType();
    public String getDisplayName();
    public boolean isEditable();
    public boolean isRemovable();
    public boolean edit(NSISWizard wizard);
    public boolean hasChildren();
    public INSISInstallElement[] getChildren();
    public void setChildren(INSISInstallElement[] children);
    public INSISInstallElement getParent();
    void setParent(INSISInstallElement parent);
    public String[] getChildTypes();
    public void addChild(INSISInstallElement child);
    public void removeChild(INSISInstallElement child);
    public void removeAllChildren();
    public Image getImage();
    public void setSettings(NSISWizardSettings settings);
    public NSISWizardSettings getSettings();
    public Object clone() throws CloneNotSupportedException;
    public String validate();
    public String validate(boolean recursive);
}
