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

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class NSISEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, INSISPreferenceConstants
{
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
            {EclipseNSISPlugin.getResourceString("task.tags.label"),TASK_TAGS_STYLE}, //$NON-NLS-1$
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
    
    private List mSyntaxStyleList;
    private ColorEditor mSyntaxColorEditor;
    private Button mStyleBold;
    private Button mStyleItalic;

    private ColorEditor mMatchingDelimsColorEditor;

    private MasterSlaveController mMasterSlaveController;
    
    public NSISEditorPreferencePage() 
    {
        super();
        setDescription(EclipseNSISPlugin.getResourceString("editor.preferences.description")); //$NON-NLS-1$
        setPreferenceStore(NSISPreferences.getPreferences().getPreferenceStore());
        mPreferenceStore= new PreferenceStoreWrapper(getPreferenceStore());
        sortModels();
    }

    private void sortModels()
    {
        Comparator comparator = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                String[] first = (String[])o1;
                String[] second = (String[])o2;
                int n = first[0].compareToIgnoreCase(second[0]);
                if(n == 0) {
                    n = first[1].compareToIgnoreCase(second[1]);
                }
                return n;
            }
        };
        Arrays.sort(mSyntaxStyleListModel, comparator);
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
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_editorprefs_context"); //$NON-NLS-1$
    }

    private void handleSyntaxStyleListSelection()
    { 
        int i= mSyntaxStyleList.getSelectionIndex();
        String key= mSyntaxStyleListModel[i][1];
        NSISSyntaxStyle style = getStyle(key);
        mSyntaxColorEditor.setRGB(style.getForeground());
        mStyleBold.setSelection(style.isBold());
        mStyleItalic.setSelection(style.isItalic());
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
    
    private Control createAppearanceGroup(Composite parent)
    {
        Composite appearanceComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        String label = EclipseNSISPlugin.getResourceString("use.spaces"); //$NON-NLS-1$
        addCheckBox(appearanceComposite, label, USE_SPACES_FOR_TABS, 0);
        
        label= EclipseNSISPlugin.getResourceString("show.matching.delimiters"); //$NON-NLS-1$
        Button cb = addCheckBox(appearanceComposite, label, MATCHING_DELIMITERS, 0);
        Label l = new Label(appearanceComposite, SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("matching.delimiters.color")); //$NON-NLS-1$
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= 20;
        l.setLayoutData(gd);
        
        mMatchingDelimsColorEditor = new ColorEditor(appearanceComposite);
        Button button = mMatchingDelimsColorEditor.getButton();
        button.setLayoutData(new GridData());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                PreferenceConverter.setValue(mPreferenceStore, MATCHING_DELIMITERS_COLOR, mMatchingDelimsColorEditor.getRGB());
            }
        });
        mMasterSlaveController = new MasterSlaveController(cb);
        mMasterSlaveController.addSlave(l);
        mMasterSlaveController.addSlave(button);
        
        return appearanceComposite;
    }
    
    private Control createSyntaxGroup(Composite parent)
    {
        Composite syntaxComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(1, false); 
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
        
        mSyntaxStyleList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleSyntaxStyleListSelection();
            }
        });
        foregroundColorButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= mSyntaxStyleListModel[i][1];
                NSISSyntaxStyle style = getStyle(key);
                style.setForeground(mSyntaxColorEditor.getRGB());
                saveStyle(key, style);
            }
        });
        
        SelectionListener boldStyleListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean bold = mStyleBold.getSelection();
                
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= mSyntaxStyleListModel[i][1];
                if (key != null) {
                    NSISSyntaxStyle style = getStyle(key);
                    style.setBold(bold);
                    saveStyle(key, style);
                }
            }
        };
        mStyleBold = new Button(stylesComposite, SWT.CHECK);
        mStyleBold.setText(EclipseNSISPlugin.getResourceString("bold")); //$NON-NLS-1$
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        mStyleBold.setLayoutData(gd);
        mStyleBold.addSelectionListener(boldStyleListener);

        SelectionListener italicStyleListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean italic = mStyleItalic.getSelection();
                
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= mSyntaxStyleListModel[i][1];
                if (key != null) {
                    NSISSyntaxStyle style = getStyle(key);
                    style.setItalic(italic);
                    saveStyle(key, style);
                }
            }
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
        NSISTextUtility.hookSourceViewer(mPreviewer);
        SourceViewerConfiguration configuration= new NSISSourceViewerConfiguration(new ChainedPreferenceStore(new IPreferenceStore[]{mPreferenceStore,getPreferenceStore(), EditorsUI.getPreferenceStore()}));
        mPreviewer.configure(configuration);
        
        String content= new String(Common.loadContentFromStream(getClass().getResourceAsStream("NSISPreview.txt"))); //$NON-NLS-1$
        IDocument document= new Document(content);
        new NSISDocumentSetupParticipant().setup(document);
        mPreviewer.setDocument(document);
        mPreviewer.setEditable(false);
        
        return mPreviewer.getControl();
    }
    
    /*
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) 
    {
        parent = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);
        
        Link link= new Link(parent, SWT.NONE);
        link.setText(EclipseNSISPlugin.getResourceString("editor.preferences.note")); //$NON-NLS-1$
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.preferencePages.GeneralTextEditor", null, null); //$NON-NLS-1$
            }
        });
        
        Group group = new Group(parent,SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("appearances.group.label")); //$NON-NLS-1$
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(1,false));
        Control c = createAppearanceGroup(group);
        c.setLayoutData(new GridData(GridData.FILL_BOTH));
        group = new Group(parent,SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("syntax.group.label")); //$NON-NLS-1$
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setLayout(new GridLayout(1,false));
        c = createSyntaxGroup(group);
        c.setLayoutData(new GridData(GridData.FILL_BOTH));
        initialize();
        Dialog.applyDialogFont(parent);
        return parent;
    }
    
    private void initialize() {
        
        initializeFields();

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
    
    private void initializeFields() 
    {
        Iterator e= mCheckBoxes.keySet().iterator();
        while (e.hasNext()) {
            Button b= (Button) e.next();
            String key= (String) mCheckBoxes.get(b);
            b.setSelection(mPreferenceStore.getBoolean(key));
        }
        mMatchingDelimsColorEditor.setRGB(PreferenceConverter.getColor(mPreferenceStore, MATCHING_DELIMITERS_COLOR));
        mMasterSlaveController.updateSlaves();
        mStyleMap.clear();
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
}
