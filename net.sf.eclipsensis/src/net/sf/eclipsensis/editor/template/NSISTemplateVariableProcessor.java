/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import java.util.*;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class NSISTemplateVariableProcessor implements IContentAssistProcessor, INSISTemplateConstants
{
    private static final IRegion[] EMPTY_IREGION_ARRAY = new IRegion[0];

    private static Comparator mTemplateVariableProposalComparator= new Comparator() {
        public int compare(Object o1, Object o2) {
            NSISTemplateVariableProposal proposal0= (NSISTemplateVariableProposal)o1;
            NSISTemplateVariableProposal proposal1= (NSISTemplateVariableProposal)o2;
            
            return proposal0.getDisplayString().compareTo(proposal1.getDisplayString());
        }

        public boolean equals(Object arg0) {
            return false;
        }
    };
    
    private TemplateContextType mContextType;
    
    /**
     * Sets the context type.
     */
    public void setContextType(TemplateContextType contextType) 
    {
        mContextType= contextType;  
    }
    
    /**
     * Gets the context type.
     */
    public TemplateContextType getContextType() 
    {
        return mContextType;    
    }   
    
    /*
     * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) 
    {
        if (mContextType == null) {
            return null;
        }

        List proposals= new ArrayList();        
        
        String text= viewer.getDocument().get();
        int start= getStart(text, documentOffset);
        int end= documentOffset;

        String string= text.substring(start, end);
        String prefix= (string.length() >= 1?string.substring(1):null);

        int offset= start;
        int length= end - start;

        for (Iterator iterator= mContextType.resolvers(); iterator.hasNext(); ) {
            TemplateVariableResolver variable= (TemplateVariableResolver) iterator.next();

            if (prefix == null || variable.getType().startsWith(prefix)) {
                proposals.add(new NSISTemplateVariableProposal(variable, offset, length, viewer));
            }
        }

        Collections.sort(proposals, mTemplateVariableProposalComparator);
        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    /* Guesses the start position of the completion */
    private int getStart(String string, int end) 
    {
        IRegion[] variables = parsePattern(string);
        int regionStart = 0;
        for (int i = 0; i < variables.length; i++) {
            int offset = variables[i].getOffset();
            if(end < offset) {
                break;
            }
            else {
                int endOffset = offset+variables[i].getLength();
                if(end <= (endOffset-1)) {
                    regionStart = offset;
                    break;
                }
                else {
                    regionStart = endOffset;
                }
            }
        }

        int start= end;

        if (start >= (regionStart+1) && string.charAt(start - 1) == '%') {
            return start - 1;
        }
        
        while ((start > regionStart) && Character.isUnicodeIdentifierPart(string.charAt(start - 1))) {
            start--;
        }

        if (start >= (regionStart+1) && string.charAt(start - 1) == '%') {
            return start - 1;
        }
            
        return end;
    }

    private IRegion[] parsePattern(String string)
    {
        int state= TEXT;
        String errorMessage= null;
        ArrayList list = new ArrayList();
        
        int offset = -1;
        outer:
        for (int i= 0; i != string.length(); i++) {
            char ch= string.charAt(i);
            
            switch (state) {
            case TEXT:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        state= ESCAPE;
                        offset = i;
                        break;
                    default:
                        break;
                }
                break;
            case ESCAPE:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        list.add(new Region(offset,i-offset+1));
                        state= TEXT;
                        offset = -1;
                        break;
                    default:
                        if(!Character.isLetter(ch)) {
                            offset = -1;
                            state= TEXT;
                        }
                        else {
                            state= IDENTIFIER;
                        }
                }
                break;
            case IDENTIFIER:
                switch (ch) {
                case IDENTIFIER_BOUNDARY:
                    list.add(new Region(offset,i-offset+1));
                    state= TEXT;
                    offset = -1;
                    break;
                default:
                    if (!Character.isLetterOrDigit(ch) && ch != '_') {
                        offset = -1;
                        state= TEXT;
                    }
                    break;
                }
                break;
            }
        }
        
        return (IRegion[])list.toArray(EMPTY_IREGION_ARRAY);
    }
    /*
     * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) 
    {
        return null;
    }

    /*
     * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() 
    {
        return new char[] {'%'};
    }

    /*
     * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() 
    {
        return null;
    }

    /*
     * @see IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() 
    {
        return null;
    }

    /*
     * @see IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() 
    {
        return null;
    }
}
