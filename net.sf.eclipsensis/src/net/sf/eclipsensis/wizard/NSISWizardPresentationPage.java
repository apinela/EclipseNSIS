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

import java.io.File;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;
import net.sf.eclipsensis.wizard.util.MasterSlaveEnabler;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Control;

public class NSISWizardPresentationPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardPresentation"; //$NON-NLS-1$
    
    private static final long DEFAULT_RESOLUTION = 32; //32 FPS
    private static final double cOSVersion;

    private static final int LICDATA_CHECK=1;
    private static final int SPLIMG_CHECK=2;
    private static final int SPLWAV_CHECK=4;
    private static final int SPLDLY_CHECK=8;
    private static final int BGIMG_CHECK=16;
    private static final int BGWAV_CHECK=32;
    private static final int ALL_CHECK=LICDATA_CHECK|SPLIMG_CHECK|SPLWAV_CHECK|SPLDLY_CHECK|BGIMG_CHECK|BGWAV_CHECK;

    private static final String[] cLicFileErrors = {"empty.license.file.error"}; //$NON-NLS-1$
    private static final String[] cSplashImageErrors = {"empty.splash.image.error"}; //$NON-NLS-1$
    private static final String[] cSplashDelayErrors = {"zero.splash.delay.error"}; //$NON-NLS-1$
    
    private Button mSplashPreviewButton = null;
    private Button mBGPreviewButton = null;
    private FontData mBGPreviewFontData;
    private FontData mBGPreviewEscapeFontData;
    private Point mBGPreviewTextLocation;
    private int mBGPreviewGradientHeight;
    
    static {
        double version;
        try {
            version = Double.parseDouble(System.getProperty("os.version")); //$NON-NLS-1$
        }
        catch(Exception ex) {
            version = 0;
        }
        cOSVersion = version;
    }
    
    /**
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardPresentationPage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.presentation.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.presentation.description")); //$NON-NLS-1$
        mBGPreviewFontData = new FontData(EclipseNSISPlugin.getResourceString("background.preview.font","1|Times New Roman|26|3|WINDOWS|1|-53|0|0|0|700|1|0|0|1|0|0|0|0|Times New Roman")); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewEscapeFontData = new FontData(EclipseNSISPlugin.getResourceString("background.preview.escape.font","1|Times New Roman|12|1|WINDOWS|1|0|0|0|0|700|0|0|0|1|0|0|0|0|Times New Roman")); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewTextLocation = new Point(Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.text.x","16")), //$NON-NLS-1$ //$NON-NLS-2$
                                           Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.text.y","8"))); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewGradientHeight = Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.gradient.height","4")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();

        createLicenseGroup(composite, bundle);
        createSplashGroup(composite, bundle);
        createBackgroundGroup(composite, bundle);

        setPageComplete(validatePage(ALL_CHECK));
    }
    
    /**
     * @param composite
     * @param bundle
     */
    private void createBackgroundGroup(Composite parent, ResourceBundle bundle)
    {
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                if(control == mBGPreviewButton) {
                    return (mSettings.isShowBackground() && 
                            validateEmptyOrValidFile(mSettings.getBackgroundBMP(),null) &&
                            validateEmptyOrValidFile(mSettings.getBackgroundWAV(),null));
                }
                else {
                    return true;
                }
            }
        };
        
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "background.group.label", null, false);  //$NON-NLS-1$

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"show.background.label",mSettings.isShowBackground(), //$NON-NLS-1$
                                         true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mSettings.setShowBackground(selection);
                setPageComplete(validateField(BGIMG_CHECK | BGWAV_CHECK));
                if(mBGPreviewButton != null) {
                    mBGPreviewButton.setEnabled(mse.canEnable(mBGPreviewButton));
                }
            }
        });

        MasterSlaveController m = new MasterSlaveController(b);
        
        Composite composite = new Composite(group,SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        Text t = NSISWizardDialogUtil.createFileBrowser(composite, mSettings.getBackgroundBMP(), false, 
                              Common.loadArrayProperty(bundle,"background.image.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"background.image.filters"), "background.image.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true,m, false);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mSettings.setBackgroundBMP(text);
                setPageComplete(validateField(BGIMG_CHECK));
                if(mBGPreviewButton != null) {
                    mBGPreviewButton.setEnabled(mse.canEnable(mBGPreviewButton));
                }
            }
        });
        
        t = NSISWizardDialogUtil.createFileBrowser(composite, mSettings.getBackgroundBMP(), false, 
                              Common.loadArrayProperty(bundle,"background.sound.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"background.sound.filters"), "background.sound.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true, m, false);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mSettings.setBackgroundWAV(text);
                setPageComplete(validateField(BGWAV_CHECK));
                if(mBGPreviewButton != null) {
                    mBGPreviewButton.setEnabled(mse.canEnable(mBGPreviewButton));
                }
            }
        });
        
        composite = new Composite(group,SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Group group2 = NSISWizardDialogUtil.createGroup(composite, 3, "background.colors.label", m, false); //$NON-NLS-1$
        ((GridLayout)group2.getLayout()).makeColumnsEqualWidth = true;
        ((GridData)group2.getLayoutData()).horizontalSpan = 1;

        String[] labels = {"background.topcolor.label","background.bottomcolor.label","background.textcolor.label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        RGB[] values = {mSettings.getBGTopColor(),mSettings.getBGBottomColor(),mSettings.getBGTextColor()};
        final ColorEditor[] ce = new ColorEditor[labels.length];
        
        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Button b = (Button)e.widget;
                int index = ((Integer)b.getData()).intValue();
                RGB rgb = ce[index].getRGB();
                switch(index) {
                    case 0:
                        mSettings.setBGTopColor(rgb);
                        break;
                    case 1:
                        mSettings.setBGBottomColor(rgb);
                        break;
                    case 2:
                        mSettings.setBGTextColor(rgb);
                        break;
                }
            }
        };

        for (int i = 0; i < labels.length; i++) {
            Composite composite3 = new Composite(group2, SWT.NONE);
            composite3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite3.setLayout(layout);
            ce[i] = NSISWizardDialogUtil.createColorEditor(composite3, values[i], labels[i], true, null, false);
            Button b2 = ce[i].getButton();
            b2.setData(new Integer(i));
            b2.addSelectionListener(sa);
        }

        mBGPreviewButton = new Button(composite, SWT.PUSH | SWT.CENTER);
        m.addSlave(mBGPreviewButton, mse);
        mBGPreviewButton.setText(bundle.getString("preview.label")); //$NON-NLS-1$
        mBGPreviewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mBGPreviewButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                previewBackground();
            }
        });
       m.updateSlaves();
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createLicenseGroup(Composite parent, ResourceBundle bundle)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 3, "license.group.label", null, false); //$NON-NLS-1$

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"show.license.label",mSettings.isShowLicense(),  //$NON-NLS-1$
                                        true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mSettings.setShowLicense(selection);
                setPageComplete(validateField(LICDATA_CHECK));
            }
        });

        MasterSlaveController m = new MasterSlaveController(b);
        Text t = NSISWizardDialogUtil.createFileBrowser(group, mSettings.getLicenseData(), false, 
                                   Common.loadArrayProperty(bundle,"license.file.filternames"),  //$NON-NLS-1$
                                   Common.loadArrayProperty(bundle,"license.file.filters"), "license.file.label", //$NON-NLS-1$ //$NON-NLS-2$
                                   true, m, true);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mSettings.setLicenseData(text);
                setPageComplete(validateField(LICDATA_CHECK));
            }
        });

        final Combo c = NSISWizardDialogUtil.createCombo(group, NSISWizardDisplayValues.LICENSE_BUTTON_NAMES, 
                                    mSettings.getLicenseButtonType(),true,"license.button.label", //$NON-NLS-1$
                                    (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT && mSettings.isShowLicense()), m, false);
        final Label l = (Label)c.getData(NSISWizardDialogUtil.LABEL);
        
        c.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mSettings.setLicenseButtonType(((Combo)e.widget).getSelectionIndex());
            }
        });
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                if(c == control || (l != null && l == control)) {
                    return (mSettings.getInstallerType() != INSTALLER_TYPE_SILENT && mSettings.isShowLicense());
                }
                else {
                    return true;
                }
            }
        };
        m.setEnabler(c,mse);
        if(l != null) {
            m.setEnabler(l,mse);
        }
        
        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToEnter(IWizardPage page, boolean forward)
            {
                c.setEnabled(mse.canEnable(c));
                if(l != null) {
                    l.setEnabled(mse.canEnable(l));
                }
            }
        });
        m.updateSlaves();
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createSplashGroup(Composite parent, ResourceBundle bundle)
    {
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                if(control == mSplashPreviewButton) {
                    return (mSettings.isShowSplash() && 
                            Common.isValidFile(mSettings.getSplashBMP()) &&
                            validateEmptyOrValidFile(mSettings.getSplashWAV(),null) && 
                            mSettings.getSplashDelay() > 0);
                }
                else {
                    return true;
                }
            }
        };
        
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "splash.group.label", null, false); //$NON-NLS-1$
    
        Button b = NSISWizardDialogUtil.createCheckBox(group,"show.splash.label",mSettings.isShowSplash(), //$NON-NLS-1$
                                         true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = ((Button)e.widget).getSelection();
                mSettings.setShowSplash(selection);
                setPageComplete(validateField(SPLIMG_CHECK | SPLWAV_CHECK | SPLDLY_CHECK));
                if(mSplashPreviewButton != null) {
                    mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                }
            }
        });
    
        MasterSlaveController m = new MasterSlaveController(b);
        
        Composite composite = new Composite(group,SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        Text t = NSISWizardDialogUtil.createFileBrowser(composite, mSettings.getSplashBMP(), false, 
                              Common.loadArrayProperty(bundle,"splash.image.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"splash.image.filters"), "splash.image.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true,m, true);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mSettings.setSplashBMP(text);
                setPageComplete(validateField(SPLIMG_CHECK));
                if(mSplashPreviewButton != null) {
                    mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                }
            }
        });
        
        t = NSISWizardDialogUtil.createFileBrowser(composite, mSettings.getSplashWAV(), false, 
                              Common.loadArrayProperty(bundle,"splash.sound.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"splash.sound.filters"), "splash.sound.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true, m, false);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mSettings.setSplashWAV(text);
                setPageComplete(validateField(SPLWAV_CHECK));
                if(mSplashPreviewButton != null) {
                    mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                }
            }
        });
        
        composite = new Composite(group,SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Group group2 = NSISWizardDialogUtil.createGroup(composite, 3, "splash.delay.label", m, false); //$NON-NLS-1$
        ((GridLayout)group2.getLayout()).makeColumnsEqualWidth = true;
        ((GridData)group2.getLayoutData()).horizontalSpan = 1;
        String[] labels = {"splash.display.label","splash.fadein.label","splash.fadeout.label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        int[] values = {mSettings.getSplashDelay(),mSettings.getFadeInDelay(),mSettings.getFadeOutDelay()};
        boolean[] required = {true, false, false};
        
        ModifyListener ml = new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                Text text = (Text)e.widget;
                String str = text.getText();
                int index = ((Integer)text.getData()).intValue();
                int value = (Common.isEmpty(str)?0:Integer.parseInt(str));
                switch(index) {
                    case 0:
                        mSettings.setSplashDelay(value);
                        setPageComplete(validateField(SPLDLY_CHECK));
                        if(mSplashPreviewButton != null) {
                            mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                        }
                        break;
                    case 1:
                        mSettings.setFadeInDelay(value);
                        break;
                    case 2:
                        mSettings.setFadeOutDelay(value);
                        break;
                }
            }
        };

        for (int i = 0; i < labels.length; i++) {
            Composite composite2 = new Composite(group2, SWT.NONE);
            composite2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite2.setLayout(layout);
            t = NSISWizardDialogUtil.createText(composite2, makeStringFromInt(values[i]), labels[i], true, null, required[i]);
            t.setData(new Integer(i));
            t.addVerifyListener(mNumberVerifyListener);
            t.addModifyListener(ml);
        }
        mSplashPreviewButton = new Button(composite, SWT.PUSH | SWT.CENTER);
        m.addSlave(mSplashPreviewButton, mse);
        mSplashPreviewButton.setText(bundle.getString("preview.label")); //$NON-NLS-1$
        mSplashPreviewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mSplashPreviewButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                previewSplash();
            }
        });
        m.updateSlaves();
    }

    private String makeStringFromInt(int value)
    {
        if(value > 0) {
            return Integer.toString(value);
        }
        return ""; //$NON-NLS-1$
    }
    
    private void previewSplash()
    {
        if(Common.isValidFile(mSettings.getSplashBMP())) {
            SplashPreviewTask task = new SplashPreviewTask();
            new Timer().scheduleAtFixedRate(task,0,task.getResolution()); 
        }
    }
    
    private void previewBackground()
    {
        final Shell shell = new Shell((Display)null, SWT.APPLICATION_MODAL | SWT.NO_TRIM);
        shell.setText(EclipseNSISPlugin.getResourceString("background.preview.title")); //$NON-NLS-1$
        final String previewText = new StringBuffer(mSettings.getName()).append(" ").append(mSettings.getVersion()).toString().trim();  //$NON-NLS-1$
        final Display display = shell.getDisplay();
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight=0;
        fillLayout.marginWidth=0;
        shell.setLayout(fillLayout);
        final Rectangle rect = display.getBounds();
        shell.setBounds(rect.x,rect.y,rect.width,rect.height);
        
        final Font previewFont = new Font(display,mBGPreviewFontData);
        final Font messageFont = new Font(display,mBGPreviewEscapeFontData);

        final Clip clip = loadAudioClip(mSettings.getBackgroundWAV());
        if(clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        
        shell.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e) {
                if(e.character == SWT.ESC) {
                    shell.close();
                    shell.dispose();
                    previewFont.dispose();
                    messageFont.dispose();
                    if(clip != null) {
                        clip.stop();
                    }
                }
            }
        });
        Canvas canvas = new Canvas(shell, SWT.NO_BACKGROUND);
        final GC gc = new GC(canvas);
        shell.open();
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e)
            {
                RGB topRGB = mSettings.getBGTopColor();
                RGB botRGB = mSettings.getBGBottomColor();
                long r = topRGB.red << 10;
                long g = topRGB.green << 10;
                long b = topRGB.blue << 10;
                long dr = (((botRGB.red << 10) - r)*4) / rect.height;
                long dg = (((botRGB.green << 10) - g)*4) / rect.height;
                long db = (((botRGB.blue << 10) - b) * 4) / rect.height;
                int ry = rect.y;
                while(ry < (rect.y+rect.height)) {
                    Color color = new Color(display,(int)(r >> 10), (int)(g >> 10), (int)(b >> 10));
                    gc.setBackground(color);
                    gc.fillRectangle(rect.x,ry,rect.width,mBGPreviewGradientHeight);
                    color.dispose();
                    ry += mBGPreviewGradientHeight;
                    r += dr;
                    g += dg;
                    b += db;
                }
                
                if(Common.isValidFile(mSettings.getBackgroundBMP())) {
                    ImageData imageData = new ImageData(mSettings.getBackgroundBMP());
                    Image image = new Image(display, imageData);
                    int x = rect.x + (rect.width - imageData.width)/2;
                    int y = rect.y + (rect.height - imageData.height)/2;
                    gc.drawImage(image,x,y);
                    image.dispose();
                }

                gc.setForeground(ColorManager.getColor(mSettings.getBGTextColor()));
                gc.setFont(previewFont);
                gc.drawString(previewText, mBGPreviewTextLocation.x, mBGPreviewTextLocation.y, true);
                
                RGB rgb = new RGB(255 & ~botRGB.red, 255 & ~botRGB.green, 255 & ~botRGB.blue);
                gc.setForeground(ColorManager.getColor(rgb));
                gc.setFont(messageFont);
                gc.drawString(EclipseNSISPlugin.getResourceString("background.preview.escape.message"),10,rect.y+rect.height-20,true); //$NON-NLS-1$
            }
        });
        canvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                gc.dispose();
            }
        });
    }

    private boolean validateField(int flag)
    {
        if(validatePage(flag)) {
            return validatePage(ALL_CHECK & ~flag);
        }
        else {
            return false;
        }
    }
    
    private boolean validateDelay(int delay, String[] messageResource)
    {
        if(delay <= 0) {
            setErrorMessage(getArrayStringResource(messageResource,0,"zero.number.error")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private boolean validatePage(int flag)
    {
        boolean b = (!mSettings.isShowLicense() || ((flag & LICDATA_CHECK) == 0 || validateFile(mSettings.getLicenseData(),cLicFileErrors))) &&
                    (!mSettings.isShowSplash() || (((flag & SPLIMG_CHECK) == 0 || validateFile(mSettings.getSplashBMP(), cSplashImageErrors)) &&
                                                ((flag & SPLWAV_CHECK) == 0 || validateEmptyOrValidFile(mSettings.getSplashWAV(),null)) &&
                                                ((flag & SPLDLY_CHECK) == 0 || validateDelay(mSettings.getSplashDelay(),cSplashDelayErrors)))) &&
                    (!mSettings.isShowBackground() || (((flag & BGIMG_CHECK) == 0 || validateEmptyOrValidFile(mSettings.getBackgroundBMP(),null)) && 
                                                   ((flag & BGWAV_CHECK) == 0 || validateEmptyOrValidFile(mSettings.getBackgroundWAV(),null))));
        setPageComplete(b);
        if(b) {
            setErrorMessage(null);
        }
        return b;
    }

    /**
     * 
     */
    private Clip loadAudioClip(String fileName)
    {
        Clip clip = null;
        if(Common.isValidFile(fileName)) {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
                AudioFormat format = ais.getFormat();
                if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                    format = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(),
                            format.getSampleSizeInBits()*2,
                            format.getChannels(),
                            format.getFrameSize()*2,
                            format.getFrameRate(),
                            true);        // big endian
                    ais = AudioSystem.getAudioInputStream(format, ais);
                }
                
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                if(AudioSystem.isLineSupported(info)) {
                    clip = (Clip)AudioSystem.getLine(info);
                    clip.open(ais);
                    ais.close();
                }
            }
            catch (Exception e1) {
                clip = null;
                e1.printStackTrace();
            }
        }
        return clip;
    }

    private class SplashPreviewTask extends TimerTask
    {
        public static final int STATE_FADE_IN = 0;
        public static final int STATE_DISPLAY = 1;
        public static final int STATE_FADE_OUT = 2;
        
        private int mState = STATE_FADE_IN;
        private int mTimeLeft = 0;
        private Image mImage = null;
        private int mFadeInDelay = 0;
        private int mDisplayDelay = 0;
        private int mFadeOutDelay = 0;
        private Display mDisplay;
        private Shell mShell = null;
        private GC mGC = null;
        private int mAlpha;
        private long mResolution;
        private Clip mClip = null;

        public SplashPreviewTask()
        {
            init();
        }

        public long getResolution()
        {
            return mResolution;
        }

        public void init()
        {
            if(mShell != null) {
                mShell.close();
                mShell.dispose();
            }
            mShell = new Shell((Display)null, SWT.APPLICATION_MODAL | SWT.NO_TRIM  | SWT.NO_BACKGROUND);
            mShell.setText(EclipseNSISPlugin.getResourceString("splash.preview.title")); //$NON-NLS-1$
            WinAPI.SetWindowLong(mShell.handle, WinAPI.GWL_EXSTYLE,
                                 WinAPI.GetWindowLong(mShell.handle, WinAPI.GWL_EXSTYLE) | WinAPI.WS_EX_LAYERED);
            
            mDisplay = mShell.getDisplay();
            FillLayout fillLayout = new FillLayout();
            fillLayout.marginHeight=0;
            fillLayout.marginWidth=0;
            mShell.setLayout(fillLayout);

            ImageData imageData = new ImageData(mSettings.getSplashBMP());
            mShell.setSize(imageData.width, imageData.height);
            Rectangle rect = mDisplay.getClientArea();
            int x = (rect.width-imageData.width)/2;
            int y = (rect.height-imageData.height)/2;
            mShell.setBounds(x,y,imageData.width,imageData.height);

            if(cOSVersion >= 5.0) {
                mResolution = DEFAULT_RESOLUTION;
                mFadeInDelay = mSettings.getFadeInDelay() >> 5;
                mDisplayDelay = mSettings.getSplashDelay() >> 5;
                mFadeOutDelay = mSettings.getFadeOutDelay() >> 5;
            }
            else {
                mResolution = mSettings.getFadeInDelay() + mSettings.getSplashDelay() + mSettings.getFadeOutDelay();
                mFadeInDelay = 0;
                mDisplayDelay = 1;
                mFadeOutDelay = 0;
            }
            
            mState = STATE_FADE_IN;
            mTimeLeft = mFadeInDelay;
            
            Canvas canvas = new Canvas(mShell, SWT.NO_BACKGROUND);
            mGC = new GC(canvas);
            canvas.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    mGC.dispose();
                }
            });

            mImage = new Image(mDisplay, imageData);
            if(cOSVersion >= 5.0) {
                WinAPI.SetLayeredWindowAttributes(mShell.handle, 0, 0, 0, (mFadeInDelay > 0?0:255), WinAPI.LWA_ALPHA);
    
            }
            else {
                mGC.drawImage(mImage,0,0);
            }
            mAlpha = -1;
            mShell.open();
            mClip = loadAudioClip(mSettings.getSplashWAV());
            if(mClip != null) {
                mClip.start();
            }
        }

        public void run(){
            final int newAlpha;
            switch(mState) {
                case STATE_FADE_IN:
                    if(mTimeLeft == 0) {
                       mTimeLeft = mDisplayDelay;
                       mState = STATE_DISPLAY;
                    }
                    else {
                        newAlpha = ((mFadeInDelay - mTimeLeft)*255)/mFadeInDelay;
                        break;
                    }
                case STATE_DISPLAY:
                    if(mTimeLeft == 0) {
                        mTimeLeft = mFadeOutDelay;
                        mState = STATE_FADE_OUT;
                    }
                    else {
                        newAlpha = 255;
                        break;
                    }
                case STATE_FADE_OUT:
                    if(mTimeLeft == 0) {
                      mDisplay.asyncExec(new Runnable(){
                          public void run(){
                              if(mClip != null && mClip.isRunning()) {
                                  mClip.stop();
                                  mClip = null;
                              }
                              if(mImage != null) {
                                  mImage.dispose();
                              }
                              mShell.close();
                              mShell.dispose();
                          }
                      });
                      cancel();
                      return;
                    }
                    else {
                        newAlpha = (mTimeLeft*255)/mFadeOutDelay;
                        break;
                    }
               default:
                   newAlpha = 255;
            }

            if(cOSVersion > 5.0) {
                mDisplay.asyncExec(new Runnable(){
                    public void run(){
                        if(mAlpha != newAlpha) {
                            mAlpha = newAlpha;
                            WinAPI.SetLayeredWindowAttributes(mShell.handle, 0, 0, 0, mAlpha, WinAPI.LWA_ALPHA);
                            mGC.drawImage(mImage,0,0);
                        }
                    }
                });
            }
            mTimeLeft--;
        }
    }
}
