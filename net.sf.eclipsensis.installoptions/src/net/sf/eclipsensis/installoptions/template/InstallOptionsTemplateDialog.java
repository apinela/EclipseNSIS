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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.template.AbstractTemplate;
import net.sf.eclipsensis.template.AbstractTemplateDialog;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class InstallOptionsTemplateDialog extends AbstractTemplateDialog
{
    private InstallOptionsWidget[] mWidgets;
    
    public InstallOptionsTemplateDialog(Shell parentShell, InstallOptionsTemplate template)
    {
        this(parentShell, template, null);
    }

    public InstallOptionsTemplateDialog(Shell parentShell, InstallOptionsWidget[] widgets)
    {
        this(parentShell, null, widgets);
    }

    private InstallOptionsTemplateDialog(Shell parentShell, InstallOptionsTemplate template, InstallOptionsWidget[] widgets)
    {
        super(parentShell, InstallOptionsTemplateManager.INSTANCE, template, template == null);
        mWidgets = widgets;
    }

    protected AbstractTemplate createTemplate(String name)
    {
        return new InstallOptionsTemplate(name);
    }

    protected void createUpdateTemplate()
    {
        super.createUpdateTemplate();
        if(isCreate()) {
            ((InstallOptionsTemplate)getTemplate()).setWidgets(mWidgets);
        }
    }

    protected Image getShellImage()
    {
        return InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.icon")); //$NON-NLS-1$
    }

    protected String getShellTitle()
    {
        return InstallOptionsPlugin.getResourceString((isCreate()?"create.template.dialog.title":"edit.template.dialog.title")); //$NON-NLS-1$
    }
    
}
