/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MasterSlaveController extends SelectionAdapter
{
    private Button mMaster;
    private HashMap mSlaves = new HashMap();
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
        for(Iterator iter=mSlaves.keySet().iterator(); iter.hasNext(); ) {
            boolean selection = mMaster.getSelection();
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
        control.setEnabled(enabled?(enabler != null?enabler.canEnable(control):enabled):enabled);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        updateSlaves();
    }
}