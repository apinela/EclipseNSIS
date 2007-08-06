/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.List;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.*;

public class InstallOptionsDropList extends InstallOptionsCombobox
{
    protected InstallOptionsDropList(INISection section)
    {
        super(section);
    }

    public String getType()
    {
        return InstallOptionsModel.TYPE_DROPLIST;
    }

    protected boolean isStateReadOnly()
    {
        return true;
    }

    public void setListItems(List listItems)
    {
        super.setListItems(listItems);
        String oldState = getState();
        String newState = (!new CaseInsensitiveSet(getListItems()).contains(oldState)?"":oldState); //$NON-NLS-1$
        if(!Common.stringsAreEqual(oldState,newState)) {
            fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE,newState));
        }
    }
}

