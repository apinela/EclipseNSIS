/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.*;

public class HelpBrowserLocalFileHandler implements IExtensionChangeHandler, IHelpBrowserLocalFileHandler
{
    private static final String EXTENSION_POINT = "helpBrowserLocalFileHandler"; //$NON-NLS-1$
    private static final String HANDLER = "handler"; //$NON-NLS-1$
    private static final String HANDLER_ID = "id"; //$NON-NLS-1$
    private static final String HANDLER_NAME = "name"; //$NON-NLS-1$
    private static final String HANDLER_EXTENSIONS = "extensions"; //$NON-NLS-1$
    private static final String HANDLER_CLASS = "class"; //$NON-NLS-1$
    
    private static final IHelpBrowserLocalFileHandler NULL_HANDLER = new IHelpBrowserLocalFileHandler() {
        public boolean handle(File file)
        {
            return false;
        }
    };

    public static final HelpBrowserLocalFileHandler INSTANCE = new HelpBrowserLocalFileHandler();

    private Map mExtensions = new LinkedHashMap();
    
    private Object mLock = new Object();

    private HelpBrowserLocalFileHandler()
    {
        super();
        final IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        loadExtensions(tracker);
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
        final BundleContext bundleContext = EclipseNSISPlugin.getDefault().getBundleContext();
        bundleContext.addBundleListener(new BundleListener() {
            public void bundleChanged(BundleEvent event)
            {
                if(event.getType() == BundleEvent.STOPPED ) {
                    bundleContext.removeBundleListener(this);
                }
                tracker.unregisterHandler(HelpBrowserLocalFileHandler.this);
            }
        });
    }

    public boolean handle(File file)
    {
        synchronized (mLock) {
            String ext = IOUtility.getFileExtension(file);
            if (!Common.isEmpty(ext)) {
                for (Iterator iter = mExtensions.keySet().iterator(); iter.hasNext();) {
                    String extensionId = (String)iter.next();
                    IExtension extension = getExtensionPointFilter().getExtension(extensionId);
                    if (extension == null) {
                        iter.remove();
                    }
                    else {
                        List handlers = (List)mExtensions.get(extensionId);
                        for (Iterator iterator = handlers.iterator(); iterator.hasNext();) {
                            HandlerDescriptor desc = (HandlerDescriptor)iterator.next();
                            if (desc.getExtensions().contains(ext)) {
                                try {
                                    return desc.getHandler().handle(file);
                                }
                                catch (Throwable e) {
                                    EclipseNSISPlugin.getDefault().log(e);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }        
    }

    private IExtensionPoint getExtensionPointFilter()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(INSISConstants.PLUGIN_ID,EXTENSION_POINT);
    }

    private void loadExtensions(IExtensionTracker tracker)
    {
        IExtensionPoint point = getExtensionPointFilter();
        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                addExtension(tracker, extensions[i]);
            }
        }
    }

    public void addExtension(IExtensionTracker tracker, IExtension extension)
    {
        synchronized (mLock) {
            if (!mExtensions.containsKey(extension.getUniqueIdentifier())) {
                IConfigurationElement[] elements = extension.getConfigurationElements();
                List handlers = new ArrayList();
                for (int i = 0; i < elements.length; i++) {
                    if (HANDLER.equals(elements[i].getName())) {
                        try {
                            HandlerDescriptor descriptor = new HandlerDescriptor(elements[i]);
                            tracker.registerObject(extension, descriptor, IExtensionTracker.REF_WEAK);
                            handlers.add(descriptor);
                        }
                        catch (Exception e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                }
                mExtensions.put(extension.getUniqueIdentifier(), handlers);
            }
        }        
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        synchronized (mLock) {
            if (mExtensions.containsKey(extension.getUniqueIdentifier())) {
                mExtensions.remove(extension.getUniqueIdentifier());
            }
        }        
    }

    private class HandlerDescriptor
    {
        private IConfigurationElement mElement;
        
        private String mId = ""; //$NON-NLS-1$
        private String mName = ""; //$NON-NLS-1$
        private Set mExtensions = new CaseInsensitiveSet();
        private IHelpBrowserLocalFileHandler mHandler = null;

        private HandlerDescriptor(IConfigurationElement element)
        {
            super();
            String id = element.getAttribute(HANDLER_ID);
            if (id != null) {
                mId = id;
            }
            String name = element.getAttribute(HANDLER_NAME);
            if (name != null) {
                mName = name;
            }
            mExtensions.addAll(Common.tokenizeToList(element.getAttribute(HANDLER_EXTENSIONS), ','));
            mElement = element;
        }

        public Set getExtensions()
        {
            return mExtensions;
        }

        public IHelpBrowserLocalFileHandler getHandler()
        {
            if(mHandler == null) {
                try {
                    mHandler = (IHelpBrowserLocalFileHandler)mElement.createExecutableExtension(HANDLER_CLASS);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    mHandler = NULL_HANDLER;
                    mExtensions.clear();
                }
            }
            return mHandler;
        }

        public String getId()
        {
            return mId;
        }

        public String getName()
        {
            return mName;
        }
    }
}
