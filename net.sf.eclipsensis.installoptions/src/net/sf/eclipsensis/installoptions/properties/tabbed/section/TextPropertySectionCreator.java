/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class TextPropertySectionCreator extends EditableElementPropertySectionCreator
{
    public TextPropertySectionCreator(InstallOptionsText text)
    {
        super(text);
    }

    protected Control createAppearancePropertySection(Composite parent, final TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        parent = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        final Composite composite = widgetFactory.createComposite(parent);
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL,true,true);
        data.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        composite.setLayoutData(data);
        final Text[] text = {createStatePropertySection(composite, widgetFactory, commandHelper)};
        if (text != null) {
            Collection flags = getWidget().getTypeDef().getFlags();
            final boolean supportMultiLine = flags.contains(InstallOptionsModel.FLAGS_MULTILINE);
            final boolean supportOnlyNumbers = flags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS);

            if (supportMultiLine || supportOnlyNumbers) {
                final PropertyChangeListener listener = new PropertyChangeListener() {
                    private boolean checkFlag(List flags, String flag)
                    {
                        for (Iterator iter = flags.iterator(); iter.hasNext();) {
                            String f = (String)iter.next();
                            if(flag.equalsIgnoreCase(f)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
                            List oldFlags = (List)evt.getOldValue();
                            List newFlags = (List)evt.getNewValue();
                            boolean hasFlag = false;
                            if(supportMultiLine && checkFlag(oldFlags, InstallOptionsModel.FLAGS_MULTILINE) != checkFlag(newFlags,InstallOptionsModel.FLAGS_MULTILINE)) {
                                Control[] controls = composite.getChildren();
                                for (int i = 0; i < controls.length; i++) {
                                    controls[i].dispose();
                                }
                                text[0] = createStatePropertySection(composite, widgetFactory, commandHelper);
                                forceLayout(composite);
                            }
                            else if(supportOnlyNumbers && checkFlag(oldFlags,InstallOptionsModel.FLAGS_ONLY_NUMBERS) != (hasFlag = checkFlag(newFlags,InstallOptionsModel.FLAGS_ONLY_NUMBERS))) {
                                if(Common.isValid(text[0])) {
                                    if(hasFlag) {
                                        text[0].addVerifyListener(getNumberVerifyListener());
                                    }
                                    else {
                                        text[0].removeVerifyListener(getNumberVerifyListener());
                                    }
                                }
                            }
                        }
                    }
                };
                getWidget().addPropertyChangeListener(listener);
                composite.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        getWidget().removePropertyChangeListener(listener);
                    }
                });
            }
        }
        return parent;
    }

    protected Text createStatePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        Text text = createTextSection(parent, InstallOptionsModel.PROPERTY_STATE, widgetFactory, commandHelper,
                getWidget().hasFlag(InstallOptionsModel.FLAGS_MULTILINE));
        if(text != null) {
            if(getWidget().hasFlag(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                text.addVerifyListener(getNumberVerifyListener());
            }
        }
        return text;
    }

    protected boolean shouldCreateAppearancePropertySection()
    {
        return true;
    }
}
