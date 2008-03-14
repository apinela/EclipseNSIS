/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import net.sf.eclipsensis.template.*;

class InstallOptionsTemplateReaderWriter extends AbstractTemplateReaderWriter
{
    static final InstallOptionsTemplateReaderWriter INSTANCE = new InstallOptionsTemplateReaderWriter();

    private InstallOptionsTemplateReaderWriter()
    {
        super();
    }

    protected AbstractTemplate createTemplate()
    {
        return new InstallOptionsTemplate();
    }
}
