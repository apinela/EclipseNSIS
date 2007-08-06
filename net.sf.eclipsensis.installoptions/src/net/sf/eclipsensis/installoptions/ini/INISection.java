/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.Position;

public class INISection extends INILine implements IINIContainer
{
    private static final long serialVersionUID = -1320834864833847467L;

    private List mChildren = new ArrayList();
    private String mName;
    private String mOriginalName;
    private Position mPosition;

    private boolean mDirty = false;

    public INISection()
    {
        super(""); //$NON-NLS-1$
    }

    INISection(String text, String delimiter, String name)
    {
        super(text,delimiter);
        mName = name;
        mOriginalName = name;
    }

    public boolean isDirty()
    {
        return mDirty;
    }

    public void setDirty(boolean dirty)
    {
        mDirty = dirty;
        super.setDirty(dirty);
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        if(!Common.stringsAreEqual(mName, name)) {
            mName = name;
            setDirty(true);
        }
    }

    public Position getPosition()
    {
        return mPosition;
    }

    public Position calculatePosition()
    {
        if(isDirty()) {
            int length = getLength();
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                length += ((INILine)iter.next()).getLength();
            }
            mPosition.setLength(length);
            setDirty(false);
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
        setDirty(true);
    }

    public void removeChild(INILine line)
    {
        if(mChildren.remove(line)) {
            line.setParent(null);
            setDirty(true);
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

    public INILine getLineAtOffset(int offset)
    {
        Position pos = getPosition();
        if(offset >= pos.offset && offset < pos.offset+pos.length) {
            if(offset < pos.offset+getLength()) {
                return this;
            }
            int start = pos.offset+getLength();
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                INILine line = (INILine)iter.next();
                if(offset < start+line.getLength()) {
                    return line;
                }
                start += line.getLength();
            }
        }
        return null;
    }

    public Position getChildPosition(INILine child)
    {
        if(mChildren.contains(child)) {
            int offset = getPosition().offset+getLength();
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                INILine line = (INILine)iter.next();
                if(line == child) {
                    return new Position(offset, line.getLength());
                }
                else {
                    offset += line.getLength();
                }
            }
        }
        return null;
    }

    protected void checkProblems(int fixFlag)
    {
        //Validate section
        final INIFile parent = (INIFile)getParent();
        if(parent != null) {
            final INISection[] sections = parent.findSections(getName());
            if(sections.length > 1) {
                if((fixFlag & VALIDATE_FIX_ERRORS) > 0) {
                    for (int i = 0; i < sections.length; i++) {
                        if(sections[i] != this) {
                            parent.removeChild(sections[i]);
                        }
                    }
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("duplicate.section.name.error", //$NON-NLS-1$
                                                        new String[]{getName()}));
                    addProblem(problem);
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.dup.sections")) { //$NON-NLS-1$
                        protected INIProblemFix[] createFixes()
                        {
                            List fixes = new ArrayList();
                            int count = 0;
                            for (int i = sections.length-1; i >= 0; i--) {
                                if(sections[i] != INISection.this) {
                                    count--;
                                    List children = sections[i].getChildren();
                                    if(!Common.isEmptyCollection(children)) {
                                        ListIterator iter = children.listIterator(children.size());
                                        while(iter.hasPrevious()) {
                                            fixes.add(new INIProblemFix((INILine)iter.previous()));
                                        }
                                    }
                                    fixes.add(new INIProblemFix(sections[i]));
                                }
                            }

                            INISection[] sections = parent.getSections();
                            if(!Common.isEmptyArray(sections)) {
                                for (int i = 0; i < sections.length; i++) {
                                    Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(sections[i].getName());
                                    if(m.matches()) {
                                        count++;
                                    }
                                }
                            }
                            int numFields = -1;
                            INISection[] section = parent.findSections(InstallOptionsModel.SECTION_SETTINGS);
                            if(section.length > 0) {
                                INIKeyValue[] keyValue = section[0].findKeyValues(InstallOptionsModel.PROPERTY_NUMFIELDS);
                                if(keyValue.length > 0) {
                                    try {
                                        numFields = Integer.parseInt(keyValue[0].getValue());
                                    }
                                    catch(Exception e) {
                                        numFields = -1;
                                    }
                                    if(numFields != count) {
                                        fixes.add(new INIProblemFix(keyValue[0],keyValue[0].buildText(Integer.toString(count))+(keyValue[0].getDelimiter()==null?"":keyValue[0].getDelimiter()))); //$NON-NLS-1$
                                    }
                                }
                            }

                            return (INIProblemFix[])fixes.toArray(new INIProblemFix[fixes.size()]);
                        }
                    });
                }
            }
        }

        for (int i=0; i<mChildren.size(); i++) {
            ((INILine)mChildren.get(i)).validate(fixFlag);
        }

        //Validate required keys
        if(getName().equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
            Collection settings = InstallOptionsModel.INSTANCE.getDialogSettings();
            INIKeyValue[] keyValues = getKeyValues();
            for (int i = 0; i < keyValues.length; i++) {
                if(!settings.contains(keyValues[i].getKey())) {
                    if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                        removeChild(keyValues[i]);
                    }
                    else {
                        INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING, InstallOptionsPlugin.getFormattedString("unrecognized.key.warning", //$NON-NLS-1$
                                                        new Object[]{InstallOptionsPlugin.getResourceString("section.label"), //$NON-NLS-1$
                                                                     InstallOptionsModel.SECTION_SETTINGS,keyValues[i].getKey()}));
                        keyValues[i].addProblem(problem);
                        final INIKeyValue keyValue = keyValues[i];
                        problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.unrecognized.key")) { //$NON-NLS-1$
                            protected INIProblemFix[] createFixes()
                            {
                                return new INIProblemFix[] {new INIProblemFix(keyValue)};
                            }
                        });
                    }
                }
            }
        }
        else {
            if(isInstallOptionsField()) {
                final List missing = new ArrayList();
                final Map requiredSettings = InstallOptionsModel.INSTANCE.getControlRequiredSettings();
                for (Iterator iter = requiredSettings.keySet().iterator(); iter.hasNext(); ) {
                    String name = (String)iter.next();
                    INIKeyValue[] keyValues = findKeyValues(name);
                    if(Common.isEmptyArray(keyValues)) {
                        if((fixFlag & VALIDATE_FIX_ERRORS) > 0) {
                            INIKeyValue keyValue = new INIKeyValue(name);
                            keyValue.setValue((String)requiredSettings.get(name));
                            addChild(0,keyValue);
                        }
                        else {
                            missing.add(name);
                        }
                    }
                }
                if(missing.size() > 0) {
                    Integer size = new Integer(missing.size());
                    StringBuffer buf = new StringBuffer();
                    Iterator iter = missing.iterator();
                    buf.append("\"").append(iter.next()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
                    while(iter.hasNext()) {
                        buf.append(", \"").append(iter.next()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("required.keys.missing", //$NON-NLS-1$
                                                        new Object[]{buf.toString(), size}));
                    addProblem(problem);
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.insert.missing.keys")) { //$NON-NLS-1$
                        protected INIProblemFix[] createFixes()
                        {
                            StringBuffer buf = new StringBuffer(INISection.this.getText());
                            INILine previous = INISection.this;
                            for (Iterator iter = missing.iterator(); iter.hasNext(); ) {
                                buf.append(previous.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:previous.getDelimiter());
                                String name = (String)iter.next();
                                INIKeyValue keyValue = new INIKeyValue(name);
                                buf.append(keyValue.buildText((String)requiredSettings.get(name)));
                                previous = keyValue;
                            }
                            return new INIProblemFix[] {new INIProblemFix(INISection.this,buf.toString())};
                        }
                    });
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
                                if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                                    removeChild(keyValues[i]);
                                }
                                else {
                                    INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING, InstallOptionsPlugin.getFormattedString("unrecognized.key.warning", //$NON-NLS-1$
                                                                                new Object[]{InstallOptionsModel.PROPERTY_TYPE,
                                                                                             type,keyValues[i].getKey()}));
                                    keyValues[i].addProblem(problem);
                                    final INIKeyValue keyValue = keyValues[i];
                                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.unrecognized.key")) { //$NON-NLS-1$
                                        protected INIProblemFix[] createFixes()
                                        {
                                            return new INIProblemFix[] {new INIProblemFix(keyValue)};
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
                else {
                    if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                        INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_TYPE);
                        keyValue.setValue(InstallOptionsModel.TYPE_UNKNOWN);
                        addChild(0,keyValue);
                    }
                    else {
                        INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING, InstallOptionsPlugin.getFormattedString("key.missing.warning", //$NON-NLS-1$
                                                        new Object[]{InstallOptionsModel.PROPERTY_TYPE}));
                        addProblem(problem);
                        problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.insert.missing.key")) { //$NON-NLS-1$
                            protected INIProblemFix[] createFixes()
                            {
                                StringBuffer buf = new StringBuffer(INISection.this.getText());
                                buf.append(INISection.this.getDelimiter() == null?INSISConstants.LINE_SEPARATOR:INISection.this.getDelimiter());
                                INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_TYPE);
                                buf.append(keyValue.buildText(InstallOptionsModel.TYPE_UNKNOWN));
                                return new INIProblemFix[] {new INIProblemFix(INISection.this,buf.toString())};
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * @return
     */
    public boolean isInstallOptionsField()
    {
        return (getName() != null && InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(getName()).matches());
    }

    public void update()
    {
        if(!Common.stringsAreEqual(mName,mOriginalName)) {
            String newText = buildText(mName);
            mOriginalName = mName;
            setText(newText);
        }
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            ((INILine)iter.next()).update();
        }
    }

    public String buildText(String name)
    {
        String text = getText();
        StringBuffer buf = new StringBuffer();
        if(Common.isEmpty(text)) {
            buf.append("[").append(name).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            int n = text.indexOf("["); //$NON-NLS-1$
            if(Common.isEmpty(mOriginalName)) {
                buf.append(text.substring(0,n+1)).append(name).append(text.substring(n+1));
            }
            else {
                n = text.indexOf(mOriginalName,n);
                buf.append(text.substring(0,n));
                buf.append(name);
                buf.append(text.substring(n+mOriginalName.length()));
            }
        }
        return buf.toString();
    }

    public int getSize()
    {
        return mChildren.size();
    }

    public INILine getChild(int index)
    {
        return (INILine)mChildren.get(index);
    }

    public INILine copy()
    {
        INISection sec = (INISection)clone();
        if(mPosition != null) {
            sec.mPosition = new Position(mPosition.getOffset(),mPosition.getLength());
        }
        return sec;
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
        section.setDirty(false);
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

    public boolean matches(INILine line)
    {
        if (this == line) {
            return true;
        }
        if (!super.matches(line)) {
            return false;
        }

        final INISection section = (INISection)line;
        if(!Common.objectsAreEqual(mName,section.mName)) {
            return false;
        }
        if(mChildren != null || section.mChildren != null) {
            if(mChildren != null && section.mChildren != null) {
                int n = mChildren.size();
                if(n == section.mChildren.size()) {
                    for(int i=0; i<n; i++) {
                        Object o1 = mChildren.get(i);
                        Object o2 = section.mChildren.get(i);
                        if(o1 instanceof INILine && o2 instanceof INILine) {
                            INILine line1 = (INILine)o1;
                            INILine line2 = (INILine)o2;
                            if(!line1.matches(line2)) {
                                return false;
                            }
                        }
                        else if(o1 != null || o2 != null){
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
