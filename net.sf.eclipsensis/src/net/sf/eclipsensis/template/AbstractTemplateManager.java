/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.net.URL;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.graphics.Image;

public abstract class AbstractTemplateManager
{
    private File mUserTemplatesStore;
    private URL mDefaultTemplatesStore;

    private List mTemplates;
    private Map mDefaultTemplatesMap;
    private AbstractTemplateReaderWriter mReaderWriter;

    public AbstractTemplateManager()
    {
        String fileName = getClass().getName()+".Templates.ser"; //$NON-NLS-1$

        mDefaultTemplatesStore = getPlugin().getBundle().getResource(getTemplatesPath().append(fileName).makeAbsolute().toString());

        File parentFolder = getPlugin().getStateLocation().toFile();
        File location = null;
        if(parentFolder != null) {
            location = new File(parentFolder,getTemplatesPath().toString());
            if(IOUtility.isValidFile(location)) {
                location.delete();
            }
            if(!location.exists()) {
                location.mkdirs();
            }
        }

        mUserTemplatesStore = new File(location,fileName);

        try {
            mDefaultTemplatesMap = loadDefaultTemplateStore();
        }
        catch (Exception e1) {
            EclipseNSISPlugin.getDefault().log(e1);
            mDefaultTemplatesMap = new HashMap();
        }
        finally {
            if(mDefaultTemplatesMap == null) {
                mDefaultTemplatesMap = new HashMap();
            }
        }

        Map map = new HashMap(mDefaultTemplatesMap);

        mTemplates = new ArrayList();
        try {
            List list = loadUserTemplateStore();
            if(list != null) {
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    AbstractTemplate template = (AbstractTemplate)iter.next();
                    if(template.getType() == AbstractTemplate.TYPE_CUSTOM) {
                        map.remove(template.getName());
                    }
                    mTemplates.add(template);
                }
            }
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        mTemplates.addAll(map.values());

        mReaderWriter = createReaderWriter();
    }

    public AbstractTemplateReaderWriter getReaderWriter()
    {
        return mReaderWriter;
    }

    protected Map loadDefaultTemplateStore() throws IOException, ClassNotFoundException
    {
        Map map = null;
        if(mDefaultTemplatesStore != null) {
            InputStream stream = mDefaultTemplatesStore.openStream();
            map = (Map)IOUtility.readObject(stream, getClass().getClassLoader());
        }

        return  map;
    }

    protected List loadUserTemplateStore() throws IOException, ClassNotFoundException
    {
        List list = null;
        if(IOUtility.isValidFile(mUserTemplatesStore)) {
            Object obj = IOUtility.readObject(mUserTemplatesStore, getClass().getClassLoader());
            if(obj instanceof Map) {
                //migrate
                Map defaults = new HashMap();
                if(mDefaultTemplatesMap != null) {
                    for (Iterator iter = mDefaultTemplatesMap.values().iterator(); iter.hasNext();) {
                        AbstractTemplate template = (AbstractTemplate)iter.next();
                        defaults.put(template.getName(),template);
                    }
                }
                list = new ArrayList();
                for(Iterator iter = ((Map)obj).values().iterator(); iter.hasNext(); ) {
                    AbstractTemplate template = (AbstractTemplate)iter.next();
                    switch(template.getType()) {
                        case AbstractTemplate.TYPE_DEFAULT:
                            continue;
                        case AbstractTemplate.TYPE_CUSTOM:
                            AbstractTemplate t = (AbstractTemplate)defaults.get(template.getName());
                            if(t == null) {
                                template.setType(AbstractTemplate.TYPE_USER);
                            }
                            else {
                                template.setId(t.getId());
                                if(t.equals(template)) {
                                   continue;
                                }
                            }
                        case AbstractTemplate.TYPE_USER:
                            list.add(template);
                    }
                }
                IOUtility.writeObject(mUserTemplatesStore, list);
            }
            else if(obj instanceof List) {
                list = (List)obj;
            }
            else if(obj instanceof Collection) {
                list = new ArrayList((Collection)obj);
            }
        }
        return list;
    }

    protected URL getDefaultTemplatesStore()
    {
        return mDefaultTemplatesStore;
    }

    protected File getUserTemplatesStore()
    {
        return mUserTemplatesStore;
    }

    public AbstractTemplate getTemplate(String id)
    {
        return (AbstractTemplate)mDefaultTemplatesMap.get(id);
    }

    public Collection getTemplates()
    {
        return Collections.unmodifiableCollection(mTemplates);
    }

    public Collection getDefaultTemplates()
    {
        return Collections.unmodifiableCollection(mDefaultTemplatesMap.values());
    }

    public boolean addTemplate(final AbstractTemplate template)
    {
        checkClass(template);
        template.setId(null);
        template.setType(AbstractTemplate.TYPE_USER);
        template.setDeleted(false);
        mTemplates.add(template);
        return true;
    }

    public boolean removeTemplate(AbstractTemplate template)
    {
        checkClass(template);
        if(mTemplates.contains(template)) {
            switch(template.getType()) {
                case AbstractTemplate.TYPE_DEFAULT:
                    template.setType(AbstractTemplate.TYPE_CUSTOM);
                case AbstractTemplate.TYPE_CUSTOM:
                    AbstractTemplate t = (AbstractTemplate)template.clone();
                    t.setDeleted(true);
                    mTemplates.add(t);
                case AbstractTemplate.TYPE_USER:
                    mTemplates.remove(template);
            }
            return true;
        }
        return false;
    }

    public boolean updateTemplate(AbstractTemplate oldTemplate, final AbstractTemplate template)
    {
        checkClass(oldTemplate);
        checkClass(template);
        if(mTemplates.contains(oldTemplate)) {
            mTemplates.remove(oldTemplate);

            if(oldTemplate.getType() != AbstractTemplate.TYPE_USER) {
                AbstractTemplate defaultTemplate = getTemplate(oldTemplate.getId());
                if(!template.equals(defaultTemplate)) {
                    template.setType(AbstractTemplate.TYPE_CUSTOM);
                }
            }
            template.setDeleted(false);
            mTemplates.add(template);
            return true;
        }
        return false;
    }

    public void restore()
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            restore((AbstractTemplate)iter.next());
        }
    }

    protected boolean restore(AbstractTemplate template)
    {
        checkClass(template);
        if(template.getType() == AbstractTemplate.TYPE_CUSTOM && template.isDeleted()) {
            template.setDeleted(false);
            AbstractTemplate defaultTemplate = getTemplate(template.getId());
            if(template.equals(defaultTemplate)) {
                template.setType(AbstractTemplate.TYPE_DEFAULT);
            }
            return true;
        }
        return false;
    }

    public boolean canRestore()
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            AbstractTemplate template = (AbstractTemplate)iter.next();
            checkClass(template);
            if(template.getType() == AbstractTemplate.TYPE_CUSTOM && template.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    public void resetToDefaults()
    {
        mTemplates.clear();
        mTemplates.addAll(mDefaultTemplatesMap.values());
    }

    public AbstractTemplate revert(AbstractTemplate template)
    {
        checkClass(template);
        if(template.getType() == AbstractTemplate.TYPE_CUSTOM) {
            AbstractTemplate defaultTemplate = (AbstractTemplate)mDefaultTemplatesMap.get(template.getId());
            checkClass(defaultTemplate);
            mTemplates.remove(template);
            mTemplates.add(defaultTemplate);
            return defaultTemplate;
        }
        return null;
    }

    public boolean canRevert(AbstractTemplate template)
    {
        checkClass(template);
        return (mTemplates.contains(template) && (template.getType() == AbstractTemplate.TYPE_CUSTOM));
    }

    /**
     * @throws IOException
     *
     */
    public void save() throws IOException
    {
        List list = new ArrayList();
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            AbstractTemplate template = (AbstractTemplate)iter.next();
            checkClass(template);
            if(template.getType() != AbstractTemplate.TYPE_DEFAULT) {
                list.add(template);
            }
        }
        IOUtility.writeObject(mUserTemplatesStore, list);
    }

    private final void checkClass(AbstractTemplate template)
    {
        if(template != null && !template.getClass().equals(getTemplateClass())) {
            throw new IllegalArgumentException(template.getClass().getName());
        }
    }

    protected abstract Plugin getPlugin();
    protected abstract Image getShellImage();
    protected abstract IPath getTemplatesPath();
    protected abstract Class getTemplateClass();
    protected abstract AbstractTemplateReaderWriter createReaderWriter();
}
