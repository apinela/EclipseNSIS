/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dnd;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsEditDomain;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.dnd.TransferData;

public class InstallOptionsTreeViewerTransfer extends InstallOptionsObjectTransfer
{
    public static final InstallOptionsTreeViewerTransfer INSTANCE = new InstallOptionsTreeViewerTransfer();
    private static final String[] TYPE_NAMES = new String[]{"Local Transfer"//$NON-NLS-1$
                                                            + System.currentTimeMillis()
                                                            + ":" + INSTANCE.hashCode()};//$NON-NLS-1$
    private static final int[] TYPE_IDS = new int[] {registerType(TYPE_NAMES[0])};

    private static EditPartViewer viewer;

    private InstallOptionsTreeViewerTransfer() 
    {
    }

    protected int[] getTypeIds() 
    {
        return TYPE_IDS;
    }

    protected String[] getTypeNames() 
    {
        return TYPE_NAMES;
    }

    public EditPartViewer getViewer() 
    {
        return viewer;
    }

    public void setViewer(EditPartViewer epv) 
    {
        viewer = epv;
    }
    
    public boolean isSupportedType(TransferData transferData)
    {
        EditDomain domain = getViewer().getEditDomain();
        if(domain instanceof InstallOptionsEditDomain && ((InstallOptionsEditDomain)domain).isReadOnly()) {
            return false;
        }
        return super.isSupportedType(transferData);
    }
}
