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
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.help.WorkbenchHelp;

public class NSISAboutDialog extends Dialog implements INSISConstants, IHyperlinkListener
{
    private static Image cAboutImage;
    private static String cAboutTitle;
    private static String cAboutHeader;
    private static String cAboutText;
    private static int cWidthHint;
    
    static {
        cAboutImage = ImageManager.getImage(EclipseNSISPlugin.getResourceString("about.icon")); //$NON-NLS-1$
        
        EclipseNSISPlugin plugin = EclipseNSISPlugin.getDefault();
        String name = plugin.getName();
        cAboutTitle = MessageFormat.format(EclipseNSISPlugin.getResourceString("about.title.format"), //$NON-NLS-1$
                                           new Object[]{name});
        
        cAboutHeader = MessageFormat.format(EclipseNSISPlugin.getResourceString("about.header.format"), //$NON-NLS-1$
                                           new Object[]{name, plugin.getVersion()});

        cAboutText = EclipseNSISPlugin.getResourceString("about.text"); //$NON-NLS-1$
        
        try {
            cWidthHint = Integer.parseInt(EclipseNSISPlugin.getResourceString("aboutdialog.wodth.hint")); //$NON-NLS-1$)
        }
        catch(NumberFormatException nfe) {
            cWidthHint = 400;
        }
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
        Color background = JFaceColors.getBannerBackground(getShell().getDisplay());
        Color foreground = JFaceColors.getBannerForeground(getShell().getDisplay());
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setBackground(background);
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.LEFT|SWT.BORDER);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(JFaceResources.getBannerFont());
        label.setText(cAboutHeader);
        GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        label.setLayoutData(data);
        
        label = new Label(composite, SWT.CENTER);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setImage(cAboutImage);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
        data.horizontalSpan = 1;
        label.setLayoutData(data);
        
        StyledText text = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY);
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 2;
        data.widthHint = cWidthHint;
        text.setLayoutData(data);
        text.setCaret(null);
        text.setFont(parent.getFont());
        text.setText(cAboutText);
        text.setLayoutData(data);
        text.setCursor(null);
        text.setBackground(background);
        text.setForeground(foreground);

        
        label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
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
