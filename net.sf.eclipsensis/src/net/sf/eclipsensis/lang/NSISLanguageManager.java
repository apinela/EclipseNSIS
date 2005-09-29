/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.lang;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.INSISHomeListener;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;

public class NSISLanguageManager implements INSISHomeListener, IEclipseNSISService
{
    public static NSISLanguageManager INSTANCE = null;
    public static final String PROPERTY_LANGUAGES="net.sf.eclipsensis.languages"; //$NON-NLS-1$
    
    private String mDefineMUILanguageText;
    private Map mLanguageMap = new CaseInsensitiveMap();
    private List mLanguages = new ArrayList();;
    private Map mLocaleLanguageMap= null;
    private Map mLanguageIdLocaleMap = null;
    private Integer mDefaultLanguageId = null;
    private PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

    public void start(IProgressMonitor monitor)
    {
        mDefineMUILanguageText = NSISKeywords.INSTANCE.getKeyword("!DEFINE").toUpperCase()+" MUI_LANGNAME "; //$NON-NLS-1$ //$NON-NLS-2$
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(NSISLanguageManager.class.getName());
            mLocaleLanguageMap = Common.loadMapProperty(bundle,"locale.language.map"); //$NON-NLS-1$
            mLanguageIdLocaleMap = Common.loadMapProperty(bundle,"langid.locale.map"); //$NON-NLS-1$
            mDefaultLanguageId = Integer.valueOf(bundle.getString("default.langid")); //$NON-NLS-1$
        }
        catch(Exception ex) {
            mDefaultLanguageId = new Integer(1033);
        }
        loadLanguages(monitor);
        NSISPreferences.INSTANCE.addListener(this);
        INSTANCE = this;
    }

    public void stop(IProgressMonitor monitor)
    {
        INSTANCE = null;
        NSISPreferences.INSTANCE.removeListener(this);
    }
    
    public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
    {
        loadLanguages(monitor);
    }
    
    public void addPropertyChangedListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangedListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void loadLanguages(IProgressMonitor monitor)
    {
        if(monitor != null) {
            monitor.subTask(EclipseNSISPlugin.getResourceString("loading.languages.message")); //$NON-NLS-1$
        }
        mLanguageMap.clear();
        mLanguages.clear();
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            File nsisHome = new File(NSISPreferences.INSTANCE.getNSISHome());
            if(nsisHome.exists()) {
                File langDir = new File(nsisHome,INSISConstants.LANGUAGE_FILES_LOCATION);
                if(langDir.exists()) {
                    File muiLangDir = new File(nsisHome,INSISConstants.MUI_LANGUAGE_FILES_LOCATION);

                    File[] langFiles = langDir.listFiles(new FileFilter() {
                       public boolean accept(File pathName)
                       {
                           return (pathName != null && pathName.isFile() && pathName.getName().toLowerCase().endsWith(INSISConstants.LANGUAGE_FILES_EXTENSION));
                       }
                    });
                    for (int i = 0; i < langFiles.length; i++) {
                        try {
                            String filename = langFiles[i].getName();
                            String name = filename.substring(0,filename.length()-INSISConstants.LANGUAGE_FILES_EXTENSION.length());
                            String displayName = name; 
                            int langId = 0;

                            BufferedReader br = new BufferedReader(new FileReader(langFiles[i]));
                            //The second non-comment line is the codepage
                            int n = 0;
                            while(n < 2) {
                                String line = br.readLine();
                                if(line != null) {
                                    line = line.trim();
                                    char c=line.charAt(0);
                                    if(c != '#' && c != ';') {
                                        if(n > 0) {
                                            langId = Integer.parseInt(line);
                                        }
                                        n++;
                                    }
                                }
                                else {
                                    break;
                                }
                            }
                            br.close();
                            if(muiLangDir.exists() && muiLangDir.isDirectory()) {
                                File muiLangFile = new File(muiLangDir,name+INSISConstants.MUI_LANGUAGE_FILES_EXTENSION);
                                if(muiLangFile.exists() && muiLangFile.isFile()) {
                                    br = new BufferedReader(new FileReader(muiLangFile));
                                    while(true) {
                                        String line = br.readLine();
                                        if(line != null) {
                                            line = line.trim();
                                            int m = line.indexOf(';');
                                            if(m < 0) {
                                                m = line.indexOf('#');
                                            }
                                            if(m >= 0) {
                                                line = line.substring(0,m);
                                            }
                                            if(line.toUpperCase().startsWith(mDefineMUILanguageText)) {
                                                line = line.substring(mDefineMUILanguageText.length()).trim();
                                                //Check for quotes.
                                                m = line.indexOf('"');
                                                if(m >= 0) {
                                                    n = line.indexOf('"',m+1);
                                                    if(n >= 0) {
                                                        line = line.substring(m+1,n);
                                                    }
                                                    else {
                                                        line = line.substring(m+1);
                                                    }
                                                }
                                                displayName = line;
                                                break;
                                            }
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                    br.close();
                                }
                            }
                            NSISLanguage language = new NSISLanguage(name,displayName,langId);
                            mLanguageMap.put(name,language);
                            mLanguageMap.put(new Integer(langId),language);
                            mLanguages.add(language);
                        }
                        catch(Exception ex) {
                            EclipseNSISPlugin.getDefault().log(ex);
                        }
                    }
                }
            }
        }
        mPropertyChangeSupport.firePropertyChange(PROPERTY_LANGUAGES,null,mLanguages);
    }

    public List getLanguages()
    {
        return new ArrayList(mLanguages);
    }
    
    public NSISLanguage getDefaultLanguage()
    {
        NSISLanguage lang = null;
        //Try the user's lang id
        lang = (NSISLanguage)mLanguageMap.get(new Integer(WinAPI.GetUserDefaultLangID()));
        if(lang == null) {
            Locale locale = Locale.getDefault();
            //Try the user's language
            lang = (NSISLanguage)mLanguageMap.get(locale.getDisplayLanguage(Locale.US));
            if(lang == null) {
                //See if this is one of the specially mapped locales
                lang = (NSISLanguage)mLanguageMap.get(((String)mLocaleLanguageMap.get(locale.toString())));
                if(lang == null) {
                    //Try the default lang id
                    lang = (NSISLanguage)mLanguageMap.get(mDefaultLanguageId);
                    if(lang == null && mLanguages.size() > 0) {
                        //When all else fails, return the first one
                        lang = (NSISLanguage)mLanguages.get(0);
                    }
                }
            }
        }
        
        return lang;
    }
    
    public Locale getDefaultLocale()
    {
        return getLocaleForLangId(mDefaultLanguageId.intValue());
    }
    
    public NSISLanguage getLanguage(String name)
    {
        return (NSISLanguage)mLanguageMap.get(name);
    }

    public Locale getLocaleForLangId(int langId)
    {
        Locale locale = null;
        int defaultLangId = mDefaultLanguageId.intValue();
        String strLangId = Integer.toString(langId);

        Object obj = mLanguageIdLocaleMap.get(strLangId);
        if(obj == null && langId != defaultLangId) {
            obj = getLocaleForLangId(defaultLangId);
            mLanguageIdLocaleMap.put(strLangId,obj);
        }
        if(obj != null) {
            if(obj instanceof Locale) {
                locale = (Locale)obj;
            }
            else {
                locale = parseLocale(obj.toString());
                mLanguageIdLocaleMap.put(strLangId, locale);
            }
        }
        return locale;
    }
    
    private Locale parseLocale(String localeText)
    {
        Locale locale = null;
        StringTokenizer st = new StringTokenizer(localeText,"_"); //$NON-NLS-1$
        int n = st.countTokens();
        if(n > 0) {
            n = Math.min(n,3);
            String[] tokens = new String[n];
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = st.nextToken();
            }
            switch(n) {
                case 1:
                    locale = new Locale(tokens[0]);
                    break;
                case 2:
                    locale = new Locale(tokens[0],tokens[1]);
                    break;
                default:
                    locale = new Locale(tokens[0],tokens[1],tokens[2]);
            }
        }
        return locale;
    }
}
