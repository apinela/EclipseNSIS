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
import java.net.URL;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class NSISWizardTemplateManager
{
    private static final File cUserTemplatesStore;
    private static final URL cDefaultTemplatesStore;

    private Map mTemplatesMap = new HashMap();
    private Map mDefaultTemplatesMap = new HashMap();
    
    private static Object[][] cPatches;
    
    static {
        cPatches = new Object[1][3];
        cPatches[0][0] = RGB.class.getName().getBytes();
        cPatches[0][1] = new byte[]{(byte)0x86, (byte)0xC9, (byte)0x2B, (byte)0x5B, (byte)0x04, (byte)0x11, (byte)0xCF, (byte)0x1D};
        cPatches[0][2] = new byte[]{(byte)0x2D, (byte)0x38, (byte)0x37, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x30, (byte)0x32};

        String fileName = NSISWizardTemplateManager.class.getName()+".Templates.ser"; //$NON-NLS-1$

        cDefaultTemplatesStore = EclipseNSISPlugin.getDefault().getBundle().getResource("/wizard/"+fileName);
        
        File parentFolder = EclipseNSISPlugin.getPluginStateLocation();
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
        
        cUserTemplatesStore = new File(location,fileName);
    }

    private void loadDefaultTemplateStore()
    {
        Map map = null;
        if(cDefaultTemplatesStore != null) {
            try {
                InputStream stream = cDefaultTemplatesStore.openStream();
                map = (Map)Common.readObject(stream);
            }
            catch (Exception e) {
                e.printStackTrace();
                map = new HashMap();
            }
        }

        if(map == null) {
            map = new HashMap();
        }
        
        mDefaultTemplatesMap =  map;
    }

    private void loadUserTemplateStore()
    {
        Map map = null;
        if(cUserTemplatesStore != null) {
            if(cUserTemplatesStore.exists() && cUserTemplatesStore.isFile()) {
                try {
                    try {
                        map = (Map)Common.readObject(cUserTemplatesStore);
                    }
                    catch(InvalidClassException ice) {
                        //This is because RGB serialVersionUID was changed (for whatever reason)
                        patchUserTemplateStore();
                        map = (Map)Common.readObject(cUserTemplatesStore);
                    }
                    if(mTemplatesMap == null) {
                        mTemplatesMap = new HashMap();
                    }
                    mTemplatesMap.putAll(map);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void patchUserTemplateStore() throws IOException
    {
        byte[] contents = Common.loadContentFromFile(cUserTemplatesStore);
        boolean changed = false;
        for(int i=0; i<cPatches.length; i++) {
            for(int j=0; j<contents.length; j++) {
                byte[] classBytes = (byte[])cPatches[i][0];
                byte[] oldUIDBytes = (byte[])cPatches[i][1];
                byte[] newUIDBytes = (byte[])cPatches[i][2];
                if(contents[j] == (classBytes)[0]) {
                    int l=1;
                    int k=j+1;
                    for(; k<contents.length && l < classBytes.length ; k++, l++) {
                        if(contents[k] != classBytes[l]) {
                            break;
                        }
                    }
                    if(l == classBytes.length && k+oldUIDBytes.length < contents.length) {
                        l = 0;
                        for(; k<contents.length && l < oldUIDBytes.length ; k++, l++) {
                            if(contents[k] != oldUIDBytes[l]) {
                                break;
                            }
                        }
                        if(l == oldUIDBytes.length) {
                            changed = true;
                            byte[] newContents = new byte[contents.length + (newUIDBytes.length-oldUIDBytes.length)];
                            System.arraycopy(contents,0,newContents,0,k-oldUIDBytes.length);
                            System.arraycopy(newUIDBytes,0,newContents,k-oldUIDBytes.length,newUIDBytes.length);
                            System.arraycopy(contents,k,newContents,
                                    k-oldUIDBytes.length+newUIDBytes.length,contents.length-k);
                            contents = newContents;
                            break;
                        }
                    }
                }
            }
        }
        if(changed) {
            //save it back
            Common.writeContentToFile(cUserTemplatesStore, contents);
        }
    }
    
    public NSISWizardTemplateManager()
    {
        loadDefaultTemplateStore();
        mTemplatesMap = new HashMap(mDefaultTemplatesMap);
        loadUserTemplateStore();
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
