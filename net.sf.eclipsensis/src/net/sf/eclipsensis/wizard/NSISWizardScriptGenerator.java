/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.*;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.Version;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
    private static final int SUB_TASK = 3;
    private static final int INDENT_INCREMENT_LENGTH = 4;
    private static final String INDENT_INCREMENT = "    ";
    
    private NSISWizardSettings mSettings = null;
    private PrintWriter mWriter = null;
    private IProgressMonitor mMonitor = null;
    private IFile mSaveFile;
    private String mIndent = "";
    
    public NSISWizardScriptGenerator(NSISWizardSettings settings)
    {
        mSettings = settings;
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
                    mMonitor.beginTask(message,mSettings.getWorkCount()+2);
                    break;
                case SUB_TASK:
                    mMonitor.subTask(message);
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
            boolean isSilent = false;
            boolean isMUI = false;
            
            mMonitor = monitor;
            String savePath = mSettings.getSavePath();
            updateMonitorTask("scriptgen.create.message",savePath,BEGIN_TASK);

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
            }).start();

            mSaveFile.create(is,true,null);
            mWriter = null;
            incrementMonitor(1);
            
            updateMonitorTask("scriptgen.open.message",savePath,SET_TASK_NAME);
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
                updateMonitorTask("scriptgen.compile.message",savePath,SET_TASK_NAME);
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        if(page != null) {
                            IEditorPart editor = page.getActiveEditor();
                            if(editor != null && editor instanceof NSISEditor) {
                                IAction action = ((NSISEditor)editor).getAction(INSISConstants.COMPILE_ACTION_ID);
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
        if(!Common.isEmpty(pathname) && mSettings.isMakePathsRelative()) {
            pathname = Common.makeRelativeLocation(reference,pathname);
        }
        return pathname;
    }

    private void writeScript()
    {
        boolean isSilent = false;
        boolean isMUI = false;
        List languages = mSettings.getLanguages();

        updateMonitorTask("scriptgen.attributes.message",null,SUB_TASK);
        switch(mSettings.getCompressorType()) {
            case MakeNSISRunner.COMPRESSOR_DEFAULT:
                break;
            default:
                writeAttribute("SetCompressor",MakeNSISRunner.COMPRESSOR_NAME_ARRAY[mSettings.getCompressorType()]);
                mWriter.println();
        }
        
        switch(mSettings.getInstallerType()) {
            case INSTALLER_TYPE_SILENT:
                isSilent = true;
                break;
            case INSTALLER_TYPE_MUI:
                isMUI = true;
                writeLine("!include \"MUI.nsh\"", 1);
                mWriter.println();
                break;
        }
        
        IContainer saveFolder = mSaveFile.getParent();

        writeAttribute("Name",mSettings.getName());
        writeAttribute("OutFile",maybeMakeRelative(saveFolder,mSettings.getOutFile()));
        mWriter.println();
        if(mSettings.isCreateUninstaller()) {
            writeDefine("REGKEY","SOFTWARE\\$(^Name)",false);
        }
        String version = mSettings.getVersion();
        if(!Common.isEmpty(version)) {
            writeDefine("VERSION",version,false);
            writeDefine("COMPANY",mSettings.getCompany(),false);
            writeDefine("URL",mSettings.getUrl(),false);
            mWriter.println();
            int[] numbers = new Version(version).getNumbers();
            StringBuffer buf = new StringBuffer("");
            int i = 0;
            for (; i < Math.min(numbers.length,4); i++) {
                buf.append((i>0?".":"")).append(numbers[i]);
            }
            for(int j=i; j<4; j++) {
                buf.append((j>0?".0":"0"));
            }
            writeAttribute("VIProductVersion",buf.toString());
            writeAttribute("VIAddVersionKey ProductName",mSettings.getName());
            writeLine("VIAddVersionKey ProductVersion \"${VERSION}\"",1);
            if(!Common.isEmpty(mSettings.getCompany())) {
                writeLine("VIAddVersionKey CompanyName \"${COMPANY}\"",1);
            }
            if(!Common.isEmpty(mSettings.getUrl())) {
                writeLine("VIAddVersionKey CompanyWebsite \"${URL}\"",1);
            }
            writeAttribute("VIAddVersionKey FileVersion","");
            writeAttribute("VIAddVersionKey FileDescription","");
            writeAttribute("VIAddVersionKey LegalCopyright","");
        }
        mWriter.println();

        writeAttribute("InstallDir",mSettings.getInstallDir());
        if(mSettings.isCreateUninstaller()) {
            writeLine("InstallDirRegKey HKLM \"${REGKEY}\" Path",1);
        }
        if(isSilent) {
            writeLine("SilentInstall",1);
        }
        writeLine("CRCCheck on",1);
        writeLine("XPStyle on",1);
        mWriter.println();

        String icon = maybeMakeRelative(saveFolder,mSettings.getIcon());
        if(!Common.isEmpty(icon)) {
            if(isMUI) {
                writeDefine("MUI_ICON",icon,false);
            }
            else {
                writeAttribute("Icon",icon);
            }
        }
        if(mSettings.isCreateUninstaller()) {
            String unIcon = maybeMakeRelative(saveFolder,mSettings.getUninstallIcon());
            if(!Common.isEmpty(unIcon)) {
                if(isMUI) {
                    writeDefine("MUI_UNICON",unIcon,false);
                }
                else {
                    writeAttribute("UninstallIcon",unIcon);
                }
            }
        }
        mWriter.println();
        
        if(!isSilent) {
            writeAttribute("ShowInstDetails",(mSettings.isShowInstDetails()?"show":"hide"));
            if(!mSettings.isAutoCloseInstaller()) {
                if(isMUI) {
                    writeLine("!define MUI_FINISHPAGE_NOAUTOCLOSE",1);
                }
                else {
                    writeLine("AutoCloseWindow false",1);
                }
                mWriter.println();
            }
            else if (!isMUI) {
                writeLine("AutoCloseWindow true",1);
                mWriter.println();
            }
        }

        if(mSettings.isCreateUninstaller()) {
            if(mSettings.isSilentUninstaller()) {
                writeAttribute("SilentUnInstall",null);
            }
            else {
                writeAttribute("ShowUninstDetails",(mSettings.isShowUninstDetails()?"show":"hide"));
                if(!mSettings.isAutoCloseUninstaller() && isMUI) {
                    writeLine("!define MUI_UNFINISHPAGE_NOAUTOCLOSE",1);
                }
            }
            mWriter.println();
        }

        if(mSettings.isCreateUninstaller() && mSettings.isEnableLanguageSupport()&&
           isMUI && mSettings.isSelectLanguage() && languages.size() > 1) {
            writeDefine("MUI_LANGDLL_REGISTRY_ROOT","HKLM",false); 
            writeDefine("MUI_LANGDLL_REGISTRY_KEY","${REGKEY}",false); 
            writeDefine("MUI_LANGDLL_REGISTRY_VALUENAME","Installer Language",false);
        }
        
        updateMonitorTask("scriptgen.pages.message",null,SUB_TASK);
        if(isMUI) {
            writeLine("!insertmacro MUI_PAGE_WELCOME",1);
            if(mSettings.isShowLicense()) {
                int licenseButtonType = mSettings.getLicenseButtonType();
                switch(licenseButtonType) {
                    case LICENSE_BUTTON_CHECKED:
                        writeLine("!define MUI_LICENSEPAGE_CHECKBOX",1);
                        break;
                    case LICENSE_BUTTON_RADIO:
                        writeLine("!define MUI_LICENSEPAGE_RADIOBUTTONS",1);
                        break;
                }
                writeInsertMacro("MUI_PAGE_LICENSE",new String[]{maybeMakeRelative(saveFolder,mSettings.getLicenseData())});
            }
            if(mSettings.isSelectComponents()) {
                writeLine("!insertmacro MUI_PAGE_COMPONENTS",1);
            }
            if(mSettings.isChangeInstallDir()) {
                writeLine("!insertmacro MUI_PAGE_DIRECTORY",1);
            }
            if(mSettings.isCreateStartMenuGroup() && mSettings.isChangeStartMenuGroup()) {
                mWriter.println();
                writeLine("!define MUI_STARTMENUPAGE_REGISTRY_ROOT \"HKCU\"",1); 
                writeDefine("MUI_STARTMENUPAGE_REGISTRY_KEY","Software\\"+mSettings.getName(),false); 
                writeLine("!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup",1);
                writeDefine("MUI_STARTMENUPAGE_DEFAULT_FOLDER",mSettings.getStartMenuGroup(),false); 
                writeLine("Var StartMenuGroup",1);
                writeLine("!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup",1);
                mWriter.println();
            }
            writeLine("!insertmacro MUI_PAGE_INSTFILES",1);
            mWriter.println();
            if(!Common.isEmpty(mSettings.getRunProgramAfterInstall())) {
                writeDefine("MUI_FINISHPAGE_RUN",mSettings.getRunProgramAfterInstall(),false);
                if(!Common.isEmpty(mSettings.getRunProgramAfterInstallParams())) {
                    writeDefine("MUI_FINISHPAGE_RUN_PARAMETERS",escapeQuotes(mSettings.getRunProgramAfterInstallParams()),false);
                }
            }
            if(!Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                writeDefine("MUI_FINISHPAGE_SHOWREADME",mSettings.getOpenReadmeAfterInstall(),false);
            }
            writeLine("!insertmacro MUI_PAGE_FINISH",1);
        }
        else {
            if(!isSilent) {
                if(mSettings.isShowLicense()) {
                    int licenseButtonType = mSettings.getLicenseButtonType();
                    switch(licenseButtonType) {
                        case LICENSE_BUTTON_CHECKED:
                            writeLine("LicenseForceSelection checkbox",1);
                            break;
                        case LICENSE_BUTTON_RADIO:
                            writeLine("LicenseForceSelection radiobuttons",1);
                            break;
                    }
                    writeAttribute("LicenseData",maybeMakeRelative(saveFolder,mSettings.getLicenseData()));
                    writeLine("Page license",1);
                }
                if(mSettings.isSelectComponents()) {
                    writeLine("Page components",1);
                }
                if(mSettings.isChangeInstallDir()) {
                    writeLine("Page directory",1);
                }
            }
            
            if(mSettings.isCreateStartMenuGroup()) {
                if(!isSilent && mSettings.isChangeStartMenuGroup()) {
                    writeAttribute("ReserveFile","${NSISDIR}\\Plugins\\StartMenu.dll");
                    writeLine("Page custom StartMenuGroupSelect",1);
                    mWriter.println();
                    writeLine("Var StartMenuGroup",1);
                    mWriter.println();
                    writeFunction("StartMenuGroupSelect");
                    writeLine("Caption \": Start Menu Folder\"");
                    writeLine("Push $R1");
                    writeInstruction("StartMenu::Select /autoadd /lastused $StartMenuGroup", mSettings.getStartMenuGroup());
                    writeLine("Pop $R1");
                    writeLine("StrCmp $R1 \"success\" success");
                    writeLine("StrCmp $R1 \"cancel\" done");
                    writeLine("MessageBox MB_OK $R1");
                    writeLine("Goto done");
                    writeLine("success:");
                    writeLine("Pop $StartMenuGroup");
                    writeLine("done:");
                    writeLine("Pop $R1");
                    writeFunctionEnd();
                }
                //TODO Add startmenu to registry
            }

            writeLine("Page instfiles",1);
            if(!Common.isEmpty(mSettings.getRunProgramAfterInstall()) ||
               !Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                mWriter.println();
                writeFunction(".onInstSuccess");
                if(!Common.isEmpty(mSettings.getRunProgramAfterInstall())) {
                    StringBuffer buf = new StringBuffer("Exec \"$\\\"").append(
                            mSettings.getRunProgramAfterInstall()).append(
                            "$\\\"");
                    write("Exec \"$\\\""+mSettings.getRunProgramAfterInstall()+"$\\\"");
                    if(!Common.isEmpty(mSettings.getRunProgramAfterInstallParams())) {
                        buf.append(" ").append(escapeQuotes(mSettings.getRunProgramAfterInstallParams()));
                    }
                    writeLine(buf.toString());
                    mWriter.println("\"");
                }
                if(!Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                    writeLine("ExecShell \"open\" \""+mSettings.getOpenReadmeAfterInstall()+"\"");
                }
                writeFunctionEnd();
            }
            mWriter.println();
        }

        updateMonitorTask("scriptgen.initfuncs.message",null,SUB_TASK);
        
        if(!isSilent && mSettings.isShowBackground()) {
            String backgroundBMP = mSettings.getBackgroundBMP();
            String backgroundWAV = mSettings.getBackgroundWAV();
            if(Common.isEmpty(backgroundBMP)) {
                writeInstruction("BGGradient",
                                 new String[] {
                                      ColorManager.rgbToHex(mSettings.getBGTopColor()),
                                      ColorManager.rgbToHex(mSettings.getBGBottomColor()),
                                      ColorManager.rgbToHex(mSettings.getBGTextColor())});
                if(!Common.isEmpty(backgroundWAV)) {
                    mWriter.println();
                    writeAttribute("ReserveFile","${NSISDIR}\\Plugins\\BGImage.dll");
                    if(isMUI) {
                        writeLine("!define MUI_CUSTOMFUNCTION_GUIINIT CustomGUIInit");
                        writeFunction("CustomGUIInit");
                    }
                    else {
                        writeFunction(".onGUIInit");
                    }
                    writeInstruction("File /oname=$PLUGINSDIR\\bgimage.wav",maybeMakeRelative(saveFolder,backgroundWAV));
                    writeLine("BGImage::Sound /NOUNLOAD /LOOP $PLUGINSDIR\\bgimage.wav");
                    writeFunctionEnd();
                    mWriter.println();
                    writeFunction(".onGUIEnd");
                    writeLine("BGImage::Sound /STOP");
                    writeFunctionEnd();
                }
            }
            else {
                writeAttribute("ReserveFile","${NSISDIR}\\Plugins\\BGImage.dll");
                if(isMUI) {
                    writeLine("!define MUI_CUSTOMFUNCTION_GUIINIT CustomGUIInit");
                    writeFunction("CustomGUIInit");
                }
                else {
                    writeFunction(".onGUIInit");
                }
                writeLine("Push $R1");
                writeLine("Push $R2");
                writeLine("BgImage::SetReturn /NOUNLOAD on");
                writeLine(new StringBuffer("BgImage::SetBg /NOUNLOAD /GRADIENT ").append(
                            flattenRGB(mSettings.getBGTopColor()," ")).append(" ").append(
                            flattenRGB(mSettings.getBGBottomColor()," ")).toString());
                writeLine("Pop $R1");
                writeLine("Strcmp $R1 \"success\" 0 error");
                if(!Common.isEmpty(backgroundBMP)) {
                    writeInstruction("File /oname=$PLUGINSDIR\\bgimage.bmp",maybeMakeRelative(saveFolder,backgroundBMP));
                    writeLine("System::call \"user32::GetSystemMetrics(i 0)i.R1\"");
                    writeLine("System::call \"user32::GetSystemMetrics(i 1)i.R2\"");
                    ImageData imageData = new ImageData(mSettings.getBackgroundBMP());
                    writeLine("IntOp $R1 $R1 - "+Integer.toString(imageData.width));
                    writeLine("IntOp $R1 $R1 / 2");
                    writeLine("IntOp $R2 $R2 - "+Integer.toString(imageData.height));
                    writeLine("IntOp $R2 $R2 / 2");
                    writeLine("BGImage::AddImage /NOUNLOAD $PLUGINSDIR\\bgimage.bmp $R1 $R2");
                }
                writeLine("CreateFont $R1 \"Times New Roman\" 26 700 /ITALIC");
                writeLine(new StringBuffer("BGImage::AddText /NOUNLOAD $(^SetupCaption) $R1 ").append(
                            flattenRGB(mSettings.getBGTextColor()," ")).append(" 16 8 500 100").toString());
                writeLine("Pop $R1");
                writeLine("Strcmp $R1 \"success\" 0 error");
                writeLine("BGImage::Redraw /NOUNLOAD");
                if(!Common.isEmpty(backgroundWAV)) {
                    writeInstruction("File /oname=$PLUGINSDIR\\bgimage.wav",maybeMakeRelative(saveFolder,backgroundWAV));
                    writeLine("BGImage::Sound /NOUNLOAD /LOOP $PLUGINSDIR\\bgimage.wav");
                }
                writeLine("Goto done");
                writeLine("error:");
                writeLine("MessageBox MB_OK|MB_ICONSTOP $R1");
                writeLine("done:");
                writeLine("Pop $R2");
                writeLine("Pop $R1");
                writeFunctionEnd();
                mWriter.println();
                writeFunction(".onGUIEnd");
                if(!Common.isEmpty(backgroundWAV)) {
                    writeLine("BGImage::Sound /NOUNLOAD /STOP");
                }
                writeLine("BGImage::Destroy");
                writeFunctionEnd();
            }
            mWriter.println();
        }

        if(mSettings.isEnableLanguageSupport()) {
            if(!isSilent && mSettings.isSelectLanguage() && languages.size() > 1) {
                if(isMUI) {
                    writeInsertMacro("MUI_RESERVEFILE_LANGDLL",null);
                }
                else {
                    writeAttribute("ReserveFile","${NSISDIR}\\Plugins\\LangDLL.dll");
                }
                mWriter.println();
            }

            if(languages.size() > 1) {
                writeLine("; First is default");
            }
            for (Iterator iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage language = (NSISLanguage) iter.next();
                if(isMUI) {
                    writeInsertMacro("MUI_LANGUAGE",new String[]{language.getName()});
                }
                else {
                    writeAttribute("LoadLanguageFile",new StringBuffer("${NSISDIR}\\").append(INSISConstants.LANGUAGE_FILES_LOCATION).append(
                                    "\\").append(language.getName()).append(INSISConstants.LANGUAGE_FILES_EXTENSION).toString());
                }
            }
            mWriter.println();
        }
        else {
            if(isMUI) {
                writeInsertMacro("MUI_LANGUAGE",new String[]{"English"});
            }
        }

        if(!isSilent && mSettings.isShowSplash()) {
            if(mSettings.getFadeInDelay() > 0 || mSettings.getFadeOutDelay() > 0) {
                writeAttribute("ReserveFile","${NSISDIR}\\Plugins\\AdvSplash.dll");
            }
            else {
                writeAttribute("ReserveFile","${NSISDIR}\\Plugins\\Splash.dll");
            }
            mWriter.println();
        }

        writeFunction(".onInit");
        writeLine("InitPluginsDir");
        if(!isSilent) {
            if(mSettings.isShowSplash()) {
                writeLine("Push $R1");
                writeInstruction("File /oname=$PLUGINSDIR\\spltmp.bmp",maybeMakeRelative(saveFolder,mSettings.getSplashBMP()));
                if(!Common.isEmpty(mSettings.getSplashWAV())) {
                    writeInstruction("File /oname=$PLUGINSDIR\\spltmp.wav",maybeMakeRelative(saveFolder,mSettings.getSplashWAV()));
                }
                if(mSettings.getFadeInDelay() > 0 || mSettings.getFadeOutDelay() > 0) {
                    writeInstruction("advsplash::show",new String[]{
                                        Integer.toString(mSettings.getSplashDelay()),
                                        Integer.toString(mSettings.getFadeInDelay()),
                                        Integer.toString(mSettings.getFadeOutDelay()),
                                        "-1","$PLUGINSDIR\\spltmp"
                                     });
                }
                else {
                    writeInstruction("splash::show",new String[]{
                            Integer.toString(mSettings.getSplashDelay()),
                            "$PLUGINSDIR\\spltmp"
                         });
                }
                writeLine("Pop $R1");
                writeLine("Pop $R1");
            }
            if(mSettings.isSelectLanguage() && languages.size() > 1) {
                if(isMUI) {
                    writeInsertMacro("MUI_LANGDLL_DISPLAY",null);
                }
                else {
                    writeLine("Push \"\"");
                    for (Iterator iter = languages.iterator(); iter.hasNext();) {
                        writeInstruction("Push",new StringBuffer("${LANG_").append(
                                         ((NSISLanguage) iter.next()).getName().toUpperCase()).append(
                                         "}").toString());
                    }
                    writeLine("Push A");
                    writeLine("LangDLL::LangDialog \"Installer Language\" \"Please select the language of the installer\"");
                    writeLine("Pop $LANGUAGE");
                    writeLine("StrCmp $LANGUAGE \"cancel\" 0 +2");
                    writeLine("Abort");
                }
            }
        }
        writeFunctionEnd();
        mWriter.println();

        if(mSettings.isCreateUninstaller() && !mSettings.isSilentUninstaller() && mSettings.isAutoCloseUninstaller()) {
            writeFunction("un.onInit");
            writeInstruction("SetAutoClose","true");
            writeFunctionEnd();
            mWriter.println();
        }

        if(mSettings.isCreateUninstaller() && mSettings.isCreateStartMenuGroup() && 
           mSettings.isCreateUninstallerStartMenuShortcut()) {
            if(mSettings.isEnableLanguageSupport()) {
                for (Iterator iter = languages.iterator(); iter.hasNext();) {
                    NSISLanguage language = (NSISLanguage) iter.next();
                    
                    String langdef = new StringBuffer("${LANG_").append(language.getName().toUpperCase()).append("}").toString();
                    
                    writeAttribute("LangString",new String[]{"UninstallLink",langdef,""});
                    
                }
            }
//            writeInstruction("");
        }
        writeSection("post",true,false,false);
        writeLine("WriteRegStr HKLM \"${REGKEY}\" Path $INSTDIR");
        if(!isMUI) {
            writeLine("WriteRegStr HKLM \"${REGKEY}\" \"Installer Language\" $LANGUAGE");
            writeLine("WriteRegStr HKLM \"${REGKEY}\" StartMenuGroup $StartMenuGroup");
        }
        if(mSettings.isCreateUninstaller()) {
            String uninstDir = "$INSTDIR\\"+mSettings.getUninstallFile();
            writeInstruction("WriteUninstaller",uninstDir);
            if(mSettings.isCreateStartMenuGroup() && mSettings.isCreateUninstallerStartMenuShortcut()) {
//                writeInstruction("");
            }
        }
        writeSectionEnd();
        mWriter.flush();
        mWriter.close();
    }
    
    private String flattenRGB(RGB rgb, String separator)
    {
        return new StringBuffer("").append(rgb.red).append(separator).append(rgb.green).append(
                                separator).append(rgb.blue).toString();
    }

    private String escapeQuotes(String text)
    {
        if(!Common.isEmpty(text)) {
            StringBuffer buf = new StringBuffer("");
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] == '"' || chars[i] == '\'') {
                    buf.append("$\\");
                }
                buf.append(chars[i]);
            }
            text = buf.toString();
        }
        return text;
    }
    
    private void writeIndent()
    {
        if(mIndent.length() > 0) {
            mWriter.print(mIndent);
        }
    }

    private void writeFunction(String name)
    {
        writeIndent();
        mWriter.print("Function");
        writeValue(name);
        indent();
    }

    private void writeFunctionEnd()
    {
        unindent();
        writeIndent();
        mWriter.println("FunctionEnd");
        incrementMonitor(1);
    }

    private void writeSection(String name, boolean hidden, boolean bold, boolean unselected)
    {
        writeIndent();
        mWriter.print("Section");
        if(!Common.isEmpty(name)) {
            if(hidden) {
                writeValue("-"+name);
            }
            else {
                if(unselected) {
                    mWriter.print(" /o");
                }
                if(bold) {
                    writeValue("!"+name);
                }
                else {
                    writeValue(name);
                }
            }
        }
        indent();
    }

    private void writeSectionEnd()
    {
        unindent();
        writeIndent();
        mWriter.println("SectionEnd");
        incrementMonitor(1);
    }

    private void writeSubSection(String caption, boolean expanded)
    {
        writeIndent();
        mWriter.print("SubSection");
        if(expanded) {
            mWriter.print(" /e");
        }
        writeValue(caption);
        indent();
    }

    private void writeSubSectionEnd()
    {
        unindent();
        writeIndent();
        mWriter.println("SubSectionEnd");
        incrementMonitor(1);
    }

    private void writeDefine(String name, String value, boolean emptyOK)
    {
        boolean isEmpty = (value == null || value.length() == 0);
        if(emptyOK || !isEmpty) {
            writeIndent();
            mWriter.print("!define ");
            write(name);
            writeValue(value);
            mWriter.println();
            incrementMonitor(1);
        }
    }
    
    private void writeAttribute(String name, Object value)
    {
        writeInstruction(name, value);
        incrementMonitor(1);
    }

    private void writeInstruction(String name, Object value)
    {
        writeIndent();
        write(name);
        if(value != null) {
            if(value instanceof String) {
                writeValue((String)value);
            }
            else if(value instanceof String[]) {
                String[] values = (String[])value;
                for (int i = 0; i < values.length; i++) {
                    writeValue(values[i]);
                }
            }
            else {
                writeValue(value.toString());
            }
        }
        mWriter.println();
    }

    private void writeInsertMacro(String name, String[] values)
    {
        writeIndent();
        mWriter.print("!insertmacro ");
        write(name);
        if(!Common.isEmptyArray(values)) {
            for (int i = 0; i < values.length; i++) {
                writeValue(values[i]);
            }
        }
        mWriter.println();
        incrementMonitor(1);
    }
    
    /**
     * @param value
     */
    private void writeValue(String value)
    {
        if(value != null) {
            boolean quoted = false;
            char[] chars= value.toCharArray();
            if(chars.length == 0) {
                quoted = true;
            }
            else {
                for (int i = 0; i < chars.length; i++) {
                    if(Character.isWhitespace(chars[i])) {
                        quoted = true;
                        break;
                    }
                }
            }
            mWriter.print((quoted?" \"":" "));
            write(value);
            if(quoted) {
                mWriter.print("\"");
            }
        }
    }
    
    private void writeLine(String line)
    {
        writeIndent();
        write(line);
        mWriter.println();
    }

    /**
     * @param text
     */
    private void write(String text)
    {
        mWriter.print(text);
    }

    private void writeLine(String line, int increment)
    {
        writeLine(line);
        if(increment > 0) {
            incrementMonitor(increment);
        }
    }

    private void writePage(String name, String[] params)
    {
        writeIndent();
        mWriter.print("Page ");
        write(name);
        if(!Common.isEmptyArray(params)) {
            for (int i = 0; i < params.length; i++) {
                if(params[i] == null) {
                    for (int j = i+1; j < params.length; j++) {
                        if(params[j] != null) {
                            params[i] = "";
                            break;
                        }
                    }
                }
                writeValue(params[i]);
            }
        }
        mWriter.println();
        incrementMonitor(1);
    }

    private void indent()
    {
        mIndent += INDENT_INCREMENT;
    }
    
    private void unindent()
    {
        if(mIndent.length() >= INDENT_INCREMENT_LENGTH) {
            mIndent = mIndent.substring(0,mIndent.length()-INDENT_INCREMENT_LENGTH);
        }
    }
}
