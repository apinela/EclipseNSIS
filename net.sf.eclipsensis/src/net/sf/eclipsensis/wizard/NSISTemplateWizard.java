/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;

public class NSISTemplateWizard extends NSISWizard
{
    public NSISTemplateWizard(NSISWizardTemplate template)
    {
        super();
        setTemplate(template);
        setNeedsProgressMonitor(false);
        setWindowTitle(EclipseNSISPlugin.getResourceString((Common.isEmpty(mTemplate.getName())?"wizard.new.template.editor.title": //$NON-NLS-1$
                                                            "wizard.edit.template.editor.title"))); //$NON-NLS-1$
    }

    public String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_scrtmpltdlg_context"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.NSISWizard#initSettings()
     */
    protected void initSettings()
    {
        NSISWizardTemplate template = getTemplate();
        if(template != null) {
            setSettings(template.getSettings());
        }
        else {
            super.initSettings();
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.NSISWizard#addStartPage()
     */
    protected void addStartPage()
    {
        addPage(new NSISWizardTemplatePage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish()
    {
        getTemplate().setSettings(getSettings());
        return true;
    }
}
