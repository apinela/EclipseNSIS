/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.*;
import java.util.Map.Entry;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;
import net.sf.eclipsensis.viewer.MapContentProvider;
import net.sf.eclipsensis.viewer.MapLabelProvider;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.*;

public class SubCommandParam extends NSISParam
{
    public static final String ATTR_COMMAND = "command"; //$NON-NLS-1$
    public static final String TAG_SUBCOMMAND = "subcommand"; //$NON-NLS-1$
    public static final String SETTING_SUBCOMMAND = "subcommand"; //$NON-NLS-1$
    
    protected Map mSubCommands;
    
    public SubCommandParam(Node node)
    {
        super(node);
    }
    
    protected void init(Node node)
    {
        super.init(node);
        loadSubCommands(node);
    }

    private void loadSubCommands(Node node)
    {
        mSubCommands = new LinkedHashMap();
        NodeList childNodes = node.getChildNodes();
        int count = childNodes.getLength();
        for(int i=0; i<count; i++) {
            Node child = childNodes.item(i);
            if(child.getNodeName().equals(TAG_SUBCOMMAND)) {
                NamedNodeMap attr = child.getAttributes();
                String command = XMLUtil.getStringValue(attr, ATTR_COMMAND);
                if(!Common.isEmpty(command)) {
                    String name = XMLUtil.getStringValue(attr, ATTR_NAME);
                    mSubCommands.put(command,(name==null?command:name));
                }
            }
        }
    }
    
    protected NSISParamEditor createParamEditor()
    {
        return new SubCommandParamEditor();
    }

    protected class SubCommandParamEditor extends NSISParamEditor
    {
        private INSISParamEditor mCommandEditor = null;
        private ComboViewer mComboViewer;
        
        protected String validateParam()
        {
            if(isSelected()) {
                if(mComboViewer != null && isValid(mComboViewer.getControl())) {
                    if(mComboViewer.getSelection().isEmpty()) {
                        return EclipseNSISPlugin.getResourceString("sub.command.param.error");  //$NON-NLS-1$
                    }
                    if(mCommandEditor != null) {
                        return mCommandEditor.validate();
                    }
                }
            }
            return null;
        }

        protected void appendParamText(StringBuffer buf)
        {
            if(mCommandEditor != null && mComboViewer != null) {
                IStructuredSelection ssel = (IStructuredSelection)mComboViewer.getSelection();
                if(!ssel.isEmpty()) {
                    if(buf.length() > 0) {
                        buf.append(" "); //$NON-NLS-1$
                    }
                    buf.append(((Map.Entry)ssel.getFirstElement()).getValue());
                    mCommandEditor.appendText(buf);
                }
            }
        }

        protected void updateState(boolean state)
        {
            super.updateState(state);
            if(mComboViewer != null) {
                if(isValid(mComboViewer.getControl())) {
                    mComboViewer.getControl().setEnabled(state);
                }
            }
            if(mCommandEditor != null) {
                mCommandEditor.setEnabled(state);
            }
        }

        public void setSettings(Map settings)
        {
            super.setSettings(settings);
            if(mCommandEditor != null) {
                if(settings != null) {
                    IStructuredSelection ssel = (IStructuredSelection)mComboViewer.getSelection();
                    if(!ssel.isEmpty()) {
                        String command = (String)((Map.Entry)ssel.getFirstElement()).getKey();
                        Map childSettings = (Map)settings.get(command);
                        if(childSettings == null) {
                            childSettings = new HashMap();
                            settings.put(command, childSettings);
                        }
                        mCommandEditor.setSettings(childSettings);
                    }                    
                }
                else {
                    mCommandEditor.setSettings(null);
                }
            }
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(getSettings() != null) {
                if(mComboViewer != null) {
                    if(isValid(mComboViewer.getControl())) {
                        IStructuredSelection ssel = (IStructuredSelection)mComboViewer.getSelection();
                        if(!ssel.isEmpty()) {
                            getSettings().put(SETTING_SUBCOMMAND, ((Map.Entry)ssel.getFirstElement()).getKey());
                        }
                    }
                }
                if(mCommandEditor != null) {
                    mCommandEditor.saveSettings();
                }
            }
        }

        protected Control createParamControl(Composite parent)
        {
            final Composite container = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 0;
            container.setLayout(layout);
            
            Combo combo = new Combo(container,SWT.READ_ONLY|SWT.DROP_DOWN);
            combo.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false));
            
            mComboViewer = new ComboViewer(combo);
            mComboViewer.setContentProvider(new MapContentProvider());
            mComboViewer.setLabelProvider(new MapLabelProvider());
            
            mComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    boolean changed = false;
                    if (mCommandEditor != null) {
                        mCommandEditor.getControl().dispose();
                        changed = true;
                    }
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    if(!sel.isEmpty()) {
                        String commandName = (String)((Map.Entry)sel.getFirstElement()).getKey();
                        NSISCommand cmd = NSISCommandManager.getCommand(commandName);
                        if (cmd != null) {
                            mCommandEditor = cmd.createEditor();
                            Map commandSettings = (Map)getSettings().get(commandName);
                            if(commandSettings == null) {
                                commandSettings = new HashMap();
                                getSettings().put(commandName,commandSettings);
                            }
                            mCommandEditor.setSettings(commandSettings);
                            Control c = mCommandEditor.createControl(container);
                            c.setLayoutData(new GridData(SWT.FILL, (c instanceof Composite?SWT.FILL:SWT.CENTER), true, true));
                            mCommandEditor.initEditor();
                            changed = true;
                        }
                    }
                    if (changed) {
                        container.layout(true);
                        Shell shell = container.getShell();
                        Point size = shell.getSize();
                        shell.setSize(size.x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                    }                    
                }
            });
            
            mComboViewer.setInput(mSubCommands);
            return container;
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            if(mComboViewer != null && isValid(mComboViewer.getControl())) {
                String commandName = (String)getSettingValue(SETTING_SUBCOMMAND, String.class, null);
                for (Iterator iter = mSubCommands.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Entry)iter.next();
                    if(entry.getKey().equals(commandName)) {
                        mComboViewer.setSelection(new StructuredSelection(entry));
                        break;
                    }
                }
            }
            if(mCommandEditor != null) {
                mCommandEditor.initEditor();
            }
        }
    }
}
