/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.help.WorkbenchHelp;

public class NSISAboutDialog extends Dialog implements INSISConstants, IHyperlinkListener
{
    private static Image cAboutImage;
    private static String cAboutTitle;
    private static String cAboutHeader;
    private static String cAboutText;
    private static String cAboutURLText;
    private static String cCPLURL;
    private static String cPluginHomeURL;
    private static Color cBackground;
    
    static {
        cAboutImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("about.icon")); //$NON-NLS-1$
        
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        String name = plugin.getName();
        cAboutTitle = MessageFormat.format(EclipseNSISPlugin.getResourceString("about.title.format"), //$NON-NLS-1$
                                           new Object[]{name});
        
        cAboutHeader = MessageFormat.format(EclipseNSISPlugin.getResourceString("about.header.format"), //$NON-NLS-1$
                                           new Object[]{name, plugin.getVersion()});

        cAboutText = EclipseNSISPlugin.getResourceString("about.text"); //$NON-NLS-1$

        cCPLURL = EclipseNSISPlugin.getResourceString("cpl.url"); //$NON-NLS-1$

        cAboutURLText = MessageFormat.format(EclipseNSISPlugin.getResourceString("about.url.format"), //$NON-NLS-1$
                                             new Object[]{name});

        cPluginHomeURL = EclipseNSISPlugin.getResourceString("plugin.home.url"); //$NON-NLS-1$

        cBackground = ColorManager.getColor(new RGB(255,255,255));
    }
    /**
     * @param parentShell
     */
    public NSISAboutDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(cAboutTitle);
    }
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        // create OK button
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                     true);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setBackground(cBackground);
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.LEFT|SWT.BORDER);
        label.setBackground(cBackground);
        label.setFont(JFaceResources.getBannerFont());
        label.setText(cAboutHeader);
        GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        label.setLayoutData(data);
        
        label = new Label(composite, SWT.CENTER|SWT.BORDER);
        label.setBackground(cBackground);
        label.setImage(cAboutImage);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
        data.horizontalSpan = 1;
        label.setLayoutData(data);
        
        label = new Label(composite, SWT.LEFT);
        label.setBackground(cBackground);
        label.setFont(JFaceResources.getDialogFont());
        label.setText(cAboutText);
        data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = 2;
        label.setLayoutData(data);

        Hyperlink link = new Hyperlink(composite,SWT.LEFT);
        link.setBackground(cBackground);
        link.setHref(cCPLURL);
        link.setText(cCPLURL);
        link.setUnderlined(true);
        link.setFont(JFaceResources.getDialogFont());
        link.addHyperlinkListener(this);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = 2;
        link.setLayoutData(data);

        label = new Label(composite, SWT.NONE);
        label.setBackground(cBackground);
        data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);

        label = new Label(composite, SWT.LEFT);
        label.setBackground(cBackground);
        label.setFont(JFaceResources.getDialogFont());
        label.setText(cAboutURLText);
        data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.HORIZONTAL_ALIGN_BEGINNING);
        label.setLayoutData(data);

        link = new Hyperlink(composite,SWT.LEFT);
        link.setBackground(cBackground);
        link.setHref(cPluginHomeURL);
        link.setText(cPluginHomeURL);
        link.setUnderlined(true);
        link.setFont(JFaceResources.getDialogFont());
        link.addHyperlinkListener(this);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING);
        link.setLayoutData(data);

        label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        label.setLayoutData(data);
        
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected Control createButtonBar(Composite parent)
    {
        Control ctl = super.createButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setFocus();
        return ctl;
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
     */
    public void linkActivated(HyperlinkEvent e)
    {
        WorkbenchHelp.displayHelpResource((String)e.getHref() + "?noframes=true"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent)
     */
    public void linkEntered(HyperlinkEvent e)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkExited(org.eclipse.ui.forms.events.HyperlinkEvent)
     */
    public void linkExited(HyperlinkEvent e)
    {
    }
}
