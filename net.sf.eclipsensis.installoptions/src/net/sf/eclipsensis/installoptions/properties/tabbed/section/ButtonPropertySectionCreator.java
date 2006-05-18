/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;


import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class ButtonPropertySectionCreator extends UneditableElementPropertySectionCreator
{
    public ButtonPropertySectionCreator(InstallOptionsButton button)
    {
        super(button);
    }

    protected Control createOtherPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        Composite composite = (Composite)super.createOtherPropertySection(parent, widgetFactory, commandHelper);
        createTextSection(composite, InstallOptionsModel.PROPERTY_STATE, widgetFactory, commandHelper);
        return composite;
    }

    protected boolean shouldCreateOtherPropertySection()
    {
        return true;
    }
}