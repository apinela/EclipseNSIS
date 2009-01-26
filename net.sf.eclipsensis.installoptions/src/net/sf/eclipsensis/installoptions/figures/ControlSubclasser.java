/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.swt.events.*;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.widgets.*;

/*
 * The use of the Callback class is, strictly speaking, verboten.
 * However, we do what we have to do...
 */
public class ControlSubclasser
{
    private static int cNewProc;
    private static Map cProcMap = new HashMap(101);

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

    private ControlSubclasser()
    {
    }

    public static void subclassControl(Control control, SWTControlFigure figure)
    {
        final int handle = control.handle;
        final int oldProc = WinAPI.GetWindowLong(handle, WinAPI.GWL_WNDPROC);
        final Integer handleInteger = new Integer(handle);
        cProcMap.put(handleInteger,new ControlInfo(oldProc, figure));
        WinAPI.SetWindowLong(handle, WinAPI.GWL_WNDPROC, cNewProc);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                WinAPI.SetWindowLong(handle, WinAPI.GWL_WNDPROC, oldProc);
                cProcMap.remove(handleInteger);
            }
        });
    }

    private static class SubclassCallback
    {
        public SubclassCallback()
        {
        }

        public int windowProc(int hWnd, int msg, int wParam, int lParam)
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
                        res=WinAPI.CallWindowProc(((ControlInfo)cProcMap.get(new Integer(hWnd))).oldProc,
                                                  hWnd, msg, wParam, lParam);
                    }
                    catch(Throwable t) {
                        InstallOptionsPlugin.getDefault().log(t);
                        res = 0;
                    }
            }

            return res;
        }
    }

    private static class ControlInfo
    {
        int oldProc;
        SWTControlFigure figure;

        public ControlInfo(int oldProc, SWTControlFigure figure)
        {
            super();
            this.oldProc = oldProc;
            this.figure = figure;
        }
    }
}