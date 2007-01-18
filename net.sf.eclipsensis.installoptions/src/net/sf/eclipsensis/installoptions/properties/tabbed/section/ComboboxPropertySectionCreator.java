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

import net.sf.eclipsensis.installoptions.model.InstallOptionsCombobox;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class ComboboxPropertySectionCreator extends ListItemsPropertySectionCreator
{
    public ComboboxPropertySectionCreator(InstallOptionsCombobox element)
    {
        super(element);
    }

//    protected void createListAndStateButtons(Composite buttons, final CheckboxTableViewer viewer, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
//    {
//        super.createListAndStateButtons(buttons, viewer, widgetFactory, commandHelper);
//        final IPropertyDescriptor stateDescriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE);
//        final ICellEditorValidator stateValidator = (ICellEditorValidator)Common.getObjectFieldValue(listItemsDescriptor, "validator", ICellEditorValidator.class); //$NON-NLS-1$
//        final Button add = widgetFactory.createButton(buttons,"",SWT.PUSH|FLAT_STYLE);
//        add.setImage(InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("select.all.icon")));
//        add.setToolTipText(EclipseNSISPlugin.getResourceString("select.all.tooltip")); //$NON-NLS-1$
//        add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//        add.addListener(SWT.Selection, new Listener() {
//            public void handleEvent(Event e) {
//                List list = (List)viewer.getInput();
//                if(list != null) {
//                    List
//                    int counter = 1;
//                    String item = InstallOptionsPlugin.getFormattedString("default.listitem.label", new Object[]{new Integer(counter++)}); //$NON-NLS-1$
//
//                    while(Common.collectionContainsIgnoreCase(list, item)) {
//                        item = InstallOptionsPlugin.getFormattedString("default.listitem.label", new Object[]{new Integer(counter++)}); //$NON-NLS-1$
//                    }
//                    list.add(item);
//                    String error = listItemsValidator.isValid(list);
//                    if(Common.isEmpty(error)) {
//                        commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_LISTITEMS, 
//                                listItemsDescriptor.getDisplayName(), getWidget(), list);
//                        viewer.refresh(false);
//                        viewer.setSelection(new StructuredSelection(item));
//                        viewer.editElement(item,0);
//                        Text t = (Text)textEditor.getControl();
//                        t.setSelection(item.length());
//                    }
//                    else {
//                        Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
//                        list.remove(item);
//                    }
//                }
//            }
//        });
//
//    }

    protected CheckboxTableViewer createListItemsAndStateSection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        createTextSection(parent, InstallOptionsModel.PROPERTY_STATE, widgetFactory, commandHelper);
        return super.createListItemsAndStateSection(parent, widgetFactory, commandHelper);
    }

}
