/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.help.INSISKeywordsListener;
import net.sf.eclipsensis.help.NSISKeywords;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * A content outline page which always represents the content of the connected
 * editor in 10 segments.
 */
public class NSISContentOutlinePage extends ContentOutlinePage implements INSISKeywordsListener
{
    private Object mInput;
    private NSISEditor mEditor;
    private boolean mDisposed = false;

    /**
     * Creates a content outline page using the given provider and the given
     * editor.
     */
    public NSISContentOutlinePage(NSISEditor editor)
    {
        super();
        mEditor = editor;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.help.INSISKeywordsListener#keywordsChanged()
     */
    public void keywordsChanged()
    {
        TreeViewer viewer = getTreeViewer();
        if(viewer != null) {
            IContentProvider contentProvider = viewer.getContentProvider();
            if(contentProvider != null && contentProvider instanceof NSISOutlineContentProvider) {
                NSISOutlineContentProvider.loadOutlineKeywordsAndImages();
                Object input = viewer.getInput();
                viewer.setInput(null);
                viewer.setInput(input);
            }
        }
    }
    
    /*
     * (non-Javadoc) Method declared on ContentOutlinePage
     */
    public void createControl(Composite parent)
    {

        super.createControl(parent);
        if(mEditor != null) {
            NSISOutlineContentProvider contentProvider = mEditor.getOutlineContentProvider();
            if(contentProvider != null) {
                TreeViewer viewer = getTreeViewer();
                viewer.setContentProvider(contentProvider);
                viewer.setLabelProvider(new NSISOutlineLabelProvider());
                viewer.addSelectionChangedListener(mEditor);
                if (mInput != null) {
                    viewer.setInput(mInput);
                }
                Point sel = mEditor.getSelectedRange();
                NSISOutlineElement element = contentProvider.findElement(sel.x,sel.y);
                if(element != null) {
                    setSelection(new StructuredSelection(element));
                }
                NSISKeywords.addKeywordsListener(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose()
    {
        super.dispose();
        if(mEditor != null) {
            getTreeViewer().removeSelectionChangedListener(mEditor);
            NSISOutlineContentProvider provider = mEditor.getOutlineContentProvider();
            if(provider != null) {
                provider.inputChanged(null, mEditor.getEditorInput());
            }
        }
        NSISKeywords.removeKeywordsListener(this);
        mDisposed = true;
    }
    
    public boolean isDisposed()
    {
        return mDisposed;
    }
    
    /**
     * Sets the input of the outline page
     */
    public void setInput(Object input)
    {
        mInput = input;
        update();
    }

    /**
     * Updates the outline page.
     */
    public void update()
    {
        TreeViewer viewer = getTreeViewer();

        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.setRedraw(false);
                viewer.setInput(mInput);
                viewer.expandAll();
                control.setRedraw(true);
            }
        }
    }

    /**
     * @return Returns the textEditor.
     */
    ITextEditor getTextEditor()
    {
        return mEditor;
    }
}