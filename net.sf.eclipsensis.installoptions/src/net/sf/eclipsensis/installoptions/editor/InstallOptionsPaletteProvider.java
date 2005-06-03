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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElementFactory;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.gef.palette.*;

public class InstallOptionsPaletteProvider
{
    private static List createCategories(PaletteRoot root)
    {
        List categories = new ArrayList();

        categories.add(createControlGroup(root));
        categories.add(createComponentsDrawer());

        return categories;
    }

    private static PaletteContainer createComponentsDrawer()
    {
        PaletteDrawer drawer = new PaletteDrawer(InstallOptionsPlugin.getResourceString("palettedrawer.name"), InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("controls.icon"))); //$NON-NLS-1$ //$NON-NLS-2$
        List entries = new ArrayList();
        InstallOptionsModel model = InstallOptionsModel.getInstance();
        String[] controlTypes = model.getControlTypes();
        for (int i = 0; i < controlTypes.length; i++) {
            entries.add(createComponentEntry(controlTypes[i]));
        }
         
        drawer.addAll(entries);
        return drawer;
    }

    private static PaletteEntry createComponentEntry(String type)
    {
        String ltype = type.toLowerCase();
        CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(InstallOptionsPlugin.getResourceString(ltype+".type.name"), //$NON-NLS-1$
                InstallOptionsPlugin.getResourceString(ltype+".type.short.desc"), //$NON-NLS-1$
                type, 
                new InstallOptionsElementFactory(type),
                InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(ltype+".type.small.icon")), //$NON-NLS-1$
                InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(ltype+".type.large.icon"))); //$NON-NLS-1$
        return entry;
    }

    private static PaletteContainer createControlGroup(PaletteRoot root)
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
