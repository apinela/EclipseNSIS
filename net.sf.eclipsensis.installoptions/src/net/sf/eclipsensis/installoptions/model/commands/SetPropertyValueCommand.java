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

import java.text.MessageFormat;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.ui.views.properties.IPropertySource;

public class SetPropertyValueCommand extends Command
{
    protected Object mPropertyValue;
    protected Object mPropertyName;
    protected Object mUndoValue;
    protected boolean mResetOnUndo;
    protected IPropertySource mTarget;

    public SetPropertyValueCommand() 
    {
        super(""); //$NON-NLS-1$
    }

    public SetPropertyValueCommand(String propLabel) 
    {
        super(MessageFormat.format(GEFMessages.SetPropertyValueCommand_Label, new Object[]{propLabel}).trim());
    }

    public boolean canExecute() 
    {
        return true;
    }

    public void execute() 
    {
        boolean wasPropertySet = getTarget().isPropertySet(mPropertyName);
        mUndoValue = getTarget().getPropertyValue(mPropertyName);
        if (mUndoValue instanceof IPropertySource) {
            mUndoValue = ((IPropertySource)mUndoValue).getEditableValue();
        }
        if (mPropertyValue instanceof IPropertySource) {
            mPropertyValue = ((IPropertySource)mPropertyValue).getEditableValue();
        }
        getTarget().setPropertyValue(mPropertyName, mPropertyValue);
        mResetOnUndo = wasPropertySet != getTarget().isPropertySet(mPropertyName);
        if (mResetOnUndo) {
            mUndoValue = null;
        }
    }

    public IPropertySource getTarget() 
    {
        return mTarget;
    }

    public void setTarget(IPropertySource aTarget) 
    {
        mTarget = aTarget;
    }

    public void redo() 
    {
        execute();
    }

    public void setPropertyId(Object pName) 
    {
        mPropertyName = pName;
    }

    public void setPropertyValue(Object val) 
    {
        mPropertyValue = val;
    }

    public void undo() 
    {
        if (mResetOnUndo) {
            getTarget().resetPropertyValue(mPropertyName);
        }
        else {
            getTarget().setPropertyValue(mPropertyName, mUndoValue);
        }
    }
}