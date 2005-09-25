/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.io.File;
import java.util.Stack;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.commands.IModelCommandListener;
import net.sf.eclipsensis.installoptions.model.commands.ModelCommandEvent;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.Tool;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.*;
import org.eclipse.ui.part.EditorActionBarContributor;

public class InstallOptionsEditDomain extends DefaultEditDomain implements IAdaptable
{
    private File mFile;
    private Tool mDefaultTool = new SelectionTool();
    
    /**
     * @param editorPart
     */
    public InstallOptionsEditDomain(IEditorPart editorPart)
    {
        super(editorPart);
        setDefaultTool(mDefaultTool);
        setCommandStack(new InstallOptionsCommandStack());
    }

    public void setCommandStack(CommandStack stack)
    {
        if(stack instanceof InstallOptionsCommandStack) {
            super.setCommandStack(stack);
        }
    }

    public boolean isReadOnly()
    {
        return mFile != null && mFile.exists() && !mFile.canWrite();
    }
    
    public void setFile(File file)
    {
        mFile = file;
    }
    
    public void setActiveTool(Tool tool)
    {
        if(!isReadOnly() || tool instanceof SelectionTool) {
            super.setActiveTool(tool);
        }
        else {
            super.setActiveTool(mDefaultTool);
        }
    }
    
    public void setDefaultTool(Tool tool)
    {
        if(!isReadOnly() || tool instanceof SelectionTool) {
            super.setDefaultTool(tool);
        }
        else {
            super.setDefaultTool(mDefaultTool);
        }
    }

    private class InstallOptionsCommandStack extends CommandStack implements IModelCommandListener
    {
        private Stack mCurrentCommands = new Stack();
        
        public synchronized void execute(Command command)
        {
            if(!isReadOnly()) {
                CompoundCommand cmd = new CompoundCommand(command.getLabel());
                cmd.add(command);
                command = cmd;
                mCurrentCommands.push(command); 
                super.execute(command);
                mCurrentCommands.pop();
            }
            else {
                IEditorActionBarContributor contributor= getEditorPart().getEditorSite().getActionBarContributor();     
                if ((contributor instanceof EditorActionBarContributor)) {
                    IActionBars actionBars= ((EditorActionBarContributor) contributor).getActionBars();
                    if (actionBars != null) {
                        IStatusLineManager manager = actionBars.getStatusLineManager();
                        if(manager != null) {
                            manager.setMessage(InstallOptionsPlugin.getFormattedString("read.only.error",new Object[]{mFile.getName()})); //$NON-NLS-1$
                        }
                    }
                }
                getEditorPart().getEditorSite().getShell().getDisplay().beep();
            }
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.installoptions.model.commands.IModelCommandListener#executeModelCommand(net.sf.eclipsensis.installoptions.model.commands.ModelCommandEvent)
         */
        public void executeModelCommand(ModelCommandEvent event)
        {
            Command command = event.getCommand();
            if(command != null) {
                if(mCurrentCommands.size() > 0) {
                    CompoundCommand current = (CompoundCommand)mCurrentCommands.peek();
                    current.add(command);
                }
                else {
                    execute(command);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter)
    {
        if(adapter == IModelCommandListener.class) {
            return getCommandStack();
        }
        return null;
    }
}

