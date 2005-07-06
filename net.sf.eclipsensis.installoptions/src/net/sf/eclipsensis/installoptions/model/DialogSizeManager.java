/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.IPreferenceStore;

public class DialogSizeManager
{
    public static final String PROPERTY_DIALOGSIZES_PREFIX = "dialogsizes."; //$NON-NLS-1$
    public static final String PROPERTY_DIALOGSIZES_COUNT = PROPERTY_DIALOGSIZES_PREFIX + "count"; //$NON-NLS-1$
    private static String SEPARATOR = new String(new char[]{'\u00FF'});
    private static List cDefaultDialogSizes = null;
    private static List cDialogSizes = null;

    private DialogSizeManager()
    {
    }

    public static Dimension getDefaultDialogSizeDimension()
    {
        List list = getDialogSizes();
        if(!Common.isEmptyCollection(list)) {
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                DialogSize element = (DialogSize)iter.next();
                if(element.isDefault()) {
                    return element.getSize();
                }
            }
        }
        return IInstallOptionsConstants.DIALOG_SIZE_DEFAULT;
    }

    public static List getDialogSizes()
    {
        if(cDialogSizes == null) {
            synchronized(DialogSize.class) {
                if(cDialogSizes == null) {
                    IPreferenceStore store = InstallOptionsPlugin.getDefault().getPreferenceStore();
                    String temp = store.getString(PROPERTY_DIALOGSIZES_COUNT); //$NON-NLS-1$
                    if(Common.isEmpty(temp)) {
                        cDialogSizes = new ArrayList();
                        for(Iterator iter=getPresetDialogSizes().iterator(); iter.hasNext(); ) {
                            try {
                                cDialogSizes.add(((DialogSize)iter.next()).clone());
                            }
                            catch (CloneNotSupportedException e) {
                            }
                        }
                    }
                    else {
                        cDialogSizes = loadDialogSizes(store);
                    }
                }
            }
        }
        
        return cDialogSizes;
    }

    public static List getPresetDialogSizes()
    {
        if(cDefaultDialogSizes == null) {
            synchronized(DialogSize.class) {
                if(cDefaultDialogSizes == null) {
                    Object source;
                    try {
                        source = ResourceBundle.getBundle(DialogSize.class.getPackage().getName()+".DialogSizes"); //$NON-NLS-1$
                    }
                    catch(MissingResourceException mre) {
                        source = null;
                    }
                    cDefaultDialogSizes = loadDialogSizes(source);
                }                
            }
        }
        return cDefaultDialogSizes;
    }

    private static String getString(Object source, String name)
    {
        if(source instanceof ResourceBundle) {
            return ((ResourceBundle)source).getString(name);
        }
        else {
            return ((IPreferenceStore)source).getString(name);
        }
    }

    private static List loadDialogSizes(Object source)
    {
        List result = new ArrayList();
        if(source != null) {
            int count = 0;
            try {
                count = Integer.parseInt(getString(source, PROPERTY_DIALOGSIZES_COUNT)); //$NON-NLS-1$
            }
            catch(RuntimeException re) {
                count = 0;
            }
            
            for(int i=0; i<count; i++) {
                try {
                    String text = getString(source,PROPERTY_DIALOGSIZES_PREFIX+i); //$NON-NLS-1$
                    if(!Common.isEmpty(text)) {
                        StringTokenizer st = new StringTokenizer(text,SEPARATOR); //$NON-NLS-1$
                        String name = st.nextToken();
                        boolean isDefault = Boolean.valueOf(st.nextToken()).booleanValue();
                        Dimension dim = new Dimension(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));
                        result.add(new DialogSize(name, isDefault, dim));
                    }
                }
                catch(RuntimeException re) {
                }
            }
        }
        
        return result;
    }

    public static void storeDialogSizes()
    {
        IPreferenceStore store = InstallOptionsPlugin.getDefault().getPreferenceStore();
        int oldCount = 0;
        try {
            oldCount = Integer.parseInt(store.getString(PROPERTY_DIALOGSIZES_COUNT));
        }
        catch(NumberFormatException nfe) {
            oldCount = 0;
        }
        List list = getDialogSizes();
        int newCount = list.size();
        for(int i=0; i<newCount; i++) {
            DialogSize ds = (DialogSize)list.get(i);
            store.setValue(PROPERTY_DIALOGSIZES_PREFIX+i,new StringBuffer(ds.getName()).append(SEPARATOR).append(
                    ds.isDefault()).append(SEPARATOR).append(ds.getSize().width).append(SEPARATOR).append(
                    ds.getSize().height).toString());
        }
        store.setValue(PROPERTY_DIALOGSIZES_COUNT,newCount);
        if(oldCount > newCount) {
            for(int i=newCount; i<oldCount; i++) {
                store.setValue(PROPERTY_DIALOGSIZES_PREFIX+i,""); //$NON-NLS-1$
            }
        }
    }
}
