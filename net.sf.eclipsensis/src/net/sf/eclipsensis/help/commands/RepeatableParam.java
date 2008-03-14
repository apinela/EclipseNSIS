/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class RepeatableParam extends NSISParam
{
    public static final String ATTR_LABEL="label"; //$NON-NLS-1$
    public static final String SETTING_CHILD_SETTINGS = "childSettings"; //$NON-NLS-1$
    private static MessageFormat cAddFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("add.repeatable.param.format")); //$NON-NLS-1$
    private static MessageFormat cRemoveFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("remove.repeatable.param.format")); //$NON-NLS-1$

    private String mLabel;
    private NSISParam mChildParam;

    public RepeatableParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        super.init(node);
        mLabel = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(node.getAttributes(), ATTR_LABEL), ""); //$NON-NLS-1$
        mChildParam = loadChildParam(node);
    }

    protected NSISParam loadChildParam(Node node)
    {
        Node child = XMLUtil.findFirstChild(node, TAG_PARAM);
        if(child != null) {
            return NSISCommandManager.createParam(child);
        }
        return null;
    }

    protected NSISParamEditor createParamEditor(INSISParamEditor parentEditor)
    {
        return new RepeatableParamEditor(parentEditor);
    }

    protected class RepeatableParamEditor extends NSISParamEditor
    {
        public static final String DATA_PARENT = "PARENT"; //$NON-NLS-1$
        public static final String DATA_BUTTONS = "BUTTONS"; //$NON-NLS-1$
        private List mChildParamEditors = new ArrayList();

        public RepeatableParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void clear()
        {
            int n = mChildParamEditors.size();
            if (n > 1) {
                for (int i = 1; i < n; i++) {
                    disposeEditor((INSISParamEditor)mChildParamEditors.remove(1));
                }
                updateControl((Composite)getControl());
            }
            ((INSISParamEditor)mChildParamEditors.get(0)).clear();
            super.clear();
        }

        /**
         * @param editor
         */
        private void disposeEditor(INSISParamEditor editor)
        {
            if(editor != null) {
                Control ctrl = editor.getControl();
                if(Common.isValid(ctrl)) {
                    Composite composite = (Composite)ctrl.getData(DATA_PARENT);
                    if(Common.isValid(composite)) {
                        composite.dispose();
                    }
                }
                editor.dispose();
            }
        }

        protected String validateParam()
        {
            String error = null;
            for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                error = ((INSISParamEditor)iter.next()).validate();
                if(error != null) {
                    break;
                }
            }
            return error;
        }

        protected void appendParamText(StringBuffer buf)
        {
            for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                ((INSISParamEditor)iter.next()).appendText(buf);
            }
        }

        public void setSettings(Map settings)
        {
            super.setSettings(settings);
            if(settings != null) {
                if(mChildParamEditors.size() > 0) {
                    List childSettingsList = getChildSettingsList();
                    for(ListIterator iter1 = mChildParamEditors.listIterator(), iter2 = childSettingsList.listIterator();iter1.hasNext() || iter2.hasNext();) {
                        INSISParamEditor editor = null;
                        Map childSettings = null;
                        if(iter1.hasNext()) {
                            editor = (INSISParamEditor)iter1.next();
                        }
                        if(iter2.hasNext()) {
                            childSettings = (Map)iter2.next();
                        }
                        if(editor != null) {
                            if(childSettings == null) {
                                childSettings = new HashMap();
                            }
                            editor.setSettings(childSettings);
                        }
                    }
                }
            }
            else {
                for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                    ((INSISParamEditor)iter.next()).setSettings(null);
                }
            }
        }

        public void saveSettings()
        {
            List childSettingsList = getChildSettingsList();
            if(childSettingsList != null) {
                childSettingsList.clear();
                if(mChildParamEditors.size() > 0) {
                    for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                        childSettingsList.add(((INSISParamEditor)iter.next()).getSettings());
                    }
                }
                getSettings().put(SETTING_CHILD_SETTINGS, childSettingsList);
            }
            super.saveSettings();
        }

        private List getChildSettingsList()
        {
            List childSettingsList;
            if(getSettings() != null) {
                childSettingsList = (List)getSettingValue(SETTING_CHILD_SETTINGS, List.class, null);
                if(childSettingsList == null) {
                    childSettingsList = new ArrayList();
                }
            }
            else {
                childSettingsList = null;
            }
            return childSettingsList;
        }

        public void reset()
        {
            super.reset();
            if (mChildParamEditors.size() > 0) {
                for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = (INSISParamEditor)iter.next();
                    editor.reset();
                    disposeEditor(editor);
                    iter.remove();
                }
                updateControl((Composite)getControl());
            }
        }

        protected void initParamEditor()
        {
            super.initParamEditor();

            if(getSettings() != null) {
                List childSettingsList = getChildSettingsList();
                if(childSettingsList.size() == 0) {
                    createChildParamEditor(0, new HashMap());
                }
                else {
                    int i=0;
                    for (Iterator iter = childSettingsList.iterator(); iter.hasNext();) {
                        createChildParamEditor(i++, (Map)iter.next());
                    }
                }
            }
            else {
                createChildParamEditor(0,null);
            }
            Composite container = (Composite)getControl();
            if(Common.isValid(container)) {
                for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                    INSISParamEditor editor = (INSISParamEditor)iter.next();
                    addEditor(container, editor);
                }
                updateControl(container);
            }
        }

        /**
         * @param childSettings
         */
        private INSISParamEditor createChildParamEditor(int index, Map childSettings)
        {
            INSISParamEditor editor = mChildParam.createEditor(this);
            editor.setSettings(childSettings);
            mChildParamEditors.add(index, editor);
            return editor;
        }

        protected Control createParamControl(Composite parent)
        {
            Composite container = new Composite(parent,SWT.NONE);
            GridLayout gridLayout = new GridLayout(1,false);
            gridLayout.marginHeight = gridLayout.marginWidth = 0;
            container.setLayout(gridLayout);
            return container;
        }

        protected boolean createMissing()
        {
            return false;
        }

        /**
         * @param parent
         * @param composite
         */
        private Control addEditor(final Composite container, final INSISParamEditor editor)
        {
            final Composite control = new Composite(container,SWT.NONE);
            GridLayout layout = new GridLayout(3,false);
            layout.marginHeight = layout.marginWidth = 0;
            layout.horizontalSpacing = 2;
            control.setLayout(layout);
            control.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

            Composite composite = new Composite(control,SWT.NONE);
            layout = new GridLayout();
            layout.marginHeight = layout.marginWidth = 0;
            layout.numColumns = (editor.getParam().isOptional()?1:0)+(Common.isEmpty(editor.getParam().getName())?0:1)+1;
            layout.makeColumnsEqualWidth = false;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
            Control c = editor.createControl(composite);
            if(c != null) {
                c.setData(DATA_PARENT,control);

                Object[] formatArgs = new Object[] {mLabel==null?Common.ZERO:new Integer(mLabel.length()), mLabel};
                c.setLayoutData(new GridData(SWT.FILL,(c instanceof Composite?SWT.FILL:SWT.CENTER),true,true));
                final Button delButton = new Button(control,SWT.PUSH);
                delButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
                delButton.setToolTipText(cRemoveFormat.format(formatArgs));
                final Button addButton = new Button(control,SWT.PUSH);
                addButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
                addButton.setToolTipText(cAddFormat.format(formatArgs));

                delButton.setImage(CommonImages.DELETE_SMALL_ICON);
                addButton.setImage(CommonImages.ADD_SMALL_ICON);

                delButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e)
                    {
                        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                            public void run()
                            {
                                mChildParamEditors.remove(editor);
                                editor.dispose();
                                control.dispose();
                                updateControl(container);
                            }
                        });
                    }
                });
                addButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e)
                    {
                        BusyIndicator.showWhile(Display.getCurrent(),new Runnable() {
                            public void run()
                            {
                                Control c = addEditor(container, createChildParamEditor(mChildParamEditors.indexOf(editor) + 1, getSettings() != null?new HashMap():null));
                                c.moveBelow(control);
                                updateControl(container);
                            }
                        });
                    }
                });
                c.setData(DATA_BUTTONS,new Button[] {delButton,addButton});
            }
            editor.initEditor();
            return control;
        }

        protected void updateState(boolean state)
        {
            updateEditors(state);
            super.updateState(state);
        }

        /**
         * @param state
         */
        private void updateEditors(boolean state)
        {
            for (int i=0; i<mChildParamEditors.size(); i++) {
                INSISParamEditor editor = (INSISParamEditor)mChildParamEditors.get(i);
                if(editor != null) {
                    Control ctrl = editor.getControl();
                    if(Common.isValid(ctrl)) {
                        Button[] buttons = (Button[])ctrl.getData(DATA_BUTTONS);
                        if(buttons != null && buttons.length == 2) {
                            if(Common.isValid(buttons[0]) && Common.isValid(buttons[1])) {
                                if(state) {
                                    buttons[0].setEnabled(i != 0 || (mChildParamEditors.size() > 1));
                                    buttons[1].setEnabled(true);
                                }
                                else {
                                    buttons[0].setEnabled(false);
                                    buttons[1].setEnabled(false);
                                }
                            }
                        }
                    }
                }
                editor.setEnabled(state);
            }
        }

        /**
         * @param container
         */
        private void updateControl(Composite container)
        {
            if(Common.isValid(container)) {
                container.layout(true,true);
                Shell shell = container.getShell();
                shell.layout(new Control[] {container});
                Point size = shell.getSize();
                shell.setSize(size.x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            }
            updateEditors(isSelected());
        }
    }
}
