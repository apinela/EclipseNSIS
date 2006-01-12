/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.text;

import java.util.Map;

import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.*;

public abstract class InstallOptionsSyntaxScanner extends BufferedRuleBasedScanner implements IInstallOptionsScanner
{
    private Map mSyntaxStylesMap;

    public InstallOptionsSyntaxScanner()
    {
        super();
        setSyntaxStyles(NSISTextUtility.parseSyntaxStylesMap(InstallOptionsPlugin.getDefault().getPreferenceStore().getString(IInstallOptionsConstants.PREFERENCE_SYNTAX_STYLES)));
    }

    public void setSyntaxStyles(Map syntaxStylesMap)
    {
        mSyntaxStylesMap = syntaxStylesMap;
        reset();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.editor.text.IInstallOptionsScanner#getOffset()
     */
    final public int getOffset()
    {
        return fOffset;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.editor.text.IInstallOptionsScanner#getDocument()
     */
    final public IDocument getDocument()
    {
        return fDocument;
    }

    protected IToken createToken(String name)
    {
        NSISSyntaxStyle style = (NSISSyntaxStyle)mSyntaxStylesMap.get(name);
        if(style != null) {
            return new Token(style.createTextAttribute());
        }
        return Token.UNDEFINED;
    }

    protected abstract void reset();
}
