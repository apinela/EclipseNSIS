/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsButton;

import org.eclipse.gef.palette.*;
import org.eclipse.gef.requests.SimpleFactory;

public class InstallOptionsPaletteProvider
{
    static private List createCategories(PaletteRoot root)
    {
        List categories = new ArrayList();

        categories.add(createControlGroup(root));
        categories.add(createComponentsDrawer());

        return categories;
    }

    private static PaletteContainer createComponentsDrawer()
    {

        PaletteDrawer drawer = new PaletteDrawer("Components", InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("components.icon"))); //$NON-NLS-1$ //$NON-NLS-2$
        List entries = new ArrayList();

        
        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(InstallOptionsPlugin.getResourceString("button.template.name"), //$NON-NLS-1$
                                                                InstallOptionsPlugin.getResourceString("button.template.short.desc"), //$NON-NLS-1$
                                                                IInstallOptionsConstants.TEMPLATE_BUTTON, 
                                                                new SimpleFactory(InstallOptionsButton.class),
                                                                InstallOptionsPlugin.getImageManager().getImageDescriptor("icons/button16.gif"), //$NON-NLS-1$ 
                                                                InstallOptionsPlugin.getImageManager().getImageDescriptor("icons/button24.gif")//$NON-NLS-1$
        );
        entries.add(combined);
         
        drawer.addAll(entries);
        return drawer;
    }

    static private PaletteContainer createControlGroup(PaletteRoot root)
    {
        PaletteGroup controlGroup = new PaletteGroup(InstallOptionsPlugin.getResourceString("palettegroup.name")); //$NON-NLS-1$

        List entries = new ArrayList();

        ToolEntry tool = new PanningSelectionToolEntry();
        entries.add(tool);
        root.setDefaultEntry(tool);

        tool = new MarqueeToolEntry();
        entries.add(tool);
        controlGroup.addAll(entries);
        return controlGroup;
    }

    static PaletteRoot createPalette()
    {
        PaletteRoot ioPalette = new PaletteRoot();
        ioPalette.addAll(createCategories(ioPalette));
        return ioPalette;
    }
}
