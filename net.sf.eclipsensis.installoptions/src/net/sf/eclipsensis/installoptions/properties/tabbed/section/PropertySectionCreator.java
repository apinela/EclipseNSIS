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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElement;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class PropertySectionCreator implements IPropertySectionCreator
{
	private InstallOptionsElement mElement;
    private NumberVerifyListener mNumberVerifyListener;

    public PropertySectionCreator(InstallOptionsElement element)
    {
        mElement = element;
    }

    public InstallOptionsElement getElement()
    {
        return mElement;
    }

    protected final Text createTextSection(Composite composite, final String propertyId, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        return createTextSection(composite, propertyId, widgetFactory, commandHelper, false);
    }

    /**
     * @param composite
     * @param propertyId
     * @param widgetFactory
     * @param commandHelper
     * @param multiline
     */
    protected final Text createTextSection(Composite composite, final String propertyId, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper, boolean multiline)
    {
        final IPropertyDescriptor descriptor = getElement().getPropertyDescriptor(propertyId);
        if (descriptor != null) {
            final TypeConverter converter = (multiline?TypeConverter.ESCAPED_STRING_CONVERTER:TypeConverter.STRING_CONVERTER);
            GridLayout layout;
            layout = (GridLayout)composite.getLayout();
            if(layout.numColumns != 2) {
                int numColumns = layout.numColumns;
                composite = widgetFactory.createComposite(composite);
                layout = new GridLayout(2,false);
                layout.marginWidth = layout.marginHeight = 0;
                composite.setLayout(layout);
                GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
                data.horizontalSpan = numColumns;
                composite.setLayoutData(data);
            }
            CLabel label = widgetFactory.createCLabel(composite, descriptor.getDisplayName());
            label.setLayoutData(new GridData(SWT.FILL,(multiline?SWT.TOP:SWT.FILL),false,false));
            ICellEditorValidator validator = (ICellEditorValidator)Common.getObjectFieldValue(descriptor, "validator", ICellEditorValidator.class); //$NON-NLS-1$
            final Text text = widgetFactory.createText(composite, converter.asString(getElement().getStringPropertyValue(propertyId)), (multiline?SWT.MULTI|SWT.V_SCROLL:SWT.SINGLE)|SWT.FLAT);
            text.setData(LABEL,label);
            text.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,multiline));
            final TextChangeHelper helper = new TextChangeHelper(validator) {
                protected String getResetValue(Text text)
                {
                    return converter.asString(getElement().getStringPropertyValue(propertyId));
                }

                protected void handleTextChange(Text text)
                {
                    if(!isNonUserChange()) {
                        String t = (String)converter.asType(text.getText());
                        if(!Common.stringsAreEqual(getElement().getStringPropertyValue(propertyId), t)) {
                            commandHelper.propertyChanged(propertyId,
                                    descriptor.getDisplayName(), getElement(), t);
                        }
                    }
                }
            };

            helper.connect(text);

            final PropertyChangeListener propertyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    helper.setNonUserChange(true);
                    try {
                        if(evt.getPropertyName().equals(propertyId)) {
                            String newText = converter.asString(evt.getNewValue());
                            if(Common.isValid(text) && !Common.stringsAreEqual(newText, text.getText())) {
                                text.setText(newText);
                            }
                        }
                    }
                    finally {
                        helper.setNonUserChange(false);
                    }
                }
            };
            getElement().addPropertyChangeListener(propertyListener);
            text.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    getElement().removePropertyChangeListener(propertyListener);
                }
            });
            return text;

        }
        return null;
    }

    protected final CCombo createComboSection(Composite composite, final String propertyId, final Map comboData, Object defaultValue, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        final IPropertyDescriptor descriptor = getElement().getPropertyDescriptor(propertyId);
        if (descriptor != null) {
            GridLayout layout;
            layout = (GridLayout)composite.getLayout();
            if(layout.numColumns != 2) {
                int numColumns = layout.numColumns;
                composite = widgetFactory.createComposite(composite);
                layout = new GridLayout(2,false);
                layout.marginWidth = layout.marginHeight = 0;
                composite.setLayout(layout);
                GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
                data.horizontalSpan = numColumns;
                composite.setLayoutData(data);
            }
            CLabel label = widgetFactory.createCLabel(composite, descriptor.getDisplayName());
            label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
            final ICellEditorValidator validator = (ICellEditorValidator)Common.getObjectFieldValue(descriptor, "validator", ICellEditorValidator.class); //$NON-NLS-1$
            final Map.Entry[] entries = (Map.Entry[])comboData.entrySet().toArray(new Map.Entry[comboData.size()]);
            Object selected = getElement().getPropertyValue(propertyId);

            final CCombo combo = widgetFactory.createCCombo(composite, SWT.DROP_DOWN|SWT.READ_ONLY|SWT.FLAT|SWT.BORDER);
            combo.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
            combo.setData(LABEL,label);

            for (int i = 0; i < entries.length; i++) {
                combo.add(String.valueOf(entries[i].getValue()));
            }
            Object selectedDisplay = comboData.get(selected);
            if(selectedDisplay == null) {
                selectedDisplay = comboData.get(defaultValue);
            }
            if(selectedDisplay != null) {
                combo.setText(String.valueOf(selectedDisplay));
            }
            final boolean[] nonUserChange = { false };
            combo.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if(!nonUserChange[0]) {
                        int n = combo.getSelectionIndex();
                        Object oldValue = getElement().getPropertyValue(propertyId);
                        Object newValue = entries[n].getKey();
                        boolean equal = false;
                        if(oldValue instanceof String && newValue instanceof String) {
                            equal = Common.stringsAreEqual((String)oldValue, (String)newValue, true);
                        }
                        else {
                            equal = Common.objectsAreEqual(oldValue, newValue);
                        }
                        if(!equal) {
                            String error = (validator==null?null:validator.isValid(newValue));
                            if(Common.isEmpty(error)) {
                                commandHelper.propertyChanged(propertyId,
                                        descriptor.getDisplayName(), getElement(), newValue);
                            }
                            else {
                                combo.removeSelectionListener(this);
                                try {
                                    Common.openError(combo.getShell(), error, InstallOptionsPlugin.getShellImage());
                                    String oldText = String.valueOf(comboData.get(oldValue));
                                    combo.setText(oldText);
                                }
                                finally {
                                    combo.addSelectionListener(this);
                                }
                            }
                        }
                    }
                }

            });
            final PropertyChangeListener propertyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    nonUserChange[0]=true;
                    try {
                        if(evt.getPropertyName().equals(propertyId)) {
                            String newValue = String.valueOf(comboData.get(evt.getNewValue()));
                            if(Common.isValid(combo) && !Common.stringsAreEqual(newValue, combo.getText())) {
                                combo.setText(newValue);
                            }
                        }
                    }
                    finally {
                        nonUserChange[0]=false;
                    }
                }
            };
            getElement().addPropertyChangeListener(propertyListener);
            combo.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    getElement().removePropertyChangeListener(propertyListener);
                }
            });
            return combo;
        }
        return null;
    }

    /**
     * @return
     */
    protected NumberVerifyListener getNumberVerifyListener()
    {
        if(mNumberVerifyListener == null) {
            mNumberVerifyListener = new NumberVerifyListener();
        }
        return mNumberVerifyListener;
    }

    protected void forceLayout(Composite composite)
    {
        Stack deferred = new Stack();
        Composite parent = composite.getParent();
        while(parent != null) {
            if(parent.isLayoutDeferred()) {
                parent.setLayoutDeferred(false);
                deferred.push(parent);
            }
            parent = parent.getParent();
        }
        composite.layout(true,true);
        while(deferred.size() > 0) {
            parent = (Composite)deferred.pop();
            parent.setLayoutDeferred(true);
        }
    }
}
