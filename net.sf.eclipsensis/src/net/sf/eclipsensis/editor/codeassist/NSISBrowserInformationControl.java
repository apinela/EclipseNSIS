/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.jdt.internal.ui.text.hover.BrowserInformationControl
 * Copyright (c) 2000-2008 IBM Corporation and others.
 *
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISExternalFileEditorInput;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class NSISBrowserInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, IInformationControlExtension3,  DisposeListener
{
    private static final int BORDER= 1;

    private Shell mShell;
    private ToolBar mToolBar;
    private Browser mBrowser;
    private boolean mBrowserHasContent;
    private int mMaxWidth= -1;
    private int mMaxHeight= -1;
    private boolean mHideScrollBars;
    private Listener mDeactivateListener;
    private ListenerList mFocusListeners= new ListenerList(ListenerList.IDENTITY);
    private String mKeyword = null;
    private ToolItem mBack = null;
    private ToolItem mForward = null;
    private Stack mBackKeywords = null;
    private Stack mForwardKeywords = null;

    private INSISBrowserFileURLHandler mFileURLHandler = new INSISBrowserFileURLHandler() {
        public void handleFile(File file)
        {
            NSISBrowserInformationControl.this.handleFile(file);
        }
    };

    private INSISBrowserKeywordURLHandler mKeywordURLHandler = new INSISBrowserKeywordURLHandler() {
        public void handleKeyword(String keyword)
        {
            NSISBrowserInformationControl.this.gotoKeyword(keyword);

        }
    };

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

        createToolBar(composite);
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
        hookLocationListener();
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
                        fontDatas[i].setHeight((int)(fontDatas[i].getHeight() * 0.9));
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

    public Shell getShell()
    {
        return mShell;
    }

    private boolean isValid(Image image)
    {
        return image !=null && !image.isDisposed();
    }

    private void createToolBar(Composite displayArea)
    {
        if(isValid(NSISBrowserUtility.BACK_IMAGE) && isValid(NSISBrowserUtility.DISABLED_BACK_IMAGE) &&
           isValid(NSISBrowserUtility.FORWARD_IMAGE) && isValid(NSISBrowserUtility.DISABLED_FORWARD_IMAGE) &&
           isValid(NSISBrowserUtility.HOME_IMAGE)) {
            mToolBar =  new ToolBar(displayArea, SWT.FLAT);
            GridData data = new GridData(SWT.RIGHT,SWT.FILL,true,false);
            mToolBar.setLayoutData(data);
            mToolBar.setBackground(mToolBar.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

            // Add a button to go back to original page
            final ToolItem home = new ToolItem(mToolBar, SWT.NONE);
            home.setImage(NSISBrowserUtility.HOME_IMAGE);
            home.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.home.text")); //$NON-NLS-1$

            // Add a button to navigate backwards through previously visited pages
            mBack = new ToolItem(mToolBar, SWT.NONE);
            mBack.setImage(NSISBrowserUtility.BACK_IMAGE);
            mBack.setDisabledImage(NSISBrowserUtility.DISABLED_BACK_IMAGE);
            mBack.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.back.text")); //$NON-NLS-1$

            // Add a button to navigate forward through previously visited pages
            mForward = new ToolItem(mToolBar, SWT.NONE);
            mForward.setImage(NSISBrowserUtility.FORWARD_IMAGE);
            mForward.setDisabledImage(NSISBrowserUtility.DISABLED_FORWARD_IMAGE);
            mForward.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.forward.text")); //$NON-NLS-1$

            Listener listener = new Listener() {
                public void handleEvent(Event event) {
                    ToolItem item = (ToolItem)event.widget;
                    if (item == home) {
                        if(!Common.isEmptyCollection(mBackKeywords)) {
                            String oldKeyword = mKeyword;
                            String keyword = (String)mBackKeywords.firstElement();
                            if(!Common.stringsAreEqual(oldKeyword, keyword)) {
                                if(setKeyword(keyword) && oldKeyword != null) {
                                    mForwardKeywords.clear();
                                    mBackKeywords.push(oldKeyword);
                                }
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (item == mBack) {
                        if(!Common.isEmptyCollection(mBackKeywords)) {
                            String oldKeyword = mKeyword;
                            String keyword = (String)mBackKeywords.pop();
                            if(setKeyword(keyword) && oldKeyword != null) {
                                mForwardKeywords.push(oldKeyword);
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (item == mForward) {
                        if(!Common.isEmptyCollection(mForwardKeywords)) {
                            String oldKeyword = mKeyword;
                            String keyword = (String)mForwardKeywords.pop();
                            if(setKeyword(keyword) && oldKeyword != null) {
                                mBackKeywords.push(oldKeyword);
                            }
                        }
                        updateToolbarButtons();
                    }
                }
            };
            home.addListener(SWT.Selection, listener);
            mBack.addListener(SWT.Selection, listener);
            mForward.addListener(SWT.Selection, listener);

            mBackKeywords = new Stack();
            mForwardKeywords = new Stack();
            updateToolbarButtons();
        }
    }

    private void gotoKeyword(String keyword)
    {
        String oldKeyword = mKeyword;
        if(setKeyword(keyword)) {
            if(oldKeyword != null && mBackKeywords != null) {
                mBackKeywords.push(oldKeyword);
            }
            if(mForwardKeywords != null) {
                mForwardKeywords.clear();
            }
            updateToolbarButtons();
        }
    }

    private boolean setKeyword(String keyword)
    {
        String help = NSISHelpURLProvider.getInstance().getKeywordHelp(keyword);
        if (!Common.isEmpty(help)) {
            mKeyword = keyword;
            mBrowser.setText(help);
            return true;
        }
        return false;
    }

    private void updateToolbarButtons()
    {
        if(mBack != null) {
            mBack.setEnabled(!Common.isEmptyCollection(mBackKeywords));
        }
        if(mForward != null) {
            mForward.setEnabled(!Common.isEmptyCollection(mForwardKeywords));
        }
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
        Point browserLoc = mBrowser.getLocation();
        Point location2= (mToolBar == null?browserLoc:mToolBar.getLocation());
        location.x += trim.x - browserLoc.x;
        location.y += trim.y - location2.y;
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
                    for (int i = 0; i < listeners.length; i++) {
                        ((FocusListener)listeners[i]).focusLost(new FocusEvent(event));
                    }
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

    private void hookLocationListener()
    {
        if(mBrowser != null && !mBrowser.isDisposed()) {
            mBrowser.addLocationListener(new LocationAdapter() {
                public void changing(LocationEvent event)
                {
                    if(!NSISBrowserUtility.ABOUT_BLANK.equalsIgnoreCase(event.location)) {
                        try {
                            NSISBrowserUtility.handleURL(event.location, mKeywordURLHandler, mFileURLHandler);
                        }
                        finally {
                            event.doit = false;
                        }
                    }
                }
            });
        }
    }

    private void handleFile(File f)
    {
        if (!HelpBrowserLocalFileHandler.INSTANCE.handle(f)) {
            if (IOUtility.isValidDirectory(f)) {
                try {
                    Program.launch(f.getCanonicalPath());
                }
                catch (IOException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else {
                String home = NSISPreferences.INSTANCE.getNSISHome();
                if(home != null) {
                    try {
                        if (f.getCanonicalPath().regionMatches(true, 0, home, 0, home.length())) {
                            String ext = IOUtility.getFileExtension(f);
                            if (NSISBrowserUtility.HTML_EXTENSIONS != null && NSISBrowserUtility.HTML_EXTENSIONS.contains(ext)) {
                                NSISHTMLHelp.showHelp(IOUtility.getFileURLString(f));
                                return;
                            }
                            else {
                                if(openInEditor(f)) {
                                    return;
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
                try {
                    Common.openExternalBrowser(IOUtility.getFileURLString(f));
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    private boolean openInEditor(File file)
    {
        IEditorInput editorInput;
        IEditorDescriptor descriptor;
        IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
        IFile ifile = null;
        IFile[] ifiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(file.getAbsolutePath()));
        if(!Common.isEmptyArray(ifiles)) {
            ifile = ifiles[0];
        }
        if(ifile != null) {
            editorInput = new FileEditorInput(ifile);
            descriptor = registry.getDefaultEditor(ifile.getName());
        }
        else {
            editorInput = new NSISExternalFileEditorInput(file);
            descriptor = registry.getDefaultEditor(file.getName());
        }
        if (descriptor != null) {
            try {
                IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                        editorInput, descriptor.getId());
                return true;
            }
            catch (PartInitException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
        return false;
    }
}

