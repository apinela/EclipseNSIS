/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import java.util.*;
import java.util.regex.Matcher;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModelTypeDef;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.Position;

public class INISection extends INILine implements IINIContainer
{
    private static final long serialVersionUID = -1320834864833847467L;
    
    private boolean mDirty = false;
    private List mChildren = new ArrayList();
    private String mName;
    private String mOriginalName;
    private Position mPosition;
    
    public INISection()
    {
        super();
    }

    INISection(String name)
    {
        super();
        mName = name;
        mOriginalName = name;
    }
    
    public String getName()
    {
        return mName;
    }
    
    public void setName(String name)
    {
        mName = name;
    }
    
    public Position getPosition()
    {
        if(mDirty) {
            int length = getLength();
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                length += ((INILine)iter.next()).getLength();
            }
            mPosition.setLength(length);
            mDirty = false;
        }
        return mPosition;
    }

    public void setPosition(Position position)
    {
        mPosition = position;
    }

    public void addChild(INILine line)
    {
        addChild(mChildren.size(),line);
    }


    public void addChild(int index, INILine line)
    {
        if(line instanceof INISection) {
            throw new IllegalArgumentException();
        }
        mChildren.add(index, line);
        line.setParent(this);
        mDirty = true;
    }
    
    public void removeChild(INILine line)
    {
        if(mChildren.remove(line)) {
            line.setParent(null);
            mDirty = true;
        }
    }

    public List getChildren()
    {
        return mChildren;
    }
    
    public INIKeyValue[] getKeyValues()
    {
        List list = new ArrayList();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine element = (INILine)iter.next();
            if(element instanceof INIKeyValue) {
                list.add(element);
            }
        }
        return (INIKeyValue[])list.toArray(new INIKeyValue[list.size()]);
    }
    
    public INIKeyValue[] findKeyValues(String key)
    {
        List list = new ArrayList();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine element = (INILine)iter.next();
            if(element instanceof INIKeyValue && ((INIKeyValue)element).getKey().equalsIgnoreCase(key)) {
                list.add(element);
            }
        }
        return (INIKeyValue[])list.toArray(new INIKeyValue[list.size()]);
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer(super.toString());
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            buf.append(iter.next());
        }
        return buf.toString();
    }
    
    public boolean hasErrors()
    {
        if(!super.hasErrors()) {
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                if(((INILine)iter.next()).hasErrors()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    public boolean hasWarnings()
    {
        if(!super.hasWarnings()) {
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                if(((INILine)iter.next()).hasWarnings()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    protected void checkProblems()
    {
        //Validate section
        INISection[] sections = ((INIFile)getParent()).findSections(getName());
        if(sections.length > 1) {
            addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("duplicate.section.name.error", //$NON-NLS-1$
                            new String[]{getName()}));
        }
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            ((INILine)iter.next()).validate();
        }

        //Validate required keys
        if(getName().equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
            Collection settings = InstallOptionsModel.INSTANCE.getDialogSettings();
            INIKeyValue[] keyValues = getKeyValues();
            for (int i = 0; i < keyValues.length; i++) {
                if(!settings.contains(keyValues[i].getKey())) {
                    keyValues[i].addProblem(INIProblem.TYPE_WARNING, InstallOptionsPlugin.getFormattedString("unrecognized.key.warning", //$NON-NLS-1$
                            new Object[]{InstallOptionsPlugin.getResourceString("section.label"), //$NON-NLS-1$
                                         InstallOptionsModel.SECTION_SETTINGS,keyValues[i].getKey()}));
                }
            }
        }
        else {
            Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(getName());
            if(m.matches()) {
                List missing = new ArrayList();
                Collection requiredSettings = InstallOptionsModel.INSTANCE.getControlRequiredSettings();
                for (Iterator iter = requiredSettings.iterator(); iter.hasNext(); ) {
                    String name = (String)iter.next();
                    INIKeyValue[] keyValues = findKeyValues(name);
                    if(Common.isEmptyArray(keyValues)) {
                        missing.add(name);
                    }
                }
                if(missing.size() > 0) {
                    Integer n = new Integer(missing.size());
                    StringBuffer buf = new StringBuffer();
                    Iterator iter = missing.iterator();
                    buf.append("\"").append(iter.next()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
                    while(iter.hasNext()) {
                        buf.append(", \"").append(iter.next()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("required.keys.missing", //$NON-NLS-1$
                                    new Object[]{buf.toString(), n}));
                }
                
                INIKeyValue[] keyValues = findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
                if(!Common.isEmptyArray(keyValues)) {
                    String type = keyValues[0].getValue();
                    InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(type);
                    if(typeDef != null) {
                        Collection settingsSet;
                        settingsSet = typeDef.getSettings();
                        keyValues = getKeyValues();
                        for (int i = 0; i < keyValues.length; i++) {
                            if(!settingsSet.contains(keyValues[i].getKey())) {
                                keyValues[i].addProblem(INIProblem.TYPE_WARNING, InstallOptionsPlugin.getFormattedString("unrecognized.key.warning", //$NON-NLS-1$
                                        new Object[]{InstallOptionsModel.PROPERTY_TYPE,
                                                     type,keyValues[i].getKey()}));
                            }
                        }
                    }
                }
                else {
                    addProblem(INIProblem.TYPE_WARNING, InstallOptionsPlugin.getFormattedString("key.missing.warning", //$NON-NLS-1$
                            new Object[]{InstallOptionsModel.PROPERTY_TYPE}));
                }
            }
        }
    }
    
    public void update()
    {
        if(!Common.stringsAreEqual(mName,mOriginalName)) {
            String text = getText();
            StringBuffer buf = new StringBuffer();
            if(Common.isEmpty(text)) {
                buf.append("[").append(mName).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else {
                int n = text.indexOf("["); //$NON-NLS-1$
                if(Common.isEmpty(mOriginalName)) {
                    buf.append(text.substring(0,n+1)).append(mName).append(text.substring(n+1));
                }
                else {
                    n = text.indexOf(mOriginalName,n);
                    buf.append(text.substring(0,n));
                    buf.append(mName);
                    buf.append(text.substring(n+mOriginalName.length()));
                }
            }
            mOriginalName = mName;
            setText(buf.toString());
        }
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            ((INILine)iter.next()).update();
        }
    }
    
    public int getSize()
    {
        return mChildren.size();
    }
    
    public INILine getChild(int index)
    {
        return (INILine)mChildren.get(index);
    }

    public Object clone()
    {
        INISection section = (INISection)super.clone();
        section.mPosition = null;
        section.mChildren = new ArrayList();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            section.addChild((INILine)line.clone());
        }
        section.mDirty = false;
        return section;
    }

    public INISection trim()
    {
        int n = mChildren.size();
        for (int i=n-1; i>=0; i--) {
            INILine line = (INILine)mChildren.get(i);
            if(line.getClass().equals(INILine.class)) {
                if(Common.isEmpty(line.getText())) {
                    removeChild(line);
                    continue;
                }
            }
            if(line.getDelimiter() == null) {
                line.setDelimiter(INSISConstants.LINE_SEPARATOR);
            }
            break;
        }
        return this;
    }
}
