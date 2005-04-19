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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public abstract class StatusMessageDialog extends IconAndMessageDialog implements IDialogConstants
{
    private DialogStatus mStatus = new DialogStatus(IStatus.OK,""); //$NON-NLS-1$
    private Image mErrorImage = null;
    private Image mWarningImage = null;
    private Image mInfoImage = null;
    private Image mOKImage = null;
        
    /**
     * Creates a new dialog.
     * 
     * @param parent the shell parent of the dialog
     */
    public StatusMessageDialog(Shell parent) 
    {
        super(parent);
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
    }
    
    /**
     * @return Returns the status.
     */
    public DialogStatus getStatus()
    {
        return mStatus;
    }
    
    /**
     * Updates the status of the ok button to reflect the given status.
     * Subclasses may override this method to update additional buttons.
     * @param status the status.
     */
    protected final void updateButtonsEnableState(IStatus status) 
    {
        Button b = getButton(IDialogConstants.OK_ID);
        if (b != null && !b.isDisposed()) {
            b.setEnabled(!status.matches(IStatus.ERROR));
        }
    }

    /*
     * @see Dialog#createDialogArea(Composite)
     */
    protected final Control createDialogArea(Composite ancestor) 
    {
        Composite parent = new Composite(ancestor, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));

        mErrorImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("error.icon")); //$NON-NLS-1$
        mWarningImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("warning.icon")); //$NON-NLS-1$
        mInfoImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("info.icon")); //$NON-NLS-1$

        int width = Math.max(mInfoImage.getBounds().width,Math.max(mErrorImage.getBounds().width,mWarningImage.getBounds().width));
        int height = Math.max(mInfoImage.getBounds().height,Math.max(mErrorImage.getBounds().height,mWarningImage.getBounds().height));
        Image tempImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("transparent.icon")); //$NON-NLS-1$
        ImageData imageData = tempImage.getImageData();
        imageData = imageData.scaledTo(width, height);
        mOKImage = new Image(getShell().getDisplay(),imageData);

        Control control = createControl(parent);
        control.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(data);
        
        Composite composite= new Composite(parent, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        message = getMessage();
        createMessageArea(composite);
        applyDialogFont(parent);        
        return parent;
    }

    protected final void refreshStatus()
    {
        updateStatus(mStatus);
    }

    protected final void updateStatus(DialogStatus status)
    {
        mStatus = status;
        updateButtonsEnableState(status);
        if(imageLabel != null && !imageLabel.isDisposed()) {
            imageLabel.setImage(getImage());
        }
        if(messageLabel != null && !messageLabel.isDisposed()) {
            messageLabel.setText(getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
     */
    protected final Image getImage()
    {
        switch(mStatus.getSeverity()) {
            case IStatus.ERROR:
                return mErrorImage;
            case IStatus.WARNING:
                return mWarningImage;
            case IStatus.INFO:
                return mInfoImage;
            default:
                return mOKImage;
        }
    }

    protected final String getMessage()
    {
        return mStatus.getMessage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close()
    {
        if(mOKImage != null && !mOKImage.isDisposed()) {
            mOKImage.dispose();
        }
        return super.close();
    }

    protected abstract Control createControl(Composite parent);

    protected class DialogStatus extends Status
    {
        public DialogStatus(int severity, String message)
        {
            super(severity,INSISConstants.PLUGIN_NAME,0,message,null);
        }
        
        public void setError(String message)
        {
            setSeverity(ERROR);
            setMessage(message);
        }
        
        public void setWarning(String message)
        {
            setSeverity(WARNING);
            setMessage(message);
        }
        
        public void setInformation(String message)
        {
            setSeverity(INFO);
            setMessage(message);
        }
        
        public void setOK()
        {
            setSeverity(OK);
            setMessage(""); //$NON-NLS-1$
        }
    }
}
