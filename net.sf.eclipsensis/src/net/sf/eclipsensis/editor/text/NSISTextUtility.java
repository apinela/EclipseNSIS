/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.ArrayList;
import java.util.regex.Pattern;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

public class NSISTextUtility implements INSISConstants
{
    public static final IRegion EMPTY_REGION = new Region(0,0);
    private static final Pattern[] cUsagePatterns;
    private static final Pattern[] cHelpPatterns;
    private static final String[] cValidPartitionTypes = {IDocument.DEFAULT_CONTENT_TYPE,
                                                          NSISPartitionScanner.NSIS_STRING};
    
    private static final int INVALID_REGIONS = -1;
    private static final int REGION1_BEFORE_REGION2 = 0;
    private static final int REGION1_OVERLAPS_LEFT_REGION2 = 1;
    private static final int REGION1_CONTAINS_REGION2 = 2;
    private static final int REGION1_OVERLAPS_RIGHT_REGION2 = 3;
    private static final int REGION1_CONTAINED_BY_REGION2= 4;
    private static final int REGION1_AFTER_REGION2 = 5;

    static {
        Pattern keyword = null;
        Pattern define = null;
        try {
            keyword = Pattern.compile("^\\s*((?:!|\\.|un\\.)?[\\w]+)\\s",Pattern.CASE_INSENSITIVE);
        }
        catch(Throwable t) {
            keyword = null;
        }
        try {
            define = Pattern.compile("(\\$(?:\\{[\\w\\.]+\\}|[\\w\\.]+))");
        }
        catch(Throwable t) {
            define = null;
        }
        cUsagePatterns = new Pattern[]{keyword};
        cHelpPatterns = new Pattern[]{keyword,define};
    }
    
    public static int computeOffset(ISourceViewer sourceViewer, boolean hoverOnly)
    {
        if (sourceViewer == null) {
            return -1;
        }
            
        if (sourceViewer instanceof ITextViewerExtension4)  {
            ITextViewerExtension4 extension4= (ITextViewerExtension4) sourceViewer;
            if (extension4.moveFocusToWidgetToken()) {
                return -100;
            }
        }
        
        if (!(sourceViewer instanceof ITextViewerExtension2)) {
            return -1;
        }
            
        ITextViewerExtension2 textViewerExtension2= (ITextViewerExtension2) sourceViewer;
        
        // does a text hover exist?
        ITextHover textHover= textViewerExtension2.getCurrentTextHover();
        int offset;
        if (textHover == null) {
            if(hoverOnly) {
                return -1;
            }
            else {
                offset = sourceViewer.getTextWidget().getCaretOffset();
            }
        }
        else {
            Point hoverEventLocation= textViewerExtension2.getHoverEventLocation();
            offset= computeOffsetAtLocation(sourceViewer, hoverEventLocation.x, hoverEventLocation.y);
        }
        return offset;
    }

    private static int computeOffsetAtLocation(ISourceViewer sourceViewer, int x, int y)
    {
        StyledText styledText= sourceViewer.getTextWidget();
        IDocument document= sourceViewer.getDocument();
        
        if (document == null) {
            return -1;      
        }

        try {
            int widgetLocation= styledText.getOffsetAtLocation(new Point(x, y));
            if (sourceViewer instanceof ITextViewerExtension5) {
                ITextViewerExtension5 extension= (ITextViewerExtension5) sourceViewer;
                return extension.widgetOffset2ModelOffset(widgetLocation);
            } 
            else {
                IRegion visibleRegion= sourceViewer.getVisibleRegion();
                return widgetLocation + visibleRegion.getOffset();
            }
        } 
        catch (IllegalArgumentException e) {
            return -1;  
        }
    }
    
    private static int getOverlapType(IRegion region1, IRegion region2)
    {
        int start1 = region1.getOffset();
        int end1 = start1 + region1.getLength() - 1;
        int start2 = region2.getOffset();
        int end2 = start2 + region2.getLength() - 1;
        if(start1 >= 0 && end1 >= start1 && start2 >= 0 && end2 >= start2) {
            if(start1 < start2)
            {
                if(end1 >= start2) {
                    return (end2 <= end1 ? REGION1_CONTAINS_REGION2 : REGION1_OVERLAPS_LEFT_REGION2);
                }
                else {
                    return REGION1_BEFORE_REGION2;
                }
            }
            else {
                if(start1 <= end2) {
                    return (end2 < end1 ? REGION1_OVERLAPS_RIGHT_REGION2 : REGION1_CONTAINED_BY_REGION2);
                }
                else {
                    return REGION1_AFTER_REGION2;
                }
            }
            
        }
        return INVALID_REGIONS;
    }

    private static IRegion intersection(IRegion region1, IRegion region2)
    {
        int start1 = region1.getOffset();
        int end1 = start1 + region1.getLength() - 1;
        int start2 = region2.getOffset();
        int end2 = start2 + region2.getLength() - 1;
        if(start1 >= 0 && end1 >= 0 && start2 >= 0 && end2 >= 0) {
            if(start1 < start2)
            {
                if(end1 >= start2) {
                    return (end2 <= end1 ? region2 : new Region(start2, (end1-start2+1)));
                }
            }
            else {
                if(start1 <= end2) {
                    return (end2 < end1 ? new Region(start1, (end2-start1+1)) : region1);
                }
            }
            
        }
        return EMPTY_REGION;
    }
    
    public static ITypedRegion[][] getNSISLines(IDocument doc)
    {
        return getNSISLines(doc, getTypedRegions(doc));
    }
    
    public static boolean contains(IRegion region, int offset)
    {
        return (region !=null && offset >= region.getOffset() && offset < region.getOffset()+region.getLength());
    }

    public static int findRegion(IRegion[] regions, int offset)
    {
        if(!Common.isEmptyArray(regions)) {
            int low = 0;
            int high = regions.length-1;

            while (low <= high) {
                int mid = (low + high) >> 1;
                IRegion midVal = regions[mid];
                if(contains(midVal,offset)) {
                    return mid;
                }
                else if (midVal.getOffset() > offset) {
                    high = mid - 1;
                }
                else if (midVal.getOffset() < offset) {
                    low = mid + 1;
                }
                else {
                    break;
                }
            }
        }
        return -1;
    }

    public static ITypedRegion[][] getNSISLines(IDocument doc, int offset)
    {
        ITypedRegion[] typedRegions = null;
        try {
            int linenum = doc.getLineOfOffset(offset);
            IRegion line = doc.getLineInformation(linenum);
            ITypedRegion typedRegion = getTypedRegionAtOffset(doc, line.getOffset());
            if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                if(contains(typedRegion,offset)) {
                    return new ITypedRegion[0][];
                }
                int startOffset = typedRegion.getOffset()+typedRegion.getLength();
                line = new Region(startOffset, line.getOffset()+line.getLength()-startOffset);
            }
            else {
                int linenum2 = linenum-1;
                while(linenum2 >= 0) {
                    IRegion line2 = doc.getLineInformation(linenum2);
                    int endOffset = line2.getOffset()+line2.getLength()-1;
                    if(endOffset >= 0) {
                        typedRegion = getTypedRegionAtOffset(doc, endOffset);
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            break;
                        }
                        if(doc.get(endOffset,1).charAt(0)!=LINE_CONTINUATION_CHAR) {
                            break;
                        }
                        typedRegion = getTypedRegionAtOffset(doc, line2.getOffset());
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            int startOffset = typedRegion.getOffset()+typedRegion.getLength();
                            line = new Region(startOffset, line.getOffset()+line.getLength()-startOffset);
                            break;
                        }
                        else {
                            line = new Region(line2.getOffset(), line.getOffset()+line.getLength()-line2.getOffset());
                            linenum2--;
                        }
                    }
                    else {
                        break;
                    }
                }
            }
            typedRegion = getTypedRegionAtOffset(doc, line.getOffset()+line.getLength()-1);
            if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                if(contains(typedRegion,offset)) {
                    return new ITypedRegion[0][];
                }
                line = new Region(line.getOffset(),typedRegion.getOffset()-line.getOffset());
            }
            else {
                if(doc.get(line.getOffset()+line.getLength()-1,1).charAt(0)==LINE_CONTINUATION_CHAR) {
                    int linenum2 = linenum+1;
                    int numlines = doc.getNumberOfLines();
                    while(linenum2 < numlines) {
                        IRegion line2 = doc.getLineInformation(linenum2);
                        typedRegion = getTypedRegionAtOffset(doc, line2.getOffset());
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            break;
                        }
                        int endOffset = line2.getOffset()+line2.getLength()-1;
                        typedRegion = getTypedRegionAtOffset(doc, endOffset);
                        if(!isValidRegionType(typedRegion.getType(),cValidPartitionTypes)) {
                            line = new Region(line.getOffset(),typedRegion.getOffset()-line.getOffset());
                            break;
                        }
                        else {
                            line = new Region(line.getOffset(), endOffset-line2.getOffset()+1);
                            if(doc.get(endOffset,1).charAt(0)!=LINE_CONTINUATION_CHAR) {
                                break;
                            }
                            else {
                                linenum2++;
                            }
                        }
                    }
                }
            }
            ITypedRegion[] partitions = getTypedRegions(doc);
            if(!Common.isEmptyArray(partitions)) {
                int startIndex = findRegion(partitions,line.getOffset());
                if(startIndex >= 0) {
                    int endIndex = findRegion(partitions,line.getOffset()+line.getLength()-1);
                    if(endIndex >= 0 && endIndex >= startIndex) {
                        typedRegions = new ITypedRegion[endIndex-startIndex+1];
                        for(int i=startIndex; i<endIndex+1; i++) {
                            typedRegions[i-startIndex] = partitions[i];
                        }
                        typedRegions[0] = new TypedRegion(line.getOffset(),typedRegions[0].getOffset()+typedRegions[0].getLength()-line.getOffset(),typedRegions[0].getType());
                        endIndex -= startIndex;
                        typedRegions[endIndex] = new TypedRegion(typedRegions[endIndex].getOffset(),line.getOffset()+line.getLength()-typedRegions[endIndex].getOffset(),typedRegions[endIndex].getType()); 
                    }
                }
            }
        }
        catch (BadLocationException e) {
        }
        return getNSISLines(doc, typedRegions);
    }

    public static ITypedRegion[][] getNSISLines(IDocument doc, ITypedRegion[] typedRegions)
    {
        ArrayList regions = new ArrayList();
        if(doc != null && doc.getLength() > 0) {
            try {
                if(!Common.isEmptyArray(typedRegions)) {
                    String[] delims = (String[])doc.getLegalLineDelimiters().clone();
                    for(int i=0; i<delims.length; i++) {
                        delims[i] = "\\"+delims[i];
                    }
                    
                    int firstLine = doc.getLineOfOffset(typedRegions[0].getOffset());
                    ITypedRegion lastRegion = typedRegions[typedRegions.length-1];
                    int lastLine = doc.getLineOfOffset(lastRegion.getOffset()+lastRegion.getLength()-1);
                    for(int i=firstLine, index = 0; index < typedRegions.length && i<= lastLine; i++) {
                        ArrayList lineRegions = new ArrayList(); 
                        IRegion line = doc.getLineInformation(i);
                        String lineDelim = doc.getLineDelimiter(i);
//                        int start = line.getOffset();
                        int start = Math.max(line.getOffset(),typedRegions[index].getOffset());
                        int end = line.getOffset() + line.getLength() - 1 + (lineDelim != null?lineDelim.length():0);
                        
                        int partitionEnd = typedRegions[index].getOffset() + typedRegions[index].getLength() - 1;
                        String type = typedRegions[index].getType();
                        boolean validPartition = type.equals(NSISPartitionScanner.NSIS_STRING) ||
                                                 type.equals(IDocument.DEFAULT_CONTENT_TYPE);
                        
                        while(end >= start) {
                            if(partitionEnd > end) {
                                if(validPartition) {
                                    String text = doc.get(start, (end-start+1));
                                    boolean found = false;
                                    for(int j=0; j<delims.length; j++) {
                                        if(text.endsWith(delims[j])) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(found) {
                                        i++;
                                        line = doc.getLineInformation(i);
                                        lineDelim = doc.getLineDelimiter(i);
                                        end = line.getOffset() + line.getLength() - 1 + (lineDelim != null?lineDelim.length():0);
                                    }
                                    else {
                                        lineRegions.add(new TypedRegion(start,(end-start+1),type));
                                        break;
                                    }
                                }
                                else {
                                    break;
                                }
                            }
                            else if (partitionEnd < end) {
                                if(validPartition) {
                                    lineRegions.add(new TypedRegion(start,(partitionEnd-start+1),type));
                                }
                                start = partitionEnd+1;
                                index++;
                                if(index < typedRegions.length) {
                                    partitionEnd = typedRegions[index].getOffset() + typedRegions[index].getLength() - 1;
                                    type = typedRegions[index].getType();
                                    validPartition = type.equals(NSISPartitionScanner.NSIS_STRING) ||
                                                     type.equals(IDocument.DEFAULT_CONTENT_TYPE);
                                }
                                else {
                                    break;
                                }
                            }
                            else {
                                if(validPartition) {
                                    lineRegions.add(new TypedRegion(start,(end-start+1),type));
                                }
                                index++;
                                break;
                            }
                        }
                        
                        if(lineRegions.size() > 0) {
                            regions.add(lineRegions.toArray(new ITypedRegion[0]));
                        }
                    }
                }
            }
            catch(BadLocationException e) {
            }
        }
        return (ITypedRegion[][])regions.toArray(new ITypedRegion[0][]);
    }
    
    /**
     * @param doc
     * @return
     * @throws BadLocationException
     */
    private static ITypedRegion[] getTypedRegions(IDocument doc)
    {
        ITypedRegion[] typedRegions;
        try {
            if(doc != null) {
                if (doc instanceof IDocumentExtension3) {
                    try {
                        typedRegions = ((IDocumentExtension3)doc).computePartitioning(NSISPartitionScanner.NSIS_PARTITIONING,0, doc.getLength(),false);
                    }
                    catch (BadPartitioningException e) {
                        typedRegions = doc.computePartitioning(0, doc.getLength());
                    }
                }
                else {
                    typedRegions = doc.computePartitioning(0, doc.getLength());
                }
            }
            else {
                typedRegions = new ITypedRegion[0];
            }
        }
        catch(BadLocationException ex) {
            typedRegions = new ITypedRegion[0];
        }
        return typedRegions;
    }
    
    /**
     * @param regionType
     * @param partitionTypes
     * @return
     */
    private static boolean isValidRegionType(String regionType, String[] partitionTypes)
    {
        boolean found = false;
        for (int i = 0; i < partitionTypes.length; i++) {
            if(partitionTypes[i].equals(regionType)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * @param offset
     * @param doc
     * @param typedRegion
     * @return
     * @throws BadLocationException
     */
    private static ITypedRegion getTypedRegionAtOffset(IDocument doc, int offset) throws BadLocationException
    {
        ITypedRegion typedRegion;
        if (doc instanceof IDocumentExtension3) {
            try {
                typedRegion = ((IDocumentExtension3)doc).getPartition(NSISPartitionScanner.NSIS_PARTITIONING,offset,false);
            }
            catch (BadPartitioningException e) {
                typedRegion = doc.getPartition(offset);
            }
        }
        else {
            typedRegion = doc.getPartition(offset);
        }
        return typedRegion;
    }

    public static boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean lineContinuationAllowed, boolean eofAllowed)
    {
        int c;
        int offset = ((NSISScanner)scanner).getOffset();
        for (int i= 1; i < sequence.length; i++) {
            c = scanner.read();
            if (c == ICharacterScanner.EOF && eofAllowed) {
                return true;
            } 
            
            if (c == LINE_CONTINUATION_CHAR) {
                int c2 = scanner.read();
                if(delimitersDetected(scanner, c2)) {
                    if(lineContinuationAllowed) {
                        i--;
                        continue;
                    }
                    else {
                        unread(scanner, ((NSISScanner)scanner).getOffset() - offset);
                        return false;
                    }
                }
                else {
                    scanner.unread();
                }
            }
    
            if (c != sequence[i]) {
                unread(scanner, ((NSISScanner)scanner).getOffset() - offset);
                return false;
            }
        }
        
        return true;
    }

    public static void unread(ICharacterScanner scanner, int count)
    {
        for (int i= 0; i < count; i++) {
            scanner.unread();
        }
    }

    public static boolean delimitersDetected(ICharacterScanner scanner, int c)
    {
        char[][] delimiters= scanner.getLegalLineDelimiters();
        for (int i= 0; i < delimiters.length; i++) {
            if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i], false, true)) {
                return true;
            }
        }
        return false;
    }

    private static boolean stringEscapeSequencesDetected(ICharacterScanner scanner, int c, char[][] specialSequences, boolean lineContinuationAllowed)
    {
        for (int i= 0; i < specialSequences.length; i++) {
            if (c == specialSequences[i][0] && sequenceDetected(scanner, specialSequences[i], lineContinuationAllowed, true)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean stringEscapeSequencesDetected(ICharacterScanner scanner, int c)
    {
        if(!(stringEscapeSequencesDetected(scanner, c, QUOTE_ESCAPE_SEQUENCES, true))) {
            stringEscapeSequencesDetected(scanner, c, WHITESPACE_ESCAPE_SEQUENCES, false);
        }
        
        return true;
    }
    
    public static String getRegionText(IDocument document, IRegion region)
    {
        String text = null;
        if(document != null && region != null && region.getLength() > 0) {
            NSISTextProcessorRule rule = new NSISTextProcessorRule();
            NSISRegionScanner scanner = new NSISRegionScanner(document, region);
            rule.setTextProcessor(new DefaultTextProcessor());
            IToken token = rule.evaluate(scanner);
            text = (String)token.getData();
        }
        return text;
    }

    public static int insertTabString(StringBuffer buffer, int offsetInLine, int tabWidth)
    {
        if (tabWidth == 0) {
            buffer.append('\t');
            return 1;
        }
        else {
            int remainder= offsetInLine % tabWidth;
            remainder= tabWidth - remainder;
            for (int i= 0; i < remainder; i++) {
                buffer.append(' ');
            }
            return remainder;
        }
    }
}
