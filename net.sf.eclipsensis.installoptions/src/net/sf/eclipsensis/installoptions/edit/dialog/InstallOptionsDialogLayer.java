/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;

public class InstallOptionsDialogLayer extends FreeformLayer implements IInstallOptionsConstants
{
    private InstallOptionsDialog mDialog;
    private Dimension mDialogSize = new Dimension();
    private boolean mDialogSizeVisible = false;
    private PropertyChangeListener mListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsDialog.PROPERTY_SIZE)) {
                setDialogSize((Dimension)evt.getNewValue());
            }
            else if(evt.getPropertyName().equals(InstallOptionsDialog.PROPERTY_DIALOG_SIZE_VISIBLE)) {
                setDialogSizeVisible(((Boolean)evt.getNewValue()).booleanValue());
            }
        }
    };
    
    public void setInstallOptionsDialog(InstallOptionsDialog dialog)
    {
        if(mDialog != dialog) {
            if(mDialog != null) {
                mDialog.removePropertyChangeListener(mListener);
            }
            mDialog = dialog;
            mDialogSize = (mDialog != null?mDialog.getSize():new Dimension());
            mDialogSizeVisible = (mDialog != null?mDialog.isDialogSizeVisible():false);
            if(mDialog != null) {
                mDialog.addPropertyChangeListener(mListener);
            }
            repaint();
        }
    }

    protected void paintFigure(Graphics graphics)
    {
        super.paintFigure(graphics);
        if(mDialog != null && mDialogSizeVisible && !mDialogSize.equals(0,0)) {
            setForegroundColor(ColorConstants.blue);
            graphics.drawRectangle(0,0,mDialogSize.width,mDialogSize.height);
        }
    }
    
    public Dimension getDialogSize()
    {
        return mDialogSize;
    }

    public void setDialogSize(Dimension size)
    {
        if(!mDialogSize.equals(size)) {
            mDialogSize = size;
            repaint();
        }
    }
    
    public boolean isDialogSizeVisible()
    {
        return mDialogSizeVisible;
    }
    
    public void setDialogSizeVisible(boolean dialogSizeVisible)
    {
        if(mDialogSizeVisible != dialogSizeVisible) {
            mDialogSizeVisible = dialogSizeVisible;
            repaint();
        }
    }
}
