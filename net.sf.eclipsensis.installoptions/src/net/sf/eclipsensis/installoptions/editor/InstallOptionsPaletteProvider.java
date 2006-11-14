/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.template.*;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.palette.*;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

public class InstallOptionsPaletteProvider
{
    private static final ImageDescriptor cImageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("templates.icon")); //$NON-NLS-1$

    private InstallOptionsPaletteProvider()
    {
    }

    private static List createCategories(GraphicalViewer viewer, PaletteRoot root)
    {
        List categories = new ArrayList();

        categories.add(createControlGroup(root));
        categories.add(createComponentsDrawer(viewer));
        categories.add(createTemplatesDrawer(viewer));
        return categories;
    }

    private static PaletteContainer createComponentsDrawer(final GraphicalViewer viewer)
    {
        final PaletteDrawer drawer = new PaletteDrawer(InstallOptionsPlugin.getResourceString("palette.components.drawer.name"), InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("controls.icon"))); //$NON-NLS-1$ //$NON-NLS-2$
        final Map entryMap = new HashMap();

        final Runnable op = new Runnable() {
            public void run()
            {
                Boolean unload = Boolean.valueOf(InstallOptionsPlugin.getDefault().getPreferenceStore().getBoolean(IInstallOptionsConstants.PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED));
                List entries = new ArrayList();
                for (Iterator iter = InstallOptionsModel.INSTANCE.getControlTypeDefs().iterator(); iter.hasNext(); ) {
                    InstallOptionsModelTypeDef typeDef = (InstallOptionsModelTypeDef)iter.next();
                    ToolEntry entry = (ToolEntry)entryMap.get(typeDef.getType());
                    if(entry == null) {
                        entry = createComponentEntry(typeDef);
                        entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, unload);
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
        InstallOptionsModel.INSTANCE.addModelListener(listener);

        final IPropertyChangeListener listener2 = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(IInstallOptionsConstants.PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)) {
                    Boolean newValue = (Boolean)event.getNewValue();
                    for(Iterator iter=entryMap.values().iterator(); iter.hasNext(); ) {
                        ((ToolEntry)iter.next()).setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, newValue);
                    }
                }
            }
        };

        InstallOptionsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(listener2);

        viewer.getControl().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                InstallOptionsModel.INSTANCE.removeModelListener(listener);
                InstallOptionsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(listener2);
            }
        });

        return drawer;
    }

    private static ToolEntry createComponentEntry(InstallOptionsModelTypeDef typeDef)
    {
        CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
                typeDef.getName(),
                typeDef.getDescription(),
                typeDef.getType(),
                InstallOptionsElementFactory.getFactory(typeDef.getType()),
                InstallOptionsPlugin.getImageManager().getImageDescriptor(typeDef.getSmallIcon()),
                InstallOptionsPlugin.getImageManager().getImageDescriptor(typeDef.getLargeIcon()));
        return entry;
    }

    private static PaletteContainer createTemplatesDrawer(final GraphicalViewer viewer)
    {
        final PaletteDrawer drawer = new PaletteDrawer(InstallOptionsPlugin.getResourceString("palette.templates.drawer.name"),  //$NON-NLS-1$
                cImageDescriptor);
        final Map entryMap = new HashMap();
        List children = new ArrayList();
        Boolean unload = Boolean.valueOf(InstallOptionsPlugin.getDefault().getPreferenceStore().getBoolean(IInstallOptionsConstants.PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED));
        for(Iterator iter = InstallOptionsTemplateManager.INSTANCE.getTemplates().iterator(); iter.hasNext(); ) {
            InstallOptionsTemplate template = (InstallOptionsTemplate)iter.next();
            if(!template.isDeleted()) {
                ToolEntry entry = createTemplateEntry(template);
                entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, unload);
                entryMap.put(template, entry);
                children.add(entry);
            }
        }
        drawer.setChildren(children);

        final IInstallOptionsTemplateListener listener = new IInstallOptionsTemplateListener() {
            public void templateChanged(InstallOptionsTemplateEvent event)
            {
                InstallOptionsTemplate oldTemplate = event.getOldTemplate();
                InstallOptionsTemplate newTemplate = event.getNewTemplate();
                ToolEntry entry;
                switch(event.getType()) {
                    case InstallOptionsTemplateEvent.TEMPLATE_ADDED:
                        entry = createTemplateEntry(newTemplate);
                        entryMap.put(newTemplate, entry);
                        drawer.add(entry);
                        break;
                    case InstallOptionsTemplateEvent.TEMPLATE_REMOVED:
                        entry = (ToolEntry)entryMap.remove(oldTemplate);
                        if(entry != null) {
                            drawer.remove(entry);
                        }
                        break;
                    case InstallOptionsTemplateEvent.TEMPLATE_UPDATED:
                        entry = (ToolEntry)entryMap.remove(oldTemplate);
                        if(entry != null) {
                            ((CombinedTemplateCreationEntry)entry).setTemplate(newTemplate);
                            entry.setLabel(newTemplate.getName());
                            entry.setVisible(newTemplate.isEnabled());
                            entry.setDescription(newTemplate.getDescription());
                            entry. setToolProperty(CreationTool.PROPERTY_CREATION_FACTORY, InstallOptionsTemplateManager.INSTANCE.getTemplateFactory(newTemplate));
                            entryMap.put(newTemplate, entry);
                        }
                        break;
                }

            }
        };
        InstallOptionsTemplateManager.INSTANCE.addTemplateListener(listener);

        final IPropertyChangeListener listener2 = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(IInstallOptionsConstants.PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)) {
                    Boolean newValue = (Boolean)event.getNewValue();
                    for(Iterator iter=entryMap.values().iterator(); iter.hasNext(); ) {
                        ((ToolEntry)iter.next()).setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, newValue);
                    }
                }
            }
        };

        InstallOptionsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(listener2);

        viewer.getControl().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                InstallOptionsTemplateManager.INSTANCE.removeTemplateListener(listener);
                InstallOptionsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(listener2);
            }
        });

        return drawer;
    }

    private static ToolEntry createTemplateEntry(InstallOptionsTemplate template)
    {
        CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
                template.getName(),
                template.getDescription(),
                template,
                InstallOptionsTemplateManager.INSTANCE.getTemplateFactory(template),
                cImageDescriptor,
                cImageDescriptor);
        entry.setToolClass(InstallOptionsTemplateCreationTool.class);
        entry.setVisible(template.isEnabled());
        return entry;
    }

    private static PaletteContainer createControlGroup(PaletteRoot root)
    {
        PaletteGroup controlGroup = new PaletteGroup(InstallOptionsPlugin.getResourceString("palette.control.group.name")); //$NON-NLS-1$

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
