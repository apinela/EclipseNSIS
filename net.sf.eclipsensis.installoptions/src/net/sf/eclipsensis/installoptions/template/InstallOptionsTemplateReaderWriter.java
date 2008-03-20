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

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.template.*;

class InstallOptionsTemplateReaderWriter extends AbstractTemplateReaderWriter
{
    static final InstallOptionsTemplateReaderWriter INSTANCE = new InstallOptionsTemplateReaderWriter();

    private InstallOptionsTemplateReaderWriter()
    {
        super();
    }

    public Collection import$(File file) throws IOException
    {
        Collection templates = super.import$(file);
        List list = new ArrayList(templates);
        boolean changed = false;
        for(ListIterator iter = list.listIterator(); iter.hasNext(); ) {
            IInstallOptionsTemplate template = (IInstallOptionsTemplate)iter.next();
            if(template instanceof InstallOptionsTemplate) {
                template = new InstallOptionsTemplate2(template);
                iter.set(template);
                changed = true;
            }
        }
        if(changed) {
            templates.clear();
            templates.addAll(list);
        }
        return templates;
    }

    protected ITemplate createTemplate()
    {
        return new InstallOptionsTemplate2();
    }
}
