/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.template.AbstractTemplateReaderWriter;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NSISWizardTemplateReaderWriter extends AbstractTemplateReaderWriter
{
    protected static final String SETTINGS_NODE= "settings"; //$NON-NLS-1$
    
    static final NSISWizardTemplateReaderWriter INSTANCE = new NSISWizardTemplateReaderWriter();
    
    private NSISWizardTemplateReaderWriter()
    {
        super();
    }

    protected AbstractTemplate createTemplate(String name)
    {
        return new NSISWizardTemplate(name);
    }

    protected Node exportContents(AbstractTemplate template, Document doc)
    {
        return ((NSISWizardTemplate)template).getSettings().toNode(doc);
    }

    protected String getContentsNodeName()
    {
        return SETTINGS_NODE;
    }

    protected void importContents(AbstractTemplate template, Node item)
    {
        NSISWizardSettings settings = new NSISWizardSettings(true);
        settings.fromNode(item);
        ((NSISWizardTemplate)template).setSettings(settings);
    }
}
