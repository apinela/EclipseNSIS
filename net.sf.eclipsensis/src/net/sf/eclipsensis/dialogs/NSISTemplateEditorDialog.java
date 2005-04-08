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
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISDocumentSetupParticipant;
import net.sf.eclipsensis.editor.template.NSISTemplateEditorSourceViewerConfiguration;
import net.sf.eclipsensis.editor.template.NSISTemplateSourceViewer;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.templates.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class NSISTemplateEditorDialog extends StatusMessageDialog
{
    private final Template mTemplate;
    
    private Text mNameText = null;
    private Text mDescriptionText = null;
    private Combo mContextCombo = null;
    private SourceViewer mPatternEditor = null;    
    private Button mInsertVariableButton = null;
    private boolean mIsNameModifiable = false;

    private DialogStatus mValidationStatus = new DialogStatus(IStatus.OK,""); //$NON-NLS-1$
    private Map mGlobalActions= new HashMap(10);
    private List mSelectionActions = new ArrayList(3);  
    private String[][] mContextTypes = null;
    
    private ContextTypeRegistry mContextTypeRegistry; 
    
    private TemplateContextType mTemplateContextType = null;
        
    /**
     * Creates a new dialog.
     * 
     * @param parent the shell parent of the dialog
     * @param template the template to edit
     * @param edit whether this is a new template or an existing being edited
     * @param isNameModifiable whether the name of the template may be modified
     * @param registry the context type registry to use
     */
    public NSISTemplateEditorDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable) 
    {
        super(parent);
        
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);

        mTemplate= template;
        mIsNameModifiable= isNameModifiable;
        
        mContextTypeRegistry= EclipseNSISPlugin.getDefault().getContextTypeRegistry();
        
        List contexts= new ArrayList();
        for (Iterator it= mContextTypeRegistry.contextTypes(); it.hasNext();) {
            TemplateContextType type= (TemplateContextType) it.next();
            contexts.add(new String[] { type.getId(), type.getName() });
        }
        mContextTypes= (String[][]) contexts.toArray(new String[contexts.size()][]);
                
        mTemplateContextType = mContextTypeRegistry.getContextType(template.getContextTypeId());
    }
    
    /**
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(EclipseNSISPlugin.getResourceString((Common.isEmpty(mTemplate.getName())?"new.template.dialog.title": //$NON-NLS-1$
                                                                                           "edit.template.dialog.title"))); //$NON-NLS-1$
    }

    public void create() 
    {
        super.create();
        if(mPatternEditor != null && Common.isEmpty(mPatternEditor.getTextWidget().getText())) {
            mValidationStatus = new DialogStatus(IStatus.ERROR,EclipseNSISPlugin.getResourceString("template.error.no.pattern")); //$NON-NLS-1$
        }

        if (mNameText != null && !mNameText.isDisposed() && Common.isEmpty(mNameText.getText())) {
            IStatus status = new DialogStatus(IStatus.ERROR,EclipseNSISPlugin.getResourceString("template.error.no.name")); //$NON-NLS-1$
            updateButtonsEnableState(status);
        }
    }

    protected Control createControl(Composite parent) 
    {
        Composite composite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        
        ModifyListener listener= new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                doTextWidgetChanged(e.widget);
            }
        };
        
        if (mIsNameModifiable) {
            createLabel(composite, EclipseNSISPlugin.getResourceString("template.name.label")); //$NON-NLS-1$ 
            
            Composite composite3= new Composite(composite, SWT.NONE);
            composite3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            layout= new GridLayout();       
            layout.numColumns= 3;
            layout.marginWidth= 0;
            layout.marginHeight= 0;
            composite3.setLayout(layout);
            
            mNameText= createText(composite3);
            mNameText.addModifyListener(listener);
            mNameText.addFocusListener(new FocusListener() {

                public void focusGained(FocusEvent e) {
                }

                public void focusLost(FocusEvent e) {
                    updateButtons();
                }
            });
            
            createLabel(composite3, EclipseNSISPlugin.getResourceString("template.context.label")); //$NON-NLS-1$       
            mContextCombo= new Combo(composite3, SWT.READ_ONLY);
    
            for (int i= 0; i < mContextTypes.length; i++) {
                mContextCombo.add(mContextTypes[i][1]);
            }
    
            mContextCombo.addModifyListener(listener);
        }
        
        createLabel(composite, EclipseNSISPlugin.getResourceString("template.description.label")); //$NON-NLS-1$      
        
        int descFlags= mIsNameModifiable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY;
        mDescriptionText= new Text(composite, descFlags );
        mDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
        
        mDescriptionText.addModifyListener(listener);

        Label patternLabel= createLabel(composite, EclipseNSISPlugin.getResourceString("template.pattern.label")); //$NON-NLS-1$
        patternLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        mPatternEditor= createEditor(composite);
        
        Label filler= new Label(composite, SWT.NONE);      
        filler.setLayoutData(new GridData());
        
        Composite composite3= new Composite(composite, SWT.NONE);
        layout= new GridLayout();       
        layout.marginWidth= 0;
        layout.marginHeight= 0;
        composite3.setLayout(layout);        
        composite3.setLayoutData(new GridData());
        
        mInsertVariableButton= new Button(composite3, SWT.NONE);
        mInsertVariableButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mInsertVariableButton.setText(EclipseNSISPlugin.getResourceString("template.insert.variable.label")); //$NON-NLS-1$
        mInsertVariableButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                mPatternEditor.getTextWidget().setFocus();
                mPatternEditor.doOperation(NSISTemplateSourceViewer.INSERT_TEMPLATE_VARIABLE);          
            }

            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        mDescriptionText.setText(mTemplate.getDescription());
        if (mIsNameModifiable) {
            mNameText.setText(mTemplate.getName());
            mNameText.addModifyListener(listener);
            mContextCombo.select(getIndex(mTemplate.getContextTypeId()));
        } else {
            mPatternEditor.getControl().setFocus();
        }
        initializeActions();

        applyDialogFont(composite);
        WorkbenchHelp.setHelp(composite,INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_templatedlg_context"); //$NON-NLS-1$
        return composite;
    }
    
    private void doTextWidgetChanged(Widget w) 
    {
        if (w == mNameText) {
            String name= mNameText.getText();
            mTemplate.setName(name);
            updateButtons();            
        } 
        else if (w == mContextCombo) {
            String name= mContextCombo.getText();
            String contextId= getContextId(name);
            mTemplate.setContextTypeId(contextId);
            mTemplateContextType = mContextTypeRegistry.getContextType(contextId);
        } 
        else if (w == mDescriptionText) {
            String desc= mDescriptionText.getText();
            mTemplate.setDescription(desc);
        }   
    }
    
    private String getContextId(String name) 
    {
        if (name == null) {
            return name;
        }
        
        for (int i= 0; i < mContextTypes.length; i++) {
            if (name.equals(mContextTypes[i][1])) {
                return mContextTypes[i][0]; 
            }
        }
        return name;
    }

    private void doSourceChanged(IDocument document) 
    {
        String text= document.get();
        mTemplate.setPattern(text);
        if(Common.isEmpty(text)) {
            mValidationStatus.setError(EclipseNSISPlugin.getResourceString("template.error.no.pattern")); //$NON-NLS-1$
        }
        else {
            mValidationStatus.setOK();
            TemplateContextType contextType= mContextTypeRegistry.getContextType(mTemplate.getContextTypeId());
            if (contextType != null) {
                try {
                    contextType.validate(text);
                } catch (TemplateException e) {
                    mValidationStatus.setError(e.getLocalizedMessage());
                }
            }
        }

        updateUndoAction();
        updateButtons();
    }   

    private static Label createLabel(Composite parent, String name) 
    {
        Label label= new Label(parent, SWT.NULL);
        label.setText(name);
        label.setLayoutData(new GridData());

        return label;
    }

    private static Text createText(Composite parent) 
    {
        Text text= new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));     
        
        return text;
    }

    private SourceViewer createEditor(Composite parent) 
    {
        SourceViewer viewer= createViewer(parent);

        IDocument document= new Document(mTemplate.getPattern());
        new NSISDocumentSetupParticipant().setup(document);
        viewer.setDocument(document);
        viewer.setEditable(true);
        
        int nLines= document.getNumberOfLines();
        if (nLines < 5) {
            nLines= 5;
        } 
        else if (nLines > 12) {
            nLines= 12; 
        }
                
        Control control= viewer.getControl();
        GridData data= new GridData(GridData.FILL_BOTH);
        data.widthHint= convertWidthInCharsToPixels(80);
        data.heightHint= convertHeightInCharsToPixels(nLines);
        control.setLayoutData(data);
        
        viewer.addTextListener(new ITextListener() {
            public void textChanged(TextEvent event) {
                if (event .getDocumentEvent() != null)
                    doSourceChanged(event.getDocumentEvent().getDocument());
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {            
            public void selectionChanged(SelectionChangedEvent event) {
                updateSelectionDependentActions();
            }
        });

        viewer.prependVerifyKeyListener(new VerifyKeyListener() {
            public void verifyKey(VerifyEvent event) {
                handleVerifyKeyPressed(event);
            }
        });
        
        return viewer;
    }
    
    /**
     * Creates the viewer to be used to display the pattern. Subclasses may override.
     * 
     * @param parent the parent composite of the viewer
     * @return a configured <code>SourceViewer</code>
     */
    private SourceViewer createViewer(Composite parent) 
    {
        final SourceViewer viewer= new NSISTemplateSourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        SourceViewerConfiguration configuration= new NSISTemplateEditorSourceViewerConfiguration(NSISPreferences.getPreferences().getPreferenceStore(),
                                                        mTemplateContextType);
        final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
        viewer.getTextWidget().setFont(fontRegistry.get(INSISPreferenceConstants.EDITOR_FONT));
        final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                viewer.getTextWidget().setFont(fontRegistry.get(INSISPreferenceConstants.EDITOR_FONT));
            }
        };
        fontRegistry.addListener(propertyChangeListener);
        viewer.getTextWidget().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                fontRegistry.removeListener(propertyChangeListener);
            }
        });
        viewer.configure(configuration);
        return viewer;
    }

    private void handleVerifyKeyPressed(VerifyEvent event) 
    {
        if (!event.doit) {
            return;
        }
        if (event.stateMask != SWT.MOD1) {
            return;
        }
        switch (event.character) {
            case ' ':
                mPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
                event.doit= false;
                break;

            // CTRL-Z
            case 'z' - 'a' + 1:
                mPatternEditor.doOperation(ITextOperationTarget.UNDO);
                event.doit= false;
                break;              
        }
    }

    private void initializeActions() 
    {
        TextViewerAction action= new TextViewerAction(mPatternEditor, ITextOperationTarget.UNDO);
        action.setText(EclipseNSISPlugin.getResourceString("undo.action.name")); //$NON-NLS-1$
        mGlobalActions.put(ITextEditorActionConstants.UNDO, action);

        action= new TextViewerAction(mPatternEditor, ITextOperationTarget.CUT);
        action.setText(EclipseNSISPlugin.getResourceString("cut.action.name")); //$NON-NLS-1$
        mGlobalActions.put(ITextEditorActionConstants.CUT, action);

        action= new TextViewerAction(mPatternEditor, ITextOperationTarget.COPY);
        action.setText(EclipseNSISPlugin.getResourceString("copy.action.name")); //$NON-NLS-1$
        mGlobalActions.put(ITextEditorActionConstants.COPY, action);

        action= new TextViewerAction(mPatternEditor, ITextOperationTarget.PASTE);
        action.setText(EclipseNSISPlugin.getResourceString("paste.action.name")); //$NON-NLS-1$
        mGlobalActions.put(ITextEditorActionConstants.PASTE, action);

        action= new TextViewerAction(mPatternEditor, ITextOperationTarget.SELECT_ALL);
        action.setText(EclipseNSISPlugin.getResourceString("selectall.action.name")); //$NON-NLS-1$
        mGlobalActions.put(ITextEditorActionConstants.SELECT_ALL, action);

        action= new TextViewerAction(mPatternEditor, ISourceViewer.CONTENTASSIST_PROPOSALS);
        action.setText(EclipseNSISPlugin.getResourceString("content.assist.proposal.label")); //$NON-NLS-1$
        mGlobalActions.put("ContentAssistProposal", action); //$NON-NLS-1$

        action= new TextViewerAction(mPatternEditor, NSISTemplateSourceViewer.INSERT_TEMPLATE_VARIABLE);
        action.setText(EclipseNSISPlugin.getResourceString("insert.template.variable.proposal.label")); //$NON-NLS-1$
        mGlobalActions.put("InsertTemplateVariableProposal", action); //$NON-NLS-1$

        mSelectionActions.add(ITextEditorActionConstants.CUT);
        mSelectionActions.add(ITextEditorActionConstants.COPY);
        mSelectionActions.add(ITextEditorActionConstants.PASTE);
        
        // create context menu
        MenuManager manager= new MenuManager(null, null);
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillContextMenu(mgr);
            }
        });

        StyledText text= mPatternEditor.getTextWidget();        
        Menu menu= manager.createContextMenu(text);
        text.setMenu(menu);
    }

    private void fillContextMenu(IMenuManager menu) 
    {
        menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_UNDO));
        menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, (IAction) mGlobalActions.get(ITextEditorActionConstants.UNDO));
        
        menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));     
        menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) mGlobalActions.get(ITextEditorActionConstants.CUT));
        menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) mGlobalActions.get(ITextEditorActionConstants.COPY));
        menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) mGlobalActions.get(ITextEditorActionConstants.PASTE));
        menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) mGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));
        menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) mGlobalActions.get("ContentAssistProposal")); //$NON-NLS-1$ //$NON-NLS-2$

        menu.add(new Separator("templates")); //$NON-NLS-1$
        menu.appendToGroup("templates", (IAction) mGlobalActions.get("InsertTemplateVariableProposal")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void updateSelectionDependentActions() 
    {
        Iterator iterator= mSelectionActions.iterator();
        while (iterator.hasNext())
            updateAction((String)iterator.next());      
    }

    private void updateUndoAction() 
    {
        IAction action= (IAction) mGlobalActions.get(ITextEditorActionConstants.UNDO);
        if (action instanceof IUpdate) {
            ((IUpdate) action).update();
        }
    }

    private void updateAction(String actionId) 
    {
        IAction action= (IAction) mGlobalActions.get(actionId);
        if (action instanceof IUpdate) {
            ((IUpdate) action).update();
        }
    }

    private int getIndex(String contextid) 
    {
        if (contextid == null) {
            return -1;
        }
        
        for (int i= 0; i < mContextTypes.length; i++) {
            if (contextid.equals(mContextTypes[i][0])) {
                return i;   
            }
        }
        return -1;
    }
    
    private void updateButtons() 
    {      
        DialogStatus status;

        boolean valid= mNameText == null || mNameText.getText().trim().length() != 0;
        if (!valid) {
            status = new DialogStatus(IStatus.ERROR,EclipseNSISPlugin.getResourceString("template.error.no.name")); //$NON-NLS-1$
        } 
        else {
            status= mValidationStatus; 
        }
        updateStatus(status);
    }
    
    private static class TextViewerAction extends Action implements IUpdate 
    {
        private int mOperationCode= -1;
        private ITextOperationTarget mOperationTarget;
    
        /** 
         * Creates a new action.
         * 
         * @param viewer the viewer
         * @param operationCode the opcode
         */
        public TextViewerAction(ITextViewer viewer, int operationCode) 
        {
            mOperationCode= operationCode;
            mOperationTarget= viewer.getTextOperationTarget();
            update();
        }
    
        /**
         * Updates the enabled state of the action.
         * Fires a property change if the enabled state changes.
         * 
         * @see Action#firePropertyChange(String, Object, Object)
         */
        public void update() 
        {
            boolean wasEnabled= isEnabled();
            boolean isEnabled= (mOperationTarget != null && mOperationTarget.canDoOperation(mOperationCode));
            setEnabled(isEnabled);
    
            if (wasEnabled != isEnabled) {
                firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE, isEnabled ? Boolean.TRUE : Boolean.FALSE);
            }
        }
        
        /**
         * @see Action#run()
         */
        public void run() 
        {
            if (mOperationCode != -1 && mOperationTarget != null) {
                mOperationTarget.doOperation(mOperationCode);
            }
        }
    }
}
