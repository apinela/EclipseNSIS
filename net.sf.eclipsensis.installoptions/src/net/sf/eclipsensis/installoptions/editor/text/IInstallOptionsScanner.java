/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.text;

import net.sf.eclipsensis.editor.text.NSISScanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ITokenScanner;

public interface IInstallOptionsScanner extends NSISScanner, ITokenScanner
{
    public IDocument getDocument();
}
