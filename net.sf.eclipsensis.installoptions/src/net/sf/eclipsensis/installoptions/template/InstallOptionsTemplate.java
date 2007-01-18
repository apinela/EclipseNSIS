/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.template.InvalidTemplateException;
import net.sf.eclipsensis.util.Common;

public class InstallOptionsTemplate extends AbstractTemplate
{
    private static final long serialVersionUID = -2080053812185962758L;
    private static final Comparator cWidgetsComparator = new Comparator(){
        public int compare(Object o1, Object o2)
        {
            InstallOptionsWidget w1 = (InstallOptionsWidget)o1;
            InstallOptionsWidget w2 = (InstallOptionsWidget)o2;
            return w1.getIndex()-w2.getIndex();
        }
    };

    private INISection[] mSections;

    public InstallOptionsTemplate(String name)
    {
        this(null, name);
    }

    public InstallOptionsTemplate(String id, String name)
    {
        super(id, name);
    }

    public InstallOptionsTemplate(String id, String name, File file)
    {
        this(id, name);
        INIFile iniFile = INIFile.load(file);
        INISection[] sections = iniFile.getSections();
        for (int i = 0; i < sections.length; i++) {
            sections[i] = (INISection)sections[i].trim().clone();
        }
        setSections(sections);
    }

    public InstallOptionsTemplate(String id, String name, InstallOptionsWidget[] widgets)
    {
        this(id, name);
        setWidgets(widgets);
    }

    protected void afterImport() throws InvalidTemplateException
    {
        if(mSections == null) {
            throw new InvalidTemplateException();
        }
    }

    public void setWidgets(InstallOptionsWidget[] widgets)
    {
        Arrays.sort(widgets, cWidgetsComparator);
        INISection[] sections = new INISection[widgets.length];
        for (int i = 0; i < widgets.length; i++) {
            sections[i] = (INISection)widgets[i].updateSection().trim().clone();
            sections[i].setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[]{new Integer(i+1)}));
            sections[i].update();
        }
        setSections(sections);
    }

    INISection[] getSections()
    {
        return mSections;
    }

    void setSections(INISection[] sections)
    {
        mSections = sections;
    }

    public InstallOptionsWidget[] createWidgets()
    {
        List list = new ArrayList();
        for (int i = 0; i < mSections.length; i++) {
            INIKeyValue[] keyValues = mSections[i].findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
            if(!Common.isEmptyArray(keyValues)) {
                String type = keyValues[0].getValue();
                InstallOptionsElementFactory factory = InstallOptionsElementFactory.getFactory(type);
                if(factory != null) {
                    list.add(factory.getNewObject((INISection)mSections[i].clone()));
                }
            }
        }
        return (InstallOptionsWidget[])list.toArray(new InstallOptionsWidget[list.size()]);
    }

    public Object clone()
    {
        InstallOptionsTemplate template = (InstallOptionsTemplate)super.clone();
        INISection[] sections = new INISection[mSections.length];
        for (int i = 0; i < sections.length; i++) {
            sections[i] = (INISection)mSections[i].clone();
        }
        template.setSections(sections);
        return template;
    }
}
