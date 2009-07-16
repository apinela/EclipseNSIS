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

import java.util.*;

public class InstallOptionsWidgetList
{
    private List<InstallOptionsWidget> mWidgets = new ArrayList<InstallOptionsWidget>();

    public void add(InstallOptionsWidget widget)
    {
        widget = (InstallOptionsWidget)widget.clone();
        widget.setIndex(mWidgets.size());
        widget.setDirty(false);
        mWidgets.add(widget);
    }

    public InstallOptionsWidget[] getWidgets()
    {
        return mWidgets.toArray(new InstallOptionsWidget[mWidgets.size()]);
    }
}
