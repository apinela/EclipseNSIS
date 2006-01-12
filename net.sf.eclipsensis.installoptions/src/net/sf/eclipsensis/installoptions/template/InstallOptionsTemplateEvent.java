/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

public class InstallOptionsTemplateEvent
{
    public static final int TEMPLATE_ADDED = 0;
    public static final int TEMPLATE_REMOVED = 1;
    public static final int TEMPLATE_UPDATED = 2;

    private int mType;
    private InstallOptionsTemplate mOldTemplate;
    private InstallOptionsTemplate mNewTemplate;

    public InstallOptionsTemplateEvent(int type, InstallOptionsTemplate oldTemplate, InstallOptionsTemplate newTemplate)
    {
        mType = type;
        mOldTemplate = oldTemplate;
        mNewTemplate = newTemplate;
    }

    public InstallOptionsTemplate getOldTemplate()
    {
        return mOldTemplate;
    }

    public InstallOptionsTemplate getNewTemplate()
    {
        return mNewTemplate;
    }

    public int getType()
    {
        return mType;
    }
}
