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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;

public class INIFile implements IDocumentListener, IINIContainer
{
    public static final String INIFILE_CATEGORY = "__installoptions_inifile"; //$NON-NLS-1$
    
    private List mChildren = new ArrayList();
    private IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(INIFILE_CATEGORY);
    private List mLines = new ArrayList();
    private List mListeners = new ArrayList();
    private int mChangeStartLine = -1;
    private int mChangeEndLine = -1;
    private IDocument mDocument = null;
    
    private boolean mDirty = false;
    private List mErrors = new ArrayList();
    private List mWarnings = new ArrayList();
    private boolean mUpdatingDocument = false;

    public void addListener(IINIFileListener listener)
    {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public void removeListener(IINIFileListener listener)
    {
        mListeners.remove(listener);
    }
    
    private void notifyListeners()
    {
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            ((IINIFileListener)iter.next()).iniFileChanged(this);
        }
    }

    public void addChild(INILine line)
    {
        addChild(mChildren.size(),line);
    }

    public void addChild(int index, INILine line)
    {
        mChildren.add(index, line);
        line.setParent(this);
    }

    public void removeChild(INILine line)
    {
        mChildren.remove(line);
        line.setParent(null);
    }

    public List getChildren()
    {
        return mChildren;
    }
    
    public INISection[] findSections(String name)
    {
        List list = new ArrayList();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            if(line instanceof INISection && ((INISection)line).getName().equalsIgnoreCase(name)) {
                list.add(line);
            }
        }
        return (INISection[])list.toArray(new INISection[0]);
    }
    
    public INISection[] getSections()
    {
        List list = new ArrayList();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            if(line instanceof INISection) {
                list.add(line);
            }
        }
        return (INISection[])list.toArray(new INISection[0]);
    }
    
    private INIComment parseComment(String text)
    {
        if(text.startsWith(";")) { //$NON-NLS-1$
            return new INIComment();
        }
        return null;
    }
    
    private INISection parseSection(String text)
    {
        if(text.startsWith("[")) { //$NON-NLS-1$
            int m = text.indexOf('[');
            int n = text.indexOf(']',m+1);
            if(n > 0) {
                text = text.substring(m+1,n).trim();
                if(Character.isLetter(text.charAt(0))) {
                    INISection section = new INISection(text.trim());
                    return section;
                }
            }
        }
        return null;
    }
    
    private INIKeyValue parseKeyValue(String text)
    {
        if(text.length() > 0) {
            int n = text.indexOf('=');
            if (Character.isLetter(text.charAt(0)) && n > 0) {
                INIKeyValue keyValue = new INIKeyValue(text.substring(0,n).trim(),
                                                       text.substring(n+1).trim());
                return keyValue;
            }
        }
        return null;
    }
    
    public void update()
    {
        mLines.clear();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine child = (INILine)iter.next();
            mLines.add(child);
            if(child instanceof INISection) {
                mLines.addAll(((INISection)child).getChildren());
            }
            child.update();
        }
        validate();
    }
    
    public void updateDocument(IDocument doc)
    {
        if(mDocument == doc) {
            try {
                mUpdatingDocument = true;
                doc.set(toString());
            }
            finally {
                mUpdatingDocument = false;
            }
        }
    }

    public void connect(IDocument doc)
    {
        if(mDocument != null) {
            disconnect(mDocument);
        }
        mDirty = true;
        mDocument = doc;
        doc.addPositionCategory(INIFILE_CATEGORY);
        doc.addDocumentListener(this);
        doc.addPositionUpdater(mPositionUpdater);
        int lineCount = doc.getNumberOfLines();
        List lines = parseLines(doc,0,lineCount-1);
        IINIContainer container = this;
        for(Iterator iter=lines.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            mLines.add(line);
            if(line instanceof IINIContainer) {
                addChild(line);
                container = (IINIContainer)line;
            }
            else {
                container.addChild(line);
            }
        }
        validate();
        notifyListeners();
    }
    
    private List parseLines(IDocument doc, int startLine, int endLine)
    {
        List lines = new ArrayList();
        for(int i=startLine; i<= endLine; i++) {
            try {
                IRegion region = doc.getLineInformation(i);
                String text = doc.get(region.getOffset(),region.getLength());
                INILine line = parse(text.trim());
                line.setText(text);
                line.setDelimiter(doc.getLineDelimiter(i));
                if(line instanceof INISection) {
                    Position pos = new Position(region.getOffset(),line.getLength());
                    ((INISection)line).setPosition(pos);
                    try {
                        doc.addPosition(INIFILE_CATEGORY,pos);
                    }
                    catch (BadPositionCategoryException e1) {
                        e1.printStackTrace();
                    }
                }
                lines.add(line);
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }
    private INILine parse(String text)
    {
        INILine line;
        line = parseComment(text);
        if(line == null) {
            line = parseSection(text);
            if(line == null) {
                line = parseKeyValue(text);
                if(line == null) {
                    line = new INILine();
                }
            }
        }
        return line;
    }

    public void disconnect(IDocument doc)
    {
        if(mDocument == doc) {
            mDocument = null;
            doc.removePositionUpdater(mPositionUpdater);
            doc.removeDocumentListener(this);
            if(doc.containsPositionCategory(INIFILE_CATEGORY)) {
                try {
                    doc.removePositionCategory(INIFILE_CATEGORY);
                }
                catch (BadPositionCategoryException e) {
                    e.printStackTrace();
                }
            }
            mChildren.clear();
            mLines.clear();
            mErrors.clear();
            mWarnings.clear();
            mDirty = false;
            notifyListeners();
        }
    }

    private int[] getLineRange(IDocument doc, int offset, int length)
    {
        int startLine = -1;
        int endLine = -1;
        try {
            startLine = doc.getLineOfOffset(offset);
            endLine = startLine;
            if(length > 0) {
                endLine = doc.getLineOfOffset(offset+length);
            }
        }
        catch (BadLocationException e) {
            startLine = -1;
            endLine = -1;
        }
        return new int[]{startLine,endLine};
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        if(!mUpdatingDocument) {
            mChangeStartLine = -1;
            mChangeEndLine = -1;
            int[] lineRange = getLineRange(event.getDocument(),event.getOffset(),event.getLength());
            mChangeStartLine = lineRange[0];
            mChangeEndLine = lineRange[1];
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event)
    {
        if(!mUpdatingDocument) {
            mDirty = true;
            int start = mChangeStartLine;
            int end = mChangeEndLine;
            for(int i=start; i <= end; i++) {
                INILine line = (INILine)mLines.get(i);
                mLines.remove(line);
                line.getParent().removeChild(line);
                i--;
                end--;
            }
            int index = 0;
            int childIndex = 0;
            IINIContainer container = this;
            if(mChangeStartLine > 0) {
                INILine previous = (INILine)mLines.get(mChangeStartLine-1);
                //TODO Fix projection annotation
                if(previous instanceof IINIContainer) {
                    childIndex = 0;
                    index = mChildren.indexOf(previous)+1;
                    container = (IINIContainer)previous;
                }
                else {
                    container = previous.getParent();
                    childIndex = container.getChildren().indexOf(previous)+1;
                    index = (container == this?childIndex:mChildren.indexOf(container)+1);
                }
            }
    
            int[] lineRange = getLineRange(event.getDocument(),event.getOffset(),event.getText().length());
            List newLines = parseLines(event.getDocument(),lineRange[0],lineRange[1]);
            for (Iterator iter = newLines.iterator(); iter.hasNext();) {
                INILine line = (INILine)iter.next();
                if(line instanceof IINIContainer) {
                    addChild(index++,line);
                    container = (IINIContainer)line;
                    childIndex = 0;
                }
                else {
                    container.addChild(childIndex++,line);
                }
                mLines.add(mChangeStartLine++,line);
            }
            for(int i=mChangeStartLine; i<mLines.size(); i++) {
                INILine line = (INILine)mLines.get(i);
                if(line instanceof IINIContainer) {
                    break;
                }
                else {
                    line.getParent().removeChild(line);
                    container.addChild(line);
                }
            }
            notifyListeners();
        }
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            buf.append(iter.next());
        }
        return buf.toString();
    }
    
    public INISection findSection(int offset, int length)
    {
        INISection[] sections = getSections();
        if(!Common.isEmptyArray(sections)) {
            int low = 0;
            int high = sections.length-1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                INISection midVal = sections[mid];
                Position p = midVal.getPosition();
                if(p.includes(offset)) {
                    if(positionContains(p,offset,length)) {
                        return midVal;
                    }
                    break;
                }
                else if (p.getOffset() > offset) {
                    high = mid - 1;
                }
                else if (p.getOffset() < offset) {
                    low = mid + 1;
                }
                else {
                    break;
                }
            }
        }
        return null;
    }
    
    public void validate()
    {
        if(mDirty) {
            mErrors.clear();
            mWarnings.clear();
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                ((INILine)iter.next()).validate();
            }
            INISection[] sections = getSections();
            List fieldSections = new ArrayList();
            if(!Common.isEmptyArray(sections)) {
                Map map = new HashMap();
                for (int i = 0; i < sections.length; i++) {
                    Matcher m = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(sections[i].getName());
                    if(m.matches()) {
                        map.put(sections[i],Integer.valueOf(m.group(1)));
                    }
                }
                fieldSections.addAll(map.entrySet());
                Collections.sort(fieldSections, new Comparator(){
                    public int compare(Object o1, Object o2)
                    {
                        Map.Entry e1 = (Map.Entry)o1;
                        Map.Entry e2 = (Map.Entry)o2;
                        return ((Integer)e1.getValue()).compareTo((Integer)e2.getValue());
                    }
                });
            }
            int n = fieldSections.size();
            if(n > 0) {
                int numFields = -1;
                INISection[] section = findSections(InstallOptionsModel.SECTION_SETTINGS);
                if(section.length == 0) {
                    mErrors.add(InstallOptionsPlugin.getFormattedString("settings.section.missing", //$NON-NLS-1$
                            new String[]{InstallOptionsModel.SECTION_SETTINGS}));
                }
                else {
                    INIKeyValue[] keyValue = section[0].findKeyValues(InstallOptionsModel.PROPERTY_NUMFIELDS);
                    if(keyValue.length == 0) {
                        section[0].addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("numfields.section.missing", //$NON-NLS-1$
                                                new String[]{InstallOptionsModel.PROPERTY_NUMFIELDS}));
                    }
                    else {
                        try {
                            numFields = Integer.parseInt(keyValue[0].getValue());
                            if(numFields != n) {
                                keyValue[0].addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("numfields.value.incorrect", //$NON-NLS-1$
                                                            new String[]{InstallOptionsModel.PROPERTY_NUMFIELDS,
                                                                         InstallOptionsModel.SECTION_FIELD_PREFIX}));
                            }
                        }
                        catch(Exception e) {
                            numFields = -1;
                        }
                    }
                }
                int nextIndex = 1;
                Integer numFields2 = new Integer(numFields);
                int missing = 0;
                StringBuffer missingBuf = new StringBuffer();
                for (Iterator iter = fieldSections.iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    int index = ((Integer)entry.getValue()).intValue();
                    if(numFields >= 0 && index > numFields) {
                        ((INISection)entry.getKey()).addProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("field.index.exceeding", //$NON-NLS-1$
                                            new Object[]{InstallOptionsModel.SECTION_FIELD_PREFIX,
                                                        (Integer)entry.getValue(),
                                                        InstallOptionsModel.PROPERTY_NUMFIELDS,
                                                        numFields2}));
                    }
                    else {
                        while(index > nextIndex) {
                            if(missing > 0) {
                                missingBuf.append(", "); //$NON-NLS-1$
                            }
                            missingBuf.append(nextIndex++);
                            missing++;
                        }
                    }
                    nextIndex++;
                }
                if(missing > 0) {
                    mErrors.add(InstallOptionsPlugin.getFormattedString("field.sections.missing", //$NON-NLS-1$
                            new Object[]{InstallOptionsModel.SECTION_FIELD_PREFIX,
                            new Integer(missing),missingBuf.toString()}));
                }
            }
            mDirty = false;
        }
    }
    
    public INIProblem[] getProblems()
    {
        validate();
        List problems = new ArrayList();
        for (Iterator iter = mErrors.iterator(); iter.hasNext();) {
            problems.add(new INIProblem(0,(String)iter.next(), INIProblem.TYPE_ERROR));
        }
        for (Iterator iter = mWarnings.iterator(); iter.hasNext();) {
            problems.add(new INIProblem(0,(String)iter.next(), INIProblem.TYPE_WARNING));
        }
        int n=1;
        for (Iterator iter = mLines.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            for (Iterator iterator = line.getErrors().iterator(); iterator.hasNext();) {
                problems.add(new INIProblem(n,(String)iterator.next(), INIProblem.TYPE_ERROR));
            }
            for (Iterator iterator = line.getWarnings().iterator(); iterator.hasNext();) {
                problems.add(new INIProblem(n,(String)iterator.next(), INIProblem.TYPE_WARNING));
            }
            n++;
        }
        return (INIProblem[])problems.toArray(new INIProblem[problems.size()]);
    }

    public boolean hasErrors()
    {
        validate();
        if(mErrors.size() == 0) {
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
        validate();
        if(mWarnings.size() == 0) {
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                if(((INILine)iter.next()).hasWarnings()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean positionContains(Position position, int offset, int length)
    {
        return (offset >= position.getOffset() && offset+length <= position.getOffset()+position.getLength());
    }
}
