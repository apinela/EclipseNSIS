/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.template.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.palette.*;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.*;
import org.eclipse.swt.events.*;

public class InstallOptionsPaletteProvider
{
    private static final ImageDescriptor cImageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("templates.icon")); //$NON-NLS-1$
    public static final Comparator cTemplateEntryComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            CombinedTemplateCreationEntry e1 = (CombinedTemplateCreationEntry)o1;
            CombinedTemplateCreationEntry e2 = (CombinedTemplateCreationEntry)o2;
            if(e1 == null && e2 != null) {
                return -1;
            }
            else if(e1 != null && e2 == null) {
                return 1;
            }
            else {
                String l1 = e1.getLabel();
                String l2 = e2.getLabel();
                if(l1 == null && l2 != null) {
                    return -1;
                }
                else if(l1 != null && l2 == null) {
                    return 1;
                }
                else {
                    return l1.compareTo(l2);
                }
            }
        }
    };

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
        final List entries = new ArrayList();
        Boolean unload = Boolean.valueOf(InstallOptionsPlugin.getDefault().getPreferenceStore().getBoolean(IInstallOptionsConstants.PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED));
        for(Iterator iter = InstallOptionsTemplateManager.INSTANCE.getTemplates().iterator(); iter.hasNext(); ) {
            InstallOptionsTemplate template = (InstallOptionsTemplate)iter.next();
            if(!template.isDeleted()) {
                ToolEntry entry = createTemplateEntry(template);
                entry.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, unload);
                entries.add(entry);
            }
        }
        Collections.sort(entries, cTemplateEntryComparator);
        drawer.setChildren(new ArrayList(entries));

        final IInstallOptionsTemplateListener listener = new IInstallOptionsTemplateListener() {
            private ToolEntry findEntry(InstallOptionsTemplate template)
            {
                for (Iterator iter = entries.iterator(); iter.hasNext();) {
                    CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry)iter.next();
                    if(Common.objectsAreEqual(template,entry.getTemplate())) {
                        return entry;
                    }
                }
                return null;
            }

            public void templatesChanged(final InstallOptionsTemplateEvent[] events)
            {
                for (int i = 0; i < events.length; i++) {
                    InstallOptionsTemplateEvent event = events[i];
                    InstallOptionsTemplate oldTemplate = event.getOldTemplate();
                    InstallOptionsTemplate newTemplate = event.getNewTemplate();
                    ToolEntry entry;
                    switch(event.getType()) {
                        case InstallOptionsTemplateEvent.TEMPLATE_ADDED:
                            entry = createTemplateEntry(newTemplate);
                            entries.add(entry);
                            break;
                        case InstallOptionsTemplateEvent.TEMPLATE_REMOVED:
                            entry = findEntry(oldTemplate);
                            if(entry != null) {
                                entries.remove(entry);
                            }
                            break;
                        case InstallOptionsTemplateEvent.TEMPLATE_UPDATED:
                            entry = findEntry(oldTemplate);
                            if(entry != null) {
                                ((CombinedTemplateCreationEntry)entry).setTemplate(newTemplate);
                                entry.setLabel(newTemplate.getName());
                                entry.setVisible(newTemplate.isEnabled());
                                entry.setDescription(newTemplate.getDescription());
                                entry.setToolProperty(CreationTool.PROPERTY_CREATION_FACTORY, InstallOptionsTemplateManager.INSTANCE.getTemplateFactory(newTemplate));
                            }
                            break;
                    }
                }
                Collections.sort(entries, cTemplateEntryComparator);
                drawer.setChildren(new ArrayList(entries));
            }
        };
        InstallOptionsTemplateManager.INSTANCE.addTemplateListener(listener);

        final IPropertyChangeListener listener2 = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(IInstallOptionsConstants.PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)) {
                    Boolean newValue = (Boolean)event.getNewValue();
                    for(Iterator iter=entries.iterator(); iter.hasNext(); ) {
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
