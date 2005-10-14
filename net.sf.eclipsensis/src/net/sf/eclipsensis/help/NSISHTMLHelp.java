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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class NSISHTMLHelp extends ViewPart implements INSISConstants
{
    private static final String FILE_URI_SCHEME = "file:///";
    private static String cFirstPage = null;
    private static final String IMAGE_LOCATION_FORMAT = EclipseNSISPlugin.getResourceString("help.browser.throbber.icon.format"); //$NON-NLS-1$
    private static final int IMAGE_COUNT = Integer.parseInt(EclipseNSISPlugin.getResourceString("help.browser.throbber.icon.count")); //$NON-NLS-1$

    private Browser mBrowser;
    private ToolBar mToolBar;
    private ProgressBar mProgressBar;
    private Label mStatusText;
    private Canvas mThrobber;

    private Image[] mImages;
    private ToolItem mBack;
    private ToolItem mForward;
    private ToolItem mHome;
    private ToolItem mStop;
    private ToolItem mRefresh;
    
    private String mStartPage;
    private int mIndex;
    private boolean mBusy;
    private INSISHomeListener mNSISHomeListener = new INSISHomeListener(){
        public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
        {
            if(monitor != null) {
                monitor.subTask(EclipseNSISPlugin.getResourceString("refreshing.browser.message")); //$NON-NLS-1$
            }
            openHelp();
        }
    };
    
    public static boolean showHelp(final String url)
    {
        final boolean[] result = {false};
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    NSISHTMLHelp htmlHelp = (NSISHTMLHelp)activePage.findView(HTMLHELP_ID);
                    if(htmlHelp == null) {
                        cFirstPage = url;
                        htmlHelp = (NSISHTMLHelp)activePage.showView(HTMLHELP_ID);
                        result[0] = htmlHelp.isActivated();
                    }
                    else {
                        activePage.activate(htmlHelp);
                        result[0] = htmlHelp.isActivated();
                        if(result[0]) {
                            htmlHelp.getBrowser().setUrl(url);
                        }
                    }
                }
                catch(PartInitException pie) {
                    result[0] = false;
                    EclipseNSISPlugin.getDefault().log(pie);
                }
            }
        });
        return result[0];
    }
    
    private Browser getBrowser()
    {
        return mBrowser;
    }
    
    private boolean isActivated()
    {
        return getBrowser() != null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        initResources();
        try {
            mBrowser = new Browser(parent, SWT.BORDER);
        }
        catch (SWTError e) {
            mBrowser = null;
            parent.setLayout(new FillLayout());
            Label label = new Label(parent, SWT.CENTER | SWT.WRAP);
            label.setText(EclipseNSISPlugin.getResourceString("help.browser.create.error")); //$NON-NLS-1$
            parent.layout(true);
            return;
        }
        parent.setLayout(new FormLayout());
        createToolBar(parent);
        createStatusArea(parent);
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(mThrobber, 5, SWT.DEFAULT);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(mStatusText, -5, SWT.DEFAULT);
        mBrowser.setLayoutData(data);
        mBrowser.addLocationListener(new LocationListener() {
            public void changed(LocationEvent event) 
            {
                mBusy = true;
            }
            
            public void changing(LocationEvent event) 
            {
                if(!Common.isEmpty(event.location)) {
                    File f = null;
                    if(event.location.regionMatches(true,0,FILE_URI_SCHEME,0,FILE_URI_SCHEME.length())) {
                        try {
                            URI url = new URI(event.location);
                            f = new File(url);
                        }
                        catch (URISyntaxException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                    else {
                        f = new File(event.location);
                    }
                    if(f != null && f.isFile()) {
                        event.doit = !HelpBrowserLocalFileHandlerManager.INSTANCE.handleFile(f);
                    }
                }
            }
        });

        openHelp();
        NSISPreferences.INSTANCE.addListener(mNSISHomeListener);
    }
    
    public void dispose()
    {
        NSISPreferences.INSTANCE.removeListener(mNSISHomeListener);
        super.dispose();
    }

    /**
     * Loads the resources
     */
    private void initResources() 
    {
        if (mImages == null) {
            MessageFormat mf = new MessageFormat(IMAGE_LOCATION_FORMAT);
            mImages = new Image[IMAGE_COUNT];
            for (int i = 0; i < IMAGE_COUNT; ++i) {
                mImages[i] = EclipseNSISPlugin.getImageManager().getImage(mf.format(new Object[]{new Integer(i)}));
            }
        }
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, String icon)
    {
        ToolItem item = new ToolItem(bar, SWT.NONE);
        item.setToolTipText(EclipseNSISPlugin.getResourceString(tooltip));
        item.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString(icon)));
        return item;
    }

    private void createToolBar(Composite displayArea)
    {
        mToolBar = new ToolBar(displayArea, SWT.FLAT);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 5);
        mToolBar.setLayoutData(data);
        
        // Add a button to navigate backwards through previously visited pages
        mBack = createToolItem(mToolBar,"help.browser.back.tooltip", //$NON-NLS-1$ //$NON-NLS-2$
                               "help.browser.back.icon"); //$NON-NLS-1$
    
        // Add a button to navigate forward through previously visited pages
        mForward = createToolItem(mToolBar,"help.browser.forward.tooltip", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.forward.icon"); //$NON-NLS-1$
    
        // Add a separator
        new ToolItem(mToolBar, SWT.SEPARATOR);
        
        // Add a button to refresh the current web page
        mRefresh = createToolItem(mToolBar,"help.browser.refresh.tooltip", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.refresh.icon"); //$NON-NLS-1$

        // Add a button to abort web page loading
        mStop = createToolItem(mToolBar,"help.browser.stop.tooltip", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.stop.icon"); //$NON-NLS-1$
        
        // Add a button to navigate to the Home page
        mHome = createToolItem(mToolBar,"help.browser.home.tooltip", //$NON-NLS-1$ //$NON-NLS-2$
                "help.browser.home.icon"); //$NON-NLS-1$

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                ToolItem item = (ToolItem)event.widget;
                if (item == mBack) {
                    mBrowser.back(); 
                }
                else if (item == mForward) {
                    mBrowser.forward();
                }
                else if (item == mStop) {
                    mBrowser.stop();
                }
                else if (item == mRefresh) {
                    mBrowser.refresh();
                }
                else if (item == mHome) {
                    mBrowser.setUrl(mStartPage);
                }
            }
        };
        mBack.addListener(SWT.Selection, listener);
        mForward.addListener(SWT.Selection, listener);
        mStop.addListener(SWT.Selection, listener);
        mRefresh.addListener(SWT.Selection, listener);
        mHome.addListener(SWT.Selection, listener);

        final Rectangle rect = mImages[0].getBounds();
        mThrobber = new Canvas(displayArea, SWT.NONE);
        data = new FormData();
        data.width = rect.width;
        data.height = rect.height;
        data.top = new FormAttachment(0, 5);
        data.right = new FormAttachment(100, -5);
        mThrobber.setLayoutData(data);
        
        mThrobber.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e) {
                Point pt = ((Canvas)e.widget).getSize();
                e.gc.drawImage(mImages[mIndex], 0, 0, rect.width, rect.height, 0, 0, pt.x, pt.y);         
            }
        });
        mThrobber.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event e) {
                mBrowser.setUrl(mStartPage);
            }
        });
        
        final Display display = displayArea.getDisplay();
        display.asyncExec(new Runnable() {
            
            public void run() {
                if (mThrobber.isDisposed()) {
                    return;
                }
                if (mBusy) {
                    mIndex ++;
                    if(mIndex == mImages.length) {
                        mIndex = 1;
                    }
                    mThrobber.redraw();
                }
                display.timerExec(100, this);
            }
        });
    }
    
    private void createStatusArea(Composite composite) 
    {
        // Add a label for displaying status messages as they are received from the control
        mStatusText = new Label(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        // Add a progress bar to display downloading progress information
        mProgressBar = new ProgressBar(composite, SWT.BORDER);

        FormData data = new FormData();
        data.left = new FormAttachment(0, 5);
        data.right = new FormAttachment(mProgressBar, 0, SWT.DEFAULT);
        data.bottom = new FormAttachment(100, -5);
        mStatusText.setLayoutData(data);
        
        data = new FormData();
        data.right = new FormAttachment(100, -5);
        data.bottom = new FormAttachment(100, -5);
        mProgressBar.setLayoutData(data);
        
        mBrowser.addStatusTextListener(new StatusTextListener() {
            public void changed(StatusTextEvent event) {
                mStatusText.setText(event.text); 
            }
        });

        mBrowser.addProgressListener(new ProgressListener() {
            public void changed(ProgressEvent event) 
            {
                if (event.total == 0) {
                    return;                            
                }
                int ratio = event.current * 100 / event.total;
                mProgressBar.setSelection(ratio);
                mBusy = event.current != event.total;
                if (!mBusy) {
                    mIndex = 0;
                    mThrobber.redraw();
                }
            }
            
            public void completed(ProgressEvent event) 
            {
                mProgressBar.setSelection(0);
                mBusy = false;
                mIndex = 0;
                mBack.setEnabled(mBrowser.isBackEnabled());
                mForward.setEnabled(mBrowser.isForwardEnabled());
                mThrobber.redraw();
            }
        });
    }   

    private void openHelp()
    {
        if (isActivated()) {
            mStartPage = NSISHelpURLProvider.INSTANCE.getCHMHelpStartPage();
            if(mStartPage == null) {
                mStartPage = "about:blank"; //$NON-NLS-1$
            }
            mBrowser.setUrl(cFirstPage == null?mStartPage:cFirstPage);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        if(isActivated()) {
            mBrowser.setFocus();
        }
    }
}
