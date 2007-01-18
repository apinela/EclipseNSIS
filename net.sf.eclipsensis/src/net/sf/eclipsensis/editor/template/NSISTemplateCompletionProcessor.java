/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.codeassist.NSISInformationUtility;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.*;
import org.eclipse.swt.graphics.Image;

public class NSISTemplateCompletionProcessor extends TemplateCompletionProcessor
{
    private static final Image TEMPLATE_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("template.icon")); //$NON-NLS-1$
    private boolean mInsertTemplatesMode = false;
    protected static final Comparator PROPOSAL_COMPARATOR = new Comparator() {

        public int compare(Object o1, Object o2)
        {
            TemplateProposal tp1 = (TemplateProposal)o1;
            TemplateProposal tp2 = (TemplateProposal)o2;
            int n = tp2.getRelevance()-tp1.getRelevance();
            if(n == 0) {
                n = tp1.getDisplayString().toLowerCase().compareTo(tp2.getDisplayString().toLowerCase());
            }
            return 0;
        }

    };

    /**
     *
     */
    public NSISTemplateCompletionProcessor()
    {
        this(false);
    }

    /**
     * @param insertTemplatesMode
     */
    public NSISTemplateCompletionProcessor(boolean insertTemplatesMode)
    {
        super();
        mInsertTemplatesMode = insertTemplatesMode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
     */
    protected Template[] getTemplates(String contextTypeId)
    {
        return EclipseNSISPlugin.getDefault().getTemplateStore().getTemplates();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#createContext(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    protected TemplateContext createContext(ITextViewer viewer, IRegion region)
    {
        TemplateContextType contextType= getContextType(viewer, region);
        if (contextType != null) {
            IDocument document= viewer.getDocument();
            return new NSISDocumentTemplateContext(contextType, document, region.getOffset(), region.getLength(), mInsertTemplatesMode);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region)
    {
        if(mInsertTemplatesMode || region.getLength() > 0) {
            return EclipseNSISPlugin.getDefault().getContextTypeRegistry().getContextType(NSISTemplateContextType.NSIS_TEMPLATE_CONTEXT_TYPE);
        }
        else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
     */
    protected Image getImage(Template template)
    {
        return TEMPLATE_IMAGE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
            int offset)
    {
        ICompletionProposal[] proposals = super.computeCompletionProposals(viewer, offset);
        if(mInsertTemplatesMode) {
            Arrays.sort(proposals, PROPOSAL_COMPARATOR);
        }
        else {
            ArrayList list = new ArrayList();
            for (int i = 0; i < proposals.length; i++) {
                if(((NSISTemplateProposal)proposals[i]).getRelevance() > 0) {
                    list.add(proposals[i]);
                }
            }
            Collections.sort(list, PROPOSAL_COMPARATOR);
            proposals = (ICompletionProposal[])Common.appendArray(list.toArray(NSISInformationUtility.EMPTY_COMPLETION_PROPOSAL_ARRAY),
                                                                  NSISInformationUtility.getCompletionsAtOffset(viewer, offset));
        }
        return proposals;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#createProposal(org.eclipse.jface.text.templates.Template, org.eclipse.jface.text.templates.TemplateContext, org.eclipse.jface.text.Region, int)
     */
    protected ICompletionProposal createProposal(Template template,
            TemplateContext context, IRegion region, int relevance)
    {
        return new NSISTemplateProposal(template, context, region, getImage(template), relevance);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getRelevance(org.eclipse.jface.text.templates.Template, java.lang.String)
     */
    protected int getRelevance(Template template, String prefix)
    {
        if (template.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
            return 90;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return NSISInformationUtility.getCompletionProposalAutoActivationCharacters();
    }
}
