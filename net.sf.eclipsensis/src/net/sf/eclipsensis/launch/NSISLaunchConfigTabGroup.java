/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import org.eclipse.debug.ui.*;

public abstract class NSISLaunchConfigTabGroup extends AbstractLaunchConfigurationTabGroup
{
    public NSISLaunchConfigTabGroup()
    {
        super();
    }

    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
        setTabs(new ILaunchConfigurationTab[]{createNSISTab(), new CommonTab()});
    }
    
    protected abstract NSISTab createNSISTab();
}
