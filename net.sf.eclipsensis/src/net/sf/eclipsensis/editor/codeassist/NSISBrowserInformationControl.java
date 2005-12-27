/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *     
 * Based upon org.eclipse.jdt.internal.ui.text.hover.BrowserInformationControl
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *     
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISBrowserInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension3,  DisposeListener 
{
    private static final int RIGHT_MARGIN= 3;
    private static final int BORDER= 1;

    private static boolean cIsAvailable= false;
    private static boolean cAvailabilityChecked= false;

    private Shell mShell;
    private Browser mBrowser;
    private boolean mBrowserHasContent;
    private int mMaxWidth= -1;
    private int mMaxHeight= -1;
    private boolean mHideScrollBars;
    private Listener mDeactivateListener;
    private ListenerList mFocusListeners= new ListenerList();

    public NSISBrowserInformationControl(Shell parent, int shellStyle, int style) 
    {
        GridLayout layout;
        GridData gd;

        mShell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | shellStyle);
        Display display= mShell.getDisplay();
        mShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

        int border= ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
        mShell.setLayout(new BorderFillLayout(border));

        Composite composite= mShell;
        layout= new GridLayout(1, false);
        layout.marginHeight= border;
        layout.marginWidth= border;
        composite.setLayout(layout);
        gd= new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gd);

        // Browser field
        mBrowser= new Browser(mShell, SWT.NONE);
        mHideScrollBars= (style & SWT.V_SCROLL) == 0 && (style & SWT.H_SCROLL) == 0;
        gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
        mBrowser.setLayoutData(gd);
        mBrowser.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        mBrowser.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        mBrowser.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e)  {
                if (e.character == 0x1B) // ESC
                    mShell.dispose();
            }

            public void keyReleased(KeyEvent e) {}
        });

        // Replace browser's built-in context menu with none
        mBrowser.setMenu(new Menu(mShell, SWT.NONE));

        addDisposeListener(this);
    }

    public static boolean isAvailable(Composite parent) 
    {
        if (!cAvailabilityChecked) {
            try {
                Browser browser= new Browser(parent, SWT.NONE);
                browser.dispose();
                cIsAvailable= true;
            } 
            catch (SWTError er) {
                cIsAvailable= false;
            } 
            finally {
                cAvailabilityChecked= true;
            }
        }

        return cIsAvailable;
    }

    public void setInformation(String content) 
    {
        mBrowserHasContent= content != null && content.length() > 0;

        if (mBrowserHasContent) {
            int shellStyle= mShell.getStyle();
            boolean RTL= (shellStyle & SWT.RIGHT_TO_LEFT) != 0;

            String[] styles= null;
            if (RTL && !mHideScrollBars) {
                styles= new String[] { "direction:rtl" }; //$NON-NLS-1$
            }
            else if (RTL && mHideScrollBars) {
                styles= new String[] { "direction:rtl", "overflow:hidden" }; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if (mHideScrollBars) {
                styles= new String[] { "overflow:hidden" }; //$NON-NLS-1$
            }

            if (styles != null) {
                StringBuffer buffer= new StringBuffer(content);
                insertStyles(buffer, styles);
                content= buffer.toString();
            }
        }

        mBrowser.setText(content);
        mBrowser.setSize(Math.min(200, mMaxWidth), Math.min(mMaxHeight, 50));
    }

    private void insertStyles(StringBuffer buffer, String[] styles) 
    {
        if (Common.isEmptyArray(styles)) {
            return;
        }

        StringBuffer styleBuf= new StringBuffer(" style=\"").append(styles[0]); //$NON-NLS-1$
        for (int i= 1; i < styles.length; i++) {
            styleBuf.append("; ").append(styles[i]); //$NON-NLS-1$
        }
        styleBuf.append('"');

        // Find insertion index
        int index= buffer.indexOf("<body "); //$NON-NLS-1$
        if (index == -1) {
            index=buffer.indexOf("<body>"); //$NON-NLS-1$
            if (index == -1) {
                return;
            }
        }

        buffer.insert(index+5, styleBuf);
    }

    public void setVisible(boolean visible) 
    {
        mShell.setVisible(visible);
    }

    public void dispose() 
    {
        if (mShell != null && !mShell.isDisposed()) {
            mShell.dispose();
        }
        else {
            widgetDisposed(null);
        }
    }

    public void widgetDisposed(DisposeEvent event) 
    {
        mShell= null;
        mBrowser= null;
    }

    public void setSize(int width, int height) 
    {
        mShell.setSize(Math.min(width, mMaxWidth), Math.min(height, mMaxHeight));
    }

    public void setLocation(Point location) 
    {
        Rectangle trim= mShell.computeTrim(0, 0, 0, 0);
        Point textLocation= mBrowser.getLocation();
        location.x += trim.x - textLocation.x;
        location.y += trim.y - textLocation.y;
        mShell.setLocation(location);
    }

    public void setSizeConstraints(int maxWidth, int maxHeight) 
    {
        mMaxWidth= maxWidth;
        mMaxHeight= maxHeight;
    }

    public Point computeSizeHint() 
    {
        return mShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    public Rectangle computeTrim() 
    {
        return mShell.computeTrim(0, 0, 0, 0);
    }

    public Rectangle getBounds() 
    {
        return mShell.getBounds();
    }

    public boolean restoresLocation() 
    {
        return false;
    }

    public boolean restoresSize() 
    {
        return false;
    }

    public void addDisposeListener(DisposeListener listener) 
    {
        mShell.addDisposeListener(listener);
    }

    public void removeDisposeListener(DisposeListener listener) 
    {
        mShell.removeDisposeListener(listener);
    }

    public void setForegroundColor(Color foreground) 
    {
        mBrowser.setForeground(foreground);
    }

    public void setBackgroundColor(Color background) 
    {
        mBrowser.setBackground(background);
    }

    public boolean isFocusControl() 
    {
        return mBrowser.isFocusControl();
    }

    public void setFocus() 
    {
        mShell.forceFocus();
        mBrowser.setFocus();
    }

    public void addFocusListener(final FocusListener listener) 
    {
        mBrowser.addFocusListener(listener);

        if (mFocusListeners.isEmpty()) {
            mDeactivateListener=  new Listener() {
                public void handleEvent(Event event) {
                    Object[] listeners= mFocusListeners.getListeners();
                    for (int i = 0; i < listeners.length; i++)
                        ((FocusListener)listeners[i]).focusLost(new FocusEvent(event));
                }
            };
            mBrowser.getShell().addListener(SWT.Deactivate, mDeactivateListener);
        }
        mFocusListeners.add(listener);
    }

    public void removeFocusListener(FocusListener listener) 
    {
        mBrowser.removeFocusListener(listener);

        mFocusListeners.remove(listener);
        if (mFocusListeners.isEmpty()) {
            mBrowser.getShell().removeListener(SWT.Deactivate, mDeactivateListener);
            mDeactivateListener= null;
        }
    }

    public boolean hasContents() 
    {
        return mBrowserHasContent;
    }

    private static class BorderFillLayout extends Layout 
    {
        final int mBorderSize;

        public BorderFillLayout(int borderSize) 
        {
            if (borderSize < 0) {
                throw new IllegalArgumentException();
            }
            mBorderSize= borderSize;
        }

        public int getBorderSize() 
        {
            return mBorderSize;
        }

        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) 
        {
            Control[] children= composite.getChildren();
            Point minSize= new Point(0, 0);

            if (children != null) {
                for (int i= 0; i < children.length; i++) {
                    Point size= children[i].computeSize(wHint, hHint, flushCache);
                    minSize.x= Math.max(minSize.x, size.x);
                    minSize.y= Math.max(minSize.y, size.y);
                }
            }

            minSize.x += mBorderSize * 2 + RIGHT_MARGIN;
            minSize.y += mBorderSize * 2;

            return minSize;
        }

        protected void layout(Composite composite, boolean flushCache) 
        {
            Control[] children= composite.getChildren();
            Point minSize= new Point(composite.getClientArea().width, composite.getClientArea().height);

            if (children != null) {
                for (int i= 0; i < children.length; i++) {
                    Control child= children[i];
                    child.setSize(minSize.x - mBorderSize * 2, minSize.y - mBorderSize * 2);
                    child.setLocation(mBorderSize, mBorderSize);
                }
            }
        }
    }
}

