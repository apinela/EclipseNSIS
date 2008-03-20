/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.net.*;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
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

        mReaderWriter = createReaderWriter();

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

        mTemplates = new ArrayList();
        loadTemplates();
    }

    /**
     *
     */
    protected final void loadTemplates()
    {
        mTemplates.clear();
        Map map = new HashMap(mDefaultTemplatesMap);

        try {
            List list = loadUserTemplateStore();
            if(list != null) {
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    ITemplate template = (ITemplate)iter.next();
                    if(template.getType() == ITemplate.TYPE_CUSTOM) {
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
                        ITemplate template = (ITemplate)iter.next();
                        defaults.put(template.getName(),template);
                    }
                }
                list = new ArrayList();
                for(Iterator iter = ((Map)obj).values().iterator(); iter.hasNext(); ) {
                    ITemplate template = (ITemplate)iter.next();
                    switch(template.getType()) {
                        case ITemplate.TYPE_DEFAULT:
                            continue;
                        case ITemplate.TYPE_CUSTOM:
                            ITemplate t = (ITemplate)defaults.get(template.getName());
                            if(t == null) {
                                template.setType(ITemplate.TYPE_USER);
                            }
                            else {
                                template.setId(t.getId());
                                if(templatesAreEqual(t,template)) {
                                   continue;
                                }
                            }
                        case ITemplate.TYPE_USER:
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

    public ITemplate getTemplate(String id)
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            ITemplate template = (ITemplate)iter.next();
            if(Common.stringsAreEqual(template.getId(),id)) {
                return template;
            }
        }
        return null;
    }

    public Collection getTemplates()
    {
        return Collections.unmodifiableCollection(mTemplates);
    }

    public Collection getDefaultTemplates()
    {
        return Collections.unmodifiableCollection(mDefaultTemplatesMap.values());
    }

    public boolean addTemplate(final ITemplate template)
    {
        checkClass(template);
        template.setId(null);
        template.setType(ITemplate.TYPE_USER);
        template.setDeleted(false);
        mTemplates.add(template);
        return true;
    }

    protected boolean containsTemplate(ITemplate template)
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            ITemplate t = (ITemplate)iter.next();
            if(templatesAreEqual(t,template)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeTemplate(ITemplate template)
    {
        checkClass(template);
        if(containsTemplate(template)) {
            switch(template.getType()) {
                case ITemplate.TYPE_DEFAULT:
                case ITemplate.TYPE_CUSTOM:
                    ITemplate t = (ITemplate)template.clone();
                    t.setType(ITemplate.TYPE_CUSTOM);
                    t.setDeleted(true);
                    mTemplates.add(t);
                case ITemplate.TYPE_USER:
                    mTemplates.remove(template);
            }
            return true;
        }
        return false;
    }

    public boolean updateTemplate(ITemplate oldTemplate, final ITemplate template)
    {
        checkClass(oldTemplate);
        checkClass(template);
        if(containsTemplate(oldTemplate)) {
            mTemplates.remove(oldTemplate);

            if(oldTemplate.getType() != ITemplate.TYPE_USER) {
                ITemplate defaultTemplate = (ITemplate)mDefaultTemplatesMap.get(oldTemplate.getId());
                if(defaultTemplate != null) {
                    template.setType(templatesAreEqual(template,defaultTemplate)?ITemplate.TYPE_DEFAULT:ITemplate.TYPE_CUSTOM);
                }
                else {
                    template.setType(ITemplate.TYPE_USER);
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
            restore((ITemplate)iter.next());
        }
    }

    protected boolean restore(ITemplate template)
    {
        checkClass(template);
        if(template.getType() == ITemplate.TYPE_CUSTOM && template.isDeleted()) {
            template.setDeleted(false);
            ITemplate defaultTemplate = (ITemplate)mDefaultTemplatesMap.get(template.getId());
            if(templatesAreEqual(template,defaultTemplate)) {
                template.setType(ITemplate.TYPE_DEFAULT);
            }
            return true;
        }
        return false;
    }

    public boolean canRestore()
    {
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            ITemplate template = (ITemplate)iter.next();
            checkClass(template);
            if(template.getType() == ITemplate.TYPE_CUSTOM && template.isDeleted()) {
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

    public ITemplate revert(ITemplate template)
    {
        checkClass(template);
        if(template.getType() == ITemplate.TYPE_CUSTOM) {
            ITemplate defaultTemplate = (ITemplate)mDefaultTemplatesMap.get(template.getId());
            checkClass(defaultTemplate);
            mTemplates.remove(template);
            mTemplates.add(defaultTemplate);
            return defaultTemplate;
        }
        return null;
    }

    public boolean canRevert(ITemplate template)
    {
        checkClass(template);
        return (containsTemplate(template) && (template.getType() == ITemplate.TYPE_CUSTOM));
    }

    public void discard()
    {
        loadTemplates();
    }

    /**
     * @throws IOException
     *
     */
    public void save() throws IOException
    {
        Map map = new LinkedHashMap();
        for (Iterator iter = mTemplates.iterator(); iter.hasNext();) {
            ITemplate template = (ITemplate)iter.next();
            checkClass(template);

            if(System.getProperty("manage.default.templates") != null) { //$NON-NLS-1$
                template.setType(ITemplate.TYPE_DEFAULT);
                map.put(template.getName(),template);
            }
            else {
                if(template.getType() != ITemplate.TYPE_DEFAULT) {
                    map.put(template,template);
                }
            }
        }
        if(System.getProperty("manage.default.templates") != null) { //$NON-NLS-1$
            try {
                mDefaultTemplatesMap.clear();
                mDefaultTemplatesMap.putAll(map);
                mTemplates.clear();
                URI uri = new URI(FileLocator.toFileURL(mDefaultTemplatesStore).toExternalForm());
                File file = new File(uri);
                IOUtility.writeObject(file, new HashMap(mDefaultTemplatesMap));
            }
            catch (URISyntaxException e) {
                throw new IOException(e.getMessage());
            }
        }
        else {
            IOUtility.writeObject(mUserTemplatesStore, new ArrayList(map.values()));
        }
    }

    private final void checkClass(ITemplate template)
    {
        if(template != null && !getTemplateClass().isAssignableFrom(template.getClass())) {
            throw new IllegalArgumentException(template.getClass().getName());
        }
    }

    private boolean templatesAreEqual(ITemplate t1, ITemplate t2)
    {
        checkClass(t1);
        checkClass(t2);
        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        return t1.isEqualTo(t2);
    }

    protected abstract Plugin getPlugin();
    protected abstract Image getShellImage();
    protected abstract IPath getTemplatesPath();
    protected abstract Class getTemplateClass();
    protected abstract AbstractTemplateReaderWriter createReaderWriter();
}
