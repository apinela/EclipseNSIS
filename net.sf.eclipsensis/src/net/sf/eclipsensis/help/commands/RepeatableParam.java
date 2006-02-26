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
import java.util.List;

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.CommonImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RepeatableParam extends NSISParam
{
    public static final String SETTING_CHILD_SETTINGS = "childSettings"; //$NON-NLS-1$
    
    private NSISParam mChildParam;
    
    public RepeatableParam(Node node)
    {
        super(node);
    }
    
    protected void init(Node node)
    {
        super.init(node);
        mChildParam = loadChildParam(node);
    }

    protected NSISParam loadChildParam(Node node)
    {
        NodeList paramNodes = node.getChildNodes();
        if(paramNodes.getLength() > 0) {
            int count = paramNodes.getLength();
            for(int k=0; k<count; k++) {
                Node paramNode = paramNodes.item(k);
                if(paramNode.getNodeName().equals(TAG_PARAM)) {
                    return NSISCommandManager.createParam(paramNode);
                }
            }
        }
        return null;
    }

    protected NSISParamEditor createParamEditor()
    {
        return new RepeatableParamEditor();
    }

    protected class RepeatableParamEditor extends NSISParamEditor
    {
        public static final String DATA_BUTTONS = "BUTTONS"; //$NON-NLS-1$
        private List mChildParamEditors = new ArrayList();
        
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
                        if(editor != null && childSettings != null) {
                            editor.setSettings(childSettings);
                        }
                        else if(editor != null) {
                            childSettings = new HashMap();
                            editor.setSettings(childSettings);
                            iter2.add(childSettings);
                        }
                        else if(childSettings != null) {
                            iter2.remove();
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

        private List getChildSettingsList()
        {
            List childSettingsList = (List)getSettingValue(SETTING_CHILD_SETTINGS, List.class, null);
            if(childSettingsList == null) {
                childSettingsList = new ArrayList();
                getSettings().put(SETTING_CHILD_SETTINGS, childSettingsList);
            }
            return childSettingsList;
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            
            if(getSettings() != null) {
                List childSettingsList = getChildSettingsList();
                if(childSettingsList.size() == 0) {
                    childSettingsList.add(new HashMap());
                }
                for (Iterator iter = childSettingsList.iterator(); iter.hasNext();) {
                    Map childSettings = (Map)iter.next();
                    INSISParamEditor editor = mChildParam.createEditor();
                    editor.setSettings(childSettings);
                    mChildParamEditors.add(editor);
                }
            }
            else {
                mChildParamEditors.add(mChildParam.createEditor());
            }
            Composite container = (Composite)getControl();
            for (Iterator iter = mChildParamEditors.iterator(); iter.hasNext();) {
                INSISParamEditor editor = (INSISParamEditor)iter.next();
                addEditor(container, editor);
            }
            updateControl(container);
        }
        
        protected Control createParamControl(Composite parent)
        {
            Composite container = new Composite(parent,SWT.NONE);
            GridLayout gridLayout = new GridLayout(3,false);
            gridLayout.marginHeight = gridLayout.marginWidth = 0;
            gridLayout.horizontalSpacing = 2;
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
        private void addEditor(final Composite container, final INSISParamEditor editor)
        {
            final Composite composite = new Composite(container,SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.marginHeight = layout.marginWidth = 0;
            layout.numColumns = (editor.getParam().isOptional()?1:0)+(Common.isEmpty(editor.getParam().getName())?0:1)+1;
            layout.makeColumnsEqualWidth = false;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
            Control c = editor.createControl(composite);
            c.setLayoutData(new GridData(SWT.FILL,(c instanceof Composite?SWT.FILL:SWT.CENTER),true,true));
            final Button delButton = new Button(container,SWT.PUSH);
            delButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
            final Button addButton = new Button(container,SWT.PUSH);
            addButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));

            delButton.setImage(CommonImages.DELETE_SMALL_ICON);
            addButton.setImage(CommonImages.ADD_SMALL_ICON);

            delButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if(getSettings() != null) {
                        List childSettingsList = getChildSettingsList();
                        int i = mChildParamEditors.indexOf(editor);
                        if(i >= 0 && i < childSettingsList.size()) {
                            childSettingsList.remove(i);
                        }
                    }
                    mChildParamEditors.remove(editor);
                    composite.dispose();
                    addButton.dispose();
                    delButton.dispose();
                    updateControl(container);
                }
            });
            addButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    INSISParamEditor ed = mChildParam.createEditor();
                    if(getSettings() != null) {
                        Map childSettings = new HashMap();
                        ed.setSettings(childSettings);
                        List childSettingsList = getChildSettingsList();
                        childSettingsList.add(childSettings);
                    }
                    mChildParamEditors.add(ed);
                    addEditor(container, ed);
                    updateControl(container);
                }
            });
            c.setData(DATA_BUTTONS,new Button[] {delButton,addButton});
            editor.initEditor();
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
                    if(isValid(ctrl)) {
                        Button[] buttons = (Button[])ctrl.getData(DATA_BUTTONS);
                        if(buttons != null && buttons.length == 2) {
                            if(isValid(buttons[0]) && isValid(buttons[1])) {
                                if(state) {
                                    boolean enabled = i != 0 || (mChildParamEditors.size() > 1);
                                    buttons[0].setEnabled(enabled);
                                    enabled = i == (mChildParamEditors.size()-1);
                                    buttons[1].setEnabled(enabled);
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
        private void updateControl(final Composite container)
        {
            container.layout(true);
            Shell shell = container.getShell();
            Point size = shell.getSize();
            shell.setSize(size.x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            updateEditors(isSelected());
        }

    }
}
