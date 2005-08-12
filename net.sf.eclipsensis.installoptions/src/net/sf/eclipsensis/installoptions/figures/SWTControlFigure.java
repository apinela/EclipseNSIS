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

import java.util.List;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class SWTControlFigure extends Figure implements IInstallOptionsFigure
{
    private static final int PRINT_BITS = WinAPI.PRF_NONCLIENT | WinAPI.PRF_CLIENT | WinAPI.PRF_ERASEBKGND | WinAPI.PRF_CHILDREN;

    private Composite mParent;
    private Image mImage;
    private boolean mNeedsReScrape = true;

    protected boolean mDisabled = false;
    private int mStyle = -1;
    private PaintListener mSWTPaintListener = new PaintListener() {
        public void paintControl(PaintEvent e) 
        {
            final Control source = (Control) e.getSource();
            if(!source.isDisposed() && source.handle > 0) {
                if(mImage != null && !mImage.isDisposed()) {
                    mImage.dispose();
                }
                mImage = getImage(source);
                source.removePaintListener(mSWTPaintListener);
                repaint();
                try {
                  source.getDisplay().asyncExec(new Runnable() {
                      public void run() {
                          if(!source.isDisposed()) {
                              source.dispose();
                          }
                      }
                  });
                }
                catch(Exception e1) { }
            }
        }
    };

    public SWTControlFigure(Composite parent, IPropertySource propertySource)
    {
        this(parent, propertySource, -1);
    }

    public SWTControlFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super();
        mStyle = style;
        setLayoutManager(new XYLayout());
        mParent = parent;
        if(mParent != null) {
            mParent.addDisposeListener(new DisposeListener(){
                public void widgetDisposed(DisposeEvent e)
                {
                    if(mImage != null && !mImage.isDisposed()) {
                        mImage.dispose();
                    }
                }
            });
        }
        init(propertySource);
    }

    protected void init(IPropertySource propertySource)
    {
        List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
        setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
        setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
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
        super.paintFigure(g);
        if (mImage != null && !mImage.isDisposed()) {
            Border b = getBorder();
            Insets insets;
            if(b != null) {
                insets = b.getInsets(this);
            }
            else {
                insets = new Insets(0,0,0,0);
            }
            g.drawImage(mImage,bounds.x+insets.left,bounds.y+insets.top);
        }
    }

    private Image getImage(Control control)
    {
        org.eclipse.swt.graphics.Rectangle rect = control.getBounds();
        if (rect.width <= 0 || rect.height <= 0) {
            return new Image(control.getDisplay(), 1, 1);
        }
        Image image = new Image (control.getDisplay(), rect.width, rect.height);
        GC gc = new GC (image);
        WinAPI.SendMessage (control.handle, WinAPI.WM_PRINT, gc.handle, PRINT_BITS);
        gc.dispose ();
        return image;
    }

    /*
     * @see org.eclipse.draw2d.Figure#layout()
     */
    protected synchronized void layout() 
    {
        if(isNeedsReScrape()) {
            if (mImage != null && !mImage.isDisposed()) {
                mImage.dispose();
            }
            mImage = null;
            if(isVisible()) {
                Control control = createSWTControl(mParent, (mStyle <0?getDefaultStyle():mStyle));
                control.setVisible(true);
                control.setEnabled(!mDisabled);
                ControlSubclasser.subclassControl(control, this);
                Point p1 = new Point(0, 0);
                translateToAbsolute(p1);
                Border b = getBorder();
                Insets insets;
                if(b != null) {
                    insets = b.getInsets(this);
                }
                else {
                    insets = new Insets(0,0,0,0);
                }
                control.setBounds(bounds.x + p1.x + insets.left, bounds.y + p1.y + insets.right, 
                                   bounds.width - (insets.left+insets.right), 
                                   bounds.height - (insets.top+insets.bottom));
                control.addPaintListener(mSWTPaintListener);
                setNeedsReScrape(false);
                
                //Force a repaint
                control.setVisible(false);
                control.setVisible(true);
            }
        }
        super.layout();
    }
    
    protected boolean isNeedsTheme()
    {
        return false;
    }

    protected String getTheme()
    {
        return null;
    }
    
    protected int getThemePartId()
    {
        return 0;
    }
    
    protected int getThemeStateId()
    {
        return 0;
    }

    int getStyle()
    {
        return mStyle;
    }

    protected abstract Control createSWTControl(Composite parent, int style);
    public abstract int getDefaultStyle();
}
