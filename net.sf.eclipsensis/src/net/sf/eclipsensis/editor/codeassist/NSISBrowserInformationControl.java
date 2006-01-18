/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *     
 * Based upon org.eclipse.jdt.internal.ui.text.hover.BrowserInformationControl
 * Copyright (c) 2000-2006 IBM Corporation and others.
 *     
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.util.ArrayList;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
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

public class NSISBrowserInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, IInformationControlExtension3,  DisposeListener 
{
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
    private String mKeyword = null;

    public NSISBrowserInformationControl(final Shell parent, int shellStyle, int style) 
    {
        GridLayout layout;

        mShell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | shellStyle);
        Display display= mShell.getDisplay();
        mShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        layout= new GridLayout(1, false);
        layout.marginHeight=  layout.marginWidth= (((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER);
        mShell.setLayout(layout);
        mShell.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

        Composite composite= new Composite(mShell,SWT.NONE);
        layout= new GridLayout(1, false);
        layout.marginHeight=  layout.marginWidth= layout.verticalSpacing = 0;
        composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

        // Browser field
        mBrowser= new Browser(composite, SWT.NONE);
        mHideScrollBars= (style & SWT.V_SCROLL) == 0 && (style & SWT.H_SCROLL) == 0;
        mBrowser.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        mBrowser.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        mBrowser.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        mBrowser.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e)  {
                if (e.character == 0x1B) {// ESC
                    if(mShell != null && !mShell.isDisposed()) {
                        mShell.dispose();
                    }
                }
            }

            public void keyReleased(KeyEvent e) {}
        });

        // Replace browser's built-in context menu with none
        mBrowser.setMenu(new Menu(mShell, SWT.NONE));
        
        if (NSISHelpURLProvider.getInstance().isNSISHelpAvailable()) {
            ParameterizedCommand command = NSISInformationUtility.getCommand(INSISConstants.GOTO_HELP_COMMAND_ID);
            ArrayList list = new ArrayList();
            ArrayList list2 = new ArrayList();
            if (command != null) {
                KeySequence[] sequences = NSISInformationUtility.getKeySequences(command);
                if (!Common.isEmptyArray(sequences)) {
                    for (int i = 0; i < sequences.length; i++) {
                        KeyStroke[] strokes = sequences[i].getKeyStrokes();
                        if (!Common.isEmptyArray(strokes) && strokes.length == 1) {
                            list.add(sequences[i]);
                            list2.add(new int[]{strokes[0].getNaturalKey(), strokes[0].getModifierKeys()});
                        }
                    }
                }

                String statusText;
                try {
                    statusText = NSISInformationUtility.buildStatusText(command.getCommand().getDescription(), (KeySequence[])list.toArray(new KeySequence[list.size()]));
                }
                catch (Exception e) {
                    statusText = null;
                }
                if (!Common.isEmpty(statusText)) {
                    final int[][] keys = (int[][])list2.toArray(new int[list2.size()][]);

                    mBrowser.addKeyListener(new KeyListener() {
                        public void keyPressed(KeyEvent e)
                        {
                            if (!Common.isEmpty(mKeyword)) {
                                for (int i = 0; i < keys.length; i++) {
                                    if (e.keyCode == keys[i][0] && (e.stateMask & keys[i][1]) == e.stateMask) {
                                        String keyword = mKeyword;
                                        if(mShell != null && !mShell.isDisposed()) {
                                            mShell.dispose();
                                        }
                                        NSISHelpURLProvider.getInstance().showHelpURL(keyword);
                                    }
                                }
                            }
                        }

                        public void keyReleased(KeyEvent e)
                        {
                        }
                    });

                    Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
                    separator.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                    separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

                    Label l = new Label(composite, SWT.NONE);
                    Font font = l.getFont();
                    FontData[] fontDatas = font.getFontData();
                    for (int i = 0; i < fontDatas.length; i++) {
                        fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
                    }
                    final Font font2 = new Font(l.getDisplay(), fontDatas);
                    l.setFont(font2);
                    l.addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e)
                        {
                            font2.dispose();
                        }
                    });
                    l.setText(statusText);
                    l.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
                    l.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                    l.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
                }
            }
        }        
        addDisposeListener(this);
    }
    
    public void addKeyListener(KeyListener listener)
    {
        if(mBrowser != null) {
            mBrowser.addKeyListener(listener);
        }
    }
    
    public void removeKeyListener(KeyListener listener)
    {
        if(mBrowser != null) {
            mBrowser.removeKeyListener(listener);
        }
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

    public void setInput(Object input)
    {
        if (input instanceof String) {
            setInformation((String)input);
        }
        else if(input instanceof NSISBrowserInformation) {
            NSISBrowserInformation info = (NSISBrowserInformation)input;
            setInformation(info.getInformation());
            mKeyword = info.getKeyword();
        }
    }

    public void setInformation(String content) 
    {
        mKeyword = null;
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
        mKeyword = null;
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
}

