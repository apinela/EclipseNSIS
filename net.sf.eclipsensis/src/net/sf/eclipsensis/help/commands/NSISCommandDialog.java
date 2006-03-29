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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class NSISCommandDialog extends StatusMessageDialog
{
    private static final String SETTING_COLLAPSE_HELP = "collapseHelp"; //$NON-NLS-1$
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
        }
    };
    private INSISParamEditor mParamEditor;
    private String mCommandText = ""; //$NON-NLS-1$
    private boolean mRemember;
    private ExpandableComposite mExpandableComposite;
    
    public NSISCommandDialog(Shell parent, NSISCommand command)
    {
        super(parent);
        mCommand = command;
        setTitle(EclipseNSISPlugin.getFormattedString("nsis.command.dialog.title.format",new String[] {mCommand.getName()})); //$NON-NLS-1$
        setShellImage(EclipseNSISPlugin.getShellImage());
        mRemember = NSISPreferences.INSTANCE.getBoolean(INSISPreferenceConstants.NSIS_COMMAND_HELPER_REMEMBER);
        if(mRemember) {
            Map map = (Map)cCommandStateMap.get(mCommand.getName());
            mSettings = (map==null?new HashMap():map);
        }
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

    protected void cancelPressed()
    {
        saveDialogSettings();
        super.cancelPressed();
    }

    private void saveDialogSettings()
    {
        if(mExpandableComposite != null) {
            mDialogSettings.put(SETTING_COLLAPSE_HELP, !mExpandableComposite.isExpanded());
        }
    }

    protected void okPressed()
    {
        saveDialogSettings();
        StringBuffer buf = new StringBuffer(mCommand.getName());
        mParamEditor.appendText(buf);
        mCommandText = buf.toString();
        NSISPreferences.INSTANCE.setValue(INSISPreferenceConstants.NSIS_COMMAND_HELPER_REMEMBER, mRemember);
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
        
        Composite c = new Composite(parent,SWT.NONE);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        layout = new GridLayout(2,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        final Button button = new Button(c,SWT.CHECK);
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

        final Button button2 = new Button(c,SWT.PUSH);
        button2.setText(EclipseNSISPlugin.getResourceString("reset.label")); //$NON-NLS-1$
        button2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mParamEditor.reset();
            }
        });
        setButtonLayoutData(button2);
        
        createHelpBrowser(parent);
        if(mBrowser != null) {
            setCurrentCommand(mCommand.getName());
        }
        
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

    private void createHelpBrowser(final Composite parent)
    {
        if(NSISBrowserUtility.isBrowserAvailable(parent)) {
            Group group = new Group(parent,SWT.NONE);
            GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            group.setLayoutData(gridData);
            GridLayout layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 2;
            group.setLayout(layout);

            int style = ExpandableComposite.FOCUS_TITLE | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR;
            if(!mCollapseHelp) {
                style |= ExpandableComposite.EXPANDED;
            }
            mExpandableComposite = new ExpandableComposite(group,SWT.NONE, style) {
                private FormColors mColors = new FormColors(parent.getDisplay());
                
                {
                    mColors.initializeSectionToolBarColors();
                }
                
                protected void onPaint(PaintEvent e) {
                    Color bg = mColors.getColor(FormColors.TB_BG);
                    Color gbg = mColors.getColor(FormColors.TB_GBG);
//                    Color fg = mColors.getColor(FormColors.TB_FG);
                    Color border = mColors.getColor(FormColors.TB_BORDER);
                    Rectangle bounds = getClientArea();
                    int theight = 0;
                    int tvmargin = GAP;
                    if ((getExpansionStyle() & TITLE_BAR) != 0) {
                        Point tsize = null;
                        Point tcsize = null;
                        if (toggle != null) {
                            tsize = toggle.getSize();
                        }
                        int twidth = bounds.width - marginWidth - marginWidth;
                        if (tsize != null) {
                            twidth -= tsize.x + GAP;
                        }
                        if (getTextClient() != null) {
                            tcsize = getTextClient().getSize();
                        }
                        if (tcsize != null) {
                            twidth -= tcsize.x + GAP;
                        }
                        Point size = textLabel.getSize();
                        if (tsize != null) {
                            theight += Math.max(theight, tsize.y);
                        }
                        if (tcsize != null) {
                            theight = Math.max(theight, tcsize.y);
                        }
                        theight = Math.max(theight, size.y);
                        theight += tvmargin + tvmargin;
                    } else {
                        theight = 5;
                    }
                    int midpoint = (theight * 66) / 100;
                    int rem = theight - midpoint;
                    GC gc = e.gc;
                    if ((getExpansionStyle() & TITLE_BAR) != 0) {
                        gc.setForeground(bg);
                        gc.setBackground(gbg);
                        gc.fillGradientRectangle(marginWidth, marginHeight, bounds.width
                                - 1 - marginWidth - marginWidth, midpoint - 1, true);
                        gc.setForeground(gbg);
                        gc.setBackground(getBackground());
                        gc
                                .fillGradientRectangle(marginWidth, marginHeight + midpoint
                                        - 1, bounds.width - 1 - marginWidth - marginWidth,
                                        rem - 1, true);
                    } else if (isExpanded()) {
                        gc.setForeground(bg);
                        gc.setBackground(getBackground());
                        gc.fillGradientRectangle(marginWidth, marginHeight, bounds.width
                                - marginWidth - marginWidth, theight, true);
                    }
                    gc.setBackground(getBackground());
                    // repair the upper left corner
                    gc.fillPolygon(new int[] { marginWidth, marginHeight, marginWidth,
                            marginHeight + 2, marginWidth + 2, marginHeight });
                    // repair the upper right corner
                    gc.fillPolygon(new int[] { bounds.width - marginWidth - 3,
                            marginHeight, bounds.width - marginWidth - 1, marginHeight,
                            bounds.width - marginWidth - 1, marginHeight + 2 });
                    gc.setForeground(border);
                    if (isExpanded() || (getExpansionStyle() & TITLE_BAR) != 0) {
                        // top left curve
                        gc.drawLine(marginWidth, marginHeight + 2, marginWidth + 2,
                                marginHeight);
                        // top edge
                        gc.drawLine(marginWidth + 2, marginHeight, bounds.width
                                - marginWidth - 3, marginHeight);
                        // top right curve
                        gc.drawLine(bounds.width - marginWidth - 3, marginHeight,
                                bounds.width - marginWidth - 1, marginHeight + 2);
                    } else {
                        // collapsed short title bar
                        // top edge
                        gc.drawLine(marginWidth, marginHeight, bounds.width - 1,
                                marginHeight);
                    }
                    if ((getExpansionStyle() & TITLE_BAR) != 0 && toggle != null
                            && !isExpanded()) {
                        // left vertical edge
                        gc.drawLine(marginWidth, marginHeight + 2, marginWidth,
                                marginHeight + theight - 1);
                        // right vertical edge
                        gc.drawLine(bounds.width - marginWidth - 1, marginHeight + 2,
                                bounds.width - marginWidth - 1, marginHeight + theight - 1);
                        // bottom edge (if closed)
                        gc.drawLine(marginWidth, marginHeight + theight - 1, bounds.width
                                - marginWidth - 1, marginHeight + theight - 1);
                    } else if (isExpanded()) {
                        // left vertical edge gradient
                        gc.fillGradientRectangle(marginWidth, marginHeight + 2, 1,
                                theight - 2, true);
                        // right vertical edge gradient
                        gc.fillGradientRectangle(bounds.width - marginWidth - 1,
                                marginHeight + 2, 1, theight - 2, true);
                    }
                }
            };
            mExpandableComposite.setText(EclipseNSISPlugin.getResourceString("nsis.command.description.label")); //$NON-NLS-1$
            makeBold(mExpandableComposite);
            gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            mExpandableComposite.setLayoutData(gridData);
            Composite composite = new Composite(mExpandableComposite,SWT.NONE);
            gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            composite.setLayoutData(gridData);
            layout = new GridLayout(1,false);
            layout.marginHeight = layout.marginWidth = 0;
            composite.setLayout(layout);
            createToolBar(composite);
            mBrowser= new Browser(composite, SWT.BORDER);
            gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
            initializeDialogUnits(mBrowser);
            gridData.widthHint = convertWidthInCharsToPixels(50);
            gridData.heightHint = convertHeightInCharsToPixels(10);
            mBrowser.setLayoutData(gridData);
            mBrowser.setMenu(new Menu(getShell(), SWT.NONE));
            mExpandableComposite.setClient(composite);
            mExpandableComposite.addExpansionListener(new ExpansionAdapter() {
                public void expansionStateChanged(ExpansionEvent e)
                {
                    Shell shell = mExpandableComposite.getShell();
                    Point size = shell.getSize();
                    shell.setSize(size.x, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                }
            });
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
