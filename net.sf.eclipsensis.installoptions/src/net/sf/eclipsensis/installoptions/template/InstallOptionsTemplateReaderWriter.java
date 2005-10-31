/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import java.io.StringReader;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.ini.INIFile;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.template.AbstractTemplateReaderWriter;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;

class InstallOptionsTemplateReaderWriter extends AbstractTemplateReaderWriter
{
    private static final String SECTIONS_NODE= "sections"; //$NON-NLS-1$
    static final InstallOptionsTemplateReaderWriter INSTANCE = new InstallOptionsTemplateReaderWriter();
    
    private InstallOptionsTemplateReaderWriter()
    {
        super();
    }

    protected AbstractTemplate createTemplate(String name)
    {
        return new InstallOptionsTemplate(name);
    }

    protected Node exportContents(AbstractTemplate template, Document document)
    {
        INISection[] iniSections = ((InstallOptionsTemplate)template).getSections();
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmptyArray(iniSections)) {
            iniSections[0].update();
            buf.append(iniSections[0]);
            for (int i = 1; i < iniSections.length; i++) {
                iniSections[i].trim().update();
                buf.append(INSISConstants.LINE_SEPARATOR).append(iniSections[i].toString());
            }
        }
        Element sections = document.createElement(SECTIONS_NODE);
        Text data = document.createTextNode(buf.toString());
        sections.appendChild(data);
        return sections;
    }

    protected String getContentsNodeName()
    {
        return SECTIONS_NODE;
    }

    protected void importContents(AbstractTemplate template, Node item)
    {
        String sections = XMLUtil.readTextNode(item);
        if(!Common.isEmpty(sections)) {
            INIFile iniFile = INIFile.load(new StringReader(sections));
            INISection[] iniSections = iniFile.getSections();
            for (int i = 0; i < iniSections.length; i++) {
                iniSections[i] = (INISection)iniSections[i].trim().clone();
            }
            ((InstallOptionsTemplate)template).setSections(iniSections);
        }        
    }
}
