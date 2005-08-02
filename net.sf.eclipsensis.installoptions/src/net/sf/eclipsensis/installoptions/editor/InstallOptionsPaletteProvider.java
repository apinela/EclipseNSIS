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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.palette.*;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

public class InstallOptionsPaletteProvider
{
    private static List createCategories(GraphicalViewer viewer, PaletteRoot root)
    {
        List categories = new ArrayList();

        categories.add(createControlGroup(viewer, root));
        categories.add(createComponentsDrawer(viewer));

        return categories;
    }

    private static PaletteContainer createComponentsDrawer(final GraphicalViewer viewer)
    {
        final PaletteDrawer drawer = new PaletteDrawer(InstallOptionsPlugin.getResourceString("palettedrawer.name"), InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("controls.icon"))); //$NON-NLS-1$ //$NON-NLS-2$
        final Map entryMap = new HashMap();
        
        final Runnable op = new Runnable() {
            public void run()
            {
                List entries = new ArrayList();
                for (Iterator iter = InstallOptionsModel.INSTANCE.getControlTypeDefs().iterator(); iter.hasNext(); ) {
                    InstallOptionsModelTypeDef typeDef = (InstallOptionsModelTypeDef)iter.next();
                    PaletteEntry entry = (PaletteEntry)entryMap.get(typeDef.getType());
                    if(entry == null) {
                        entry = createComponentEntry(typeDef);
                        entryMap.put(typeDef.getType(), entry);
                    }
                    entries.add(entry);
                }
                 
                drawer.setChildren(entries);
            }
        };
        op.run();
        final IModelListener listener = new IModelListener(){
            public void modelChanged()
            {
                op.run();
            }
        };
        InstallOptionsModel.INSTANCE.addListener(listener);
        viewer.getControl().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                InstallOptionsModel.INSTANCE.removeListener(listener);
            }
        });
        return drawer;
    }

    private static PaletteEntry createComponentEntry(InstallOptionsModelTypeDef typeDef)
    {
        CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
                typeDef.getName(),
                typeDef.getDescription(),
                typeDef.getType(), 
                InstallOptionsElementFactory.getFactory(typeDef.getType()),
                InstallOptionsPlugin.getImageManager().getImageDescriptor(typeDef.getSmallIcon()),
                InstallOptionsPlugin.getImageManager().getImageDescriptor(typeDef.getLargeIcon()));
        entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, Boolean.FALSE);
        return entry;
    }

    private static PaletteContainer createControlGroup(GraphicalViewer viewer, PaletteRoot root)
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

    static PaletteRoot createPalette(GraphicalViewer viewer)
    {
        PaletteRoot paletteRoot = new PaletteRoot();
        paletteRoot.addAll(createCategories(viewer, paletteRoot));
        return paletteRoot;
    }
}
