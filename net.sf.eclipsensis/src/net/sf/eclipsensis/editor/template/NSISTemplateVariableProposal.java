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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class NSISTemplateVariableProposal implements ICompletionProposal
{
    private TemplateVariableResolver mResolver;
    private int mOffset;
    private int mLength;    
    private ITextViewer mViewer;
    
    private Point mSelection;

    /**
     * Creates a template variable proposal.
     * 
     * @param variable the template variable
     * @param offset the offset to replace
     * @param length the length to replace
     * @param viewer the viewer
     */
    public NSISTemplateVariableProposal(TemplateVariableResolver variable, int offset, int length, ITextViewer viewer) {
        mResolver= variable;
        mOffset= offset;
        mLength= length;
        mViewer= viewer;
    }
    
    /*
     * @see ICompletionProposal#apply(IDocument)
     */
    public void apply(IDocument document) {

        try {
            String variable= mResolver.getType().equals("percent") ? "%%" : "%" + mResolver.getType() + '%'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            document.replace(mOffset, mLength, variable);
            mSelection= new Point(mOffset + variable.length(), 0);

        } catch (BadLocationException e) {
            EclipseNSISPlugin.getDefault().log(e);

            Shell shell= mViewer.getTextWidget().getShell();
            Common.openError(shell, e.getLocalizedMessage(), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
        }
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    public Point getSelection(IDocument document) {
        return mSelection;
    }

    /*
     * @see ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        return null;
    }

    /*
     * @see ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        return mResolver.getType() + " - " + mResolver.getDescription(); //$NON-NLS-1$
    }

    /*
     * @see ICompletionProposal#getImage()
     */
    public Image getImage() {
        return null;
    }

    /*
     * @see ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return null;
    }
}
