/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.w3c.dom.*;

public class NSISCommandManager
{
    public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String TAG_COMMAND = "command"; //$NON-NLS-1$
    public static final String TAG_VERSION = "version"; //$NON-NLS-1$
    private static Map cCommandsMap =  new HashMap();
    private static Map cParamConstructorsMap = new HashMap();
    private static List cVersionList;

    static {
        Properties props = new Properties();
        try {
            props.load(NSISCommandManager.class.getResourceAsStream("NSISCommandParams.properties")); //$NON-NLS-1$
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        Class[] argTypes = { Node.class };
        for(Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            String className = props.getProperty(name);
            Class clasz;
            try {
                clasz = Class.forName(className);
                Constructor c = clasz.getConstructor(argTypes);
                cParamConstructorsMap.put(name,c);
            }
            catch (Exception e1) {
                EclipseNSISPlugin.getDefault().log(e1);
            }
        }
        
        InputStream is = null;
        try {
            is = new BufferedInputStream(NSISCommandManager.class.getResourceAsStream("NSISCommands.xml")); //$NON-NLS-1$
            Document doc= XMLUtil.loadDocument(is);

            NodeList versionNodes = doc.getElementsByTagName(TAG_VERSION);
            int count = versionNodes.getLength();

            for(int i=0; i<count; i++) {
                Node versionNode = versionNodes.item(i);
                Version version = new Version(XMLUtil.getStringValue(versionNode.getAttributes(),ATTR_NAME));
                Map commandsMap = new HashMap();
                cCommandsMap.put(version, commandsMap);
                NodeList commandNodes = versionNode.getChildNodes();
                int count2 = commandNodes.getLength();
                for(int j=0; j<count2; j++) {
                    Node commandNode = commandNodes.item(j);
                    if(commandNode.getNodeName().equals(TAG_COMMAND)) {
                        NSISCommand command = new NSISCommand(commandNode);
                        commandsMap.put(command.getName(), command);
                    }
                }
            }
            cVersionList = new ArrayList(cCommandsMap.keySet());
            Collections.sort(cVersionList);
        }
        catch (Throwable t) {
            EclipseNSISPlugin.getDefault().log(t);
        }
        finally {
            IOUtility.closeIO(is);
        }
    }
    
    public static NSISCommand getCommand(String name)
    {
        Version version = NSISPreferences.INSTANCE.getNSISVersion();
        for (ListIterator iter = cVersionList.listIterator(cVersionList.size()); iter.hasPrevious();) {
            Version v = (Version)iter.previous();
            if(version.compareTo(v) >= 0) {
                Map commandsMap = (Map)cCommandsMap.get(v);
                if(commandsMap != null) {
                    NSISCommand command = (NSISCommand)commandsMap.get(name);
                    if(command != null) {
                        return command;
                    }
                }
            }
            else {
                continue;
            }
        }
     
        return null;
    }
    
    public static NSISCommand[] getCommands()
    {
        Version version = NSISPreferences.INSTANCE.getNSISVersion();
        Map map = new HashMap();
        for (Iterator iter = cVersionList.iterator(); iter.hasNext();) {
            Version v = (Version)iter.next();
            if(version.compareTo(v) >= 0) {
                Map commandsMap = (Map)cCommandsMap.get(v);
                if(commandsMap != null) {
                    map.putAll(commandsMap);
                }
            }
            else {
                break;
            }
        }
        return (NSISCommand[])map.values().toArray(new NSISCommand[map.size()]);
    }

    static NSISParam createParam(Node paramNode)
    {
        NamedNodeMap attributes = paramNode.getAttributes();
        String type = XMLUtil.getStringValue(attributes,ATTR_TYPE);
        if(!Common.isEmpty(type)) {
            Constructor c = (Constructor)cParamConstructorsMap.get(type);
            try {
                return (NSISParam)c.newInstance(new Node[] {paramNode});
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
        return null;
    }
    
    private NSISCommandManager()
    {
    }
}
