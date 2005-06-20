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

import java.util.ArrayList;

import net.sf.eclipsensis.editor.text.NSISWhitespaceDetector;
import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class InstallOptionsRuleBasedScanner extends BufferedRuleBasedScanner
        implements IInstallOptionsScanner
{
    /**
     * 
     */
    public InstallOptionsRuleBasedScanner()
    {
        super();
        ArrayList list = new ArrayList();
        list.add(new BeginningOfLineWordPatternRule(new InstallOptionsWordDetector('[',']'),
            "[","]",new Token(new TextAttribute(ColorManager.getColor(ColorManager.TEAL),null,SWT.NONE)))); //$NON-NLS-1$ //$NON-NLS-2$
        list.add(new ExclusiveEndSequenceWordPatternRule(new InstallOptionsWordDetector('\0','='),null,"=", //$NON-NLS-1$
            new Token(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_BLUE),null,SWT.NONE))));
        list.add(new WordRule(new IWordDetector(){

            public boolean isWordStart(char c)
            {
                return (c == '=');
            }

            public boolean isWordPart(char c)
            {
                return false;
            }}
        ,new Token(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_RED),null,SWT.NONE))));
        list.add(new NumberRule(new Token(new TextAttribute(ColorManager.getColor(ColorManager.CHOCOLATE),null,SWT.NONE))));
        list.add(new WhitespaceRule(new NSISWhitespaceDetector()));
        setRules((IRule[])list.toArray(new IRule[0]));
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
}
