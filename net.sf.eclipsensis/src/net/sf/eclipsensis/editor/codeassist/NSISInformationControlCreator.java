/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.text.MessageFormat;
import java.util.ArrayList;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;

public class NSISInformationControlCreator implements IInformationControlCreator,INSISConstants //,ICommandListener
{
    private ParameterizedCommand[] mCommands = null;
    private int mStyle = SWT.NONE;
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
        this(commandIds,SWT.NONE);
    }
    
    public NSISInformationControlCreator(String[] commandIds, int style)
    {
        ArrayList list = new ArrayList();
        if(!Common.isEmptyArray(commandIds)) {
            ICommandService commandService= (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);
            for(int i=0; i<commandIds.length; i++) {
                Command command = commandService.getCommand(commandIds[i]);
                if(command != null) {
                    ParameterizedCommand pc = new ParameterizedCommand(command, null);
                    if(!list.contains(pc)) {
                        list.add(pc);
                    }
                }
            }
        }
        mCommands = (ParameterizedCommand[])list.toArray(new ParameterizedCommand[list.size()]);
        mStyle = style;
    }

    private String buildStatusText()
    {
        String statusText = null;
        if(!Common.isEmptyArray(mCommands)) {
            IBindingService bindingService = (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
            ArrayList params = new ArrayList();
            for (int i = 0; i < mCommands.length; i++) {
                TriggerSequence[] sequences = bindingService.getActiveBindingsFor(mCommands[i]);
                if (!Common.isEmptyArray(sequences)) {
                    for (int j = 0; j < sequences.length; j++) {
                        if(sequences[j] instanceof KeySequence) {
                            KeySequence keySequence= (KeySequence)sequences[j];
                            try {
                                String keyText = keySequence.format();
                                String description = mCommands[i].getCommand().getDescription();
                                params.add(keyText);
                                params.add(description);
                            }
                            catch(Exception e) {
                            }
                        }
                    }
                }       
            }
            if(params.size() > 0) {
                String format = EclipseNSISPlugin.getResourceString("information.status.format."+params.size()/2); //$NON-NLS-1$
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