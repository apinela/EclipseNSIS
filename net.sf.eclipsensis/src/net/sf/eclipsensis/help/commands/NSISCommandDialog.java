/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.help.NSISHTMLHelp;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class NSISCommandDialog extends StatusMessageDialog
{
    private static final int MIN_WINDOW_HEIGHT = 400;
    private static final int MIN_WINDOW_WIDTH = 350;
    private static final String SETTING_COLLAPSE_HELP = "collapseHelp"; //$NON-NLS-1$
    private static Map cCommandStateMap;
    private static final Object JOB_FAMILY = new Object();
    
    private NSISCommand mCommand;

    private Browser mBrowser;
    private Control mParamEditorControl;
    private String mCurrentCommand = null;
    private ToolItem mBack = null;
    private ToolItem mForward = null;
    private Stack mBackCommands = null;
    private Stack mForwardCommands = null;
    private Map mSettings = null;
    private boolean mCollapseHelp = true;
    private IDialogSettings mDialogSettings = null;
    
    static {
        final File stateLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),"net.sf.eclipsensis.help.commands.NSISCommandSettings.ser"); //$NON-NLS-1$
        EclipseNSISPlugin.getDefault().registerService(new IEclipseNSISService() {
            private boolean mStarted = false;
            
            public void start(IProgressMonitor monitor)
            {
                if(IOUtility.isValidFile(stateLocation)) {
                    try {
                        cCommandStateMap = (Map)IOUtility.readObject(stateLocation);
                    }
                    catch (Exception e) {
                        EclipseNSISPlugin.getDefault().log(e);
                        cCommandStateMap = null;
                    }
                }
                if(cCommandStateMap == null) {
                    cCommandStateMap = new HashMap();
                }
                mStarted = true;
            }

            public void stop(IProgressMonitor monitor)
            {
                try {
                    IOUtility.writeObject(stateLocation, cCommandStateMap);
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
                mStarted = false;
            }

            public boolean isStarted()
            {
                return mStarted;
            }
        });
    }
    
    private INSISBrowserFileURLHandler mFileURLHandler = new INSISBrowserFileURLHandler() {
        public void handleFile(File file)
        {
            NSISCommandDialog.this.handleFile(file);
        }
    };
    private INSISBrowserKeywordURLHandler mKeywordURLHandler = new INSISBrowserKeywordURLHandler() {
        public void handleKeyword(String keyword)
        {
            gotoCommand(keyword);

        }
    };

    private Listener mFilter = new Listener() {
        final JobScheduler jobScheduler = EclipseNSISPlugin.getDefault().getJobScheduler();
        
        private boolean isChildOf(Control parent, Control child)
        {
            while(child != null) {
                if(child == parent) {
                    return true;
                }
                child = child.getParent();
            }
            return false;
        }

        public void handleEvent(Event event)
        {
            if(event.widget instanceof Control) {
                if(mParamEditorControl != null && !mParamEditorControl.isDisposed() && isChildOf(mParamEditorControl,(Control)event.widget)) {
                    jobScheduler.cancelJobs(JOB_FAMILY);
                    jobScheduler.scheduleUIJob(JOB_FAMILY,"NSISCommandDialog", new IJobStatusRunnable() { //$NON-NLS-1$
                        public IStatus run(IProgressMonitor monitor)
                        {
                            validate();
                            return Status.OK_STATUS;
                        }
                    });
                }
            }
        }
    };
    private INSISParamEditor mParamEditor;
    private String mCommandText = ""; //$NON-NLS-1$
    private boolean mRemember;
    private Composite mControl;
    
    public NSISCommandDialog(Shell parent, NSISCommand command)
    {
        super(parent);
        mCommand = command;
        setTitle(EclipseNSISPlugin.getResourceString("nsis.command.wizard.title")); //$NON-NLS-1$
        setShellImage(EclipseNSISPlugin.getShellImage());
        mSettings = (Map)cCommandStateMap.get(mCommand.getName());
        mRemember = (mSettings != null);
        mParamEditor = mCommand.createEditor();
        setParamEditorState();
        IDialogSettings dialogSettings = EclipseNSISPlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if(mDialogSettings == null) {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        try {
            mCollapseHelp = mDialogSettings.getBoolean(SETTING_COLLAPSE_HELP);
        }
        catch(Exception ex) {
            mCollapseHelp = false;
        }
    }
    
    protected Point getInitialSize()
    {
        Point p = super.getInitialSize();
        if(p.y < MIN_WINDOW_HEIGHT) {
            p.y = MIN_WINDOW_HEIGHT;
        }
        int width = mParamEditorControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        Composite parent = mParamEditorControl.getParent();
        while(parent != null) {
            Layout layout = parent.getLayout();
            if (layout instanceof GridLayout) {
                GridLayout gridLayout = (GridLayout)layout;
                width += 2*gridLayout.marginWidth;
            }
            parent = parent.getParent();
        }
        width = Math.max(width, MIN_WINDOW_WIDTH);
        if(mCollapseHelp) {
            p.x = width;
        }
        else {
            p.x = 2*width + ((GridLayout)mControl.getLayout()).horizontalSpacing;                        
        }
        return p;
    }

    public void create()
    {
        super.create();
        getButton(IDialogConstants.OK_ID).setText(IDialogConstants.FINISH_LABEL);
        if(mParamEditor != null) {
            mParamEditor.initEditor();
        }
        validate();
    }


    public String getCommandText()
    {
        return mCommandText;
    }

    public boolean close()
    {
        mParamEditor = null;
        getShell().getDisplay().removeFilter(SWT.Modify, mFilter);
        getShell().getDisplay().removeFilter(SWT.Selection, mFilter);
        return super.close();
    }

    private void validate()
    {
        if(mParamEditor != null) {
           String error = mParamEditor.validate();
           if(error != null) {
               updateStatus(new DialogStatus(IStatus.ERROR,error));
           }
           else {
               updateStatus(new DialogStatus(IStatus.OK,"")); //$NON-NLS-1$
           }
        }
    }

    protected void cancelPressed()
    {
        saveDialogSettings();
        super.cancelPressed();
    }

    private void saveDialogSettings()
    {
        mDialogSettings.put(SETTING_COLLAPSE_HELP, mCollapseHelp);
    }

    protected void okPressed()
    {
        saveDialogSettings();
        StringBuffer buf = new StringBuffer(mCommand.getName());
        mParamEditor.appendText(buf);
        mCommandText = buf.toString();
        if(mRemember) {
            cCommandStateMap.put(mCommand.getName(), mSettings);
        }
        else {
            cCommandStateMap.remove(mCommand.getName());
        }
        super.okPressed();
    }

    protected void createControlAndMessageArea(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = layout.verticalSpacing = layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayout(new GridLayout(1, false));
        composite2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        Color white = composite2.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        composite2.setBackground(white);
        
        Label titleHeader = new Label(composite2,SWT.NONE);
        titleHeader.setBackground(white);
        makeBold(titleHeader);
        titleHeader.setText(EclipseNSISPlugin.getResourceString("nsis.command.wizard.title")); //$NON-NLS-1$
        titleHeader.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        Label titleSubHeader = new Label(composite2,SWT.WRAP);
        titleSubHeader.setBackground(white);
        titleSubHeader.setText(EclipseNSISPlugin.getResourceString("nsis.command.wizard.subtitle")); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        data.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.INDENT);
        titleSubHeader.setLayoutData(data);
        
        Label titleImage = new Label(composite,SWT.NONE);
        titleImage.setBackground(white);
        titleImage.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,true));
        titleImage.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("command.wizard.title.image"))); //$NON-NLS-1$
        
        Label titleSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData(SWT.FILL,SWT.FILL,true,false);
        data.horizontalSpan = 2;
        titleSeparator.setLayoutData(data);

        super.createControlAndMessageArea(parent);
    }
    
    private Group wrapParamEditorControl(Composite parent)
    {
        Group g = new Group(parent,SWT.NONE);
        g.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.marginTop = 5;
        g.setLayout(layout);
        mParamEditorControl.setParent(g);
        mParamEditorControl.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        return g;
    }
    
    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayout(new GridLayout(1,false));
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        
        mControl = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2,true);
        layout.marginHeight = layout.marginWidth = 0;
        mControl.setLayout(layout);
        mControl.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        
        final Composite child = new Composite(mControl, SWT.NONE);
        layout = new GridLayout(2,false);
        layout.marginHeight = layout.marginWidth = 0;
        child.setLayout(layout);
        Label l = new Label(child,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        l.setText(mCommand.getName());
        makeBold(l);
        
        final String showText = EclipseNSISPlugin.getResourceString("show.description.label"); //$NON-NLS-1$
        final String hideText = EclipseNSISPlugin.getResourceString("hide.description.label"); //$NON-NLS-1$
        final Button toggleHelp = new Button(child,SWT.PUSH);
        GridData gridData = new GridData(SWT.FILL,SWT.CENTER,false,false);
        GC gc = new GC(toggleHelp);
        gridData.widthHint = Math.max(gc.stringExtent(showText).x, gc.stringExtent(hideText).x)+10;
        gc.dispose();
        toggleHelp.setLayoutData(gridData);
        toggleHelp.setText(mCollapseHelp?showText:hideText);
        
        Composite c = new Composite(child,SWT.None);
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        data.horizontalSpan = 2;
        c.setLayoutData(data);
        layout = new GridLayout(1,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        mParamEditorControl = mParamEditor.createControl(c);
        if(mParamEditorControl != null) {
            Group g;
            if(mParamEditorControl instanceof Group) {
                g = (Group)mParamEditorControl;
                if(!Common.isEmpty(g.getText())) {
                    g = wrapParamEditorControl(c);
                }
                else {
                    mParamEditorControl.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
                    ((GridLayout)g.getLayout()).marginTop = 5;
                }
            }
            else {
                g = wrapParamEditorControl(c);
            }
            g.setText(EclipseNSISPlugin.getResourceString("nsis.command.parameters.label")); //$NON-NLS-1$
        }
        
        c = new Composite(child,SWT.NONE);
        data = new GridData(SWT.FILL,SWT.FILL,true,false);
        data.horizontalSpan = 2;
        c.setLayoutData(data);
        layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        final Button button = new Button(c,SWT.CHECK);
        button.setText(EclipseNSISPlugin.getResourceString("nsis.command.wizard.remember.label")); //$NON-NLS-1$
        button.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        button.setSelection(mRemember);

        final Button button2 = new Button(c,SWT.PUSH);
        button2.setText(EclipseNSISPlugin.getResourceString("revert.label")); //$NON-NLS-1$
        button2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mParamEditor.reset();
                mParamEditor.initEditor();
            }
        });
        setButtonLayoutData(button2);
        button2.setEnabled(mRemember && !Common.isEmptyMap(mSettings));

        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mRemember = button.getSelection();
                setParamEditorState();
                button2.setEnabled(mRemember && !Common.isEmptyMap(mSettings));
            }
        });

        Button button3 = new Button(c,SWT.PUSH);
        button3.setText(EclipseNSISPlugin.getResourceString("clear.label")); //$NON-NLS-1$
        button3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mParamEditor.clear();
            }
        });
        setButtonLayoutData(button3); 
        
        data = new GridData(SWT.FILL,SWT.FILL,true,true);
        child.setLayoutData(data);
        
        final Composite child2 = new Composite(mControl, SWT.NONE);
        child2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(1,false);
        layout.marginHeight = layout.marginWidth = 0;
        child2.setLayout(layout);
        createHelpBrowser(child2);
        if(mBrowser != null) {
            setCurrentCommand(mCommand.getName());
            toggleHelp.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    mCollapseHelp = !mCollapseHelp;
                    toggleHelp.setText(mCollapseHelp?showText:hideText);
                    ((GridLayout)mControl.getLayout()).numColumns = (mCollapseHelp?1:2);
                    child2.setVisible(!mCollapseHelp);
                    
                    ((GridData)child2.getLayoutData()).exclude = mCollapseHelp;
                    int width1 = getShell().getSize().x;
                    int width2 = mControl.getSize().x;
                    int margin = width1-width2;
                    if(mCollapseHelp) {
                        width2 = (width2 - ((GridLayout)mControl.getLayout()).horizontalSpacing)/2;
                    }
                    else {
                        width2 = 2*width2 + ((GridLayout)mControl.getLayout()).horizontalSpacing;                        
                    }
                    width1 = width2+margin;
                    getShell().setSize(width1,getShell().getSize().y);
                    mControl.layout(true);
                }
            });
            ((GridLayout)mControl.getLayout()).numColumns = (mCollapseHelp?1:2);
            child2.setVisible(!mCollapseHelp);
            ((GridData)child2.getLayoutData()).exclude = mCollapseHelp;
        }
        else {
            mCollapseHelp = true;
            ((GridLayout)mControl.getLayout()).numColumns = 1;
            child2.dispose();
            toggleHelp.setVisible(false);
        }
        
        getShell().getDisplay().addFilter(SWT.Modify, mFilter);
        getShell().getDisplay().addFilter(SWT.Selection, mFilter);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_cmdwizard_context");
        return composite;
    }

    private void makeBold(Control control)
    {
        FontData[] f = control.getFont().getFontData();
        for (int i = 0; i < f.length; i++) {
            f[i].setStyle(f[i].getStyle()|SWT.BOLD);
        }
        final Font font = new Font(control.getDisplay(),f);
        control.setFont(font);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                font.dispose();
            }
        });
    }

    private void createHelpBrowser(final Composite parent)
    {
        if(NSISBrowserUtility.isBrowserAvailable(parent)) {
            Group group = new Group(parent,SWT.NONE);
            group.setText(EclipseNSISPlugin.getResourceString("nsis.command.description.label")); //$NON-NLS-1$
            GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            group.setLayoutData(gridData);
            GridLayout layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 2;
            group.setLayout(layout);

            Composite composite = new Composite(group,SWT.NONE);
            gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            composite.setLayoutData(gridData);
            layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 0;
            composite.setLayout(layout);
            createToolBar(composite);
            mBrowser= new Browser(composite, SWT.BORDER);
            gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            initializeDialogUnits(mBrowser);
            gridData.heightHint = convertHeightInCharsToPixels(10);
            mBrowser.setLayoutData(gridData);
            mBrowser.setMenu(new Menu(getShell(), SWT.NONE));
            hookLocationListener();
        }
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
            
            ToolBar toolBar =  new ToolBar(displayArea, SWT.FLAT);
            toolBar.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));

            // Add a button to go back to original page
            final ToolItem home = new ToolItem(toolBar, SWT.NONE);
            home.setImage(NSISBrowserUtility.HOME_IMAGE);
            home.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.home.text")); //$NON-NLS-1$
    
            // Add a button to navigate backwards through previously visited pages
            mBack = new ToolItem(toolBar, SWT.NONE);
            mBack.setImage(NSISBrowserUtility.BACK_IMAGE);
            mBack.setDisabledImage(NSISBrowserUtility.DISABLED_BACK_IMAGE);
            mBack.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.back.text")); //$NON-NLS-1$
    
            // Add a button to navigate forward through previously visited pages
            mForward = new ToolItem(toolBar, SWT.NONE);
            mForward.setImage(NSISBrowserUtility.FORWARD_IMAGE);
            mForward.setDisabledImage(NSISBrowserUtility.DISABLED_FORWARD_IMAGE);
            mForward.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.forward.text")); //$NON-NLS-1$

            Listener listener = new Listener() {
                public void handleEvent(Event event) {
                    ToolItem item = (ToolItem)event.widget;
                    if (item == home) {
                        if(!Common.isEmptyCollection(mBackCommands)) {
                            String oldKeyword = mCurrentCommand;
                            String keyword = (String)mBackCommands.firstElement();
                            if(!Common.stringsAreEqual(oldKeyword, keyword)) {
                                if(setCurrentCommand(keyword) && oldKeyword != null) {
                                    mForwardCommands.clear();
                                    mBackCommands.push(oldKeyword);
                                }
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (item == mBack) {
                        if(!Common.isEmptyCollection(mBackCommands)) {
                            String oldKeyword = mCurrentCommand;
                            String keyword = (String)mBackCommands.pop();
                            if(setCurrentCommand(keyword) && oldKeyword != null) {
                                mForwardCommands.push(oldKeyword);
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (item == mForward) {
                        if(!Common.isEmptyCollection(mForwardCommands)) {
                            String oldKeyword = mCurrentCommand;
                            String keyword = (String)mForwardCommands.pop();
                            if(setCurrentCommand(keyword) && oldKeyword != null) {
                                mBackCommands.push(oldKeyword);
                            }
                        }
                        updateToolbarButtons();
                    }
                }
            };
            home.addListener(SWT.Selection, listener);
            mBack.addListener(SWT.Selection, listener);
            mForward.addListener(SWT.Selection, listener);
            
            mBackCommands = new Stack();
            mForwardCommands = new Stack();
            updateToolbarButtons();
        }
    }
    
    private void gotoCommand(String command)
    {
        String oldCommand = mCurrentCommand;
        if(setCurrentCommand(command)) {
            if(oldCommand != null && mBackCommands != null) {
                mBackCommands.push(oldCommand);
            }
            if(mForwardCommands != null) {
                mForwardCommands.clear();
            }
            updateToolbarButtons();
        }
    }

    private boolean setCurrentCommand(String command)
    {
        String help = NSISHelpURLProvider.getInstance().getKeywordHelp(command);
        if (!Common.isEmpty(help)) {
            mCurrentCommand = command;
            mBrowser.setText(help);
            return true;
        }
        return false;
    }

    private void updateToolbarButtons()
    {
        if(mBack != null) {
            mBack.setEnabled(!Common.isEmptyCollection(mBackCommands));
        }
        if(mForward != null) {
            mForward.setEnabled(!Common.isEmptyCollection(mForwardCommands));
        }
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
                            NSISHTMLHelp.showHelp(f.toURI().toURL().toString());
                            return;
                        }
                    }
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }                                                
            }
            try {
                Common.openExternalBrowser(f.toURI().toURL().toString());
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }                
        }
    }

    /**
     * 
     */
    private void setParamEditorState()
    {
        if(mRemember) {
            if(mSettings == null) {
                mSettings = new HashMap();
            }
            mParamEditor.setSettings(mSettings);
        }
        else {
            mParamEditor.setSettings(null);
        }
    }
}
