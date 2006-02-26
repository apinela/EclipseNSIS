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
import net.sf.eclipsensis.util.XMLUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GroupParam extends NSISParam
{
    public static final String ATTR_DEPENDS = "depends"; //$NON-NLS-1$
    public static final String SETTING_CHILD_SETTINGS = "childSettings"; //$NON-NLS-1$
    protected NSISParam[] mChildParams;
    protected Map mDependencies;
    
    public GroupParam(Node node)
    {
        super(node);
    }

    protected void init(Node node)
    {
        super.init(node);
        loadParams(node);
    }

    public NSISParam[] getChildParams()
    {
        return mChildParams;
    }

    protected NSISParamEditor createParamEditor()
    {
        return new GroupParamEditor();
    }

    private void addDependent(Object parent, NSISParam dependent)
    {
        List dependents = (List)mDependencies.get(parent);
        if(dependents == null) {
            dependents = new ArrayList();
            mDependencies.put(parent, dependents);
        }
        dependents.add(dependent);
    }

    private void loadParams(Node node)
    {
        mDependencies = new HashMap();
        List params = new ArrayList();
        NodeList paramNodes = node.getChildNodes();
        if(paramNodes.getLength() > 0) {
            int count = paramNodes.getLength();
            for(int k=0; k<count; k++) {
                Node paramNode = paramNodes.item(k);
                if(paramNode.getNodeName().equals(TAG_PARAM)) {
                    NSISParam param = NSISCommandManager.createParam(paramNode);
                    if(param != null) {
                        params.add(param);
                    }
                    int depends = XMLUtil.getIntValue(paramNode.getAttributes(), ATTR_DEPENDS,-1);
                    int index = params.size()-1;
                    if(depends >= 0 && depends != index) {
                        if(depends < params.size()) {
                            NSISParam dependsParam = (NSISParam)params.get(depends);
                            if(dependsParam.isOptional()) {
                                addDependent(dependsParam, param);
                            }
                        }
                        else {
                            addDependent(new Integer(depends), param);
                        }
                    }
                    List dependents = (List)mDependencies.remove(new Integer(index));
                    if(dependents != null) {
                        if(param.isOptional()) {
                            mDependencies.put(param, dependents);
                        }
                    }
                }
            }
        }
        mChildParams = (NSISParam[])params.toArray(new NSISParam[params.size()]);
    }
    
    protected class GroupParamEditor extends NSISParamEditor
    {
        protected INSISParamEditor[] mParamEditors;

        public GroupParamEditor()
        {
            super();
            Map map = new HashMap();
            mParamEditors = new INSISParamEditor[mChildParams.length];
            for (int i = 0; i < mChildParams.length; i++) {
                mParamEditors[i] = mChildParams[i].createEditor();
                map.put(mChildParams[i], mParamEditors[i]);
            }
            for (Iterator iter= mDependencies.keySet().iterator(); iter.hasNext(); ) {
                Object obj = iter.next();
                if (obj instanceof NSISParam) {
                    NSISParam param = (NSISParam)obj;
                    List dependents = (List)mDependencies.get(param);
                    if (!Common.isEmptyCollection(dependents)) {
                        NSISParamEditor parentEditor = (NSISParamEditor)map.get(param);
                        List list = new ArrayList();
                        for (Iterator iterator = dependents.iterator(); iterator.hasNext();) {
                            list.add(map.get(iterator.next()));
                        }
                        parentEditor.setDependents(list);
                    }
                }                
            }
        }

        protected void updateState(boolean state)
        {
            super.updateState(state);
            if(!Common.isEmptyArray(mParamEditors)) {
                List dependents = new ArrayList();
                for (int i = 0; i < mParamEditors.length; i++) {
                    if(!dependents.contains(mParamEditors[i])) {
                        mParamEditors[i].setEnabled(state);
                    }
                    List list = mParamEditors[i].getDependents();
                    if(!Common.isEmptyCollection(list)) {
                        dependents.addAll(list);
                    }
                }
            }
        }

        protected String validateParam()
        {
            String validText = null;
            if(!Common.isEmptyArray(mParamEditors)) {
                for (int i = 0; i < mParamEditors.length; i++) {
                    validText = mParamEditors[i].validate();
                    if(validText != null) {
                        break;
                    }
                }
            }
            return validText;
        }

        protected void appendParamText(StringBuffer buf)
        {
            if(!Common.isEmptyArray(mParamEditors)) {
                for (int i = 0; i < mParamEditors.length; i++) {
                    mParamEditors[i].appendText(buf);
                }
            }
        }

        public void setSettings(Map settings)
        {
            super.setSettings(settings);
            if(!Common.isEmptyArray(mParamEditors)) {
                if(settings != null) {
                    Map[] childSettings = (Map[])settings.get(SETTING_CHILD_SETTINGS);
                    if(childSettings == null || childSettings.length != mParamEditors.length) {
                        childSettings = new Map[mParamEditors.length];
                        settings.put(SETTING_CHILD_SETTINGS, childSettings);
                    }
                    for (int i = 0; i < mParamEditors.length; i++) {
                        if(childSettings[i] == null) {
                            childSettings[i] = new HashMap();
                        }
                        mParamEditors[i].setSettings(childSettings[i]);
                    }
                }
                else {
                    for (int i = 0; i < mParamEditors.length; i++) {
                        mParamEditors[i].setSettings(null);
                    }
                }
            }
        }

        public void saveSettings()
        {
            super.saveSettings();
            if(!Common.isEmptyArray(mParamEditors) && getSettings() != null) {
                for (int i = 0; i < mParamEditors.length; i++) {
                    mParamEditors[i].saveSettings();
                }
            }
        }

        protected boolean createMissing()
        {
            return false;
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Group(parent,SWT.NONE);
            GridLayout layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 2;
            parent.setLayout(layout);
            if(!Common.isEmptyArray(mChildParams)) {
                layout.numColumns = getLayoutNumColumns();
                
                for (int i = 0; i < mParamEditors.length; i++) {
                    createChildParamControl(parent, i);
                }
            }
            return parent;
        }

        protected void initParamEditor()
        {
            super.initParamEditor();
            if(!Common.isEmptyArray(mParamEditors)) {
                for (int i = 0; i < mParamEditors.length; i++) {
                    mParamEditors[i].initEditor();
                }
            }
        }
        
        protected int getLayoutNumColumns()
        {
            boolean isOptional = false;
            boolean hasName = false;
            for (int i = 0; i < mChildParams.length; i++) {
                if(!Common.isEmpty(mChildParams[i].getName())) {
                    hasName = true;
                }
                if(mChildParams[i].isOptional()) {
                    isOptional = true;
                }
            }

            return 1+(isOptional?1:0)+(hasName?1:0);
        }

        protected void createChildParamControl(Composite parent, int index)
        {
            mParamEditors[index].createControl(parent);
        }
    }
}
