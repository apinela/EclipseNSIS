/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.*;
import org.eclipse.ui.keys.KeySequence;


public class NSISInformationControlCreator implements IInformationControlCreator,INSISConstants //,ICommandListener
{
    private ICommand[] mCommands = null;
    private int mStyle = SWT.NULL;
    private DefaultInformationControl.IInformationPresenter mInformationPresenter =
        new DefaultInformationControl.IInformationPresenter()
        {
            public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight)
            {
                hoverInfo = hoverInfo.trim();
                int n = hoverInfo.indexOf(' ');
                if(n <= 0) {
                    n = hoverInfo.length();
                }
                presentation.addStyleRange(new StyleRange(0,n,
                                                display.getSystemColor(SWT.COLOR_INFO_FOREGROUND),
                                                display.getSystemColor(SWT.COLOR_INFO_BACKGROUND),
                                                SWT.BOLD));
                return hoverInfo;
            }
        };

    public NSISInformationControlCreator(String[] commandIds)
    {
        this(commandIds,SWT.NULL);
    }
    
    public NSISInformationControlCreator(String[] commandIds, int style)
    {
        ArrayList list = new ArrayList();
        if(!Common.isEmptyArray(commandIds)) {
            ICommandManager commandManager= PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
            for(int i=0; i<commandIds.length; i++) {
                ICommand command = commandManager.getCommand(commandIds[i]);
                if(command != null && !list.contains(command)) {
                    list.add(command);
                }
            }
        }
        mCommands = (ICommand[])list.toArray(new ICommand[0]);
        mStyle = style;
    }

    private String buildStatusText()
    {
        String statusText = null;
        if(!Common.isEmptyArray(mCommands)) {
            ArrayList params = new ArrayList();
            for (int i = 0; i < mCommands.length; i++) {
                List list= mCommands[i].getKeySequenceBindings();
                if (!list.isEmpty()) {
                    KeySequence keySequence= ((IKeySequenceBinding)list.get(0)).getKeySequence();
                    try {
                        String keyText = keySequence.format();
                        String description = mCommands[i].getDescription();
                        params.add(keyText);
                        params.add(description);
                    }
                    catch(NotDefinedException nde) {
                    }
                }       
            }
            if(params.size() > 0) {
                String format = EclipseNSISPlugin.getResourceString("information.status.format."+params.size()/2);
                if(!Common.isEmpty(format)) {
                    statusText = MessageFormat.format(format, params.toArray());
                }
            }
        }
        
        return statusText;
    }

    public IInformationControl createInformationControl(Shell parent)
    {
        DefaultInformationControl informationControl = new DefaultInformationControl(parent,mStyle,mInformationPresenter,buildStatusText());
        return informationControl;
    }
}