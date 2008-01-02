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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
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
        return InstallOptionsTemplate.class;
    }

    public synchronized InstallOptionsTemplateCreationFactory getTemplateFactory(InstallOptionsTemplate template)
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

    public boolean addTemplate(AbstractTemplate template)
    {
        if(super.addTemplate(template)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)template);
            return true;
        }
        return false;
    }

    public boolean updateTemplate(AbstractTemplate oldTemplate, AbstractTemplate newTemplate)
    {
        if(super.updateTemplate(oldTemplate, newTemplate)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, (InstallOptionsTemplate)oldTemplate, (InstallOptionsTemplate)newTemplate);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean removeTemplate(AbstractTemplate template)
    {
        if(super.removeTemplate(template)) {
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, (InstallOptionsTemplate)template, null);
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
            InstallOptionsTemplate template = (InstallOptionsTemplate)iter.next();
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
        }
        super.resetToDefaults();
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)iter.next());
        }
    }

    protected boolean restore(AbstractTemplate template)
    {
        if(super.restore(template)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)template);
            return true;
        }
        return false;
    }

    public AbstractTemplate revert(AbstractTemplate template)
    {
        InstallOptionsTemplate newTemplate = (InstallOptionsTemplate)super.revert(template);
        if(newTemplate != null) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, (InstallOptionsTemplate)template, newTemplate);
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

    private void queueEvent(int type, InstallOptionsTemplate oldTemplate, InstallOptionsTemplate newTemplate)
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

    public void save() throws IOException
    {
        super.save();
        notifyListeners();
    }

    public void discard()
    {
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            InstallOptionsTemplate template = (InstallOptionsTemplate)iter.next();
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
        }
        super.discard();
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)iter.next());
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

    protected boolean templatesAreEqual(AbstractTemplate t1, AbstractTemplate t2)
    {
        if(super.templatesAreEqual(t1,t2)) {
            INISection[] sections1 = ((InstallOptionsTemplate)t1).getSections();
            INISection[] sections2 = ((InstallOptionsTemplate)t2).getSections();
            if (sections1==sections2) {
                return true;
            }
            if (sections1==null || sections2==null) {
                return false;
            }

            int length = sections1.length;
            if (sections2.length != length) {
                return false;
            }

            for (int i=0; i<length; i++) {
                if (!(sections1[i]==null ? sections2[i]==null : sections1[i].matches(sections2[i]))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
