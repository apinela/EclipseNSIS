/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.debug.ui.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class NSISLaunchConfigTabGroup extends AbstractLaunchConfigurationTabGroup
{
    public NSISLaunchConfigTabGroup()
    {
        super();
    }

    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
        CommonTab commonTab = new CommonTab() {
            public void createControl(Composite parent)
            {
                super.createControl(parent);
                PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_launchconfig_common_context"); //$NON-NLS-1$
            }
        };
        setTabs(new ILaunchConfigurationTab[]{new NSISGeneralTab(), new NSISSymbolsTab(false), commonTab});
    }
}
