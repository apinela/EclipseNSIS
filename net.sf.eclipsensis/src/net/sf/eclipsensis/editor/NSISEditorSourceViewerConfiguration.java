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

import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.editor.template.NSISTemplateCompletionProcessor;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;

public class NSISEditorSourceViewerConfiguration extends NSISSourceViewerConfiguration
{
    protected InformationPresenter mInformationPresenter = null;
    protected NSISTextHover mTextHover = null;
    protected NSISAnnotationHover mAnnotationHover = null;

    public NSISEditorSourceViewerConfiguration(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
        mTextHover = new NSISTextHover();
        
        NSISInformationProvider informationProvider = new NSISInformationProvider();
        IInformationControlCreator informationControlCreator = new NSISInformationControlCreator(null,SWT.V_SCROLL|SWT.H_SCROLL);
        informationProvider.setInformationPresenterControlCreator(informationControlCreator);
        mInformationPresenter = new InformationPresenter(informationControlCreator);
        mInformationPresenter.setInformationProvider(informationProvider,NSISPartitionScanner.NSIS_STRING);
        mInformationPresenter.setInformationProvider(informationProvider,IDocument.DEFAULT_CONTENT_TYPE);
        mInformationPresenter.setSizeConstraints(60, 5, true, true);
        mAnnotationHover = new NSISAnnotationHover();
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return mAnnotationHover;
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
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer)
    {
        return mInformationPresenter;
    }
    
    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createContentAssistant(sourceViewer,
                        new IContentAssistProcessor[] {new NSISTemplateCompletionProcessor(),
                                                       new NSISCompletionProcessor()},
                        new String[][] { {IDocument.DEFAULT_CONTENT_TYPE},
                                         {NSISPartitionScanner.NSIS_STRING}},
                        true);
    }

    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IContentAssistant getInsertTemplateAssistant(ISourceViewer sourceViewer)
    {
        return NSISSourceViewerConfigurationTools.createContentAssistant(sourceViewer,
                new IContentAssistProcessor[] {new NSISTemplateCompletionProcessor(true)},
                new String[][] { {IDocument.DEFAULT_CONTENT_TYPE} },
                false);
    }
}