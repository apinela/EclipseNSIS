/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class NSISOutlineContentResources implements IEclipseNSISService,  INSISKeywordsListener
{
    private static NSISOutlineContentResources cInstance = null;

    private static final String[] cTypes = {"!define", "!if","!ifdef", "!ifndef", "!ifmacrodef",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                            "!ifmacrondef", "!else", "!else if","!else ifdef", "!else ifndef", "!else ifmacrodef",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                                            "!else ifmacrondef", "!endif", "!macro", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "Function", "FunctionEnd", "Section", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "SubSection", "SubSectionEnd", "SectionGroup", "SectionGroupEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "Page", "PageEx", "PageExEnd","!include","Var", "Name","#label","#global label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    private static final String[] cClosingTypes = {"!endif", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    "FunctionEnd", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    "SubSectionEnd", "SectionGroupEnd",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    "PageExEnd","Name"}; //$NON-NLS-1$ //$NON-NLS-2$
    private static final File cFilterCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISOutlineContentResources.class.getName()+".Filter.ser"); //$NON-NLS-1$

    private List<String> mTypeList = null;
    private Map<String, String> mTypes = null;
    private Map<String, String> mTypeNames = null;
    private Map<String, Image> mImages = null;
    private List<NSISContentOutlinePage> mPages = null;
    private Collection<String> mFilteredTypes = null;

    public static NSISOutlineContentResources getInstance()
    {
        return cInstance;
    }

    private void load()
    {
        mTypeList.clear();
        mTypes.clear();
        mTypeNames.clear();
        mImages.clear();
        for(int i=0; i<cTypes.length; i++) {
            String type = cTypes[i];
            String typeName;
            if(type.charAt(0) == '#') {
                type = typeName = type.substring(1);
                mTypeList.add(type);
            }
            else {
                mTypeList.add(type);
                typeName = NSISKeywords.getInstance().getKeyword(type, false);
                if(!NSISKeywords.getInstance().isValidKeyword(typeName)) {
                    continue;
                }
            }
            mTypes.put(typeName,type);
            mTypeNames.put(type, typeName);
            mImages.put(type,EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString(new StringBuffer("outline.").append( //$NON-NLS-1$
                    type.toLowerCase().replaceAll("!","").replaceAll(" ",".")).append(".icon").toString(),null))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
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
                mTypeList = new ArrayList<String>();
                mTypes = new CaseInsensitiveMap<String>();
                mTypeNames = new HashMap<String, String>();
                mImages = new HashMap<String, Image>();
                mPages = new ArrayList<NSISContentOutlinePage>();
                if(IOUtility.isValidFile(cFilterCacheFile)) {
                    try {
                        mFilteredTypes = IOUtility.readObject(cFilterCacheFile);
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
        for(Iterator<NSISContentOutlinePage> iter=mPages.iterator(); iter.hasNext(); ) {
            iter.next().refresh();
        }
    }

    public Collection<String> getTypes()
    {
        return Collections.unmodifiableSet(mTypeNames.keySet());
    }

    public String getType(String typeName)
    {
        if(typeName.endsWith(":")) //$NON-NLS-1$
        {
            //label
            return mTypes.get("label"); //$NON-NLS-1$
        }
        return mTypes.get(typeName);
    }

    public int getTypeIndex(String type)
    {
        return (mTypes.containsKey(type)?mTypeList.indexOf(type):-1);
    }

    public String getTypeName(String type)
    {
        return mTypeNames.get(type);
    }

    public Image getImage(String type)
    {
        return mImages.get(type);
    }

    public Collection<String> getFilteredTypes()
    {
        return Collections.unmodifiableCollection(mFilteredTypes);
    }

    public void setFilteredTypes(Collection<String> collection)
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
