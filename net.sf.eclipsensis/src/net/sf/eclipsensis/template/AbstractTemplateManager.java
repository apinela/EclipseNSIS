/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractTemplateManager
{
    private File mUserTemplatesStore;
    private URL mDefaultTemplatesStore;

    private Map mTemplatesMap;
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
            if(location.exists() && location.isFile()) {
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
            mDefaultTemplatesMap = new LinkedHashMap();
        }
        finally {
            if(mDefaultTemplatesMap == null) {
                mDefaultTemplatesMap = new LinkedHashMap();
            }
        }

        mTemplatesMap = new LinkedHashMap(mDefaultTemplatesMap);
        try {
            Map map = loadUserTemplateStore();
            if(map != null) {
                mTemplatesMap.putAll(map);
            }
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }

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

    protected Map loadUserTemplateStore() throws IOException, ClassNotFoundException
    {
        Map map = null;
        if(mUserTemplatesStore != null) {
            if(mUserTemplatesStore.exists() && mUserTemplatesStore.isFile()) {
                map = (Map)IOUtility.readObject(mUserTemplatesStore, getClass().getClassLoader());
            }
        }
        return map;
    }

    protected URL getDefaultTemplatesStore()
    {
        return mDefaultTemplatesStore;
    }

    protected File getUserTemplatesStore()
    {
        return mUserTemplatesStore;
    }

    public AbstractTemplate getTemplate(String name)
    {
        return (AbstractTemplate)mTemplatesMap.get(name);
    }

    public Collection getTemplates()
    {
        return mTemplatesMap.values();
    }

    public Collection getDefaultTemplates()
    {
        return mDefaultTemplatesMap.values();
    }

    public boolean addTemplate(final AbstractTemplate template)
    {
        checkClass(template);
        AbstractTemplate oldTemplate = (AbstractTemplate)mTemplatesMap.get(template.getName());
        if(oldTemplate != null) {
            int type = oldTemplate.getType();
            if(type != AbstractTemplate.TYPE_CUSTOM || !oldTemplate.isDeleted()) {
                final boolean[] rv = { true };
                Display.getDefault().syncExec(new Runnable() {
                    public void run()
                    {
                        if(!Common.openConfirm(null,EclipseNSISPlugin.getFormattedString("template.save.confirm", //$NON-NLS-1$
                                                     new String[]{template.getName()}),getShellImage())) {
                            rv[0] = false;
                        }
                    }
                });
                if(!rv[0]) {
                    return false;
                }
                else {
                    switch(oldTemplate.getType()) {
                        case AbstractTemplate.TYPE_DEFAULT:
                            template.setType(AbstractTemplate.TYPE_CUSTOM);
                            break;
                        default:
                            template.setType(oldTemplate.getType());
                            break;
                    }
                }
            }
            else {
                template.setType(AbstractTemplate.TYPE_CUSTOM);
            }
        }
        else {
            template.setType(AbstractTemplate.TYPE_USER);
        }
        template.setDeleted(false);
        mTemplatesMap.put(template.getName(),template);
        return true;
    }

    public boolean removeTemplate(final AbstractTemplate template)
    {
        checkClass(template);
        if(mTemplatesMap.containsKey(template.getName())) {
            switch(template.getType()) {
                case AbstractTemplate.TYPE_DEFAULT:
                    template.setType(AbstractTemplate.TYPE_CUSTOM);
                case AbstractTemplate.TYPE_CUSTOM:
                    template.setDeleted(true);
                    break;
                case AbstractTemplate.TYPE_USER:
                    mTemplatesMap.remove(template.getName());
            }
            return true;
        }
        return false;
    }


    public boolean updateTemplate(AbstractTemplate oldTemplate, final AbstractTemplate template)
    {
        checkClass(oldTemplate);
        checkClass(template);
        if(mTemplatesMap.containsKey(oldTemplate.getName())) {
            if(!oldTemplate.getName().equals(template.getName())) {
                AbstractTemplate otherTemplate = (AbstractTemplate)mTemplatesMap.get(template.getName());
                if(otherTemplate != null) {
                    Display display = Display.getDefault();
                    final boolean[] rv = { true };
                    display.syncExec(new Runnable() {
                        public void run()
                        {
                            if(!Common.openConfirm(null,EclipseNSISPlugin.getFormattedString("template.save.confirm", //$NON-NLS-1$
                                                         new String[]{template.getName()}),getShellImage())) {
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
            if(oldTemplate.getType() == AbstractTemplate.TYPE_DEFAULT) {
                template.setType(AbstractTemplate.TYPE_CUSTOM);
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
            AbstractTemplate template = (AbstractTemplate)iter.next();
            restore(template);
        }
    }

    protected boolean restore(AbstractTemplate template)
    {
        checkClass(template);
        if(template.getType() == AbstractTemplate.TYPE_CUSTOM && template.isDeleted()) {
            template.setDeleted(false);
            return true;
        }
        return false;
    }

    public boolean canRestore()
    {
        for (Iterator iter = mTemplatesMap.values().iterator(); iter.hasNext();) {
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
        mTemplatesMap.clear();
        mTemplatesMap.putAll(mDefaultTemplatesMap);
    }

    public AbstractTemplate revert(AbstractTemplate template)
    {
        checkClass(template);
        if(template.getType() == AbstractTemplate.TYPE_CUSTOM) {
            AbstractTemplate defaultTemplate = (AbstractTemplate)mDefaultTemplatesMap.get(template.getName());
            checkClass(defaultTemplate);
            mTemplatesMap.put(defaultTemplate.getName(),defaultTemplate);
            return defaultTemplate;
        }
        return null;
    }

    public boolean canRevert(AbstractTemplate template)
    {
        checkClass(template);
        return (mTemplatesMap.containsKey(template.getName()) && (template.getType() == AbstractTemplate.TYPE_CUSTOM));
    }

    /**
     * @throws IOException
     *
     */
    public void save() throws IOException
    {
        Map map = new LinkedHashMap();
        for (Iterator iter = mTemplatesMap.values().iterator(); iter.hasNext();) {
            AbstractTemplate template = (AbstractTemplate)iter.next();
            checkClass(template);
            if(template.getType() != AbstractTemplate.TYPE_DEFAULT) {
                map.put(template.getName(),template);
            }
        }
        IOUtility.writeObject(mUserTemplatesStore, map);
    }

    private void checkClass(AbstractTemplate template)
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
