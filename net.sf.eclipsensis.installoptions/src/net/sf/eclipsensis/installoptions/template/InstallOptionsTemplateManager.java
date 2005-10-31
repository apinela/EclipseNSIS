/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import java.util.*;

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
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)template);
            return true;
        }
        return false;
    }
    
    public boolean updateTemplate(AbstractTemplate oldTemplate, AbstractTemplate newTemplate)
    {
        if(super.updateTemplate(oldTemplate, newTemplate)) {
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, (InstallOptionsTemplate)oldTemplate, (InstallOptionsTemplate)newTemplate);
            return true;
        }
        else {
            return false;
        }
    }
    
    public boolean removeTemplate(AbstractTemplate template)
    {
        if(super.removeTemplate(template)) {
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, (InstallOptionsTemplate)template, null);
            return true;
        }
        else {
            return false;
        }
    }

    public void resetToDefaults()
    {
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, (InstallOptionsTemplate)iter.next(), null);
        }
        super.resetToDefaults();
        for(Iterator iter=getTemplates().iterator(); iter.hasNext(); ) {
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)iter.next());
        }
    }

    protected boolean restore(AbstractTemplate template)
    {
        if(super.restore(template)) {
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, (InstallOptionsTemplate)template);
            return true;
        }
        return false;
    }

    public AbstractTemplate revert(AbstractTemplate template)
    {
        InstallOptionsTemplate oldTemplate = (InstallOptionsTemplate)getTemplate(template.getName());
        InstallOptionsTemplate newTemplate = (InstallOptionsTemplate)super.revert(template);
        if(newTemplate != null) {
            notifyListeners(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, oldTemplate, newTemplate);
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
    
    private void notifyListeners(int type, InstallOptionsTemplate oldTemplate, InstallOptionsTemplate newTemplate)
    {
        InstallOptionsTemplateEvent event = new InstallOptionsTemplateEvent(type, oldTemplate, newTemplate);
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            ((IInstallOptionsTemplateListener)iter.next()).templateChanged(event);
        }
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
