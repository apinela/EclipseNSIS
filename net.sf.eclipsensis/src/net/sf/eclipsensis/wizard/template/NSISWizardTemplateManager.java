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

import java.io.*;
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

    private Collection mTemplates;
    private Collection mDefaultTemplates;
    private Collection mDeleteQueue = new HashSet();
    private HashMap mDefaultTemplatesMap = new HashMap();
    private HashMap mTemplatesMap = new HashMap();
    
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

    static File getLocation(NSISWizardTemplate template)
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

        String fileName = NSISWizardTemplateManager.class.getName()+".Templates.xml"; //$NON-NLS-1$
        cDefaultTemplatesStore = (DEFAULT_LOCATION==null?null:new File(DEFAULT_LOCATION,fileName));
        cUserTemplatesStore = new File(USER_LOCATION,fileName);
    }

    private static Collection loadTemplateStore(File store)
    {
        Collection set = null;
        if(store != null) {
            if(store.exists() && store.isFile()) {
                try {
                    set = cReaderWriter.read(new BufferedInputStream(new FileInputStream(store)));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    set = new HashSet();
                }
            }
        }

        if(set == null) {
            set = new HashSet();
        }
        
        return set;
    }
    
    public NSISWizardTemplateManager()
    {
        mTemplates = new HashSet();
        mDefaultTemplates = loadTemplateStore(cDefaultTemplatesStore);
        for (Iterator iter = mDefaultTemplates.iterator(); iter.hasNext();) {
            NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
            mDefaultTemplatesMap.put(element.getName(),element);
            mTemplates.add(element);
            mTemplatesMap.put(element.getName(),element);
        }
        Collection coll = loadTemplateStore(cUserTemplatesStore);
        for (Iterator iter = coll.iterator(); iter.hasNext();) {
            NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
            NSISWizardTemplate defaultElement = (NSISWizardTemplate)mDefaultTemplatesMap.get(element.getName());
            if(defaultElement != null) {
                element.setType(NSISWizardTemplate.TYPE_CUSTOM);
                mTemplates.remove(defaultElement);
                mTemplatesMap.remove(defaultElement.getName());
            }
            else {
                element.setType(NSISWizardTemplate.TYPE_USER);
            }
            mTemplates.add(element);
            mTemplatesMap.put(element.getName(),element);
        }
    }
    
    public Collection getTemplates()
    {
        return mTemplates;
    }
    
    public Collection getDefaultTemplates()
    {
        return mDefaultTemplates;
    }
    
    public boolean addTemplate(final NSISWizardTemplate template)
    {
        if(mTemplates.contains(template)) {
            return updateTemplate(template);
        }
        else {
            NSISWizardTemplate oldTemplate = (NSISWizardTemplate)mTemplatesMap.get(template.getName());
            if(oldTemplate == null) {
                oldTemplate = (NSISWizardTemplate)mDefaultTemplatesMap.get(template.getName());
            }
            if(oldTemplate != null) {
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
                    mTemplates.remove(oldTemplate);
                    mTemplatesMap.remove(oldTemplate.getName());
                    switch(oldTemplate.getType()) {
                        case NSISWizardTemplate.TYPE_DEFAULT:
                            template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                            break;
                        default:
                            mDeleteQueue.add(oldTemplate);
                            template.setType(oldTemplate.getType());
                            break;
                    }
                }
            }
            template.setDeleted(false);
            mTemplates.add(template);
            mTemplatesMap.put(template.getName(),template);
            return true;
        }
    }
    
    public boolean removeTemplate(final NSISWizardTemplate template)
    {
        if(mTemplates.contains(template)) {
            switch(template.getType()) {
                case NSISWizardTemplate.TYPE_DEFAULT:
                    if(!template.isLoaded()) {
                        template.loadSettings();
                        template.setSettingsChanged(true);
                    }
                    template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                case NSISWizardTemplate.TYPE_CUSTOM:
                    template.setDeleted(true);
                    break;
                case NSISWizardTemplate.TYPE_USER:
                    mTemplates.remove(template);
                    mDeleteQueue.add(template);
                    mTemplatesMap.remove(template.getName());
            }
            return true;
        }
        return false;
    }
    
    public boolean updateTemplate(final NSISWizardTemplate template)
    {
        if(mTemplates.contains(template)) {
            NSISWizardTemplate oldTemplate = null;
            for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
                NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
                if(element.equals(template)) {
                    oldTemplate = element;
                }
            }
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
                        switch(otherTemplate.getType()) {
                            case NSISWizardTemplate.TYPE_DEFAULT:
                                template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                                break;
                            default:
                                mDeleteQueue.add(otherTemplate);
                                break;
                        }
                        mTemplates.remove(otherTemplate);
                        mTemplatesMap.remove(otherTemplate.getName());
                    }
                }
                
            }
            else {
                if(!oldTemplate.isDifferentFrom(template)) {
                    return false;
                }
                switch(template.getType()) {
                    case NSISWizardTemplate.TYPE_DEFAULT:
                        if(!template.isLoaded()) {
                            template.loadSettings();
                            template.setSettingsChanged(true);
                        }
                        template.setType(NSISWizardTemplate.TYPE_CUSTOM);
                }
            }
            mTemplates.remove(oldTemplate);
            mTemplatesMap.remove(oldTemplate.getName());
            mTemplates.add(template);
            mTemplatesMap.put(template.getName(),template);
            return true;
        }
        return false;
    }

    public void restore()
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
            if(element.getType() == NSISWizardTemplate.TYPE_CUSTOM && element.isDeleted()) {
                element.setDeleted(false);
            }
        }
    }

    public boolean canRestore()
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            NSISWizardTemplate element = (NSISWizardTemplate)iter.next();
            if(element.getType() == NSISWizardTemplate.TYPE_CUSTOM && element.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    public void resetToDefaults()
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            NSISWizardTemplate template = (NSISWizardTemplate)iter.next();
            iter.remove();
            mTemplatesMap.remove(template.getName());
            if(template.getType() != NSISWizardTemplate.TYPE_DEFAULT) {
                mDeleteQueue.add(template);
            }
        }
        mTemplates.clear();
        mTemplatesMap.clear();
        mTemplates.addAll(mDefaultTemplates);
        mTemplatesMap.putAll(mDefaultTemplatesMap);
    }

    public void revert(NSISWizardTemplate template)
    {
        if(template.getType() == NSISWizardTemplate.TYPE_CUSTOM) {
            NSISWizardTemplate defaultTemplate = (NSISWizardTemplate)mDefaultTemplatesMap.get(template.getName());
            mTemplates.remove(template);
            mTemplatesMap.remove(template.getName());
            mDeleteQueue.add(template);
            mTemplates.add(defaultTemplate);
            mTemplatesMap.put(defaultTemplate.getName(),defaultTemplate);
        }
    }
    
    public boolean canRevert(NSISWizardTemplate template)
    {
        return (mTemplates.contains(template) && (template.getType() == NSISWizardTemplate.TYPE_CUSTOM));
    }
    
    /**
     * @throws IOException
     * 
     */
    public void save() throws IOException
    {
        HashSet set = new HashSet();
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
                NSISWizardTemplate template = (NSISWizardTemplate)iter.next();
                if(template.getType() != NSISWizardTemplate.TYPE_DEFAULT) {
                    template.saveSettings();
                    set.add(template);
                }
        }
        for (Iterator iter = mDeleteQueue.iterator(); iter.hasNext();) {
            NSISWizardTemplate template = (NSISWizardTemplate)iter.next();
            template.deleteSettings();
            iter.remove();
        }
        cReaderWriter.save(set,new BufferedOutputStream(new FileOutputStream(cUserTemplatesStore)));
    }
}
