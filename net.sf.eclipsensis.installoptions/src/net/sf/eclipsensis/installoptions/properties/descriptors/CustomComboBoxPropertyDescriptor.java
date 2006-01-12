/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.descriptors;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;

public class CustomComboBoxPropertyDescriptor extends ComboBoxPropertyDescriptor
{
    private String[] mData;
    private String[] mDisplay;
    private int mDefault;

    public CustomComboBoxPropertyDescriptor(String id, String displayName, String[] data, String[] display, int default1)
    {
        super(id, displayName, new String[0]);
        mData = data;
        mDisplay = display;
        mDefault = default1;

        setLabelProvider(new LabelProvider(){
            public String getText(Object element)
            {
                if(element instanceof String) {
                    for(int i=0; i<mData.length; i++) {
                        if(mData[i].equals(element)) {
                            return mDisplay[i];
                        }
                    }
                    return mDisplay[mDefault];
                }
                return super.getText(element);
            }
        });
    }

    public CellEditor createPropertyEditor(Composite parent)
    {
        return new ComboBoxCellEditor(parent,mDisplay,SWT.READ_ONLY) {
            protected Object doGetValue()
            {
                Integer i = (Integer)super.doGetValue();
                return mData[i.intValue()];
            }
    
            protected void doSetValue(Object value)
            {
                int val = mDefault;
                for(int i=0; i<mData.length; i++) {
                    if(mData[i].equals(value)) {
                        val = i;
                        break;
                    }
                }
                super.doSetValue(new Integer(val));
            }
        };
    }
}