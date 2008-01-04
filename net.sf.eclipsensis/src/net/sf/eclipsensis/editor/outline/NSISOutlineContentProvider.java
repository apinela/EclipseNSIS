/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.EmptyContentProvider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.*;


/**
 * Divides the editor's document into ten segments and provides elements for
 * them.
 */
public class NSISOutlineContentProvider extends EmptyContentProvider implements INSISConstants
{
    private static final NSISOutlineElement[] EMPTY_CHILDREN = new NSISOutlineElement[0];
    public static final String NSIS_OUTLINE = "__nsis_outline"; //$NON-NLS-1$
    public static final String NSIS_OUTLINE_SELECT = "__nsis_outline_select"; //$NON-NLS-1$

    public static final int DEFINE = 0;
    public static final int IF = DEFINE+1;
    public static final int IFDEF = IF+1;
    public static final int IFNDEF = IFDEF+1;
    public static final int IFMACRODEF = IFNDEF+1;
    public static final int IFNMACRODEF = IFMACRODEF+1;
    public static final int ENDIF = IFNMACRODEF+1;
    public static final int MACRO = ENDIF+1;
    public static final int MACROEND = MACRO+1;
    public static final int FUNCTION = MACROEND+1;
    public static final int FUNCTIONEND = FUNCTION+1;
    public static final int SECTION = FUNCTIONEND+1;
    public static final int SECTIONEND = SECTION+1;
    public static final int SUBSECTION = SECTIONEND+1;
    public static final int SUBSECTIONEND = SUBSECTION+1;
    public static final int SECTIONGROUP = SUBSECTIONEND+1;
    public static final int SECTIONGROUPEND = SECTIONGROUP+1;
    public static final int PAGE = SECTIONGROUPEND+1;
    public static final int PAGEEX = PAGE+1;
    public static final int PAGEEXEND = PAGEEX+1;
    public static final int INCLUDE = PAGEEXEND+1;
    public static final int VAR = INCLUDE+1;
    public static final int NAME = VAR + 1;

    private ITextEditor mEditor;
    private IPath mPath = null;
    private IAnnotationModel mAnnotationModel;
    private IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(NSIS_OUTLINE);
    private IPositionUpdater mSelectPositionUpdater = new DefaultPositionUpdater(NSIS_OUTLINE_SELECT);

    private NSISOutlineElement[] mRootElement = null;
    private NSISOutlineContentResources mResources;

    private List mFilteredTypes;

    /**
     * @param page
     */
    public NSISOutlineContentProvider(ITextEditor editor)
    {
        mEditor = editor;
        mResources = NSISOutlineContentResources.getInstance();
        mFilteredTypes = new ArrayList(mResources.getFilteredTypes());
        Collections.sort(mFilteredTypes);
        inputChanged(null, mEditor.getEditorInput());
    }

    public List getFilteredTypes()
    {
        return mFilteredTypes;
    }

    public void setFilteredTypes(List types)
    {
        mFilteredTypes.clear();
        mFilteredTypes.addAll(types);
        mResources.setFilteredTypes(mFilteredTypes);
    }

    private boolean isFiltered(String type)
    {
        return mFilteredTypes.contains(type);
    }

    private NSISOutlineElement openElement(NSISOutlineElement current, NSISOutlineElement element,
                                           int[] invalidParents)
    {
        boolean found = false;
        if(!Common.isEmptyArray(invalidParents)) {
            for(int i=0; i<invalidParents.length; i++) {
                if(mResources.getTypeIndex(current.getType()) == invalidParents[i]) {
                    found = true;
                    break;
                }
            }
        }
        if(!found) {
            current.addChild(element);
            current = element;
        }
        return current;
    }

    private NSISOutlineElement closeElement(IDocument document, NSISOutlineElement current, NSISOutlineElement element,
                                            int[] validTypes) throws BadLocationException, BadPositionCategoryException
    {
        if(!Common.isEmptyArray(validTypes)) {
            List elementsToClose = new ArrayList();
            boolean found = false;
            NSISOutlineElement el = current;
            while(el.getType() != NSISOutlineElement.ROOT && !found) {
                elementsToClose.add(el);
                for (int i = 0; i < validTypes.length; i++) {
                    if(mResources.getTypeIndex(el.getType()) == validTypes[i]) {
                        found = true;
                        current = el.getParent();
                        for (Iterator iter = elementsToClose.iterator(); iter.hasNext();) {
                            el = (NSISOutlineElement)iter.next();
                            el.merge(element.getPosition());
                            document.addPosition(NSIS_OUTLINE,el.getPosition());
                            document.addPosition(NSIS_OUTLINE_SELECT,el.getSelectPosition());
                            if(mAnnotationModel != null) {
                                mAnnotationModel.addAnnotation(new ProjectionAnnotation(), el.getPosition());
                            }
                        }
                        break;
                    }
                }

                if(!found) {
                    el = el.getParent();
                }
            }
        }
        return current;
    }

    private void addLine(IDocument document, NSISOutlineElement current,
                         NSISOutlineElement element) throws BadLocationException, BadPositionCategoryException
    {
        document.addPosition(NSIS_OUTLINE,element.getPosition());
        document.addPosition(NSIS_OUTLINE_SELECT,element.getSelectPosition());
        current.addChild(element);
    }

    /**
     * @param nsisLine
     * @return
     */
    private Position getLinePosition(ITypedRegion[] nsisLine)
    {
        ITypedRegion lastRegion = nsisLine[nsisLine.length-1];
        int length = lastRegion.getOffset()+lastRegion.getLength() - nsisLine[0].getOffset();
        return new Position(nsisLine[0].getOffset(),length);
    }

    private void parse(IDocument document)
    {
        ITypedRegion[] partitions = NSISTextUtility.getNSISPartitions(document);
        if(mAnnotationModel != null) {
            for(Iterator iter = mAnnotationModel.getAnnotationIterator(); iter.hasNext(); ) {
                mAnnotationModel.removeAnnotation((Annotation)iter.next());
            }
            for (int i = 0; i < partitions.length; i++) {
                if(partitions[i].getType().equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                    mAnnotationModel.addAnnotation(new ProjectionAnnotation(), new Position(partitions[i].getOffset(),partitions[i].getLength()));
                }
            }
        }
        ITypedRegion[][] nsisLines = NSISTextUtility.getNSISLines(document, partitions);
        boolean isHeader = (mPath != null && NSH_EXTENSION.equalsIgnoreCase(mPath.getFileExtension()));
        NSISOutlineElement rootElement = new NSISOutlineElement(NSISOutlineElement.ROOT,
                                                EclipseNSISPlugin.getResourceString(isHeader?"outline.root.header.label":"outline.root.installer.label"), //$NON-NLS-1$ //$NON-NLS-2$
                                                null);
        rootElement.setPosition(new Position(0,document.getLength()));
        try {
            document.addPosition(NSIS_OUTLINE,rootElement.getPosition());
            document.addPosition(NSIS_OUTLINE_SELECT,rootElement.getSelectPosition());
        }
        catch (Exception e) {
        }
        if(!Common.isEmptyArray(nsisLines)) {
            NSISOutlineElement current = rootElement;
            for (int i = 0; i < nsisLines.length; i++) {
                NSISOutlineData nsisToken = null;
                ITypedRegion[] typedRegions = nsisLines[i];
                int j = 0;
                if(!Common.isEmptyArray(typedRegions)) {
                    for (; j < typedRegions.length; j++) {
                        String regionType = typedRegions[j].getType();
                        if(regionType.equals(NSISPartitionScanner.NSIS_STRING)) {
                            NSISOutlineRule rule = new NSISOutlineRule(true);
                            IToken token = rule.evaluate(new NSISRegionScanner(document, typedRegions[j]));
                            if(!token.isUndefined()) {
                                nsisToken = (NSISOutlineData)token.getData();
                            }
                            break;
                        }
                        else if(regionType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                            NSISOutlineRule rule = new NSISOutlineRule(false);
                            IToken token = rule.evaluate(new NSISRegionScanner(document, typedRegions[j]));
                            if(token.isWhitespace()) {
                                continue;
                            }
                            else {
                                if(!token.isUndefined()) {
                                    nsisToken = (NSISOutlineData)token.getData();
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                try {
                    if(nsisToken != null) {
                        IRegion region2 = nsisToken.getRegion();
                        Position position = new Position(region2.getOffset(),region2.getLength());
                        StringBuffer name = new StringBuffer(""); //$NON-NLS-1$
                        int type = mResources.getTypeIndex(nsisToken.getType());
                        switch(type) {
                            case DEFINE:
                            case IF:
                            case IFDEF:
                            case IFNDEF:
                            case IFMACRODEF:
                            case IFNMACRODEF:
                            case MACRO:
                            case FUNCTION:
                            case SECTION:
                            case SUBSECTION:
                            case SECTIONGROUP:
                            case PAGE:
                            case PAGEEX:
                            case INCLUDE:
                            case VAR:
                            case NAME:
                                if(j < typedRegions.length) {
                                    ITypedRegion region = null;
                                    int k= j;
                                    int newOffset = position.getOffset()+position.getLength();
                                    int newEnd = typedRegions[k].getOffset()+typedRegions[k].getLength();
                                    if(newOffset < newEnd) {
                                        region = new TypedRegion(newOffset, newEnd-newOffset,typedRegions[j].getType());
                                    }
                                    else {
                                        k++;
                                        if(k < typedRegions.length) {
                                            region = typedRegions[k];
                                        }
                                    }
                                    outer:
                                    while(k<typedRegions.length) {
                                        String regionType = region.getType();
                                        NSISOutlineTextData data;
                                        String temp = null;
                                        if(!regionType.equals(NSISPartitionScanner.NSIS_STRING) &&
                                           !regionType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                                            break;
                                        }
                                        NSISOutlineRule rule = null;
                                        rule = new NSISOutlineRule(regionType.equals(NSISPartitionScanner.NSIS_STRING), false);
                                        NSISRegionScanner regionScanner = new NSISRegionScanner(document, region);

                                        inner:
                                        while(true) {
                                            IToken token = rule.evaluate(regionScanner);
                                            data = (NSISOutlineTextData) token.getData();
                                            String name2 = data.getName();
                                            if(!Common.isEmpty(name2)) {
                                                temp = name2;
                                            }
                                            else {
                                                if(regionScanner.getOffset() > newEnd) {
                                                    k++;
                                                    if(k < typedRegions.length) {
                                                        region = typedRegions[k];
                                                    }
                                                    break inner;
                                                }
                                            }
                                            if(temp != null) {
                                                switch(type) {
                                                    case SECTION:
                                                        if(regionType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                                                            if(temp.equalsIgnoreCase("/o")) { //$NON-NLS-1$
                                                                continue;
                                                            }
                                                        }
                                                        else {
                                                            if(temp.substring(1,temp.length()-1).equalsIgnoreCase("/o")) { //$NON-NLS-1$
                                                                continue;
                                                            }
                                                        }
                                                        if(temp.startsWith("-") || temp.startsWith("!")) { //$NON-NLS-1$ //$NON-NLS-2$
                                                            temp = temp.substring(1);
                                                        }
                                                        if(temp.length() > 0) {
                                                            name.append(temp);
                                                            break outer;
                                                        }
                                                        break;
                                                    case SECTIONGROUP:
                                                    case SUBSECTION:
                                                        if(regionType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                                                            if(temp.equalsIgnoreCase("/e")) { //$NON-NLS-1$
                                                                continue;
                                                            }
                                                        }
                                                        else {
                                                            if(temp.substring(1,temp.length()-1).equalsIgnoreCase("/e")) { //$NON-NLS-1$
                                                                continue;
                                                            }
                                                        }
                                                        if(temp.startsWith("!")) { //$NON-NLS-1$
                                                            temp = temp.substring(1);
                                                        }
                                                        if(temp.length() > 0) {
                                                            name.append(temp);
                                                            break outer;
                                                        }
                                                        break;
                                                    case PAGE:
                                                        if( (regionType.equals(IDocument.DEFAULT_CONTENT_TYPE) && temp.equalsIgnoreCase("custom"))|| //$NON-NLS-1$
                                                            temp.substring(1,temp.length()-1).equalsIgnoreCase("custom")) { //$NON-NLS-1$
                                                           name.append(temp);
                                                        }
                                                        else {
                                                            if(name.length() > 0) {
                                                                name.append(" "); //$NON-NLS-1$
                                                            }
                                                            name.append(temp);
                                                            break outer;
                                                        }
                                                        break;
                                                    default:
                                                        name.append(temp);
                                                        break outer;
                                                }
                                            }
                                            ITypedRegion region3 = data.getRegion();
                                            newOffset = region3.getOffset()+region3.getLength();
                                            newEnd = typedRegions[k].getOffset()+typedRegions[k].getLength();
                                            if(newOffset < newEnd) {
                                                region = new TypedRegion(newOffset, newEnd-newOffset,typedRegions[j].getType());
                                            }
                                            else {
                                                k++;
                                                if(k < typedRegions.length) {
                                                    region = typedRegions[k];
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        Position linePosition = getLinePosition(nsisLines[i]);
                        String text = document.get(linePosition.getOffset(),linePosition.getLength());
                        String text2 = text.trim();
                        if(type == NAME) {
                            if(!isHeader) {
                                String name2 = Common.maybeUnquote(name.toString());
                                if(name2.length() > 0) {
                                    rootElement.setName(name2);
                                    rootElement.setSelectPosition(new Position(linePosition.getOffset()+text.indexOf(text2),text2.length()));
                                }
                            }
                        }
                        else {
                            NSISOutlineElement element = new NSISOutlineElement(nsisToken.getType(), name.toString(), position);
                            element.setPosition(linePosition);
                            element.setSelectPosition(new Position(linePosition.getOffset()+text.indexOf(text2),text2.length()));
                            int currentType = mResources.getTypeIndex(current.getType());
                            switch(type) {
                                case DEFINE:
                                    addLine(document, current, element);
                                    break;
                                case IF:
                                case IFDEF:
                                case IFNDEF:
                                case IFMACRODEF:
                                case IFNMACRODEF:
                                case MACRO:
                                    current = openElement(current, element, null);
                                    break;
                                case ENDIF:
                                    current = closeElement(document, current, element,
                                                         new int[]{IF, IFDEF, IFNDEF, IFMACRODEF, IFNMACRODEF});
                                    break;
                                case MACROEND:
                                    current = closeElement(document, current, element,
                                                         new int[]{MACRO});
                                    break;
                                case FUNCTION:
                                    current = openElement(current, element, new int[]{SECTION,SUBSECTION,SECTIONGROUP,FUNCTION});
                                    break;
                                case FUNCTIONEND:
                                    current = closeElement(document, current, element,
                                                         new int[]{FUNCTION});
                                    break;
                                case SECTION:
                                    current = openElement(current, element, new int[]{SECTION,FUNCTION});
                                    break;
                                case SECTIONEND:
                                    current = closeElement(document, current, element,
                                                         new int[]{SECTION});
                                    break;
                                case SUBSECTION:
                                case SECTIONGROUP:
                                    current = openElement(current, element, new int[]{SECTION,FUNCTION});
                                    break;
                                case SUBSECTIONEND:
                                case SECTIONGROUPEND:
                                    current = closeElement(document, current, element,
                                                         new int[]{SECTIONGROUP,SUBSECTION});
                                    break;
                                case PAGE:
                                case INCLUDE:
                                case VAR:
                                    if(current.getType() == NSISOutlineElement.ROOT || currentType == MACRO ||
                                       currentType == IFDEF || currentType == IFNDEF ||
                                       currentType == IFMACRODEF || currentType == IFNMACRODEF) {
                                        addLine(document, current, element);
                                    }
                                    break;
                                case PAGEEX:
                                    current = openElement(current, element, new int[]{SECTION,SUBSECTION,SECTIONGROUP,FUNCTION});
                                    break;
                                case PAGEEXEND:
                                    current = closeElement(document, current, element,
                                                           new int[]{PAGEEX});
                                    break;
                            }
                        }
                    }
                }
                catch(Exception ex) {
                }
            }
        }

        mRootElement = new NSISOutlineElement[] {rootElement};
    }

    public void inputChanged(Object oldInput, Object newInput)
    {
        if(oldInput == null || newInput == null || !oldInput.equals(newInput)) {
            if (oldInput != null) {
                if (mEditor != null) {
                    IDocumentProvider documentProvider = mEditor.getDocumentProvider();
                    if (documentProvider != null) {
                        IDocument document = documentProvider.getDocument(oldInput);
                        if (document != null) {
                            try {
                                document.removePositionCategory(NSIS_OUTLINE);
                            }
                            catch (BadPositionCategoryException x) {
                            }
                            document.removePositionUpdater(mPositionUpdater);
                            try {
                                document.removePositionCategory(NSIS_OUTLINE_SELECT);
                            }
                            catch (BadPositionCategoryException x) {
                            }
                            document.removePositionUpdater(mSelectPositionUpdater);
                        }
                    }
                }
                mAnnotationModel = null;
            }

            mRootElement = null;
            mPath = null;
            if (newInput != null) {
                IPathEditorInput pathInput = NSISEditorUtilities.getPathEditorInput(newInput);
                if(pathInput != null) {
                    mPath = pathInput.getPath();
                }
                mAnnotationModel = (IAnnotationModel) mEditor.getAdapter(ProjectionAnnotationModel.class);
                if (mEditor != null) {
                    IDocumentProvider documentProvider = mEditor.getDocumentProvider();
                    if (documentProvider != null) {
                        IDocument document = documentProvider.getDocument(newInput);
                        if (document != null) {
                            document.addPositionCategory(NSIS_OUTLINE);
                            document.addPositionUpdater(mPositionUpdater);
                            document.addPositionCategory(NSIS_OUTLINE_SELECT);
                            document.addPositionUpdater(mSelectPositionUpdater);

                            parse(document);
                        }
                    }
                }
            }
        }
    }

    /*
     * @see IContentProvider#inputChanged(Viewer, Object, Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        inputChanged(oldInput, newInput);
    }

    /*
     * @see IContentProvider#dispose
     */
    public void dispose()
    {
        if(mRootElement != null) {
            mRootElement = null;
        }
    }

    /*
     * @see IContentProvider#isDeleted(Object)
     */
    public boolean isDeleted(Object element)
    {
        return false;
    }

    /*
     * @see IStructuredContentProvider#getElements(Object)
     */
    public Object[] getElements(Object element)
    {
        return mRootElement;
    }

    /*
     * @see ITreeContentProvider#hasChildren(Object)
     */
    public boolean hasChildren(Object element)
    {
        if(element instanceof NSISOutlineElement) {
            for (Iterator iter = ((NSISOutlineElement)element).getChildren().iterator(); iter.hasNext();) {
                NSISOutlineElement child = (NSISOutlineElement)iter.next();
                if(!isFiltered(child.getType())) {
                    return true;
                }
                else if(hasChildren(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * @see ITreeContentProvider#getParent(Object)
     */
    public Object getParent(Object element)
    {
        if (element instanceof NSISOutlineElement) {
            NSISOutlineElement parent = ((NSISOutlineElement)element).getParent();
            if(parent != null) {
                if(isFiltered(parent.getType())) {
                    return getParent(parent);
                }
            }
            return parent;
        }
        else {
            return null;
        }
    }

    /*
     * @see ITreeContentProvider#getChildren(Object)
     */
    public Object[] getChildren(Object element)
    {
        return getChildren(element, true);
    }

    private Object[] getChildren(Object element, boolean filtered)
    {
        NSISOutlineElement[] children = EMPTY_CHILDREN;
        if (element instanceof NSISOutlineElement) {
            List list = new ArrayList();
            addChildren((NSISOutlineElement)element, list, filtered);
            children = (NSISOutlineElement[])list.toArray(children);
        }
        return children;
    }

    private void addChildren(NSISOutlineElement element, List list, boolean filtered)
    {
        for (Iterator iter = element.getChildren().iterator(); iter.hasNext();) {
            NSISOutlineElement child = (NSISOutlineElement)iter.next();
            if(filtered && isFiltered(child.getType())) {
                addChildren(child, list, filtered);
            }
            else {
                list.add(child);
            }
        }
    }

    private boolean positionContains(Position position, int offset, int length)
    {
        return (offset >= position.getOffset() && offset+length <= position.getOffset()+position.getLength());
    }

    public NSISOutlineElement findElement(int offset, int length)
    {
        return findElement(mRootElement, offset, length);
    }

    private NSISOutlineElement findElement(Object[] elements, int offset, int length)
    {
        if(!Common.isEmptyArray(elements)) {
            int low = 0;
            int high = elements.length-1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                NSISOutlineElement midVal = (NSISOutlineElement)elements[mid];
                Position position = midVal.getPosition();
                if(position.includes(offset)) {
                    if(positionContains(position,offset,length)) {
                        NSISOutlineElement val = null;
                        if(hasChildren(midVal)) {
                            val = findElement(getChildren(midVal),offset,length);
                        }
                        return (val == null?midVal:val);
                    }
                    break;
                }
                else if (position.getOffset() > offset) {
                    high = mid - 1;
                }
                else if (position.getOffset() < offset) {
                    low = mid + 1;
                }
                else {
                    break;
                }
            }
        }
        return null;
    }

    public void refresh()
    {
        if(mEditor != null) {
            if(mAnnotationModel == null) {
                mAnnotationModel = (IAnnotationModel) mEditor.getAdapter(ProjectionAnnotationModel.class);
            }
            parse(mEditor.getDocumentProvider().getDocument(mEditor.getEditorInput()));
        }
    }

    private class NSISOutlineRule implements IRule, INSISConstants
    {
        private boolean mIsString;
        private boolean mMatchKeywords;

        public NSISOutlineRule(boolean isString)
        {
            this(isString, true);
        }

        /**
         * @param isString
         * @param matchKeywords
         */
        public NSISOutlineRule(boolean isString, boolean matchKeywords)
        {
            mIsString = isString;
            mMatchKeywords = matchKeywords;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
         */
        public IToken evaluate(ICharacterScanner scanner)
        {
            boolean nonWhiteSpaceFound = false;

            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            int c;
            int offset = ((NSISScanner)scanner).getOffset();
            while((c = scanner.read()) != ICharacterScanner.EOF) {
                outer: {
                    if(c == LINE_CONTINUATION_CHAR) {
                        int c2 = scanner.read();
                        if(NSISTextUtility.delimitersDetected(scanner,c2)) {
                            continue;
                        }
                        else {
                            scanner.unread();
                        }
                    }
                    if(mIsString) {
                        for (int i= 0; i < QUOTE_ESCAPE_SEQUENCES.length; i++) {
                            if (c == QUOTE_ESCAPE_SEQUENCES[i][0] && NSISTextUtility.sequenceDetected(scanner, QUOTE_ESCAPE_SEQUENCES[i], true, false)) {
                                buf.append(QUOTE_ESCAPE_SEQUENCES[i][QUOTE_ESCAPE_SEQUENCES[i].length-1]);
                                break outer;
                            }
                        }
                        for (int i= 0; i < WHITESPACE_ESCAPE_SEQUENCES.length; i++) {
                            if (c == WHITESPACE_ESCAPE_SEQUENCES[i][0] && NSISTextUtility.sequenceDetected(scanner, WHITESPACE_ESCAPE_SEQUENCES[i], false, false)) {
                                switch(WHITESPACE_ESCAPE_SEQUENCES[i][WHITESPACE_ESCAPE_SEQUENCES[i].length-1])
                                {
                                    case 'n':
                                        buf.append('\n');
                                        break;
                                    case 'r':
                                        buf.append('\r');
                                        break;
                                    case 't':
                                        buf.append('\t');
                                        break;
                                }
                                break outer;
                            }
                        }

                        buf.append((char)c);
                    }
                    else {
                        if(Character.isWhitespace((char)c)) {
                            if(nonWhiteSpaceFound) {
                                scanner.unread();
                                break;
                            }
                        }
                        else {
                            if(!nonWhiteSpaceFound) {
                                nonWhiteSpaceFound = true;
                            }
                            buf.append((char)c);
                        }
                    }
                }
            }

            int offset2 = ((NSISScanner)scanner).getOffset();
            String text = buf.toString();
            return createToken(text,offset,offset2-offset);
        }

        protected IToken createToken(String text, int startOffset, int length)
        {
            if(mMatchKeywords) {
                if(text.length()==0 && !mIsString) {
                    return Token.WHITESPACE;
                }
                else {
                    if(mIsString) {
                        if(text.length() > 0) {
                            if(text.length() > 1 && text.charAt(0) == text.charAt(text.length()-1)) {
                                text = text.substring(1,text.length()-1);
                            }
                            else {
                                text = text.substring(1);
                            }
                        }
                    }
                    String type = mResources.getType(text);

                    return (type == null?Token.UNDEFINED:new Token(new NSISOutlineData(type, new Region(startOffset,length))));
                }
            }
            else {
                return new Token(new NSISOutlineTextData(text, new TypedRegion(startOffset,length,(mIsString?NSISPartitionScanner.NSIS_STRING:IDocument.DEFAULT_CONTENT_TYPE))));
            }
        }
    }

    private class NSISOutlineData
    {
        private String  mType;
        private IRegion mRegion;

        /**
         * @param type
         * @param region
         */
        public NSISOutlineData(String type, IRegion region)
        {
            mType = type;
            mRegion = region;
        }

        /**
         * @return Returns the region.
         */
        public IRegion getRegion()
        {
            return mRegion;
        }

        /**
         * @return Returns the type.
         */
        public String getType()
        {
            return mType;
        }
    }

    private class NSISOutlineTextData
    {
        private String mName;
        private ITypedRegion mRegion;

        /**
         * @param name
         * @param region
         */
        public NSISOutlineTextData(String name, ITypedRegion region)
        {
            mName = name;
            mRegion = region;
        }
        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return mName;
        }
        /**
         * @return Returns the region.
         */
        public ITypedRegion getRegion()
        {
            return mRegion;
        }
    }
}