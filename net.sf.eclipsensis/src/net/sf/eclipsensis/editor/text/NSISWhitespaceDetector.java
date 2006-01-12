/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class NSISWhitespaceDetector implements IWhitespaceDetector
{
    /*
     * (non-Javadoc) Method declared on IWhitespaceDetector
     */
    public boolean isWhitespace(char character)
    {
        return Character.isWhitespace(character);
    }
}