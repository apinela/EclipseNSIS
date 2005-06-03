/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import java.util.*;

import net.sf.eclipsensis.installoptions.properties.descriptors.CustomPropertyDescriptor;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.internal.ui.properties.PropertySheetEntry;
import org.eclipse.gef.internal.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class CustomPropertySheetEntry extends UndoablePropertySheetEntry
{
    /**
     * 
     */
    public CustomPropertySheetEntry()
    {
        super();
    }

    /**
     * @param stack
     */
    public CustomPropertySheetEntry(CommandStack stack)
    {
        super(stack);
    }

    protected PropertySheetEntry createChildEntry()
    {
        return new CustomPropertySheetEntry();
    }

    protected List computeMergedPropertyDescriptors()
    {
        List list = super.computeMergedPropertyDescriptors();
        ArrayList list2 = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            IPropertyDescriptor element = (IPropertyDescriptor)iter.next();
            if(element instanceof CustomPropertyDescriptor) {
                list2.add(((CustomPropertyDescriptor)element).getDelegate());
            }
            else {
                list2.add(element);
            }
        }
        return list2;
    }
}
