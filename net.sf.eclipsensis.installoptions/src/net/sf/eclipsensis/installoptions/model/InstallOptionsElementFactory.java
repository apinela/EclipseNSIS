/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.gef.requests.CreationFactory;

public class InstallOptionsElementFactory implements CreationFactory
{
    private String mTemplate;
    /**
     * 
     */
    public InstallOptionsElementFactory(String template)
    {
        super();
        mTemplate = template;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject()
    {
        //TODO Add more objects here
        if (IInstallOptionsConstants.TEMPLATE_BUTTON.equals(mTemplate)) {
            return new InstallOptionsButton();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    
    public Object getObjectType() {
        return mTemplate;
    }
}
