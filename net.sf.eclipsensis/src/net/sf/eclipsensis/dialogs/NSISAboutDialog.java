/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class NSISAboutDialog extends Dialog implements INSISConstants
{
    private static final Image cAboutImage;
    private static final String cAboutTitle;
    private static final String cAboutHeader;
    private static final String cAboutText;
    private static final String[] cURISchemes = {"http://","https://","ftp://","mailto:","news:"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final boolean[] cURIOpaqueSchemes = {false,false,false,true,true};
    private static final ArrayList cLinks = new ArrayList();
    
    private LinkedHashMap mStyleRanges = new LinkedHashMap();
    private boolean mMouseDown = false;
    private boolean mDragging = false;
    
    static {
        Arrays.sort(cURISchemes,new Comparator() {
            public int compare(Object o1, Object o2)
            {
                return ((String)o2).length()-((String)o1).length();
            }
        });
        cAboutImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("about.icon")); //$NON-NLS-1$
        
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        String name = plugin.getName();
        cAboutTitle = EclipseNSISPlugin.getFormattedString("about.title.format", //$NON-NLS-1$
                                           new Object[]{name});
        
        cAboutHeader = EclipseNSISPlugin.getFormattedString("about.header.format", //$NON-NLS-1$
                                           new Object[]{name, plugin.getVersion()});

        cAboutText = parseAboutText(EclipseNSISPlugin.getResourceString("about.text")); //$NON-NLS-1$
    }
    /**
     * @param parentShell
     */
    public NSISAboutDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(cAboutTitle);
    }
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        // create OK button
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                     true);
    }

    protected Control createDialogArea(Composite parent) {
        final Display display = getShell().getDisplay();
        Color background = JFaceColors.getBannerBackground(display);
        Color foreground = JFaceColors.getBannerForeground(display);
        
        Composite composite = (Composite)super.createDialogArea(parent);
        composite.setBackground(background);
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.LEFT);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(JFaceResources.getBannerFont());
        NSISPreferences prefs = NSISPreferences.getPreferences();
        if(prefs.getNSISExe() != null) {
            StringBuffer buf = new StringBuffer(cAboutHeader).append(INSISConstants.LINE_SEPARATOR);
            buf.append(EclipseNSISPlugin.getFormattedString("about.header.format", //$NON-NLS-1$
                                                new Object[]{EclipseNSISPlugin.getResourceString("makensis.display.name"),  //$NON-NLS-1$
                                                             prefs.getNSISVersion().toString()}));
            label.setText(buf.toString());
        }
        else {
            label.setText(cAboutHeader);
        }
        GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        label.setLayoutData(data);
        
        label = new Label(composite, SWT.CENTER);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setImage(cAboutImage);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
        data.horizontalSpan = 1;
        label.setLayoutData(data);
        
        final StyledText text = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 2;
        data.widthHint = convertWidthInCharsToPixels(80);
        text.setLayoutData(data);
        text.setCaret(null);
        text.setFont(parent.getFont());
        text.setText(cAboutText);
        text.setLayoutData(data);
        text.setCursor(null);
        text.setBackground(background);
        text.setForeground(foreground);
        if(!Common.isEmptyCollection(cLinks)) {
            int i=0;
            foreground = JFaceColors.getHyperlinkText(display);
            for (Iterator iter = cLinks.iterator(); iter.hasNext();) {
                Link link = (Link)iter.next();
                StyleRange styleRange = new StyleRange(link.getRange()[0], link.getRange()[1], foreground, background);
                mStyleRanges.put(styleRange,link.getURI());
                text.setStyleRange(styleRange);
            }
        }
        
        text.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                mMouseDown = true;
            }
            public void mouseUp(MouseEvent e) {
                mMouseDown = false;
                int offset = text.getCaretOffset();
                String link = getLinkAtOffset(offset);
                if (mDragging) {
                    mDragging = false;
                    if (link != null) {
                        text.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
                    }
                } 
                else if (link != null) { 
                    text.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
                    openLink(link);
                    StyleRange selectionRange = getCurrentStyleRange(text);
                    text.setSelectionRange(selectionRange.start, selectionRange.length);
                    text.setCursor(null);
                }
            }
        });

        text.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                if (mMouseDown) {
                    if (!mDragging) {
                        text.setCursor(null);
                    }
                    mDragging = true;
                    return;
                }
                int offset = -1;
                try {
                    offset = text.getOffsetAtLocation(new Point(e.x, e.y));
                } 
                catch (IllegalArgumentException ex) {
                }
                if (offset == -1) {
                    text.setCursor(null);
                }
                else if (getLinkAtOffset(offset) != null) {
                    text.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
                }
                else {
                    text.setCursor(null);
                }
            }
        });

        text.addKeyListener(new KeyAdapter() {
            public void keyPressed (KeyEvent event){
                if(event.character == ' ' || event.character == SWT.CR){
                    int offset = text.getSelection().x + 1;

                    String link = getLinkAtOffset(offset);
                    if (link != null) {    
                        text.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
                        openLink(link);
                        StyleRange selectionRange = getCurrentStyleRange(text);
                        if(selectionRange != null) {
                            text.setSelectionRange(selectionRange.start, selectionRange.length);
                            text.setCursor(null);
                        }
                    }
                    return;
                }   
            }
        });
        
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                switch (e.detail) {
                    case SWT.TRAVERSE_ESCAPE:
                    {
                        e.doit = true;
                        break;
                    }
                    case SWT.TRAVERSE_TAB_NEXT:
                    {
                        Point sel = text.getSelection();
                        int charCount = text.getCharCount();
                        if ((sel.x == charCount) && (sel.y == charCount)){
                            text.setSelection(0);
                        }
                        StyleRange nextRange  = getNextStyleRange(text);
                        if (nextRange == null) {
                            text.setSelection(0);
                            e.doit = true;
                        } else {
                            text.setSelectionRange(nextRange.start, nextRange.length);
                            e.doit = true;
                            e.detail = SWT.TRAVERSE_NONE;
                        }
                        break;
                    }
                    case SWT.TRAVERSE_TAB_PREVIOUS:
                    {
                        Point sel = text.getSelection();
                        if ((sel.x == 0) && (sel.y == 0)) {
                            text.setSelection(text.getCharCount());
                        }
                        StyleRange previousRange = getPreviousStyleRange(text);
                        if (previousRange == null) {
                            text.setSelection(text.getCharCount());
                            e.doit = true;
                        }
                        else {
                            text.setSelectionRange(previousRange.start, previousRange.length);
                            e.doit = true;
                            e.detail = SWT.TRAVERSE_NONE;
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        });
        
        label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        return composite;
    }

    private String getLinkAtOffset(int offset)
    {
        for(Iterator iter=mStyleRanges.keySet().iterator(); iter.hasNext(); ) {
            StyleRange styleRange = (StyleRange)iter.next();
            if(offset >= styleRange.start && offset < (styleRange.start+styleRange.length)) {
                return (String)mStyleRanges.get(styleRange);
            }
        }
        return null;
    }

    private StyleRange getCurrentStyleRange(StyledText st)
    {
        Point sel = st.getSelection();
        int start = sel.x;
        int end = sel.y;
        
        for (Iterator iter=mStyleRanges.keySet().iterator(); iter.hasNext(); ) {
            StyleRange range = (StyleRange)iter.next();
            if(start >= range.start && end < (range.start + range.length)) {
                return range;
            }
        }
        return null;
    }

    private StyleRange getNextStyleRange(StyledText st)
    {
        int end = st.getSelection().y;
        
        for (Iterator iter=mStyleRanges.keySet().iterator(); iter.hasNext(); ) {
            StyleRange range = (StyleRange)iter.next();
            if(end <= range.start) {
                return range;
            }
        }
        return null;
    }

    private StyleRange getPreviousStyleRange(StyledText st)
    {
        int start = st.getSelection().x;
        
        StyleRange previous = null;
        for (Iterator iter=mStyleRanges.keySet().iterator(); iter.hasNext(); ) {
            StyleRange range = (StyleRange)iter.next();
            if(start <= (range.start+range.length-1)) {
                break;
            }
            previous = range;
        }
        return previous;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected Control createButtonBar(Composite parent)
    {
        Control ctl = super.createButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setFocus();
        return ctl;
    }

    public void openLink(String link)
    {
        WorkbenchHelp.displayHelpResource(link); //$NON-NLS-1$
    }
    
    private static String parseAboutText(String aboutText)
    {
        if(!Common.isEmptyArray(cURISchemes)) {
            int[] pos = new int[10];
            int size = 0;
            String tempAboutText = aboutText.toLowerCase();
            for (int j = 0; j < cURISchemes.length; j++) {
                int n = 0;
                while(n>= 0 && n < tempAboutText.length()) {
                    n = tempAboutText.indexOf(cURISchemes[j],n);
                    if(n >= 0) {
                        if(size == pos.length) {
                            pos = (int[])Common.resizeArray(pos,pos.length+10);
                        }
                        pos[size++] = n;
                        n++;
                    }
                }
            }
            if(size > 0) {
                pos = (int[])Common.resizeArray(pos,size);
                Arrays.sort(pos);
                StringBuffer newText = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = aboutText.toCharArray();
                int currPos = 0;
                for (int i = 0; i < pos.length; i++) {
                    newText.append((char[])Common.subArray(chars,currPos,pos[i]));
                    currPos = pos[i];
                    int nextPos = 0;
                    while(i < pos.length-1) {
                        if(pos[i+1] > currPos) {
                            nextPos = pos[i+1];
                            break;
                        }
                        else {
                            i++;
                        }
                    }
                    if(i == (pos.length - 1)) {
                        nextPos = chars.length;
                    }
                    currPos = parseURI(newText,chars,currPos,nextPos);
                }
                if(currPos >=0 && currPos < chars.length) {
                    newText.append((char[])Common.subArray(chars,currPos,chars.length));
                }
                return newText.toString();
            }
        }
        return aboutText;
    }
    
    private static int parseURI(StringBuffer buffer, char[] chars, int startPos, int endPos)
    {
        int[] range = {buffer.length(),0};
        String temp = new String((char[])Common.subArray(chars,startPos,endPos)).toLowerCase();
        StringBuffer tempBuf = new StringBuffer(""); //$NON-NLS-1$
        boolean isOpaque = false;
        for (int i = 0; i < cURISchemes.length; i++) {
            if(temp.startsWith(cURISchemes[i])) {
                tempBuf.append((char[])Common.subArray(chars,startPos,startPos+cURISchemes[i].length()));
                startPos += cURISchemes[i].length();
                isOpaque = cURIOpaqueSchemes[i];
                break;
            }
        }
        boolean found = false;
        int lastPos = -1;
        for (int i = startPos; i < endPos; i++) {
            if(Character.isWhitespace(chars[i])) {
                found = true;
                break;
            }
            lastPos = i;
        }
        if(!found) {
            lastPos = endPos-1;
        }
        while(lastPos >= startPos) {
            char c = chars[lastPos];
            if(!Character.isLetterOrDigit(c)) {
                if(isOpaque || (c != '?' && c != '#')) {
                    lastPos--;
                    continue;
                }
            }
            break;
        }
        
        tempBuf.append((char[])Common.subArray(chars,startPos,lastPos+1));
        startPos = lastPos+1;
        
        try {
            URI uri = new URI(tempBuf.toString());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String authority = uri.getAuthority();
            int port = uri.getPort();
            String path = uri.getPath();
            String query = uri.getQuery();
            String fragment = uri.getFragment();

            if (uri.isOpaque()) {
                buffer.append(uri.getSchemeSpecificPart());
            } 
            else {
                if (scheme != null) {
                    buffer.append(scheme).append(':');
                }
                if (host != null) {
                    buffer.append("//"); //$NON-NLS-1$
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null) {
                        buffer.append(userInfo).append('@');
                    }
                    boolean needBrackets = ((host.indexOf(':') >= 0) && !host.startsWith("[") && !host.endsWith("]")); //$NON-NLS-1$ //$NON-NLS-2$
                    if (needBrackets) {
                        buffer.append('[');
                    }
                    buffer.append(host);
                    if (needBrackets) {
                        buffer.append(']');
                    }
                    if (port != -1) {
                        buffer.append(':').append(port);
                    }
                } 
                else if (authority != null) {
                    buffer.append("//"); //$NON-NLS-1$
                    buffer.append(authority);
                }
                if (path != null) {
                    buffer.append(path);
                }
                if (query != null) {
                    buffer.append('?');
                    buffer.append(query);
                }
            }
            if (fragment != null) {
                buffer.append('#');
                buffer.append(fragment);
            }
            range[1] = buffer.length()-range[0];
            String uriString = uri.toString();
            if(query != null) {
                int n = uriString.lastIndexOf('?');
                uriString = new StringBuffer(uriString.substring(0,n+1)).append("noframes=true&").append( //$NON-NLS-1$
                            uriString.substring(n+1)).toString();
            }
            else {
                if(fragment != null) {
                    int n = uriString.lastIndexOf('#');
                    uriString = new StringBuffer(uriString.substring(0,n)).append("?noframes=true").append( //$NON-NLS-1$
                                uriString.substring(n)).toString();
                }
                else {
                    uriString = uriString + "?noframes=true"; //$NON-NLS-1$
                }
            }
            cLinks.add(new Link(range,uriString));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            buffer.append(tempBuf);
        }
        
        return startPos;
    }

    private static class Link
    {
        private int[] mRange;
        private String mURI;
        
        /**
         * @param range
         * @param uri
         */
        public Link(int[] range, String uri)
        {
            super();
            mRange = range;
            mURI = uri;
        }
        
        /**
         * @return Returns the range.
         */
        public int[] getRange()
        {
            return mRange;
        }
        
        /**
         * @return Returns the uri.
         */
        public String getURI()
        {
            return mURI;
        }
    }
}
