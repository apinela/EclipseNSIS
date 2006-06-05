/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.jobs;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

class NSISDownloadUpdateJob extends NSISHttpUpdateJob
{
    private static Map cImageCache = new HashMap();
    private static final int DOWNLOAD_BUFFER_SIZE = 32768;
    protected static final File DOWNLOAD_FOLDER = EclipseNSISUpdatePlugin.getPluginStateLocation();
    protected static final File IMAGE_CACHE_FOLDER = new File(EclipseNSISUpdatePlugin.getPluginStateLocation(),"imageCache"); //$NON-NLS-1$
    protected static final MessageFormat INSTALL_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.update.prompt")); //$NON-NLS-1$

    private String mVersion;
    
    NSISDownloadUpdateJob(String version, NSISUpdateJobSettings settings, INSISUpdateJobRunner jobRunner)
    {
        super(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.message")).format(new String[]{version}), settings, jobRunner); //$NON-NLS-1$
        mVersion = version;
    }
    
    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated() && ((getSettings().getAction() & SchedulerConstants.UPDATE_INSTALL) == 0);
    }

    protected URL getURL() throws IOException
    {
        String site = cPreferenceStore.getString(IUpdatePreferenceConstants.SOURCEFORGE_MIRROR);
        if(!Common.isEmpty(site)) {
            return NSISUpdateURLs.getDownloadURL(site, mVersion);
        }
        return null;
    }

    protected URL getDefaultURL() throws IOException
    {
        return NSISUpdateURLs.getDownloadURL(mVersion);
    }

    protected HttpURLConnection makeConnection(IProgressMonitor monitor, URL url, URL defaultURL) throws IOException
    {
        try {
            monitor.beginTask(getName(),100);
            return superMakeConnection(new NestedProgressMonitor(monitor,getName(),25), url, defaultURL);
        }
        catch (IOException ex) {
            final List downloadSites = getDownloadSites(new NestedProgressMonitor(monitor,getName(),25), url, defaultURL);
            while(!Common.isEmptyCollection(downloadSites)) {
                DownloadSite site;
                if(getSettings().isAutomated()) {
                    site = (DownloadSite)downloadSites.remove(0);
                }
                else {
                    final DownloadSite[] selectedSite = {null};
                    final int[] rv = new int[1];
                    displayExec(new Runnable() {
                        public void run()
                        {
                            DownloadSiteSelectionDialog dialog = new DownloadSiteSelectionDialog(Display.getDefault().getActiveShell(),downloadSites);
                            rv[0] = dialog.open();
                            if(rv[0] == Window.OK) {
                                selectedSite[0] = dialog.getSelectedSite();
                            }
                        }
                    });
                    if(rv[0] == Window.CANCEL || selectedSite[0] == null) {
                        monitor.setCanceled(true);
                        return null;
                    }
                    site = selectedSite[0];
                    downloadSites.remove(site);
                }
                monitor.worked(25);
                try {
                    return superMakeConnection(new NestedProgressMonitor(monitor,getName(),25), site.getURL(), null);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            throw ex;
        }
        finally {
            monitor.done();
        }
    }

    private List getDownloadSites(IProgressMonitor monitor, URL url, URL defaultURL)
    {
        try {
            String taskName = EclipseNSISUpdatePlugin.getFormattedString("download.update.retrieve.alternate.message.format",new String[] {getName()});
            monitor.beginTask(taskName, 100); //$NON-NLS-1$
            List downloadSites = new ArrayList();
            HttpURLConnection conn2 = null;
            String content = null;
            try {
                conn2 = superMakeConnection(new NestedProgressMonitor(monitor,taskName,25), NSISUpdateURLs.getSelectDownloadURL(mVersion), null);
                content = getContent(conn2);
                monitor.worked(25);
            }
            catch(IOException ioe) {
                return null;
            }
            finally {
                if (conn2 != null) {
                    conn2.disconnect();
                }
            }
            if(content != null) {
                ParserDelegator parserDelegator = new ParserDelegator();
                DownloadURLsParserCallback callback = new DownloadURLsParserCallback(mVersion);
                try {
                    parserDelegator.parse(new StringReader(content), callback, true);
                }
                catch (IOException e1) {
                    return null;
                }
                List sites = callback.getSites();
                int count=0;
                for (Iterator iter = sites.iterator(); iter.hasNext();) {
                    String[] element = (String[])iter.next();
                    if(element != null && element.length == 4) {
                        try {
                            URL url2 = NSISUpdateURLs.getGenericDownloadURL(element[3], mVersion);
                            if(url2.equals(url) || url2.equals(defaultURL)) {
                                continue;
                            }
                            URL imageURL = new URL(element[0]);
                            String path = imageURL.getPath();
                            int n = path.lastIndexOf("/"); //$NON-NLS-1$
                            if(n >= 0) {
                                path = path.substring(n+1);
                            }
                            if(!Common.isEmpty(path)) {
                                File imageFile = new File(IMAGE_CACHE_FOLDER,path);
                                Image image = (Image)cImageCache.get(imageFile);
                                if(image == null) {
                                    if(!IOUtility.isValidFile(imageFile)) {
                                        if(!IOUtility.isValidDirectory(IMAGE_CACHE_FOLDER)) {
                                            IMAGE_CACHE_FOLDER.mkdirs();
                                        }
                                        FileOutputStream fos = null;
                                        conn2 = null;
                                        try {
                                            conn2 = superMakeConnection(null, imageURL, null);
                                            fos = new FileOutputStream(imageFile);
                                            download(conn2, null, null, fos);
                                        }
                                        finally {
                                            IOUtility.closeIO(fos);
                                            if(conn2 != null) {
                                                conn2.disconnect();
                                            }
                                        }
                                    }
                                    if(IOUtility.isValidFile(imageFile)) {
                                        final ImageData imageData = new ImageData(imageFile.getAbsolutePath());
                                        final Image[] imageArray = new Image[1];
                                        Display.getDefault().syncExec(new Runnable() {
                                            public void run()
                                            {
                                                try {
                                                    imageArray[0] = new Image(Display.getDefault(), imageData);
                                                }
                                                catch(Exception ex) {
                                                    imageArray[0] = null;
                                                }
                                            }
                                        });
                                        image = imageArray[0];
                                        if(image != null) {
                                            cImageCache.put(imageFile, image);
                                        }
                                    }
                                }
                                if(image != null) {
                                    downloadSites.add(new DownloadSite(image,element[1],element[2],url2));
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    monitor.worked(50*(++count/sites.size()));
                }
            }

            return downloadSites;
        }
        finally {
            monitor.done();
        }
    }
    /**
     * @param conn
     * @throws IOException
     */
    private String getContent(HttpURLConnection conn) throws IOException
    {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(DOWNLOAD_BUFFER_SIZE);
            download(conn, null, null, os);
        }
        catch(Exception e) {
            IOException ioe;
            if(e instanceof IOException) {
                ioe = (IOException)e;
            }
            else {
                ioe = (IOException)new IOException(e.getMessage()).initCause(e);
            }
            throw ioe;
        }
        finally {
            IOUtility.closeIO(os);
        }

        String content;
        try {
            content = os.toString(conn.getContentEncoding());
        }
        catch(Exception ex) {
            content = os.toString();
        }
        return content;
    }

    /**
     * @param url
     * @param defaultURL
     * @return
     * @throws IOException
     */
    private HttpURLConnection superMakeConnection(IProgressMonitor monitor, URL url, URL defaultURL) throws IOException
    {
        return super.makeConnection(monitor, url, defaultURL);
    }

    protected IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException
    {
        try {
            monitor.beginTask(getName(),100);
            URL url = conn.getURL();
            String fileName = url.getPath();
            int index = fileName.lastIndexOf('/');
            if(index >= 0) {
                fileName = fileName.substring(index+1);
            }
    
            File setupExe = new File(DOWNLOAD_FOLDER,fileName);
            if(!setupExe.exists()) {
                if(IOUtility.isValidFile(DOWNLOAD_FOLDER)) {
                    DOWNLOAD_FOLDER.delete();
                }
                if(!IOUtility.isValidDirectory(DOWNLOAD_FOLDER)) {
                    DOWNLOAD_FOLDER.mkdirs();
                }
                
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(setupExe);
                    IStatus status = download(conn, new NestedProgressMonitor(monitor, getName(), 50), getName(), os);
                    if(!status.isOK()) {
                        return status;
                    }
                }
                catch(Exception e) {
                    if(setupExe.exists()) {
                        IOUtility.closeIO(os);
                        os = null;
                        setupExe.delete();
                        IOException ioe;
                        if(e instanceof IOException) {
                            ioe = (IOException)e;
                        }
                        else {
                            ioe = (IOException)new IOException(e.getMessage()).initCause(e);
                        }
                        throw ioe;
                    }
                }
                finally {
                    IOUtility.closeIO(os);
                    if (monitor.isCanceled()) {
                        if(setupExe.exists()) {
                            setupExe.delete();
                        }
                        return Status.CANCEL_STATUS;
                    }
                }
            }
            else {
                try {
                    //This is a hack, otherwise the messagedialog sometimes closes immediately
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                }
                finally {
                    monitor.worked(50);
                }
            }
            
            if(setupExe.exists()) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                IStatus status = handleInstall(setupExe);
                if(!status.isOK()) {
                    return status;
                }
            }
            monitor.worked(50);
            return Status.OK_STATUS;
        }
        finally {
            monitor.done();
        }
    }

    protected IStatus download(HttpURLConnection conn, IProgressMonitor monitor, String name, OutputStream os) throws IOException
    {
        int length = 0;
        String[] args = null;

        MessageFormat mf = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.progress.format")); //$NON-NLS-1$
        if(monitor != null) {
            length = conn.getContentLength();
            if(length <= 0) {
                monitor.beginTask(name, IProgressMonitor.UNKNOWN);
            }
            else {
                args = new String[] {name,"0"}; //$NON-NLS-1$
                monitor.beginTask(mf.format(args), 101);
            }
            monitor.worked(1);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
        }
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(conn.getInputStream());

            ByteBuffer buf = ByteBuffer.allocateDirect(DOWNLOAD_BUFFER_SIZE);
            ReadableByteChannel channel = Channels.newChannel(is);
            WritableByteChannel fileChannel = Channels.newChannel(os);
            int worked = 0;
            int totalread = 0;
            int numread = channel.read(buf);
            while(numread >= 0) {
                if (monitor != null && monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                totalread += numread;
                if(buf.position() >= buf.limit()) {
                    buf.flip();
                    fileChannel.write(buf);
                    
                    if(monitor != null && length > 0) {
                        int newWorked = Math.round(totalread*100/length);
                        args[1]=Integer.toString(newWorked);
                        monitor.setTaskName(mf.format(args));
                        monitor.worked(newWorked-worked);
                        worked = newWorked;
                    }

                    buf.rewind();
                }
                numread = channel.read(buf);
            }
            if(buf.position() > 0) {
                buf.flip();
                fileChannel.write(buf);
            }
            if(monitor != null && length > 0) {
                args[1]="100"; //$NON-NLS-1$
                monitor.setTaskName(mf.format(args));
                monitor.worked(100-worked);
            }
            fileChannel.close();
            channel.close();
        }
        finally {
            IOUtility.closeIO(is);
            IOUtility.closeIO(os);
            if (monitor != null) {
                if(monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
            }
        }

        return Status.OK_STATUS;
    }

    protected IStatus handleInstall(final File setupExe)
    {
        if(setupExe.exists()) {
            displayExec(new Runnable() {
                public void run()
                {
                    NSISUpdateJobSettings settings = getSettings();
                    boolean automated = settings.isAutomated();
                    boolean install = ((settings.getAction() & SchedulerConstants.UPDATE_INSTALL) == SchedulerConstants.UPDATE_INSTALL);
                    if(!install) {
                        automated = false;
                        install = Common.openQuestion(Display.getCurrent().getActiveShell(), 
                                    INSTALL_UPDATE_MESSAGEFORMAT.format(new String[] {mVersion}), 
                                    EclipseNSISUpdatePlugin.getShellImage());
                    }
                    if(install) {
                        settings = new NSISUpdateJobSettings(automated, settings.getAction());
                        INSISUpdateJobRunner jobRunner = getJobRunner();
                        NSISUpdateJob job = new NSISInstallUpdateJob(mVersion, setupExe, settings);
                        if(jobRunner == null) {
                            job.schedule();
                        }
                        else {
                            jobRunner.run(job);
                        }
                    }
                }
            });
        }
        return Status.OK_STATUS;
    }

    protected String formatException(Throwable e)
    {
        return new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.error")).format(new String[] {mVersion,e.getMessage()}); //$NON-NLS-1$
    }
    
    private class DownloadSite
    {
        private Image mImage;
        private String mLocation;
        private String mContinent;
        private URL mURL;

        public DownloadSite(Image image, String location, String continent, URL url)
        {
            mImage = image;
            mLocation = location;
            mContinent = continent;
            mURL = url;
        }

        public String getContinent()
        {
            return mContinent;
        }

        public Image getImage()
        {
            return mImage;
        }

        public String getLocation()
        {
            return mLocation;
        }

        public URL getURL()
        {
            return mURL;
        }
    }
    
    private static final String SAVE_PREFERRED = "savePreferred"; //$NON-NLS-1$

    private class DownloadSiteSelectionDialog extends Dialog
    {
        private List mDownloadSites;
        private DownloadSite mSelectedSite = null;
        private Button mSavePreferred = null;
        private IDialogSettings mDialogSettings;
        
        protected DownloadSiteSelectionDialog(Shell parentShell, List downloadSites)
        {
            super(parentShell);
            
            mDownloadSites = downloadSites;
            initDialogSettings();
        }

        private void initDialogSettings()
        {
            IDialogSettings pluginDialogSettings = EclipseNSISUpdatePlugin.getDefault().getDialogSettings();
            String name = getClass().getName();
            mDialogSettings = pluginDialogSettings.getSection(name);
            if(mDialogSettings == null) {
                mDialogSettings = pluginDialogSettings.addNewSection(name);
            }
        }

        protected void configureShell(Shell shell)
        {
            super.configureShell(shell);
            shell.setText(EclipseNSISUpdatePlugin.getResourceString("download.sites.dialog.title")); //$NON-NLS-1$
            shell.setImage(EclipseNSISUpdatePlugin.getShellImage());
        }
        
        private void makeLabel(Composite parent, Image image, String text, Color bgColor, MouseListener listener, MouseTrackListener listener2)
        {
            parent = new Composite(parent,SWT.NONE);
            parent.setBackground(bgColor);
            parent.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
            parent.addMouseListener(listener);
            if(listener2 != null) {
                parent.addMouseTrackListener(listener2);
            }
            GridLayout layout = new GridLayout(1,false);
            layout.marginWidth = layout.marginHeight = 0;
            parent.setLayout(layout);
            
            Label l = new Label(parent,SWT.NONE);
            l.setBackground(bgColor);
            if(image != null) {
                l.setImage(image);
            }
            if(text != null) {
                l.setText(text);
            }
            l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
            l.addMouseListener(listener);
            if(listener2 != null) {
                l.addMouseTrackListener(listener2);
            }
        }

        protected void okPressed()
        {
            mDialogSettings.put(SAVE_PREFERRED, mSavePreferred.getSelection());
            if(mSavePreferred.getSelection()) {
                cPreferenceStore.setValue(IUpdatePreferenceConstants.SOURCEFORGE_MIRROR, mSelectedSite.getURL().getHost());
            }
            super.okPressed();
        }

        protected Control createDialogArea(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);
            Label l = new Label(composite, SWT.NONE);
            l.setText(EclipseNSISUpdatePlugin.getResourceString("download.sites.dialog.header")); //$NON-NLS-1$
            l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            ScrolledComposite scrolledComposite = new ScrolledComposite(composite,SWT.BORDER|SWT.V_SCROLL);
            final Color white = scrolledComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE);
            scrolledComposite.setBackground(white);
            
            Composite composite2 = new Composite(scrolledComposite, SWT.NONE);
            scrolledComposite.setContent(composite2);
            composite2.setBackground(white);
            GridLayout layout = new GridLayout(4,false);
            composite2.setLayout(layout);
            SelectionAdapter selectionAdapter = new SelectionAdapter() {
                public void widgetDefaultSelected(SelectionEvent e)
                {
                    widgetSelected(e);
                    okPressed();
                }

                public void widgetSelected(SelectionEvent e)
                {
                    Button button = (Button)e.widget;
                    if(button.getSelection()) {
                        mSelectedSite = (DownloadSite)button.getData();
                    }
                }
            };
            for (ListIterator iter = mDownloadSites.listIterator(); iter.hasNext();) {
                DownloadSite site = (DownloadSite)iter.next();
                final Button button = new Button(composite2,SWT.RADIO);
                button.setBackground(white);
                button.setSelection(!iter.hasPrevious());
                button.setData(site);
                button.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
                button.addSelectionListener(selectionAdapter);

                MouseAdapter mouseAdapter = new MouseAdapter() {
                    public void mouseUp(MouseEvent e)
                    {
                        button.setFocus();
                    }

                    public void mouseDoubleClick(MouseEvent e)
                    {
                        mouseUp(e);
                        okPressed();
                    }
                };

                MouseTrackAdapter mouseTrackAdapter = null;
                if(WinAPI.AreVisualStylesEnabled()) {
                    mouseTrackAdapter = new MouseTrackAdapter() {
                        private void paint(int selectedStateId, int unselectedStateId)
                        {
                            GC gc = new GC(button);
                            if(button.getSelection()) {
                                WinAPI.DrawWidgetThemeBackGround(button.handle,gc.handle,"BUTTON",WinAPI.BP_RADIOBUTTON,selectedStateId); //$NON-NLS-1$
                            }
                            else {
                                WinAPI.DrawWidgetThemeBackGround(button.handle,gc.handle,"BUTTON",WinAPI.BP_RADIOBUTTON,unselectedStateId); //$NON-NLS-1$
                            }
                            gc.dispose();
                        }
                        
                        public void mouseEnter(MouseEvent e)
                        {
                            paint(WinAPI.RBS_CHECKEDHOT,WinAPI.RBS_UNCHECKEDHOT);
                        }
    
                        public void mouseExit(MouseEvent e)
                        {
                            paint(WinAPI.RBS_CHECKEDNORMAL,WinAPI.RBS_UNCHECKEDNORMAL);
                        }
                    };
                }

                makeLabel(composite2, site.getImage(), null, white, mouseAdapter, mouseTrackAdapter);
                makeLabel(composite2, null, site.getLocation(), white, mouseAdapter, mouseTrackAdapter);
                makeLabel(composite2, null, site.getContinent(), white, mouseAdapter, mouseTrackAdapter);
                
                if(iter.hasNext()) {
                    l = new Label(composite2,SWT.HORIZONTAL|SWT.SEPARATOR);
                    GridData data = new GridData(SWT.FILL,SWT.FILL,false,false);
                    data.horizontalSpan = 4;
                    l.setLayoutData(data);
                }
            }
            
            Point size = composite2.computeSize(SWT.DEFAULT,SWT.DEFAULT);
            composite2.setSize(size);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.heightHint = Math.min(size.y,300);
            data.widthHint = size.x;
            scrolledComposite.setLayoutData(data);
            
            mSavePreferred = new Button(composite,SWT.CHECK);
            mSavePreferred.setText(EclipseNSISUpdatePlugin.getResourceString("download.sites.dialog.save.label")); //$NON-NLS-1$
            boolean b = true;
            if(mDialogSettings.get(SAVE_PREFERRED) != null) {
                b = mDialogSettings.getBoolean(SAVE_PREFERRED);
            }
            mSavePreferred.setSelection(b);
            mSavePreferred.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            return composite;
        }

        public DownloadSite getSelectedSite()
        {
            return mSelectedSite;
        }
    }
}
