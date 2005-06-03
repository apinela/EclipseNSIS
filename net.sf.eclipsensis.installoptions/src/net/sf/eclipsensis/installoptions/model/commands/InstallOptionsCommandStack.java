/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import java.util.Stack;

import org.eclipse.gef.commands.*;

public class InstallOptionsCommandStack extends CommandStack implements IModelCommandListener
{
    private Stack mCurrentCommands = new Stack();
    
    public synchronized void execute(Command command)
    {
        CompoundCommand cmd = new CompoundCommand(command.getLabel());
        cmd.add(command);
        command = cmd;
        mCurrentCommands.push(command); 
        super.execute(command);
        mCurrentCommands.pop(); 
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
