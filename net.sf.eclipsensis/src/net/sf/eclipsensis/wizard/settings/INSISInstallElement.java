/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.wizard.INSISWizardConstants;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public interface INSISInstallElement extends INSISWizardConstants
{
    public String getType();
    public String getDisplayName();
    public boolean isEditable();
    public boolean isRemovable();
    public boolean edit(Composite composite);
    public boolean hasChildren();
    public INSISInstallElement[] getChildren();
    public INSISInstallElement getParent();
    void setParent(INSISInstallElement parent);
    public String[] getChildTypes();
    public void addChild(INSISInstallElement child);
    public void removeChild(INSISInstallElement child);
    public Image getImage();
    public void setSettings(NSISWizardSettings settings);
    public NSISWizardSettings getSettings();
}
