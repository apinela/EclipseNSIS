/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.codeassist.NSISInformationUtility;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.IPropertyAdaptable;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;


public class NSISSourceViewer extends ProjectionViewer implements IPropertyChangeListener
{
    public static final int GOTO_HELP = 1000;
    public static final int INSERT_FILE = GOTO_HELP + 1;
    public static final int INSERT_DIRECTORY = INSERT_FILE + 1;
    public static final int INSERT_COLOR = INSERT_DIRECTORY + 1;
    public static final int TABS_TO_SPACES = INSERT_COLOR + 1;
    public static final int TOGGLE_COMMENT = TABS_TO_SPACES + 1;
    public static final int ADD_BLOCK_COMMENT = TOGGLE_COMMENT + 1;
    public static final int REMOVE_BLOCK_COMMENT = ADD_BLOCK_COMMENT + 1;
    
    private IPreferenceStore mPreferenceStore = null;
    private NSISAutoIndentStrategy mAutoIndentStrategy = null;
    private NSISTabConversionStrategy mTabConversionStrategy = null;
    private ILineTracker mLineTracker = null;
    private String[] mConfiguredContentTypes = null;
    private HashSet mPropertyQueue = new HashSet();

   /**
     * @param parent
     * @param ruler
     * @param overviewRuler
     * @param showsAnnotationOverview
     * @param styles
     */
    public NSISSourceViewer(Composite parent, IVerticalRuler ruler,
            IOverviewRuler overviewRuler, boolean showsAnnotationOverview,
            int styles)
    {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
        mLineTracker = new DefaultLineTracker();
    }

    public boolean mustProcessPropertyQueue()
    {
        return mPropertyQueue.size() > 0;
    }

    public void processPropertyQueue()
    {
        HashSet contentTypes = new HashSet();
        for(Iterator iter = mPropertyQueue.iterator(); iter.hasNext(); ) {
            String property = (String)iter.next();
            for(int i=0; i<mConfiguredContentTypes.length; i++) {
                IPresentationDamager damager = fPresentationReconciler.getDamager(mConfiguredContentTypes[i]);
                if(damager instanceof IPropertyAdaptable) {
                    IPropertyAdaptable adaptable = (IPropertyAdaptable)damager;
                    if(adaptable.canAdaptToProperty(mPreferenceStore, property)) {
                        contentTypes.add(mConfiguredContentTypes[i]);
                        adaptable.adaptToProperty(mPreferenceStore, property);
                    }
                }
            }
        }
        
        IDocument doc = getDocument();
        try {
            ITypedRegion[] regions = null;
            if(doc instanceof IDocumentExtension3) {
                regions = ((IDocumentExtension3)doc).computePartitioning(fPartitioning,0,doc.getLength(),false);
            }
            else {
                regions = doc.computePartitioning(0,doc.getLength());
            }
            for (int i = 0; i < regions.length; i++) {
                if(contentTypes.contains(regions[i].getType())) {
                    invalidateTextPresentation(regions[i].getOffset(),regions[i].getLength());
//                    fPresentationReconciler.
                }
            }
        }
        catch (BadPartitioningException e) {
        }
        catch (BadLocationException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ISourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
     */
    public void configure(SourceViewerConfiguration configuration)
    {
        if(configuration instanceof NSISSourceViewerConfiguration) {
            mPreferenceStore = ((NSISSourceViewerConfiguration)configuration).getPreferenceStore();
        }
        super.configure(configuration);
        if(configuration instanceof NSISSourceViewerConfiguration) {
            mAutoIndentStrategy = new NSISAutoIndentStrategy(mPreferenceStore);
            mTabConversionStrategy = new NSISTabConversionStrategy(mPreferenceStore);
            mPreferenceStore.addPropertyChangeListener(this);
            mAutoIndentStrategy.updateFromPreferences();
            mTabConversionStrategy.updateFromPreferences();
            mConfiguredContentTypes = configuration.getConfiguredContentTypes(this);
            for(int i=0; i<mConfiguredContentTypes.length; i++) {
                prependAutoEditStrategy(mAutoIndentStrategy,mConfiguredContentTypes[i]);
                prependAutoEditStrategy(mTabConversionStrategy,mConfiguredContentTypes[i]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     */
    public void unconfigure()
    {
        mAutoIndentStrategy = null;
        mTabConversionStrategy = null;
        if(mPreferenceStore != null) {
            mPreferenceStore.removePropertyChangeListener(this);
            mPreferenceStore = null;
        }
        mConfiguredContentTypes = null;
        super.unconfigure();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        String property = event.getProperty();
        if(property.equals(INSISPreferenceConstants.TAB_WIDTH)||
           property.equals(INSISPreferenceConstants.USE_SPACES_FOR_TABS)) {
            for(Iterator iter=fIndentChars.keySet().iterator(); iter.hasNext(); ) {
                setIndentPrefixes(calculatePrefixes(),(String)iter.next());
            }
            
            for(Iterator iter=fAutoIndentStrategies.keySet().iterator(); iter.hasNext(); ) {
                String contentType = (String)iter.next();
                List list = (List)fAutoIndentStrategies.get(contentType);
                if(!Common.isEmptyCollection(list)) {
                    for (Iterator iter2 = list.iterator(); iter2.hasNext();) {
                        IAutoEditStrategy autoEditStrategy = (IAutoEditStrategy)iter2.next();
                        if(autoEditStrategy instanceof NSISAutoEditStrategy) {
                            ((NSISAutoEditStrategy)autoEditStrategy).updateFromPreferences();
                        }
                    }
                }
            }
        }
        else {
            for(int i=0; i<mConfiguredContentTypes.length; i++) {
                IPresentationDamager damager = fPresentationReconciler.getDamager(mConfiguredContentTypes[i]);
                if(damager instanceof IPropertyAdaptable) {
                    IPropertyAdaptable adaptable = (IPropertyAdaptable)damager;
                    if(adaptable.canAdaptToProperty(mPreferenceStore, property)) {
                        mPropertyQueue.add(property);
                    }
                }
            }
        }
    }
    
    public String[] calculatePrefixes()
    {
        ArrayList list= new ArrayList();

        // prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
                
        int tabWidth= mPreferenceStore.getInt(INSISPreferenceConstants.TAB_WIDTH);
        boolean useSpaces= mPreferenceStore.getBoolean(INSISPreferenceConstants.USE_SPACES_FOR_TABS);
        
        for (int i= 0; i <= tabWidth; i++) {
            StringBuffer prefix= new StringBuffer();

            if (useSpaces) {
                for (int j= 0; j + i < tabWidth; j++)
                    prefix.append(' ');
                
                if (i != 0)
                    prefix.append('\t');                
            } else {    
                for (int j= 0; j < i; j++)
                    prefix.append(' ');
                
                if (i != tabWidth)
                    prefix.append('\t');
            }
            
            list.add(prefix.toString());
        }
        
        return (String[]) list.toArray(new String[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTarget#canDoOperation(int)
     */
    public boolean canDoOperation(int operation)
    {
        switch(operation) {
            case GOTO_HELP:
                return (fInformationPresenter != null);
            case INSERT_FILE:
            case INSERT_DIRECTORY:
            case INSERT_COLOR:
            case TABS_TO_SPACES:
            case TOGGLE_COMMENT:
            case REMOVE_BLOCK_COMMENT:
            case ADD_BLOCK_COMMENT:
                return true;
            default:
                return super.canDoOperation(operation);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
     */
    public void doOperation(int operation)
    {
        String text = null;
        switch(operation) {
            case GOTO_HELP:
            {
                doGotoHelp();
                return;
            }
            case INSERT_FILE:
            case INSERT_DIRECTORY:
                
                if(operation == INSERT_FILE) {
                    FileDialog dialog = new FileDialog(getControl().getShell(),SWT.OPEN);
                    dialog.setText(EclipseNSISPlugin.getResourceString("insert.file.description")); //$NON-NLS-1$
                    text = dialog.open();
                }
                else {
                    DirectoryDialog dialog = new DirectoryDialog(getControl().getShell());
                    dialog.setText(EclipseNSISPlugin.getResourceString("insert.directory.description")); //$NON-NLS-1$
                    text = dialog.open();
                }
                if(!Common.isEmpty(text)) {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if(page != null) {
                        IEditorPart editor = page.getActiveEditor();
                        if(editor != null && editor instanceof NSISEditor) {
                            IEditorInput editorInput = editor.getEditorInput();
                            if(editorInput != null && editorInput instanceof IFileEditorInput) {
                                IFile file = ((IFileEditorInput)editorInput).getFile();
                                if(file != null) {
                                    text = Common.makeRelativeLocation(file, text);
                                }
                            }
                        }
                    }
                }
                break;
            case INSERT_COLOR:
            {
                ColorDialog dialog = new ColorDialog(getControl().getShell());
                dialog.setText(EclipseNSISPlugin.getResourceString("insert.color.description")); //$NON-NLS-1$
                RGB rgb = dialog.open();
                if(rgb != null) {
                    text = ColorManager.rgbToHex(rgb);
                }
                break;
            }
            case TABS_TO_SPACES:
            {
                doConvertTabsToSpaces();
                return;
            }
            case TOGGLE_COMMENT:
            {
                doToggleComment();
                return;
            }
            case ADD_BLOCK_COMMENT:
            {
                doAddBlockComment();
                return;
            }
            case REMOVE_BLOCK_COMMENT:
            {
                doRemoveBlockComment();
                return;
            }
            default:
            {
                super.doOperation(operation);
                return;
            }
        }
        if(text != null) {
            IDocument doc = getDocument();
            Point p = getSelectedRange();
            try {
                doc.replace(p.x,p.y,text);
            }
            catch (BadLocationException e) {
            }
        }
    }
    
    private void doAddBlockComment()
    {
        try {
            Point p = getSelectedRange();
            if(p.y > 0) {
                IDocument doc = getDocument();
                ITypedRegion region = null;
                int startPos = p.x;
                int endPos = p.x+p.y-1;
                StringBuffer newText = new StringBuffer("/*"); //$NON-NLS-1$
                int pos = p.x;
                while(true) {
                    region = NSISTextUtility.getTypedRegionAtOffset(doc,pos);
                    int regionStart = region.getOffset();
                    int regionLen = region.getLength();
                    int regionEnd = regionStart+regionLen-1;
                    String regionType = region.getType();
                    if(regionType.equals(NSISPartitionScanner.NSIS_SINGLELINE_COMMENT) ||
                            regionType.equals(NSISPartitionScanner.NSIS_STRING)) {
                        if(regionStart < startPos) {
                            startPos = regionStart;
                        }
                        newText.append(doc.get(regionStart,regionLen));
                        if(regionEnd >= endPos) {
                            endPos = regionEnd;
                            break;
                        }
                        else {
                            pos = regionEnd+1;
                        }
                    }
                    else if(regionType.equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                        if(regionStart < startPos) {
                            startPos = regionStart;
                        }
                        newText.append(doc.get(regionStart+2,regionLen-4));
                        if(regionEnd >= endPos) {
                            endPos = regionEnd;
                            break;
                        }
                        else {
                            pos = regionEnd+1;
                        }
                    }
                    else {
                        regionStart = Math.max(startPos,regionStart);
                        if(regionEnd >= endPos) {
                            newText.append(doc.get(regionStart,endPos-regionStart+1));
                            break;
                        }
                        else {
                            newText.append(doc.get(regionStart,regionEnd-regionStart+1));
                            pos = regionEnd+1;
                        }
                    }
                }
                newText.append("*/"); //$NON-NLS-1$
                doc.replace(startPos,endPos-startPos+1,newText.toString());
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
        
    }
    
    private void doRemoveBlockComment()
    {
        try {
            IDocument doc = getDocument();
            Point p = getSelectedRange();
            int startPos = p.x;
            int endPos = (p.x>0?p.x+p.y-1:startPos);
            int pos = startPos;
            StringBuffer newText = new StringBuffer("");
            ITypedRegion region;
            while(true) {
                region = NSISTextUtility.getTypedRegionAtOffset(doc,pos);
                int regionStart = region.getOffset();
                int regionLen = region.getLength();
                int regionEnd = regionStart+regionLen-1;
                String regionType = region.getType();
                if(regionType.equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                    newText.append(doc.get(regionStart+2,regionLen-4));
                    if(regionStart < startPos) {
                        startPos = regionStart;
                    }
                    if(regionEnd >= endPos) {
                        endPos = regionEnd;
                        break;
                    }
                    else {
                        pos = regionEnd+1;
                    }
                }
                else {
                    if(regionEnd >= endPos) {
                        newText.append(doc.get(pos,(endPos-pos+1)));
                        break;
                    }
                    else {
                        newText.append(doc.get(pos,(regionEnd-pos+1)));
                        pos = regionEnd+1;
                    }
                }
            }
            doc.replace(startPos,endPos-startPos+1,newText.toString());
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void doToggleComment()
    {
        try {
            IDocument doc = getDocument();
            Point p = getSelectedRange();
            int startLine = doc.getLineOfOffset(p.x);
            int endLine = (p.y==0?startLine:doc.getLineOfOffset(p.x+p.y-1));
            boolean allAreCommented=true;
            IRegion region = null;
            String[] text = new String[endLine-startLine+1];
            for(int i=startLine; i<=endLine; i++) {
                region = doc.getLineInformation(i);
                text[i-startLine] = doc.get(region.getOffset(),region.getLength());
                String text2 = text[i-startLine].trim();
                if(!text2.startsWith(";") && !text2.startsWith("#")) { //$NON-NLS-1$ //$NON-NLS-2$
                    allAreCommented = false;
                }
            }
            
            int startPos = doc.getLineOffset(startLine);
            int length = region.getOffset()+region.getLength()-startPos;

            StringBuffer newText = new StringBuffer(""); //$NON-NLS-1$
            for(int i=startLine; i<=endLine; i++) {
                if(i > startLine) {
                    newText.append(doc.getLineDelimiter(i-1));
                }
                if(allAreCommented) {
                    String text2 = text[i-startLine].trim();
                    int n = text[i-startLine].indexOf(text2);
                    newText.append(text[i-startLine].substring(0,n));
                    if(n < (text[i-startLine].length()-1)) {
                        newText.append(text[i-startLine].substring(n+1));
                    }
                }
                else {
                    newText.append(";").append(text[i-startLine]); //$NON-NLS-1$
                }
            }
            
            doc.replace(startPos,length,newText.toString());
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void doConvertTabsToSpaces()
    {
        int tabWidth = mPreferenceStore.getInt(INSISPreferenceConstants.TAB_WIDTH);
        IDocument doc = getDocument();
        Point p = getSelectedRange();
        String text;
        if(p.y == 0) {
            p.x = 0;
            p.y = doc.getLength();
        }
        try {
            text = doc.get(p.x,p.y);
            text = convertTabsToSpaces(getDocument(),p.x,text,tabWidth);
            doc.replace(p.x,p.y,text);
        }
        catch (BadLocationException e) {
        }
    }

    private void doGotoHelp()
    {
        int offset = NSISTextUtility.computeOffset(this,false);
        if(offset >= 0) {
            String url = null;
            String keyword;
            IRegion region = NSISInformationUtility.getInformationRegionAtOffset(this,offset,false);
            keyword = NSISTextUtility.getRegionText(getDocument(),region);
            NSISHelpURLProvider.getInstance().showHelpURL(keyword);
        }
    }

    private String convertTabsToSpaces(IDocument doc, int textOffset, String text, int tabWidth)
    {
        if (text != null) {
            int index= text.indexOf('\t');
            if (index > -1) {
                StringBuffer buffer= new StringBuffer();
                mLineTracker.set(text);
                int lines= mLineTracker.getNumberOfLines();
                
                try {
                    for (int i= 0; i < lines; i++) {
                        int offset= mLineTracker.getLineOffset(i);
                        int endOffset= offset + mLineTracker.getLineLength(i);
                        String line= text.substring(offset, endOffset);
                        
                        int position= 0;
                        if (i == 0) {
                            IRegion firstLine= doc.getLineInformationOfOffset(textOffset);
                            position= textOffset - firstLine.getOffset();   
                        }
                        
                        int length= line.length();
                        for (int j= 0; j < length; j++) {
                            char c= line.charAt(j);
                            if (c == '\t') {
                                position += NSISTextUtility.insertTabString(buffer,position,tabWidth);
                            } else {
                                buffer.append(c);
                                ++position;
                            }
                        }
                    }
                    
                    text= buffer.toString();
                } catch (BadLocationException x) {
                }
            }
        }
        return text;
    }

    protected class NSISTabConversionStrategy extends NSISAutoIndentStrategy
    {
        /**
         * @param preferenceStore
         */
        public NSISTabConversionStrategy(IPreferenceStore preferenceStore)
        {
            super(preferenceStore);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
         */
        public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd)
        {
            if(mUseSpacesForTabs) {
                if (cmd.length == 0 && cmd.text != null) {
                    cmd.text = convertTabsToSpaces(doc, cmd.offset, cmd.text, mTabWidth);
                }            
            }
        }
    }
}