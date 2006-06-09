/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.IInstallOptionsEditor;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.INSISSettingsConstants;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
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
    private static INSISConsole cDummyConsole = new NullNSISConsole();

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
        mSettings.setVerbosity(INSISSettingsConstants.VERBOSITY_ALL);
        mSettings.setSymbols(symbols);
        String label = InstallOptionsPlugin.getResourceString(resource);
        setText(label);
        setToolTipText(label);
        MakeNSISRunner.addListener(this);
        updateEnabled();
    }

    private void updateEnabled()
    {
        setEnabled((mEditor != null && EclipseNSISPlugin.getDefault().isConfigured() && !MakeNSISRunner.isCompiling()));
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        switch(event.getType()) {
            case MakeNSISRunEvent.STARTED:
                setEnabled(false);
                break;
            case MakeNSISRunEvent.STOPPED:
                updateEnabled();
                break;
        }
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
            }
            else {
                IEditorInput editorInput = mEditor.getEditorInput();
                if(editorInput instanceof IFileEditorInput) {
                    IFile file = ((IFileEditorInput)editorInput).getFile();
                    if(file.exists()) {
                        IPath location = file.getLocation();
                        if(location != null) {
                            doPreview(location.toFile());
                        }
                        else {
                            Common.openError(shell,EclipseNSISPlugin.getResourceString("local.filesystem.error"),  //$NON-NLS-1$
                                    InstallOptionsPlugin.getShellImage());
                        }
                    }
                }
                else if(editorInput instanceof IPathEditorInput) {
                    doPreview(new File(((IPathEditorInput)editorInput).getPath().toOSString()));
                }
            }
        }
    }

    private void doPreview(final File file)
    {
        final Shell shell = mEditor.getSite().getShell();
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
        try {
            BusyIndicator.showWhile(mEditor.getSite().getShell().getDisplay(), new Runnable() {
                public void run() {
                    try {
                        ModalContext.run(new IRunnableWithProgress() {
                            public void run(IProgressMonitor monitor)
                            {
                                try {
                                    monitor.beginTask(InstallOptionsPlugin.getResourceString("previewing.script.task.name"),IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                                    LinkedHashMap symbols = mSettings.getSymbols();
                                    symbols.put("PREVIEW_INI",file.getAbsolutePath()); //$NON-NLS-1$
                                    String pref = InstallOptionsPlugin.getDefault().getPreferenceStore().getString(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG);
                                    NSISLanguage lang;
                                    if(pref.equals("")) { //$NON-NLS-1$
                                        lang = NSISLanguageManager.getInstance().getDefaultLanguage();
                                    }
                                    else {
                                        lang = NSISLanguageManager.getInstance().getLanguage(pref);
                                        if(lang == null) {
                                            lang = NSISLanguageManager.getInstance().getDefaultLanguage();
                                            InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG, ""); //$NON-NLS-1$
                                        }
                                    }
                                    symbols.put("PREVIEW_LANG",lang.getName()); //$NON-NLS-1$
                                    Locale locale = NSISLanguageManager.getInstance().getLocaleForLangId(lang.getLangId());
                                    if(getId().equals(PREVIEW_MUI_ID)) {
                                        symbols.put("PREVIEW_TITLE",InstallOptionsPlugin.getResourceString(locale,"preview.setup.title")); //$NON-NLS-1$ //$NON-NLS-2$
                                        symbols.put("PREVIEW_SUBTITLE",InstallOptionsPlugin.getResourceString(locale,"preview.setup.subtitle")); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                    else {
                                        symbols.put("PREVIEW_BRANDING",InstallOptionsPlugin.getResourceString(locale,"preview.setup.branding")); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                    symbols.put("PREVIEW_NAME",InstallOptionsPlugin.getResourceString(locale,"preview.setup.name")); //$NON-NLS-1$ //$NON-NLS-2$

                                    mSettings.setSymbols(symbols);
                                    final File previewScript = getPreviewScript();
                                    long timestamp = System.currentTimeMillis();
                                    MakeNSISResults results = null;
                                    results = MakeNSISRunner.compile(previewScript, mSettings, cDummyConsole, new INSISConsoleLineProcessor() {
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
                                            if (IOUtility.isValidFile(outfile) && outfile.lastModified() > timestamp) {
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
                                finally {
                                    monitor.done();
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
        }
        finally {
            pmd.close();
        }
    }

    private File getPreviewScript() throws IOException
    {
        return IOUtility.ensureLatest(InstallOptionsPlugin.getDefault().getBundle(), 
                new Path("/preview/preview.nsi"),  //$NON-NLS-1$
                new File(InstallOptionsPlugin.getPluginStateLocation(),"preview")); //$NON-NLS-1$
    }
    
    private class PreviewSettings extends NSISSettings
    {
        public boolean showStatistics()
        {
            return false;
        }

        public boolean getBoolean(String name)
        {
            return false;
        }

        public int getInt(String name)
        {
            return 0;
        }

        public String getString(String name)
        {
            return ""; //$NON-NLS-1$
        }

        public Object loadObject(String name)
        {
            return null;
        }

        public void removeBoolean(String name)
        {
        }

        public void removeInt(String name)
        {
        }

        public void removeString(String name)
        {
        }

        public void removeObject(String name)
        {
        }

        public void setValue(String name, boolean value)
        {
        }

        public void setValue(String name, int value)
        {
        }

        public void setValue(String name, String value)
        {
        }

        public void storeObject(String name, Object object)
        {
        }

        public String getName()
        {
            return null;
        }
    }
}