/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;

public class NSISWizardTemplateManager
{
    public static final File DEFAULT_LOCATION;
    public static final File USER_LOCATION;

    private static final File cUserTemplatesStore;
    private static final File cDefaultTemplatesStore;
    private static final NSISWizardTemplateReaderWriter cReaderWriter = new NSISWizardTemplateReaderWriter();

    private Map mTemplatesMap = new HashMap();
    private Map mDefaultTemplatesMap = new HashMap();
    
    private static File checkLocation(File parentFolder)
    {
        File location = null;
        if(parentFolder != null) {
            location = new File(parentFolder,"wizard"); //$NON-NLS-1$
            if(location.exists() && location.isFile()) {
                location.delete();
            }
            if(!location.exists()) {
                location.mkdirs();
            }
        }
        return location;
    }

    private static File getLocation(NSISWizardTemplate template)
    {
        return (template.getType()==NSISWizardTemplate.TYPE_DEFAULT?DEFAULT_LOCATION:USER_LOCATION);
    }
    
    static {
        File pluginLocation;
        try {
            pluginLocation = new Path(Platform.resolve(Platform.find(EclipseNSISPlugin.getDefault().getBundle(), new Path("/"))).getFile()).toFile(); //$NON-NLS-1$
        }
        catch (IOException e) {
            pluginLocation = null;
            e.printStackTrace();
        }
        DEFAULT_LOCATION = checkLocation(pluginLocation);
        USER_LOCATION = checkLocation(EclipseNSISPlugin.getPluginStateLocation());

        String fileName = NSISWizardTemplateManager.class.getName()+".Templates.ser"; //$NON-NLS-1$
        cDefaultTemplatesStore = (DEFAULT_LOCATION==null?null:new File(DEFAULT_LOCATION,fileName));
        cUserTemplatesStore = new File(USER_LOCATION,fileName);
    }

    private static Map loadTemplateStore(File store)
    {
        Map map = null;
        if(store != null) {
            if(store.exists() && store.isFile()) {
                try {
                    map = (Map)Common.readObject(store);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    map = new HashMap();
                }
            }
        }

        if(map == null) {
            map = new HashMap();
        }
        
        return map;
    }
    
    public NSISWizardTemplateManager()
    {
        mDefaultTemplatesMap = loadTemplateStore(cDefaultTemplatesStore);
        mTemplatesMap = new HashMap(mDefaultTemplatesMap);
        mTemplatesMap.putAll(loadTemplateStore(cUserTemplatesStore));
    }
    
    public Collection getTemplates()
    {
        return mTemplatesMap.values();
    }
    
    public Collection getDefaultTemplates()
    {
        return mDefaultTemplatesMap.values();
    }
    
    public boolean addTemplate(final NSISWizardTemplate template)
    {
        NSISWizardTemplate oldTemplate = (NSISWizardTemplate)mTemplatesMap.get(template.getName());
        if(oldTemplate != null) {
            int type = oldTemplate.getType();
            if(type != NSISWizardTemplate.TYPE_CUSTOM || !oldTemplate.isDeleted()) {
                final boolean[] rv = { true };
                Display.getDefault().syncExec(new Runnable() {
                    public void run()
                    {
                        if(!Common.openConfirm(null,EclipseNSISPlugin.getFormattedString("wizard.template.save.confirm", //$NON-NLS-1$
                                                     new String[]{template.getName()}))) {
                            rv[0] = false;
                        }
                    }
                });
                if(!rv[0]) {
                    return false;
                }
                else {
                    switch(oldTemplate.getType()) {
                        case NSISWizardTemplate.TYPE_DEFAULT:
                            template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                            break;
                        default:
                            template.setType(oldTemplate.getType());
                            break;
                    }
                }
            }
            else {
                template.setType(NSISWizardTemplate.TYPE_CUSTOM);
            }
        }
        else {
            template.setType(NSISWizardTemplate.TYPE_USER);
        }
        template.setDeleted(false);
        mTemplatesMap.put(template.getName(),template);
        return true;
    }
    
    public boolean removeTemplate(final NSISWizardTemplate template)
    {
        if(mTemplatesMap.containsKey(template.getName())) {
            switch(template.getType()) {
                case NSISWizardTemplate.TYPE_DEFAULT:
                    template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                case NSISWizardTemplate.TYPE_CUSTOM:
                    template.setDeleted(true);
                    break;
                case NSISWizardTemplate.TYPE_USER:
                    mTemplatesMap.remove(template.getName());
            }
            return true;
        }
        return false;
    }
 
    
    public boolean updateTemplate(NSISWizardTemplate oldTemplate, final NSISWizardTemplate template)
    {
        if(mTemplatesMap.containsKey(oldTemplate.getName())) {
            if(!oldTemplate.getName().equals(template.getName())) {
                NSISWizardTemplate otherTemplate = (NSISWizardTemplate)mTemplatesMap.get(template.getName());
                if(otherTemplate != null) {
                    Display display = Display.getDefault();
                    final boolean[] rv = { true };
                    display.syncExec(new Runnable() {
                        public void run()
                        {
                            if(!Common.openConfirm(null,EclipseNSISPlugin.getFormattedString("wizard.template.save.confirm", //$NON-NLS-1$
                                                         new String[]{template.getName()}))) {
                                rv[0] = false;
                            }
                        }
                    });
                    if(!rv[0]) {
                        return false;
                    }
                    else {
                        removeTemplate(oldTemplate);
                        oldTemplate = otherTemplate;
                    }
                }
            }
            if(oldTemplate.getType() == NSISWizardTemplate.TYPE_DEFAULT) {
                template.setType(NSISWizardTemplate.TYPE_CUSTOM);
            }
            else {
                template.setType(oldTemplate.getType());
            }
            mTemplatesMap.remove(oldTemplate.getName());
            template.setDeleted(false);
            mTemplatesMap.put(template.getName(),template);
            return true;
        }
        return false;
    }

    public void restore()
    {
        for (Iterator iter = mTemplatesMap.values().iterator(); iter.hasNext();) {
            NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
            if(element.getType() == NSISWizardTemplate.TYPE_CUSTOM && element.isDeleted()) {
                element.setDeleted(false);
            }
        }
    }

    public boolean canRestore()
    {
        for (Iterator iter = mTemplatesMap.values().iterator(); iter.hasNext();) {
            NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
            if(element.getType() == NSISWizardTemplate.TYPE_CUSTOM && element.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    public void resetToDefaults()
    {
        mTemplatesMap.clear();
        mTemplatesMap.putAll(mDefaultTemplatesMap);
    }

    public NSISWizardTemplate revert(NSISWizardTemplate template)
    {
        if(template.getType() == NSISWizardTemplate.TYPE_CUSTOM) {
            NSISWizardTemplate defaultTemplate = (NSISWizardTemplate)mDefaultTemplatesMap.get(template.getName());
            mTemplatesMap.put(defaultTemplate.getName(),defaultTemplate);
            return defaultTemplate;
        }
        return null;
    }
    
    public boolean canRevert(NSISWizardTemplate template)
    {
        return (mTemplatesMap.containsKey(template.getName()) && (template.getType() == NSISWizardTemplate.TYPE_CUSTOM));
    }
    
    /**
     * @throws IOException
     * 
     */
    public void save() throws IOException
    {
        HashMap map = new HashMap();
        for (Iterator iter = mTemplatesMap.values().iterator(); iter.hasNext();) {
            NSISWizardTemplate template = (NSISWizardTemplate)iter.next();
            if(template.getType() != NSISWizardTemplate.TYPE_DEFAULT) {
                map.put(template.getName(),template);
            }
        }
        Common.writeObject(cUserTemplatesStore, map);
    }
}
