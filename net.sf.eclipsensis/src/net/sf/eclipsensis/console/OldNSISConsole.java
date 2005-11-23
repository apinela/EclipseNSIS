/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

//TODO Remove in 0.9.5
/**
 * @deprecated
 */
public class OldNSISConsole extends ViewPart

{
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayout(new GridLayout(1,false));
        composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        Link link = new Link(composite,SWT.NONE);
        link.setBackground(link.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        link.setText("The EclipseNSIS console view has been deprecated. The console is now available as part of the standard Eclipse console. Click <a href=\"\">here</a> to close this view and switch to the EclipseNSIS console. This view will be removed in the next release of EclipseNSIS."); //$NON-NLS-1$
        link.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IViewPart view = activePage.findView(INSISConstants.CONSOLE_ID);
                        if(view != null) {
                            activePage.hideView(view);
                        }
                        NSISConsoleFactory.showConsole();
                    }
                });
            }
        });
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
    {
	}
}