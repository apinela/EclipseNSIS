/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsRootEditPart;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RootEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public abstract class SWTControlFigure extends Figure
{
    private FigureCanvas mCanvas;
    private Image mImage;
    private Rectangle mImageBounds;
    private boolean mNeedsReScrape = true;
    private InstallOptionsRootEditPart mRootEditPart;
    
    private FigureListener mFigureListener = new FigureListener() {
        public void figureMoved(IFigure source) {
            if (mNeedsReScrape) {
                layout();
            }
        }
    };
    
    protected boolean isNeedsReScrape()
    {
        return mNeedsReScrape;
    }

    protected void setNeedsReScrape(boolean needsReScrape)
    {
        mNeedsReScrape = needsReScrape;
    }

    private PropertyChangeListener mPropertyListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(Viewport.PROPERTY_VIEW_LOCATION)) {
                if(mNeedsReScrape) {
                    layout();
                }
            }
        }
    };
    
    private PaintListener mSWTPaintListener = new PaintListener() {
        public void paintControl(PaintEvent e) {
            final Control source = (Control) e.getSource();
            if(!source.isDisposed() && source.handle > 0) {
                if (e.width >= bounds.width && e.height >= bounds.height){
                    mNeedsReScrape = false;
                } else {
                    mNeedsReScrape = true;
                }
    
                if(mImage != null) {
                    e.gc.copyArea(mImage, mImageBounds.x-bounds.x, mImageBounds.y-bounds.y);
                }
    
                try {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            if(!source.isDisposed()) {
                                source.removePaintListener(mSWTPaintListener);
                            }
                            source.dispose();
                        }
                    });
                }
                catch(Exception e1) { }
                repaint();
            }
        }
    };

    public SWTControlFigure(RootEditPart editpart)
    {
        super();
        mRootEditPart = (InstallOptionsRootEditPart)editpart;
        setLayoutManager(new XYLayout());
        mCanvas = (FigureCanvas) mRootEditPart.getViewer().getControl();
        mCanvas.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                if(mImage != null && !mImage.isDisposed()) {
                    mImage.dispose();
                }
            }
        });
    }

    public void addNotify()
    {
        Viewport viewPort = mCanvas.getViewport();
        viewPort.addFigureListener(mFigureListener);
        addFigureListener(mFigureListener);
        viewPort.addPropertyChangeListener(mPropertyListener);
        super.addNotify();
    }

    public void removeNotify() 
    {
        super.removeNotify();
        Viewport viewPort = mCanvas.getViewport();
        viewPort.removeFigureListener(mFigureListener);
        viewPort.removePropertyChangeListener(mPropertyListener);
        removeFigureListener(mFigureListener);
    }

    public void setBounds(Rectangle rect)
    {
        if(bounds.width != rect.width || bounds.height != rect.height) {
            mNeedsReScrape = true;
        }
        super.setBounds(rect);
    }

    protected void paintFigure(Graphics g) 
    {
        g.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        super.paintFigure(g);
        if (mImage != null) {
            if(mImageBounds.width != bounds.width || mImageBounds.height != bounds.height) {
                g.drawImage(mImage, mImageBounds.x,mImageBounds.y);
            }
            else {
                g.drawImage(mImage,bounds.x,bounds.y);
            }
        }
    }

    /*
     * @see org.eclipse.draw2d.Figure#layout()
     */
    protected void layout() 
    {
        if(mNeedsReScrape) {
            Rectangle clientArea = mCanvas.getViewport().getClientArea();
            Rectangle rect = clientArea.intersect(bounds);
            if(rect.width > 0 && rect.height > 0) {
                if (mImage != null && !mImage.isDisposed()) {
                    mImage.dispose();
                }
                mImage = null;
                mImageBounds = rect;
                mImage = new Image(mCanvas.getDisplay(), mImageBounds.width,mImageBounds.height);
                Control control = createSWTControl(mCanvas);
                ControlSubclasser.subclassControl(control);
                Point p1 = new Point(0, 0);
                translateToAbsolute(p1);
                control.setBounds(bounds.x + p1.x, bounds.y + p1.y, 
                        bounds.width,bounds.height);
                control.moveAbove(null);
                control.addPaintListener(mSWTPaintListener);
            }
        }
        super.layout();
    }
    
    /**
     * @return
     */
    protected abstract Control createSWTControl(Composite parent);
}
