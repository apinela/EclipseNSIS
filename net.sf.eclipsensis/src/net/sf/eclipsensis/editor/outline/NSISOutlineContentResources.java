/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISService;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class NSISOutlineContentResources implements IEclipseNSISService,  INSISKeywordsListener
{
    private static NSISOutlineContentResources cInstance = null;

    private static final String[] cTypes = {"!define", "!if","!ifdef", "!ifndef", "!ifmacrodef",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                            "!ifnmacrodef", "!endif", "!macro", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "Function", "FunctionEnd", "Section", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "SubSection", "SubSectionEnd", "SectionGroup", "SectionGroupEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "Page", "PageEx", "Pageexend","!include","Var"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String[] cClosingTypes = {"!endif", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    "FunctionEnd", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    "SubSectionEnd", "SectionGroupEnd",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    "Pageexend"}; //$NON-NLS-1$
    private static final File cFilterCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISOutlineContentResources.class.getName()+".Filter.ser"); //$NON-NLS-1$

    private List mTypeList = null;
    private Map mTypes = null;
    private Map mTypeNames = null;
    private Map mImages = null;
    private List mPages = null;
    private Collection mFilteredTypes = null;

    public static NSISOutlineContentResources getInstance()
    {
        return cInstance;
    }

    private void load()
    {
        mTypes.clear();
        mTypeNames.clear();
        mImages.clear();
        for(int i=0; i<cTypes.length; i++) {
            String typeName = NSISKeywords.getInstance().getKeyword(cTypes[i], false);
            if(NSISKeywords.getInstance().isValidKeyword(typeName)) {
                mTypes.put(typeName,cTypes[i]);
                mTypeNames.put(cTypes[i], typeName);
                mImages.put(cTypes[i],EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString(new StringBuffer("outline.").append( //$NON-NLS-1$
                                                                                cTypes[i].toLowerCase().replaceAll("!","")).append(".icon").toString(),null))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            try {
                monitor.beginTask("", 1); //$NON-NLS-1$
                monitor.subTask(EclipseNSISPlugin.getResourceString("loading.outline.message")); //$NON-NLS-1$
                mTypeList = Arrays.asList(cTypes);
                mTypes = new CaseInsensitiveMap();
                mTypeNames = new HashMap();
                mImages = new HashMap();
                mPages = new ArrayList();
                if(IOUtility.isValidFile(cFilterCacheFile)) {
                    try {
                        mFilteredTypes = (Collection)IOUtility.readObject(cFilterCacheFile);
                    }
                    catch (Exception e) {
                        EclipseNSISPlugin.getDefault().log(e);
                        mFilteredTypes = new CaseInsensitiveSet();
                    }
                }
                else {
                    mFilteredTypes = new CaseInsensitiveSet();
                }
                load();
                NSISKeywords.getInstance().addKeywordsListener(this);
                cInstance = this;
            }
            finally {
                monitor.done();
            }
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISKeywords.getInstance().removeKeywordsListener(this);
            if(!Common.isEmptyCollection(mFilteredTypes)) {
                try {
                    IOUtility.writeObject(cFilterCacheFile,mFilteredTypes);
                }
                catch (IOException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else if(IOUtility.isValidFile(cFilterCacheFile)) {
                cFilterCacheFile.delete();
            }
            mTypeList = null;
            mTypes = null;
            mTypeNames = null;
            mImages = null;
            mPages = null;
        }
    }

    void connect(NSISContentOutlinePage page)
    {
        if(!mPages.contains(page)) {
            mPages.add(page);
        }
    }

    void disconnect(NSISContentOutlinePage page)
    {
        mPages.remove(page);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.help.INSISKeywordsListener#keywordsChanged()
     */
    public void keywordsChanged()
    {
        load();
        if(Display.getCurrent() == null) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    refreshPages();
                }
            });
        }
        else {
            refreshPages();
        }
    }

    private void refreshPages()
    {
        for(Iterator iter=mPages.iterator(); iter.hasNext(); ) {
            ((NSISContentOutlinePage)iter.next()).refresh();
        }
    }

    public Collection getTypes()
    {
        return Collections.unmodifiableSet(mTypeNames.keySet());
    }

    public String getType(String typeName)
    {
        return (String)mTypes.get(typeName);
    }

    public int getTypeIndex(String type)
    {
        return (mTypes.containsKey(type)?mTypeList.indexOf(type):-1);
    }

    public String getTypeName(String type)
    {
        return (String)mTypeNames.get(type);
    }

    public Image getImage(String type)
    {
        return (Image)mImages.get(type);
    }

    public Collection getFilteredTypes()
    {
        return Collections.unmodifiableCollection(mFilteredTypes);
    }

    public void setFilteredTypes(Collection collection)
    {
        mFilteredTypes.clear();
        if(collection != null) {
            mFilteredTypes.addAll(collection);
        }
    }

    public boolean isClosingType(String type)
    {
        for (int i = 0; i < cClosingTypes.length; i++) {
            if(Common.stringsAreEqual(type, cClosingTypes[i],true)) {
                return true;
            }
        }
        return false;
    }
}
