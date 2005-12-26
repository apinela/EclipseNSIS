/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.filemon;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.IOUtility;

public class FileMonitor
{
    public static final long POLL_INTERVAL;
    public static final int FILE_MODIFIED = 0;
    public static final int FILE_DELETED = 1;

    public static final FileMonitor INSTANCE = new FileMonitor();

    private static final WeakReference[] EMPTY_ARRAY = new WeakReference[0];
    private Timer mTimer;
    private Map mRegistry = new LinkedHashMap();

    static {
        long interval;
        try {
            interval = Long.parseLong(EclipseNSISPlugin.getResourceString("file.change.monitor.poll.interval","500")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch(Throwable t) {
            interval = 500;
        }
        POLL_INTERVAL = interval;
    }

    private FileMonitor()
    {
        super();
    }

    public void start()
    {
        mTimer = new Timer(true);
        mTimer.schedule(new FileChangeTimerTask(), 0, POLL_INTERVAL);
    }

    public void stop()
    {
        mTimer.cancel();
        mTimer = null;
    }

    public void register(File file, IFileChangeListener listener)
    {
        if(IOUtility.isValidFile(file)) {
            FileChangeRegistryEntry entry = (FileChangeRegistryEntry)mRegistry.get(file);
            if(entry == null) {
                entry = new FileChangeRegistryEntry();
                entry.lastModified = file.lastModified();
                mRegistry.put(file,entry);
            }
            for(Iterator iter=entry.listeners.iterator(); iter.hasNext(); ) {
                if(((WeakReference)iter.next()).get() == listener) {
                    if(entry.listeners.isEmpty()) {
                        mRegistry.remove(file);
                        return;
                    }
                }
            }
            entry.listeners.add(new WeakReference(listener));
        }
    }

    public void unregister(File file, IFileChangeListener listener)
    {
        FileChangeRegistryEntry entry = (FileChangeRegistryEntry)mRegistry.get(file);
        if(entry != null) {
            for(Iterator iter=entry.listeners.iterator(); iter.hasNext(); ) {
                if(((WeakReference)iter.next()).get() == listener) {
                    iter.remove();
                    if(entry.listeners.isEmpty()) {
                        mRegistry.remove(file);
                        return;
                    }
                }
            }
        }
    }

    public void unregister(File file)
    {
        mRegistry.remove(file);
    }

    private class FileChangeTimerTask extends TimerTask
    {
        public void run()
        {
            File[] files = (File[])mRegistry.keySet().toArray(new File[mRegistry.size()]);
            for(int i=0; i<files.length; i++) {
                File file = files[i];
                FileChangeRegistryEntry entry = (FileChangeRegistryEntry)mRegistry.get(file);
                if(!IOUtility.isValidFile(file)) {
                    /* Sleep 50 ms & see if the file shows up again- i.e.,
                     * we caught the event in the middle of a move operation. */
                    try {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    if(!IOUtility.isValidFile(file)) {
                        /* Yup, it's really gone. Bummer. */
                        mRegistry.remove(file);
                        fireChanged(FILE_DELETED, file, entry);
                        continue;
                    }
                }
                long lastModified = file.lastModified();
                if(lastModified != entry.lastModified) {
                    entry.lastModified = lastModified;
                    fireChanged(FILE_MODIFIED, file, entry);
                }
                if(entry.listeners.isEmpty()) {
                    mRegistry.remove(file);
                }
            }
        }

        private void fireChanged(int type, File file, FileChangeRegistryEntry entry)
        {
            WeakReference[] listeners = (WeakReference[])entry.listeners.toArray(EMPTY_ARRAY);
            for (int i = 0; i < listeners.length; i++) {
                IFileChangeListener listener = (IFileChangeListener)listeners[i].get();
                if(listener == null) {
                    entry.listeners.remove(listeners[i]);
                }
                else {
                    listener.fileChanged(type, file);
                }
            }
        }
    }

    private class FileChangeRegistryEntry
    {
        long lastModified;
        List listeners = new ArrayList();
    }
}
