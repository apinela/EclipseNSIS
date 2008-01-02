/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import net.sf.eclipsensis.installoptions.model.InstallOptionsLabel;

public class LabelPropertySectionCreator extends UneditableElementPropertySectionCreator
{
    public LabelPropertySectionCreator(InstallOptionsLabel label)
    {
        super(label);
    }

    protected boolean isTextPropertyMultiline()
    {
        return true;
    }
}
