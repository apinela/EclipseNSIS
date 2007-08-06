/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class NSISHeaderAssociationManager implements IEclipseNSISService/*, IResourceChangeListener*/
{
    private static NSISHeaderAssociationManager cInstance = null;
    private static File cCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),"net.sf.eclipsensis.NSISHeaderAssociations.ser"); //$NON-NLS-1$
    private static IWorkspaceRoot cRoot = ResourcesPlugin.getWorkspace().getRoot();

    private HashMap mScriptMap;
    private HashMap mHeaderMap;

    public static NSISHeaderAssociationManager getInstance()
    {
        return cInstance;
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            load();
//            cRoot.getWorkspace().addResourceChangeListener(this,IResourceChangeEvent.POST_CHANGE);
            cInstance = this;
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
//            cRoot.getWorkspace().removeResourceChangeListener(this);
            store();
        }
    }

    private void store()
    {
        HashMap map = new HashMap();
        for (Iterator iter = mScriptMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            IFile script = (IFile)entry.getKey();
            if(IOUtility.isValidFile(script)) {
                List headers = (List)entry.getValue();
                if(Common.isEmptyCollection(headers)) {
                    iter.remove();
                }
                else {
                    List list = new ArrayList();
                    for (Iterator iterator = headers.iterator(); iterator.hasNext();) {
                        IFile header = (IFile)iterator.next();
                        if(IOUtility.isValidFile(header)) {
                            list.add(header.getFullPath().toString());
                        }
                    }
                    if(list.isEmpty()) {
                        iter.remove();
                    }
                    else {
                        map.put(script.getFullPath().toString(),list);
                    }
                }
            }
            else {
                iter.remove();
            }
        }
        try {
            IOUtility.writeObject(cCacheFile,map);
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    private void load()
    {
        Map cache = null;
        if(IOUtility.isValidFile(cCacheFile)) {
            try {
                cache = (HashMap)IOUtility.readObject(cCacheFile);
            }
            catch (Exception e) {
                cache = null;
            }
        }
        mScriptMap = new HashMap();
        mHeaderMap = new HashMap();

        if (cache != null) {
            for (Iterator iter = cache.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry)iter.next();
                try {
                    IFile script = cRoot.getFile(new Path((String)entry.getKey()));
                    if(IOUtility.isValidFile(script)) {
                        List list = (List)entry.getValue();
                        for (ListIterator iterator = list.listIterator(); iterator.hasNext();) {
                            try {
                                IFile header = cRoot.getFile(new Path((String)iterator.next()));
                                if(IOUtility.isValidFile(header)) {
                                    iterator.set(header);
                                    mHeaderMap.put(header,script);
                                }
                                else {
                                    iterator.remove();
                                }
                            }
                            catch (Exception e) {
                            }
                        }
                        if(!list.isEmpty()) {
                            mScriptMap.put(script,list);
                        }
                    }
                }
                catch(Exception ex) {
                }
            }
        }
    }

    public List getAssociatedHeaders(IFile script)
    {
        List list = (List)mScriptMap.get(script);
        return list != null?Collections.unmodifiableList(list):Collections.EMPTY_LIST;
    }

    public void addAssociatedHeader(IFile script, IFile header)
    {
        associateWithScript(header, script);
    }

    public void removeAssociatedHeader(IFile script, IFile header)
    {
        disassociateFromScript(header);
    }

    public void associateWithScript(IFile header, IFile script)
    {
        if (header != null) {
            IFile oldScript = (IFile)mHeaderMap.get(header);
            if (!Common.objectsAreEqual(oldScript, script)) {
                if (oldScript != null) {
                    List list = (List)mScriptMap.get(oldScript);
                    if (list != null) {
                        list.remove(header);
                        if(list.isEmpty()) {
                            mScriptMap.remove(oldScript);
                        }
                    }
                }
                if (script != null) {
                    List list = (List)mScriptMap.get(script);
                    if (list == null) {
                        list = new ArrayList();
                        mScriptMap.put(script,list);
                    }
                    list.add(header);
                }
            }
            if(script != null) {
                mHeaderMap.put(header,script);
            }
            else {
                mHeaderMap.remove(header);
            }
        }
    }

    public void disassociateFromScript(IFile header)
    {
        associateWithScript(header, null);
    }

    public IFile getAssociatedScript(IFile header)
    {
        if(header != null) {
            return (IFile)mHeaderMap.get(header);
        }
        return null;
    }
//
//    public void resourceChanged(IResourceChangeEvent event)
//    {
//        try {
//            switch (event.getType())
//            {
//                case IResourceChangeEvent.POST_CHANGE:
//                    System.out.println("Resources have changed.");
//                    event.getDelta().accept(new DeltaPrinter());
//                    break;
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    class DeltaPrinter implements IResourceDeltaVisitor {
//        public boolean visit(IResourceDelta delta) {
//           IResource res = delta.getResource();
//           switch (delta.getKind()) {
//              case IResourceDelta.REMOVED:
//                 System.out.print("Resource ");
//                 System.out.print(res.getFullPath());
//                 System.out.println(" was removed.");
//                 break;
//              case IResourceDelta.CHANGED:
//                 System.out.print("Resource ");
//                 System.out.print(res.getFullPath());
//                 System.out.println(" has changed.");
//                 break;
//           }
//           if((delta.getFlags() & IResourceDelta.OPEN) > 0) {
//               boolean open = res.getProject().isOpen();
//              System.out.print("Resource ");
//              System.out.print(res.getFullPath());
//              System.out.println(" was "+(open?"opened":"closed"));
//           }
//           return true; // visit the children
//        }
//     }
}
