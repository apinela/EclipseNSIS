/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardWelcomePage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardWelcome"; //$NON-NLS-1$
    
    /**
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardWelcomePage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.welcome.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.welcome.description")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        final GridLayout layout = new GridLayout(1,true);
        composite.setLayout(layout);
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.header", true, null, false); //$NON-NLS-1$
        l.setFont(JFaceResources.getBannerFont());
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Label l2 = NSISWizardDialogUtil.createLabel(composite,"wizard.welcome.text", true, null, false); //$NON-NLS-1$
        final GridData gridData = (GridData)l2.getLayoutData();
        gridData.widthHint = WIDTH_HINT;

        NSISWizardDialogUtil.createLabel(composite,"wizard.required.text", true, null, true); //$NON-NLS-1$
        
        composite.addListener (SWT.Resize,  new Listener () {
            boolean init = false;
            
            public void handleEvent (Event e) {
                if(init) {
                    Point size = composite.getSize();
                    gridData.widthHint = size.x - 2*layout.marginWidth;
                    composite.layout();
                }
                else {
                    init=true;
                }
            }
        });

        setPageComplete(true);
    }

}
