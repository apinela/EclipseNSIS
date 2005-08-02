/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.IEclipseNSISService;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

public class NSISOutlineContentResources implements IEclipseNSISService,  INSISKeywordsListener
{
    public static NSISOutlineContentResources INSTANCE = null;  //$NON-NLS-1$
    
    private static final String[] cTypes = {"!define", "!ifdef", "!ifndef", "!ifmacrodef",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "!ifnmacrodef", "!endif", "!macro", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "Function", "FunctionEnd", "Section", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "SubSection", "SubSectionEnd", "Page", "PageEx",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            "Pageexend","!include"}; //$NON-NLS-1$ //$NON-NLS-2$

    private final List mTypeList = Arrays.asList(cTypes);
    private final Map mTypes = new CaseInsensitiveMap();
    private final Map mTypeNames = new HashMap();
    private final Map mImages = new HashMap();
    private final List mPages = new ArrayList();
    
    private void load()
    {
        mTypes.clear();
        mTypeNames.clear();
        mImages.clear();
        for(int i=0; i<cTypes.length; i++) {
            String typeName = NSISKeywords.INSTANCE.getKeyword(cTypes[i]);
            if(NSISKeywords.INSTANCE.isValidKeyword(typeName)) {
                mTypes.put(typeName,cTypes[i]);
                mTypeNames.put(cTypes[i], typeName);
                mImages.put(cTypes[i],EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString(new StringBuffer("outline.").append( //$NON-NLS-1$
                                                                                cTypes[i].toLowerCase().replaceAll("!","")).append(".icon").toString(),null))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    public void start(IProgressMonitor monitor)
    {
        monitor.subTask(EclipseNSISPlugin.getResourceString("loading.outline.message")); //$NON-NLS-1$
        load();
        NSISKeywords.INSTANCE.addKeywordsListener(this);
        INSTANCE = this;
    }

    public void stop(IProgressMonitor monitor)
    {
        INSTANCE = null;
        NSISKeywords.INSTANCE.removeKeywordsListener(this);
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
        for(Iterator iter=mPages.iterator(); iter.hasNext(); ) {
            ((NSISContentOutlinePage)iter.next()).refresh();
        }
    }

    public String getType(String typeName)
    {
        return (String)mTypes.get(typeName);
    }

    public int getTypeIndex(String type)
    {
        return mTypeList.indexOf(type);
    }
    
    public String getTypeName(String type)
    {
        return (String)mTypeNames.get(type);
    }
    
    public Image getImage(String type)
    {
        return (Image)mImages.get(type);
    }
}
