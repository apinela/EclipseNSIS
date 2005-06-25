/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.io.Serializable;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.INSISWizardConstants;
import net.sf.eclipsensis.wizard.NSISWizard;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NSISWizardSettings extends AbstractNodeConvertible implements INSISWizardConstants, Serializable, Cloneable
{
    public static final String NODE = "settings"; //$NON-NLS-1$
    public static final String CHILD_NODE = "attribute"; //$NON-NLS-1$
    
    private static final long serialVersionUID = -3872062583870145866L;

    private String mName = EclipseNSISPlugin.getResourceString("wizard.default.name",""); //$NON-NLS-1$ //$NON-NLS-2$
    private String mCompany = ""; //$NON-NLS-1$
    private String mVersion = ""; //$NON-NLS-1$
    private String mUrl = ""; //$NON-NLS-1$
    private String mOutFile = EclipseNSISPlugin.getResourceString("wizard.default.installer",""); //$NON-NLS-1$ //$NON-NLS-2$
    private int mCompressorType = MakeNSISRunner.COMPRESSOR_DEFAULT;
    private int mInstallerType = INSTALLER_TYPE_MUI;
    private String mIcon = ""; //$NON-NLS-1$
    private boolean mShowSplash = false;
    private String mSplashBMP = ""; //$NON-NLS-1$
    private String mSplashWAV = ""; //$NON-NLS-1$
    private int mSplashDelay = 1000;
    private int mFadeInDelay = 600;
    private int mFadeOutDelay = 400;
    private boolean mShowBackground = false;
    private RGB mBGTopColor = ColorManager.NAVY_BLUE;
    private RGB mBGBottomColor = ColorManager.BLACK;
    private RGB mBGTextColor = ColorManager.WHITE;
    private String mBackgroundBMP = ""; //$NON-NLS-1$
    private String mBackgroundWAV = ""; //$NON-NLS-1$
    private boolean mShowLicense = false;
    private String mLicenseData = ""; //$NON-NLS-1$
    private int mLicenseButtonType = LICENSE_BUTTON_CLASSIC;
    private boolean mEnableLanguageSupport = false;
    private ArrayList mLanguages = new ArrayList();
    private boolean mSelectLanguage = false;
    private String mInstallDir = new StringBuffer(NSISKeywords.getKeyword("$PROGRAMFILES")).append("\\").append(mName).toString(); //$NON-NLS-1$ //$NON-NLS-2$
    private boolean mChangeInstallDir = true;
    private boolean mCreateStartMenuGroup = false;
    private String mStartMenuGroup = mName;
    private boolean mChangeStartMenuGroup = false;
    private boolean mShowInstDetails = false;
    private String mRunProgramAfterInstall = ""; //$NON-NLS-1$
    private String mRunProgramAfterInstallParams = ""; //$NON-NLS-1$
    private String mOpenReadmeAfterInstall = ""; //$NON-NLS-1$
    private boolean mAutoCloseInstaller = false;
    private boolean mShowUninstDetails = false;
    private boolean mAutoCloseUninstaller = false;
    private boolean mCreateUninstallerStartMenuShortcut = true;
    private boolean mCreateUninstallerControlPanelEntry = true;
    private boolean mSilentUninstaller = false;
    private boolean mSelectComponents = false;
    
    private boolean mCreateUninstaller = true;
    private String mUninstallIcon = ""; //$NON-NLS-1$
    private String mUninstallFile = EclipseNSISPlugin.getResourceString("wizard.default.uninstaller",""); //$NON-NLS-1$ //$NON-NLS-2$
    private String mSavePath = ""; //$NON-NLS-1$
    private boolean mMakePathsRelative = true;
    private boolean mCompileScript = true;
    private boolean mTestScript = false;
    
    private INSISInstallElement mInstaller;

    private transient NSISWizard mWizard = null;
    
    public NSISWizardSettings()
    {
        this(false);
    }

    public NSISWizardSettings(boolean empty)
    {
        super();
        if(!empty) {
            mInstaller = new NSISInstaller();
            mInstaller.setSettings(this);
            NSISSection section = (NSISSection)NSISInstallElementFactory.create(NSISSection.TYPE);
            section.setName(EclipseNSISPlugin.getResourceString("main.section.name")); //$NON-NLS-1$
            section.setDescription(EclipseNSISPlugin.getResourceString("main.section.description")); //$NON-NLS-1$
            section.setHidden(true);
            mInstaller.addChild(section);
        }
        else {
            mInstaller = null;
        }
    }
    
    /**
     * @return Returns the wizard.
     */
    public NSISWizard getWizard()
    {
        return mWizard;
    }
    /**
     * @param wizard The wizard to set.
     */
    public void setWizard(NSISWizard wizard)
    {
        mWizard = wizard;
    }
    /**
     * @return Returns the autoCloseInstaller.
     */
    public boolean isAutoCloseInstaller()
    {
        return mAutoCloseInstaller;
    }

    /**
     * @param autoCloseInstaller The autoCloseInstaller to set.
     */
    public void setAutoCloseInstaller(boolean autoCloseInstaller)
    {
        mAutoCloseInstaller = autoCloseInstaller;
    }

    /**
     * @return Returns the bGBottomColor.
     */
    public RGB getBGBottomColor()
    {
        return mBGBottomColor;
    }

    /**
     * @param bottomColor The bGBottomColor to set.
     */
    public void setBGBottomColor(RGB bottomColor)
    {
        mBGBottomColor = bottomColor;
    }

    /**
     * @return Returns the bGGradient.
     */
    public boolean isShowBackground()
    {
        return mShowBackground;
    }
 
    /**
     * @param gradient The bGGradient to set.
     */
    public void setShowBackground(boolean gradient)
    {
        mShowBackground = gradient;
    }

    /**
     * @return Returns the bGImage.
     */
    public String getBackgroundBMP()
    {
        return mBackgroundBMP;
    }

    /**
     * @param image The bGImage to set.
     */
    public void setBackgroundBMP(String backgroundBMP)
    {
        mBackgroundBMP = backgroundBMP;
    }
    
    /**
     * @return Returns the backgroundWAV.
     */
    public String getBackgroundWAV()
    {
        return mBackgroundWAV;
    }

    /**
     * @param backgroundWAV The backgroundWAV to set.
     */
    public void setBackgroundWAV(String backgroundWAV)
    {
        mBackgroundWAV = backgroundWAV;
    }
    /**
     * @return Returns the bGTextColor.
     */
    public RGB getBGTextColor()
    {
        return mBGTextColor;
    }
    
    /**
     * @param textColor The bGTextColor to set.
     */
    public void setBGTextColor(RGB textColor)
    {
        mBGTextColor = textColor;
    }
    
    /**
     * @return Returns the bGTopColor.
     */
    public RGB getBGTopColor()
    {
        return mBGTopColor;
    }
    
    /**
     * @param topColor The bGTopColor to set.
     */
    public void setBGTopColor(RGB topColor)
    {
        mBGTopColor = topColor;
    }
    
    /**
     * @return Returns the changeInstallDir.
     */
    public boolean isChangeInstallDir()
    {
        return mChangeInstallDir;
    }
    
    /**
     * @param changeInstallDir The changeInstallDir to set.
     */
    public void setChangeInstallDir(boolean changeInstallDir)
    {
        mChangeInstallDir = changeInstallDir;
    }
    
    /**
     * @return Returns the changeProgramGroup.
     */
    public boolean isChangeStartMenuGroup()
    {
        return mChangeStartMenuGroup;
    }
    
    /**
     * @param changeStartMenuGroup The changeStartMenuGroup to set.
     */
    public void setChangeStartMenuGroup(boolean changeStartMenuGroup)
    {
        mChangeStartMenuGroup = changeStartMenuGroup;
    }
    
    /**
     * @return Returns the company.
     */
    public String getCompany()
    {
        return mCompany;
    }
    
    /**
     * @param company The company to set.
     */
    public void setCompany(String company)
    {
        mCompany = company;
    }
    
    /**
     * @return Returns the createStartMenuGroup.
     */
    public boolean isCreateStartMenuGroup()
    {
        return mCreateStartMenuGroup;
    }
    
    /**
     * @param createStartMenuGroup The createStartMenuGroup to set.
     */
    public void setCreateStartMenuGroup(boolean createStartMenuGroup)
    {
        mCreateStartMenuGroup = createStartMenuGroup;
    }
    
    /**
     * @return Returns the createUninstaller.
     */
    public boolean isCreateUninstaller()
    {
        return mCreateUninstaller;
    }
    
    /**
     * @param createUninstaller The createUninstaller to set.
     */
    public void setCreateUninstaller(boolean createUninstaller)
    {
        mCreateUninstaller = createUninstaller;
    }
    
    /**
     * @return Returns the fadeInTime.
     */
    public int getFadeInDelay()
    {
        return mFadeInDelay;
    }
    
    /**
     * @param fadeInDelay The fadeInDelay to set.
     */
    public void setFadeInDelay(int fadeInDelay)
    {
        mFadeInDelay = fadeInDelay;
    }
    
    /**
     * @return Returns the fadeOutTime.
     */
    public int getFadeOutDelay()
    {
        return mFadeOutDelay;
    }
    
    /**
     * @param fadeOutDelay The fadeOutDelay to set.
     */
    public void setFadeOutDelay(int fadeOutDelay)
    {
        mFadeOutDelay = fadeOutDelay;
    }
    
    /**
     * @return Returns the installDir.
     */
    public String getInstallDir()
    {
        return mInstallDir;
    }
    
    /**
     * @param installDir The installDir to set.
     */
    public void setInstallDir(String installDir)
    {
        mInstallDir = installDir;
    }
    
    /**
     * @return Returns the icon.
     */
    public String getIcon()
    {
        return mIcon;
    }
    
    /**
     * @param icon The icon to set.
     */
    public void setIcon(String icon)
    {
        mIcon = icon;
    }
    
    /**
     * @return Returns the installType.
     */
    public int getInstallerType()
    {
        return mInstallerType;
    }
    
    /**
     * @param installType The installType to set.
     */
    public void setInstallerType(int installerType)
    {
        mInstallerType = installerType;
    }
    
    /**
     * @return Returns the licenseButtonType.
     */
    public int getLicenseButtonType()
    {
        return mLicenseButtonType;
    }
    
    /**
     * @param licenseButtonType The licenseButtonType to set.
     */
    public void setLicenseButtonType(int licenseButtonType)
    {
        mLicenseButtonType = licenseButtonType;
    }
    
    /**
     * @return Returns the licenseData.
     */
    public String getLicenseData()
    {
        return mLicenseData;
    }
    
    /**
     * @param licenseData The licenseData to set.
     */
    public void setLicenseData(String licenseData)
    {
        mLicenseData = licenseData;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mName = name;
    }
    
    /**
     * @return Returns the outFile.
     */
    public String getOutFile()
    {
        return mOutFile;
    }
    
    /**
     * @param outFile The outFile to set.
     */
    public void setOutFile(String outFile)
    {
        mOutFile = outFile;
    }
    
    /**
     * @return Returns the startMenuGroup.
     */
    public String getStartMenuGroup()
    {
        return mStartMenuGroup;
    }
    
    /**
     * @param startMenuGroup The startMenuGroup to set.
     */
    public void setStartMenuGroup(String startMenuGroup)
    {
        mStartMenuGroup = startMenuGroup;
    }
    
    /**
     * @return Returns the runProgramAfterInstall.
     */
    public String getRunProgramAfterInstall()
    {
        return mRunProgramAfterInstall;
    }
    
    /**
     * @param runAfterInstall The runProgramAfterInstall to set.
     */
    public void setRunProgramAfterInstall(String runProgramAfterInstall)
    {
        mRunProgramAfterInstall = runProgramAfterInstall;
    }

    /**
     * @return Returns the showInstDetails.
     */
    public boolean isShowInstDetails()
    {
        return mShowInstDetails;
    }
    
    /**
     * @param showInstDetails The showInstDetails to set.
     */
    public void setShowInstDetails(boolean showInstDetails)
    {
        mShowInstDetails = showInstDetails;
    }
    
    /**
     * @return Returns the showLicense.
     */
    public boolean isShowLicense()
    {
        return mShowLicense;
    }
    
    /**
     * @param showLicense The showLicense to set.
     */
    public void setShowLicense(boolean showLicense)
    {
        mShowLicense = showLicense;
    }
    
    /**
     * @return Returns the splashBMP.
     */
    public String getSplashBMP()
    {
        return mSplashBMP;
    }
    
    /**
     * @param splashBMP The splashBMP to set.
     */
    public void setSplashBMP(String splashBMP)
    {
        mSplashBMP = splashBMP;
    }
    
    /**
     * @return Returns the splashTime.
     */
    public int getSplashDelay()
    {
        return mSplashDelay;
    }
    
    /**
     * @param splashDelay The splashDelay to set.
     */
    public void setSplashDelay(int splashDelay)
    {
        mSplashDelay = splashDelay;
    }
    
    /**
     * @return Returns the splashWAV.
     */
    public String getSplashWAV()
    {
        return mSplashWAV;
    }
    
    /**
     * @param splashWAV The splashWAV to set.
     */
    public void setSplashWAV(String splashWAV)
    {
        mSplashWAV = splashWAV;
    }
    
    /**
     * @return Returns the uninstallFile.
     */
    public String getUninstallFile()
    {
        return mUninstallFile;
    }
    
    /**
     * @param uninstallFile The uninstallFile to set.
     */
    public void setUninstallFile(String uninstallFile)
    {
        mUninstallFile = uninstallFile;
    }
    
    /**
     * @return Returns the uninstallIcon.
     */
    public String getUninstallIcon()
    {
        return mUninstallIcon;
    }
    
    /**
     * @param uninstallIcon The uninstallIcon to set.
     */
    public void setUninstallIcon(String uninstallIcon)
    {
        mUninstallIcon = uninstallIcon;
    }
    
    /**
     * @return Returns the url.
     */
    public String getUrl()
    {
        return mUrl;
    }
    
    /**
     * @param url The url to set.
     */
    public void setUrl(String url)
    {
        mUrl = url;
    }
    
    /**
     * @return Returns the version.
     */
    public String getVersion()
    {
        return mVersion;
    }
    
    /**
     * @param version The version to set.
     */
    public void setVersion(String version)
    {
        mVersion = version;
    }

    /**
     * @return Returns the createMultilingual.
     */
    public boolean isEnableLanguageSupport()
    {
        return mEnableLanguageSupport;
    }
    
    /**
     * @param createMultilingual The createMultilingual to set.
     */
    public void setEnableLanguageSupport(boolean enableLanguageSupport)
    {
        mEnableLanguageSupport = enableLanguageSupport;
    }
    
    /**
     * @return Returns the selectLanguage.
     */
    public boolean isSelectLanguage()
    {
        return mSelectLanguage;
    }
    
    /**
     * @param selectLanguage The selectLanguage to set.
     */
    public void setSelectLanguage(boolean selectLanguage)
    {
        mSelectLanguage = selectLanguage;
    }
    
    /**
     * @return Returns the languages.
     */
    public ArrayList getLanguages()
    {
        return mLanguages;
    }
    
    /**
     * @param languages The languages to set.
     */
    public void setLanguages(ArrayList languages)
    {
        if(mLanguages != languages) {
            mLanguages.clear();
            mLanguages.addAll(languages);
        }
    }
    
    /**
     * @return Returns the showSplash.
     */
    public boolean isShowSplash()
    {
        return mShowSplash;
    }
    
    /**
     * @param showSplash The showSplash to set.
     */
    public void setShowSplash(boolean showSplash)
    {
        mShowSplash = showSplash;
    }

    /**
     * @return Returns the compileScript.
     */
    public boolean isCompileScript()
    {
        return mCompileScript;
    }

    /**
     * @param compileScript The compileScript to set.
     */
    public void setCompileScript(boolean compileScript)
    {
        mCompileScript = compileScript;
    }

    /**
     * @return Returns the testScript.
     */
    public boolean isTestScript()
    {
        return mTestScript;
    }

    /**
     * @param testScript The testScript to set.
     */
    public void setTestScript(boolean testScript)
    {
        mTestScript = testScript;
    }

    /**
     * @return Returns the makePathsRelative.
     */
    public boolean isMakePathsRelative()
    {
        return mMakePathsRelative;
    }

    /**
     * @param makePathsRelative The makePathsRelative to set.
     */
    public void setMakePathsRelative(boolean makePathsRelative)
    {
        mMakePathsRelative = makePathsRelative;
    }
    
    /**
     * @return Returns the savePath.
     */
    public String getSavePath()
    {
        return mSavePath;
    }
    
    /**
     * @param savePath The savePath to set.
     */
    public void setSavePath(String savePath)
    {
        mSavePath = savePath;
    }
    /**
     * @return Returns the compressorType.
     */
    public int getCompressorType()
    {
        return mCompressorType;
    }

    /**
     * @param compressorType The compressorType to set.
     */
    public void setCompressorType(int compressorType)
    {
        mCompressorType = compressorType;
    }

    public INSISInstallElement getInstaller()
    {
        return mInstaller;
    }
    
    public void setInstaller(INSISInstallElement installer)
    {
        mInstaller = installer;
    }
    
    /**
     * @return Returns the autoCloseUninstaller.
     */
    public boolean isAutoCloseUninstaller()
    {
        return mAutoCloseUninstaller;
    }
    
    /**
     * @param autoCloseUninstaller The autoCloseUninstaller to set.
     */
    public void setAutoCloseUninstaller(boolean autoCloseUninstaller)
    {
        mAutoCloseUninstaller = autoCloseUninstaller;
    }
    
    /**
     * @return Returns the openReadmeAfterInstall.
     */
    public String getOpenReadmeAfterInstall()
    {
        return mOpenReadmeAfterInstall;
    }
    
    /**
     * @param openReadmeAfterInstall The openReadmeAfterInstall to set.
     */
    public void setOpenReadmeAfterInstall(String openReadmeAfterInstall)
    {
        mOpenReadmeAfterInstall = openReadmeAfterInstall;
    }
    
    /**
     * @return Returns the runProgramAfterInstallParams.
     */
    public String getRunProgramAfterInstallParams()
    {
        return mRunProgramAfterInstallParams;
    }
    
    /**
     * @param runProgramAfterInstallParams The runProgramAfterInstallParams to set.
     */
    public void setRunProgramAfterInstallParams(
            String runProgramAfterInstallParams)
    {
        mRunProgramAfterInstallParams = runProgramAfterInstallParams;
    }
    
    /**
     * @return Returns the showUninstDetails.
     */
    public boolean isShowUninstDetails()
    {
        return mShowUninstDetails;
    }
    
    /**
     * @param showUninstDetails The showUninstDetails to set.
     */
    public void setShowUninstDetails(boolean showUninstDetails)
    {
        mShowUninstDetails = showUninstDetails;
    }
    
    /**
     * @return Returns the createUninstallerControlPanelEntry.
     */
    public boolean isCreateUninstallerControlPanelEntry()
    {
        return mCreateUninstallerControlPanelEntry;
    }
    
    /**
     * @param createUninstallerControlPanelEntry The createUninstallerControlPanelEntry to set.
     */
    public void setCreateUninstallerControlPanelEntry(
            boolean createUninstallerControlPanelEntry)
    {
        mCreateUninstallerControlPanelEntry = createUninstallerControlPanelEntry;
    }
    
    /**
     * @return Returns the createUninstallerStartMenuShortcut.
     */
    public boolean isCreateUninstallerStartMenuShortcut()
    {
        return mCreateUninstallerStartMenuShortcut;
    }
    
    /**
     * @param createUninstallerStartMenuShortcut The createUninstallerStartMenuShortcut to set.
     */
    public void setCreateUninstallerStartMenuShortcut(
            boolean createUninstallerStartMenuShortcut)
    {
        mCreateUninstallerStartMenuShortcut = createUninstallerStartMenuShortcut;
    }
    
    /**
     * @return Returns the silentUninstaller.
     */
    public boolean isSilentUninstaller()
    {
        return mSilentUninstaller;
    }
    
    /**
     * @param silentUninstaller The silentUninstaller to set.
     */
    public void setSilentUninstaller(boolean silentUninstaller)
    {
        mSilentUninstaller = silentUninstaller;
    }
    
    /**
     * @return Returns the selectComponents.
     */
    public boolean isSelectComponents()
    {
        return mSelectComponents;
    }
    
    /**
     * @param selectComponents The selectComponents to set.
     */
    public void setSelectComponents(boolean selectComponents)
    {
        mSelectComponents = selectComponents;
    }


    protected Object getNodeValue(Node node, String name, Class clasz)
    {
        if(name.equals("installer")) { //$NON-NLS-1$
            NodeList nodeList = node.getChildNodes();
            int n = nodeList.getLength();
            for(int i=0; i<n; i++) {
                Node childNode = nodeList.item(i);
                if(childNode.getNodeName().equals(INSISInstallElement.NODE)) {
                    NSISInstaller installer = (NSISInstaller)NSISInstallElementFactory.createFromNode(childNode,NSISInstaller.TYPE);
                    if(installer != null) {
                        installer.setSettings(this);
                    }
                    return installer;
                }
            }
            return null;
        }
        else {
            return super.getNodeValue(node, name, clasz);
        }
    }
    
    protected String getNodeName()
    {
        return NODE; //$NON-NLS-1$
    }

    protected String getChildNodeName()
    {
        return CHILD_NODE;
    }
    
    protected String convertToString(String name, Object obj)
    {
        if(obj instanceof RGB) {
            return StringConverter.asString((RGB)obj);
        }
        else if(obj instanceof NSISLanguage) {
            return ((NSISLanguage)obj).getName();
        }
        else if(obj instanceof Collection && name.equals("languages")) { //$NON-NLS-1$
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            if(!Common.isEmptyCollection((Collection)obj)) {
                Iterator iter = ((Collection)obj).iterator();
                buf.append(convertToString(name, iter.next()));
                while(iter.hasNext()) {
                    buf.append(",").append(convertToString(name, iter.next())); //$NON-NLS-1$
                }
            }
            return buf.toString();
        }
        return super.convertToString(name, obj);
    }
    protected Object convertFromString(String name, String string, Class clasz)
    {
        if(clasz.equals(RGB.class)) {
            return StringConverter.asRGB(string);
        }
        else if(name.equals("languages")) { //$NON-NLS-1$
            String[] langNames = Common.tokenize(string,',');
            ArrayList languages = new ArrayList();
            NSISLanguageManager languageManager = NSISLanguageManager.getInstance();
            for (int i = 0; i < langNames.length; i++) {
                NSISLanguage language = languageManager.getLanguage(langNames[i]);
                if(language != null) {
                    languages.add(language);
                }
            }
            return languages;
        }
        else {
            return super.convertFromString(name, string, clasz);
        }
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        NSISWizardSettings settings = (NSISWizardSettings)super.clone();
        settings.mBGTopColor = cloneRGB(mBGTopColor);
        settings.mBGBottomColor = cloneRGB(mBGBottomColor);
        settings.mBGTextColor = cloneRGB(mBGTextColor);
        settings.mLanguages = (mLanguages==null?null:(ArrayList)mLanguages.clone());
        settings.mWizard = null;
        settings.mInstaller = (INSISInstallElement)mInstaller.clone();
        settings.mInstaller.setSettings(settings);
        return settings;
    }

    /**
     * @return
     */
    private RGB cloneRGB(RGB rgb)
    {
        return (rgb==null?null:new RGB(rgb.red,rgb.green,rgb.blue));
    }
}
