/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.INSISConsoleLineProcessor;
import net.sf.eclipsensis.console.NSISConsoleLine;
import net.sf.eclipsensis.console.model.NSISConsoleModel;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.IInstallOptionsEditor;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.Disposable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class PreviewAction extends Action implements Disposable, IMakeNSISRunListener
{
    public static final String PREVIEW_CLASSIC_ID = "net.sf.eclipsensis.installoptions.preview_classic"; //$NON-NLS-1$
    public static final String PREVIEW_MUI_ID = "net.sf.eclipsensis.installoptions.preview_mui"; //$NON-NLS-1$
    private static NSISConsoleModel cConsoleModel = new NSISConsoleModel() {
        public boolean supportsStatistics()
        {
            return false;
        }
    };
   
    private IInstallOptionsEditor mEditor;
    private PreviewSettings mSettings = new PreviewSettings();
    
    public PreviewAction(int type, IInstallOptionsEditor editor)
    {
        super();
        mEditor = editor;
        String resource;
        LinkedHashMap symbols = new LinkedHashMap();
        switch(type) {
            case IInstallOptionsConstants.PREVIEW_CLASSIC:
                setId(PREVIEW_CLASSIC_ID);
                resource = "preview.action.classic.label"; //$NON-NLS-1$
                break;
            default:
                setId(PREVIEW_MUI_ID);
                resource = "preview.action.mui.label"; //$NON-NLS-1$
                symbols.put("PREVIEW_MUI",null); //$NON-NLS-1$
        }
        mSettings.setVerbosity(INSISPreferenceConstants.VERBOSITY_ERRORS);
        mSettings.setSymbols(symbols);
        String label = InstallOptionsPlugin.getResourceString(resource);
        setText(label); //$NON-NLS-1$
        setToolTipText(label); //$NON-NLS-1$
        MakeNSISRunner.addListener(this);
        updateEnabled();
    }
    
    private void updateEnabled()
    {
        setEnabled((mEditor != null && EclipseNSISPlugin.getDefault().isConfigured() && !MakeNSISRunner.isCompiling()));
    }

    public void started()
    {
        setEnabled(false);
    }

    public void stopped()
    {
        updateEnabled();
    }

    public void scriptUpdated()
    {
        updateEnabled();
    }

    public void dispose()
    {
        MakeNSISRunner.removeListener(this);
    }

    public void run() 
    {
        if(mEditor != null) {
            Shell shell = mEditor.getSite().getShell();
            if(mEditor.isDirty()) {
                if(!Common.openConfirm(shell,InstallOptionsPlugin.getResourceString("save.before.preview.confirm"), //$NON-NLS-1$
                                       InstallOptionsPlugin.getShellImage())) {
                    return;
                }
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                dialog.open();
                IProgressMonitor progressMonitor = dialog.getProgressMonitor();
                mEditor.doSave(progressMonitor);
                dialog.close();
                if(progressMonitor.isCanceled()) {
                    return;
                }
            }
            INIFile iniFile = mEditor.getINIFile();
            if(iniFile.hasErrors()) {
                Common.openError(shell,InstallOptionsPlugin.getResourceString("ini.errors.preview.error"),  //$NON-NLS-1$
                        InstallOptionsPlugin.getShellImage());
                return;
            }
            INISection settings = iniFile.findSections(InstallOptionsModel.SECTION_SETTINGS)[0];
            INIKeyValue numFields = settings.findKeyValues(InstallOptionsModel.PROPERTY_NUMFIELDS)[0];
            if(Integer.parseInt(numFields.getValue()) <= 0) {
                Common.openError(shell,InstallOptionsPlugin.getResourceString("ini.numfields.preview.error"),  //$NON-NLS-1$
                        InstallOptionsPlugin.getShellImage());
                return;
            }
            IEditorInput editorInput = mEditor.getEditorInput();
            if(editorInput instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput)editorInput).getFile();
                if(file.exists()) {
                    doPreview(file.getLocation().toFile());
                }
            }
            else if(editorInput instanceof IPathEditorInput) {
                doPreview(new File(((IPathEditorInput)editorInput).getPath().toOSString()));
            }
        }
    }
    
    private void doPreview(File file)
    {
        try {
            final Shell shell = mEditor.getSite().getShell();
            LinkedHashMap symbols = mSettings.getSymbols();
            symbols.put("PREVIEW_INI",file.getAbsolutePath()); //$NON-NLS-1$
            String pref = InstallOptionsPlugin.getDefault().getPreferenceStore().getString(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG);
            NSISLanguage lang;
            if(pref.equals("")) { //$NON-NLS-1$
                lang = NSISLanguageManager.INSTANCE.getDefaultLanguage();
            }
            else {
                lang = NSISLanguageManager.INSTANCE.getLanguage(pref); 
                if(lang == null) {
                    lang = NSISLanguageManager.INSTANCE.getDefaultLanguage();
                    InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG, ""); //$NON-NLS-1$
                }
            }
            symbols.put("PREVIEW_LANG",lang.getName()); //$NON-NLS-1$
            Locale locale = NSISLanguageManager.INSTANCE.getLocaleForLangId(lang.getLangId());
            if(getId().equals(PREVIEW_MUI_ID)) {
                symbols.put("PREVIEW_TITLE",InstallOptionsPlugin.getResourceString(locale,"preview.setup.title")); //$NON-NLS-1$ //$NON-NLS-2$
                symbols.put("PREVIEW_SUBTITLE",InstallOptionsPlugin.getResourceString(locale,"preview.setup.subtitle")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            symbols.put("PREVIEW_NAME",InstallOptionsPlugin.getResourceString(locale,"preview.setup.name")); //$NON-NLS-1$ //$NON-NLS-2$
    
            mSettings.setSymbols(symbols);
            final File previewScript = getPreviewScript();
            final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell) {
                protected void configureShell(Shell shell)
                {
                    super.configureShell(shell);
                    Rectangle rect = shell.getDisplay().getBounds();
                    shell.setLocation(rect.x+rect.width+1,rect.y+rect.height+1);
                }
    
                protected Rectangle getConstrainedShellBounds(Rectangle preferredSize)
                {
                    Rectangle rect = shell.getDisplay().getBounds();
                    return new Rectangle(rect.x+rect.width+1,rect.y+rect.height+1,preferredSize.width,preferredSize.height);
                }
            };
            pmd.open();
            BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
                public void run() {
                    try {
                        ModalContext.run(new IRunnableWithProgress() {
                            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                            {
                                try {
                                    long timestamp = System.currentTimeMillis();
                                    MakeNSISResults results = null;
                                    results = MakeNSISRunner.compile(previewScript, mSettings, cConsoleModel, new INSISConsoleLineProcessor() {
                                        public NSISConsoleLine processText(String text)
                                        {
                                            return NSISConsoleLine.info(text);
                                        }
        
                                        public void reset()
                                        {
                                        }
                                    });
                                    if(results != null) {
                                        if (results.getReturnCode() != 0) {
                                            List errors = results.getProblems();
                                            if (Common.isEmptyCollection(errors)) {
                                                errors = cConsoleModel.getErrors();
                                            }
                                            final String error;
                                            if (!Common.isEmptyCollection(errors)) {
                                                Iterator iter = errors.iterator();
                                                StringBuffer buf = new StringBuffer(((NSISConsoleLine)iter.next()).toString());
                                                while (iter.hasNext()) {
                                                    buf.append(INSISConstants.LINE_SEPARATOR).append(((NSISConsoleLine)iter.next()).toString());
                                                }
                                                error = buf.toString();
                                            }
                                            else {
                                                error = InstallOptionsPlugin.getResourceString("preview.compile.error"); //$NON-NLS-1$
                                            }
                                            shell.getDisplay().asyncExec(new Runnable() {
                                                public void run()
                                                {
                                                    Common.openError(shell, error, InstallOptionsPlugin.getShellImage());
                                                }
                                            });
                                        }
                                        else {
                                            final File outfile = new File(results.getOutputFileName());
                                            if (outfile.exists() && outfile.isFile() && outfile.lastModified() > timestamp) {
                                                MakeNSISRunner.testInstaller(outfile.getAbsolutePath(), null, true);
                                            }
                                        }
                                    }
                                }
                                catch (final Exception e) {
                                    InstallOptionsPlugin.getDefault().log(e);
                                    shell.getDisplay().asyncExec(new Runnable() {
                                        public void run() {
                                            Common.openError(shell, e.getMessage(), InstallOptionsPlugin.getShellImage());
                                        }
                                    });
                                }
                            }
                        },true,pmd.getProgressMonitor(),shell.getDisplay());
                    }
                    catch (Exception e) {
                        InstallOptionsPlugin.getDefault().log(e);
                        Common.openError(shell, e.getMessage(), InstallOptionsPlugin.getShellImage());
                    }
                }
            });
            pmd.close();
            }
        catch (final IOException e) {
            InstallOptionsPlugin.getDefault().log(e);
            Common.openError(mEditor.getSite().getShell(),e.getMessage(),InstallOptionsPlugin.getShellImage());
        }
    }
    
    private File getPreviewScript() throws IOException
    {
        File previewFolder = new File(InstallOptionsPlugin.getPluginStateLocation(),"preview"); //$NON-NLS-1$
        if(previewFolder.exists() && previewFolder.isFile()) {
            previewFolder.delete();
        }
        if(!previewFolder.exists()) {
            previewFolder.mkdirs();
        }

        File previewScript = new File(previewFolder,"preview.nsi"); //$NON-NLS-1$
        if(previewScript.exists() && previewScript.isDirectory()) {
            previewScript.delete();
        }
        if(!previewScript.exists()) {
            URL url = InstallOptionsPlugin.getDefault().getBundle().getEntry("/preview/preview.nsi"); //$NON-NLS-1$
            InputStream is = null;
            byte[] data;
            try {
                is = new BufferedInputStream(url.openStream());
                data = Common.loadContentFromStream(is);
            }
            finally {
                Common.closeIO(is);
            }
            Common.writeContentToFile(previewScript,data);
        }
        return previewScript;
    }
    
    private class PreviewSettings extends NSISSettings
    {
        protected boolean getBoolean(String name)
        {
            return false;
        }

        protected int getInt(String name)
        {
            return 0;
        }

        protected String getString(String name)
        {
            return ""; //$NON-NLS-1$
        }

        protected Object loadObject(String name)
        {
            return null;
        }

        protected void removeBoolean(String name)
        {
        }

        protected void removeInt(String name)
        {
        }

        protected void removeString(String name)
        {
        }

        protected void setValue(String name, boolean value)
        {
        }

        protected void setValue(String name, int value)
        {
        }

        protected void setValue(String name, String value)
        {
        }

        protected void storeObject(String name, Object object)
        {
        }

        public String getName()
        {
            return null;
        }
    }
}