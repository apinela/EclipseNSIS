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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISDocumentSetupParticipant;
import net.sf.eclipsensis.editor.template.*;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class NSISTemplatePreferencePage extends TemplatePreferencePage
{
    private EclipseNSISPlugin mPlugin = null;
    private NSISTemplateVariableProcessor mTemplateProcessor;
    
    /**
     * 
     */
    public NSISTemplatePreferencePage()
    {
        mPlugin = EclipseNSISPlugin.getDefault();
        setPreferenceStore(mPlugin.getPreferenceStore());
        setTemplateStore(mPlugin.getTemplateStore());
        setContextTypeRegistry(mPlugin.getContextTypeRegistry());
        mTemplateProcessor= new NSISTemplateVariableProcessor();
    }

    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#isShowFormatterSetting()
     */
    protected boolean isShowFormatterSetting() 
    {
        return false;
    }
    
    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
     */
    protected String getFormatterPreferenceKey() 
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
     */
    protected SourceViewer createViewer(Composite parent)
    {
        final NSISTemplateSourceViewer viewer= new NSISTemplateSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        SourceViewerConfiguration configuration= new NSISTemplateSourceViewerConfiguration(getPreferenceStore());
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
        
        IDocument document= new Document();
        new NSISDocumentSetupParticipant().setup(document);
        viewer.setDocument(document);
        viewer.setEditable(false);
        
        return viewer;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createTemplateEditDialog(org.eclipse.jface.text.templates.Template, boolean, boolean)
     */
    protected Dialog createTemplateEditDialog(Template template, boolean edit, boolean isNameModifiable)
    {
        return new NSISTemplateEditorDialog(getShell(),template, edit, isNameModifiable);
    }
    
    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() 
    {
        boolean ok= super.performOk();
        
        mPlugin.savePluginPreferences();
        
        return ok;
    }
}
