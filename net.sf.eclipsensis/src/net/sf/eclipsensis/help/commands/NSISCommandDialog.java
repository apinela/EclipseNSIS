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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISService;
import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.help.NSISHTMLHelp;
import net.sf.eclipsensis.help.NSISHelpURLProvider;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

public class NSISCommandDialog extends StatusMessageDialog
{
    private static Map cCommandStateMap;
    private static final Object JOB_FAMILY = new Object();
    
    private NSISCommand mCommand;

    private Browser mBrowser;
    private Control mControl;
    private String mCurrentCommand = null;
    private ToolItem mBack = null;
    private ToolItem mForward = null;
    private Stack mBackCommands = null;
    private Stack mForwardCommands = null;
    private Map mSettings = null;
    
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
            if(mControl != null && !mControl.isDisposed() && isChildOf(mControl,(Control)event.widget)) {
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
    };
    private INSISParamEditor mParamEditor;
    private String mCommandText = ""; //$NON-NLS-1$
    private boolean mRemember;
    
    public NSISCommandDialog(Shell parent, NSISCommand command)
    {
        super(parent);
        mCommand = command;
        setTitle(EclipseNSISPlugin.getFormattedString("nsis.command.dialog.title.format",new String[] {mCommand.getName()})); //$NON-NLS-1$
        setShellImage(EclipseNSISPlugin.getShellImage());
        mRemember = NSISPreferences.INSTANCE.getPreferenceStore().getBoolean(INSISPreferenceConstants.NSIS_COMMAND_HELPER_REMEMBER);
        if(mRemember) {
            Map map = (Map)cCommandStateMap.get(mCommand.getName());
            mSettings = (map==null?new HashMap():map);
        }
        mParamEditor = mCommand.createEditor();
        setParamEditorState();
    }
    

    public void create()
    {
        super.create();
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

    protected void okPressed()
    {
        StringBuffer buf = new StringBuffer(mCommand.getName());
        mParamEditor.appendText(buf);
        mCommandText = buf.toString();
        NSISPreferences.INSTANCE.getPreferenceStore().setValue(INSISPreferenceConstants.NSIS_COMMAND_HELPER_REMEMBER, mRemember);
        if(mRemember) {
            cCommandStateMap.put(mCommand.getName(), mSettings);
        }
        else {
            cCommandStateMap.clear();
        }
        super.okPressed();
    }

    protected Control createControl(Composite parent)
    {
        parent = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = layout.marginWidth = 0;
        parent.setLayout(layout);
        Label l = new Label(parent,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        l.setText(mCommand.getName());
        makeBold(l);
        mControl = mParamEditor.createControl(parent);
        mControl.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        
        createHelpBrowser(parent);
        if(mBrowser != null) {
            setCurrentCommand(mCommand.getName());
        }

        final Button button = new Button(parent,SWT.CHECK);
        button.setText(EclipseNSISPlugin.getResourceString("nsis.command.helper.remember.label")); //$NON-NLS-1$
        button.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        button.setSelection(mRemember);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mRemember = button.getSelection();
                setParamEditorState();
            }
        });
        
        getShell().getDisplay().addFilter(SWT.Modify, mFilter);
        getShell().getDisplay().addFilter(SWT.Selection, mFilter);
        return parent;
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

    private void createHelpBrowser(Composite parent)
    {
        if(NSISBrowserUtility.isBrowserAvailable(parent)) {
            Group group = new Group(parent,SWT.SHADOW_ETCHED_IN);
            GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            group.setLayoutData(gridData);
            GridLayout layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 2;
            group.setLayout(layout);
            createToolBar(group);
            mBrowser= new Browser(group, SWT.BORDER);
            gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            initializeDialogUnits(mBrowser);
            gridData.widthHint = convertWidthInCharsToPixels(50);
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
            Composite c = new Composite(displayArea, SWT.NONE);
            c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = layout.marginWidth = 0;
            c.setLayout(layout);
            Label l = new Label(c,SWT.NONE);
            l.setText(EclipseNSISPlugin.getResourceString("nsis.command.description.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
            makeBold(l);
            
            ToolBar toolBar =  new ToolBar(c, SWT.FLAT);
            toolBar.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

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
