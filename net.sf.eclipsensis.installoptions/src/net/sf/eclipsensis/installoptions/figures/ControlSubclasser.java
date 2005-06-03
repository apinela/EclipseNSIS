/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.HashMap;

import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/*
 * The use of the Callback class is, strictly speaking, verboten.
 * However, we do what we have to do...
 */
public class ControlSubclasser
{
    private static int cNewProc;
    private static HashMap cProcMap = new HashMap(101);
    
    int mOldProc, mNewProc;
    int mControlHandle;
    
    static {
        SubclassCallback subCallback = new SubclassCallback();
        final Callback callback = new Callback(subCallback,"windowProc",4); //$NON-NLS-1$
        Display.getDefault().disposeExec(new Runnable() {
           public void run()
           {
               callback.dispose();
           }
        });
        cNewProc = callback.getAddress();
    }
    
    public static void subclassControl(Control control)
    {
        final int handle = control.handle;
        final int oldProc = WinAPI.GetWindowLong(handle, WinAPI.GWL_WNDPROC);
        final Integer handleInteger = new Integer(handle);
        cProcMap.put(handleInteger,new Integer(oldProc));
        WinAPI.SetWindowLong(handle, WinAPI.GWL_WNDPROC, cNewProc);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                WinAPI.SetWindowLong(handle, WinAPI.GWL_WNDPROC, oldProc);
                cProcMap.remove(handleInteger);
            }
        });
    }
    
    public static class SubclassCallback
    {
        private SubclassCallback()
        {
        }
        
        public int windowProc(int hwnd, int msg, int wParam, int lParam)
        {
            int res;
            
            switch (msg)
            {
                case WinAPI.WM_NCHITTEST:
                    res = WinAPI.HTTRANSPARENT;
                    break;
                case WinAPI.WM_SETFOCUS:
                case WinAPI.WM_KEYDOWN:
                case WinAPI.WM_CHAR:
                case WinAPI.WM_SYSCHAR:
                    res = 0;
                    break;
                default:
                    try {
                        res=WinAPI.CallWindowProc(((Integer)cProcMap.get(new Integer(hwnd))).intValue(),
                                                  hwnd, msg, wParam, lParam);
                    }
                    catch(Throwable t) {
                        //Ignore any errors
                        res = 0;
                    }
            }
            
            return res;
        }
    }
}