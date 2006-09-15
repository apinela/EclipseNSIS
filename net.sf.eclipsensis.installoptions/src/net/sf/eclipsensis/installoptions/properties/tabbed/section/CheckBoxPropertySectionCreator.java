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


import net.sf.eclipsensis.installoptions.model.InstallOptionsCheckBox;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class CheckBoxPropertySectionCreator extends UneditableElementPropertySectionCreator
{
    public CheckBoxPropertySectionCreator(InstallOptionsCheckBox checkbox)
    {
        super(checkbox);
    }

    protected Control createAppearancePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite composite = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        InstallOptionsCheckBox checkbox = (InstallOptionsCheckBox)getWidget();
        Integer[] stateData = checkbox.getStateData();
        String[] stateDisplay = checkbox.getStateDisplay();
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        for (int i = 0; i < Math.min(stateData.length,stateDisplay.length); i++) {
            map.put(stateData[i], stateDisplay[i]);
        }
        Integer defaultValue = null;
        if(checkbox.getStateDefault() < map.size()) {
            defaultValue = stateData[checkbox.getStateDefault()];
        }
        createComboSection(composite, InstallOptionsModel.PROPERTY_STATE, map, defaultValue, widgetFactory, commandHelper);
        return composite;
    }
}
