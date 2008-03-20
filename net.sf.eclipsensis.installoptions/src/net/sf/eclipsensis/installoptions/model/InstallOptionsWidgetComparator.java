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

import java.util.Comparator;

public final class InstallOptionsWidgetComparator implements Comparator
{
    public static final Comparator INSTANCE = new InstallOptionsWidgetComparator(false);
    public static final Comparator REVERSE_INSTANCE = new InstallOptionsWidgetComparator(true);

    private boolean mReversed = false;

    public InstallOptionsWidgetComparator(boolean reversed)
    {
        super();
        mReversed = reversed;
    }

    public int compare(Object o1, Object o2)
    {
        InstallOptionsWidget w1 = (InstallOptionsWidget)o1;
        InstallOptionsWidget w2 = (InstallOptionsWidget)o2;
        return (mReversed?-1:1)*(w1.getIndex()-w2.getIndex());
    }
}