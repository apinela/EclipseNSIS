/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
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

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class PathRequestPropertySectionCreator extends EditableElementPropertySectionCreator
{
    public PathRequestPropertySectionCreator(InstallOptionsPathRequest element)
    {
        super(element);
    }

    protected Control createAppearancePropertySection(Composite parent, final TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        parent = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        createTextSection(parent, InstallOptionsModel.PROPERTY_STATE, widgetFactory, commandHelper);
        return parent;
    }

    protected boolean shouldCreateAppearancePropertySection()
    {
        return true;
    }

    protected boolean shouldCreateOtherPropertySection()
    {
        return true;
    }
}
