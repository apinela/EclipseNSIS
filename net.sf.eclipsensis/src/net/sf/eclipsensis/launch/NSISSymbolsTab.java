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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.*;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

class NSISSymbolsTab extends NSISTab implements INSISSettingsEditorPageListener
{
    static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.filtername")}; //$NON-NLS-1$
 
    protected NSISSettingsEditorPage createPage()
    {
        return new NSISSettingsEditorSymbolsPage(mSettings) {
            protected ControlAdapter createTableControlListener()
            {
                return NSISSymbolsTab.this.createTableControlListener(super.createTableControlListener());
            }
        };
    }

    protected IFilter createSettingsFilter()
    {
        return new IFilter() {
            public boolean select(Object toTest)
            {
                return INSISPreferenceConstants.SYMBOLS.equals(toTest);
            }
        };
    }
    
    public Image getImage() 
    {
        //FIXME change image
        return EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("nsis.symbols.tab.icon")); //$NON-NLS-1$
    }

    public void createControl(Composite parent)
    {
        super.createControl(parent);
        //FIXME change help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_launchconfig_nsis_context"); //$NON-NLS-1$
    }

    public String getName()
    {
        //FIXME change name
        return EclipseNSISPlugin.getResourceString("launchconfig.symbols.tab.name"); //$NON-NLS-1$
    }
}