/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.util.*;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.CaseInsensitiveProperties;
import net.sf.eclipsensis.util.Common;

public class NSISHelpURLProvider implements INSISConstants
{
    private static Properties cHelpURLs = new CaseInsensitiveProperties();
    
    public static String getHelpURL(String keyWord)
    {
        if(!Common.isEmpty(keyWord)) {
            return cHelpURLs.getProperty(keyWord);
        }
        else {
            return null;
        }
    }
    
    static
    {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NSISHelpURLProvider.class.getName());
            for(Enumeration enum=bundle.getKeys(); enum.hasMoreElements();) {
                String key = (String)enum.nextElement();
                String value = new StringBuffer("/").append(PLUGIN_NAME).append("/").append(
                                                NSIS_REFERENCE_DOCS_PREFIX).append(
                                                (String)bundle.getString(key)).toString();
                cHelpURLs.setProperty(key, value);
            }
        } 
        catch (MissingResourceException x) {
        }
    }
}
