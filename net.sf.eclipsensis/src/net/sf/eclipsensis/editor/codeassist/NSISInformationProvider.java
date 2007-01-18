/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.help.NSISUsageProvider;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.*;

public class NSISInformationProvider implements IInformationProvider,
        IInformationProviderExtension, IInformationProviderExtension2, INSISConstants
{
    private IInformationControlCreator mInformationControlCreator;

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer, int)
     */
    public IRegion getSubject(ITextViewer textViewer, int offset)
    {
        return NSISInformationUtility.getInformationRegionAtOffset(textViewer, offset, true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public String getInformation(ITextViewer textViewer, IRegion subject)
    {
        return (String)getInformation2(textViewer, subject);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public Object getInformation2(ITextViewer textViewer, IRegion subject)
    {
        if (subject != null) {
            if (subject.getLength() > -1) {
                String word = NSISTextUtility.getRegionText(textViewer.getDocument(),subject);
                return getInformation(word);
            }
        }
        return null;
    }

    protected Object getInformation(String word)
    {
        return NSISUsageProvider.getInstance().getUsage(word);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
     */
    public IInformationControlCreator getInformationPresenterControlCreator()
    {
        return mInformationControlCreator;
    }

    /**
     * @param informationControlCreator The informationControlCreator to set.
     */
    public void setInformationPresenterControlCreator(IInformationControlCreator informationControlCreator)
    {
        mInformationControlCreator = informationControlCreator;
    }
}
