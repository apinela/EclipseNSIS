/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import java.util.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class MasterSlaveController extends SelectionAdapter
{
    private Button mMaster;
    private Map mSlaves = new HashMap();
    private boolean mIsReverse = false;

    public MasterSlaveController(Button button)
    {
        this(button,false);
    }

    public MasterSlaveController(Button button, boolean isReverse)
    {
        mMaster = button;
        mIsReverse = isReverse;
        mMaster.addSelectionListener(this);
        mMaster.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        mMaster.removeSelectionListener(MasterSlaveController.this);
                        mSlaves.clear();
                        mMaster = null;
                    }
                });
    }

    public void addSlave(Control control)
    {
        mSlaves.put(control, null);
    }

    public void addSlave(Control control, MasterSlaveEnabler enabler)
    {
        mSlaves.put(control, enabler);
    }

    public void setEnabler(Control control, MasterSlaveEnabler enabler)
    {
        if(mSlaves.containsKey(control)) {
            mSlaves.put(control, enabler);
        }
    }

    public void removeSlave(Control control)
    {
        mSlaves.remove(control);
    }

    public void updateSlaves()
    {
        updateSlaves(mMaster.getSelection());
    }

    public void updateSlaves(boolean hint)
    {
        updateSlavesInternal(mMaster.getSelection() && hint);
    }

    /**
     * @param selection
     */
    private void updateSlavesInternal(boolean selection)
    {
        for(Iterator iter=mSlaves.keySet().iterator(); iter.hasNext(); ) {
            Control slave = (Control)iter.next();
            MasterSlaveEnabler enabler = (MasterSlaveEnabler)mSlaves.get(slave);
            recursiveSetEnabled(slave, (mIsReverse?!selection:selection), enabler);
        }
    }

    private void recursiveSetEnabled(Control control, boolean enabled, MasterSlaveEnabler enabler)
    {
        if(control instanceof Composite) {
            Control[] children = ((Composite)control).getChildren();
            for (int i = 0; i < children.length; i++) {
                recursiveSetEnabled(children[i],enabled, enabler);
            }
        }
        enabled = enabled?(enabler != null?enabler.canEnable(control):enabled):enabled;
        control.setEnabled(enabled);
        if(enabler != null) {
            enabler.enabled(control,enabled);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        updateSlaves();
    }
}