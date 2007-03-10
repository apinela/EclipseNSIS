/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.installoptions.figures.DashedLineBorder;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.DummyNSISSettings;
import net.sf.eclipsensis.installoptions.util.FontUtility;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.script.NSISScriptProblem;
import net.sf.eclipsensis.settings.INSISSettingsConstants;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.Disposable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class PreviewAction extends Action implements Disposable, IMakeNSISRunListener
{
    public static final String PREVIEW_CLASSIC_ID = "net.sf.eclipsensis.installoptions.preview_classic"; //$NON-NLS-1$
    public static final String PREVIEW_MUI_ID = "net.sf.eclipsensis.installoptions.preview_mui"; //$NON-NLS-1$

    private static final String cMUIDialogSizeName = InstallOptionsPlugin.getResourceString("mui.dialog.size.name"); //$NON-NLS-1$
    private static final String cClassicDialogSizeName = InstallOptionsPlugin.getResourceString("classic.dialog.size.name"); //$NON-NLS-1$
    private static INSISConsole cDummyConsole = new NullNSISConsole();
    private static HashMap cPreviewCache = new HashMap();
    private static HashMap cBitmapCache = new HashMap();
    private static HashMap cIconCache = new HashMap();

    private IInstallOptionsEditor mEditor;
    private NSISSettings mSettings = new DummyNSISSettings();
    private IPreferenceStore mPreferenceStore = InstallOptionsPlugin.getDefault().getPreferenceStore();

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
        mSettings.setVerbosity(INSISSettingsConstants.VERBOSITY_DEFAULT);
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
                boolean autosaveBeforePreview = mPreferenceStore.getBoolean(IInstallOptionsConstants.PREFERENCE_AUTOSAVE_BEFORE_PREVIEW);
                boolean shouldSave = autosaveBeforePreview;
                if(!shouldSave) {
                    MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                            InstallOptionsPlugin.getShellImage(), InstallOptionsPlugin.getResourceString("save.before.preview.confirm"), //$NON-NLS-1$
                            MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0,
                            InstallOptionsPlugin.getResourceString("confirm.toggle.message"),false); //$NON-NLS-1$
                    shouldSave = (dialog.open()==0);
                    if(dialog.getToggleState()) {
                        mPreferenceStore.setValue(IInstallOptionsConstants.PREFERENCE_AUTOSAVE_BEFORE_PREVIEW, true);
                    }
                }
                if(shouldSave) {
                    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                    dialog.open();
                    IProgressMonitor progressMonitor = dialog.getProgressMonitor();
                    mEditor.doSave(progressMonitor);
                    dialog.close();
                    if(progressMonitor.isCanceled()) {
                        return;
                    }
                }
                else {
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
                            doPreview(iniFile, location.toFile());
                        }
                        else {
                            Common.openError(shell,EclipseNSISPlugin.getResourceString("local.filesystem.error"),  //$NON-NLS-1$
                                    InstallOptionsPlugin.getShellImage());
                        }
                    }
                }
                else if(editorInput instanceof IPathEditorInput) {
                    doPreview(iniFile, new File(((IPathEditorInput)editorInput).getPath().toOSString()));
                }
            }
        }
    }

    private void doPreview(final INIFile iniFile, final File file)
    {
        final Shell shell = mEditor.getSite().getShell();
        BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
            public void run() {
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
                    ModalContext.run(new IRunnableWithProgress() {
                        private File createPreviewFile(File previewFile, INIFile inifile, final NSISLanguage lang) throws IOException
                        {
                            if(previewFile == null) {
                                previewFile = File.createTempFile("preview",".ini"); //$NON-NLS-1$ //$NON-NLS-2$
                                previewFile.deleteOnExit();
                            }
                            inifile = inifile.copy();
                            InstallOptionsDialog dialog = InstallOptionsDialog.loadINIFile(inifile);
                            DialogSize dialogSize;
                            if(getId().equals(PREVIEW_MUI_ID)) {
                                dialogSize = DialogSizeManager.getDialogSize(cMUIDialogSizeName);
                            }
                            else {
                                dialogSize = DialogSizeManager.getDialogSize(cClassicDialogSizeName);
                            }
                            if(dialogSize == null) {
                                dialogSize = DialogSizeManager.getDefaultDialogSize();
                            }
                            dialog.setDialogSize(dialogSize);
                            Font font = FontUtility.getInstallOptionsFont();
                            final DashedLineBorder border = new DashedLineBorder();
                            for(Iterator iter=dialog.getChildren().iterator(); iter.hasNext(); ) {
                                InstallOptionsWidget widget = (InstallOptionsWidget)iter.next();
                                if(widget instanceof InstallOptionsPicture) {
                                    final InstallOptionsPicture picture = (InstallOptionsPicture)widget;
                                    final Dimension dim = widget.toGraphical(widget.getPosition(), font).getSize();
                                    final HashMap cache;
                                    switch(picture.getSWTImageType()) {
                                        case SWT.IMAGE_BMP:
                                            cache = cBitmapCache;
                                            break;
                                        case SWT.IMAGE_ICO:
                                            cache = cIconCache;
                                            break;
                                        default:
                                            continue;
                                    }
                                    final File[] imageFile = new File[] {(File)cache.get(dim)};
                                    if(!IOUtility.isValidFile(imageFile[0])) {
                                        shell.getDisplay().syncExec(new Runnable() {
                                            public void run()
                                            {
                                                Image bitmap = new Image(shell.getDisplay(), dim.width, dim.height);
                                                GC gc = new GC(bitmap);
                                                gc.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                                                gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                                                gc.fillRectangle(0, 0, dim.width, dim.height);
                                                border.paint(new SWTGraphics(gc),new org.eclipse.draw2d.geometry.Rectangle(0, 0, dim.width, dim.height));
//                                                gc.setLineDash(DashedLineBorder.DASHES);
//                                                gc.drawRectangle(0, 0, dim.width-1, dim.height-1);
                                                Image widgetImage = picture.getImage();
                                                Rectangle rect = widgetImage.getBounds();
                                                int x, y, width, height;
                                                if (rect.width > dim.width) {
                                                    x = 0;
                                                    width = dim.width;
                                                }
                                                else {
                                                    x = (dim.width - rect.width) / 2;
                                                    width = rect.width;
                                                }
                                                if (rect.height > dim.height) {
                                                    y = 0;
                                                    height = dim.height;
                                                }
                                                else {
                                                    y = (dim.height - rect.height) / 2;
                                                    height = rect.height;
                                                }
                                                gc.drawImage(widgetImage, 0, 0, rect.width, rect.height, x, y, width, height);
                                                gc.dispose();
                                                ImageLoader loader = new ImageLoader();
                                                loader.data = new ImageData[]{bitmap.getImageData()};
                                                try {
                                                    imageFile[0] = File.createTempFile("preview", picture.getFileExtension()); //$NON-NLS-1$
                                                    imageFile[0].deleteOnExit();
                                                    loader.save(imageFile[0].getAbsolutePath(), picture.getSWTImageType());
                                                    cache.put(dim, imageFile[0]);
                                                }
                                                catch (IOException e) {
                                                    imageFile[0] = null;
                                                    InstallOptionsPlugin.getDefault().log(e);
                                                    cache.remove(dim);
                                                }
                                            }
                                        });
                                    }
                                    if(imageFile[0] != null) {
                                        picture.setPropertyValue(InstallOptionsModel.PROPERTY_TEXT, imageFile[0].getAbsolutePath());
                                    }
                                }
                            }
                            inifile = dialog.updateINIFile();
                            IOUtility.writeContentToFile(previewFile,inifile.toString().getBytes());
                            return previewFile;
                        }

                        public void run(IProgressMonitor monitor)
                        {
                            try {
                                monitor.beginTask(InstallOptionsPlugin.getResourceString("previewing.script.task.name"),IProgressMonitor.UNKNOWN); //$NON-NLS-1$
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
                                PreviewCacheKey key = new PreviewCacheKey(file,lang);
                                File previewFile = (File)cPreviewCache.get(key);
                                if(previewFile == null || file.lastModified() > previewFile.lastModified()) {
                                    previewFile = createPreviewFile(previewFile, iniFile, lang);
                                    cPreviewCache.put(key,previewFile);
                                }

                                LinkedHashMap symbols = mSettings.getSymbols();

                                symbols.put("PREVIEW_INI",previewFile.getAbsolutePath()); //$NON-NLS-1$
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
                                            StringBuffer buf = new StringBuffer(((NSISScriptProblem)iter.next()).getText());
                                            while (iter.hasNext()) {
                                                buf.append(INSISConstants.LINE_SEPARATOR).append(((NSISScriptProblem)iter.next()).getText());
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
                finally {
                    pmd.close();
                }
            }
        });
    }

    private File getPreviewScript() throws IOException
    {
        return IOUtility.ensureLatest(InstallOptionsPlugin.getDefault().getBundle(),
                new Path("/preview/preview.nsi"),  //$NON-NLS-1$
                new File(InstallOptionsPlugin.getPluginStateLocation(),"preview")); //$NON-NLS-1$
    }

    private class PreviewCacheKey
    {
        private File mFile;
        private NSISLanguage mLanguage;

        public PreviewCacheKey(File file, NSISLanguage language)
        {
            mFile = file;
            mLanguage = language;
        }

        public int hashCode()
        {
            int result = 31 + ((mFile == null)?0:mFile.hashCode());
            result = 31 * result + ((mLanguage == null)?0:mLanguage.hashCode());
            return result;
        }

        public boolean equals(Object obj)
        {
            if(obj != this) {
                if(obj instanceof PreviewCacheKey) {
                    return Common.objectsAreEqual(mFile,((PreviewCacheKey)obj).mFile) &&
                        Common.objectsAreEqual(mLanguage,((PreviewCacheKey)obj).mLanguage);
                }
                return false;
            }
            return true;
        }
    }
}