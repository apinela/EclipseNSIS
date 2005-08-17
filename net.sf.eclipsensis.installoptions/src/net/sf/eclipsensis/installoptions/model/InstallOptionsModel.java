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

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class InstallOptionsModel implements IPropertyChangeListener
{
    public static final String TYPE_DIALOG = "Dialog"; //$NON-NLS-1$
    public static final String TYPE_LABEL = "Label"; //$NON-NLS-1$
    public static final String TYPE_LINK = "Link"; //$NON-NLS-1$
    public static final String TYPE_BUTTON = "Button"; //$NON-NLS-1$
    public static final String TYPE_CHECKBOX = "CheckBox"; //$NON-NLS-1$
    public static final String TYPE_RADIOBUTTON = "RadioButton"; //$NON-NLS-1$
    public static final String TYPE_FILEREQUEST = "FileRequest"; //$NON-NLS-1$
    public static final String TYPE_DIRREQUEST = "DirRequest"; //$NON-NLS-1$
    public static final String TYPE_BITMAP = "Bitmap"; //$NON-NLS-1$
    public static final String TYPE_ICON = "Icon"; //$NON-NLS-1$
    public static final String TYPE_GROUPBOX = "GroupBox"; //$NON-NLS-1$
    public static final String TYPE_TEXT = "Text"; //$NON-NLS-1$
    public static final String TYPE_PASSWORD = "Password"; //$NON-NLS-1$
    public static final String TYPE_COMBOBOX = "Combobox"; //$NON-NLS-1$
    public static final String TYPE_DROPLIST = "DropList"; //$NON-NLS-1$
    public static final String TYPE_LISTBOX = "Listbox"; //$NON-NLS-1$
    
    public static final String SECTION_SETTINGS = "Settings"; //$NON-NLS-1$
    public static final String SECTION_FIELD_PREFIX = "Field"; //$NON-NLS-1$
    public static final Pattern SECTION_FIELD_PATTERN = Pattern.compile(SECTION_FIELD_PREFIX+" ([1-9][0-9]*)",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    public static final MessageFormat SECTION_FIELD_FORMAT = new MessageFormat(SECTION_FIELD_PREFIX+" {0}"); //$NON-NLS-1$

    public static final String PROPERTY_TYPE = "Type"; //$NON-NLS-1$
    public static final String PROPERTY_LEFT = "Left"; //$NON-NLS-1$
    public static final String PROPERTY_TOP = "Top"; //$NON-NLS-1$
    public static final String PROPERTY_RIGHT = "Right"; //$NON-NLS-1$
    public static final String PROPERTY_BOTTOM = "Bottom"; //$NON-NLS-1$
    public static final String PROPERTY_NUMFIELDS = "NumFields"; //$NON-NLS-1$
    public static final String PROPERTY_POSITION = "Position"; //$NON-NLS-1$
    public static final String PROPERTY_INDEX = "Index"; //$NON-NLS-1$
    public static final String PROPERTY_FLAGS = "Flags"; //$NON-NLS-1$
    public static final String PROPERTY_TEXT = "Text"; //$NON-NLS-1$
    public static final String PROPERTY_STATE = "State"; //$NON-NLS-1$
    public static final String PROPERTY_MAXLEN = "MaxLen"; //$NON-NLS-1$
    public static final String PROPERTY_MINLEN = "MinLen"; //$NON-NLS-1$
    public static final String PROPERTY_VALIDATETEXT = "ValidateText"; //$NON-NLS-1$
    public static final String PROPERTY_CHILDREN = "Children"; //$NON-NLS-1$
    public static final String PROPERTY_TITLE =  "Title"; //$NON-NLS-1$
    public static final String PROPERTY_CANCEL_ENABLED = "CancelEnabled"; //$NON-NLS-1$
    public static final String PROPERTY_CANCEL_SHOW = "CancelShow"; //$NON-NLS-1$;
    public static final String PROPERTY_BACK_ENABLED = "BackEnabled"; //$NON-NLS-1$;
    public static final String PROPERTY_CANCEL_BUTTON_TEXT = "CancelButtonText"; //$NON-NLS-1$;
    public static final String PROPERTY_NEXT_BUTTON_TEXT = "NextButtonText"; //$NON-NLS-1$;
    public static final String PROPERTY_BACK_BUTTON_TEXT = "BackButtonText"; //$NON-NLS-1$;
    public static final String PROPERTY_RECT = "Rect"; //$NON-NLS-1$;
    public static final String PROPERTY_RTL = "RTL"; //$NON-NLS-1$;
    public static final String PROPERTY_FILTER = "Filter"; //$NON-NLS-1$;
    public static final String PROPERTY_ROOT = "Root"; //$NON-NLS-1$;
    public static final String PROPERTY_TXTCOLOR = "TxtColor"; //$NON-NLS-1$;
    public static final String PROPERTY_LISTITEMS = "ListItems"; //$NON-NLS-1$;

    public static final String FLAGS_DISABLED = "DISABLED"; //$NON-NLS-1$
    public static final String FLAGS_RIGHT = "RIGHT"; //$NON-NLS-1$
    public static final String FLAGS_ONLY_NUMBERS = "ONLY_NUMBERS"; //$NON-NLS-1$
    public static final String FLAGS_MULTILINE = "MULTILINE"; //$NON-NLS-1$
    public static final String FLAGS_NOWORDWRAP = "NOWORDWRAP"; //$NON-NLS-1$
    public static final String FLAGS_HSCROLL = "HSCROLL"; //$NON-NLS-1$
    public static final String FLAGS_VSCROLL = "VSCROLL"; //$NON-NLS-1$
    public static final String FLAGS_READONLY = "READONLY"; //$NON-NLS-1$
    public static final String FLAGS_MULTISELECT = "MULTISELECT"; //$NON-NLS-1$
    public static final String FLAGS_EXTENDEDSELECT = "EXTENDEDSELCT"; //$NON-NLS-1$
    
    public static final String STATE_UNCHECKED="0"; //$NON-NLS-1$
    public static final String STATE_CHECKED="1"; //$NON-NLS-1$
    
    public static final String OPTION_DEFAULT=""; //$NON-NLS-1$
    public static final String OPTION_NO="0"; //$NON-NLS-1$
    public static final String OPTION_YES="1"; //$NON-NLS-1$

    public static InstallOptionsModel INSTANCE = new InstallOptionsModel();
    
    private List mListeners = new ArrayList();
    private Set mDialogSettings = new CaseInsensitiveSet();
    private Map mCachedControlTypes = new CaseInsensitiveMap();
    private Map mControlTypes = new CaseInsensitiveMap();
    private Set mControlRequiredSettings = new CaseInsensitiveSet();
    private int mMaxLength;
    
    /**
     * 
     */
    private InstallOptionsModel()
    {
        super();
        loadModel();
        NSISPreferences.INSTANCE.getPreferenceStore().addPropertyChangeListener(this);
    }
    
    public int getMaxLength()
    {
        return mMaxLength;
    }

    public void addModelListener(IModelListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public void removeModelListener(IModelListener listener)
    {
        mListeners.remove(listener);
    }
    
    private void notifyListeners()
    {
        for (Iterator iter = mListeners.iterator(); iter.hasNext();) {
            ((IModelListener)iter.next()).modelChanged();
        }
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
            loadModel();
        }
    }

    private void loadModel()
    {
        List controlRequiredSettings = new ArrayList();
        List controlTypes = new ArrayList();
        List dialogSettings = new ArrayList();
        Map controlSettings = new CaseInsensitiveMap();
        Map controlFlags = new CaseInsensitiveMap();
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(getClass().getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }

        if(bundle != null) {
            Version nsisVersion = getNSISVersion();
            HashMap versionMap = new HashMap();
            for(Enumeration enum=bundle.getKeys(); enum.hasMoreElements();) {
                String key = (String)enum.nextElement();
                int n = key.indexOf('#');
                if(n > 1) {
                    String name = key.substring(0,n);
                    Version version = new Version(key.substring(n+1));
                    if(nsisVersion.compareTo(version) >= 0) {
                        ArrayList list = (ArrayList)versionMap.get(version);
                        if(list == null) {
                            list = new ArrayList();
                            versionMap.put(version, list);
                        }
                        list.add(new String[]{name,key});
                    }
                }
            }
            ArrayList versionList = new ArrayList(versionMap.keySet());
            Collections.sort(versionList);
            
            for (Iterator iter = versionList.iterator(); iter.hasNext();) {
                Version version = (Version)iter.next();
                ArrayList nameList = (ArrayList)versionMap.get(version);
                for (Iterator iter2 = nameList.iterator(); iter2.hasNext();) {
                    String[] element = (String[])iter2.next();
                    String name = element[0];
                    String key = element[1];
                    
                    String[] values = Common.loadArrayProperty(bundle,key);
                    List list = null;
                    if(name.equals("Dialog.Settings")) { //$NON-NLS-1$
                        list = dialogSettings;
                    }
                    else if(name.equals("Dialog.Control.Types")) { //$NON-NLS-1$
                        list = controlTypes;
                    }
                    else if(name.equals("Control.Required.Settings")) { //$NON-NLS-1$
                        list = controlRequiredSettings;
                    }
                    else {
                        int n = name.indexOf("."); //$NON-NLS-1$
                        if(n > 0) {
                            String type = name.substring(0,n);
                            Map map;
                            if(name.endsWith(".Settings")) { //$NON-NLS-1$
                                map = controlSettings;
                            }
                            else if(name.endsWith(".Flags")) { //$NON-NLS-1$
                                map = controlFlags;
                            }
                            else {
                                map = null;
                            }
                            if(map != null) {
                                list = (List)map.get(type);
                                if(list == null) {
                                    list = new ArrayList();
                                    map.put(type,list);
                                }
                            }
                        }
                    }
                    if(list != null) {
                        processValues(list,values);
                    }
                }
            }
        }

        mDialogSettings.clear();
        mDialogSettings.addAll(dialogSettings);
        
        mControlRequiredSettings.clear();
        mControlRequiredSettings.addAll(controlRequiredSettings);

        mCachedControlTypes.putAll(mControlTypes);
        mControlTypes.clear();
        for (Iterator iter = controlTypes.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            InstallOptionsModelTypeDef typeDef = (InstallOptionsModelTypeDef)mCachedControlTypes.remove(type);
            if(typeDef == null) {
                String name = bundle.getString(type+".Name"); //$NON-NLS-1$
                String description = bundle.getString(type+".Description"); //$NON-NLS-1$
                String largeIcon = bundle.getString(type+".LargeIcon"); //$NON-NLS-1$
                String smallIcon = bundle.getString(type+".SmallIcon"); //$NON-NLS-1$
                String model = bundle.getString(type+".Model"); //$NON-NLS-1$
                String part = bundle.getString(type+".Part"); //$NON-NLS-1$
                typeDef = new InstallOptionsModelTypeDef(type, name, description, smallIcon, largeIcon, model, part);
            }
            mControlTypes.put(type,typeDef);
            List list = (List)controlSettings.get(type);
            if(list == null) {
                list = new ArrayList();
            }
            list.addAll(0,controlRequiredSettings);
            typeDef.setSettings(list);
            typeDef.setFlags((List)controlFlags.get(type));
        }
        
        try {
            mMaxLength = Integer.parseInt(NSISPreferences.INSTANCE.getNSISOption("NSIS_MAX_STRLEN")); //$NON-NLS-1$
        }
        catch(Exception ex){
            mMaxLength = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
        }

        notifyListeners();
    }

    public Version getNSISVersion()
    {
        Version nsisVersion;
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            nsisVersion = NSISPreferences.INSTANCE.getNSISVersion();
        }
        else {
            nsisVersion = NSISValidator.MINIMUM_NSIS_VERSION;
        }
        return nsisVersion;
    }
    
    private void processValues(List list, String[] values)
    {
        for (int i = 0; i < values.length; i++) {
            int n = values[i].indexOf('~');
            if(values[i].charAt(0) == '-') {
                list.remove(values[i].substring(1));
            }
            else if(n > 0) {
                String oldValue = values[i].substring(0,n);
                String newValue = values[i].substring(n+1);
                int index = list.indexOf(oldValue);
                if(index >= 0) {
                    list.remove(oldValue);
                    list.add(index, newValue);
                }
                else {
                    list.add(newValue);
                }
            }
            else {
                if(values[i].charAt(0) == '+') {
                    values[i] = values[i].substring(1);
                }
                list.add(values[i]);
            }
        }
    }
    
    public InstallOptionsModelTypeDef getControlTypeDef(String type)
    {
        return (InstallOptionsModelTypeDef)mControlTypes.get(type);
    }
    
    public Collection getControlRequiredSettings()
    {
        return Collections.unmodifiableSet(mControlRequiredSettings);
    }
    
    public Collection getControlTypeDefs()
    {
        return Collections.unmodifiableCollection(mControlTypes.values());
    }
    
    public Collection getDialogSettings()
    {
        return Collections.unmodifiableSet(mDialogSettings);
    }
}
