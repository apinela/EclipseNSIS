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

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.*;

public class NSISSourceViewerConfiguration extends SourceViewerConfiguration implements INSISConstants, INSISPreferenceConstants
{
    protected static final String[] cConfiguredContentTypes;
    protected InformationPresenter mInformationPresenter = null;
    protected NSISTextHover mTextHover = null;
    protected NSISAnnotationHover mAnnotationHover = null;
    protected NSISDoubleClickSelector mDoubleClickStrategy = null;
    protected IPreferenceStore mPreferenceStore = null;
    
    static {
        cConfiguredContentTypes = new String[NSISPartitionScanner.NSIS_PARTITION_TYPES.length+1];
        cConfiguredContentTypes[0]=IDocument.DEFAULT_CONTENT_TYPE;
        System.arraycopy(NSISPartitionScanner.NSIS_PARTITION_TYPES,0,cConfiguredContentTypes,1,NSISPartitionScanner.NSIS_PARTITION_TYPES.length);
    }
    
    public NSISSourceViewerConfiguration(IPreferenceStore preferenceStore)
    {
        super();
        mPreferenceStore = preferenceStore;
        mDoubleClickStrategy = new NSISDoubleClickSelector();
    }
    
    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return mAnnotationHover;
    }

    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
     */
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer)
    {
        return NSISPartitionScanner.NSIS_PARTITIONING;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
    {
        return cConfiguredContentTypes;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public String getDefaultPrefix(ISourceViewer sourceViewer,
            String contentType)
    {
        return (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" : null); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public ITextDoubleClickStrategy getDoubleClickStrategy(
            ISourceViewer sourceViewer, String contentType)
    {
        return mDoubleClickStrategy;
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType)
    {
        if(sourceViewer instanceof NSISSourceViewer) {
            return ((NSISSourceViewer)sourceViewer).calculatePrefixes();
        }
        else {
            return new String[]{"\t","    "}; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer)
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        DefaultDamagerRepairer dr = new NSISDamagerRepairer(new NSISCodeScanner(mPreferenceStore));
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        
        dr = new NSISDamagerRepairer(new NSISCommentScanner(mPreferenceStore));
        reconciler.setDamager(dr, NSISPartitionScanner.NSIS_SINGLELINE_COMMENT);
        reconciler.setRepairer(dr, NSISPartitionScanner.NSIS_SINGLELINE_COMMENT);
        reconciler.setDamager(dr, NSISPartitionScanner.NSIS_MULTILINE_COMMENT);
        reconciler.setRepairer(dr, NSISPartitionScanner.NSIS_MULTILINE_COMMENT);

        dr = new NSISDamagerRepairer(new NSISStringScanner(mPreferenceStore));
        reconciler.setDamager(dr, NSISPartitionScanner.NSIS_STRING);
        reconciler.setRepairer(dr, NSISPartitionScanner.NSIS_STRING);

        return reconciler;
    }

    /* (non-Javadoc)
     * Method declared on SourceViewerConfiguration
     */
    public int getTabWidth(ISourceViewer sourceViewer)
    {
        return mPreferenceStore.getInt(INSISPreferenceConstants.TAB_WIDTH);
    }

    /* (non-Javadoc)
     * Method declared on SourceViewerConfiguration
     */
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
    {
        return mTextHover;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationControlCreator(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IInformationControlCreator getInformationControlCreator(
            ISourceViewer sourceViewer)
    {
        return (mTextHover != null?mTextHover.getHoverControlCreator():null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationPresenter(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IInformationPresenter getInformationPresenter(
            ISourceViewer sourceViewer)
    {
        return mInformationPresenter;
    }
    
    public IPreferenceStore getPreferenceStore()
    {
        return mPreferenceStore;
    }

    protected class NSISCommentScanner extends NSISSingleTokenScanner
    {
       /**
         * @param preferenceStore
         */
        public NSISCommentScanner(IPreferenceStore preferenceStore)
        {
            super(preferenceStore);
            mProperty = INSISPreferenceConstants.COMMENTS_STYLE;
        }

        /**
         * @return
         */
        protected IToken getDefaultToken()
        {
            IToken defaultToken= createTokenFromPreference(INSISPreferenceConstants.COMMENTS_STYLE);
            return defaultToken;
        }
    }
}