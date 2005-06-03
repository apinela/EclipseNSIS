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
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class InstallOptionsDialogLayer extends FreeformLayer implements IInstallOptionsConstants
{
    private List mChildren = new ArrayList();
    private InstallOptionsDialog mDialog;
    private Dimension mDialogSize = new Dimension();
    private boolean mDialogSizeVisible = false;
    private PropertyChangeListener mListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_SIZE)) {
                setDialogSize(dialogUnitsToPixels((Dimension)evt.getNewValue()));
            }
            else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_DIALOG_SIZE_VISIBLE)) {
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
            mDialogSize = dialogUnitsToPixels((mDialog != null?mDialog.getSize():new Dimension()));
            mDialogSizeVisible = (mDialog != null?mDialog.isDialogSizeVisible():false);
            if(mDialog != null) {
                mDialog.addPropertyChangeListener(mListener);
            }
            repaint();
        }
    }

    private Dimension dialogUnitsToPixels(Dimension d)
    {
        Font f = Display.getDefault().getSystemFont();
        return FigureUtility.dialogUnitsToPixels(d,f);
    }

    protected void paintFigure(Graphics graphics)
    {
        super.paintFigure(graphics);
        if(mDialog != null && mDialogSizeVisible && !mDialogSize.equals(0,0)) {
            setForegroundColor(ColorConstants.blue);
            graphics.drawRectangle(0,0,mDialogSize.width,mDialogSize.height);
        }
    }
    
    public void add(IFigure child, Object constraint, int index)
    {
        mChildren.add(child);
        super.add(child, constraint, index);
    }

    public void remove(IFigure child)
    {
        if ((child.getParent() == this) && getChildren().contains(child)) {
            mChildren.remove(child);
        }
        super.remove(child);
    }

    protected void paintChildren(Graphics graphics)
    {
        IFigure child;

        Rectangle clip = Rectangle.SINGLETON;
        for (Iterator iter = mChildren.iterator(); iter.hasNext(); ) {
            child = (IFigure)iter.next();
            if (child.isVisible() && child.intersects(graphics.getClip(clip))) {
                graphics.clipRect(child.getBounds());
                child.paint(graphics);
                graphics.restoreState();
            }
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
