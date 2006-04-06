/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.help.NSISUsageProvider;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.script.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

public class NSISWizardScriptGenerator implements INSISWizardConstants
{
    private static final int BEGIN_TASK = 1;
    private static final int SET_TASK_NAME = 2;

    private boolean mNewRmDirUsage = false;

    private NSISWizardSettings mSettings = null;
    private PrintWriter mWriter = null;
    private IProgressMonitor mMonitor = null;
    private IFile mSaveFile;
    private static final String cUninstallRegKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\$(^Name)"; //$NON-NLS-1$
    private String mNsisDirKeyword;

    private NSISScript mScript;
    private List mUnSectionList;
    private NSISScriptFunction mUnOnInitFunction;
    private int mSectionCounter = 0;
    private int mSectionGroupCounter = 0;
    private INSISScriptElement mSectionsPlaceHolder;
    private String[] mHKeyNames;

    public NSISWizardScriptGenerator(NSISWizardSettings settings)
    {
        mSettings = settings;
        String usage = NSISUsageProvider.getInstance().getUsage(getKeyword("RmDir")); //$NON-NLS-1$
        if(!Common.isEmpty(usage)) {
            usage = usage.toUpperCase();
            String search = new StringBuffer(getKeyword("/r")).append("|").append( //$NON-NLS-1$ //$NON-NLS-2$
                                getKeyword("/REBOOTOK")).toString().toUpperCase(); //$NON-NLS-1$
            mNewRmDirUsage = (usage.indexOf(search) < 0);
        }
        mHKeyNames = NSISWizardDisplayValues.getHKEYNames();
    }

    private void updateMonitorTask(String resource, Object arg, int flag)
    {
        if(mMonitor != null) {
            String message = EclipseNSISPlugin.getResourceString(resource);
            if(arg != null) {
                Object[] args;
                if(arg.getClass().isArray()) {
                    args = (Object[])arg;
                }
                else {
                    args = new Object[]{arg};
                }
                message = MessageFormat.format(message,args);
            }
            switch(flag) {
                case BEGIN_TASK:
                    mMonitor.beginTask(message,3);
                    break;
                case SET_TASK_NAME:
                    mMonitor.setTaskName(message);
                    break;
            }
        }
    }

    private void incrementMonitor(int work)
    {
        if(mMonitor != null) {
            mMonitor.worked(work);
        }
    }

    public synchronized void generate(Shell shell, IProgressMonitor monitor) throws CoreException, IOException
    {
        try {
            mMonitor = monitor;
            String savePath = mSettings.getSavePath();
            updateMonitorTask("scriptgen.create.message",savePath,BEGIN_TASK); //$NON-NLS-1$

            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

            mSaveFile = root.getFile(new Path(savePath));
            if(mSaveFile.exists()) {
                mSaveFile.delete(true,true,null);
            }

            PipedOutputStream pos = new PipedOutputStream();
            mWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pos)));
            InputStream is = new BufferedInputStream(new PipedInputStream(pos));

            new Thread(new Runnable() {
                public void run()
                {
                    writeScript();
                }
            },EclipseNSISPlugin.getResourceString("wizard.script.generator.thread.name")).start(); //$NON-NLS-1$

            mSaveFile.create(is,true,null);
            new NSISTaskTagUpdater().updateTaskTags(mSaveFile);
            mWriter = null;
            incrementMonitor(1);

            updateMonitorTask("scriptgen.open.message",savePath,SET_TASK_NAME); //$NON-NLS-1$
            shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try {
                        IDE.openEditor(page, mSaveFile, true);
                    } catch (PartInitException e) {
                    }
                }
            });
            incrementMonitor(1);
            if(mSettings.isCompileScript()) {
                updateMonitorTask("scriptgen.compile.message",savePath,SET_TASK_NAME); //$NON-NLS-1$
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        if(page != null) {
                            IEditorPart editor = page.getActiveEditor();
                            if(editor instanceof NSISEditor) {
                                IAction action = null;
                                if(mSettings.isTestScript()) {
                                    action = ((NSISEditor)editor).getAction(INSISConstants.COMPILE_TEST_ACTION_ID);
                                }
                                else {
                                    action = ((NSISEditor)editor).getAction(INSISConstants.COMPILE_ACTION_ID);
                                }
                                if(action != null) {
                                    action.run();
                                }
                            }
                        }
                    }
                });
                incrementMonitor(1);
            }
            mMonitor.done();
        }
        finally {
            mMonitor = null;
            if(mWriter != null) {
                mWriter.close();
            }
        }
    }

    private String maybeMakeRelative(IContainer reference, String pathname)
    {
        if(pathname.toUpperCase().startsWith(mNsisDirKeyword)) {
            pathname = Common.quote(pathname);
        }
        else {
            if(!Common.isEmpty(pathname) && mSettings.isMakePathsRelative()) {
                pathname = IOUtility.makeRelativeLocation(reference,pathname);
            }
        }
        return pathname;
    }

    private void writeScript()
    {
        String defaultTaskTag = ""; //$NON-NLS-1$
        Collection taskTags = NSISPreferences.INSTANCE.getTaskTags();
        for (Iterator iter = taskTags.iterator(); iter.hasNext();) {
            NSISTaskTag taskTag = (NSISTaskTag)iter.next();
            if(taskTag.isDefault()) {
                defaultTaskTag = taskTag.getTag();
                break;
            }
        }
        mNsisDirKeyword = getKeyword("${NSISDIR}").toUpperCase(); //$NON-NLS-1$
        boolean isSilent = false;
        boolean isMUI = false;
        switch(mSettings.getInstallerType()) {
            case INSTALLER_TYPE_SILENT:
                isSilent = true;
                break;
            case INSTALLER_TYPE_MUI:
                isMUI = true;
                break;
        }

        List languages = mSettings.getLanguages();
        mScript = new NSISScript(mSettings.getName());

        if(mSettings.getCompressorType() != MakeNSISRunner.COMPRESSOR_DEFAULT) {
            Object args;
            String solidKeyword = NSISKeywords.getInstance().getKeyword("/SOLID"); //$NON-NLS-1$
            if(NSISKeywords.getInstance().isValidKeyword(solidKeyword) && mSettings.isSolidCompression()) {
                args = new String[]{solidKeyword,MakeNSISRunner.COMPRESSOR_NAME_ARRAY[mSettings.getCompressorType()]};
            }
            else {
                args = MakeNSISRunner.COMPRESSOR_NAME_ARRAY[mSettings.getCompressorType()];
            }
            mScript.addElement(new NSISScriptAttribute("SetCompressor",args)); //$NON-NLS-1$
            mScript.addElement(new NSISScriptBlankLine());
        }
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.defines.comment"))); //$NON-NLS-1$
        INSISScriptElement definesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        INSISScriptElement muiDefsPlaceHolder = null;
        if(isMUI) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.muidefs.comment"))); //$NON-NLS-1$
            muiDefsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        }
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.includes.comment"))); //$NON-NLS-1$
        INSISScriptElement includePlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.reservedfiles.comment"))); //$NON-NLS-1$
        INSISScriptElement reservedFilesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.variables.comment"))); //$NON-NLS-1$
        INSISScriptElement varsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.pages.comment"))); //$NON-NLS-1$
        INSISScriptElement pagesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.attributes.comment"))); //$NON-NLS-1$
        INSISScriptElement attributesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.sections.comment"))); //$NON-NLS-1$
        mSectionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        INSISScriptElement unsectionsPlaceHolder = null;
        if(mSettings.isCreateUninstaller()) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.unsections.comment"))); //$NON-NLS-1$
            unsectionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        }
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.functions.comment"))); //$NON-NLS-1$
        INSISScriptElement functionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        INSISScriptElement unfunctionsPlaceHolder = null;
        if(mSettings.isCreateUninstaller()) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.unfunctions.comment"))); //$NON-NLS-1$
            unfunctionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        }

        mScript.insertElement(includePlaceHolder,new NSISScriptInclude("Sections.nsh")); //$NON-NLS-1$
        if(isMUI) {
            mScript.insertElement(includePlaceHolder,new NSISScriptInclude("MUI.nsh")); //$NON-NLS-1$
        }

        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("OutFile",maybeMakeRelative(mSaveFile.getParent(),mSettings.getOutFile()))); //$NON-NLS-1$

        mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("REGKEY",Common.quote("SOFTWARE\\$(^Name)"))); //$NON-NLS-1$ //$NON-NLS-2$

        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("InstallDir",mSettings.getInstallDir())); //$NON-NLS-1$
        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("CRCCheck",getKeyword("on"))); //$NON-NLS-1$ //$NON-NLS-2$
        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("XPStyle",getKeyword("on"))); //$NON-NLS-1$ //$NON-NLS-2$

        String icon = maybeMakeRelative(mSaveFile.getParent(),mSettings.getIcon());
        if(!Common.isEmpty(icon)) {
            if(isMUI) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_ICON",icon)); //$NON-NLS-1$
            }
            else {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("Icon",icon)); //$NON-NLS-1$
            }
        }

        if(!isSilent) {
            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("ShowInstDetails",getKeyword((mSettings.isShowInstDetails()?"show":"hide")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(!mSettings.isAutoCloseInstaller()) {
                if(isMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_NOAUTOCLOSE")); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("AutoCloseWindow",getKeyword("false"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            else if (!isMUI) {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("AutoCloseWindow",getKeyword("true"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else {
            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("SilentInstall",getKeyword("silent"))); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if(isMUI) {
            mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_WELCOME")); //$NON-NLS-1$
            if(mSettings.isShowLicense()) {
                int licenseButtonType = mSettings.getLicenseButtonType();
                switch(licenseButtonType) {
                    case LICENSE_BUTTON_CHECKED:
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LICENSEPAGE_CHECKBOX")); //$NON-NLS-1$
                        break;
                    case LICENSE_BUTTON_RADIO:
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LICENSEPAGE_RADIOBUTTONS")); //$NON-NLS-1$
                        break;
                }
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_LICENSE",new String[]{maybeMakeRelative(mSaveFile.getParent(),mSettings.getLicenseData())})); //$NON-NLS-1$
            }
            if(mSettings.isSelectComponents()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_COMPONENTS")); //$NON-NLS-1$
            }
            if(mSettings.isChangeInstallDir()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_DIRECTORY")); //$NON-NLS-1$
            }
            if(mSettings.isCreateStartMenuGroup()) {
                mScript.insertElement(varsPlaceHolder,new NSISScriptAttribute("Var","StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$
                if(mSettings.isChangeStartMenuGroup()) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_REGISTRY_ROOT",getKeyword("HKLM")));  //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_NODISABLE")); //$NON-NLS-1$
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_REGISTRY_KEY","${REGKEY}"));  //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_REGISTRY_VALUENAME","StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_DEFAULT_FOLDER",mSettings.getStartMenuGroup()));  //$NON-NLS-1$
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_STARTMENU",new String[]{"Application","$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_INSTFILES")); //$NON-NLS-1$
            if(!Common.isEmpty(mSettings.getRunProgramAfterInstall())) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_RUN",mSettings.getRunProgramAfterInstall())); //$NON-NLS-1$
                if(!Common.isEmpty(mSettings.getRunProgramAfterInstallParams())) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_RUN_PARAMETERS",Common.escapeQuotes(mSettings.getRunProgramAfterInstallParams()))); //$NON-NLS-1$
                }
            }
            if(!Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_SHOWREADME",mSettings.getOpenReadmeAfterInstall())); //$NON-NLS-1$
            }
            mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_FINISH")); //$NON-NLS-1$

            if(mSettings.isCreateUninstaller()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_UNPAGE_CONFIRM")); //$NON-NLS-1$
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_UNPAGE_INSTFILES")); //$NON-NLS-1$
            }
        }
        else {
            if(!isSilent) {
                if(mSettings.isShowLicense()) {
                    int licenseButtonType = mSettings.getLicenseButtonType();
                    switch(licenseButtonType) {
                        case LICENSE_BUTTON_CHECKED:
                            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("LicenseForceSelection",getKeyword("checkbox"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case LICENSE_BUTTON_RADIO:
                            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("LicenseForceSelection",getKeyword("radiobuttons"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                    }
                    mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("LicenseData",maybeMakeRelative(mSaveFile.getParent(),mSettings.getLicenseData()))); //$NON-NLS-1$
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("license"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(mSettings.isSelectComponents()) {
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("components"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(mSettings.isChangeInstallDir()) {
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("directory"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            if(mSettings.isCreateStartMenuGroup()) {
                mScript.insertElement(varsPlaceHolder,new NSISScriptAttribute("Var","StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$
                if(!isSilent && mSettings.isChangeStartMenuGroup()) {
                    mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(mNsisDirKeyword+"\\Plugins\\StartMenu.dll"))); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",new String[]{getKeyword("custom"),"StartMenuGroupSelect","", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                               Common.quote((mSettings.isEnableLanguageSupport()?": $(StartMenuPageTitle)": //$NON-NLS-1$
                                                                MessageFormat.format(": {0}",new String[]{ //$NON-NLS-1$
                                                                EclipseNSISPlugin.getResourceString("scriptgen.start.menu.page.title")})))})); //$NON-NLS-1$
                    NSISScriptFunction fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction("StartMenuGroupSelect")); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Push",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptInstruction("StartMenu::Select",new String[]{"/autoadd","/text", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            Common.quote((mSettings.isEnableLanguageSupport()?": $(StartMenuPageText)": //$NON-NLS-1$
                             EclipseNSISPlugin.getResourceString("scriptgen.start.menu.page.text"))), //$NON-NLS-1$
                             "/lastused","$StartMenuGroup",mSettings.getStartMenuGroup()})); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptInstruction("StrCmp",new String[]{getKeyword("$R1"),"success","success"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    fn.addElement(new NSISScriptInstruction("StrCmp",new String[]{getKeyword("$R1"),"cancel","done"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    fn.addElement(new NSISScriptInstruction("MessageBox",new String[]{getKeyword("MB_OK"),getKeyword("$R1")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    fn.addElement(new NSISScriptInstruction("Goto","done")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptLabel("success")); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Pop","$StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptLabel("done")); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(functionsPlaceHolder,new NSISScriptBlankLine());
                }
            }

            mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("instfiles"))); //$NON-NLS-1$ //$NON-NLS-2$
            if(!Common.isEmpty(mSettings.getRunProgramAfterInstall()) ||
               !Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                mScript.addElement(new NSISScriptBlankLine());
                NSISScriptFunction fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction(getKeyword(".onInstSuccess"))); //$NON-NLS-1$
                if(!Common.isEmpty(mSettings.getRunProgramAfterInstall())) {
                    StringBuffer buf = new StringBuffer("$\\\"").append( //$NON-NLS-1$
                            mSettings.getRunProgramAfterInstall()).append(
                            "$\\\""); //$NON-NLS-1$
                    if(!Common.isEmpty(mSettings.getRunProgramAfterInstallParams())) {
                        buf.append(" ").append(Common.escapeQuotes(mSettings.getRunProgramAfterInstallParams())); //$NON-NLS-1$
                    }
                    fn.addElement(new NSISScriptInstruction("Exec",buf.toString())); //$NON-NLS-1$
                    mScript.insertElement(functionsPlaceHolder,new NSISScriptBlankLine());
                }
                if(!Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                    fn.addElement(new NSISScriptInstruction("ExecShell",new String[]{"open",mSettings.getOpenReadmeAfterInstall()})); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        if(!isSilent && mSettings.isShowBackground()) {
            String backgroundBMP = mSettings.getBackgroundBMP();
            String backgroundWAV = mSettings.getBackgroundWAV();
            if(Common.isEmpty(backgroundBMP)) {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("BGGradient", //$NON-NLS-1$
                                 new String[] {
                                      ColorManager.rgbToHex(mSettings.getBGTopColor()),
                                      ColorManager.rgbToHex(mSettings.getBGBottomColor()),
                                      ColorManager.rgbToHex(mSettings.getBGTextColor())}));
                if(!Common.isEmpty(backgroundWAV)) {
                    mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(mNsisDirKeyword+"\\Plugins\\BGImage.dll"))); //$NON-NLS-1$ //$NON-NLS-2$
                    NSISScriptFunction fn;
                    if(isMUI) {
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_CUSTOMFUNCTION_GUIINIT","CustomGUIInit")); //$NON-NLS-1$ //$NON-NLS-2$
                        fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction("CustomGUIInit")); //$NON-NLS-1$
                    }
                    else {
                        fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIInit"))); //$NON-NLS-1$
                    }
                    fn.addElement(new NSISScriptInstruction("File",new String[]{new StringBuffer(getKeyword("/oname")).append( //$NON-NLS-1$ //$NON-NLS-2$
                                                            "=").append(getKeyword("$PLUGINSDIR")).append("\\bgimage.wav").toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                            maybeMakeRelative(mSaveFile.getParent(),backgroundWAV)}));
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound",new String[]{getKeyword("/NOUNLOAD"), //$NON-NLS-1$ //$NON-NLS-2$
                                                                                          "/LOOP", //$NON-NLS-1$
                                                                                          getKeyword("$PLUGINSDIR")+"\\bgimage.wav"})); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(functionsPlaceHolder,new NSISScriptBlankLine());

                    fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIEnd"))); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound","/STOP")); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(functionsPlaceHolder,new NSISScriptBlankLine());
                }
            }
            else {
                mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(mNsisDirKeyword+"\\Plugins\\BGImage.dll"))); //$NON-NLS-1$ //$NON-NLS-2$
                NSISScriptFunction fn;
                if(isMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_CUSTOMFUNCTION_GUIINIT","CustomGUIInit")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction("CustomGUIInit")); //$NON-NLS-1$
                }
                else {
                    fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIInit"))); //$NON-NLS-1$
                }
                fn.addElement(new NSISScriptInstruction("Push",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Push",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("BgImage::SetReturn",new String[]{getKeyword("/NOUNLOAD"),"on"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fn.addElement(new NSISScriptInstruction("BgImage::SetBg",new String[]{getKeyword("/NOUNLOAD"),"/GRADIENT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                new StringBuffer(flattenRGB(mSettings.getBGTopColor()," ")).append(" ").append( //$NON-NLS-1$ //$NON-NLS-2$
                                                 flattenRGB(mSettings.getBGBottomColor()," ")).toString()})); //$NON-NLS-1$
                fn.addElement(new NSISScriptInstruction("Pop","$R1")); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Strcmp",new String[]{getKeyword("$R1"),"success","0","error"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                if(!Common.isEmpty(backgroundBMP)) {
                    fn.addElement(new NSISScriptInstruction("File",new String[]{new StringBuffer(getKeyword("/oname")).append( //$NON-NLS-1$ //$NON-NLS-2$
                                                "=").append(getKeyword("$PLUGINSDIR")).append("\\bgimage.bmp").toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                maybeMakeRelative(mSaveFile.getParent(),backgroundBMP)}));
                    fn.addElement(new NSISScriptInstruction("System::call","user32::GetSystemMetrics(i 0)i.R1")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptInstruction("System::call","user32::GetSystemMetrics(i 1)i.R2")); //$NON-NLS-1$ //$NON-NLS-2$
                    ImageData imageData = new ImageData(mSettings.getBackgroundBMP());
                    fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R1"),getKeyword("$R1"),"-",Integer.toString(imageData.width)})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R1"),getKeyword("$R1"),"/","2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R2"),getKeyword("$R2"),"-",Integer.toString(imageData.height)})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R2"),getKeyword("$R2"),"/","2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    fn.addElement(new NSISScriptInstruction("BGImage::AddImage",new String[]{getKeyword("/NOUNLOAD"), //$NON-NLS-1$ //$NON-NLS-2$
                                    getKeyword("$PLUGINSDIR")+"\\bgimage.bmp",getKeyword("$R1"),getKeyword("$R2")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                fn.addElement(new NSISScriptInstruction("CreateFont",new String[]{getKeyword("$R1"),"Times New Roman","26","700",getKeyword("/ITALIC")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                fn.addElement(new NSISScriptInstruction("BGImage::AddText",new String[]{getKeyword("/NOUNLOAD"),Common.quote("$(^SetupCaption)"),getKeyword("$R1"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                      flattenRGB(mSettings.getBGTextColor()," "),"16","8","500","100"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Strcmp",new String[]{getKeyword("$R1"),"success","0","error"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fn.addElement(new NSISScriptInstruction("BGImage::Redraw",getKeyword("/NOUNLOAD"))); //$NON-NLS-1$ //$NON-NLS-2$
                if(!Common.isEmpty(backgroundWAV)) {
                    fn.addElement(new NSISScriptInstruction("File",new String[]{new StringBuffer(getKeyword("/oname")).append( //$NON-NLS-1$ //$NON-NLS-2$
                                                            "=").append(getKeyword("$PLUGINSDIR")).append("\\bgimage.wav").toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                            maybeMakeRelative(mSaveFile.getParent(),backgroundWAV)}));
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound",new String[]{getKeyword("/NOUNLOAD"), //$NON-NLS-1$ //$NON-NLS-2$
                                                            "/LOOP",getKeyword("$PLUGINSDIR")+"\\bgimage.wav"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                fn.addElement(new NSISScriptInstruction("Goto","done")); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptLabel("error")); //$NON-NLS-1$
                fn.addElement(new NSISScriptInstruction("MessageBox",new String[]{new StringBuffer(getKeyword("MB_OK")).append( //$NON-NLS-1$ //$NON-NLS-2$
                                                        "|").append(getKeyword("MB_ICONSTOP")).toString(),getKeyword("$R1")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fn.addElement(new NSISScriptLabel("done")); //$NON-NLS-1$
                fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(functionsPlaceHolder,new NSISScriptBlankLine());

                fn = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIEnd"))); //$NON-NLS-1$
                if(!Common.isEmpty(backgroundWAV)) {
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound",new String[]{getKeyword("/NOUNLOAD"),"/STOP"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                fn.addElement(new NSISScriptInstruction("BGImage::Destroy")); //$NON-NLS-1$
                mScript.insertElement(functionsPlaceHolder,new NSISScriptBlankLine());
            }
        }

        NSISLanguage defaultLanguage = null;
        if(mSettings.isEnableLanguageSupport()) {
            if(!isSilent && mSettings.isSelectLanguage() && languages.size() > 1) {
                if(isMUI) {
                    mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptInsertMacro("MUI_RESERVEFILE_LANGDLL")); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(mNsisDirKeyword+"\\Plugins\\LangDLL.dll"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            INSISScriptElement languagesPlaceHolder = mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptBlankLine());
            mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.languages.comment"))); //$NON-NLS-1$
            defaultLanguage = (NSISLanguage)languages.get(0);
            for (Iterator iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage language = (NSISLanguage) iter.next();
                if(isMUI) {
                    mScript.insertElement(languagesPlaceHolder,new NSISScriptInsertMacro("MUI_LANGUAGE",language.getName())); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(languagesPlaceHolder,new NSISScriptAttribute("LoadLanguageFile", //$NON-NLS-1$
                                    new StringBuffer(mNsisDirKeyword).append("\\").append(INSISConstants.LANGUAGE_FILES_LOCATION).append( //$NON-NLS-1$
                                    "\\").append(language.getName()).append(INSISConstants.LANGUAGE_FILES_EXTENSION).toString())); //$NON-NLS-1$
                }
            }
        }
        else {
            if(isMUI) {
                defaultLanguage = NSISLanguageManager.getInstance().getLanguage("English"); //$NON-NLS-1$
                mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptBlankLine());
                mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_LANGUAGE",defaultLanguage.getName())); //$NON-NLS-1$
                mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.languages.comment"))); //$NON-NLS-1$
            }
            else {
                defaultLanguage = NSISLanguageManager.getInstance().getDefaultLanguage();
            }
        }

        String version = mSettings.getVersion();
        if(!Common.isEmpty(version)) {
            mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("VERSION",version)); //$NON-NLS-1$
            mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("COMPANY",mSettings.getCompany())); //$NON-NLS-1$
            mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("URL",mSettings.getUrl())); //$NON-NLS-1$

            int[] numbers = new Version(version).getNumbers();
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            int i = 0;
            for (; i < Math.min(numbers.length,4); i++) {
                buf.append((i>0?".":"")).append(numbers[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            for(int j=i; j<4; j++) {
                buf.append((j>0?".0":"0")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String langId=(mSettings.isEnableLanguageSupport()?new StringBuffer(getKeyword("/LANG")).append("=").append(defaultLanguage.getLangDef()).toString():null); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("VIProductVersion",buf.toString())); //$NON-NLS-1$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"ProductName",mSettings.getName()})); //$NON-NLS-1$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey("ProductVersion",new String[]{Common.quote("${VERSION}")})); //$NON-NLS-1$ //$NON-NLS-2$
            if(!Common.isEmpty(mSettings.getCompany())) {
                mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"CompanyName",Common.quote("${COMPANY}")})); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(!Common.isEmpty(mSettings.getUrl())) {
                mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"CompanyWebsite",Common.quote("${URL}")})); //$NON-NLS-1$ //$NON-NLS-2$
            }
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"FileVersion",""})); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"FileDescription",""})); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"LegalCopyright",""})); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if(!isSilent && mSettings.isShowSplash()) {
            if(mSettings.getFadeInDelay() > 0 || mSettings.getFadeOutDelay() > 0) {
                mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(mNsisDirKeyword+"\\Plugins\\AdvSplash.dll"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else {
                mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(mNsisDirKeyword+"\\Plugins\\Splash.dll"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        NSISScriptFunction onInitFunction = (NSISScriptFunction)mScript.insertElement(functionsPlaceHolder,new NSISScriptFunction(getKeyword(".onInit"))); //$NON-NLS-1$
        onInitFunction.addElement(new NSISScriptInstruction("InitPluginsDir")); //$NON-NLS-1$
        if(mSettings.isCreateStartMenuGroup() && (isSilent || !mSettings.isChangeStartMenuGroup())) {
            onInitFunction.addElement(new NSISScriptInstruction("StrCpy",new String[]{"$StartMenuGroup", mSettings.getStartMenuGroup()})); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if(!isSilent) {
            if(mSettings.isShowSplash()) {
                onInitFunction.addElement(new NSISScriptInstruction("Push",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                onInitFunction.addElement(new NSISScriptInstruction("File", //$NON-NLS-1$
                                            new String[]{new StringBuffer(getKeyword("/oname")).append("=").append( //$NON-NLS-1$ //$NON-NLS-2$
                                                    getKeyword("$PLUGINSDIR")).append("\\spltmp.bmp").toString(), //$NON-NLS-1$ //$NON-NLS-2$
                                                    maybeMakeRelative(mSaveFile.getParent(),mSettings.getSplashBMP())}));
                if(!Common.isEmpty(mSettings.getSplashWAV())) {
                    onInitFunction.addElement(new NSISScriptInstruction("File", //$NON-NLS-1$
                                            new String[]{new StringBuffer(getKeyword("/oname")).append("=").append( //$NON-NLS-1$ //$NON-NLS-2$
                                                    getKeyword("$PLUGINSDIR")).append("\\spltmp.wav").toString(), //$NON-NLS-1$ //$NON-NLS-2$
                                                    maybeMakeRelative(mSaveFile.getParent(),mSettings.getSplashWAV())}));
                }
                if(mSettings.getFadeInDelay() > 0 || mSettings.getFadeOutDelay() > 0) {
                    onInitFunction.addElement(new NSISScriptInstruction("advsplash::show",new String[]{ //$NON-NLS-1$
                                        Integer.toString(mSettings.getSplashDelay()),
                                        Integer.toString(mSettings.getFadeInDelay()),
                                        Integer.toString(mSettings.getFadeOutDelay()),
                                        "-1",getKeyword("$PLUGINSDIR")+"\\spltmp" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                     }));
                }
                else {
                    onInitFunction.addElement(new NSISScriptInstruction("splash::show", //$NON-NLS-1$
                            new String[]{
                            Integer.toString(mSettings.getSplashDelay()),
                            getKeyword("$PLUGINSDIR")+"\\spltmp" //$NON-NLS-1$ //$NON-NLS-2$
                         }));
                }
                onInitFunction.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                onInitFunction.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(mSettings.isEnableLanguageSupport() && mSettings.isSelectLanguage() && languages.size() > 1) {
                if(isMUI) {
                    onInitFunction.addElement(new NSISScriptInsertMacro("MUI_LANGDLL_DISPLAY")); //$NON-NLS-1$
                }
                else {
                    onInitFunction.addElement(new NSISScriptInstruction("Push","")); //$NON-NLS-1$ //$NON-NLS-2$
                    for (Iterator iter = languages.iterator(); iter.hasNext();) {
                        NSISLanguage language = (NSISLanguage) iter.next();
                        onInitFunction.addElement(new NSISScriptInstruction("Push",language.getLangDef())); //$NON-NLS-1$
                        onInitFunction.addElement(new NSISScriptInstruction("Push",language.getDisplayName())); //$NON-NLS-1$
                    }
                    onInitFunction.addElement(new NSISScriptInstruction("Push","A")); //$NON-NLS-1$ //$NON-NLS-2$
                    onInitFunction.addElement(new NSISScriptInstruction("LangDLL::LangDialog",new String[]{ //$NON-NLS-1$
                            EclipseNSISPlugin.getResourceString("scriptgen.langdialog.title"),EclipseNSISPlugin.getResourceString("scriptgen.langdialog.message")})); //$NON-NLS-1$ //$NON-NLS-2$
                    onInitFunction.addElement(new NSISScriptInstruction("Pop",getKeyword("$LANGUAGE"))); //$NON-NLS-1$ //$NON-NLS-2$
                    onInitFunction.addElement(new NSISScriptInstruction("StrCmp",new String[]{ //$NON-NLS-1$
                            getKeyword("$LANGUAGE"),Common.quote("cancel"),"0","+2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    onInitFunction.addElement(new NSISScriptInstruction("Abort")); //$NON-NLS-1$
                }
            }
        }

        NSISScriptSection postSection = new NSISScriptSection("post",false,true,false); //$NON-NLS-1$
        postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path",getKeyword("$INSTDIR")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if(!isMUI) {
            if(mSettings.isSelectLanguage() && languages.size() > 1) {
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                        getKeyword("HKLM"),Common.quote("${REGKEY}"),"InstallerLanguage",getKeyword("$LANGUAGE")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            if(mSettings.isCreateStartMenuGroup() && !isSilent && mSettings.isChangeStartMenuGroup()) {
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                        getKeyword("HKLM"),Common.quote("${REGKEY}"),"StartMenuGroup","$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }

        NSISScriptSection unPostSection = null;
        mUnOnInitFunction = null;

        if(mSettings.isCreateUninstaller()) {
            unPostSection = new NSISScriptSection("un.post",false,false,false); //$NON-NLS-1$
            mScript.insertAfterElement(mSectionsPlaceHolder,new NSISScriptBlankLine());
            NSISScriptMacro macro = (NSISScriptMacro)mScript.insertAfterElement(mSectionsPlaceHolder,new NSISScriptMacro("SELECT_UNSECTION",new String[]{"SECTION_NAME","UNSECTION_ID"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            macro.addElement(new NSISScriptInstruction("Push",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{getKeyword("$R0"),getKeyword("HKLM"),Common.quote("${REGKEY}\\Components"),Common.quote("${SECTION_NAME}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            macro.addElement(new NSISScriptInstruction("StrCmp",new String[]{"$R0","1","0","next${UNSECTION_ID}"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            macro.addElement(new NSISScriptInsertMacro("SelectSection",Common.quote("${UNSECTION_ID}"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("GoTo","done${UNSECTION_ID}")); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptLabel("next${UNSECTION_ID}")); //$NON-NLS-1$
            macro.addElement(new NSISScriptInsertMacro("UnselectSection",Common.quote("${UNSECTION_ID}"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptLabel("done${UNSECTION_ID}")); //$NON-NLS-1$
            macro.addElement(new NSISScriptInstruction("Pop",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertAfterElement(mSectionsPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.select.unsection.macro.comment"))); //$NON-NLS-1$

            unPostSection.addElement(new NSISScriptInstruction("RmDir",new String[]{getKeyword("/REBOOTOK"),getKeyword("$INSTDIR")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(mSettings.isCreateStartMenuGroup()) {
                unPostSection.addElement(0,new NSISScriptInstruction("RmDir",new String[]{ //$NON-NLS-1$
                        getKeyword("/REBOOTOK"),getKeyword("$SMPROGRAMS")+"\\$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegKey",new String[]{getKeyword("/IfEmpty"), //$NON-NLS-1$ //$NON-NLS-2$
                    getKeyword("HKLM"),Common.quote("${REGKEY}")})); //$NON-NLS-1$ //$NON-NLS-2$
            unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegKey",new String[]{getKeyword("/IfEmpty"), //$NON-NLS-1$ //$NON-NLS-2$
                    getKeyword("HKLM"),Common.quote("${REGKEY}\\Components")})); //$NON-NLS-1$ //$NON-NLS-2$
            unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                    getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(!isMUI) {
                if(mSettings.isSelectLanguage() && languages.size() > 1) {
                    unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                            getKeyword("HKLM"),Common.quote("${REGKEY}"),"InstallerLanguage"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            if(mSettings.isCreateStartMenuGroup() && !isSilent && mSettings.isChangeStartMenuGroup()) {
                unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                        getKeyword("HKLM"),Common.quote("${REGKEY}"),"StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("InstallDirRegKey",new String[]{getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String unIcon = maybeMakeRelative(mSaveFile.getParent(),mSettings.getUninstallIcon());
            if(!Common.isEmpty(unIcon)) {
                if(isMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_UNICON",unIcon)); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("UninstallIcon",unIcon)); //$NON-NLS-1$
                }
            }
            if(mSettings.isSilentUninstaller()) {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("SilentUnInstall", getKeyword("normal"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("ShowUninstDetails",getKeyword((mSettings.isShowUninstDetails()?"show":"hide")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if(!mSettings.isAutoCloseUninstaller() && isMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_UNFINISHPAGE_NOAUTOCLOSE")); //$NON-NLS-1$
                }
            }
            if(mSettings.isEnableLanguageSupport()&& isMUI && mSettings.isSelectLanguage() &&
                    languages.size() > 1) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LANGDLL_REGISTRY_ROOT",getKeyword("HKLM")));  //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LANGDLL_REGISTRY_KEY","${REGKEY}"));  //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LANGDLL_REGISTRY_VALUENAME","InstallerLanguage")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            mUnOnInitFunction = (NSISScriptFunction)mScript.insertElement(unfunctionsPlaceHolder,new NSISScriptFunction(getKeyword("un.onInit"))); //$NON-NLS-1$

            if(!mSettings.isSilentUninstaller() && mSettings.isAutoCloseUninstaller()) {
                mUnOnInitFunction.addElement(new NSISScriptInstruction("SetAutoClose",getKeyword("true"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            mUnOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{getKeyword("$INSTDIR"),getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            if(mSettings.isCreateStartMenuGroup()) {
                if(isSilent || !mSettings.isChangeStartMenuGroup()) {
                    mUnOnInitFunction.addElement(new NSISScriptInstruction("StrCpy",new String[]{"$StartMenuGroup", mSettings.getStartMenuGroup()})); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                    if(isMUI) {
                        mUnOnInitFunction.addElement(new NSISScriptInsertMacro("MUI_STARTMENU_GETFOLDER",new String[] {"Application","$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    else {
                        mUnOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{"$StartMenuGroup",getKeyword("HKLM"),Common.quote("${REGKEY}"),"StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    }
                }
            }
            if(mSettings.isEnableLanguageSupport()) {
                if(!isSilent && mSettings.isSelectLanguage() && languages.size() > 1) {
                    if(isMUI) {
                        mUnOnInitFunction.addElement(new NSISScriptInsertMacro("MUI_UNGETLANGUAGE")); //$NON-NLS-1$
                    }
                    else {
                        mUnOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{getKeyword("$LANGUAGE"),getKeyword("HKLM"),Common.quote("${REGKEY}"),"InstallerLanguage"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    }
                }
            }

            String uninstallFile = new StringBuffer(getKeyword("$INSTDIR")).append("\\").append(mSettings.getUninstallFile()).toString(); //$NON-NLS-1$ //$NON-NLS-2$
            postSection.addElement(new NSISScriptInstruction("SetOutPath",getKeyword("$INSTDIR"))); //$NON-NLS-1$ //$NON-NLS-2$
            postSection.addElement(new NSISScriptInstruction("WriteUninstaller",uninstallFile)); //$NON-NLS-1$
            unPostSection.addElement(0,new NSISScriptInstruction("Delete",new String[]{getKeyword("/REBOOTOK"),uninstallFile})); //$NON-NLS-1$ //$NON-NLS-2$

            if(mSettings.isCreateStartMenuGroup()) {
                if(isMUI && mSettings.isChangeStartMenuGroup()) {
                    postSection.addElement(new NSISScriptInsertMacro("MUI_STARTMENU_WRITE_BEGIN","Application")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(mSettings.isCreateUninstallerStartMenuShortcut()) {
                    postSection.addElement(new NSISScriptInstruction("SetOutPath",getKeyword("$SMPROGRAMS")+"\\$StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    String startMenuLink = Common.quote(new StringBuffer(getKeyword("$SMPROGRAMS")).append("\\$StartMenuGroup\\").append( //$NON-NLS-1$ //$NON-NLS-2$
                            mSettings.isEnableLanguageSupport()?"$(^UninstallLink)": //$NON-NLS-1$
                                        EclipseNSISPlugin.getResourceString("scriptgen.uninstall.link")).append(".lnk").toString()); //$NON-NLS-1$ //$NON-NLS-2$
                    postSection.addElement(new NSISScriptInstruction("CreateShortcut",new String[]{startMenuLink,uninstallFile})); //$NON-NLS-1$

                    unPostSection.addElement(0,new NSISScriptInstruction("Delete",new String[]{getKeyword("/REBOOTOK"),startMenuLink})); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(isMUI && mSettings.isChangeStartMenuGroup()) {
                    postSection.addElement(new NSISScriptInsertMacro("MUI_STARTMENU_WRITE_END")); //$NON-NLS-1$
                }
            }

            if(mSettings.isCreateUninstallerControlPanelEntry()) {
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"DisplayName",Common.quote("$(^Name)")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                if(!Common.isEmpty(mSettings.getVersion())) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"DisplayVersion",Common.quote("${VERSION}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                if(!Common.isEmpty(mSettings.getCompany())) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"Publisher",Common.quote("${COMPANY}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                if(!Common.isEmpty(mSettings.getUrl())) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"URLInfoAbout",Common.quote("${URL}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"DisplayIcon",uninstallFile})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"UninstallString",uninstallFile})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                postSection.addElement(new NSISScriptInstruction("WriteRegDWORD",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"NoModify","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                postSection.addElement(new NSISScriptInstruction("WriteRegDWORD",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"NoRepair","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegKey",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey)})); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        mUnSectionList = (unPostSection == null?null:new ArrayList());
        INSISInstallElement[] contents =  mSettings.getInstaller().getChildren();
        mSectionCounter = 0;
        mSectionGroupCounter = 0;
        for (int i = 0; i < contents.length; i++) {
            if(contents[i] instanceof NSISSection) {
                mScript.insertElement(mSectionsPlaceHolder, buildSection((NSISSection)contents[i]));
            }
            else if(contents[i] instanceof NSISSectionGroup) {
                mScript.insertElement(mSectionsPlaceHolder, buildSectionGroup((NSISSectionGroup)contents[i]));
            }
            mScript.insertElement(mSectionsPlaceHolder, new NSISScriptBlankLine());
        }
        String sectionId = MessageFormat.format("SEC{0,number,0000}",new Object[]{new Integer(mSectionCounter++)}); //$NON-NLS-1$
        postSection.setIndex(sectionId);
        mScript.insertElement(mSectionsPlaceHolder, postSection);

        if(unPostSection != null) {
            if(!Common.isEmptyCollection(mUnSectionList)) {
                Collections.reverse(mUnSectionList);
                for(Iterator iter=mUnSectionList.iterator(); iter.hasNext(); ) {
                    mScript.insertElement(unsectionsPlaceHolder,(NSISScriptSection)iter.next());
                    mScript.insertElement(unsectionsPlaceHolder, new NSISScriptBlankLine());
                }
            }
            unPostSection.setIndex("UN"+sectionId); //$NON-NLS-1$
            mScript.insertElement(unsectionsPlaceHolder,unPostSection);
        }

        if(mSettings.isEnableLanguageSupport()) {
            Locale defaultLocale = NSISLanguageManager.getInstance().getDefaultLocale();
            ResourceBundle defaultBundle = EclipseNSISPlugin.getDefault().getResourceBundle(defaultLocale);
            NSISScriptlet smScriptlet = new NSISScriptlet();
            NSISScriptlet unlinkScriptlet = new NSISScriptlet();
            for (Iterator iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage language = (NSISLanguage) iter.next();
                Locale locale = NSISLanguageManager.getInstance().getLocaleForLangId(language.getLangId());
                ResourceBundle bundle;
                if(locale.equals(defaultLocale)) {
                    bundle = defaultBundle;
                }
                else {
                    bundle = EclipseNSISPlugin.getDefault().getResourceBundle(locale);
                    if(!bundle.equals(defaultBundle) && !validateLocale(locale,bundle.getLocale())) {
                        bundle = defaultBundle;
                    }
                }
                if(mSettings.isCreateUninstaller() && mSettings.isCreateStartMenuGroup() && mSettings.isCreateUninstallerStartMenuShortcut()) {
                    unlinkScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                                    new String[]{"^UninstallLink",language.getLangDef(), //$NON-NLS-1$
                                   bundle.getString("scriptgen.uninstall.link")})); //$NON-NLS-1$
                }
                if(!isMUI && mSettings.isCreateStartMenuGroup() && !isSilent && mSettings.isChangeStartMenuGroup()) {
                    smScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                            new String[]{"StartMenuPageTitle",language.getLangDef(), //$NON-NLS-1$
                           bundle.getString("scriptgen.start.menu.page.title")})); //$NON-NLS-1$
                    smScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                            new String[]{"StartMenuPageText",language.getLangDef(), //$NON-NLS-1$
                           bundle.getString("scriptgen.start.menu.page.text")})); //$NON-NLS-1$
                }
            }
            if(smScriptlet.size() > 0 || unlinkScriptlet.size() > 0) {
                mScript.addElement(new NSISScriptBlankLine());
                mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.langstring.comment1"))); //$NON-NLS-1$
                mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getFormattedString("scriptgen.langstring.comment2",new Object[]{defaultTaskTag}).trim())); //$NON-NLS-1$
                if(smScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(smScriptlet);
                }
                if(unlinkScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(unlinkScriptlet);
                }
            }
        }
        mWriter.print(NSISScriptSingleLineComment.PREFIX_HASH+" "); //$NON-NLS-1$
        mWriter.println(EclipseNSISPlugin.getResourceString("scriptgen.header.comment")); //$NON-NLS-1$
        mWriter.print(NSISScriptSingleLineComment.PREFIX_HASH+" "); //$NON-NLS-1$
        mWriter.println(DateFormat.getDateTimeInstance().format(new Date()));
        mWriter.println();
        mScript.write(new NSISScriptWriter(mWriter));
        mWriter.flush();
        mWriter.close();
    }

    /**
     * @param langId
     * @param args
     */
    private NSISScriptAttribute createVersionInfoKey(String langId, String[] args)
    {
        if(langId != null) {
            String[] temp = new String[Common.isEmptyArray(args)?1:args.length+1];
            temp[0]=langId;
            if(!Common.isEmptyArray(args)) {
                System.arraycopy(args,0,temp,1,args.length);
            }
            args = temp;
        }
        return new NSISScriptAttribute("VIAddVersionKey",args); //$NON-NLS-1$
    }

    private NSISScriptSectionGroup buildSectionGroup(NSISSectionGroup subsec)
    {
        NSISScriptSectionGroup scriptSecgrp = new NSISScriptSectionGroup(subsec.getCaption(),subsec.isDefaultExpanded(),subsec.isBold(),
                                                                MessageFormat.format("SECGRP{0,number,0000}",new Object[]{new Integer(mSectionGroupCounter++)})); //$NON-NLS-1$
        INSISInstallElement[] children = subsec.getChildren();
        if(!Common.isEmptyArray(children)) {
            scriptSecgrp.addElement((children[0] instanceof NSISSectionGroup?
                                        (INSISScriptElement)buildSectionGroup((NSISSectionGroup)children[0]):
                                        (INSISScriptElement)buildSection((NSISSection)children[0])));
            for (int i = 1; i < children.length; i++) {
                scriptSecgrp.addElement(new NSISScriptBlankLine());
                scriptSecgrp.addElement((children[i] instanceof NSISSectionGroup?
                        (INSISScriptElement)buildSectionGroup((NSISSectionGroup)children[i]):
                        (INSISScriptElement)buildSection((NSISSection)children[i])));
            }
        }

        return scriptSecgrp;
    }

    private NSISScriptSection buildSection(NSISSection sec)
    {
        String sectionId = MessageFormat.format("SEC{0,number,0000}",new Object[]{new Integer(mSectionCounter++)}); //$NON-NLS-1$
        String unSectionId = "UN"+sectionId; //$NON-NLS-1$
        NSISScriptSection section = new NSISScriptSection(sec.getName(),sec.isBold(), sec.isHidden(), sec.isDefaultUnselected(),
                sectionId);
        NSISScriptSection unSection = null;

        if(mUnSectionList!=null) {
            mUnOnInitFunction.addElement(new NSISScriptInsertMacro("SELECT_UNSECTION",new String[]{sec.getName(),new StringBuffer("${").append( //$NON-NLS-1$ //$NON-NLS-2$
                    unSectionId).append("}").toString()})); //$NON-NLS-1$

            unSection = new NSISScriptSection("un."+sec.getName(),false, false, true,  //$NON-NLS-1$
                                              unSectionId);
            mUnSectionList.add(unSection);
        }
        INSISInstallElement[] children = sec.getChildren();
        String outdir = ""; //$NON-NLS-1$
        int overwriteMode = -1;
        for (int i = 0; i < children.length; i++) {
            String type = children[i].getType();
            if(children[i] instanceof INSISInstallFileSystemObject) {
                INSISInstallFileSystemObject fsObject = (INSISInstallFileSystemObject)children[i];
                String tempOutdir = fsObject.getDestination();
                if (type.equals(NSISInstallDirectory.TYPE)) {
                    String sourceDir = ((NSISInstallDirectory)children[i]).getName();
                    tempOutdir = new StringBuffer(tempOutdir).append("\\").append(new Path(sourceDir).lastSegment()).toString(); //$NON-NLS-1$
                }
                if(!outdir.equalsIgnoreCase(tempOutdir)) {
                    outdir = tempOutdir;
                    section.addElement(new NSISScriptInstruction("SetOutPath",outdir)); //$NON-NLS-1$
                }
                if(overwriteMode != fsObject.getOverwriteMode()) {
                    overwriteMode = fsObject.getOverwriteMode();
                    switch(overwriteMode) {
                        case OVERWRITE_ON:
                            section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("on"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case OVERWRITE_OFF:
                            section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("off"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case OVERWRITE_TRY:
                            section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("try"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case OVERWRITE_NEWER:
                            section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("ifnewer"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case OVERWRITE_IFDIFF:
                            section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("ifdiff"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        default:
                            section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("lastused"))); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                if(type.equals(NSISInstallFile.TYPE)) {
                    section.addElement(new NSISScriptInstruction("File",maybeMakeRelative(mSaveFile.getParent(),((NSISInstallFile)children[i]).getName()))); //$NON-NLS-1$
                    if(unSection != null) {
                        unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                                                    new String[]{getKeyword("/REBOOTOK"), //$NON-NLS-1$
                                new StringBuffer(outdir).append("\\").append(new Path(((NSISInstallFile)children[i]).getName()).lastSegment()).toString()})); //$NON-NLS-1$
                    }
                }
                else if (type.equals(NSISInstallFiles.TYPE)) {
                    INSISInstallElement[] children2 = ((NSISInstallFiles)children[i]).getChildren();
                    for (int j = 0; j < children2.length; j++) {
                        section.addElement(new NSISScriptInstruction("File",maybeMakeRelative(mSaveFile.getParent(),((NSISInstallFiles.FileItem)children2[j]).getName()))); //$NON-NLS-1$
                        if(unSection != null) {
                            unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                                                        new String[]{getKeyword("/REBOOTOK"), //$NON-NLS-1$
                                    new StringBuffer(outdir).append("\\").append(new Path(((NSISInstallFiles.FileItem)children2[j]).getName()).lastSegment()).toString()})); //$NON-NLS-1$
                        }
                    }
                }
                else if (type.equals(NSISInstallDirectory.TYPE)) {
                    NSISInstallDirectory installDirectory = (NSISInstallDirectory)children[i];
                    String name = installDirectory.getDisplayName() + "\\*"; //$NON-NLS-1$
                    if(installDirectory.isRecursive()) {
                        section.addElement(new NSISScriptInstruction("File",new String[]{getKeyword("/r"),maybeMakeRelative(mSaveFile.getParent(),name)})); //$NON-NLS-1$ //$NON-NLS-2$
                        if(unSection != null) {
                            if(mNewRmDirUsage) {
                                unSection.addElement(0,new NSISScriptInstruction("RmDir", //$NON-NLS-1$
                                                                            new String[]{getKeyword("/r"), //$NON-NLS-1$
                                                                                         getKeyword("/REBOOTOK"), //$NON-NLS-1$
                                                                                         outdir}));
                            }
                            else {
                                unSection.addElement(0,new NSISScriptInstruction("RmDir", //$NON-NLS-1$
                                                                            new String[]{getKeyword("/r"), //$NON-NLS-1$
                                                                                         outdir}));
                            }
                        }
                    }
                    else {
                        section.addElement(new NSISScriptInstruction("File",maybeMakeRelative(mSaveFile.getParent(),name))); //$NON-NLS-1$
                        if(unSection != null) {
                            unSection.addElement(0,new NSISScriptInstruction("RmDir", //$NON-NLS-1$
                                    new String[]{getKeyword("/REBOOTOK"),outdir})); //$NON-NLS-1$
                            unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                    new String[]{getKeyword("/REBOOTOK"),outdir+"\\*"})); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
            else if (type.equals(NSISInstallShortcut.TYPE)) {
                NSISInstallShortcut shortcut = (NSISInstallShortcut)children[i];
                if(!outdir.equalsIgnoreCase(shortcut.getLocation())) {
                    outdir = shortcut.getLocation();
                    section.addElement(new NSISScriptInstruction("SetOutPath",outdir)); //$NON-NLS-1$
                }
                String location = new StringBuffer(outdir).append("\\").append( //$NON-NLS-1$
                                                   shortcut.getName()).append(".lnk").toString(); //$NON-NLS-1$
                section.addElement(new NSISScriptInstruction("CreateShortcut",new String[]{location, //$NON-NLS-1$
                        (shortcut.getShortcutType()==SHORTCUT_INSTALLELEMENT?shortcut.getPath():shortcut.getUrl())}));
                if(unSection != null) {
                    unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                                                new String[]{getKeyword("/REBOOTOK"),location})); //$NON-NLS-1$
                }
            }
            else if (type.equals(NSISInstallRegistryKey.TYPE)) {
                NSISInstallRegistryKey regKey = (NSISInstallRegistryKey)children[i];
                String rootKey = ""; //$NON-NLS-1$
                if(regKey.getRootKey() >= 0 && regKey.getRootKey() < mHKeyNames.length) {
                    rootKey = mHKeyNames[regKey.getRootKey()];
                }
                section.addElement(new NSISScriptInstruction("WriteRegStr", //$NON-NLS-1$
                                   new String[]{
                                       rootKey,
                                       regKey.getSubKey(),"","" //$NON-NLS-1$ //$NON-NLS-2$
                                       }));
                if(unSection != null) {
                    unSection.addElement(0,new NSISScriptInstruction("DeleteRegKey", //$NON-NLS-1$
                                                                new String[]{getKeyword("/IfEmpty"), //$NON-NLS-1$
                                                                rootKey,
                                                                regKey.getSubKey()}));
                }
            }
            else if (type.equals(NSISInstallRegistryValue.TYPE)) {
                NSISInstallRegistryValue regValue = (NSISInstallRegistryValue)children[i];
                String rootKey = ""; //$NON-NLS-1$
                if(regValue.getRootKey() >= 0 && regValue.getRootKey() < mHKeyNames.length) {
                    rootKey = mHKeyNames[regValue.getRootKey()];
                }
                section.addElement(new NSISScriptInstruction(
                                   (regValue.getValueType()==REG_DWORD?"WriteRegDWORD":"WriteRegStr"), //$NON-NLS-1$ //$NON-NLS-2$
                                   new String[]{rootKey,regValue.getSubKey(),regValue.getValue(),
                                           regValue.getData()
                                       }));
                if(unSection != null) {
                    unSection.addElement(0,new NSISScriptInstruction("DeleteRegValue", //$NON-NLS-1$
                                                                new String[]{rootKey,
                                                                regValue.getSubKey(),
                                                                regValue.getValue()}));
                }
            }
        }
        section.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                getKeyword("HKLM"),Common.quote("${REGKEY}\\Components"),sec.getName(),"1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if(unSection != null) {
            unSection.addElement(new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                getKeyword("HKLM"),Common.quote("${REGKEY}\\Components"),sec.getName()})); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return section;
    }

    private boolean validateLocale(Locale requested, Locale received)
    {
        if(!requested.equals(received)) {
            String country1 = requested.getCountry();
            String language1 = requested.getLanguage();
            String country2 = received.getCountry();
            String language2 = received.getLanguage();
            if(Common.isEmpty(received.getVariant())) {
                if(country1.equals(country2)) {
                    return (Common.isEmpty(language2) || language1.equals(language2));
                }
            }
            return false;
        }
        return true;
    }

    private String getKeyword(String keyword)
    {
        return NSISKeywords.getInstance().getKeyword(keyword);
    }

    private String flattenRGB(RGB rgb, String separator)
    {
        return new StringBuffer("").append(rgb.red).append(separator).append(rgb.green).append( //$NON-NLS-1$
                                separator).append(rgb.blue).toString();
    }
}
