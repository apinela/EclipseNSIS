/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.text.MessageFormat;
import java.util.Map;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.dialogs.NSISConfigWizardDialog;
import net.sf.eclipsensis.help.NSISHelpTOC.NSISHelpTOCNode;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.MapContentProvider;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class NSISHTMLHelp extends ViewPart implements INSISConstants
{
    public static final String ECLIPSENSIS_URI_SCHEME = "eclipsensis:"; //$NON-NLS-1$
    public static final String FILE_URI_SCHEME = "file:"; //$NON-NLS-1$
    private static String cFirstPage = null;
    private static final String IMAGE_LOCATION_FORMAT = EclipseNSISPlugin.getResourceString("help.browser.throbber.icon.format"); //$NON-NLS-1$
    private static final int IMAGE_COUNT = Integer.parseInt(EclipseNSISPlugin.getResourceString("help.browser.throbber.icon.count")); //$NON-NLS-1$

    private boolean mShowNav;
    private boolean mSynched;
    
    private Browser mBrowser;
    private ProgressBar mProgressBar;
    private Label mStatusText;
    private Canvas mThrobber;

    private Image[] mThrobberImages;
    private ToolItem mBackButton;
    private ToolItem mForwardButton;
    private ToolItem mHomeButton;
    private ToolItem mStopButton;
    private ToolItem mRefreshButton;
    private ToolItem mShowHideNavButton;
    private ToolItem mSynchedButton;

    private String mStartPage;
    private int mThrobberImageIndex;
    private boolean mBusy;
    private INSISHelpURLListener mHelpURLListener = new INSISHelpURLListener(){
        public void helpURLsChanged()
        {
            if(Display.getCurrent() != null) {
                init();
            }
            else {
                Display.getDefault().syncExec(new Runnable() {
                    public void run()
                    {
                        init();
                    }
                });
            }
        }
    };
    private TreeViewer mContentsViewer;
    private SashForm mSashForm;
    private ToolItem mSeparator;
    private ListViewer mIndexViewer;
    private TabFolder mNavigationPane;
    private ToolBar mToolBar;

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
                            cFirstPage = url;
                            htmlHelp.openHelp();
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
        mShowNav = NSISPreferences.INSTANCE.getBoolean(INSISPreferenceConstants.NSIS_HELP_VIEW_SHOW_NAV);
        mSynched = NSISPreferences.INSTANCE.getBoolean(INSISPreferenceConstants.NSIS_HELP_VIEW_SYNCHED);
        
        initResources();
        
        mSashForm = new SashForm(parent, SWT.HORIZONTAL|SWT.SMOOTH);
        createNavigationPane(mSashForm);
        Composite composite = new Composite(mSashForm,SWT.NONE);
        composite.setLayout(new FillLayout());
        mSashForm.setWeights(new int[] {1,4});
        try {
            mBrowser = new Browser(composite, SWT.BORDER);
        }
        catch (SWTError e) {
            mBrowser = null;
            if(mSashForm != null) {
                mSashForm.dispose();
            }
            parent.setLayout(new FillLayout());
            Label label = new Label(parent, SWT.CENTER | SWT.WRAP);
            label.setText(EclipseNSISPlugin.getResourceString("help.browser.create.error")); //$NON-NLS-1$
            parent.layout(true);
            return;
        }

        parent.setLayout(new FormLayout());
        createToolBar(parent);
        createThrobber(parent);
        createStatusArea(parent);
        Label l = new Label(parent,SWT.SEPARATOR|SWT.HORIZONTAL);
        FormData data = new FormData();
        data.left = new FormAttachment(0,0);
        data.right = new FormAttachment(100,0);
        data.top = new FormAttachment(mThrobber, 5, SWT.DEFAULT);
        l.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(l, 0, SWT.DEFAULT);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(mStatusText, -5, SWT.DEFAULT);
        mSashForm.setLayoutData(data);
        
        mBrowser.addLocationListener(new LocationListener() {
            public void changed(LocationEvent event)
            {
                mBusy = true;
                String location = event.location;
                synch(location);
            }

            public void changing(LocationEvent event)
            {
                if(!Common.isEmpty(event.location)) {
                    File f = null;
                    if(event.location.regionMatches(true,0,FILE_URI_SCHEME,0,FILE_URI_SCHEME.length())) {
                        try {
                            URI url = new URI(event.location);
                            if(url.getFragment() != null) {
                                int n = event.location.lastIndexOf('#');
                                if(n >= 0) {
                                    url = new URI(event.location.substring(0,n));
                                }
                            }
                            if(url != null) {
                                f = new File(url);
                            }
                        }
                        catch (URISyntaxException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                    else if(event.location.regionMatches(true,0,ECLIPSENSIS_URI_SCHEME,0,ECLIPSENSIS_URI_SCHEME.length())) {
                        String action = event.location.substring(ECLIPSENSIS_URI_SCHEME.length());
                        if(action.equals(NSISHelpProducer.CONFIGURE)) {
                            IJobStatusRunnable runnable = new IJobStatusRunnable() {
                                public IStatus run(IProgressMonitor monitor)
                                {
                                    new NSISConfigWizardDialog(getSite().getShell()).open();
                                    return Status.OK_STATUS;
                                }
                            };
                            EclipseNSISPlugin.getDefault().getJobScheduler().scheduleUIJob(NSISHTMLHelp.class, EclipseNSISPlugin.getResourceString("configure.nsis.job.name"), runnable); //$NON-NLS-1$
                            event.doit = false;
                        }
                    }
                    else {
                        f = new File(event.location);
                    }
                    File f2 = NSISHelpURLProvider.getInstance().translateCachedFile(f);
                    if(f2 != null && f2.exists()) {
                        event.doit = !HelpBrowserLocalFileHandler.INSTANCE.handle(f2);
                    }
                    if(event.doit) {
                        //File has been translated and not handled.
                        //Handle it manually
                        if(f2.isDirectory()) {
                            event.doit = false;
                            try {
                                Program.launch(f2.getCanonicalPath());
                            }
                            catch (IOException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                                mBrowser.setUrl(IOUtility.getFileURLString(f2));
                            }
                        }
                        else if(!Common.objectsAreEqual(f,f2)) {
                            event.doit = false;
                            mBrowser.setUrl(IOUtility.getFileURLString(f2));
                        }
                    }
                }
            }
        });
        
        init();
        NSISHelpURLProvider.getInstance().addListener(mHelpURLListener);
    }

    private void init()
    {
        Composite parent = mBrowser.getParent();
        NSISHelpTOC toc = NSISHelpURLProvider.getInstance().getCachedHelpTOC();
        if(toc == null) {
            mShowHideNavButton.dispose();
            mShowHideNavButton = null;
            mSynchedButton.dispose();
            mSynchedButton = null;
            mSeparator.dispose();
            mSeparator = null;
            if(mSashForm.getMaximizedControl() != parent) {
                mSashForm.setMaximizedControl(parent);
            }
            mContentsViewer.setInput(null);
            mIndexViewer.setInput(null);
        }
        else {
            if(mShowHideNavButton == null) {
                createNavToolItems();
            }
            if(mShowNav) {
                if(mSashForm.getMaximizedControl() != null) {
                    mSashForm.setMaximizedControl(null);
                }
            }
            else {
                if(mSashForm.getMaximizedControl() != parent) {
                    mSashForm.setMaximizedControl(parent);
                }
            }
            mContentsViewer.setInput(toc);
            Map index = NSISHelpURLProvider.getInstance().getCachedHelpIndex();
            TabItem indexTabItem = null;
            TabItem[] tabItems = mNavigationPane.getItems();
            for (int i = 0; i < tabItems.length; i++) {
                if(Common.objectsAreEqual(tabItems[i].getControl(),mIndexViewer.getControl())) {
                    indexTabItem = tabItems[i];
                    break;
                }
            }
            if(index == null) {
                if(indexTabItem != null) {
                    indexTabItem.dispose();
                }
                mIndexViewer.setInput(null);
            }
            else {
                if(indexTabItem == null) {
                    TabItem tabItem = new TabItem(mNavigationPane,SWT.NONE,1);
                    tabItem.setControl(mIndexViewer.getControl());
                }
                mIndexViewer.setInput(index);
            }
        }
        
        openHelp();
    }

    /**
     * @param toc
     * @param location
     */
    private void synch(String location)
    {
        if(mSynched && mContentsViewer != null) {
            try {
                new URL(location);
            }
            catch(MalformedURLException mue) {
                String suffix = "";
                int n = location.lastIndexOf('#');
                if(n > 0) {
                    suffix = location.substring(n);
                    location = location.substring(0,n);
                }
                File f = new File(location);
                location = IOUtility.getFileURLString(f)+suffix;

            }
            NSISHelpTOC toc = NSISHelpURLProvider.getInstance().getCachedHelpTOC();
            NSISHelpTOCNode node = toc.getNode(location);
            if(node == null) {
                int n = location.lastIndexOf('#');
                if(n >= 0) {
                    location = location.substring(0,n+1);
                    node = toc.getNode(location);
                    if(node == null) {
                        location = location.substring(0,n);
                        node = toc.getNode(location);
                    }
                }
                else {
                    node = toc.getNode(location+"#");
                }
            }
            if(node != null) {
                ISelection sel = mContentsViewer.getSelection();
                if(sel.isEmpty() || !Common.objectsAreEqual(node,((StructuredSelection)sel).getFirstElement())) {
                    mContentsViewer.setSelection(new StructuredSelection(node));
                }
            }
            else {
                mContentsViewer.setSelection(StructuredSelection.EMPTY);
            }
        }
    }

    public void dispose()
    {
        NSISHelpURLProvider.getInstance().removeListener(mHelpURLListener);
        super.dispose();
    }

    /**
     * Loads the resources
     */
    private void initResources()
    {
        if (mThrobberImages == null) {
            MessageFormat mf = new MessageFormat(IMAGE_LOCATION_FORMAT);
            mThrobberImages = new Image[IMAGE_COUNT];
            for (int i = 0; i < IMAGE_COUNT; ++i) {
                mThrobberImages[i] = EclipseNSISPlugin.getImageManager().getImage(mf.format(new Object[]{new Integer(i)}));
            }
        }
    }

    private void createNavigationPane(Composite parent)
    {
        mNavigationPane = new TabFolder(parent, SWT.NONE);
        TabItem item = new TabItem(mNavigationPane, SWT.NONE);
        item.setText("&Contents");
        Tree tree = new Tree(mNavigationPane,SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL);
        mContentsViewer = new TreeViewer(tree);
        final ITreeContentProvider contentProvider = new ITreeContentProvider() {
            public Object[] getChildren(Object parentElement)
            {
                if(parentElement instanceof NSISHelpTOC) {
                    return ((NSISHelpTOC)parentElement).getChildren().toArray();
                }
                else if(parentElement instanceof NSISHelpTOCNode) {
                    return ((NSISHelpTOCNode)parentElement).getChildren().toArray();
                }
                return null;
            }

            public Object getParent(Object element)
            {
                if(element instanceof NSISHelpTOCNode) {
                    return ((NSISHelpTOCNode)element).getParent();
                }
                return null;
            }

            public boolean hasChildren(Object element)
            {
                if(element instanceof NSISHelpTOC) {
                    return !Common.isEmptyCollection(((NSISHelpTOC)element).getChildren());
                }
                else if(element instanceof NSISHelpTOCNode) {
                    return !Common.isEmptyCollection(((NSISHelpTOCNode)element).getChildren());
                }
                return false;
            }

            public Object[] getElements(Object inputElement)
            {
                if(inputElement instanceof NSISHelpTOC) {
                    return getChildren(inputElement);
                }
                return null;
            }

            public void dispose()
            {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

        };
        mContentsViewer.setContentProvider(contentProvider);
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        final Image helpClosed = imageManager.getImage(EclipseNSISPlugin.getResourceString("help.closed.icon"));
        final Image helpOpen = imageManager.getImage(EclipseNSISPlugin.getResourceString("help.open.icon"));
        final Image helpPage = imageManager.getImage(EclipseNSISPlugin.getResourceString("help.page.icon"));
        mContentsViewer.setLabelProvider(new LabelProvider() {
            public Image getImage(Object element)
            {
                if(element instanceof NSISHelpTOCNode) {
                    NSISHelpTOCNode node = (NSISHelpTOCNode)element;
                    if(Common.isEmptyCollection(node.getChildren())) {
                        return helpPage;
                    }
                    else {
                        if(mContentsViewer.getExpandedState(element)) {
                            return helpOpen;
                        }
                        else {
                            return helpClosed;
                        }
                    }
                }
                return super.getImage(element);
            }

            public String getText(Object element)
            {
                if(element instanceof NSISHelpTOCNode) {
                    return ((NSISHelpTOCNode)element).getName();
                }
                return super.getText(element);
            }
        });
        mContentsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if(!event.getSelection().isEmpty()) {
                    Object element = ((IStructuredSelection)event.getSelection()).getFirstElement();
                    if(element instanceof NSISHelpTOCNode) {
                        mBrowser.setUrl(((NSISHelpTOCNode)element).getURL());
                    }
                }
            }
        });
        mContentsViewer.addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event)
            {
                updateLabels(mContentsViewer, event);
            }

            /**
             * @param treeViewer
             * @param event
             */
            private void updateLabels(final TreeViewer treeViewer, TreeExpansionEvent event)
            {
                final Object element = event.getElement();
                if(element instanceof NSISHelpTOCNode) {
                    treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            treeViewer.update(element,null);
                        }
                    });
                }
            }

            public void treeExpanded(TreeExpansionEvent event)
            {
                updateLabels(mContentsViewer, event);
            }
        });
        
        item.setControl(tree);

        item = new TabItem(mNavigationPane, SWT.NONE);
        item.setText("I&ndex");
        List list = new List(mNavigationPane, SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL);
        mIndexViewer = new ListViewer(list);
        mIndexViewer.setContentProvider(new MapContentProvider());
        mIndexViewer.setLabelProvider(new LabelProvider() {
            public String getText(Object element)
            {
                if(element instanceof Map.Entry) {
                    return String.valueOf(((Map.Entry)element).getKey());
                }
                return super.getText(element);
            }
        });
        mIndexViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                if(!sel.isEmpty()) {
                    Object element = sel.getFirstElement();
                    if(element instanceof Map.Entry) {
                        mBrowser.setUrl((String)((Map.Entry)element).getValue());
                    }
                }
            }
        });
        mIndexViewer.setComparator(new ViewerComparator());
        item.setControl(list);
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, Image icon)
    {
        return createToolItem(bar, tooltip, icon, bar.getItemCount());
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, Image icon, int index)
    {
        return createToolItem(bar, tooltip, icon, index, SWT.PUSH);
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, Image icon, int index, int style)
    {
        ToolItem item = new ToolItem(bar, style);
        item.setToolTipText(EclipseNSISPlugin.getResourceString(tooltip));
        item.setImage(icon);
        return item;
    }

    private void createToolBar(Composite parent)
    {
        mToolBar = new ToolBar(parent, SWT.FLAT);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 5);
        mToolBar.setLayoutData(data);

        createNavToolItems();
        
        // Add a button to navigate backwards through previously visited pages
        mBackButton = createToolItem(mToolBar,"help.browser.back.tooltip", //$NON-NLS-1$
                               CommonImages.BROWSER_BACK_ICON);

        // Add a button to navigate forward through previously visited pages
        mForwardButton = createToolItem(mToolBar,"help.browser.forward.tooltip", //$NON-NLS-1$
                               CommonImages.BROWSER_FORWARD_ICON);

        // Add a separator
        new ToolItem(mToolBar, SWT.SEPARATOR);

        // Add a button to abort web page loading
        mStopButton = createToolItem(mToolBar,"help.browser.stop.tooltip", //$NON-NLS-1$
                               CommonImages.BROWSER_STOP_ICON);

        // Add a button to refresh the current web page
        mRefreshButton = createToolItem(mToolBar,"help.browser.refresh.tooltip", //$NON-NLS-1$
                               CommonImages.BROWSER_REFRESH_ICON);

        // Add a button to navigate to the Home page
        mHomeButton = createToolItem(mToolBar,"help.browser.home.tooltip", //$NON-NLS-1$
                               CommonImages.BROWSER_HOME_ICON);

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                ToolItem item = (ToolItem)event.widget;
                if (item == mBackButton) {
                    mBrowser.back();
                }
                else if (item == mForwardButton) {
                    mBrowser.forward();
                }
                else if (item == mStopButton) {
                    mBrowser.stop();
                }
                else if (item == mRefreshButton) {
                    mBrowser.refresh();
                }
                else if (item == mHomeButton) {
                    cFirstPage = null;
                    openHelp();
                }
            }
        };
        mBackButton.addListener(SWT.Selection, listener);
        mForwardButton.addListener(SWT.Selection, listener);
        mStopButton.addListener(SWT.Selection, listener);
        mRefreshButton.addListener(SWT.Selection, listener);
        mHomeButton.addListener(SWT.Selection, listener);
    }

    /**
     * 
     */
    private void createNavToolItems()
    {
        // Add a button to show/hide navigation pane
        final String showNavToolTip = EclipseNSISPlugin.getResourceString("help.browser.shownav.tooltip");
        final String hideNavToolTip = EclipseNSISPlugin.getResourceString("help.browser.hidenav.tooltip");
        mShowHideNavButton = createToolItem(mToolBar,(mShowNav?hideNavToolTip:showNavToolTip),
                                       (mShowNav?CommonImages.BROWSER_HIDENAV_ICON:CommonImages.BROWSER_SHOWNAV_ICON), 0);

        // Add a button to sync browser with contents
        mSynchedButton = createToolItem(mToolBar,"help.browser.synced.tooltip", //$NON-NLS-1$
                                 CommonImages.BROWSER_SYNCED_ICON, 1, SWT.CHECK);
        mSynchedButton.setSelection(mSynched);
        mSynchedButton.setEnabled(mShowNav);

        // Add a separator
        mSeparator = new ToolItem(mToolBar, SWT.SEPARATOR, 2);
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                ToolItem item = (ToolItem)event.widget;
                if (item == mShowHideNavButton) {
                    if(mSashForm.getMaximizedControl() == null) {
                        mShowNav = false;
                        mSashForm.setMaximizedControl(mBrowser.getParent());
                        item.setImage(CommonImages.BROWSER_SHOWNAV_ICON);
                        item.setToolTipText(showNavToolTip);
                    }
                    else {
                        mShowNav = true;
                        mSashForm.setMaximizedControl(null);
                        item.setImage(CommonImages.BROWSER_HIDENAV_ICON);
                        item.setToolTipText(hideNavToolTip);
                    }
                    mSynchedButton.setEnabled(mShowNav);
                    NSISPreferences.INSTANCE.setValue(INSISPreferenceConstants.NSIS_HELP_VIEW_SHOW_NAV,mShowNav);
                }
                else if (item == mSynchedButton) {
                    mSynched = mSynchedButton.getSelection();
                    NSISPreferences.INSTANCE.setValue(INSISPreferenceConstants.NSIS_HELP_VIEW_SYNCHED,mSynched);
                    synch(mBrowser.getUrl());
                }
            }
        };
        mShowHideNavButton.addListener(SWT.Selection, listener);
        mSynchedButton.addListener(SWT.Selection, listener);
    }

    /**
     * @param displayArea
     */
    private void createThrobber(Composite displayArea)
    {
        FormData data;
        final Rectangle rect = mThrobberImages[0].getBounds();
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
                e.gc.drawImage(mThrobberImages[mThrobberImageIndex], 0, 0, rect.width, rect.height, 0, 0, pt.x, pt.y);
            }
        });
        mThrobber.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event e) {
                cFirstPage = null;
                openHelp();
            }
        });

        final Display display = displayArea.getDisplay();
        display.asyncExec(new Runnable() {

            public void run() {
                if (mThrobber.isDisposed()) {
                    return;
                }
                if (mBusy) {
                    mThrobberImageIndex ++;
                    if(mThrobberImageIndex == mThrobberImages.length) {
                        mThrobberImageIndex = 1;
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
                    mBusy = false;
                }
                else {
                    int ratio = event.current * 100 / event.total;
                    mProgressBar.setSelection(ratio);
                    mBusy = event.current != event.total;
                }
                if (!mBusy) {
                    mThrobberImageIndex = 0;
                    mThrobber.redraw();
                }
            }

            public void completed(ProgressEvent event)
            {
                mProgressBar.setSelection(0);
                mBusy = false;
                mThrobberImageIndex = 0;
                mBackButton.setEnabled(mBrowser.isBackEnabled());
                mForwardButton.setEnabled(mBrowser.isForwardEnabled());
                mThrobber.redraw();
            }
        });
    }

    private void openHelp()
    {
        if (isActivated()) {
            if (!EclipseNSISPlugin.getDefault().isConfigured()) {
                mBrowser.setText(EclipseNSISPlugin.getFormattedString("unconfigured.browser.help.format", //$NON-NLS-1$
                        new String[] {NSISHelpProducer.STYLE,ECLIPSENSIS_URI_SCHEME,
                                      NSISHelpProducer.CONFIGURE}));
            }
            else {
                mStartPage = NSISHelpURLProvider.getInstance().getCachedHelpStartPage();
                if (mStartPage == null) {
                    mStartPage = "about:blank"; //$NON-NLS-1$
                }
                mBrowser.setUrl(cFirstPage == null?mStartPage:cFirstPage);
                cFirstPage = null;
            }
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
