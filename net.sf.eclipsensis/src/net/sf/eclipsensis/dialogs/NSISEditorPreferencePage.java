/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.settings.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class NSISEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, INSISPreferenceConstants
{
    private final String[][] mAppearanceColorListModel= {
        {EclipseNSISPlugin.getResourceString("line.number.foreground.color"), LINE_NUMBER_RULER_COLOR, null}, //$NON-NLS-1$
        {EclipseNSISPlugin.getResourceString("current.line.highlight.color"), CURRENT_LINE_COLOR, null}, //$NON-NLS-1$
        {EclipseNSISPlugin.getResourceString("matching.delimiters.color"), MATCHING_DELIMITERS_COLOR, null}, //$NON-NLS-1$
        {EclipseNSISPlugin.getResourceString("print.margin.color"), PRINT_MARGIN_COLOR, null}, //$NON-NLS-1$
        {EclipseNSISPlugin.getResourceString("selection.foreground.color"), SELECTION_FOREGROUND_COLOR, SELECTION_FOREGROUND_DEFAULT_COLOR}, //$NON-NLS-1$
        {EclipseNSISPlugin.getResourceString("selection.background.color"), SELECTION_BACKGROUND_COLOR, SELECTION_BACKGROUND_DEFAULT_COLOR}, //$NON-NLS-1$
    };
    
    private final String[][] mSyntaxStyleListModel = {
            {EclipseNSISPlugin.getResourceString("comments.label"),COMMENTS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("strings.label"),STRINGS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("commands.label"),COMMANDS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("installer.attributes.label"),INSTALLER_ATTRIBUTES_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("compiletime.commands.label"),COMPILETIME_COMMANDS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("callbacks.label"),CALLBACKS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("instructions.label"),INSTRUCTIONS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("instruction.parameters.label"),INSTRUCTION_PARAMETERS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("instruction.options.label"),INSTRUCTION_OPTIONS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("symbols.label"),SYMBOLS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("predefined.variables.label"),PREDEFINED_VARIABLES_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("langstrings.label"),LANGSTRINGS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("userdefined.variables.label"),USERDEFINED_VARIABLES_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("numbers.label"),NUMBERS_STYLE} //$NON-NLS-1$
    };
    
    private HashMap mStyleMap = new HashMap();
    private PreferenceStoreWrapper mPreferenceStore;
    private NSISSourceViewer mPreviewer;
    
    private Map mCheckBoxes= new HashMap();
    private SelectionListener mCheckBoxListener= new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
        }
        public void widgetSelected(SelectionEvent e) {
            Button button= (Button) e.widget;
            mPreferenceStore.setValue((String) mCheckBoxes.get(button), button.getSelection());
        }
    };
    
    private Map mTextFields= new HashMap();
    private ModifyListener mTextFieldListener= new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            Text text= (Text) e.widget;
            mPreferenceStore.setValue((String) mTextFields.get(text), text.getText());
        }
    };

    private ArrayList mNumberFields= new ArrayList();
    private ModifyListener mNumberFieldListener= new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            numberFieldChanged((Text) e.widget);
        }
    };
    
    private List mAppearanceColorList;
    private ColorEditor mAppearanceColorEditor;
    private Button mAppearanceColorDefault;

    private List mSyntaxStyleList;
    private ColorEditor mSyntaxColorEditor;
    private Button mStyleBold;
    private Button mStyleItalic;

    private boolean mFieldsInitialized= false;
    private SelectionListener mAccessibilityListener;
    
    public NSISEditorPreferencePage() {
        setDescription(EclipseNSISPlugin.getResourceString("editor.preferences.description")); //$NON-NLS-1$
        setPreferenceStore(NSISPreferences.getPreferences().getPreferenceStore());
        mPreferenceStore= new PreferenceStoreWrapper(getPreferenceStore());
    }

    /*
     * @see IWorkbenchPreferencePage#init()
     */ 
    public void init(IWorkbench workbench) {
    }

    /*
     * @see PreferencePage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
//        WorkbenchHelp.setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
    }

    private void handleAppearanceColorListSelection()
    { 
        int i= mAppearanceColorList.getSelectionIndex();
        String key= mAppearanceColorListModel[i][1];
        RGB rgb= PreferenceConverter.getColor(mPreferenceStore, key);
        mAppearanceColorEditor.setRGB(rgb);      
        updateAppearanceColorWidgets(mAppearanceColorListModel[i][2]);
    }

    private void handleSyntaxStyleListSelection()
    { 
        int i= mSyntaxStyleList.getSelectionIndex();
        String key= mSyntaxStyleListModel[i][1];
        NSISSyntaxStyle style = getStyle(key);
        mSyntaxColorEditor.setRGB(style.mForeground);
        mStyleBold.setSelection(style.mBold);
        mStyleItalic.setSelection(style.mItalic);
    }
    
    private NSISSyntaxStyle getStyle(String key)
    {
        NSISSyntaxStyle style = (NSISSyntaxStyle)mStyleMap.get(key);
        if(style == null) {
            style = NSISSyntaxStyle.parse(mPreferenceStore.getString(key));
            mStyleMap.put(key,style);
        }
        return style;
    }

    private void updateAppearanceColorWidgets(String systemDefaultKey)
    {
        if (systemDefaultKey == null) {
            mAppearanceColorDefault.setSelection(false);
            mAppearanceColorDefault.setVisible(false);
            mAppearanceColorEditor.getButton().setEnabled(true);
        } else {
            boolean systemDefault= mPreferenceStore.getBoolean(systemDefaultKey);
            mAppearanceColorDefault.setSelection(systemDefault);
            mAppearanceColorDefault.setVisible(true);
            mAppearanceColorEditor.getButton().setEnabled(!systemDefault);
        }
    }
    
    private Control createAppearancePage(Composite parent)
    {
        Composite appearanceComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        String label= EclipseNSISPlugin.getResourceString("displayed.tab.width"); //$NON-NLS-1$
        addTextField(appearanceComposite, label, TAB_WIDTH, 3, 0, true);

        label= EclipseNSISPlugin.getResourceString("use.spaces"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, USE_SPACES_FOR_TABS, 0);
        
        label= EclipseNSISPlugin.getResourceString("print.margin.column"); //$NON-NLS-1$
        addTextField(appearanceComposite, label, PRINT_MARGIN_COLUMN, 3, 0, true);
                
        label= EclipseNSISPlugin.getResourceString("show.overview.ruler"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, OVERVIEW_RULER, 0);
                
        label= EclipseNSISPlugin.getResourceString("show.line.numbers"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, LINE_NUMBER_RULER, 0);

        label= EclipseNSISPlugin.getResourceString("highlight.current.line"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, CURRENT_LINE, 0);
                
        label= EclipseNSISPlugin.getResourceString("show.print.margin"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, PRINT_MARGIN, 0);
        
        label= EclipseNSISPlugin.getResourceString("show.matching.delimiters"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, MATCHING_DELIMITERS, 0);

        label= EclipseNSISPlugin.getResourceString("accessibility.disable.custom.carets"); //$NON-NLS-1$
        final Button customCaretButton= addCheckBox(appearanceComposite, label, USE_CUSTOM_CARETS, 0);

        label= EclipseNSISPlugin.getResourceString("accessibility.wide.caret"); //$NON-NLS-1$
        final Button wideCaretButton= addCheckBox(appearanceComposite, label, WIDE_CARET, 20);

        boolean customCaretState= mPreferenceStore.getBoolean(USE_CUSTOM_CARETS);
        wideCaretButton.setEnabled(customCaretState);
        
        mAccessibilityListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                wideCaretButton.setEnabled(customCaretButton.getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        customCaretButton.addSelectionListener(mAccessibilityListener);

        Label l= new Label(appearanceComposite, SWT.LEFT );
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 2;
        gd.heightHint= convertHeightInCharsToPixels(1) / 2;
        l.setLayoutData(gd);
        
        l= new Label(appearanceComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("appearance.options")); //$NON-NLS-1$
        gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 2;
        l.setLayoutData(gd);

        Composite editorComposite= new Composite(appearanceComposite, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        editorComposite.setLayout(layout);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        gd.horizontalSpan= 2;
        editorComposite.setLayoutData(gd);      

        mAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.heightHint= convertHeightInCharsToPixels(5);
        mAppearanceColorList.setLayoutData(gd);
                        
        Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        layout.numColumns= 2;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        l= new Label(stylesComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("color")); //$NON-NLS-1$
        gd= new GridData();
        gd.horizontalAlignment= GridData.BEGINNING;
        l.setLayoutData(gd);

        mAppearanceColorEditor= new ColorEditor(stylesComposite);
        Button foregroundColorButton= mAppearanceColorEditor.getButton();
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        foregroundColorButton.setLayoutData(gd);

        SelectionListener colorDefaultSelectionListener= new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                boolean systemDefault= mAppearanceColorDefault.getSelection();
                mAppearanceColorEditor.getButton().setEnabled(!systemDefault);
                
                int i= mAppearanceColorList.getSelectionIndex();
                String key= mAppearanceColorListModel[i][2];
                if (key != null)
                    mPreferenceStore.setValue(key, systemDefault);
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        
        mAppearanceColorDefault= new Button(stylesComposite, SWT.CHECK);
        mAppearanceColorDefault.setText(EclipseNSISPlugin.getResourceString("system.default")); //$NON-NLS-1$
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        mAppearanceColorDefault.setLayoutData(gd);
        mAppearanceColorDefault.setVisible(false);
        mAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);
        
        mAppearanceColorList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                handleAppearanceColorListSelection();
            }
        });
        foregroundColorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                int i= mAppearanceColorList.getSelectionIndex();
                String key= mAppearanceColorListModel[i][1];
                
                PreferenceConverter.setValue(mPreferenceStore, key, mAppearanceColorEditor.getRGB());
            }
        });
        
        return appearanceComposite;
    }
    
    private Control createSyntaxPage(Composite parent)
    {
        Composite syntaxComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 1;
        syntaxComposite.setLayout(layout);

        Label l= new Label(syntaxComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("syntax.options")); //$NON-NLS-1$
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 1;
        l.setLayoutData(gd);

        Composite listComposite= new Composite(syntaxComposite, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        listComposite.setLayout(layout);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        gd.horizontalSpan= 1;
        listComposite.setLayoutData(gd);      

        mSyntaxStyleList= new List(listComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
        gd.heightHint= convertHeightInCharsToPixels(5);
        gd.widthHint= convertWidthInCharsToPixels(30);
        mSyntaxStyleList.setLayoutData(gd);
                        
        Composite stylesComposite= new Composite(listComposite, SWT.NONE);
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        layout.numColumns= 2;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        l= new Label(stylesComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("color")); //$NON-NLS-1$
        gd= new GridData();
        gd.horizontalAlignment= GridData.BEGINNING;
        l.setLayoutData(gd);

        mSyntaxColorEditor= new ColorEditor(stylesComposite);
        Button foregroundColorButton= mSyntaxColorEditor.getButton();
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        foregroundColorButton.setLayoutData(gd);
        
        mSyntaxStyleList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                handleSyntaxStyleListSelection();
            }
        });
        foregroundColorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= mSyntaxStyleListModel[i][1];
                NSISSyntaxStyle style = getStyle(key);
                style.mForeground = mSyntaxColorEditor.getRGB();
                saveStyle(key, style);
            }
        });
        
        SelectionListener boldStyleListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                boolean bold = mStyleBold.getSelection();
                
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= mSyntaxStyleListModel[i][1];
                if (key != null) {
                    NSISSyntaxStyle style = getStyle(key);
                    style.mBold = bold;
                    saveStyle(key, style);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        mStyleBold = new Button(stylesComposite, SWT.CHECK);
        mStyleBold.setText(EclipseNSISPlugin.getResourceString("bold")); //$NON-NLS-1$
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        mStyleBold.setLayoutData(gd);
        mStyleBold.addSelectionListener(boldStyleListener);

        SelectionListener italicStyleListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                boolean italic = mStyleItalic.getSelection();
                
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= mSyntaxStyleListModel[i][1];
                if (key != null) {
                    NSISSyntaxStyle style = getStyle(key);
                    style.mItalic = italic;
                    saveStyle(key, style);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        mStyleItalic = new Button(stylesComposite, SWT.CHECK);
        mStyleItalic.setText(EclipseNSISPlugin.getResourceString("italic")); //$NON-NLS-1$
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        mStyleItalic.setLayoutData(gd);
        mStyleItalic.addSelectionListener(italicStyleListener);

        l= new Label(syntaxComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("preview")); //$NON-NLS-1$
        gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 1;
        l.setLayoutData(gd);

        Control previewer= createPreviewer(syntaxComposite);
        gd= new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan= 1;
        gd.widthHint= convertWidthInCharsToPixels(20);
        gd.heightHint= convertHeightInCharsToPixels(10);
        previewer.setLayoutData(gd);

        return syntaxComposite;
    }
    
    private void saveStyle(String key, NSISSyntaxStyle style)
    {
        mPreferenceStore.setValue(key, style.toString());
        if(mPreviewer != null && mPreviewer.mustProcessPropertyQueue()) {
            mPreviewer.processPropertyQueue();
        }
    }
    
    private Control createPreviewer(Composite parent)
    {
        mPreviewer= new NSISSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        SourceViewerConfiguration configuration= new NSISSourceViewerConfiguration(mPreferenceStore);
        mPreviewer.configure(configuration);
        Font font= JFaceResources.getFont(JFaceResources.TEXT_FONT);
        mPreviewer.getTextWidget().setFont(font);
        
        String content= loadPreviewContentFromFile("NSISPreview.txt"); //$NON-NLS-1$
        IDocument document= new Document(content);
        new NSISDocumentSetupParticipant().setup(document);
        mPreviewer.setDocument(document);
        mPreviewer.setEditable(false);
        
        return mPreviewer.getControl();
    }

    private String loadPreviewContentFromFile(String filename) {
        String line;
        String separator= System.getProperty("line.separator"); //$NON-NLS-1$
        StringBuffer buffer= new StringBuffer(512);
        BufferedReader reader= null;
        try {
            reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
            while ((line= reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(separator);
            }
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (reader != null) {
                try { 
                    reader.close();
                } 
                catch (IOException e) {}
            }
        }
        return buffer.toString();
    }
    
    /*
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        
        initializeDefaultColors();
        TabFolder folder = new TabFolder(parent, SWT.NONE);
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("appearances.tab.label")); //$NON-NLS-1$
        item.setControl(createAppearancePage(folder));
        item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("syntax.tab.label")); //$NON-NLS-1$
        item.setControl(createSyntaxPage(folder));
        initialize();
        Dialog.applyDialogFont(folder);
        return folder;
    }
    
    private void initialize() {
        
        initializeFields();
        
        for (int i= 0; i < mAppearanceColorListModel.length; i++) {
            mAppearanceColorList.add(mAppearanceColorListModel[i][0]);
        }
        mAppearanceColorList.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (mAppearanceColorList != null && !mAppearanceColorList.isDisposed()) {
                    mAppearanceColorList.select(0);
                    handleAppearanceColorListSelection();
                }
            }
        });

        for (int i= 0; i < mSyntaxStyleListModel.length; i++) {
            mSyntaxStyleList.add(mSyntaxStyleListModel[i][0]);
        }
        mSyntaxStyleList.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (mSyntaxStyleList != null && !mSyntaxStyleList.isDisposed()) {
                    mSyntaxStyleList.select(0);
                    handleSyntaxStyleListSelection();
                }
            }
        });
    }
    
    private void initializeFields() {
        
        Iterator e= mCheckBoxes.keySet().iterator();
        while (e.hasNext()) {
            Button b= (Button) e.next();
            String key= (String) mCheckBoxes.get(b);
            b.setSelection(mPreferenceStore.getBoolean(key));
        }
        
        e= mTextFields.keySet().iterator();
        while (e.hasNext()) {
            Text t= (Text) e.next();
            String key= (String) mTextFields.get(t);
            t.setText(mPreferenceStore.getString(key));
        }
        
        mStyleMap.clear();
        mFieldsInitialized= true;
        validateAllNumbers();
        
        mAccessibilityListener.widgetSelected(null);
    }
    
    private void initializeDefaultColors() {    
        if (!getPreferenceStore().contains(SELECTION_BACKGROUND_COLOR)) {
            RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
            PreferenceConverter.setDefault(mPreferenceStore, SELECTION_BACKGROUND_COLOR, rgb);
            PreferenceConverter.setDefault(getPreferenceStore(), SELECTION_BACKGROUND_COLOR, rgb);
        }
        if (!getPreferenceStore().contains(SELECTION_FOREGROUND_COLOR)) {
            RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB();
            PreferenceConverter.setDefault(mPreferenceStore, SELECTION_FOREGROUND_COLOR, rgb);
            PreferenceConverter.setDefault(getPreferenceStore(), SELECTION_FOREGROUND_COLOR, rgb);
        }
    }
    
    /*
     * @see PreferencePage#performOk()
     */
    public boolean performOk() {
        mPreferenceStore.update();
        NSISEditor.updatePresentations();
        return super.performOk();
    }
    
    /*
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaults() {

        mPreferenceStore.loadDefaults();
        
        initializeFields();

        handleAppearanceColorListSelection();

        handleSyntaxStyleListSelection();
        if(mPreviewer != null && mPreviewer.mustProcessPropertyQueue()) {
            mPreviewer.processPropertyQueue();
        }

        super.performDefaults();
    }
    
    /*
     * @see DialogPage#dispose()
     */
    public void dispose() {
        
        if (mPreferenceStore != null) {
            ((PreferenceStoreWrapper)mPreferenceStore).dispose();
            mPreferenceStore= null;
        }
        super.dispose();
    }
    
    private Button addCheckBox(Composite parent, String label, String key, int indentation) {       
        Button checkBox= new Button(parent, SWT.CHECK);
        checkBox.setText(label);
        
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        gd.horizontalSpan= 2;
        checkBox.setLayoutData(gd);
        checkBox.addSelectionListener(mCheckBoxListener);
        
        mCheckBoxes.put(checkBox, key);
        
        return checkBox;
    }
    
    private Control addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {
        
        Label labelControl= new Label(composite, SWT.NONE);
        labelControl.setText(label);
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        labelControl.setLayoutData(gd);
        
        Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);     
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
        textControl.setLayoutData(gd);
        textControl.setTextLimit(textLimit);
        mTextFields.put(textControl, key);
        if (isNumber) {
            mNumberFields.add(textControl);
            textControl.addModifyListener(mNumberFieldListener);
        } else {
            textControl.addModifyListener(mTextFieldListener);
        }
            
        return textControl;
    }
    
    private static void indent(Control control) {
        GridData gridData= new GridData();
        gridData.horizontalIndent= 20;
        control.setLayoutData(gridData);        
    }
    
    private void numberFieldChanged(Text textControl) {
        String number= textControl.getText();
        if(validatePositiveNumber(number, true)) {
            mPreferenceStore.setValue((String) mTextFields.get(textControl), number);
        }
    }
    
    private boolean validatePositiveNumber(String number, boolean showMessageBox) {
        if (number.length() == 0) {
            if(showMessageBox) {
                MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                        EclipseNSISPlugin.getResourceString("empty.input")); //$NON-NLS-1$
            }
            return false;
        } else {
            try {
                int value= Integer.parseInt(number);
                if (value < 0) {
                    if(showMessageBox) {
                        MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                                MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.input"),  //$NON-NLS-1$
                                                                     new Object[]{number})); //$NON-NLS-1$
                    }
                    return false;
                }
            } catch (NumberFormatException e) {
                if(showMessageBox) {
                    MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                            MessageFormat.format(EclipseNSISPlugin.getResourceString("invalid.input"),  //$NON-NLS-1$
                                                 new Object[]{number})); //$NON-NLS-1$
                }
                return false;
            }
        }
        return true;
    }
    
    private void validateAllNumbers()
    {
        if (!mFieldsInitialized) {
            return;
        }
        
        for (int i= 0; i < mNumberFields.size(); i++) {
            Text text= (Text) mNumberFields.get(i);
            if(!validatePositiveNumber(text.getText(),false)) {
                text.setText(""); //$NON-NLS-1$
            }
        }
    }
}
