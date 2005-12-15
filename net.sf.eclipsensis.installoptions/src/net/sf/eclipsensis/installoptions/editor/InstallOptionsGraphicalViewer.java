/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.dnd.InstallOptionsTemplateTransfer;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class InstallOptionsGraphicalViewer extends GraphicalViewerImpl
{
    private InstallOptionsDialog mDialog  = null;
    private IFigure mRootFigure;
    private int mStyle;

    public InstallOptionsGraphicalViewer(InstallOptionsDialog dialog)
    {
        this(dialog, SWT.NONE);
    }

    public InstallOptionsGraphicalViewer(InstallOptionsDialog dialog, int style)
    {
        mDialog = dialog;
        mStyle = style;
    }

    public InstallOptionsDialog getDialog()
    {
        return mDialog;
    }

    public final Control createControl(Composite parent)
    {
        if(mDialog != null) {
            String rtl = mDialog.getRTL();
            if(InstallOptionsDialog.OPTION_YES.equals(rtl)) {
                mStyle |= SWT.RIGHT_TO_LEFT;
                mStyle &= ~SWT.LEFT_TO_RIGHT;
            }
            else {
                mStyle |= SWT.LEFT_TO_RIGHT;
                mStyle &= ~SWT.RIGHT_TO_LEFT;
            }
        }
        final FigureCanvas canvas = new FigureCanvas(parent, mStyle, getLightweightSystem());
        if(mDialog != null) {
            final PropertyChangeListener listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    String property = evt.getPropertyName();
                    if(property.equals(InstallOptionsModel.PROPERTY_RTL)) {
                        String rtl = (String)evt.getNewValue();
                        int windowsStyle = WinAPI.GetWindowLong(canvas.handle,WinAPI.GWL_EXSTYLE);
                        int style = canvas.getStyle();
                        if(InstallOptionsDialog.OPTION_YES.equals(rtl)) {
                            windowsStyle |= WinAPI.WS_EX_LAYOUTRTL;
                            style &= ~SWT.LEFT_TO_RIGHT;
                            style |= SWT.RIGHT_TO_LEFT;
                        }
                        else {
                            windowsStyle &= ~WinAPI.WS_EX_LAYOUTRTL;
                            style |= SWT.LEFT_TO_RIGHT;
                            style &= ~SWT.RIGHT_TO_LEFT;
                        }
                        WinAPI.SetWindowLong(canvas.handle,WinAPI.GWL_EXSTYLE,windowsStyle);
                        WinAPI.SetObjectFieldValue(canvas,"style",style); //$NON-NLS-1$
                        getRootFigure().repaint();
                    }
                    else if(property.equals(DialogSizeManager.PROPERTY_DIALOGSIZES)) {
                        DialogSize oldDialogSize = mDialog.getDialogSize();
                        DialogSize newDialogSize = DialogSizeManager.getDialogSize(oldDialogSize.getName());
                        if(newDialogSize == null) {
                            setProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE,DialogSizeManager.getDefaultDialogSize().getCopy());
                        }
                        else if(!oldDialogSize.equals(newDialogSize)) {
                            setProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE,newDialogSize.getCopy());
                        }
                    }
                }
            };
            mDialog.addPropertyChangeListener(listener);
            DialogSizeManager.addPropertyChangeListener(listener);
            canvas.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    DialogSizeManager.removePropertyChangeListener(listener);
                    mDialog.removePropertyChangeListener(listener);
                }
            });
        }
        super.setControl(canvas);
        installRootFigure();
        return canvas;
    }

    protected FigureCanvas getFigureCanvas()
    {
        return (FigureCanvas)getControl();
    }

    private void installRootFigure()
    {
        if (getFigureCanvas() == null) {
            return;
        }
        if (mRootFigure instanceof Viewport) {
            getFigureCanvas().setViewport((Viewport)mRootFigure);
        }
        else {
            getFigureCanvas().setContents(mRootFigure);
        }
    }

    public void reveal(EditPart part)
    {
        super.reveal(part);
        Viewport port = getFigureCanvas().getViewport();
        IFigure target = ((GraphicalEditPart)part).getFigure();
        Rectangle exposeRegion = target.getBounds().getCopy();
        target = target.getParent();
        while (target != null && target != port) {
            target.translateToParent(exposeRegion);
            target = target.getParent();
        }
        exposeRegion.expand(5, 5);

        Dimension viewportSize = port.getClientArea().getSize();

        Point topLeft = exposeRegion.getTopLeft();
        Point bottomRight = exposeRegion.getBottomRight().translate(viewportSize.getNegated());
        Point finalLocation = new Point();
        if (viewportSize.width < exposeRegion.width) {
            finalLocation.x = Math.min(bottomRight.x, Math.max(topLeft.x, port.getViewLocation().x));
        }
        else {
            finalLocation.x = Math.min(topLeft.x, Math.max(bottomRight.x, port.getViewLocation().x));
        }

        if (viewportSize.height < exposeRegion.height) {
            finalLocation.y = Math.min(bottomRight.y, Math.max(topLeft.y, port.getViewLocation().y));
        }
        else {
            finalLocation.y = Math.min(topLeft.y, Math.max(bottomRight.y, port.getViewLocation().y));
        }

        getFigureCanvas().scrollSmoothTo(finalLocation.x, finalLocation.y);
    }

    protected IFigure getRootFigure()
    {
        return mRootFigure;
    }

    protected void setRootFigure(IFigure figure)
    {
        mRootFigure = figure;
        installRootFigure();
    }

    public void addDropTargetListener(final TransferDropTargetListener listener)
    {
        if(listener.getTransfer() instanceof TemplateTransfer) {
            super.addDropTargetListener(new TransferDropTargetListener() {
                public void dragEnter(DropTargetEvent event)
                {
                    listener.dragEnter(event);
                }

                public void dragLeave(DropTargetEvent event) {
                    listener.dragLeave(event);
                }

                public void dragOperationChanged(DropTargetEvent event)
                {
                    listener.dragOperationChanged(event);
                }

                public void dragOver(DropTargetEvent event)
                {
                    listener.dragOver(event);
                }

                public void drop(DropTargetEvent event)
                {
                    listener.drop(event);
                }

                public void dropAccept(DropTargetEvent event)
                {
                    listener.dropAccept(event);
                }

                public Transfer getTransfer() {
                    return InstallOptionsTemplateTransfer.INSTANCE;
                }

                public boolean isEnabled(DropTargetEvent event)
                {
                    return listener.isEnabled(event);
                }
            });
        }
        else {
            super.addDropTargetListener(listener);
        }
    }
}