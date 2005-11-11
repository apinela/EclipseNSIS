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

import org.eclipse.jface.text.rules.*;

public class ExclusiveEndSequenceWordPatternRule extends BeginningOfLineRule
{
    private IWordDetector mDetector;
    private StringBuffer mBuffer= new StringBuffer();

    ExclusiveEndSequenceWordPatternRule(IWordDetector detector, String startSequence, String endSequence, IToken token)
    {
        super(startSequence, endSequence, token, false);
        mDetector = detector;
    }

    protected boolean endSequenceDetected(ICharacterScanner scanner)
    {
        mBuffer.setLength(0);
        if(mDetector instanceof InstallOptionsWordDetector) {
            ((InstallOptionsWordDetector)mDetector).reset();
        }
        int c= scanner.read();
        while (c != ICharacterScanner.EOF && mDetector.isWordPart((char) c)) {
            mBuffer.append((char) c);
            c= scanner.read();
        }
        scanner.unread();

        if (mBuffer.length() > 0 && mBuffer.length() >= fEndSequence.length) {
            for (int i=fEndSequence.length - 1, j= mBuffer.length() - 1; i >= 0; i--, j--) {
                if (fEndSequence[i] != mBuffer.charAt(j)) {
                    unreadBuffer(scanner);
                    return false;
                }
                else {
                    scanner.unread();
                    mBuffer.deleteCharAt(j);
                }
            }
            return true;
        }
        unreadBuffer(scanner);
        return false;
    }

    /**
     * Returns the characters in the buffer to the scanner.
     * Note that the rule must also return the characters
     * read in as part of the start sequence expect the first one.
     *
     * @param scanner the scanner to be used
     */
    protected void unreadBuffer(ICharacterScanner scanner)
    {
        mBuffer.insert(0, fStartSequence);
        for (int i= mBuffer.length() - 1; i > 0; i--) {
            scanner.unread();
        }
    }
}
