/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.*;

public class NSISTextHover implements ITextHover, ITextHoverExtension, INSISConstants
{
    private NSISInformationProvider mInformationProvider;

    public NSISTextHover()
    {
        mInformationProvider = new NSISInformationProvider();
        mInformationProvider.setInformationPresenterControlCreator(new NSISInformationControlCreator(new String[]{STICKY_HELP_COMMAND_ID, GOTO_HELP_COMMAND_ID}));
    }

    /*
     * (non-Javadoc) Method declared on ITextHover
     */
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        return (String)mInformationProvider.getInformation2(textViewer, hoverRegion);
    }

    /*
     * (non-Javadoc) Method declared on ITextHover
     */
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        return mInformationProvider.getSubject(textViewer, offset);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
     */
    public IInformationControlCreator getHoverControlCreator()
    {
        return mInformationProvider.getInformationPresenterControlCreator();
    }
}