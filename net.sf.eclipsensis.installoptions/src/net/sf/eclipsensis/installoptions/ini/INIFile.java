/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;

public class INIFile implements IDocumentListener, IINIContainer
{
    private static String STRING_CR = new String("\r"); //$NON-NLS-1$
    private static String STRING_LF = new String("\n"); //$NON-NLS-1$
    private static String STRING_CRLF = new String("\r\n"); //$NON-NLS-1$
    
    public static final int INIFILE_CONNECTED = 0;
    public static final int INIFILE_MODIFIED = 1;
    public static final int INIFILE_DISCONNECTED = 2;

    public static final String INIFILE_CATEGORY = "__installoptions_inifile"; //$NON-NLS-1$

    private List mChildren = new ArrayList();
    private IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(INIFILE_CATEGORY);
    private List mLines = new ArrayList();
    private List mListeners = new ArrayList();
    private int mChangeStartLine = -1;
    private int mChangeEndLine = -1;
    private IDocument mDocument = null;

    private boolean mDirty = false;
    private List mProblems = new ArrayList();
    private boolean mErrors = false;
    private boolean mWarnings = false;
    private boolean mUpdatingDocument = false;
    
    private int mValidateFixMode = INILine.VALIDATE_FIX_NONE;

    public boolean isDirty()
    {
        return mDirty;
    }

    public void setDirty(boolean dirty)
    {
        mDirty = dirty;
    }

    public INIFile copy()
    {
        INIFile copy = new INIFile();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = ((INILine)iter.next()).copy();
            copy.addChild(line);
        }
        return copy;
    }

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

    private void notifyListeners(int event)
    {
        IINIFileListener[] listeners = (IINIFileListener[])mListeners.toArray(new IINIFileListener[mListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].iniFileChanged(this, event);
        }
    }

    public INILine getLineAtOffset(int offset)
    {
        int start = 0;
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            if(line instanceof INISection) {
                Position pos = ((INISection)line).getPosition();
                line = ((INISection)line).getLineAtOffset(offset);
                if(line != null) {
                    return line;
                }
                start += pos.length;
            }
            else {
                if(offset >= start && offset < start+line.getLength()) {
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
            if(child instanceof INISection) {
                Position pos = ((INISection)child).getPosition();
                return new Position(pos.offset,child.getLength());
            }
            else {
                int offset = 0;
                for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                    INILine line = (INILine)iter.next();
                    if(line instanceof INISection) {
                        break;
                    }
                    else if(line == child) {
                        return new Position(offset, line.getLength());
                    }
                    else {
                        offset += line.getLength();
                    }
                }
            }
        }
        return null;
    }

    public void addChild(INILine line)
    {
        int index = mChildren.size();
        if(line instanceof INISection) {
            Position pos = ((INISection)line).getPosition();
            if(pos != null) {
                for(int i=0; i<mChildren.size(); i++) {
                    INILine child = (INILine)mChildren.get(i);
                    if(child instanceof INISection) {
                        Position pos2 = ((INISection)child).getPosition();
                        if(pos2 == null || pos.getOffset() < pos2.getOffset() || 
                                (pos.getOffset()==pos2.getOffset() && pos.getLength() < pos2.getLength())) {
                            index = i;
                            break;
                        }
                    }
                }
            }
        }
        addChild(index,line);
    }

    public void addChild(int index, INILine line)
    {
        mChildren.add(index, line);
        line.setParent(this);
        setDirty(true);
    }

    public void removeChild(INILine line)
    {
        mChildren.remove(line);
        line.setParent(null);
        setDirty(true);
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

    public IDocument getDocument()
    {
        return mDocument;
    }

    private static INIComment parseComment(String text, String delimiter)
    {
        if(text.startsWith(";")) { //$NON-NLS-1$
            return new INIComment(text, delimiter);
        }
        return null;
    }

    private static INISection parseSection(String text, String delimiter)
    {
        if(text.startsWith("[")) { //$NON-NLS-1$
            int m = text.indexOf('[');
            int n = text.indexOf(']',m+1);
            if(n > 0) {
                String name = text.substring(m+1,n).trim();
                if(Character.isLetter(name.charAt(0))) {
                    INISection section = new INISection(text, delimiter, name.trim());
                    return section;
                }
            }
        }
        return null;
    }

    private static INIKeyValue parseKeyValue(String text, String delimiter)
    {
        if(text.length() > 0) {
            int n = text.indexOf('=');
            if (Character.isLetter(text.charAt(0)) && n > 0) {
                INIKeyValue keyValue = new INIKeyValue(text, delimiter,
                                                       text.substring(0,n).trim(),
                                                       text.substring(n+1).trim());
                return keyValue;
            }
        }
        return null;
    }

    public void update()
    {
        update(INILine.VALIDATE_FIX_NONE);
    }

    public void update(int fixFlag)
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
        validate(fixFlag);
        setDirty(false);
    }

    public void updateDocument()
    {
        if(mDocument != null) {
            try {
                mUpdatingDocument = true;
                mDocument.set(toString());
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
        setDirty(true);
        mDocument = doc;
        doc.addPositionCategory(INIFILE_CATEGORY);
        doc.addDocumentListener(this);
        doc.addPositionUpdater(mPositionUpdater);
        int lineCount = doc.getNumberOfLines();
        IINIContainer container = this;
        List lines = parseLines(doc,0,lineCount-1);
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
        notifyListeners(INIFILE_CONNECTED);
    }

    public void save(File file)
    {
       BufferedWriter writer = null;
       try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(toString());
        }
        catch (IOException e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(writer);
        }
    }

    public static INIFile load(File file)
    {
        try {
            return load(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return null;
        }
    }

    public static INIFile load(Reader r)
    {
        INIFile iniFile = new INIFile();
        BufferedReader br = null;
        try {
            if(r instanceof BufferedReader) {
                br = (BufferedReader)r;
            }
            else {
                br = new BufferedReader(r);
            }
            String delimiter;
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            IINIContainer container = iniFile;
            int n = br.read();
            while(n != -1) {
                char c = (char)n;
                n = br.read();
                switch(c) {
                    case SWT.CR:
                        if((char)n != SWT.LF) {
                            delimiter = STRING_CR;
                        }
                        else {
                            delimiter = STRING_CRLF;
                        }
                        break;
                    case SWT.LF:
                        delimiter = STRING_LF;
                        break;
                    default:
                        buf.append(c);
                        continue;
                }
                container = loadLine(iniFile, container, buf.toString(),delimiter);
                buf.setLength(0);
                delimiter = null;
            }
            if(buf.length() > 0) {
                container = loadLine(iniFile, container, buf.toString(),null);
            }
        }
        catch (Exception e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(br);
        }

        return iniFile;
    }

    private static IINIContainer loadLine(INIFile iniFile, IINIContainer container, String text, String delimiter)
    {
        INILine line = parse(text.trim(), delimiter);
        iniFile.mLines.add(line);
        if(line instanceof IINIContainer) {
            iniFile.addChild(line);
            container = (IINIContainer)line;
        }
        else {
            container.addChild(line);
        }
        return container;
    }

    private List parseLines(IDocument doc, int startLine, int endLine)
    {
        List lines = new ArrayList();
        for(int i=startLine; i<= endLine; i++) {
            try {
                IRegion region = doc.getLineInformation(i);
                String text = doc.get(region.getOffset(),region.getLength());
                INILine line = parse(text.trim(),doc.getLineDelimiter(i));
                if(line instanceof INISection) {
                    Position pos = new Position(region.getOffset(),line.getLength());
                    ((INISection)line).setPosition(pos);
                    try {
                        doc.addPosition(INIFILE_CATEGORY,pos);
                    }
                    catch (BadPositionCategoryException e1) {
                        InstallOptionsPlugin.getDefault().log(e1);
                    }
                }
                lines.add(line);
            }
            catch (BadLocationException e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }
        return lines;
    }

    private static INILine parse(String text, String delimiter)
    {
        INILine line;
        line = parseComment(text, delimiter);
        if(line == null) {
            line = parseSection(text, delimiter);
            if(line == null) {
                line = parseKeyValue(text, delimiter);
                if(line == null) {
                    line = new INILine(text, delimiter);
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
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
            mChildren.clear();
            mLines.clear();
            mProblems.clear();
            mErrors = false;
            mWarnings = false;
            setDirty(false);
            notifyListeners(INIFILE_DISCONNECTED);
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
            setDirty(true);
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

            String text = event.getText();
            int[] lineRange = getLineRange(event.getDocument(),event.getOffset(),(text==null?0:text.length()));
            List newLines = parseLines(event.getDocument(),lineRange[0],lineRange[1]);
            for (Iterator iter = newLines.iterator(); iter.hasNext();) {
                INILine line = (INILine)iter.next();
                if(line instanceof IINIContainer) {
                    addChild(index++,line);
                    container = (IINIContainer)line;
                    childIndex = 0;
                }
                else {
                    if(container == this) {
                        addChild(index++,line);
                    }
                    else {
                        container.addChild(childIndex++,line);
                    }
                }
                mLines.add(mChangeStartLine++,line);
            }
            for(int i=mChangeStartLine; i<mLines.size(); i++) {
                INILine line = (INILine)mLines.get(i);
                if(line instanceof IINIContainer) {
                    break;
                }
                else if(line.getParent() != container) {
                    line.getParent().removeChild(line);
                    container.addChild(line);
                }
            }
            notifyListeners(INIFILE_MODIFIED);
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
                Position p = midVal.calculatePosition();
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

    public int getValidateFixMode()
    {
        return mValidateFixMode;
    }

    public void setValidateFixMode(int validateFixMode)
    {
        mValidateFixMode = validateFixMode;
    }

    public void validate()
    {
        validate(false);
    }

    public void validate(boolean force)
    {
        validate(mValidateFixMode,force);
    }

    public void validate(int fixFlag)
    {
        validate(fixFlag, false);
    }

    private void addProblem(INIProblem problem)
    {
        if(!mProblems.contains(problem)) {
            mProblems.add(problem);
            mErrors = mErrors || INIProblem.TYPE_ERROR.equals(problem.getType());
            mWarnings = mWarnings || INIProblem.TYPE_WARNING.equals(problem.getType());
        }
    }

    public void validate(int fixFlag, boolean force)
    {
        if(mDirty || force) {
            mProblems.clear();
            mErrors = false;
            mWarnings = false;
            for (int i=0; i < mChildren.size(); i++) {
                ((INILine)mChildren.get(i)).validate(fixFlag);
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
            int numFields = -1;
            INISection[] section = findSections(InstallOptionsModel.SECTION_SETTINGS);
            if(section.length == 0) {
                if(n > 0) {
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        section = new INISection[] {new INISection(InstallOptionsModel.SECTION_SETTINGS)};
                        addChild(section[0]);
                        mLines.add(section[0]);
                        INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_NUMFIELDS);
                        keyValue.setValue(Integer.toString(n));
                        section[0].addChild(keyValue);
                        mLines.add(keyValue);
                    }
                    else {
                        addProblem(new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("settings.section.missing", //$NON-NLS-1$
                                new String[]{InstallOptionsModel.SECTION_SETTINGS})));
                    }
                }
            }
            
            if(section.length > 0) {
                INIKeyValue[] keyValue = section[0].findKeyValues(InstallOptionsModel.PROPERTY_NUMFIELDS);
                if(keyValue.length == 0) {
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        keyValue = new INIKeyValue[] {new INIKeyValue(InstallOptionsModel.PROPERTY_NUMFIELDS)};
                        keyValue[0].setValue(Integer.toString(n));
                        section[0].addChild(keyValue[0]);
                        int index = mLines.indexOf(section[0]);
                        mLines.add(index+1,keyValue);
                    }
                    else {
                        section[0].addProblem(new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("numfields.section.missing", //$NON-NLS-1$
                                                new String[]{InstallOptionsModel.PROPERTY_NUMFIELDS})));
                    }
                }
                
                if(keyValue.length > 0) {
                    try {
                        numFields = Integer.parseInt(keyValue[0].getValue());
                    }
                    catch(Exception e) {
                        numFields = -1;
                    }
                    if(numFields != n) {
                        if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                            keyValue[0].setValue(Integer.toString(n));
                            keyValue[0].update();
                            numFields = n;
                        }
                        else {
                            keyValue[0].addProblem(new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("numfields.value.incorrect", //$NON-NLS-1$
                                                        new String[]{InstallOptionsModel.PROPERTY_NUMFIELDS,
                                                                     InstallOptionsModel.SECTION_FIELD_PREFIX})));
                        }
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
                    INISection sec = (INISection)entry.getKey();
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        sec.setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[] {new Integer(nextIndex)})); 
                        sec.update();
                    }
                    else {
                        sec.addProblem(new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("field.index.exceeding", //$NON-NLS-1$
                                            new Object[]{InstallOptionsModel.SECTION_FIELD_PREFIX,
                                                        (Integer)entry.getValue(),
                                                        InstallOptionsModel.PROPERTY_NUMFIELDS,
                                                        numFields2})));
                    }
                }
                else if(index > nextIndex) {
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        INISection sec = (INISection)entry.getKey();
                        sec.setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[] {new Integer(nextIndex)})); 
                        sec.update();
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
                }
                nextIndex++;
            }
            if(missing > 0) {
                mProblems.add(new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("field.sections.missing", //$NON-NLS-1$
                        new Object[]{InstallOptionsModel.SECTION_FIELD_PREFIX,
                        new Integer(missing),missingBuf.toString()})));
            }
            if(force) {
                notifyListeners(INIFILE_MODIFIED);
            }
        }
    }

    public INIProblem[] getProblems()
    {
        validate();
        List problems = new ArrayList(mProblems);
        int n=1;
        for (Iterator iter = mLines.iterator(); iter.hasNext();) {
            INILine line = (INILine)iter.next();
            for (Iterator iterator = line.getProblems().iterator(); iterator.hasNext();) {
                INIProblem problem = (INIProblem)iterator.next();
                problem.setLine(n);
                problems.add(problem);
            }
            n++;
        }
        return (INIProblem[])problems.toArray(new INIProblem[problems.size()]);
    }

    public boolean hasErrors()
    {
        validate();
        if(!mErrors) {
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
        if(!mWarnings) {
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
