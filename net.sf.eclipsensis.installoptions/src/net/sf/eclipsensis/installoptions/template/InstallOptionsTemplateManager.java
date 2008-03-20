/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.template.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;

public class InstallOptionsTemplateManager extends AbstractTemplateManager
{
    private static final Path cPath = new Path("templates"); //$NON-NLS-1$
    public static final InstallOptionsTemplateManager INSTANCE = new InstallOptionsTemplateManager();

    private List mListeners = new ArrayList();
    private Map mTemplateFactories = new HashMap();
    private List mEventQueue = new ArrayList();

    private InstallOptionsTemplateManager()
    {
        super();
    }

    protected Plugin getPlugin()
    {
        return InstallOptionsPlugin.getDefault();
    }

    protected IPath getTemplatesPath()
    {
        return cPath;
    }

    protected Class getTemplateClass()
    {
        return IInstallOptionsTemplate.class;
    }

    public synchronized InstallOptionsTemplateCreationFactory getTemplateFactory(IInstallOptionsTemplate template)
    {
        InstallOptionsTemplateCreationFactory factory = (InstallOptionsTemplateCreationFactory)mTemplateFactories.get(template);
        if(factory == null) {
            if(getTemplates().contains(template)) {
                factory = new InstallOptionsTemplateCreationFactory(template);
                mTemplateFactories.put(template, factory);
            }
        }
        return factory;
    }

    public boolean addTemplate(ITemplate template)
    {
        if(super.addTemplate(template)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (IInstallOptionsTemplate)template);
            return true;
        }
        return false;
    }

    public boolean updateTemplate(ITemplate oldTemplate, ITemplate newTemplate)
    {
        if(super.updateTemplate(oldTemplate, newTemplate)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, (IInstallOptionsTemplate)oldTemplate, (IInstallOptionsTemplate)newTemplate);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean removeTemplate(ITemplate template)
    {
        if(super.removeTemplate(template)) {
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, (IInstallOptionsTemplate)template, null);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void resetToDefaults()
    {
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            IInstallOptionsTemplate template = (IInstallOptionsTemplate)iter.next();
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
        }
        super.resetToDefaults();
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (IInstallOptionsTemplate)iter.next());
        }
    }

    protected boolean restore(ITemplate template)
    {
        if(super.restore(template)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (IInstallOptionsTemplate)template);
            return true;
        }
        return false;
    }

    public ITemplate revert(ITemplate template)
    {
        IInstallOptionsTemplate newTemplate = (IInstallOptionsTemplate)super.revert(template);
        if(newTemplate != null) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, (IInstallOptionsTemplate)template, newTemplate);
        }
        return newTemplate;
    }

    public void addTemplateListener(IInstallOptionsTemplateListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeTemplateListener(IInstallOptionsTemplateListener listener)
    {
        mListeners.remove(listener);
    }

    private void queueEvent(int type, IInstallOptionsTemplate oldTemplate, IInstallOptionsTemplate newTemplate)
    {
        mEventQueue.add(new InstallOptionsTemplateEvent(type, oldTemplate, newTemplate));
    }

    private void notifyListeners()
    {
        InstallOptionsTemplateEvent[] events = (InstallOptionsTemplateEvent[])mEventQueue.toArray(new InstallOptionsTemplateEvent[mEventQueue.size()]);
        mEventQueue.clear();
        IInstallOptionsTemplateListener[] listeners = (IInstallOptionsTemplateListener[])mListeners.toArray(new IInstallOptionsTemplateListener[mListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            try {
                listeners[i].templatesChanged(events);
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }
    }

    protected Map loadDefaultTemplateStore() throws IOException, ClassNotFoundException
    {
        Map map = super.loadDefaultTemplateStore();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Entry)iter.next();
            IInstallOptionsTemplate template = (IInstallOptionsTemplate)entry.getValue();
            if(template instanceof InstallOptionsTemplate) {
                template = new InstallOptionsTemplate2(template);
                entry.setValue(template);
            }
        }
        return map;
    }

    protected List loadUserTemplateStore() throws IOException, ClassNotFoundException
    {
        List list = super.loadUserTemplateStore();
        for (ListIterator iter = list.listIterator(); iter.hasNext();) {
            IInstallOptionsTemplate template = (IInstallOptionsTemplate)iter.next();
            if(template instanceof InstallOptionsTemplate) {
                template = new InstallOptionsTemplate2(template);
                iter.set(template);
            }
        }
        return list;
    }

    public void save() throws IOException
    {
        super.save();
        notifyListeners();
    }

    public void discard()
    {
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            IInstallOptionsTemplate template = (IInstallOptionsTemplate)iter.next();
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
        }
        super.discard();
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (IInstallOptionsTemplate)iter.next());
        }

        notifyListeners();
    }

    protected AbstractTemplateReaderWriter createReaderWriter()
    {
        return InstallOptionsTemplateReaderWriter.INSTANCE;
    }

    protected Image getShellImage()
    {
        return InstallOptionsPlugin.getShellImage();
    }
}
