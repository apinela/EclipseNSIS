/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.Map;

import net.sf.eclipsensis.installoptions.editor.text.InstallOptionsSyntaxScanner;

import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

public class InstallOptionsDamagerRepairer extends DefaultDamagerRepairer
{
    public InstallOptionsDamagerRepairer(InstallOptionsSyntaxScanner scanner)
    {
        super(scanner);
    }

    public void setSyntaxStyles(Map syntaxStyles)
    {
        ((InstallOptionsSyntaxScanner)fScanner).setSyntaxStyles(syntaxStyles);
    }
}