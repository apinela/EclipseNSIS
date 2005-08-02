/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.sound.sampled.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
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
     * @param pageName
     * @param title
     */
    public NSISWizardPresentationPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.presentation.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.presentation.description")); //$NON-NLS-1$
        mBGPreviewFontData = new FontData(EclipseNSISPlugin.getResourceString("background.preview.font","1|Times New Roman|24|3|WINDOWS|1|-53|0|0|0|700|1|0|0|1|0|0|0|0|Times New Roman")); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewEscapeFontData = new FontData(EclipseNSISPlugin.getResourceString("background.preview.escape.font","1|Times New Roman|12|1|WINDOWS|1|0|0|0|0|700|0|0|0|1|0|0|0|0|Times New Roman")); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewTextLocation = new Point(Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.text.x","16")), //$NON-NLS-1$ //$NON-NLS-2$
                                           Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.text.y","8"))); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewGradientHeight = Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.gradient.height","4")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizpresentation_context"; //$NON-NLS-1$
    }
    
    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();

        createLicenseGroup(composite, bundle);
        createSplashGroup(composite, bundle);
        createBackgroundGroup(composite, bundle);

        setPageComplete(validatePage(ALL_CHECK));
        
        return composite;
    }
    
    /**
     * @param composite
     * @param bundle
     */
    private void createBackgroundGroup(Composite parent, ResourceBundle bundle)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag) { }
            
            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(control == mBGPreviewButton) {
                    return (settings.isShowBackground() && 
                            validateEmptyOrValidFile(Common.decodePath(settings.getBackgroundBMP()),null) &&
                            validateEmptyOrValidFile(Common.decodePath(settings.getBackgroundWAV()),null));
                }
                else {
                    return true;
                }
            }
        };
        
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "background.group.label", null, false);  //$NON-NLS-1$

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"show.background.label",settings.isShowBackground(), //$NON-NLS-1$
                                         true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mWizard.getSettings().setShowBackground(selection);
                setPageComplete(validateField(BGIMG_CHECK | BGWAV_CHECK));
                if(mBGPreviewButton != null) {
                    mBGPreviewButton.setEnabled(mse.canEnable(mBGPreviewButton));
                }
            }
        });

        final MasterSlaveController m = new MasterSlaveController(b);
        
        Composite composite = new Composite(group,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Text t = NSISWizardDialogUtil.createFileBrowser(composite, settings.getBackgroundBMP(), false, 
                              Common.loadArrayProperty(bundle,"background.image.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"background.image.filters"), "background.image.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true,m, false);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setBackgroundBMP(text);
                setPageComplete(validateField(BGIMG_CHECK));
                if(mBGPreviewButton != null) {
                    mBGPreviewButton.setEnabled(mse.canEnable(mBGPreviewButton));
                }
            }
        });
        
        final Text t2 = NSISWizardDialogUtil.createFileBrowser(composite, settings.getBackgroundWAV(), false, 
                              Common.loadArrayProperty(bundle,"background.sound.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"background.sound.filters"), "background.sound.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true, m, false);
        t2.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setBackgroundWAV(text);
                setPageComplete(validateField(BGWAV_CHECK));
                if(mBGPreviewButton != null) {
                    mBGPreviewButton.setEnabled(mse.canEnable(mBGPreviewButton));
                }
            }
        });
        
        composite = new Composite(group,SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Group group2 = NSISWizardDialogUtil.createGroup(composite, 3, "background.colors.label", m, false); //$NON-NLS-1$
        ((GridLayout)group2.getLayout()).makeColumnsEqualWidth = true;
        ((GridData)group2.getLayoutData()).horizontalSpan = 1;

        String[] labels = {"background.topcolor.label","background.bottomcolor.label","background.textcolor.label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        RGB[] values = {settings.getBGTopColor(),settings.getBGBottomColor(),settings.getBGTextColor()};
        final ColorEditor[] ce = new ColorEditor[labels.length];
        
        SelectionAdapter sa = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                NSISWizardSettings settings = mWizard.getSettings();

                Button b = (Button)e.widget;
                int index = ((Integer)b.getData()).intValue();
                RGB rgb = ce[index].getRGB();
                switch(index) {
                    case 0:
                        settings.setBGTopColor(rgb);
                        break;
                    case 1:
                        settings.setBGBottomColor(rgb);
                        break;
                    case 2:
                        settings.setBGTextColor(rgb);
                        break;
                }
            }
        };

        for (int i = 0; i < labels.length; i++) {
            Composite composite3 = new Composite(group2, SWT.NONE);
            composite3.setLayoutData(new GridData());
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
        mBGPreviewButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        mBGPreviewButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                previewBackground();
            }
        });
        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                b.setSelection(settings.isShowBackground());
                t.setText(settings.getBackgroundBMP());
                t2.setText(settings.getBackgroundWAV());
                ce[0].setRGB(settings.getBGTopColor());
                ce[1].setRGB(settings.getBGBottomColor());
                ce[2].setRGB(settings.getBGTextColor());
                m.updateSlaves();
            }});
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createLicenseGroup(Composite parent, ResourceBundle bundle)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group group = NSISWizardDialogUtil.createGroup(parent, 3, "license.group.label", null, false); //$NON-NLS-1$

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"show.license.label",settings.isShowLicense(),  //$NON-NLS-1$
                                        true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = b.getSelection();
                mWizard.getSettings().setShowLicense(selection);
                setPageComplete(validateField(LICDATA_CHECK));
            }
        });

        final MasterSlaveController m = new MasterSlaveController(b);
        final Text t = NSISWizardDialogUtil.createFileBrowser(group, settings.getLicenseData(), false, 
                                   Common.loadArrayProperty(bundle,"license.file.filternames"),  //$NON-NLS-1$
                                   Common.loadArrayProperty(bundle,"license.file.filters"), "license.file.label", //$NON-NLS-1$ //$NON-NLS-2$
                                   true, m, true);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setLicenseData(text);
                setPageComplete(validateField(LICDATA_CHECK));
            }
        });

        final Combo c = NSISWizardDialogUtil.createCombo(group, NSISWizardDisplayValues.LICENSE_BUTTON_TYPE_NAMES, 
                                    settings.getLicenseButtonType(),true,"license.button.label", //$NON-NLS-1$
                                    (settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isShowLicense()), m, false);
        final Label l = (Label)c.getData(NSISWizardDialogUtil.LABEL);
        
        c.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setLicenseButtonType(((Combo)e.widget).getSelectionIndex());
            }
        });
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag) { }
            
            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(c == control || (l != null && l == control)) {
                    return (settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isShowLicense());
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
            public void aboutToShow()
            {
                c.setEnabled(mse.canEnable(c));
                if(l != null) {
                    l.setEnabled(mse.canEnable(l));
                }
            }
        });
        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                b.setSelection(settings.isShowLicense());
                t.setText(settings.getLicenseData());
                int n = settings.getLicenseButtonType();
                if(n >= 0 && n < NSISWizardDisplayValues.LICENSE_BUTTON_TYPE_NAMES.length) {
                    c.setText(NSISWizardDisplayValues.LICENSE_BUTTON_TYPE_NAMES[n]);
                }
                else {
                    c.clearSelection();
                    c.setText(""); //$NON-NLS-1$
                }
                c.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isShowLicense());

                m.updateSlaves();
            }});
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createSplashGroup(Composite parent, ResourceBundle bundle)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag) { }
            
            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(control == mSplashPreviewButton) {
                    return (settings.isShowSplash() && 
                            Common.isValidFile(Common.decodePath(settings.getSplashBMP())) &&
                            validateEmptyOrValidFile(Common.decodePath(settings.getSplashWAV()),null) && 
                            settings.getSplashDelay() > 0);
                }
                else {
                    return true;
                }
            }
        };
        
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, "splash.group.label", null, false); //$NON-NLS-1$
    
        final Button b = NSISWizardDialogUtil.createCheckBox(group,"show.splash.label",settings.isShowSplash(), //$NON-NLS-1$
                                         true, null, false);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                boolean selection = ((Button)e.widget).getSelection();
                mWizard.getSettings().setShowSplash(selection);
                setPageComplete(validateField(SPLIMG_CHECK | SPLWAV_CHECK | SPLDLY_CHECK));
                if(mSplashPreviewButton != null) {
                    mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                }
            }
        });
    
        final MasterSlaveController m = new MasterSlaveController(b);
        
        Composite composite = new Composite(group,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Text t = NSISWizardDialogUtil.createFileBrowser(composite, settings.getSplashBMP(), false, 
                              Common.loadArrayProperty(bundle,"splash.image.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"splash.image.filters"), "splash.image.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true,m, true);
        t.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setSplashBMP(text);
                setPageComplete(validateField(SPLIMG_CHECK));
                if(mSplashPreviewButton != null) {
                    mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                }
            }
        });
        
        final Text t2 = NSISWizardDialogUtil.createFileBrowser(composite, settings.getSplashWAV(), false, 
                              Common.loadArrayProperty(bundle,"splash.sound.filternames"),  //$NON-NLS-1$
                              Common.loadArrayProperty(bundle,"splash.sound.filters"), "splash.sound.label", //$NON-NLS-1$ //$NON-NLS-2$
                              true, m, false);
        t2.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setSplashWAV(text);
                setPageComplete(validateField(SPLWAV_CHECK));
                if(mSplashPreviewButton != null) {
                    mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                }
            }
        });
        
        composite = new Composite(group,SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Group group2 = NSISWizardDialogUtil.createGroup(composite, 3, "splash.delay.label", m, false); //$NON-NLS-1$
        ((GridLayout)group2.getLayout()).makeColumnsEqualWidth = true;
        ((GridData)group2.getLayoutData()).horizontalSpan = 1;
        String[] labels = {"splash.display.label","splash.fadein.label","splash.fadeout.label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        int[] values = {settings.getSplashDelay(),settings.getFadeInDelay(),settings.getFadeOutDelay()};
        boolean[] required = {true, false, false};
        
        ModifyListener ml = new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                Text text = (Text)e.widget;
                String str = text.getText();
                int index = ((Integer)text.getData()).intValue();
                int value = (Common.isEmpty(str)?0:Integer.parseInt(str));
                switch(index) {
                    case 0:
                        settings.setSplashDelay(value);
                        setPageComplete(validateField(SPLDLY_CHECK));
                        if(mSplashPreviewButton != null) {
                            mSplashPreviewButton.setEnabled(mse.canEnable(mSplashPreviewButton));
                        }
                        break;
                    case 1:
                        settings.setFadeInDelay(value);
                        break;
                    case 2:
                        settings.setFadeOutDelay(value);
                        break;
                }
            }
        };

        final Text[] t3 = new Text[labels.length];
        for (int i = 0; i < labels.length; i++) {
            Composite composite2 = new Composite(group2, SWT.NONE);
            composite2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite2.setLayout(layout);
            t3[i] = NSISWizardDialogUtil.createText(composite2, makeStringFromInt(values[i]), labels[i], true, null, required[i]);
            t3[i].setData(new Integer(i));
            t3[i].addVerifyListener(mNumberVerifyListener);
            t3[i].addModifyListener(ml);
        }
        mSplashPreviewButton = new Button(composite, SWT.PUSH | SWT.CENTER);
        m.addSlave(mSplashPreviewButton, mse);
        mSplashPreviewButton.setText(bundle.getString("preview.label")); //$NON-NLS-1$
        mSplashPreviewButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        mSplashPreviewButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                previewSplash();
            }
        });
        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                b.setSelection(settings.isShowSplash());
                t.setText(settings.getSplashBMP());
                t2.setText(settings.getSplashWAV());
                t3[0].setText(makeStringFromInt(settings.getSplashDelay()));
                t3[1].setText(makeStringFromInt(settings.getSplashDelay()));
                t3[2].setText(makeStringFromInt(settings.getSplashDelay()));
                m.updateSlaves();
            }});
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
        if(Common.isValidFile(Common.decodePath(mWizard.getSettings().getSplashBMP()))) {
            SplashPreviewTask task = new SplashPreviewTask();
            new Timer().scheduleAtFixedRate(task,0,task.getResolution()); 
        }
    }
    
    private void previewBackground()
    {
        final NSISWizardSettings settings = mWizard.getSettings();

        final Shell shell = new Shell((Display)null, SWT.APPLICATION_MODAL | SWT.NO_TRIM);
        shell.setText(EclipseNSISPlugin.getResourceString("background.preview.title")); //$NON-NLS-1$
        final String previewText = EclipseNSISPlugin.getFormattedString("background.preview.text", new Object[]{settings.getName()});  //$NON-NLS-1$
        final Display display = shell.getDisplay();
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight=0;
        fillLayout.marginWidth=0;
        shell.setLayout(fillLayout);
        final Rectangle rect = display.getBounds();
        shell.setBounds(rect.x,rect.y,rect.width,rect.height);
        
        final Font previewFont = new Font(display,mBGPreviewFontData);
        final Font messageFont = new Font(display,mBGPreviewEscapeFontData);
        final Clip clip = loadAudioClip(Common.decodePath(settings.getBackgroundWAV()));
        
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
        shell.forceActive();
        if(clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e)
            {
                RGB topRGB = settings.getBGTopColor();
                RGB botRGB = settings.getBGBottomColor();
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
                
                String backgroundBMP = Common.decodePath(settings.getBackgroundBMP());
                if(Common.isValidFile(backgroundBMP)) {
                    ImageData imageData = new ImageData(backgroundBMP);
                    Image image = new Image(display, imageData);
                    int x = rect.x + (rect.width - imageData.width)/2;
                    int y = rect.y + (rect.height - imageData.height)/2;
                    gc.drawImage(image,x,y);
                    image.dispose();
                }

                gc.setForeground(ColorManager.getColor(settings.getBGTextColor()));
                gc.setFont(previewFont);
                gc.drawString(previewText, mBGPreviewTextLocation.x, mBGPreviewTextLocation.y, true);
                
                gc.setForeground(ColorManager.getNegativeColor(botRGB));
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

    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();
    
            boolean b = (!settings.isShowLicense() || ((flag & LICDATA_CHECK) == 0 || validateFile(Common.decodePath(settings.getLicenseData()),cLicFileErrors))) &&
                        (!settings.isShowSplash() || (((flag & SPLIMG_CHECK) == 0 || validateFile(Common.decodePath(settings.getSplashBMP()), cSplashImageErrors)) &&
                                                    ((flag & SPLWAV_CHECK) == 0 || validateEmptyOrValidFile(Common.decodePath(settings.getSplashWAV()),null)) &&
                                                    ((flag & SPLDLY_CHECK) == 0 || validateDelay(settings.getSplashDelay(),cSplashDelayErrors)))) &&
                        (!settings.isShowBackground() || (((flag & BGIMG_CHECK) == 0 || validateEmptyOrValidFile(Common.decodePath(settings.getBackgroundBMP()),null)) && 
                                                       ((flag & BGWAV_CHECK) == 0 || validateEmptyOrValidFile(Common.decodePath(settings.getBackgroundWAV()),null))));
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    /**
     * 
     */
    private Clip loadAudioClip(String fileName)
    {
        Clip clip = null;
        if(Common.isValidFile(fileName)) {
            AudioInputStream ais = null;
            try {
                ais = AudioSystem.getAudioInputStream(new File(fileName));
                AudioFormat format = ais.getFormat();
                if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED && format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
                    if(AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, format)) {
                        format = new AudioFormat(
                                AudioFormat.Encoding.PCM_SIGNED,
                                format.getSampleRate(),
                                format.getSampleSizeInBits(),
                                format.getChannels(),
                                format.getFrameSize(),
                                format.getFrameRate(),
                                true);        // big endian
                        ais = AudioSystem.getAudioInputStream(format, ais);
                    }
                }
                
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                if(AudioSystem.isLineSupported(info)) {
                    clip = (Clip)AudioSystem.getLine(info);
                    clip.open(ais);
                }
            }
            catch (Exception e1) {
                clip = null;
                e1.printStackTrace();
            }
            finally {
                if(ais != null) {
                    try {
                        ais.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    ais = null;
                }
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
        private int mAlpha;
        private long mResolution;
        private Clip mClip = null;
        private boolean mAdvSplash = true;

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
            NSISWizardSettings settings = mWizard.getSettings();

            if(mShell != null) {
                mShell.close();
                mShell.dispose();
            }

            if(cOSVersion >= 5.0) {
                mResolution = DEFAULT_RESOLUTION;
                mFadeInDelay = settings.getFadeInDelay() >> 5;
                mDisplayDelay = settings.getSplashDelay() >> 5;
                mFadeOutDelay = settings.getFadeOutDelay() >> 5;
            }
            else {
                mResolution = settings.getFadeInDelay() + settings.getSplashDelay() + settings.getFadeOutDelay();
                mFadeInDelay = 0;
                mDisplayDelay = 1;
                mFadeOutDelay = 0;
            }

            mAdvSplash = cOSVersion >= 5.0 && (mFadeInDelay > 0 || mFadeOutDelay > 0);
            mShell = new Shell(getShell().getDisplay(), SWT.APPLICATION_MODAL | SWT.NO_TRIM | SWT.NO_BACKGROUND);
            mShell.setText(EclipseNSISPlugin.getResourceString("splash.preview.title")); //$NON-NLS-1$
            if(mAdvSplash) {
                WinAPI.SetWindowLong(mShell.handle, WinAPI.GWL_EXSTYLE,
                                     WinAPI.GetWindowLong(mShell.handle, WinAPI.GWL_EXSTYLE) | WinAPI.WS_EX_LAYERED);
            }
            
            mDisplay = mShell.getDisplay();
            FillLayout fillLayout = new FillLayout();
            fillLayout.marginHeight=0;
            fillLayout.marginWidth=0;
            mShell.setLayout(fillLayout);

            ImageData imageData = new ImageData(Common.decodePath(settings.getSplashBMP()));
            mShell.setSize(imageData.width, imageData.height);
            Rectangle rect = mDisplay.getClientArea();
            int x = (rect.width-imageData.width)/2;
            int y = (rect.height-imageData.height)/2;
            mShell.setBounds(x,y,imageData.width,imageData.height);
            mState = STATE_FADE_IN;
            mTimeLeft = mFadeInDelay;

            mImage = new Image(mDisplay, imageData);
            Label l = new Label(mShell,SWT.NONE);
            l.setImage(mImage);
            if(mAdvSplash) {
                WinAPI.SetLayeredWindowAttributes(mShell.handle, 0, 0, 0, (mFadeInDelay > 0?0:255), WinAPI.LWA_ALPHA);
            }

            mAlpha = -1;
            mShell.open();
            mClip = loadAudioClip(Common.decodePath(settings.getSplashWAV()));
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

            if(mAdvSplash) {
                mDisplay.asyncExec(new Runnable(){
                    public void run(){
                        if(mAlpha != newAlpha) {
                            mAlpha = newAlpha;
                            WinAPI.SetLayeredWindowAttributes(mShell.handle, 0, 0, 0, mAlpha, WinAPI.LWA_ALPHA);
                        }
                    }
                });
            }
            mTimeLeft--;
        }
    }
}
