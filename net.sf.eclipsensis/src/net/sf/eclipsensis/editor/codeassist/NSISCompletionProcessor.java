/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.*;

/**
 * NSIS completion processor.
 */
public class NSISCompletionProcessor implements IContentAssistProcessor
{
    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        if(documentOffset > 0 ) {
            return NSISInformationUtility.getCompletionsAtOffset(viewer,documentOffset-1);
        }
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IContentAssistProcessor
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return new char[] { '.', '/','$','!' };
    }

    /* (non-Javadoc)
     * Method declared on IContentAssistProcessor
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IContentAssistProcessor
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null; //mValidator;
    }

    /* (non-Javadoc)
     * Method declared on IContentAssistProcessor
     */
    public String getErrorMessage()
    {
        return null;
    }
}