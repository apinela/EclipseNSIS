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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class NSISInstallElementLabelProvider extends LabelProvider
{
    public Image getImage(Object element) {
        if(element instanceof INSISInstallElement) {
            return ((INSISInstallElement)element).getImage();
        }
        else {
            return super.getImage(element);
        }
    }

    public String getText(Object element) {
        if(element instanceof INSISInstallElement) {
            return ((INSISInstallElement)element).getDisplayName();
        }
        else {
            return super.getText(element);
        }
    }

    public boolean isLabelProperty(Object element, String property) {
        if(element instanceof INSISInstallElement) {
            return false;
        }
        else {
            return super.isLabelProperty(element, property);
        }
    }
}