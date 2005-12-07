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
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.ui.PlatformUI;

public class HelpBrowserLocalFileHandler implements IExtensionChangeHandler, IHelpBrowserLocalFileHandler
{
    private static final String EXTENSION_POINT = "helpBrowserLocalFileHandler"; //$NON-NLS-1$
    private static final String HANDLER_ID = "id"; //$NON-NLS-1$
    private static final String HANDLER_NAME = "name"; //$NON-NLS-1$
    private static final String HANDLER_EXTENSIONS = "extensions"; //$NON-NLS-1$
    private static final String HANDLER_CLASS = "class"; //$NON-NLS-1$

    public static final HelpBrowserLocalFileHandler INSTANCE = new HelpBrowserLocalFileHandler();

    private Map mExtensions = new LinkedHashMap();

    private HelpBrowserLocalFileHandler()
    {
        super();
        IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        loadExtensions(tracker);
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
    }

    public boolean handle(File file)
    {
        String ext = IOUtility.getFileExtension(file);
        if(!Common.isEmpty(ext)) {
            for(Iterator iter=mExtensions.keySet().iterator(); iter.hasNext(); ) {
                String extensionId = (String)iter.next();
                IExtension extension = getExtensionPointFilter().getExtension(extensionId);
                if(extension == null) {
                    iter.remove();
                }
                else {
                    List handlers = (List)mExtensions.get(extensionId);
                    for (Iterator iterator = handlers.iterator(); iterator.hasNext();) {
                        HandlerDescriptor desc = (HandlerDescriptor)iterator.next();
                        if(desc.extensions.contains(ext)) {
                            try {
                                return desc.handler.handle(file);
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
        if(!mExtensions.containsKey(extension.getUniqueIdentifier())) {
            IConfigurationElement[] elements = extension.getConfigurationElements();
            List handlers = new ArrayList();
            for (int i = 0; i < elements.length; i++) {
                try {
                    HandlerDescriptor descriptor = new HandlerDescriptor();
                    String id = elements[i].getAttribute(HANDLER_ID);
                    if(id != null) {
                        descriptor.id = id;
                    }
                    String name = elements[i].getAttribute(HANDLER_NAME);
                    if(name != null) {
                        descriptor.name = name;
                    }
                    descriptor.extensions.addAll(Common.tokenizeToList(elements[i].getAttribute(HANDLER_EXTENSIONS),','));
                    descriptor.handler = (IHelpBrowserLocalFileHandler)elements[i].createExecutableExtension(HANDLER_CLASS);

                    tracker.registerObject(extension, descriptor,IExtensionTracker.REF_WEAK);
                    handlers.add(descriptor);
                }
                catch(Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            mExtensions.put(extension.getUniqueIdentifier(), handlers);
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        if(mExtensions.containsKey(extension.getUniqueIdentifier())) {
            mExtensions.remove(extension.getUniqueIdentifier());
        }
    }

    private class HandlerDescriptor
    {
        String id = ""; //$NON-NLS-1$
        String name = ""; //$NON-NLS-1$
        Set extensions = new CaseInsensitiveSet();
        IHelpBrowserLocalFileHandler handler = null;
    }
}
