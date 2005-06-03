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

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public abstract class SWTControlFigure extends Figure implements IInstallOptionsFigure
{
    private Image mImage;
    private Control mControl;
    private Rectangle mImageBounds;
    private boolean mNeedsReScrape = true;

    protected FigureCanvas mCanvas;
    protected boolean mDisabled = false;
    protected GraphicalEditPart mEditPart;
    
    private int mStyle = -1;
    
    private FigureListener mFigureListener = new FigureListener() {
        public void figureMoved(IFigure source) {
            if (isNeedsReScrape()) {
                layout();
            }
        }
    };

    private PropertyChangeListener mPropertyListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(Viewport.PROPERTY_VIEW_LOCATION)) {
                if(isNeedsReScrape()) {
                    layout();
                }
            }
        }
    };
    
    private PaintListener mSWTPaintListener = new PaintListener() {
        public void paintControl(PaintEvent e) 
        {
            if(e.width > 0 && e.height > 0) {
                final Control source = (Control) e.getSource();
                if(!source.isDisposed() && source.handle > 0) {
                    Point p1 = new Point(0, 0);
                    translateToAbsolute(p1);
                    int borderWidth = source.getBorderWidth();
//                    setNeedsReScrape(!(e.width+2*borderWidth >= bounds.width && e.height+2*borderWidth >= bounds.height));
                    setNeedsReScrape(mImageBounds.width != bounds.width || mImageBounds.height != bounds.height);
//                    e.gc.copyArea(mImage, mImageBounds.x-bounds.x, mImageBounds.y-bounds.y);
                    GC gc = new GC(mCanvas);
                    gc.copyArea(mImage, p1.x+mImageBounds.x, p1.y+mImageBounds.y);
                    gc.dispose();
        
                    try {
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                if(!source.isDisposed()) {
                                    source.removePaintListener(mSWTPaintListener);
                                    source.dispose();
                                }
                            }
                        });
                    }
                    catch(Exception e1) { }
                    repaint();
                }
            }
        }
    };

    public SWTControlFigure(GraphicalEditPart editPart)
    {
        super();
        mEditPart = editPart;
        setLayoutManager(new XYLayout());
        mCanvas = (FigureCanvas) editPart.getViewer().getControl();
        mCanvas.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                if(mImage != null && !mImage.isDisposed()) {
                    mImage.dispose();
                }
            }
        });
    }

    public GraphicalEditPart getEditPart()
    {
        return mEditPart;
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
    
    public void setDisabled(boolean disabled)
    {
        mDisabled = disabled;
    }

    public void refresh()
    {
        if(!isNeedsReScrape()) {
            setNeedsReScrape(true);
        }
        layout();
    }

    protected boolean isNeedsReScrape()
    {
        return mNeedsReScrape;
    }

    protected void setNeedsReScrape(boolean needsReScrape)
    {
        mNeedsReScrape = needsReScrape;
    }

    public void setBounds(Rectangle rect)
    {
        if(bounds.width != rect.width || bounds.height != rect.height) {
            setNeedsReScrape(true);
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
    protected synchronized void layout() 
    {
        if(isNeedsReScrape()) {
            Rectangle clientArea = mCanvas.getViewport().getClientArea();
            Rectangle rect = clientArea.intersect(bounds);
            if(rect.width > 0 && rect.height > 0) {
                if (mImage != null && !mImage.isDisposed()) {
                    mImage.dispose();
                }
                mImage = null;
                mImageBounds = rect;
                mImage = new Image(mCanvas.getDisplay(), mImageBounds.width,mImageBounds.height);

                if(mControl != null && !mControl.isDisposed()) {
                    mControl.removePaintListener(mSWTPaintListener);
                    mControl.dispose();
                }
                mControl = createSWTControl(mCanvas);
                mControl.setVisible(isVisible());
                mControl.setEnabled(!mDisabled);
                ControlSubclasser.subclassControl(mControl);
                Point p1 = new Point(0, 0);
                translateToAbsolute(p1);
                mControl.setBounds(bounds.x + p1.x, bounds.y + p1.y, 
                        bounds.width,bounds.height);
                mControl.moveAbove(null);
                mControl.addPaintListener(mSWTPaintListener);
            }
        }
        super.layout();
    }
    
    
    public int getStyle()
    {
        return (mStyle <0?getDefaultStyle():mStyle);
    }
    
    public void setStyle(int style)
    {
        mStyle = style;
    }
    
    /**
     * @return
     */
    protected abstract Control createSWTControl(Composite parent);
    public abstract int getDefaultStyle();
}
