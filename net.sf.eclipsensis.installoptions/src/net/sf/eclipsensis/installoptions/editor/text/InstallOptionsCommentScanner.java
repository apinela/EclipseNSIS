/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.text;

import java.util.Arrays;
import java.util.Comparator;

import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;

public class InstallOptionsCommentScanner extends BufferedRuleBasedScanner implements IInstallOptionsScanner
{
    /**
     * 
     */
    public InstallOptionsCommentScanner()
    {
        super();
        setDefaultReturnToken(new Token(new TextAttribute(ColorManager.getColor(ColorManager.GREY),
                null, SWT.ITALIC)));
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.editor.text.IInstallOptionsScanner#getOffset()
     */
    public int getOffset()
    {
        return fOffset;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.editor.text.IInstallOptionsScanner#getDocument()
     */
    public IDocument getDocument()
    {
        return fDocument;
    }

    public void setRange(IDocument document, int offset, int length)
    {
        super.setRange(document, offset, length);
        Arrays.sort(fDelimiters,new Comparator() {
            public int compare(Object a, Object b)
            {
                return ((char[])b).length-((char[])a).length;
            }
        });
    }
}
