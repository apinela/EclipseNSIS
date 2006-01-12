/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.installoptions.builder.InstallOptionsBuilder;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.gef.GEFPlugin;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class InstallOptionsPlugin extends AbstractUIPlugin implements IInstallOptionsConstants
{
    public static final RGB SYNTAX_COMMENTS = new RGB(0x7f,0x9f,0xbf);
    public static final RGB SYNTAX_NUMBERS = new RGB(0x61,0x31,0x1e);
    public static final RGB SYNTAX_SECTIONS = new RGB(0x0,0x50,0x50);
    private static Image cShellImage;
    private static File cStateLocation = null;

    private static InstallOptionsPlugin cPlugin;
    private Map mResourceBundles = new HashMap();
    public static final String[] BUNDLE_NAMES = new String[]{RESOURCE_BUNDLE,MESSAGE_BUNDLE};
    private ImageManager mImageManager;
    private String mName = null;
    private static boolean cCheckedEditorAssociation = false;
    private JobScheduler mJobScheduler = new JobScheduler();

    /**
     *
     */
    public InstallOptionsPlugin()
    {
        super();
        cPlugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static InstallOptionsPlugin getDefault() {
        return cPlugin;
    }

    public static ImageManager getImageManager()
    {
        return getDefault().mImageManager;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        return getResourceString(key, key);
    }

    public static String getResourceString(Locale locale, String key)
    {
        return getResourceString(locale, key, key);
    }

    public static String getResourceString(String key, String defaultValue)
    {
        return getResourceString(Locale.getDefault(),key,defaultValue);
    }

    public static String getResourceString(Locale locale, String key, String defaultValue)
    {
        InstallOptionsPlugin plugin = getDefault();
        if(plugin != null) {
            ResourceBundle bundle = plugin.getResourceBundle(locale);
            try {
                return (bundle != null) ? bundle.getString(key) : defaultValue;
            }
            catch (MissingResourceException e) {
            }
        }
        return defaultValue;
    }


    public static String getFormattedString(String key, Object[] args)
    {
        return MessageFormat.format(getResourceString(key),args);
    }

    /**
     * Returns the string from the plugin bundle's resource bundle,
     * or 'key' if not found.
     */
    public static String getBundleResourceString(String key)
    {
        return Platform.getResourceString(getDefault().getBundle(), key);
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return getResourceBundle(Locale.getDefault());
    }

    public synchronized ResourceBundle getResourceBundle(Locale locale)
    {
        if(!mResourceBundles.containsKey(locale)) {
            mResourceBundles.put(locale,new CompoundResourceBundle(getClass().getClassLoader(),locale, BUNDLE_NAMES));
        }
        return (ResourceBundle)mResourceBundles.get(locale);
    }

    private void initializePreference(IPreferenceStore store, String name, String defaultValue)
    {
        store.setDefault(name,defaultValue);
        if(!store.contains(name)) {
            store.setToDefault(name);
        }
    }

    private void initializePreferences()
    {
        IPreferenceStore store = getPreferenceStore();
        initializePreference(store,PREFERENCE_SHOW_GRID,SHOW_GRID_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_RULERS,SHOW_RULERS_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_GUIDES,SHOW_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_DIALOG_SIZE,SHOW_DIALOG_SIZE_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GRID,SNAP_TO_GRID_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GEOMETRY,SNAP_TO_GEOMETRY_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GUIDES,SNAP_TO_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_GLUE_TO_GUIDES,GLUE_TO_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER.asString(GRID_SPACING_DEFAULT));
        initializePreference(store,PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER.asString(GRID_ORIGIN_DEFAULT));
        initializePreference(store,PREFERENCE_GRID_STYLE,GRID_STYLE_DEFAULT);
        initializePreference(store,PREFERENCE_CHECK_EDITOR_ASSOCIATION,CHECK_EDITOR_ASSOCIATION_DEFAULT.toString());

        String preference = store.getString(IInstallOptionsConstants.PREFERENCE_SYNTAX_STYLES);
        Map map;
        if(!Common.isEmpty(preference)) {
            map = NSISTextUtility.parseSyntaxStylesMap(preference);
        }
        else {
            map = new LinkedHashMap();
        }
        boolean changed = setSyntaxStyles(map);
        if(changed) {
            store.putValue(IInstallOptionsConstants.PREFERENCE_SYNTAX_STYLES, NSISTextUtility.flattenSyntaxStylesMap(map));
        }
        initializePaletteViewerPreferences(store);
    }

    private void initializePaletteViewerPreferences(IPreferenceStore store)
    {
        //This should be done just once.
        if(!store.getBoolean(PREFERENCE_PALETTE_VIEWER_PREFS_INIT)) {
            String[] properties = {
                    PaletteViewerPreferences.PREFERENCE_LAYOUT,
                    PaletteViewerPreferences.PREFERENCE_AUTO_COLLAPSE,
                    PaletteViewerPreferences.PREFERENCE_COLUMNS_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_LIST_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_ICONS_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_DETAILS_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_FONT
                };
            IPreferenceStore gefStore = GEFPlugin.getDefault().getPreferenceStore();
            for (int i = 0; i < properties.length; i++) {
                if(!store.contains(properties[i]) && gefStore.contains(properties[i])) {
                    store.setValue(properties[i], gefStore.getString(properties[i]));
                }
            }
            store.setValue(PREFERENCE_PALETTE_VIEWER_PREFS_INIT,true);
        }
    }

    public boolean setSyntaxStyles(Map map)
    {
        boolean changed = setSyntaxStyle(map,IInstallOptionsConstants.COMMENT_STYLE,new NSISSyntaxStyle(SYNTAX_COMMENTS,null,false,true,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.SECTION_STYLE,new NSISSyntaxStyle(SYNTAX_SECTIONS,null,true,false,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.KEY_STYLE,new NSISSyntaxStyle(ColorManager.BLUE,null,false,false,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.KEY_VALUE_DELIM_STYLE,new NSISSyntaxStyle(ColorManager.RED,null,false,false,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.NUMBER_STYLE,new NSISSyntaxStyle(SYNTAX_NUMBERS,null,true,false,false,false));
        return changed;
    }

    private boolean setSyntaxStyle(Map map, String name, NSISSyntaxStyle style)
    {
        if(!map.containsKey(name) || map.get(name) == null) {
            map.put(name,style);
            return true;
        }
        return false;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        mName = (String)getBundle().getHeaders().get("Bundle-Name"); //$NON-NLS-1$
        mImageManager = new ImageManager(this);
        cShellImage = mImageManager.getImage(getResourceString("installoptions.icon")); //$NON-NLS-1$
        initializePreferences();
        mJobScheduler.start();
        new Thread(new Runnable(){
            public void run()
            {
                InstallOptionsBuilder.buildWorkspace(null);
            }
        }, InstallOptionsPlugin.getResourceString("workspace.build.thread.name")).start(); //$NON-NLS-1$
    }

    public void stop(BundleContext context) throws Exception
    {
        mJobScheduler.stop();
        mImageManager = null;
        super.stop(context);
    }

    public JobScheduler getJobScheduler()
    {
        return mJobScheduler;
    }

    public void log(Throwable t)
    {
        ILog log = getLog();
        if(log != null) {
            IStatus status;
            if(t instanceof CoreException) {
                status = ((CoreException)t).getStatus();
            }
            else {
                String message = t.getMessage();
                status = new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR, message==null?t.getClass().getName():message,t);
            }
            log.log(status);
        }
        else {
            t.printStackTrace();
        }
    }

    public static synchronized void checkEditorAssociation()
    {
        if(!cCheckedEditorAssociation) {
            cCheckedEditorAssociation = true;
            final boolean toggleState = getDefault().getPreferenceStore().getBoolean(PREFERENCE_CHECK_EDITOR_ASSOCIATION);
            if(toggleState) {
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                for(int i=0; i<INI_EXTENSIONS.length; i++) {
                    IEditorDescriptor descriptor = editorRegistry.getDefaultEditor("*."+INI_EXTENSIONS[i]); //$NON-NLS-1$
                    if(descriptor == null || (!descriptor.getId().equals(INSTALLOPTIONS_DESIGN_EDITOR_ID) && !descriptor.getId().equals(INSTALLOPTIONS_SOURCE_EDITOR_ID))) {
                        Display.getDefault().asyncExec(new Runnable(){
                            public void run()
                            {
                                MessageDialogWithToggle dialog = new MessageDialogWithToggle(
                                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                        getDefault().getName(),
                                        InstallOptionsPlugin.getShellImage(),
                                        getResourceString("check.default.editor.question"),  //$NON-NLS-1$
                                        MessageDialog.QUESTION,
                                        new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0,
                                        getResourceString("check.default.editor.toggle"), !toggleState); //$NON-NLS-1$
                                dialog.setPrefStore(getDefault().getPreferenceStore());
                                dialog.setPrefKey(PREFERENCE_CHECK_EDITOR_ASSOCIATION);
                                dialog.open();
                                if(dialog.getReturnCode() == IDialogConstants.YES_ID) {
                                    for(int i=0; i<INI_EXTENSIONS.length; i++) {
                                        editorRegistry.setDefaultEditor("*."+INI_EXTENSIONS[i],INSTALLOPTIONS_DESIGN_EDITOR_ID); //$NON-NLS-1$
                                    }
                                    //Cast to inner class because otherwise it cannot be saved.
                                    ((EditorRegistry)editorRegistry).saveAssociations();
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    public static Image getShellImage()
    {
        return cShellImage;
    }

    public static synchronized File getPluginStateLocation()
    {
        if(cStateLocation == null) {
            InstallOptionsPlugin plugin = getDefault();
            if(plugin != null) {
                cStateLocation = plugin.getStateLocation().toFile();
            }
        }
        return cStateLocation;
    }
}
