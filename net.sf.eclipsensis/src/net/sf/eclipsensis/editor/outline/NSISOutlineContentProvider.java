/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.util.Iterator;
import java.util.Stack;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;
import net.sf.eclipsensis.editor.text.NSISRegionScanner;
import net.sf.eclipsensis.editor.text.NSISScanner;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Divides the editor's document into ten segments and provides elements for
 * them.
 */
public class NSISOutlineContentProvider implements ITreeContentProvider, INSISConstants
{
    public static final String NSIS_OUTLINE = "__nsis_outline"; //$NON-NLS-1$

    public static final int DEFINE = 0;
    public static final int IFDEF = DEFINE+1;
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
    public static final int SECTIONGROUP = SECTIONEND+1;
    public static final int SECTIONGROUPEND = SECTIONGROUP+1;
    public static final int PAGE = SECTIONGROUPEND+1;
    public static final int PAGEEX = PAGE+1;
    public static final int PAGEEXEND = PAGEEX+1;
    
    private static final int ROOT = Integer.MAX_VALUE;
    
    private static final String[] cOutlineKeywordNames = {"!define", "!ifdef", "!ifndef", "!ifmacrodef",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                         "!ifnmacrodef", "!endif", "!macro", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                         "Function", "FunctionEnd", "Section", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                         "SubSection", "SubSectionEnd", "Page", "PageEx",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                         "Pageexend"}; //$NON-NLS-1$

    public static final String[] OUTLINE_KEYWORDS = new String[cOutlineKeywordNames.length];
    
    public static final Image[] OUTLINE_IMAGES = new Image[cOutlineKeywordNames.length];
    
    static {
        loadOutlineKeywordsAndImages();
    }
    
    /**
     * 
     */
    public static void loadOutlineKeywordsAndImages()
    {
        for(int i=0; i<cOutlineKeywordNames.length; i++) {
            OUTLINE_KEYWORDS[i] = NSISKeywords.getKeyword(cOutlineKeywordNames[i]);
            OUTLINE_IMAGES[i] = ImageManager.getImage(EclipseNSISPlugin.getResourceString(new StringBuffer("outline.").append( //$NON-NLS-1$
                                                     cOutlineKeywordNames[i].toLowerCase().replaceAll("!","")).append(".icon").toString(),null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private ITextEditor mEditor;
    private IAnnotationModel mAnnotationModel;
    private IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(NSIS_OUTLINE);

    private NSISOutlineElement[] mOutlineElements = null;
    
    /**
     * @param page
     */
    public NSISOutlineContentProvider(ITextEditor editor)
    {
        mEditor = editor;
        inputChanged(null, mEditor.getEditorInput());
    }
    
    private NSISOutlineElement openElement(NSISOutlineElement current, NSISOutlineElement element,
                                           int[] invalidParents)
    {
        boolean found = false;
        if(!Common.isEmptyArray(invalidParents)) {
            for(int i=0; i<invalidParents.length; i++) {
                if(current.getType() == invalidParents[i]) {
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
            while(current.getType() != ROOT) {
                boolean found = false;
                for (int i = 0; i < validTypes.length; i++) {
                    if(current.getType() == validTypes[i]) {
                        found = true;
                        break;
                    }
                }
                NSISOutlineElement parent = current.getParent();
                if(found) {
                    current.merge(element.getPosition());
                    document.addPosition(NSIS_OUTLINE,current.getPosition());
                    if(mAnnotationModel != null) {
                        mAnnotationModel.addAnnotation(new ProjectionAnnotation(), current.getPosition());
                    }
                }
                else {
                    parent.removeChild(current);
                }
                current = parent;
                if(found) {
                    break;
                }
            }
        }
        return current;
    }
    
    private void addLine(IDocument document, NSISOutlineElement current, 
                         NSISOutlineElement element) throws BadLocationException, BadPositionCategoryException
    {
        document.addPosition(NSIS_OUTLINE,element.getPosition());
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

    /*
        private IAnnotationModel getAnnotationModel(ITextEditor editor) {
            return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
        }
        
        public void run() {
            ITextEditor editor= getTextEditor();
            ISelection selection= editor.getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection textSelection= (ITextSelection) selection;
                if (!textSelection.isEmpty()) {
                    IAnnotationModel model= getAnnotationModel(editor);
                    if (model != null) {
                        
                        int start= textSelection.getStartLine();
                        int end= textSelection.getEndLine();
                        
                        try {
                            IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
                            int offset= document.getLineOffset(start);
                            int endOffset= document.getLineOffset(end + 1);
                            Position position= new Position(offset, endOffset - offset);
                            model.addAnnotation(new ProjectionAnnotation(), position);
                        } catch (BadLocationException x) {
                            // ignore
                        }
                    }
                }
            }
        }
 */

    private void parse(IDocument document)
    {
        ITypedRegion[][] nsisLines = NSISTextUtility.getNSISLines(document);
        if(mAnnotationModel != null) {
            for(Iterator iter = mAnnotationModel.getAnnotationIterator(); iter.hasNext(); ) {
                mAnnotationModel.removeAnnotation((Annotation)iter.next());
            }
        }
        if(!Common.isEmptyArray(nsisLines)) {
            NSISOutlineElement rootElement = new NSISOutlineElement(ROOT,null);
            Stack parents = new Stack();
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
                        Position position = new Position(nsisToken.mRegion.getOffset(),nsisToken.mRegion.getLength());
                        StringBuffer name = new StringBuffer(""); //$NON-NLS-1$
                        switch(nsisToken.mType) {
                            case DEFINE:
                            case IFDEF:
                            case IFNDEF:
                            case IFMACRODEF:
                            case IFNMACRODEF:
                            case MACRO:
                            case FUNCTION:
                            case SECTION:
                            case SECTIONGROUP:
                            case PAGE:
                            case PAGEEX:
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

                                        while(true) {
                                            IToken token = rule.evaluate(regionScanner);
                                            data = (NSISOutlineTextData) token.getData();
                                            if(!Common.isEmpty(data.mName)) {
                                                temp = data.mName;
                                            }
                                            if(temp != null) {
                                                if(nsisToken.mType == SECTION) {
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
                                                }
                                                if(nsisToken.mType == SECTIONGROUP) {
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
                                                }
                                                else if(nsisToken.mType == PAGE) {
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
                                                }
                                                else {
                                                    name.append(temp);
                                                    break outer;
                                                }
                                            }
                                            newOffset = data.mRegion.getOffset()+data.mRegion.getLength();
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
                        NSISOutlineElement element = new NSISOutlineElement(nsisToken.mType, name.toString(), position);
                        element.setPosition(getLinePosition(nsisLines[i]));
                        switch(nsisToken.mType) {
                            case DEFINE:
                                addLine(document, current, element);
                                break;
                            case IFDEF:
                            case IFNDEF:
                            case IFMACRODEF:
                            case IFNMACRODEF:
                            case MACRO:
                                current = openElement(current, element, null);
                                break;
                            case ENDIF:
                                current = closeElement(document, current, element, 
                                                     new int[]{IFDEF, IFNDEF, IFMACRODEF, IFNMACRODEF});
                                break;
                            case MACROEND:
                                current = closeElement(document, current, element, 
                                                     new int[]{MACRO});
                                break;
                            case FUNCTION:
                                current = openElement(current, element, new int[]{SECTION,SECTIONGROUP,FUNCTION});
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
                            case SECTIONGROUP:
                                current = openElement(current, element, new int[]{SECTION,SECTIONGROUP,FUNCTION});
                                break;
                            case SECTIONGROUPEND:
                                current = closeElement(document, current, element, 
                                                     new int[]{SECTIONGROUP});
                                break;
                            case PAGE:
                                if(current.getType() == ROOT) {
                                    addLine(document, current, element);
                                }
                                break;
                            case PAGEEX:
                                current = openElement(current, element, new int[]{SECTION,SECTIONGROUP,FUNCTION});
                                break;
                            case PAGEEXEND:
                                current = closeElement(document, current, element, 
                                                       new int[]{PAGEEX});
                                break;
                        }
                    }
                }
                catch(Exception ex) {
                }
            }
            
            mOutlineElements = (NSISOutlineElement[])getChildren(rootElement);
        }
    }

    public void inputChanged(Object oldInput, Object newInput)
    {
        if(oldInput == null || newInput == null || !oldInput.equals(newInput)) {
            if (oldInput != null) {
                IDocument document = mEditor.getDocumentProvider().getDocument(oldInput);
                if (document != null) {
                    try {
                        document.removePositionCategory(NSIS_OUTLINE);
                    }
                    catch (BadPositionCategoryException x) {
                    }
                    document.removePositionUpdater(mPositionUpdater);
                }
                mAnnotationModel = null;
            }
    
            mOutlineElements = null;
    
            if (newInput != null) {
                mAnnotationModel = (IAnnotationModel) mEditor.getAdapter(ProjectionAnnotationModel.class);
                IDocument document = mEditor.getDocumentProvider().getDocument(newInput);
                if (document != null) {
                    document.addPositionCategory(NSIS_OUTLINE);
                    document.addPositionUpdater(mPositionUpdater);
    
                    parse(document);
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
        if (mOutlineElements != null) {
            mOutlineElements = null;
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
        return (mOutlineElements != null?mOutlineElements:new Object[0]);
    }

    /*
     * @see ITreeContentProvider#hasChildren(Object)
     */
    public boolean hasChildren(Object element)
    {
        return (element instanceof NSISOutlineElement &&
                ((NSISOutlineElement)element).getChildren().size() > 0);
    }

    /*
     * @see ITreeContentProvider#getParent(Object)
     */
    public Object getParent(Object element)
    {
        if (element instanceof NSISOutlineElement) {
            return ((NSISOutlineElement)element).getParent();
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
        NSISOutlineElement[] children = new NSISOutlineElement[0];
        if (element instanceof NSISOutlineElement) {
            children = (NSISOutlineElement[])((NSISOutlineElement)element).getChildren().toArray(children);
        }
        return children;
    }
    
    private boolean positionContains(Position position, int offset, int length)
    {
        return (offset >= position.getOffset() && offset+length <= position.getOffset()+position.getLength());
    }

    private boolean positionContains(Position position, Position position2)
    {
        return positionContains(position, position2.getOffset(), position2.getLength());
    }

    public NSISOutlineElement findElement(int offset, int length)
    {
        return findElement(mOutlineElements, offset, length);
    }

    public NSISOutlineElement findElement(Object[] elements, int offset, int length)
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
                    int type = -1;
    
                    for (int i = 0; i < OUTLINE_KEYWORDS.length; i++) {
                        if(text.equalsIgnoreCase(OUTLINE_KEYWORDS[i])) {
                            type = i;
                            break;
                        }
                    }
    
                    return (type == -1?Token.UNDEFINED:new Token(new NSISOutlineData(type, new Region(startOffset,length))));
                }
            }
            else {
                return new Token(new NSISOutlineTextData(text, new TypedRegion(startOffset,length,(mIsString?NSISPartitionScanner.NSIS_STRING:IDocument.DEFAULT_CONTENT_TYPE))));
            }
        }
    }
    
    private class NSISOutlineData
    {
        int mType;
        IRegion mRegion;
        
        /**
         * @param type
         * @param region
         */
        public NSISOutlineData(int type, IRegion region)
        {
            mType = type;
            mRegion = region;
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
    }
}