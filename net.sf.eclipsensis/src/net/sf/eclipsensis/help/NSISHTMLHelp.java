/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class NSISHTMLHelp extends ViewPart implements INSISConstants
{
    private static final int DISPID_AMBIENT_DLCONTROL = -5512;
    private static final int DLCTL_DLIMAGES = 0x10;
    
    private static final int CSC_UPDATECOMMANDS = -1;
    private static final int CSC_NAVIGATEFORWARD = 1;
    private static final int CSC_NAVIGATEBACK = 2;

    // Constants for Browser ReadyState
    private static final int READYSTATE_UNINITIALIZED = 0;
    private static final int READYSTATE_LOADING = 1;
    private static final int READYSTATE_LOADED = 2;
    private static final int READYSTATE_INTERACTIVE = 3;
    private static final int READYSTATE_COMPLETE = 4;
    
    // Browser Control Events 
    private static final int STATUS_TEXT_CHANGE = 102; // Statusbar text changed.
    private static final int PROGRESS_CHANGE = 108; // Fired when download progress is updated.
    private static final int COMMAND_STATE_CHANGE = 105; // The enabled state of a command changed

    // Browser properties
    private static final int DISPID_READYSTATE = -525;
    
    private static String cFirstPage = null;

    private OleFrame mOleFrame = null;
    private OleControlSite mOleControlSite = null;
    private OleAutomation mOleAutomation = null;

    private Font mBrowserFont;

    private ProgressBar mProgressBar;
    private Label mStatusText;
    
    private ToolItem mBack;
    private ToolItem mForward;
    private ToolItem mHome;
    private ToolItem mStop;
    private ToolItem mRefresh;
    
    private boolean mActivated = false;
    
    private String mStartPage;
    
    public static boolean showHelp(final String url)
    {
        final boolean[] result = {false};
        if(NSISPreferences.getPreferences().isAutoShowConsole()) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run()
                {
                    try {
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        NSISHTMLHelp htmlHelp = (NSISHTMLHelp)activePage.findView(HTMLHELP_ID);
                        if(htmlHelp == null) {
                            cFirstPage = url;
                            htmlHelp = (NSISHTMLHelp)activePage.showView(HTMLHELP_ID);
                        }
                        else {
                            activePage.activate(htmlHelp);
                            htmlHelp.goUrl(url);
                        }
                        result[0] = htmlHelp.isActivated();
                    }
                    catch(PartInitException pie) {
                        result[0] = false;
                        pie.printStackTrace();
                    }
                }
            });
        }
        return result[0];
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);

        mBrowserFont = new Font (null, "MS Sans Serif", 8, SWT.NULL); //$NON-NLS-1$
        createToolBar(composite);
        createHelpBrowser(composite);
        createStatusArea(composite);
        openHelp();
        IPreferenceStore prefs = NSISPreferences.getPreferences().getPreferenceStore();
        prefs.addPropertyChangeListener(new IPropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent event)
            {
                if(event.getProperty().equals(INSISPreferenceConstants.NSIS_HOME)) {
                    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                        public void run()
                        {
                            openHelp();
                        }
                    });
                }
            }
        });
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, String text, String icon, boolean enabled, Listener listener)
    {
        ToolItem item = new ToolItem(bar, SWT.NONE);
        item.setToolTipText(EclipseNSISPlugin.getResourceString(tooltip));
//        item.setText(EclipseNSISPlugin.getResourceString(text));
        item.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString(icon)));
        item.setEnabled(enabled);
        item.addListener(SWT.Selection, listener);
        return item;
    }

    private void createToolBar(Composite displayArea)
    {
        ToolBar bar = new ToolBar(displayArea, SWT.FLAT);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        bar.setLayoutData(gridData);
        
        // Add a button to navigate backwards through previously visited pages
        mBack = createToolItem(bar,"help.browser.back.tooltip","help.browser.back.text", //$NON-NLS-1$ //$NON-NLS-2$
                               "help.browser.back.icon",false,  //$NON-NLS-1$
                               new Listener() {
                                   public void handleEvent(Event e) {
                                       if(mOleAutomation != null) {
                                           goBack();
                                       }
                                   }
                               });
    
        // Add a button to navigate forward through previously visited pages
        mForward = createToolItem(bar,"help.browser.forward.tooltip","help.browser.forward.text", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.forward.icon",false,  //$NON-NLS-1$
                new Listener() {
                    public void handleEvent(Event e) {
                        if (mOleAutomation != null) {
                            goForward();
                        }
                    }
                });
    
        // Add a separator
        new ToolItem(bar, SWT.SEPARATOR);
        
        // Add a button to refresh the current web page
        mRefresh = createToolItem(bar,"help.browser.refresh.tooltip","help.browser.refresh.text", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.refresh.icon",false,  //$NON-NLS-1$
                new Listener() {
                    public void handleEvent(Event e) {
                        if (mOleAutomation != null) {
                            refresh();
                        }
                    }
                });

        // Add a button to abort web page loading
        mStop = createToolItem(bar,"help.browser.stop.tooltip","help.browser.stop.text", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.stop.icon",false,  //$NON-NLS-1$
                new Listener() {
                    public void handleEvent(Event e) {
                        if (mOleAutomation != null) {
                            stop();
                        }
                    }
                });
        
        // Add a button to navigate to the Home page
        mHome = createToolItem(bar,"help.browser.home.tooltip","help.browser.home.text", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.home.icon",false,  //$NON-NLS-1$
                new Listener() {
                    public void handleEvent(Event e) {
                        if (mOleAutomation != null) {
                            goUrl(mStartPage);
                        }
                    }
                });
    }
    
    /**
     * @param composite
     */
    private void createHelpBrowser(Composite composite)
    {
        mOleFrame = new OleFrame(composite,SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gridData.horizontalSpan = 3;
        mOleFrame.setLayoutData(gridData);
        mOleFrame.setLayout(new FillLayout());

        try {
            // Create an Automation object for access to extended capabilities
            mOleControlSite = new OleControlSite(mOleFrame,SWT.NONE,"Shell.Explorer"); //$NON-NLS-1$
            Variant download = new Variant(DLCTL_DLIMAGES);
            mOleControlSite.setSiteProperty(DISPID_AMBIENT_DLCONTROL, download);
            mOleAutomation = new OleAutomation(mOleControlSite);
        } catch (SWTException ex) {
            // Creation may have failed because control is not installed on machine
            Label label = new Label(mOleFrame, SWT.BORDER);
            label.setText(EclipseNSISPlugin.getResourceString("help.browser.create.error")); //$NON-NLS-1$
            if(mOleAutomation != null) {
                mOleAutomation.dispose();
                mOleAutomation = null;
            }
            return;
        }

        // Respond to ProgressChange events by updating the Progress bar
        mOleControlSite.addEventListener(PROGRESS_CHANGE, new OleListener() {
            public void handleEvent(OleEvent event) 
            {
                Variant progress = event.arguments[0];
                Variant maxProgress = event.arguments[1];
                if (progress == null || maxProgress == null) {
                    return;
                }
                mProgressBar.setMaximum(maxProgress.getInt());
                mProgressBar.setSelection(progress.getInt());
            }
        });
        
        // Respond to StatusTextChange events by updating the Status Text label
        mOleControlSite.addEventListener(STATUS_TEXT_CHANGE, new OleListener() {
            public void handleEvent(OleEvent event) 
            {
                Variant statusText = event.arguments[0];
                if (mStatusText == null || statusText == null) {
                    return;
                }
                String text = statusText.getString();
                if (text != null) {
                    mStatusText.setText(text);
                }
            }
        });
        
        // Listen for changes to the ready state and print out the current state 
        mOleControlSite.addPropertyListener(DISPID_READYSTATE, new OleListener() {
            public void handleEvent(OleEvent event) 
            {
                if (event.detail == OLE.PROPERTY_CHANGING) {
                    return;
                }
                int state = getReadyState();
                switch (state) {
                    case READYSTATE_UNINITIALIZED:
                        if (mStatusText != null) {
                            mStatusText.setText(EclipseNSISPlugin.getResourceString("help.browser.state.uninitialized.text")); //$NON-NLS-1$
                        }
                        mBack.setEnabled(false);
                        mForward.setEnabled(false);
                        mHome.setEnabled(false);
                        mRefresh.setEnabled(false);
                        mStop.setEnabled(false);
                        break;
                    case READYSTATE_LOADING:
                        if (mStatusText != null) {
                            mStatusText.setText(EclipseNSISPlugin.getResourceString("help.browser.state.loading.text")); //$NON-NLS-1$
                        }
                        mHome.setEnabled(true);
                        mRefresh.setEnabled(true);
                        mStop.setEnabled(true);
                        break;
                    case READYSTATE_LOADED:
                        if (mStatusText != null) {
                            mStatusText.setText(EclipseNSISPlugin.getResourceString("help.browser.state.loaded.text")); //$NON-NLS-1$
                        }
                        mStop.setEnabled(true);
                        break;
                    case READYSTATE_INTERACTIVE:
                        if (mStatusText != null) {
                            mStatusText.setText(EclipseNSISPlugin.getResourceString("help.browser.state.interactive.text")); //$NON-NLS-1$
                        }
                        mStop.setEnabled(true);
                        break;
                    case READYSTATE_COMPLETE:
                        if (mStatusText != null) {
                            mStatusText.setText(EclipseNSISPlugin.getResourceString("help.browser.state.complete.text")); //$NON-NLS-1$
                        }
                        mStop.setEnabled(false);
                        break;
                }
            }
        });

        // Listen for changes to the active command states
        mOleControlSite.addEventListener(COMMAND_STATE_CHANGE, new OleListener() {
            public void handleEvent(OleEvent event) 
            {
                if (event.type != COMMAND_STATE_CHANGE) return;
                final int commandID = (event.arguments[0] != null) ? event.arguments[0].getInt() : 0;
                final boolean commandEnabled = (event.arguments[1] != null) ? event.arguments[1].getBoolean() : false;
                
                switch (commandID) {
                    case CSC_NAVIGATEBACK:
                        mBack.setEnabled(commandEnabled);
                        break;
                    case CSC_NAVIGATEFORWARD:
                        mForward.setEnabled(commandEnabled);
                        break;
                }
            }
        });

        // in place activate the ActiveX control        
        mActivated = (mOleControlSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE) == OLE.S_OK);
    }
    
    private void createStatusArea(Composite composite) 
    {
        // Add a progress bar to display downloading progress information
        mProgressBar = new ProgressBar(composite, SWT.BORDER);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.BEGINNING;
        gridData.verticalAlignment = GridData.FILL;
        mProgressBar.setLayoutData(gridData);        

        // Add a label for displaying status messages as they are received from the control
        mStatusText = new Label(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        gridData.horizontalSpan = 2;
        mStatusText.setLayoutData(gridData);
        mStatusText.setFont(mBrowserFont);
    }   

    public void dispose()
    {
        if(mBrowserFont != null) {
            mBrowserFont.dispose();
            mBrowserFont = null;
        }
        if(mOleControlSite != null) {
            mOleAutomation.dispose();
            mOleAutomation = null;
            mOleControlSite.dispose();
            mOleControlSite = null;
        }
        mOleFrame.dispose();
        mOleFrame = null;
    }

    private void openHelp()
    {
        if (mActivated) {
            mStartPage = NSISHelpURLProvider.getInstance().getCHMHelpStartPage();
            if(mStartPage == null) {
                mStartPage = "about:blank"; //$NON-NLS-1$
            }
            goUrl(cFirstPage == null?mStartPage:cFirstPage);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        mOleFrame.setFocus();
    }
    private boolean isActivated()
    {
        return mActivated;
    }

    private int[] getOleIDOfNames(Object name)
    {
        if(name instanceof String) {
            return mOleAutomation.getIDsOfNames(new String[]{(String)name});
        }
        else if(name instanceof String[]) {
            return mOleAutomation.getIDsOfNames((String[])name);
        }
        else {
            return null;
        }
    }
    
    private Variant getOleProperty(Object name)
    {
        return mOleAutomation.getProperty(getOleIDOfNames(name)[0]);
    }

    private void invokeOleFunction(Object name)
    {
        mOleAutomation.invoke(getOleIDOfNames(name)[0]);
    }

    /**
     * Returns the current web page title.
     * 
     * @return the current web page title String
     */
    public String getLocationName() 
    {
        // dispid=210, type=PROPGET, name="LocationName"
        Variant result = getOleProperty("LocationName"); //$NON-NLS-1$
        if (result == null || result.getType() != OLE.VT_BSTR) return null;
        return result.getString();
    }

    /**
     * Returns the current URL.
     * 
     * @return the current URL String
     */
    public String getLocationURL() 
    {
        // dispid=211, type=PROPGET, name="LocationURL"
        Variant result = getOleProperty("LocationURL"); //$NON-NLS-1$
        if (result == null || result.getType() != OLE.VT_BSTR) return null;
        return result.getString();
    }

    /**
     * Returns the current state of the control.
     * 
     * @return the current state of the control, one of:
     *         READYSTATE_UNINITIALIZED;
     *         READYSTATE_LOADING;
     *         READYSTATE_LOADED;
     *         READYSTATE_INTERACTIVE;
     *         READYSTATE_COMPLETE.
     */
    public int getReadyState() 
    {
        // dispid=4294966771, type=PROPGET, name="ReadyState"
        Variant result = getOleProperty("ReadyState"); //$NON-NLS-1$
        if (result == null || result.getType() != OLE.VT_I4) return -1;
        return result.getInt();
    }
    
    /**
     * Navigates backwards through previously visited web sites.
     */
    public void goBack() 
    {
        // dispid=100, type=METHOD, name="GoBack"
        invokeOleFunction("GoBack"); //$NON-NLS-1$
    }
        
    /**
     * Navigates backwards through previously visited web sites.
     */
    public void goForward() 
    {
        // dispid=101, type=METHOD, name="GoForward"
        invokeOleFunction("GoForward"); //$NON-NLS-1$
    }
    
    /**
     * Navigates to a particular URL.
     */
    public void goUrl(String url) 
    {
        // dispid=104, type=METHOD, name="Navigate"
        int[] rgdispid = getOleIDOfNames(new String[]{"Navigate", "URL"}); //$NON-NLS-1$ //$NON-NLS-2$
        
        Variant[] rgvarg = new Variant[1];
        rgvarg[0] = new Variant(url);
        int[] rgdispidNamedArgs = new int[1];
        rgdispidNamedArgs[0] = rgdispid[1]; // identifier of argument
        mOleAutomation.invoke(rgdispid[0], rgvarg, rgdispidNamedArgs);
    }
    
    /**
     * Refreshes the currently viewed page.
     *
     * @return the platform-defined result code for the "Refresh" method invocation
     */
    public void refresh()
    {
        // dispid= 4294966746, type=METHOD, name="Refresh"
        mOleAutomation.invokeNoReply(getOleIDOfNames("Refresh")[0]); //$NON-NLS-1$
    }
    
    /**
     * Aborts loading of the currnet page.
     *
     * @return the platform-defined result code for the "Stop" method invocation
     */
    public void stop() 
    {
        // dispid=106, type=METHOD, name="Stop"
        invokeOleFunction("Stop"); //$NON-NLS-1$
    }   
}
