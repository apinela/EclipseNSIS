/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.gef.commands.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertySheetEntry;

public class InstallOptionsPropertySheetEntry extends PropertySheetEntry
{
    private CommandStackListener mCommandStackListener;

    private CommandStack mStack;
    
    private InstallOptionsPropertySheetEntry()
    {
    }

    public InstallOptionsPropertySheetEntry(CommandStack stack) 
    {
        setCommandStack(stack);
    }

    /**
     * @see org.eclipse.ui.views.properties.PropertySheetEntry#createChildEntry()
     */
    protected PropertySheetEntry createChildEntry() 
    {
        return new InstallOptionsPropertySheetEntry();
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#dispose()
     */
    public void dispose() 
    {
        if (mStack != null) {
            mStack.removeCommandStackListener(mCommandStackListener);
        }
        super.dispose();
    }

    CommandStack getCommandStack() 
    {
        //only the root has, and is listening too, the command stack
        if (getParent() != null) {
            return ((InstallOptionsPropertySheetEntry)getParent()).getCommandStack();
        }
        return mStack;
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySheetEntry#resetPropertyValue()
     */
    public void resetPropertyValue() 
    {
        CompoundCommand cc = new CompoundCommand();
        ResetValueCommand restoreCmd;

        if (getParent() == null) {
            // root does not have a default value
            return;
        }

        //  Use our parent's values to reset our values.
        boolean change = false;
        Object[] objects = getParent().getValues();
        for (int i = 0; i < objects.length; i++) {
            IPropertySource source = getPropertySource(objects[i]);
            if (source.isPropertySet(getDescriptor().getId())) {
                //source.resetPropertyValue(getDescriptor()getId());
                restoreCmd = new ResetValueCommand();
                restoreCmd.setTarget(source);
                restoreCmd.setPropertyId(getDescriptor().getId());
                cc.add(restoreCmd);         
                change = true;
            }
        }
        if (change) {
            getCommandStack().execute(cc);
            refreshFromRoot();
        }
    }

    void setCommandStack(CommandStack stack) 
    {
        mStack = stack;
        mCommandStackListener = new CommandStackListener() {
            public void commandStackChanged(EventObject e) {
                refreshFromRoot();
            }
        };
        stack.addCommandStackListener(mCommandStackListener);
    }

    /**
     * @see PropertySheetEntry#valueChanged(PropertySheetEntry)
     */
    protected void valueChanged(PropertySheetEntry child) 
    {
        valueChanged((InstallOptionsPropertySheetEntry)child, new ForwardUndoCompoundCommand());
    }

    protected Command createChangeTypeCommand(IPropertySource target, String displayName, Object propertyId, Object value)
    {
        if(target instanceof InstallOptionsWidget) {
            InstallOptionsWidget oldChild = (InstallOptionsWidget)target;
            if(oldChild.getParent() != null) {
                String oldType = oldChild.getType();
                String newType = (String)value;
                if(!Common.stringsAreEqual(oldType, newType)) {
                    InstallOptionsElementFactory oldFactory = InstallOptionsElementFactory.getFactory(oldType);
                    InstallOptionsElementFactory newFactory = InstallOptionsElementFactory.getFactory(newType);
                    if(!oldFactory.getObjectType().equals(newFactory.getObjectType())) {
                        INISection oldSection = oldChild.updateSection();
                        final INISection section = (INISection)oldSection.copy();
                        INIKeyValue[] keyValues = section.findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                        if(!Common.isEmptyArray(keyValues)) {
                            keyValues[0].setValue(newType);
                        }
                        else {
                            INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_TYPE);
                            keyValue.setValue(newType);
                            section.addChild(keyValue);
                        }
                        section.validate(INILine.VALIDATE_FIX_ERRORS);
                        if(section.hasErrors()) {
                            Display.getDefault().syncExec(new Runnable() {
                                public void run()
                                {
                                    ListDialog dialog = new ListDialog(Display.getCurrent().getActiveShell()) {
                                        protected int getTableStyle()
                                        {
                                            return super.getTableStyle() | SWT.READ_ONLY;
                                        }
                                    };
                                    dialog.setAddCancelButton(false);
                                    ArrayList list = new ArrayList();
                                    list.addAll(section.getErrors());
                                    int maxChars = 0;
                                    for(Iterator iter=section.getChildren().iterator(); iter.hasNext(); ) {
                                        List errors = ((INILine)iter.next()).getErrors();
                                        for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                                            String error = (String)iterator.next();
                                            maxChars = Math.max(maxChars, (error==null?0:error.length()));
                                            list.add(error);
                                        }
                                    }
                                    dialog.setWidthInChars(maxChars);
                                    dialog.setHeightInChars(list.size());
                                    dialog.setContentProvider(new CollectionContentProvider());
                                    dialog.setLabelProvider(new LabelProvider());
                                    dialog.setTitle(EclipseNSISPlugin.getResourceString("error.title")); //$NON-NLS-1$
                                    dialog.setMessage("Control type cannot be changed because of unrecoverable errors.");
                                    dialog.setInput(list);
                                    dialog.open();
                                }
                            });
                            return null;
                        }
                        InstallOptionsWidget newChild = (InstallOptionsWidget)newFactory.getNewObject(section);
                        ChangeTypeCommand cmd = null;
                        cmd = new ChangeTypeCommand();
                        cmd.setParent(oldChild.getParent());
                        cmd.setChild(oldChild);
                        cmd.setNewChild(newChild);
                        return cmd;
                    }
                }
            }
            else {
                return null;
            }
        }
        return createSetValueCommand(target, displayName, propertyId, value);
    }

    protected Command createSetValueCommand(IPropertySource target, String displayName, Object propertyId, Object value)
    {
        SetValueCommand setCommand = new SetValueCommand(displayName);
        setCommand.setTarget(target);
        setCommand.setPropertyId(propertyId);
        setCommand.setPropertyValue(value);
        return setCommand;
    }

    void valueChanged(InstallOptionsPropertySheetEntry child, CompoundCommand command) 
    {
        CompoundCommand cc = new CompoundCommand();
        command.add(cc);

        Object propertyId = child.getDescriptor().getId();

        for (int i = 0; i < getValues().length; i++) {
            IPropertySource target = getPropertySource(getValues()[i]);
            if(InstallOptionsModel.PROPERTY_TYPE.equals(propertyId)) {
                cc.add(createChangeTypeCommand(target, child.getDisplayName(), propertyId, child.getValues()[i]));
            }
            else {
                cc.add(createSetValueCommand(target, child.getDisplayName(), propertyId, child.getValues()[i]));
            }
        }

        // inform our parent
        if (getParent() != null) {
            ((InstallOptionsPropertySheetEntry)getParent()).valueChanged(this, command);
        }
        else {
            //I am the root entry
            mStack.execute(command);
        }
    }
}