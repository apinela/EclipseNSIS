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
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.template.AbstractTemplateManager;
import net.sf.eclipsensis.template.AbstractTemplateReaderWriter;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

public class NSISWizardTemplateManager extends AbstractTemplateManager
{
    private static final Path cPath = new Path("wizard"); //$NON-NLS-1$
    private static Object[][] cPatches;
    
    static {
        cPatches = new Object[1][3];
        cPatches[0][0] = RGB.class.getName().getBytes();
        cPatches[0][1] = new byte[]{(byte)0x86, (byte)0xC9, (byte)0x2B, (byte)0x5B, (byte)0x04, (byte)0x11, (byte)0xCF, (byte)0x1D};
        cPatches[0][2] = new byte[]{(byte)0x2D, (byte)0x38, (byte)0x37, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x30, (byte)0x32};
    }

    public NSISWizardTemplateManager()
    {
        super();
        Collection templates = getDefaultTemplates();
        for (Iterator iter = templates.iterator(); iter.hasNext();) {
            updateTemplateLanguages((NSISWizardTemplate)iter.next());
        }
        templates = getTemplates();
        for (Iterator iter = templates.iterator(); iter.hasNext();) {
            NSISWizardTemplate template = (NSISWizardTemplate)iter.next();
            if(template.getType() != NSISWizardTemplate.TYPE_DEFAULT) {
                updateTemplateLanguages(template);
            }
        }
    }

    private void updateTemplateLanguages(NSISWizardTemplate template)
    {
        NSISWizardSettings settings = template.getSettings();
        if(settings != null) {
            ArrayList languages = settings.getLanguages();
            for(ListIterator iter = languages.listIterator(); iter.hasNext(); ) {
                NSISLanguage lang = (NSISLanguage)iter.next();
                NSISLanguage lang2 = NSISLanguageManager.getInstance().getLanguage(lang.getName());
                if(lang2 != null) {
                    iter.set(lang2);
                }
                else {
                    iter.remove();
                }
            }
        }
    }
    
    protected Map loadUserTemplateStore() throws IOException, ClassNotFoundException
    {
        Map map = null;
        try {
            map = super.loadUserTemplateStore();
        }
        catch(InvalidClassException ice) {
            //This maybe because RGB serialVersionUID changed from Eclipse 3.0 to 3.1
            patchUserTemplateStore(getUserTemplatesStore());
            map = super.loadUserTemplateStore();
        }
        if(map != null) {
            //XXX Remove in 0.9.4
            boolean needsSave = false;
            for(Iterator iter=map.values().iterator(); iter.hasNext(); ) {
                needsSave |= ((NSISWizardTemplate)iter.next()).syncUp();
            }
            if(needsSave) {
                Common.writeObject(getUserTemplatesStore(), map);
            }
        }
        return map;
    }
    
    private static void patchUserTemplateStore(File store) throws IOException
    {
        byte[] contents = Common.loadContentFromFile(store);
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
            Common.writeContentToFile(store, contents);
        }
    }

    protected Plugin getPlugin()
    {
        return EclipseNSISPlugin.getDefault();
    }

    protected Class getTemplateClass()
    {
        return NSISWizardTemplate.class;
    }

    protected IPath getTemplatesPath()
    {
        return cPath;
    }

    protected AbstractTemplateReaderWriter createReaderWriter()
    {
        return NSISWizardTemplateReaderWriter.INSTANCE;
    }

    protected Image getShellImage()
    {
        return EclipseNSISPlugin.getShellImage();
    }
}
