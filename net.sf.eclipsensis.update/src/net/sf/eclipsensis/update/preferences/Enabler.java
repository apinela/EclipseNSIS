/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

class Enabler
{
    private static Map cEnablers = new HashMap();

    private Button mButton;
    private Control[] mDependents;
    
    public Enabler(Button button, Control[] dependents)
    {
        mButton = button;
        mDependents = dependents;
        mButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                run();
            }
        });
        
        cEnablers.put(mButton, this);
        mButton.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                cEnablers.remove(mButton);
            }
        });
    }
    
    public void run()
    {
        boolean selected = mButton.getSelection();
        for (int i = 0; i < mDependents.length; i++) {
            mDependents[i].setEnabled(selected);
            if(mDependents[i] instanceof Button) {
                Enabler enabler = get(mDependents[i]);
                if(enabler != null) {
                    enabler.run();
                }
            }
        }
    }

    public static Enabler get(Control control)
    {
        return (Enabler)cEnablers.get(control);
    }
}